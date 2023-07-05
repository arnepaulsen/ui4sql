/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import router.SessionMgr;
import forms.*;
import java.util.Hashtable;
import java.util.Date;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import services.ExcelWriter;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * 
 * Display a list page for Message Metrics from tmessage_stat
 * 
 * Uniqueness: First plugin to use 'listOnly', so there is no Form display or
 * template First plugin to include summary totals as the last line of the list
 * page, it was ugly.
 * 
 * 
 * Change Log:
 * 
 * 10/2/07 - Force a filter of date > today if no filters are specified
 * 
 * 6/16/09 - Can't get the vlist_metric to work on MySQL... doen't like the
 * union clause.
 * 
 * 9/23/09 - Fix sql query date formats
 * 
 * 10/13/10 - standardize view name, set listOnly = true
 * 
 * 
 */
public class MetricPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructors
	 * 
	 **************************************************************************/

	public MetricPlugin() throws services.ServicesException {
		super();
		this.setTableName("tmessage_stat");
		this.setKeyName("message_stat_id");
		this.setTargetTitle("Metrics");
		this.setListOrder("i.reference_nm, activity_date");
		this.setShowAuditSubmitApprove(false);
		this.setEditOk(true);
		this.setExcelOk(true);
		this.setListOnly(true);
		

	}

	public String getListTitle() {

		// Force in today's date on the 'From Date' filter if we have no filters
		// selected
		String fromDateFilter = "";

		if (sm.Parm("FilterFromDate").length() == 0) {

			SimpleDateFormat sdf_to_query = new SimpleDateFormat("MM/dd/yyyy");
			Date d = new Date();
			fromDateFilter = sdf_to_query.format(d);

		} else
			fromDateFilter = sm.Parm("FilterFromDate");

		return "Interface Metrics"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Start:&nbsp;<input type='Text' name='FilterFromDate' id='FilterFromDate' maxlength='11' size='11' value="
				+ fromDateFilter
				+ ">&nbsp;<a href=\"javascript:NewCal('FilterFromDate','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;End:&nbsp;&nbsp;<input type='Text' name='FilterToDate' id='FilterToDate' maxlength='11' size='11'value="
				+ sm.Parm("FilterToDate")
				+ ">&nbsp;<a href=\"javascript:NewCal('FilterToDate','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>";
	}

	public Hashtable getList() throws services.ServicesException {

		StringBuffer sb = new StringBuffer();

		sb
				.append("select  'Metric' as target, message_stat_id, "
						+ "concat( i.reference_nm ,' ' , i.title_nm) as reference_nm , "
						+ dbprefix
						+ "FormatDateTime(activity_date, 'mm/dd/yy') as activity_disp, "
						+ "	message_tx, instance_cd, c.code_desc as direction_tx, count_no,i.reference_nm from tmessage_stat ");

		for (int s = 0; s < moreListJoins.length; s++) {
			sb.append(moreListJoins[s]);
		}

		sb.append(" WHERE 1 = 1 " + getListAnd());

		sb
				.append(" UNION select 'Total', 0, 'Total', ' ', ' ', ' ', ' ', sum(count_no), ''  FROM tmessage_stat ");

		for (int s = 0; s < moreListJoins.length; s++) {
			sb.append(moreListJoins[s]);
		}

		sb.append(" WHERE 1 = 1 " + getListAnd());

		// sb.append(" ORDER BY 1,2, 3");

		return db.getList("tmessage_stat", "message_stat_id", sb.toString());

	}

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		this.setListHeaders(new String[] { "Interface", "Date", "Message",
				"Instance", "Direction", "Count" });

		this
				.setMoreListJoins(new String[] {
						" left join tinterface i  on tmessage_stat.interface_id = i.interface_id",
						" left join tcodes c on i.direction_cd = c.code_value and c.code_type_id  = 21 ", });

	}

	public boolean listOnly() {
		return true;
	}

	public boolean getListColumnCenterOn(int columnNumber) {
		if (columnNumber == 0)
			return true;
		else
			return false;
	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	// all columns have selections
	public boolean listColumnHasSelector(int columnNumber) {
		return (columnNumber == 0 || columnNumber == 2 || columnNumber == 3 || columnNumber == 4);
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 4) {
			return getListSelector("FilterDirection", "0", "Direction", sm
					.getCodes("Direction"));
		}

		if (columnNumber == 3) {

			// get our own table in order to omit the 'All' value.
			Hashtable instances = sm
					.getTable(
							"tinstances",
							"select code_value, code_value, code_value from tcodes  where code_type_id =107 and code_value <> 'AA'");

			return getListSelector("FilterInstance", "", "All Instance",
					instances);

		}

		// message
		if (columnNumber == 2) {

			Hashtable messages = sm
					.getTable("tmessages",
							"select reference_nm, message_id, reference_nm from tmessage");

			WebFieldSelect wf = new WebFieldSelect("FilterMessage", (sm.Parm(
					"FilterMessage").length() == 0 && !sm.Parm("FilterMessage")
					.equalsIgnoreCase("0")) ? new Integer("0") : new Integer(sm
					.Parm("FilterMessage")), messages, "All Msg Types");

			wf.setDisplayClass("listform");
			return wf;
		}

		// interface
		if (columnNumber == 0) {

			Hashtable interfaces = sm
					.getTable(
							"tinterface_names1",
							"select concat(reference_nm , '  ' , title_nm) as ref_nm, interface_id, "
									+ "concat(reference_nm , '  ' , title_nm) as title_nm  from tinterface order by ref_nm ");

			WebFieldSelect wf = new WebFieldSelect(
					"FilterInterface",
					(sm.Parm("FilterInterface").length() == 0 && !sm.Parm(
							"FilterInterface").equalsIgnoreCase("0")) ? new Integer(
							"0")
							: new Integer(sm.Parm("FilterInterface")),
					interfaces, "All Interfaces");

			wf.setDisplayClass("listform");
			return wf;
		}

		// will never get here

		return getListSelector("dummy", new Integer(""), "badd..",
				new Hashtable());

	}

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
		 */

		StringBuffer sb = new StringBuffer();

		// filter on direction
		if (sm.Parm("FilterDirection").length() == 0) {

		}

		else {
			if (!sm.Parm("FilterDirection").equalsIgnoreCase("0")) {
				sb.append(" AND direction_cd = '" + sm.Parm("FilterDirection")
						+ "'");
			}
		}

		// default status to open if no filter present
		if (sm.Parm("FilterInstance").length() == 0) {
			sb.append(" AND instance_cd = 'CA'");
		}

		else {
			if (!sm.Parm("FilterInstance").equalsIgnoreCase("0")) {
				sb.append(" AND instance_cd = '" + sm.Parm("FilterInstance")
						+ "'");
			}
		}

		// filter on interface
		if (sm.Parm("FilterInterface").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterInterface").equalsIgnoreCase("0")) {
				sb.append(" AND tmessage_stat.interface_id = "
						+ sm.Parm("FilterInterface"));
			}
		}

		// filter on message type
		if (sm.Parm("FilterMessage").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterMessage").equalsIgnoreCase("0")) {
				sb.append(" AND tmessage_stat.message_id = "
						+ sm.Parm("FilterMessage"));
			}
		}


		if (sm.Parm("FilterFromDate").length() > 0) {

			try {
				SimpleDateFormat sdf_from_browser = new SimpleDateFormat(
						"MM/dd/yyyy");

				SimpleDateFormat sdf_to_query = new SimpleDateFormat("yyyy/MM/dd");

				Date d = sdf_from_browser.parse(sm.Parm("FilterFromDate"));
				// debug ("date from browser: " + d.toString());
				String dateQuery = sdf_to_query.format(d);
				// debug("date to query : " + dateQuery.toString());

				sb.append(" AND activity_date >= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("Message Stats :  error parsing install FROM date : "
						+ sm.Parm("FilterFromDate"));
			}
		}

		if (sm.Parm("FilterToDate").length() > 0) {

			try {
				SimpleDateFormat sdf_from_browser = new SimpleDateFormat(
						"MM/dd/yyyy");

				SimpleDateFormat sdf_to_query = new SimpleDateFormat("yyyy/MM/dd");

				Date d = sdf_from_browser.parse(sm.Parm("FilterToDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND activity_date <= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("Message stats - error parsing install TO date : "
						+ sm.Parm("FilterToDate"));
			}
		}

		// Check to make sure there is at least one filter

		// force in a from date = today if none specified

		if (emptyFilters()) {

			SimpleDateFormat sdf_to_query = new SimpleDateFormat("yyyy/MM/dd");

			Date today = new Date();
			String dateQuery = sdf_to_query.format(today);

			sb.append(" AND activity_date >= '" + dateQuery + "'");

		}

		return sb.toString();
	}

	// return true if there are no filters selected
	private boolean emptyFilters() {

		if (sm.Parm("FilterToDate").length() == 0
				&& sm.Parm("FilterFromDate").length() == 0
				&& sm.Parm("FilterMessage").length() == 0
				&& sm.Parm("FilterInterface").length() == 0
				&& sm.Parm("FilterDirection").length() == 0)
			return true;
		else
			return false;

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 */

	// this page has to detail form.
	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		return ht;

	}

	/*
	 * Excel Interface
	 */

	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "metric.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName().replace(" ", "") + "_Metrics";
		short startRow = 1;
		int columns = 7;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);

	}

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vexcelmetric WHERE 1=1 ");

		SimpleDateFormat sdf_from_browser = new SimpleDateFormat("MM/dd/yyyy");
		
		SimpleDateFormat sdf_to_query;
		
		if (sm.isSQLServer()) {
			sdf_to_query = new SimpleDateFormat("yyyy/MM/dd");
		}
		else {
			sdf_to_query = new SimpleDateFormat("yyyy/MM/dd");
				
		}

		if (sm.Parm("FilterFromDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterFromDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND activity_date >= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("MetricPlugin  - error parsing FROM date : "
						+ sm.Parm("FilterFromDate"));
			}
		}

		if (sm.Parm("FilterToDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterToDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND activity_date <= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("MetricPlugin - error parsing TO date : "
						+ sm.Parm("FilterToDate"));
			}
		}

		// Interface id
		if (sm.Parm("FilterInterface").length() > 0) {
			if (!sm.Parm("FilterInterface").equalsIgnoreCase("0"))

				sb.append(" AND interface_id  = " + sm.Parm("FilterInterface"));
		}

		// Instance
		if (sm.Parm("FilterInstance").length() == 0) {
		} else {
			if (!sm.Parm("FilterInstance").equalsIgnoreCase("0")) {
				sb
						.append(" AND Instance = '" + sm.Parm("FilterInstance")
								+ "'");
			}
		}

		// Direction
		if (sm.Parm("FilterDirection").length() == 0) {
		} else {
			if (!sm.Parm("FilterDirection").equalsIgnoreCase("0")) {
				sb.append(" AND InOut = '" + sm.Parm("FilterDirection") + "'");
			}
		}

		// Direction
		if (sm.Parm("FilterMessage").length() == 0) {
		} else {
			if (!sm.Parm("FilterMessage").equalsIgnoreCase("0")) {
				sb.append(" AND message_id = " + sm.Parm("FilterMessage"));
			}
		}

		sb.append(" ORDER BY Interface_No, Date");
		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

}
