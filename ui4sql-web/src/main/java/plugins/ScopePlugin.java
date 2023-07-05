/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * 
 *   2/15 added mySql
 * 	3/11 add join on project Permissions
 * */

/**
 * Scope Change Request
 * 
 * 
 * Keywords:
 * 
 * 
 */
public class ScopePlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTOR
	// *******************
	

	public ScopePlugin() throws services.ServicesException {
		super();
		this.setTableName("tscope");
		this.setKeyName("scope_id");
		this.setTargetTitle("Scope Change Request");
		this.setShowAuditSubmitApprove(true);
		
		this.setListHeaders( new String[] { "Reference", "Title", "Approved",
				"Priority", "Owner" });

		this.setMoreListColumns(new  String[] { "tscope.reference_nm",
				"tscope.title_nm", "approved.code_desc as approved_tx",
				"priority.code_desc as priority_tx",
				"concat(own.last_name, ',', own.first_name) as theOwner" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes approved on tscope.board_review_ok_cd = approved.code_value and approved.code_type_id  = 3  ",
				" left join tcodes priority on tscope.priority_cd = priority.code_value and priority.code_type_id  = 55  ",
				" left join tuser own on tscope.owner_uid = own.user_id " });

	}

	/*
	 * List Selectors
	 * 
	 */

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 2)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		// default the status to open when starting new list
		WebFieldSelect wf = new WebFieldSelect("FilterApproved", (sm.Parm(
				"FilterApproved").length() == 0 ? "O" : sm
				.Parm("FilterApproved")), sm.getCodes("YESNO"), "Approved?");
		wf.setDisplayClass("listform");
		return wf;

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present
		if (sm.Parm("FilterApproved").length() == 0) {
			sb.append(" AND approved.code_value = 'N'");
		}

		else {
			if (!sm.Parm("FilterApproved").equalsIgnoreCase("0")) {
				sb.append(" AND approved.code_value = '"
						+ sm.Parm("FilterApproved") + "'");
			}
		}

		return sb.toString();

	}

	// *********************************
	// Web Fields for Display
	// *********************************

	// center the confirmed flag
	public boolean getListColumnCenterOn(int x) {
		if (false)
			return true;
		else
			return false;
	}

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Ids
		 */

		
		ht.put("pmo_uid", new WebFieldSelect("pmo_uid",
				addMode ? new Integer("0") : db.getInteger("pmo_uid"), sm
						.getUserHT(), true));

		
		ht.put("requestor_uid", new WebFieldSelect("requestor_uid",
				addMode ? new Integer("0") : db.getInteger("requestor_uid"), sm
						.getUserHT(), true));

		ht.put("reviewer_uid", new WebFieldSelect("reviewer_uid",
				addMode ? new Integer("0") : db.getInteger("reviewer_uid"), sm
						.getUserHT(), true));

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));
		
		ht.put("contact_uid", new WebFieldSelect("contact_uid",
				addMode ? new Integer("0") : db.getInteger("contact_uid"), sm
						.getUserHT(), true));

		ht.put("authorized_uid", new WebFieldSelect("authorized_uid",
				addMode ? new Integer("0") : db.getInteger("authorized_uid"),
				sm.getUserHT(), true));

		ht.put("deferred_uid", new WebFieldSelect("deferred_uid",
				addMode ? new Integer("0") : db.getInteger("deferred_uid"), sm
						.getUserHT(), true));

		ht.put("approved_uid", new WebFieldSelect("approved_uid",
				addMode ? new Integer("0") : db.getInteger("approved_uid"), sm
						.getUserHT(), true));

		ht.put("declined_uid", new WebFieldSelect("declined_uid",
				addMode ? new Integer("0") : db.getInteger("declined_uid"), sm
						.getUserHT(), true));

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));

		/*
		 * Flags / codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("OPENCLOSE")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("production_cd", new WebFieldSelect("production_cd",
				addMode ? "" : db.getText("production_cd"), sm
						.getCodes("YESNO")));

		ht.put("safety_cd", new WebFieldSelect("safety_cd", addMode ? "" : db
				.getText("safety_cd"), sm.getCodes("YESNO")));

		ht.put("board_review_ok_cd", new WebFieldSelect("board_review_ok_cd",
				addMode ? "" : db.getText("board_review_ok_cd"), sm
						.getCodes("YESNO")));
		
	
		

		/*
		 * Flags
		 */

		ht.put("golive_flag", new WebFieldCheckbox("golive_flag", addMode ? "" : db
				.getText("golive_flag"), ""));
		
		ht.put("hardware_flag", new WebFieldCheckbox("hardware_flag",
				addMode ? "N" : db.getText("hardware_flag"), ""));

		ht.put("software_flag", new WebFieldCheckbox("software_flag",
				addMode ? "N" : db.getText("software_flag"), ""));

		ht.put("interfaces_flag", new WebFieldCheckbox("interfaces_flag",
				addMode ? "N" : db.getText("interfaces_flag"), ""));

		ht.put("resources_flag", new WebFieldCheckbox("resources_flag",
				addMode ? "N" : db.getText("resources_flag"), ""));

		ht.put("training_flag", new WebFieldCheckbox("training_flag",
				addMode ? "N" : db.getText("training_flag"), ""));

		ht.put("requirements_flag", new WebFieldCheckbox("requirements_flag",
				addMode ? "N" : db.getText("requirements_flag"), ""));

		ht.put("testing_flag", new WebFieldCheckbox("testing_flag",
				addMode ? "N" : db.getText("testing_flag"), ""));

		ht.put("declined_flag", new WebFieldCheckbox("declined_flag",
				addMode ? "N" : db.getText("declined_flag"), ""));

		ht.put("deferred_flag", new WebFieldCheckbox("deferred_flag",
				addMode ? "N" : db.getText("deferred_flag"), ""));

		ht.put("approved2_flag", new WebFieldCheckbox("approved2_flag",
				addMode ? "N" : db.getText("approved2_flag"), ""));
		
		ht.put("funding_verify_flag", new WebFieldCheckbox("funding_verify_flag",
				addMode ? "N" : db.getText("funding_verify_flag"), ""));


		/*
		 * Impact codes
		 */

		ht.put("impact_pmo_flag", new WebFieldCheckbox("impact_pmo_flag",
				addMode ? "N" : db.getText("impact_pmo_flag"), ""));

		ht.put("impact_pbs_flag", new WebFieldCheckbox("impact_pbs_flag",
				addMode ? "N" : db.getText("impact_pbs_flag"), ""));

		ht.put("impact_tpmg_flag", new WebFieldCheckbox("impact_tpmg_flag",
				addMode ? "N" : db.getText("impact_tpmg_flag"), ""));

		ht.put("impact_hp_flag", new WebFieldCheckbox("impact_hp_flag",
				addMode ? "N" : db.getText("impact_hp_flag"), ""));

		ht.put("impact_it_flag", new WebFieldCheckbox("impact_it_flag",
				addMode ? "N" : db.getText("impact_it_flag"), ""));

		ht.put("impact_ops_flag", new WebFieldCheckbox("impact_ops_flag",
				addMode ? "N" : db.getText("impact_ops_flag"), ""));

		ht.put("impact_labor_flag", new WebFieldCheckbox("impact_labor_flag",
				addMode ? "N" : db.getText("impact_labor_flag"), ""));

		ht.put("analysis_auth_flag", new WebFieldCheckbox("analysis_auth_flag",
				addMode ? "N" : db.getText("analysis_auth_flag"), ""));

		ht.put("deferred_flag", new WebFieldCheckbox("deferred_flag",
				addMode ? "N" : db.getText("deferred_flag"), ""));
		
		/*
		 * 
		 * new stuff 12/1/06
		 */
		

			
		
		ht.put("impact_comm_flag", new WebFieldCheckbox("impact_comm_flag", addMode ? "N" : db
				.getText("impact_comm_flag"), ""));

		ht.put("impact_op_flag", new WebFieldCheckbox("impact_op_flag", addMode ? "" : db
				.getText("impact_op_flag"), ""));

		ht.put("impact_legacy_flag", new WebFieldCheckbox("impact_legacy_flag", addMode ? "" : db
				.getText("impact_legacy_flag"), ""));
		
		ht.put("impact_dmdl_flag", new WebFieldCheckbox("impact_dmdl_flag", addMode ? "" : db
				.getText("impact_dmdl_flag"), "" ));
		
		ht.put("impact_finance_flag", new WebFieldCheckbox("impact_finance_flag", addMode ? "" : db
				.getText("impact_finance_flag"), ""));

		ht.put("impact_demo_flag", new WebFieldCheckbox("impact_demo_flag", addMode ? "" : db
				.getText("impact_demo_flag"), ""));

		ht.put("impact_envs_flag", new WebFieldCheckbox("impact_envs_flag", addMode ? "" : db
				.getText("impact_envs_flag"), ""));

		ht.put("impact_interface_flag", new WebFieldCheckbox("impact_interface_flag", addMode ? "" : db
				.getText("impact_interface_flag"), ""));

		ht.put("impact_rc_flag", new WebFieldCheckbox("impact_rc_flag", addMode ? "" : db
				.getText("impact_rc_flag"), ""));

		ht.put("impact_rims_flag", new WebFieldCheckbox("impact_rims_flag", addMode ? "" : db
				.getText("impact_rims_flag"), ""));
	
		ht.put("impact_testing_flag", new WebFieldCheckbox("impact_testing_flag", addMode ? "" : db
				.getText("impact_testing_flag"), ""));

		ht.put("impact_wf_plan_flag", new WebFieldCheckbox("impact_wf_plan_flag", addMode ? "" : db
				.getText("impact_wf_plan_flag"), ""));

		ht.put("impact_wf_safety_flag", new WebFieldCheckbox("impact_wf_safety_flag", addMode ? "" : db
				.getText("impact_wf_safety_flag"), ""));

		ht.put("impact_ecl_flag", new WebFieldCheckbox("impact_ecl_flag", addMode ? "" : db
				.getText("impact_ecl_flag"), ""));
				
		ht.put("impact_other_flag", new WebFieldCheckbox("impact_other_flag", addMode ? "" : db
				.getText("impact_other_flag"), ""));
		
		ht.put("impact_impl_flag", new WebFieldCheckbox("impact_impl_flag", addMode ? "" : db
				.getText("impact_impl_flag"), ""));

		ht.put("impact_ip_flag", new WebFieldCheckbox("impact_ip_flag", addMode ? "" : db
				.getText("impact_ip_flag"), ""));

		ht.put("impact_hos_ops_flag", new WebFieldCheckbox("impact_hos_ops_flag", addMode ? "" : db
				.getText("impact_hos_ops_flag"), ""));

		ht.put("impact_care_flag", new WebFieldCheckbox("impact_care_flag", addMode ? "" : db
				.getText("impact_care_flag"), ""));

		ht.put("impact_med_op_flag", new WebFieldCheckbox("impact_med_op_flag", addMode ? "" : db
				.getText("impact_med_op_flag"), ""));

		ht.put("impact_med_fo_flag", new WebFieldCheckbox("impact_med_fo_flag", addMode ? "" : db
				.getText("impact_med_fo_flag"), ""));
		
		ht.put("impact_nops_flag", new WebFieldCheckbox("impact_nops_flag", addMode ? "" : db
				.getText("impact_nops_flag"), ""));

		ht.put("impact_kpit_flag", new WebFieldCheckbox("impact_kpit_flag", addMode ? "" : db
				.getText("impact_kpit_flag"), ""));

		ht.put("impact_epic_flag", new WebFieldCheckbox("impact_epic_flag", addMode ? "" : db
				.getText("impact_epic_flag"), ""));

		ht.put("impact_dhmo_flag", new WebFieldCheckbox("impact_dhmo_flag", addMode ? "" : db
				.getText("impact_dhmo_flag"), ""));

	

		/*
		 * Dates
		 */

		ht.put("entered_date", new WebFieldDate("entered_date",
				addMode ? "" : db.getText("entered_date")));

			
		ht.put("submitted_date", new WebFieldDate("submitted_date",
				addMode ? "" : db.getText("submitted_date")));

		ht.put("authorized_date", new WebFieldDate("authorized_date",
				addMode ? "" : db.getText("authorized_date")));
		
		ht.put("reviewed_date", new WebFieldDate("reviewed_date",
				addMode ? "" : db.getText("reviewed_date")));
		
		ht.put("deferred_date", new WebFieldDate("deferred_date", addMode ? ""
				: db.getText("deferred_date")));

		ht.put("approved_date", new WebFieldDate("approved_date", addMode ? ""
				: db.getText("approved_date")));

		ht.put("declined_date", new WebFieldDate("declined_date", addMode ? ""
				: db.getText("declined_date")));

		ht.put("assigned_date", new WebFieldDate("assigned_date", addMode ? ""
				: db.getText("assigned_date")));

		ht.put("start_date", new WebFieldDate("start_date", addMode ? "" : db
				.getText("start_date")));
		
		ht.put("completed_date", new WebFieldDate("completed_date", addMode ? ""
				: db.getText("completed_date")));

		ht.put("est_complete_date", new WebFieldDate("est_complete_date", addMode ? ""
				: db.getText("est_complete_date")));

		
		/*
		 * Strings
		 */

		ht.put("ipm_name", new WebFieldString("ipm_name", (addMode ? "" : db
				.getText("ipm_name")), 32, 32));
		
		ht.put("contact_email_tx", new WebFieldString("contact_email_tx", (addMode ? "" : db
				.getText("contact_email_tx")), 64, 64));
		ht.put("contact_phone_tx", new WebFieldString("contact_phone_tx", (addMode ? "" : db
				.getText("contact_phone_tx")), 32, 32));
		
		
		ht.put("pmo_nm", new WebFieldString("pmo_nm", (addMode ? "" : db
				.getText("pmo_nm")), 32, 32));
		
		ht.put("team_tx", new WebFieldString("team_tx", (addMode ? "" : db
				.getText("team_tx")), 40, 40));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 32, 32));

		ht.put("golive_tx", new WebFieldString("golive_tx", (addMode ? "" : db
				.getText("golive_tx")), 60, 127));

		ht.put("reviewer_title_nm", new WebFieldString("reviewer_title_nm",
				(addMode ? "" : db.getText("reviewer_title_nm")), 32, 64));

		ht.put("other_tx", new WebFieldString("other_tx", (addMode ? "" : db
				.getText("other_tx")), 12, 24));

		ht.put("impact_other_tx", new WebFieldString("impact_other_tx",
				(addMode ? "" : db.getText("impact_other_tx")), 12, 24));

		ht.put("yr1_cost_tx", new WebFieldString("yr1_cost_tx", (addMode ? ""
				: db.getText("yr1_cost_tx")), 12, 24));
		ht.put("yr2_cost_tx", new WebFieldString("yr2_cost_tx", (addMode ? ""
				: db.getText("yr2_cost_tx")), 12, 24));
		ht.put("yr3_cost_tx", new WebFieldString("yr3_cost_tx", (addMode ? ""
				: db.getText("yr3_cost_tx")), 12, 24));
		ht.put("cmt_cost_tx", new WebFieldString("cmt_cost_tx", (addMode ? ""
				: db.getText("cmt_cost_tx")), 12, 24));

		ht.put("yr1_sched_tx", new WebFieldString("yr1_sched_tx", (addMode ? ""
				: db.getText("yr1_sched_tx")), 12, 24));
		ht.put("yr2_sched_tx", new WebFieldString("yr2_sched_tx", (addMode ? ""
				: db.getText("yr2_sched_tx")), 12, 24));
		ht.put("yr3_sched_tx", new WebFieldString("yr3_sched_tx", (addMode ? ""
				: db.getText("yr3_sched_tx")), 12, 24));
		ht.put("cmt_sched_tx", new WebFieldString("cmt_sched_tx", (addMode ? ""
				: db.getText("cmt_sched_tx")), 12, 24));

		ht.put("yr1_rsrc_tx", new WebFieldString("yr1_rsrc_tx", (addMode ? ""
				: db.getText("yr1_rsrc_tx")), 12, 24));
		ht.put("yr2_rsrc_tx", new WebFieldString("yr2_rsrc_tx", (addMode ? ""
				: db.getText("yr2_rsrc_tx")), 12, 24));
		ht.put("yr3_rsrc_tx", new WebFieldString("yr3_rsrc_tx", (addMode ? ""
				: db.getText("yr3_rsrc_tx")), 12, 24));
		ht.put("cmt_rsrc_tx", new WebFieldString("cmt_rsrc_tx", (addMode ? ""
				: db.getText("cmt_rsrc_tx")), 12, 24));

		ht.put("yr1_func_tx", new WebFieldString("yr1_func_tx", (addMode ? ""
				: db.getText("yr1_func_tx")), 12, 24));
		ht.put("yr2_func_tx", new WebFieldString("yr2_func_tx", (addMode ? ""
				: db.getText("yr2_func_tx")), 12, 24));
		ht.put("yr3_func_tx", new WebFieldString("yr3_func_tx", (addMode ? ""
				: db.getText("yr3_func_tx")), 12, 24));
		ht.put("cmt_func_tx", new WebFieldString("cmt_func_tx", (addMode ? ""
				: db.getText("cmt_func_tx")), 12, 24));

		ht.put("yr1_infra_tx", new WebFieldString("yr1_infra_tx", (addMode ? ""
				: db.getText("yr1_infra_tx")), 12, 24));
		ht.put("yr2_infra_tx", new WebFieldString("yr2_infra_tx", (addMode ? ""
				: db.getText("yr2_infra_tx")), 12, 24));
		ht.put("yr3_infra_tx", new WebFieldString("yr3_infra_tx", (addMode ? ""
				: db.getText("yr3_infra_tx")), 12, 24));
		ht.put("cmt_infra_tx", new WebFieldString("cmt_infra_tx", (addMode ? ""
				: db.getText("cmt_infra_tx")), 12, 24));

		ht.put("yr1_qlty_tx", new WebFieldString("yr1_qlty_tx", (addMode ? ""
				: db.getText("yr1_qlty_tx")), 12, 24));
		ht.put("yr2_qlty_tx", new WebFieldString("yr2_qlty_tx", (addMode ? ""
				: db.getText("yr2_qlty_tx")), 12, 24));
		ht.put("yr3_qlty_tx", new WebFieldString("yr3_qlty_tx", (addMode ? ""
				: db.getText("yr3_qlty_tx")), 12, 24));
		ht.put("cmt_qlty_tx", new WebFieldString("cmt_qlty_tx", (addMode ? ""
				: db.getText("cmt_qlty_tx")), 12, 24));


		ht.put("impact_rqr_nm", new WebFieldString("impact_rqr_nm", (addMode ? "" : db
				.getText("impact_rqr_nm")), 80, 80));
		
		
		ht.put("turnaround_tx", new WebFieldString("turnaround_tx", (addMode ? "" : db
				.getText("turnaround_tx")), 80, 80));



		
		
		/*
		 * Blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 6, 110));

		ht.put("comments_blob", new WebFieldText("comments_blob", addMode ? "" : db
				.getText("comments_blob"), 4, 110));
		
		ht.put("request_blob", new WebFieldText("request_blob", addMode ? ""
				: db.getText("request_blob"), 12, 110));

		ht.put("implications_blob", new WebFieldText("implications_blob",
				addMode ? "" : db.getText("implications_blob"), 4, 110));

		ht.put("assessment_blob", new WebFieldText("assessment_blob",
				addMode ? "" : db.getText("assessment_blob"), 4, 110));

		ht.put("alternatives_blob", new WebFieldText("alternatives_blob",
				addMode ? "" : db.getText("alternatives_blob"), 4, 110));

		ht.put("benefit_blob", new WebFieldText("benefit_blob", addMode ? ""
				: db.getText("benefit_blob"), 4, 110));
		
		
		ht.put("work_impact_blob", new WebFieldText("work_impact_blob", addMode ? ""
				: db.getText("work_impact_blob"), 4, 110));


		ht.put("impact_rqr_txt", new WebFieldText("impact_rqr_txt", addMode ? "" : db
				.getText("impact_rqr_txt"), 6, 110));

		
		return ht;

	}

}
