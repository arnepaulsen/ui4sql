/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import db.*;

import java.util.*;

import router.SessionMgr;

/**
 * Issue Plugin
 * 
 * Change log:
 * 
 * 6/20 added logic in 'beforeUpdate' to set the closed_by_uid and closed_date
 * 
 * 8/22 added list selector for status column
 * 
 * 4/20/09 = Wow ! convert to a Spring bean !!
 * 
 * 9/28 Remove unused filter column 3
 * 
 * 
 */

public class SBARPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	/*
	 * List Page Filters
	 */

	private String subProjectQuery = "select c.order_by as odor, code_value, code_desc "
			+ " from tcodes c where c.code_desc2 = 'SBAR' "
			+ " and c.code_type_id = 157  ";

	// note the symbolic %PROJECTID% is resolved at run-time!

	private BeanFieldSelect filterStatus = new BeanFieldSelect(2,
			"FilterStatus", "status_cd", "", "", "Status?", "SBARSTATUS");

	private BeanFieldSelect filterPriority = new BeanFieldSelect(3,
			"FilterPriority", "priority_cd", "", "", "Priority?", "PRIORITYTRIAGE");
	
	
	
	// 99 = code word for userId
	private BeanFieldSelect filterSubProject = new BeanFieldSelect(4,
			"FilterSubproject", "sub_cd	", "", "", "Category?", "SQL",
			subProjectQuery);
	
	private BeanFieldSelect filterAction = new BeanFieldSelect(5,
			"FilterAction", "fac_actn_reqr_cd", "", "", "Facility Action?", "YESNO");

	private static String followupQuery = "SELECT DISTINCT concat(last_name, ', ', first_name) as odor, tuser.user_id, concat(last_name , ',', first_name) from tsbar "
			+ " LEFT JOIN tuser on tsbar.follow_up_uid = tuser.user_id where user_id is not null ; ";

	// 99 = code word for userId

	private BeanFieldSelect filterFollow = new BeanFieldSelect(6,
			"FilterFollowup", "follow_up_uid", 0, 0, "Follow-Up By ?", "SQL",
			followupQuery);

	private BeanFieldString filterTopic = new BeanFieldString(1, "title_nm",
			"Topic Search ?", 32, 32);

	/*
	 * Web Beans
	 */

	private BeanFieldString reference_nm = new BeanFieldString("reference_nm",
			4, 4);

	private BeanFieldString requestor_nm = new BeanFieldString("requestor_nm",
			64, 128);

	private BeanFieldString submitted_nm = new BeanFieldString("submitted_nm",
			45, 45);

	private BeanFieldLink file_link = new BeanFieldLink("docushare_link_tx",
			90, 255);

	private BeanFieldString originator_nm = new BeanFieldString(
			"originator_nm", 45, 45);

	private BeanFieldString owner_nm = new BeanFieldString("owner_nm", 45, 45);

	private BeanFieldString pm_nm = new BeanFieldString("pm_nm", 45, 45);

	private BeanFieldString combined_cat = new BeanFieldString(
			"combined_cat_tx", 64, 128);

	private BeanFieldSelect follow_up_uid = new BeanFieldSelect(
			"follow_up_uid", 0, "userHT"); // secret code for
	// sm.getUserId()

	private BeanFieldSelect status_cd = new BeanFieldSelect("status_cd", "N",
			"SBARSTATUS");

	private BeanFieldSelect type_cd = new BeanFieldSelect("type_cd", "",
			"ISSUTYPE");

	private BeanFieldSelect priority_cd = new BeanFieldSelect("priority_cd",
			"", "PRIORITYTRIAGE");
	

	private BeanFieldSelect approve_cd = new BeanFieldSelect("approve_cd", "",
			"YESNOUNK");
	
	private BeanFieldSelect fac_actn_reqr_cd = new BeanFieldSelect("fac_actn_reqr_cd", "",
	"YESNO");

	private BeanFieldSelect sub_cd = new BeanFieldSelect("sub_cd", "", "SQL",
			subProjectQuery);

	private BeanFieldString title_nm = new BeanFieldString("title_nm", 64, 64);

	private BeanFieldString assigned_nm = new BeanFieldString("assigned_nm",
			64, 64);

	private BeanFieldDisplay closed_date = new BeanFieldDisplay("closed_date");

	private BeanFieldDisplay closed_by = new BeanFieldDisplay("closed_by",
			"last_name");

	private BeanFieldDate install_date = new BeanFieldDate("install_date");

	private BeanFieldDate id_date = new BeanFieldDate("id_date");

	private BeanFieldString prod_rfc_no = new BeanFieldString("prod_rfc_no", 8,
			8);

	private BeanFieldString defect_no = new BeanFieldString("defect_no", 8, 8);

	private BeanFieldString problem_no = new BeanFieldString("problem_no", 8, 8);

	/*
	 * Dates
	 */

	private BeanFieldDate sbar_recv_date = new BeanFieldDate("sbar_recv_date");

	private BeanFieldDate sbar_present_date = new BeanFieldDate(
			"sbar_present_date");

	private BeanFieldDate workplan_recv_date = new BeanFieldDate(
			"workplan_recv_date");

	private BeanFieldDate workplan_present_date = new BeanFieldDate(
			"workplan_present_date");

	private BeanFieldDate recommend_recv_date = new BeanFieldDate(
			"recommend_recv_date");

	private BeanFieldDate recommend_present_date = new BeanFieldDate(
			"recommend_present_date");

	private BeanFieldDate communication_date = new BeanFieldDate(
			"communication_date");

	private BeanFieldDate implementation_date = new BeanFieldDate(
			"implementation_date");

	/*
	 * 
	 * blobs
	 */

	
	private BeanFieldText facility_action_blob = new BeanFieldText(
			"facility_action_blob", 4, 100);

	
	private BeanFieldText proposed_solution_blob = new BeanFieldText(
			"proposed_solution_blob", 4, 100);

	private BeanFieldText mitigated_steps_blob = new BeanFieldText(
			"mitigated_steps_blob", 4, 100);

	private BeanFieldText desc_blob = new BeanFieldText("desc_blob", 4, 100);

	private BeanFieldText notes_blob = new BeanFieldText("notes_blob", 4, 100);

	private BeanFieldText impact_blob = new BeanFieldText("impact_blob", 4, 100);

	private BeanFieldText progress_blob = new BeanFieldText("progress_blob", 4,
			100);

	private BeanFieldText decision_blob = new BeanFieldText("decision_blob", 4,
			100);

	private BeanFieldText resolution_blob = new BeanFieldText(
			"resolution_blob", 4, 100);
	
	private BeanFieldText discussion_blob = new BeanFieldText(
			"discussion_blob", 4, 100);
	

	/*
	 * SBAR Stuff
	 */

	private BeanFieldText situation_blob = new BeanFieldText("situation_blob",
			4, 100);
	private BeanFieldText background_blob = new BeanFieldText(
			"background_blob", 4, 100);
	private BeanFieldText assessment_blob = new BeanFieldText(
			"assessment_blob", 4, 100);
	private BeanFieldText recommendations_blob = new BeanFieldText(
			"recommendations_blob", 4, 100);

	/*
	 * JavaScript fields for DatePicker - must null-out the js when in show-mode ..
	 * because there is no HTML element to set if the click the calendar.
	 */

	private BeanFieldDisplay install_date_js = new BeanFieldDisplay(
			"install_date_js");
	private BeanFieldDisplay communication_date_js = new BeanFieldDisplay(
			"communication_date_js");
	private BeanFieldDisplay implementation_date_js = new BeanFieldDisplay(
			"implementation_date_js");

	private BeanFieldDisplay id_date_js = new BeanFieldDisplay("id_date_js");
	private BeanFieldDisplay sbar_recv_date_js = new BeanFieldDisplay(
			"sbar_recv_date_js");
	private BeanFieldDisplay sbar_present_date_js = new BeanFieldDisplay(
			"sbar_present_date_js");
	private BeanFieldDisplay workplan_recv_date_js = new BeanFieldDisplay(
			"workplan_recv_date_js");
	private BeanFieldDisplay workplan_present_date_js = new BeanFieldDisplay(
			"workplan_present_date_js");
	private BeanFieldDisplay recommend_recv_date_js = new BeanFieldDisplay(
			"recommend_recv_date_js");
	private BeanFieldDisplay recommend_present_date_js = new BeanFieldDisplay(
			"recommend_present_date_js");

	public SBARPlugin() throws services.ServicesException {
		super();

		this.setTableName("tsbar");
		this.setKeyName("sbar_id");
		this
				.setTargetTitle("Issue Triage <b>Proprietary & Confidential. For Internal Use Only.</b>");

		this.setListHeaders(new String[] { "Id", "Topic", "Status", "Decision",
				"Category", "Owner", "Follow-Up", "Submittor" });

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Attachment");
		this.setDetailTargetLabel("Attachments");

		this.setSelectViewName("vsbar");
		this.setListViewName("vsbar_list");

		this.setListOrder("reference_nm");

		this.setExcelOk(true);

		this.setNextOk(false);

		this.setExcelTemplate("vsbar_excel", "sbar.xls", 1, 29);
		// this.setUpdatesLevel("administrator");

		this.setSubmitOk(false);

		this.setShowAuditSubmitApprove(false);

		this.setListFilters(new BeanWebField[] { filterStatus,
				filterSubProject, filterPriority, filterFollow, filterTopic, filterAction });

	}

	public void init(SessionMgr parmSm) {
		super.init(parmSm);

		boolean editMode = parmSm.Parm("Action").equalsIgnoreCase("edit")
				|| parmSm.Parm("Action").equalsIgnoreCase("add");

		try {

			/*
			 * if in show mode, blank out the date picker js because there is no
			 * input tag , and that give a js error
			 */
			if (!editMode) {
				install_date_js.setDisplayValue("");
				implementation_date_js.setDisplayValue("");
				communication_date_js.setDisplayValue("");
				id_date_js.setDisplayValue("");
				sbar_recv_date_js.setDisplayValue("");
				sbar_present_date_js.setDisplayValue("");
				workplan_recv_date_js.setDisplayValue("");
				workplan_present_date_js.setDisplayValue("");
				recommend_recv_date_js.setDisplayValue("");
				recommend_present_date_js.setDisplayValue("");

			} else {
				// debug("setting install_date js");
				install_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('install_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");
				implementation_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('implementation_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");
				communication_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('communication_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

				id_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('id_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

				sbar_recv_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('sbar_recv_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

				sbar_present_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('sbar_present_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

				workplan_recv_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('workplan_recv_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

				workplan_present_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('workplan_present_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

				recommend_recv_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('recommend_recv_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

				recommend_present_date_js
						.setDisplayValue("<a href=\"javascript:NewCal('recommend_present_date','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>");

			}

			this.setWebFieldBeans(new BeanWebField[] { reference_nm,
					assigned_nm, status_cd, type_cd, priority_cd, sub_cd,
					title_nm, closed_date, situation_blob, background_blob,
					notes_blob, assessment_blob, recommendations_blob,
					impact_blob, closed_by, install_date, id_date, prod_rfc_no,
					problem_no, requestor_nm, defect_no, combined_cat,
					follow_up_uid, progress_blob, decision_blob,
					resolution_blob, approve_cd, desc_blob, submitted_nm,
					originator_nm, owner_nm, pm_nm, sbar_recv_date,
					sbar_present_date, workplan_recv_date,
					workplan_present_date, recommend_recv_date,
					recommend_present_date, communication_date,
					implementation_date, install_date_js,
					implementation_date_js, communication_date_js, id_date_js,
					sbar_recv_date_js, sbar_present_date_js,
					workplan_recv_date_js, workplan_present_date_js,
					recommend_recv_date_js, recommend_present_date_js,
					proposed_solution_blob, mitigated_steps_blob, file_link , facility_action_blob, discussion_blob,fac_actn_reqr_cd});

			this.setBlockList(sm.getIssueTriageLevel().equalsIgnoreCase("N"));

			/*
			 * allow edits for normal users
			 */

			if (sm.getIssueTriageLevel().equalsIgnoreCase("U")) {

				this.setEditOk(true);
				this.setCopyOk(true);
				this.setExcelOk(true);
				this.setDeleteOk(false);

			} else {
				if (sm.getIssueTriageLevel().equalsIgnoreCase("A")) {
					this.setEditOk(true);
					this.setCopyOk(true);
					this.setExcelOk(true);
					this.setDeleteOk(true);

				} else {
					this.setDeleteOk(false);
					this.setEditOk(false);
					this.setCopyOk(false);
					this.setExcelOk(false);
					this.setAddOk(false);
				}
			}

		} catch (Exception e) {
			debug("sbar init: " + e.toString());
		}

	}

	public boolean afterGet() {

		sm.setParentTable("tsbar");

		sm.setParentId(this.rowId, db.getText("title_nm"));
		sm.setParentKeyName("sbar_id");

		/*
		 * log the user access
		 */

		// omit logging for KP-IT administrators
		/** Implemented per request received by email
		 * Arne
				Can you please remove the follow names from the logs sections of PMO.  
				IP Issue Triage Administrator:
				Bellah
				Cadet
				Cadwell
				Gavigan
				Gavigan
				Macias
				Mendoza
				Paulsen
				Randall
				Rohr
				Samara
				White 
				We don't need to know if administrators logged in or not.  
				Thanks
				________________________________________
				Neezar V. Samara
				KPHC Business Consultant - Northern California
				Regional Triage Team
		 */
		if (sm.getIssueTriageLevel().equalsIgnoreCase("A")) {
			return true;
		}
		
		// 
		String query = " insert into tlog (division_id, access_cd, access_uid, added_uid, added_date, form_id, key_id) values(1, 'R', "
				+ sm.getUserId().toString()
				+ ", 1, now(), 129, "
				+ this.rowId.toString() + "  )";

		try {
			db.runQuery(query);
		} catch (Exception e) {
			debug("sBAR log error : " + e.toString());

		}
		// setUpdatesOk(!getUserRoleCode().equalsIgnoreCase("brw"));

		return true;
	}

}
