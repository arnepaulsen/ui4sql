/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import router.SessionMgr;

import java.util.Date;
import java.util.Hashtable;

import db.DbField;
import forms.*;

/**
 * 
 *   2/15 added mySql
 *  3/10 use tcodes instead of zproject_status_codes
 *  6/20 add where for division_id on list query 
 * */

/**
 * Projects
 * 
 * May seem odd that the projects data manager sub-classes the Division data
 * manager. This allows projects to be filtered by divsion, and only show those
 * projects in that division that the user is permitted to.
 * 
 * 
 * 8/21/06 - add logic for SQL-Server - handle null in date, mysql is 0000-00-00
 * 11/7/06 - Change list selector from Stage to Owner.. more important! 1/12/97
 * - Add links to Defect, Incident, Change Request, RFC, etc.
 * 
 * 8/21/07 use user-defined fuction concat2
 * 
 * 9/21/09 Fix Project-Selector page (list page when changing projects) . only
 * show 'wip' projects and filter on user division . fix list titles
 * 
 * 6/14/10 Remove obsolete concat2 function
 * 
 */

public class ProjectPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * CONSTRUCTORS
	 * 
	 **************************************************************************/

	public static String listColumnHeaders[] = { "Name", "Status", "Rank", "Type", "Application", "Owner", "Status",
			"Approved" };

	public ProjectPlugin() throws services.ServicesException {
		super();
	}

	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection

		this.setTableName("tproject");
		this.setKeyName("project_id");
		this.setTargetTitle("Projects");
		this.setListOrder("rank");
		this.setIsRootTable(false);
		this.setListHeaders(listColumnHeaders);
		this.setListOrder("Project_Name");

		/*
		 * link to other logs, such as Incident, Defect, Release (design) Note, Reqeust
		 * For Change
		 */

		this.setMoreSelectColumns(new String[] { "menu_cd", " trfc.title_nm as rfc_title ",
				" treleasenote.title_nm as note_title ", " tincident.title_nm as incident_title ",
				" tdefect." + "title_nm as defect_title", " tadhoc.title_nm as adhoc_title", " tadhoc.adhoc_id",
				" tepic_cr.cr_id", " tepic_cr.title_nm as cr_title" });

		this.setMoreSelectJoins(new String[] {
				" left join tuser_project on tproject.project_id = tuser_project.project_id ",
				" join tprocess on tproject.process_id = tprocess.process_id ",
				" left join trfc on tproject.rfc_id = trfc.rfc_id and tproject.rfc_id != 0 ",
				" left join tepic_cr on tproject.cr_no = tepic_cr.cr_no and tproject.cr_no != 0 ",
				" left join tdefect on tproject.defect_id = tdefect.defect_id and tproject.defect_id != 0 ",
				" left join tadhoc on tproject.ra_no = tadhoc.ra_no and tadhoc.ra_no != 0 ",
				" left join treleasenote on tproject.release_note_id = treleasenote.release_note_id and treleasenote.release_note_id != 0 ",
				" left join tincident on tproject.incident_id = tincident.incident_id and tincident.incident_id != 0 " });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not

		if (sm.Parm("ReturnTo").length() > 2) {
			return false;
		}

		if (columnNumber == 3 || columnNumber == 4 || columnNumber == 5 || columnNumber == 6)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public String listBgColor(int columnNumber, String value, DbField[] fields) {

		if (columnNumber < 900) {
			if (value != null)
				return "bgcolor=" + fields[10].getText();
			else
				return "";
		} else
			return "";
	}

	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {

		case 3: {

			WebFieldSelect wf = new WebFieldSelect("FilterType",
					sm.Parm("FilterType").length() == 0 ? "" : sm.Parm("FilterType"), sm.getCodes("PROJTYPE"), "Type?");
			wf.setDisplayClass("listform");
			return wf;
		}

		case 4: {

			return getListSelector("FilterApplication", sm.Parm("FilterApplication").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterApplication")), "Application ?", sm.getApplicationFilter());

		}

		// Proj. Manager
		case 5: {

			return getListSelector("FilterPM", new Integer("0"), "PM ?", sm.getUserHT());

		}
		// Project Status

		case 6:
		default: {

			WebFieldSelect wf = new WebFieldSelect("FilterStatus",
					sm.Parm("FilterStatus").length() == 0 ? "WIP" : sm.Parm("FilterStatus"), sm.getCodes("STATUS"),
					"Status?");
			wf.setDisplayClass("listform");
			return wf;

		}

		}
	}

	// list queries must have the table Target as first column, rowKey
	// as
	// second... then fields
	public String getListQuery() {

		// sql server:
		// approved_query = " CASE isDate(tproject.reviewed_date) WHEN 1 THEN 'Yes' ELSE
		// 'No' END as 'Approved_desc' ";

		return "select 'Project' as target, tproject.project_id,  project_name, progress.code_desc as Progress_desc, "
				+ "concat('&nbsp;&nbsp;', tproject.priority_cd) as rank, type.code_desc as type_desc, "
				+ " tapplications.application_name ,pm.last_name as last_name, " + " stat.code_desc as StatDesc,  "
				+ " if (tproject.reviewed_date =  '0000-00-00' , 'No' ,  'Yes' )  as 'Approved_desc' "
				+ " , progress.code_desc2" + " from tproject "
				+ " left join tapplications on tproject.primary_application_id = tapplications.application_id "
				+ " left join tuser pm on tproject.pm_uid = pm.user_id "
				+ " left join tdivision on tproject.division_id = tdivision.division_id "
				+ " left join tcodes as stat on tproject.status_cd = stat.code_value and stat.code_type_id = 5 "
				+ " left join tcodes as type on tproject.type_cd = type.code_value and type.code_type_id = 110 "
				+ " left join tcodes as progress on tproject.progress_cd = progress.code_value and progress.code_type_id = 71 "
				+ " left join tprocess on tproject.process_id = tprocess.process_id  WHERE 1=1 ";

	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		sb.append(" and tdivision.division_id = " + sm.getDivisionId().toString());

		if (sm.Parm("ReturnTo").length() > 2) {
			return sb.toString() + " and stat.code_value  = 'WIP' ";
		}

		/*
		 * Add in filters from list page
		 */

		// default status to WIP if no filter present

		if (sm.Parm("FilterType").length() > 1) {

			sb.append(" AND tproject.type_cd= '" + sm.Parm("FilterType") + "'");

		}

		// don't filter if picking context

		if (sm.Parm("FilterApplication").length() == 0) {
			// sb.append(" AND tproject.primary_application_id = "
			// + sm.getApplicationId().toString());
		} else {
			if (!sm.Parm("FilterApplication").equalsIgnoreCase("0")) {
				sb.append(" AND tproject.primary_application_id = " + sm.Parm("FilterApplication") + " ");
			}
		}

		// default status to WIP if no filter present
		if (sm.Parm("FilterStatus").length() == 0 && sm.Parm("ReturnTo").length() == 0) {
			// sb.append(" AND stat.code_value = 'WIP'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0") && sm.Parm("ReturnTo").length() == 0) {
				sb.append(" AND stat.code_value = '" + sm.Parm("FilterStatus") + "'");
			}
		}

		// current stage
		if (sm.Parm("FilterPM").length() > 0 && !sm.Parm("FilterPM").equalsIgnoreCase("0")) {
			sb.append(" AND tproject.pm_uid = " + sm.Parm("FilterPM"));

		}

		return sb.toString();

	}

	public boolean deleteOk() {
		return false;
	}

	public void afterAdd(Integer newRowKey) throws services.ServicesException {
		// if adding a new Project... must set up a permission, else the
		// person who just added cannot even see it

		String javaDate = dateFormat.format(new Date()); // returns

		String sql = new String("insert into tuser_project (project_id, user_id, role_cd, added_uid, added_date) "
				+ " values( " + newRowKey.toString() + "," + sm.getUserId().toString() + ",'UPD', "
				+ sm.getUserId().toString() + ", '" + db.flipToYYYYMMDD_HHMM(javaDate) + "')");

		db.runQuery(sql);
		sm.cacheProjectFilter();
		sm.setProjectId(newRowKey);
	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 */
	public Hashtable<String, WebField> getWebFields(String parmMode) throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
		// boolean showMode = parmMode.equalsIgnoreCase("show") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		// save off the project id to the session
		if (!addMode) {
			try {
				sm.setProjectId(db.getInteger("project_id"));
				// WHY-WHY sm.setFilterId("ProjectId", projectId);
			} catch (NumberFormatException e) {
			}
		}

		/*
		 * get ht's
		 */

		Hashtable htProcesses = sm.getTable("tprocess",
				"select title_nm, process_id, title_nm from tprocess where division_id = "
						+ sm.getDivisionId().toString() + " order by title_nm");

		Hashtable htCommitments = sm.getTable("tcommitment",
				"select title_nm, commitment_id, title_nm from tcommitment where division_id = "
						+ sm.getDivisionId().toString() + " order by title_nm");

		Hashtable htStages = sm.getTable("tstage", "select title_nm, stage_id, title_nm from tstage");

		/*
		 * Strings
		 */

		ht.put("vendor_contact_tx",
				new WebFieldString("vendor_contact_tx", addMode ? "" : db.getText("vendor_contact_tx"), 64, 64));

		ht.put("dependencies_tx",
				new WebFieldString("dependencies_tx", addMode ? "" : db.getText("dependencies_tx"), 64, 64));

		ht.put("chng_req_nm", new WebFieldString("chng_req_nm", addMode ? "" : db.getText("chng_req_nm"), 32, 32));

		ht.put("project_name", new WebFieldString("project_name", (addMode ? "" : db.getText("project_name")), 24, 50));

		ht.put("rfc_id", new WebFieldString("rfc_id", addMode ? "" : db.getText("rfc_id"), 8, 8));

		ht.put("release_note_id",
				new WebFieldString("release_note_id", addMode ? "" : db.getText("release_note_id"), 8, 8));

		ht.put("incident_id", new WebFieldString("incident_id", addMode ? "" : db.getText("incident_id"), 8, 8));

		ht.put("defect_id", new WebFieldString("defect_id", addMode ? "" : db.getText("defect_id"), 8, 8));

		ht.put("cr_no", new WebFieldString("cr_no", addMode ? "" : db.getText("cr_no"), 5, 5));

		ht.put("ra_no", new WebFieldString("ra_no", addMode ? "" : db.getText("ra_no"), 5, 5));

		ht.put("sr_no", new WebFieldString("sr_no", addMode ? "" : db.getText("sr_no"), 5, 5));

		String rfcLink = new String("");
		String noteLink = new String("");
		String incidentLink = new String("");
		String defectLink = new String("");
		String crLink = new String("");
		String adhocLink = new String("");

		if (!addMode) {

			rfcLink = "<A href=Router?Target=Rfc&Action=Show&Relation=this&RowKey=" + db.getText("rfc_id") + ">"
					+ db.getText("rfc_title") + "</A>";

			noteLink = "<A href=Router?Target=ReleaseNote&Action=Show&Relation=this&RowKey="
					+ db.getText("release_note_id") + ">" + db.getText("note_title") + "</A>";

			incidentLink = "<A href=Router?Target=Incident&Action=Show&Relation=this&RowKey="
					+ db.getText("incident_id") + ">" + db.getText("incident_title") + "</A>";

			defectLink = "<A href=Router?Target=Defect&Action=Show&Relation=this&RowKey=" + db.getText("defect_id")
					+ ">" + db.getText("defect_title") + "</A>";

			crLink = "<A href=Router?Target=Epiccr&Action=Show&Relation=this&RowKey=" + db.getText("cr_id") + ">"
					+ db.getText("cr_title") + "</A>";

			adhocLink = "<A href=Router?Target=Adhoc&Action=Show&Relation=this&RowKey=" + db.getText("adhoc_id") + ">"
					+ db.getText("adhoc_title") + "</A>";

		}

		ht.put("rfcname", new WebFieldDisplay("rfcname", addMode ? "" : rfcLink));

		ht.put("notename", new WebFieldDisplay("notename", addMode ? "" : noteLink));

		ht.put("incidentname", new WebFieldDisplay("incidentname", addMode ? "" : incidentLink));

		ht.put("defectname", new WebFieldDisplay("defectname", addMode ? "" : defectLink));

		ht.put("crname", new WebFieldDisplay("crname", addMode ? "" : crLink));

		ht.put("adhocname", new WebFieldDisplay("adhocname", addMode ? "" : adhocLink));

		/*
		 * Id's
		 */

		ht.put("pm_uid", new WebFieldSelect("pm_uid", addMode ? new Integer("0") : (Integer) db.getObject("pm_uid"),
				sm.getUserHT(), true, true));

		ht.put("stakeholder_uid", new WebFieldSelect("stakeholder_uid",
				addMode ? new Integer("0") : (Integer) db.getObject("stakeholder_uid"), sm.getUserHT(), true, true));

		ht.put("team_leader_uid", new WebFieldSelect("team_leader_uid",
				addMode ? new Integer("0") : (Integer) db.getObject("team_leader_uid"), sm.getUserHT(), true, true));

		ht.put("business_mgr_uid", new WebFieldSelect("business_mgr_uid",
				addMode ? new Integer("0") : (Integer) db.getObject("business_mgr_uid"), sm.getUserHT(), true, true));

		ht.put("process_mgr_uid", new WebFieldSelect("process_mgr_uid",
				addMode ? new Integer("0") : (Integer) db.getObject("process_mgr_uid"), sm.getUserHT(), true, true));

		ht.put("primary_application_id",
				new WebFieldSelect("primary_application_id",
						addMode ? new Integer("0") : (Integer) db.getObject("primary_application_id"),
						sm.getApplicationFilter()));

		ht.put("commitment_id", new WebFieldSelect("commitment_id",
				addMode ? new Integer("0") : db.getInteger("commitment_id"), htCommitments));

		ht.put("process_id", new WebFieldSelect("process_id", addMode ? new Integer("0") : db.getInteger("process_id"),
				htProcesses));

		/*
		 * Numbers
		 */

		ht.put("est_days_no", new WebFieldString("est_days_no", (addMode ? "" : db.getText("est_days_no")), 3, 6));

		ht.put("est_months_no",
				new WebFieldString("est_months_no", (addMode ? "" : db.getText("est_months_no")), 3, 6));

		ht.put("est_roi_amt", new WebFieldString("est_roi_amt", (addMode ? "" : db.getText("est_roi_amt")), 3, 6));

		ht.put("est_cost_amt", new WebFieldString("est_cost_amt", (addMode ? "" : db.getText("est_cost_amt")), 3, 6));

		ht.put("act_days_no", new WebFieldString("act_days_no", (addMode ? "" : db.getText("act_days_no")), 3, 6));

		ht.put("act_months_no",
				new WebFieldString("act_months_no", (addMode ? "" : db.getText("act_months_no")), 3, 6));

		ht.put("act_roi_amt", new WebFieldString("act_roi_amt", (addMode ? "" : db.getText("act_roi_amt")), 3, 6));

		ht.put("act_cost_amt", new WebFieldString("act_cost_amt", (addMode ? "" : db.getText("act_cost_amt")), 3, 6));

		// funding amts
		ht.put("operation_amt",
				new WebFieldString("operation_amt", (addMode ? "" : db.getText("operation_amt")), 3, 6));

		ht.put("capital_amt", new WebFieldString("capital_amt", (addMode ? "" : db.getText("capital_amt")), 3, 6));

		ht.put("total_amt", new WebFieldString("total_amt", (addMode ? "" : db.getText("total_amt")), 3, 6));

		/*
		 * Codes
		 */

		ht.put("team_cd",
				new WebFieldSelect("team_cd", addMode ? "NEW" : db.getText("team_cd"), sm.getCodes("SUITES")));

		ht.put("status_cd",
				new WebFieldSelect("status_cd", addMode ? "NEW" : db.getText("status_cd"), sm.getCodes("STATUS")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db.getText("type_cd"), sm.getCodes("PROJTYPE")));

		ht.put("current_stage_id", new WebFieldSelect("current_stage_id",
				addMode ? new Integer("0") : db.getInteger("current_stage_id"), htStages));

		ht.put("progress_cd",
				new WebFieldSelect("progress_cd", addMode ? "1" : db.getText("progress_cd"), sm.getCodes("RYG")));

		ht.put("priority_cd",
				new WebFieldSelect("priority_cd", addMode ? "1" : db.getText("priority_cd"), sm.getCodes("RANK10")));

		/*
		 * Flags
		 */
		ht.put("completed_flag",
				new WebFieldCheckbox("completed_flag", addMode ? "N" : db.getText("completed_flag"), "Completed"));

		/*
		 * Dates
		 */
		ht.put("plan_start_date",
				new WebFieldString("plan_start_date", addMode ? "" : db.getText("plan_start_date"), 10, 10));

		ht.put("plan_end_date",
				new WebFieldString("plan_end_date", addMode ? "" : db.getText("plan_end_date"), 10, 10));

		ht.put("actual_start_date",
				new WebFieldString("actual_start_date", addMode ? "" : db.getText("actual_start_date"), 10, 10));

		ht.put("actual_end_date",
				new WebFieldString("actual_end_date", addMode ? "" : db.getText("actual_end_date"), 10, 10));

		/*
		 * Blobs
		 */

		ht.put("problem_stmt", new WebFieldText("problem_stmt", addMode ? "" : db.getText("problem_stmt"), 3, 80));

		ht.put("bsns_env", new WebFieldText("bsns_env", addMode ? "" : db.getText("bsns_env"), 3, 80));

		ht.put("cust_needs", new WebFieldText("cust_needs", addMode ? "" : db.getText("cust_needs"), 3, 80));

		ht.put("effects_tx", new WebFieldText("effects_tx", addMode ? "" : db.getText("effects_tx"), 3, 80));

		ht.put("scope_in", new WebFieldText("scope_in", addMode ? "" : db.getText("scope_in"), 3, 80));

		ht.put("scope_out", new WebFieldText("scope_out", addMode ? "" : db.getText("scope_out"), 3, 80));

		ht.put("resources_tx", new WebFieldText("resources_tx", addMode ? "" : db.getText("resources_tx"), 3, 80));

		ht.put("budget_tx", new WebFieldText("budget_tx", addMode ? "" : db.getText("budget_tx"), 3, 80));

		ht.put("timeline_tx", new WebFieldText("timeline_tx", addMode ? "" : db.getText("timeline_tx"), 3, 80));

		return ht;
	}
}
