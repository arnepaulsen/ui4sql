/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import router.SessionMgr;
import services.ServicesException;
import forms.*;
import db.*;

/**
 * Closure Plugin
 * 
 * Change log:
 * 
 * 9/14/2005 - project analysis
 * 8/22/06 - handle sqlserver null dates
 * 
 * general user-supplied information about success of project
 * and
 * statistics of 
 *  - change request count
 *  - defect count
 *  - risk realization totals vs possible
 *  - function point totals vs planned
 */

public class ClosurePlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	
	
	
	public ClosurePlugin() throws services.ServicesException {
		super();

		this.setContextSwitchOk (false);

		this.setTableName("tclosure");
		this.setKeyName("closure_id");
		this.setTargetTitle("Closure Analysis");
		
		this.setDeleteLevel("Executive");
		
		this.setListHeaders (new String[] { "Project", "Completed", "Size/Days",
				"Priority", "Owner" });

		// columns after last header are not shown! used to match list filters
		// only
		this.setMoreListColumns (new String[] { "project_name",
				"actual_end_date", "act_days_no",
				"p.code_desc as priority_desc",
				"concat(u.last_name, ',', u.first_name)", "s.code_value",
				"p.code_value", "tproject.pm_uid" });

		this.setMoreListJoins (new String[] {
				" left join tproject on tclosure.project_id = tproject.project_id ",
				" left join tcodes s on tclosure.accessment_cd = s.code_value and s.code_type_id  = 67 ",
				" left join tcodes p on tproject.priority_cd = p.code_value and p.code_type_id  = 55 ",
				" left join tuser u on tproject.pm_uid = u.user_id " });

		this.setMoreSelectJoins (new String[] {
				" left join tproject on tclosure.project_id = tproject.project_id",
				" left join tpoint on tproject.project_id = tpoint.project_id ",
				" left join tprocess on tproject.process_id = tprocess.process_id",
				" left join tcommitment on tproject.commitment_id = tcommitment.commitment_id" });

		this.setMoreSelectColumns (new String[] { "tproject.project_name",
				"tprocess.title_nm as ProcessName",
				"tpoint.total_points_no as totalPoints",
				"tcommitment.title_nm as CommitmentName" });

	}



	/*
	 * Get some statistics before adding, and save into tclosure
	 */

	public boolean beforeAdd(Hashtable ht) {

		/*
		 * total defects to tclosure
		 */
		String sQuery = "select project_id , count(*) as the_count from tdefect "
				+ " where project_id  = "
				+ sm.Parm("project_id")
				+ " group by project_id";

		setTotal(sQuery, ht, "defect_cnt");

		/*
		 * total change requests
		 */

		sQuery = "select project_id , count(*) as the_count from tchange_request "
				+ " where project_id  = "
				+ sm.Parm("project_id")
				+ " group by project_id";

		setTotal(sQuery, ht, "change_cnt");

		/*
		 * total realized losses
		 */

		sQuery = "select project_id , sum(loss_amt) as the_count from trisk "
				+ " where project_id  = " + sm.Parm("project_id")
				+ " group by project_id";

		setTotal(sQuery, ht, "realized_loss_amt");
		
		return true;

	}

	/*
	 * Runs a query to get result, and put a DbField in the HT
	 */

	private void setTotal(String query, Hashtable ht, String fieldName) {

		try {
			ResultSet rs = db.getRS(query);

			// check if complete, or deliverable join is not found
			while (rs.next() == true) {
				int x = rs.getInt("the_count");
				ht
						.put(fieldName, new DbFieldInteger(fieldName,
								new Integer(x)));

			}
			rs.close();
		} catch (ServicesException e) {

		} catch (SQLException e) {

		}
	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 1 || columnNumber == 3 || columnNumber == 4)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		String[][] closeDates = { { "1", "2", "3" },
				{ "Last 1 Month", "Last 3 Months", "Last 6 Months" } };

		switch (columnNumber) {

		case 1: {
			debug("return date selector");
			WebFieldSelect wf = new WebFieldSelect("FilterDates", sm
					.Parm("FilterDates"), closeDates);
			wf.setDisplayClass("listform");
			wf.setSelectPrompt("All Close Dates");
			debug("done date selecto");
			return wf;
		}

		case 3:

		{

			WebFieldSelect wf = new WebFieldSelect("FilterPriority", sm
					.Parm("FilterPriority"), sm.getCodes("PRIORITY"),
					"All Priorities");
			wf.setDisplayClass("listform");
			return wf;

		}

		case 4:
		default: {

			WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
					"FilterUser").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterUser"))), sm.getUserHT(),
					"All Project Managers");
			wf.setDisplayClass("listform");
			return wf;

		}
		}

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present

		/*
		 * Filter date ranges
		 */

		String  qry_end_date ;
		
		if (sm.isSQLServer()) qry_end_date  = " isDate(actual_end_date) = 1 ";
		else qry_end_date = "actual_end_date <> '0000-00-00'";
		
		// 1=last month
		if (sm.Parm("FilterDates").equalsIgnoreCase("1")) {
			sb
					.append(" AND DATEDIFF(tproject.actual_end_date,now()) > -30 and " + qry_end_date);

		}
		// 2=last 3 months
		if (sm.Parm("FilterDates").equalsIgnoreCase("2")) {
			sb
					.append(" AND DATEDIFF(tproject.actual_end_date,now()) > -90 and " + qry_end_date);

		}
		// 3=last 6 months
		if (sm.Parm("FilterDates").equalsIgnoreCase("3")) {
			sb
					.append(" AND DATEDIFF(tproject.actual_end_date,now()) > -180 and " + qry_end_date);

		}

		/*
		 * filter Priority
		 * 
		 */

		if ((!sm.Parm("FilterPriority").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterPriority").length() > 0)) {
			sb
					.append(" AND p.code_value = '" + sm.Parm("FilterPriority")
							+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND tproject.pm_uid = " + sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		// container for the web fields
		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = sm.getUserHT();

		/*
		 * sm will cache on first call, so sql is only run first
		 */

		if (addMode) {
			ht.put("project_id", new WebFieldSelect("project_id", addMode ? sm
					.getProjectId() : (Integer) db.getObject("project_id"), sm
					.getProjectFilter(), true));
		} else {
			ht.put("project_id", new WebFieldDisplay("project_id", db
					.getText("project_name")));
		}

		/*
		 * Computed fields during add mode, only show at display time
		 */

		if (!addMode) {
			// these are computed during add mode... redundant data!
			ht.put("defect_cnt", new WebFieldDisplay("defect_cnt", db
					.getText("defect_cnt")));
			ht.put("change_cnt", new WebFieldDisplay("change_cnt", db
					.getText("change_cnt")));

			ht.put("realized_loss_amt", new WebFieldDisplay(
					"realized_loss_amt", db.getText("realized_loss_amt")));

			// this one is joined from tpoint table..
			ht.put("point_cnt", new WebFieldDisplay("point_", db
					.getText("totalPoints")));

		}

		/*
		 * Strings
		 */

		ht.put("summary_tx", new WebFieldString("summary_tx", addMode ? "" : db
				.getText("summary_tx"), 64, 100));

		/*
		 * Id's
		 */
		ht.put("process_id", new WebFieldDisplay("process_id", db
				.getText("ProcessName")));

		ht.put("commitment_id", new WebFieldDisplay("commitment_id", db
				.getText("CommitmentName")));

		/*
		 * codes
		 * 
		 */
		ht.put("accessment_cd", new WebFieldSelect("accessment_cd",
				addMode ? "New" : db.getText("accessment_cd"), sm
						.getCodes("CLOSURE")));

		/*
		 * blobs
		 */
		ht.put("accessment_blob", new WebFieldText("accessment_blob",
				addMode ? "" : db.getText("accessment_blob"), 3, 80));

		ht.put("success_blob", new WebFieldText("success_blob", addMode ? ""
				: db.getText("success_blob"), 3, 80));

		ht.put("failure_blob", new WebFieldText("failure_blob", addMode ? ""
				: db.getText("failure_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		/*
		 * save the old status code... do not put any under-scores in the web
		 * name so it won't get treated as a db update column
		 */

		return ht;

	}

}
