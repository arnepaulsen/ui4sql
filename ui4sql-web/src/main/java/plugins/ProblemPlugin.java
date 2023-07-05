/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Change Request Plugin
 * 
 * 3/22 New Page
 */

public class ProblemPlugin extends AbsProjectPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public ProblemPlugin() throws services.ServicesException {
		super();
		this.setTableName("tproblem");
		this.setKeyName("problem_id");
		this.setListHeaders (listColumnHeaders);
		this.setTargetTitle("Problem Report");
		this.moreListColumns = extraListColumns;
		this.moreListJoins = extraListJoins;
	}

	// *******************
	// List Page
	// *******************

	private String listColumnHeaders[] = { "Title", "Type", "Status",
			"Priority", "Assigned" };

	private String extraListColumns[] = { "title_nm", "probtype.code_desc as prob_type",
			"stat.code_desc as status_desc", "pri.code_desc as pri_desc ",
			"concat(u.last_name, ',', u.first_name)" };

	private String extraListJoins[] = {
			" left join tcodes probtype on tproblem.type_cd = probtype.code_value and probtype.code_type_id  = 78 ",
			" left join tcodes stat on tproblem.status_cd = stat.code_value and stat.code_type_id  = 45 ",
			" left join tcodes pri on tproblem.priority_cd = pri.code_value and pri.code_type_id  = 7 ",
			" left join tuser u on tproblem.assigned_to_uid = u.user_id " };

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
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

		if (columnNumber == 2) {
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "O" : sm
					.Parm("FilterStatus")), sm.getCodes("ISSUESTAT"),
					"All Status");
				wf.setDisplayClass("listform");
			return wf;
		} else {
			if (columnNumber == 3) {

				WebFieldSelect wf = new WebFieldSelect("FilterPriority", sm
						.Parm("FilterPriority"), sm.getCodes("PRIORITY"),
						"All Priorities");
				wf.setDisplayClass("listform");
				return wf;
			} else {

				WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
						"FilterUser").length() == 0 ? new Integer("0")
						: new Integer(sm.Parm("FilterUser"))), sm.getUserHT(),
						"All Assignees");
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
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND stat.code_value <> 'CMP'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND stat.code_value = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		if ((!sm.Parm("FilterPriority").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterPriority").length() > 0)) {
			sb.append(" AND pri.code_value = '" + sm.Parm("FilterPriority")
					+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND tproblem.assigned_to_uid = "
					+ sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	// *******************
	// Web Page Display
	// *******************

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();
		
		/*
		 * Id's
		 */
		ht.put("initiated_by_uid", new WebFieldSelect("initiated_by_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("initiated_by_uid"), sm.getUserHT()));

		ht.put("assigned_to_uid", new WebFieldSelect("assigned_to_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_to_uid"), sm.getUserHT()));

		/*
		 * Dates
		 */
		ht.put("discover_date", new WebFieldDate("discover_date",
				addMode ? "" : db.getText("discover_date")));

		ht.put("required_date", new WebFieldDate("required_date", addMode ? ""
				: db.getText("required_date")));

		ht.put("resolved_date", new WebFieldDate("resolved_date",
				addMode ? "" : db.getText("resolved_date")));

		ht.put("assigned_date", new WebFieldDate("assigned_date",
				addMode ? "" : db.getText("assigned_date")));

		ht.put("received_date", new WebFieldDate("received_date", addMode ? ""
				: db.getText("received_date")));
		/*
		 * Strings
		 * 
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32));

		ht.put("problem_rpt_nm", new WebFieldString("problem_rpt_nm",
				(addMode ? "" : db.getText("problem_rpt_nm")), 32, 32));

		/*
		 * Codes
		 */

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("PROBLEMTYPE"), true));
		
		ht.put("status_cd", new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("ISSUESTAT")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd",
				addMode ? "" : db.getText("priority_cd"), sm
						.getCodes("PRIORITY")));

		ht.put("severity_cd", new WebFieldSelect("severity_cd",
				addMode ? "" : db.getText("severity_cd"), sm
						.getCodes("SEVERITY")));

		/*
		 * Blobs
		 */

		ht.put("problem_cause", new WebFieldString("problem_cause",
				(addMode ? "" : db.getText("problem_cause")), 32, 32));

		ht.put("blob_problem_desc", new WebFieldText("blob_problem_desc",
				addMode ? "" : db.getText("blob_problem_desc"), 5, 100));

		ht.put("blob_findings", new WebFieldText("blob_findings",
				addMode ? "" : db.getText("blob_findings"), 5, 100));

		ht.put("blob_resolution", new WebFieldText("blob_resolution",
				addMode ? "" : db.getText("blob_resolution"), 5, 100));

		ht.put("blob_disposition", new WebFieldText("blob_disposition",
				addMode ? "" : db.getText("blob_disposition"), 3, 60));

		/*
		 * Return
		 */

		return ht;

	}

}
