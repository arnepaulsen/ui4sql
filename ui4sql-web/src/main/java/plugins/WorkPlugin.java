/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import services.ExcelWriter;
import forms.*;

import java.util.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

/**
 * 
 * Bridges Sub-Project Work 
 * 
 * a way to create light-weight projects under the umbrella of a larger project.
 * 
 * keywords:
 * 
 * Notes... this table is a view of two tables. the audit fields are in the
 * second table. you can't update fields in both tables, so you have to just
 * update the main table first, then use the new-row-key to insert the second
 * table audit fields.
 * 
 * to do : merge two tables into one,
 * 
 * 
 * 
 * Change Log:
 * 
 * 
 * 11/14/07 - don't default the Review filter to "yes", show 'no' value as well.
 * 4/4/8 - default the selection on list page for EpicReview = y and priority =
 * y or n
 * 
 * 9/10/08 - add Ad-Hoc #
 * 
 * 10/17/09 = add Excel
 * 
 */
public class WorkPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructors
	 * 
	 **************************************************************************/

	private static SimpleDateFormat sdf_out = new SimpleDateFormat("MM/dd/yyyy");

	String outDate = sdf_out.format(new Date());

	public WorkPlugin() throws services.ServicesException {
		super();
		this.setTableName("twork"); // twork
		this.setKeyName("work_id");
		this.setTargetTitle("Work Effort");
		this.setListOrder("work_id");
		this.setShowAuditSubmitApprove(false);
		this.setGotoOk(true);
		this.setExcelOk(true);

		this.setGotoDisplayName("Log #: ");
		this.setGotoKeyName("work_id");

	}

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		this.setListHeaders(new String[] { "ID#", "Title", "Status",
				"Priority", "Review", "Suite", "Builder", "Interface" });

		this
				.setMoreListJoins(new String[] {
						" left join tinterface i  on twork.interface_id = i.interface_id",
						" left join tuser ac on twork.owner_uid = ac.user_id",
						" left join tcodes priority on twork.priority_cd = priority.code_value and priority.code_type_id =  7 ",
						" left join tcodes status on twork.status_cd = status.code_value and status.code_type_id =  101 ",
						" left join tcodes suite on twork.suite_cd = suite.code_value and suite.code_type_id =  95 ",
						" left join tcodes review  on twork.review_cd = review.code_value and review.code_type_id =  3 ", });

		this
				.setMoreListColumns(new String[] {
						"work_id",
						"twork.title_nm as work_title",
						"status.code_desc as stat_desc",
						"priority.code_desc as pri_desc",
						"review.code_desc as review_yn ",
						"suite.code_desc as suite_desc",
						"concat(ac.last_name , ',' , substring(ac.first_name,1,1) ) as theName",
						"i.title_nm as intr_title" });

	}

	// create Excel from ResultSet and save to the path in the web.xml config
	// file
	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "BridgesWork.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName().replace(" ", "")
				+ "_Bridges_Queue_";
		int columns = 6;
		short startRow = 1;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);

	}

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vexcelwork WHERE 1=1 ");

		if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
			sb.append(" AND Status  LIKE '" + sm.Parm("FilterStatus") + "%'");
		}

		// filter review date
		if (sm.Parm("FilterReview").length() > 0) {
			if (!sm.Parm("FilterReview").equalsIgnoreCase("0")) {
				sb.append(" AND review_date = '" + sm.Parm("FilterReview")
						+ "'");
			}
		}

		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
		}
		return rs;
	}

	// force the user back to the list page if they enter an invalid 'goto' cr
	// 
	public String getDataFormName() {

		if (sm.Parm("Action").equalsIgnoreCase("goto")
				&& rowId.equals(new Integer("-1"))) {
			return "list";
		}
		return "";
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

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2)
			return getListSelector("FilterStatus", "O", "All Status", sm
					.getCodes("STATUS1"));

		if (columnNumber == 3)
			return getListSelector("FilterPriority", "HM", "All Priorities", sm
					.getCodes("PRIORITYCOMBO"));

		if (columnNumber == 4)
			return getListSelector("FilterReview", "Y", "Review Any", sm
					.getCodes("YESNO"));

		if (columnNumber == 5)
			return getListSelector("FilterSuite", "", "All Suites", sm
					.getCodes("SUITES"));

		if (columnNumber == 6) {
			// bridges ac

			Hashtable bridges_ac = sm
					.getTable(
							"bridges_ac",
							"select concat(last_name , ',' , substring(first_name,1,1) ) as last_name , user_id, concat(last_name , ',' , substring(first_name,1,1)) as ac_name from tuser where default_appl_id = 13  order by last_name ");

			WebFieldSelect wf = new WebFieldSelect("FilterAC", (sm.Parm(
					"FilterAC").length() == 0 && !sm.Parm("FilterAC")
					.equalsIgnoreCase("0")) ? sm.getUserId() : new Integer(sm
					.Parm("FilterAC")), bridges_ac, "All Bridges A/C");

			wf.setDisplayClass("listform");
			return wf;
		}

		if (columnNumber == 7) {
			// bridges interface

			Hashtable interfaces = sm.getTable("tinterface",
					"select title_nm, interface_id, title_nm from tinterface  "
							+ " order by 1 ");

			return getListSelector("FilterInterface", new Integer("0"),
					"All Interfaces", interfaces);
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

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND status.code_value = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND status.code_value = '"
						+ sm.Parm("FilterStatus") + "'");
			}
		}

		// filter on priority.... tricky one.. has HM for high+medium priorities
		if (sm.Parm("FilterPriority").length() == 0) {
			sb
					.append(" AND (priority.code_value = 'H' OR priority.code_value = 'M') ");

		} else {
			if (!sm.Parm("FilterPriority").equalsIgnoreCase("0")) {

				if (sm.Parm("FilterPriority").equalsIgnoreCase("HM"))
					sb
							.append(" AND (priority.code_value = 'H' OR priority.code_value = 'M') ");
				else

					sb.append(" AND priority.code_value = '"
							+ sm.Parm("FilterPriority") + "'");
			}
		}

		// filter on suite
		if (sm.Parm("FilterSuite").length() == 0) {
		} else {
			if (!sm.Parm("FilterSuite").equalsIgnoreCase("0")) {
				sb.append(" AND suite.code_value = '" + sm.Parm("FilterSuite")
						+ "'");
			}
		}

		// filter on review flag

		if (sm.Parm("FilterReview").length() == 0) {
			sb.append(" AND review.code_value = 'Y'");
		} else {
			if (!sm.Parm("FilterReview").equalsIgnoreCase("0")) {
				sb.append(" AND review.code_value = '"
						+ sm.Parm("FilterReview") + "'");
			}
		}

		// filter on bridges a/c

		if (sm.Parm("FilterAC").length() == 0) {
			sb.append(" and twork.owner_uid = " + sm.getUserId().toString());
		} else {
			if (!sm.Parm("FilterAC").equalsIgnoreCase("0"))
				sb.append(" AND twork.owner_uid = " + sm.Parm("FilterAC"));

		}

		// filter on interface
		if (sm.Parm("FilterInterface").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterInterface").equalsIgnoreCase("0")) {
				sb.append(" AND twork.interface_id = "
						+ sm.Parm("FilterInterface"));
			}
		}

		return sb.toString();
	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 */

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Ids
		 */

		Hashtable interfaces = sm
				.getTable(
						"tinterface",
						"select title_nm, interface_id, title_nm from tinterface  "
								+ " union "
								+ " select ' Multiple' as 'a', 0 as 'b' , ' Multiple' as 'c' "
								+ " order by 1 ");

		Hashtable bridges = sm
				.getTable(
						"bridges_ac",
						"select concat(last_name , ',' , substring(first_name,1,1)) as last_name , user_id, "
								+ "concat(last_name , ',' , substring(first_name,1,1)) as ac_name from tuser where default_appl_id = 13  order by last_name ");

		ht.put("workid", new WebFieldDisplay("workid", addMode ? "" : db
				.getInteger("work_id").toString()));

		ht.put("interface_id", new WebFieldSelect("interface_id",
				addMode ? new Integer("0") : db.getInteger("interface_id"),
				interfaces, true));

		ht.put("owner_uid", new WebFieldSelect("owner_uid", addMode ? sm
				.getUserId() : db.getInteger("owner_uid"), bridges, true));

		// ht.put("owner_id",
		// new WebFieldSelect("owner_id", addMode ? new Integer("0")
		// : (Integer) db.getObject("owner_id"), sm.getUserHT()));
		/*
		 * Codes
		 */

		// status_cd is defined as a char(10), and it failes to compare to
		// varchar(1), so substring it.
		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd").substring(0, 1), sm.getCodes("STATUS1")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		ht.put("severity_cd", new WebFieldSelect("severity_cd", addMode ? ""
				: db.getText("severity_cd"), sm.getCodes("PRIORITY")));

		ht.put("review_cd", new WebFieldSelect("review_cd", addMode ? "" : db
				.getText("review_cd"), sm.getCodes("YESNO")));

		ht.put("patient_safety_cd", new WebFieldSelect("patient_safety_cd",
				addMode ? "" : db.getText("patient_safety_cd"), sm
						.getCodes("YESNO")));

		ht.put("regulatory_cd", new WebFieldSelect("regulatory_cd",
				addMode ? "" : db.getText("regulatory_cd"), sm
						.getCodes("YESNO")));

		ht.put("workaround_cd", new WebFieldSelect("workaround_cd",
				addMode ? "" : db.getText("workaround_cd"), sm
						.getCodes("YESNO")));

		ht.put("suite_cd", new WebFieldSelect("suite_cd", addMode ? "OP" : db
				.getText("suite_cd"), sm.getCodes("SUITES")));

		ht.put("release_cd", new WebFieldSelect("release_cd", addMode ? "" : db
				.getText("release_cd"), sm.getCodes("KRLSE")));

		String[][] prod_dev = { { "P", "D" }, { "Production", "Development" } };

		ht.put("development_cd", new WebFieldSelect("development_cd",
				addMode ? "D" : db.getText("development_cd"), prod_dev));

		/*
		 * Dates
		 */

		ht
				.put("target_close_dt", new WebFieldString("target_close_dt",
						addMode ? "" : trimDate(db.getText("target_close_dt")),
						10, 10));

		/*
		 * Strings
		 */

		ht.put("est_hrs_tx", new WebFieldString("est_hrs_tx", (addMode ? ""
				: db.getText("est_hrs_tx")), 32, 64));

		ht.put("mrn_count_tx", new WebFieldString("mrn_count_tx", (addMode ? ""
				: db.getText("mrn_count_tx")), 32, 64));

		ht.put("user_impact_tx", new WebFieldString("user_impact_tx",
				(addMode ? "" : db.getText("user_impact_tx")), 32, 64));

		ht.put("frequency_tx", new WebFieldString("frequency_tx", (addMode ? ""
				: db.getText("frequency_tx")), 32, 64));

		ht.put("assessment_tx", new WebFieldString("assessment_tx",
				(addMode ? "" : db.getText("assessment_tx")), 32, 64));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 128));

		ht.put("epic_contact_tx", new WebFieldString("epic_contact_tx",
				(addMode ? "" : db.getText("epic_contact_tx")), 24, 24));

		ht.put("owner_tx", new WebFieldString("owner_tx", (addMode ? "" : db
				.getText("owner_tx")), 24, 24));

		/*
		 * numbers
		 */
		ht.put("rfc_no", new WebFieldString("rfc_no", (addMode ? "" : db
				.getText("rfc_no")), 8, 8));

		ht.put("ra_no", new WebFieldString("ra_no", (addMode ? "" : db
				.getText("ra_no")), 8, 8));

		ht.put("defect_no", new WebFieldString("defect_no", (addMode ? "" : db
				.getText("defect_no")), 8, 8));

		ht.put("incident_no", new WebFieldString("incident_no", (addMode ? ""
				: db.getText("incident_no")), 8, 8));

		ht.put("cr_no", new WebFieldString("cr_no", (addMode ? "" : db
				.getText("cr_no")), 4, 4));

		ht.put("adhoc_no", new WebFieldString("adhoc_no", (addMode ? "" : db
				.getText("adhoc_no")), 4, 4));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 8, 120));

		ht.put("tasks_blob", new WebFieldText("tasks_blob", addMode ? "" : db
				.getText("tasks_blob"), 8, 120));

		ht.put("remediation_blob", new WebFieldText("remediation_blob",
				addMode ? "" : db.getText("remediation_blob"), 5, 120));

		ht.put("history_blob", new WebFieldText("history_blob", addMode ? ""
				: db.getText("history_blob"), 5, 120));

		ht.put("cause_blob", new WebFieldText("cause_blob", addMode ? "" : db
				.getText("cause_blob"), 5, 120));

		ht.put("alternative_blob", new WebFieldText("alternative_blob",
				addMode ? "" : db.getText("alternative_blob"), 5, 120));

		ht.put("notes_tx", new WebFieldText("notes_tx", addMode ? "" : db
				.getText("notes_tx"), 6, 120));

		ht.put("issues_blob", new WebFieldText("issues_blob", addMode ? "" : db
				.getText("issues_blob"), 6, 120));

		ht.put("impact_blob", new WebFieldText("impact_blob", addMode ? "" : db
				.getText("impact_blob"), 6, 120));

		ht.put("scope_blob", new WebFieldText("scope_blob", addMode ? "" : db
				.getText("scope_blob"), 6, 120));

		/*
		 * Return ht
		 */

		return ht;

	}

	private String trimDate(String theDate) {

		try {
			debug("trimDate" + theDate);

			if (theDate == null)
				return "";

			if (theDate.length() < 10)
				return theDate.substring(0, theDate.length());

			if (theDate.equalsIgnoreCase("01/01/1900 12:00"))
				return "";

			return theDate.substring(0, 10);
		} catch (Exception e) {
			return "";
		}

	}

}
