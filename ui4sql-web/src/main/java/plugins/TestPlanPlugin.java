/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;

/**
 * 
 *   2/15 added mySql
 * 3/13 as 'target' to list query 
 * */

/*******************************************************************************
 * Test Specification TableManager
 * 
 * Change Log:
 * 
 * TODO : add logic to turn off reviewed by and reviewed date when pass_flag
 * turned off 3/10 convert to tcodes
 * 
 * 8/2/10 add remedyno to hidden, so getRemedy() does not have to query db to get rfc#
 * 
 */

public class TestPlanPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	/*
	 * 
	 * Hidden
	 */
	
	private static BeanFieldHidden wfRemedyNo = new BeanFieldHidden("remedyno", "sr_no" );
	
	
	/*
	 * Strings
	 */
	private static BeanFieldString wfTitle = new BeanFieldString("title_nm",
			64, 64);

	private static BeanFieldString sr_no = new BeanFieldString("sr_no", 8, 8);

	private static BeanFieldString cab_no = new BeanFieldString("cab_sr_no", 8,
			8);

	private static BeanFieldDisplay sr_link = new BeanFieldDisplay("srlink");

	/*
	 * user ids
	 */

	private static BeanFieldSelect test_lead = new BeanFieldSelect(
			"test_lead_uid", 0, "userHT"); // secret code for sm.getUserId()

	private static BeanFieldSelect proj_manager = new BeanFieldSelect(
			"manager_uid", 0, "userHT"); // secret code for sm.getUserId()

	/*
	 * Floats
	 */

	private static BeanFieldString wfActExec = new BeanFieldString(
			"actual_exec_pct", 4, 4);

	private static BeanFieldString wfActPass = new BeanFieldString(
			"actual_pass_pct", 4, 4);

	private static BeanFieldString wfPlanExec = new BeanFieldString(
			"plan_exec_pct", 4, 4);

	private static BeanFieldString wfPlanPass = new BeanFieldString(
			"plan_pass_pct", 4, 4);

	/*
	 * remedy display
	 */

	BeanFieldDisplay wfUrgency = new BeanFieldDisplay("remedy_urgency");

	BeanFieldDisplay wfStatus = new BeanFieldDisplay("remedy_status");

	BeanFieldDisplay wfOwner = new BeanFieldDisplay("owner_uid", "remedyOwner");

	BeanFieldDisplay wfRequestor = new BeanFieldDisplay("requester_uid",
			"remedyRequester");

	BeanFieldDisplay wfRemedyEndDt = new BeanFieldDisplay("fmt_remedy_end_dt");

	BeanFieldDisplay wfRemedyRequestDt = new BeanFieldDisplay(
			"fmt_remedy_requested_completion_dt");

	BeanFieldDisplay wfRemedAsOf = new BeanFieldDisplay("remedy_asof_date");

	/*
	 * selectors / codes
	 */
	BeanFieldSelect wfProgress = new BeanFieldSelect("progress_cd", "", "RYG");

	BeanFieldSelect wfSox = new BeanFieldSelect("sox_compliance_cd", "", "RYG");

	BeanFieldSelect wfPlanSignStat = new BeanFieldSelect("plan_sign_cd", "",
			"RYG");

	BeanFieldSelect wfCaseSignStat = new BeanFieldSelect("case_sign_cd", "",
			"RYG");

	BeanFieldSelect wfSummaryStat = new BeanFieldSelect("summary_dt_st_cd", "",
			"RYG");

	BeanFieldSelect wfPhase = new BeanFieldSelect("phase_cd", "", "TESTPHASE");

	BeanFieldSelect wfPlanStat = new BeanFieldSelect("plan_plan_st_cd", "",
			"RYG");

	BeanFieldSelect wfEcpStat = new BeanFieldSelect("plan_ecp_st_cd", "", "RYG");

	BeanFieldSelect wfEngageStat = new BeanFieldSelect("plan_engage_st_cd", "",
			"RYG");

	BeanFieldSelect wfPrepStat = new BeanFieldSelect("plan_prep_st_cd", "",
			"RYG");

	BeanFieldSelect wfExecStat = new BeanFieldSelect("plan_exec_st_cd", "",
			"RYG");

	BeanFieldSelect wfEndStat = new BeanFieldSelect("plan_end_st_cd", "", "RYG");

	/*
	 * dates
	 */

	private static BeanFieldDate install_dt = new BeanFieldDate("install_dt");

	private static BeanFieldDate cab_dt = new BeanFieldDate("cab_dt");

	private static BeanFieldDate plan_sign_dt = new BeanFieldDate(
			"plan_sign_date");

	private static BeanFieldDate case_sign_dt = new BeanFieldDate(
			"case_sign_date");

	private static BeanFieldDate summary_sign_dt = new BeanFieldDate(
			"summary_sign_date");

	private static BeanFieldDate test_start_date = new BeanFieldDate(
			"test_start_date");

	private static BeanFieldDate test_end_date = new BeanFieldDate(
			"test_end_date");

	private static BeanFieldDate test_asof_date = new BeanFieldDate(
			"test_case_asof_date");

	private static BeanFieldDate summary_date = new BeanFieldDate(
			"summary_sign_date");

	/*
	 * blobs
	 */

	BeanFieldText wfStatusText = new BeanFieldText("status_summary_blob", 4, 80);

	BeanFieldText wfDesc = new BeanFieldText("desc_blob", 4, 80);

	BeanFieldText wfScope = new BeanFieldText("scope_blob", 4, 80);

	BeanFieldText wfInput = new BeanFieldText("entrance_criteria_blob", 4, 80);

	BeanFieldText wfRisk = new BeanFieldText("risk_blob", 4, 80);

	BeanFieldText wfMitigate = new BeanFieldText("mitagate_blob", 4, 80);

	BeanFieldText wfResult = new BeanFieldText("result_summary_blob", 4, 80);

	BeanFieldText wfActivity = new BeanFieldText("current_activity_blob", 4, 80);

	BeanFieldText wfPlans = new BeanFieldText("next_week_plans_blob", 4, 80);

	BeanFieldText wfPlanECP = new BeanFieldText("plan_ecp_blob", 4, 80);

	BeanFieldText wfPlanEngage = new BeanFieldText("plan_engage_blob", 4, 80);

	BeanFieldText wfPlanEndEngage = new BeanFieldText("plan_end_engage_blob",
			4, 80);

	BeanFieldText wfPlanPlan = new BeanFieldText("plan_plan_blob", 4, 80);

	BeanFieldText wfPlanPrep = new BeanFieldText("plan_prep_blob", 4, 80);

	BeanFieldText wfExec = new BeanFieldText("plan_exec_blob", 4, 80);

	BeanFieldText wfPlanEnd = new BeanFieldText("plan_end_blob", 4, 80);

	BeanFieldDisplay wfHost = new BeanFieldDisplay("host");

	BeanFieldDisplay wfTomcat = new BeanFieldDisplay("tomcat_name");

	/*
	 * List Filters
	 */

	private BeanFieldSelect filterStatus = new BeanFieldSelect(3,
			"FilterStatus", "status_cd", "O", "O", "Status?", "STATOIP");

	public TestPlanPlugin() throws services.ServicesException {
		super();

		this.setTableName("ttest");
		this.setKeyName("test_id");
		this.setTargetTitle("Test Plans / Status");

		this.setAddOk(false);
		this.setCopyOk(false);
		this.setNextOk(false);
		this.setSubmitOk(false);
		this.setShowAuditSubmitApprove(false);
		this.setDeleteOk(false);

		this.setListOrder("");

		//this.remedyKey = "sr_no";

		this.setRemedyOk(true);

		// filterStatus.setAltCodeSet(true);

		this.setListFilters(new BeanFieldSelect[] { filterStatus });

		this.setListHeaders(new String[] { "SR No.", "Title", "Sox", "Status",
				"Progress", "CAB", "Test Leader", "Phase", "Risk Area",
				"Plan Sign-Off", "Case Sign-Off", "Summary Sign-Off" });

		this
				.setMoreSelectColumns(new String[] {
						"trfc.rfc_id",
						"rem_stat.code_desc  as remedy_status ",
						"rem_urgency.code_desc as remedy_urgency",
						"concat(owner.last_name , ',' , owner.first_name) as remedyOwner  ",
						"concat(rqstr.last_name , ',' , rqstr.first_name) as remedyRequester ",
						"FormatDateTime(ttest.remedy_end_dt, 'mm/dd/yyyy') as fmt_remedy_end_dt",
						"FormatDateTime(ttest.remedy_requested_completion_dt, 'mm/dd/yyyy') as fmt_remedy_requested_completion_dt",
						"concat('<A href=Router?Target=RfcRipCab&Action=Show&Relation=this&RowKey=', trfc.rfc_id, '>Show CAB SR</A>') as srlink", });

		this
				.setMoreSelectJoins(new String[] {
						"left join trfc on ttest.cab_sr_no = trfc.rfc_no ",
						"left join tcodes rem_stat on ttest.status_cd = rem_stat.code_value and rem_stat.code_type_id = 118 ",
						"left join tcodes rem_urgency  on ttest.urgency_cd = rem_urgency.code_value and rem_urgency.code_type_id = 2 ",
						"left join tcontact rqstr on ttest.requester_uid = rqstr.contact_id ",
						"left join tcontact owner on ttest.owner_uid = owner.contact_id  " });

		this
				.setMoreListColumns(new String[] {
						"ttest.sr_no",
						"title_nm",
						"sox.code_desc as sox",
						"tcodes.code_desc2 as status_desc",
						"progress.code_desc as Progress",
						dbprefix
								+ "FormatDateTime(cab_dt,'mm/dd/yy') AS cab_dt",
						"leader.last_name as TestLeader",
						"phase.code_desc as phase_desc",
						"risk_blob",
						dbprefix
								+ "FormatDateTime(plan_sign_date,'mm/dd/yy') AS plan_dt",
						dbprefix
								+ "FormatDateTime(case_sign_date,'mm/dd/yy') AS sign_dt",
						dbprefix
								+ "FormatDateTime(summary_sign_date,'mm/dd/yy') AS summary_dt" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes sox on ttest.sox_compliance_cd = sox.code_value and sox.code_type_id  =  71 ",
						" left join tcodes on ttest.status_cd = tcodes.code_value and tcodes.code_type_id  =  93 ",
						" left join tcodes progress on ttest.progress_cd = progress.code_value and progress.code_type_id  =  71 ",
						" left join tuser leader on ttest.test_lead_uid = leader.user_id ",
						" left join tcodes phase on ttest.phase_cd = phase.code_value and phase.code_type_id  =  36 ", });

	}

	public boolean afterGet() {
		sm.setRfcNo(db.getText("sr_no"), db.getText("title_nm"));
		return true;

	}

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		wfHost.setDisplayValue(sm.getHost() + ":" + sm.getServerPort());
		wfTomcat.setDisplayValue(sm.getTomcatName());

		this.setWebFieldBeans(new BeanWebField[] { wfRemedyNo, wfTitle, wfPhase, wfScope,
				cab_dt, sr_no, wfStatus, wfProgress, wfStatusText, wfSox,
				wfResult, wfDesc, wfInput, wfRisk, test_lead, wfMitigate,
				plan_sign_dt, case_sign_dt, wfPlanSignStat, wfCaseSignStat,
				summary_sign_dt, test_start_date, test_end_date, wfPlanExec,
				wfActExec, wfPlanPass, wfActPass, test_asof_date, wfActivity,
				wfPlans, wfPlanECP, wfPlanEngage, wfPlanPlan, wfPlanPrep,
				wfExec, proj_manager, wfPlanEnd, wfPlanStat, wfEcpStat,
				wfExecStat, wfPlanStat, wfEndStat, wfEngageStat, wfPrepStat,
				wfPlanEndEngage, sr_link, summary_date, wfSummaryStat, cab_no,
				wfUrgency, wfOwner, wfRequestor, wfRemedyEndDt,
				wfRemedyRequestDt, wfRemedAsOf, wfHost, wfTomcat });

	}

}
