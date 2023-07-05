/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import db.DbFieldString;

import remedy.RemedyChangeSubmit;
import router.SessionMgr;
import services.ServicesException;
import forms.WebField;
import forms.WebFieldDate;
import forms.WebFieldDisplay;
import forms.WebFieldHidden;
import forms.WebFieldSelect;
import forms.WebFieldString;
import forms.WebFieldText;

/*******************************************************************************
 * Future REmedy
 * ... to create an SR/RFC in Remedy
 * ... instead of using 'init' action
 * 
 */

public class ServiceRequestRemedyPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public boolean remedyAddError = false;

	
	private static String[] sListHeaders = { "SR#", "Title",
			"--remedy staytus--", "--remedy Gruup", "Install&nbsp;Date",
			"--Requestor--" };

	public ServiceRequestRemedyPlugin() throws services.ServicesException {
		super();

		this.setTableName("tsr");
		this.setKeyName("sr_id");
		this.setListOrder("sr_no");

//		this.remedyType = "SR";
//		this.remedyKey = "sr_no";

		this.setListHeaders(sListHeaders);
		this.setSubmitOk(false);
		this.setNextOk(false);
		this.setListViewName("vsr_list");
		this.setSelectViewName("vsr");

		// link to child BT
		setHasDetailForm(true);
		setTargetTitle("Service Requests");
		setDetailTarget("ServiceRequestChild");
		setDetailTargetLabel("BT's");

	}
	

	public String getDataFormNamexx() {
		if (this.remedyAddError)
			return "Add";

		return "";

	}
	

	public void init(SessionMgr parmSm) {
		super.init(parmSm);

		if ((sm.Parm("Action").equalsIgnoreCase("Add") || getDataFormName()
				.equalsIgnoreCase("Add"))
				&& sm.Parm("FilterMode").equalsIgnoreCase("Initiate")) {
			this.setTemplateName("ServiceRequestInit.html");
		} else {
			this.setTemplateName("ServiceRequest.html");
		}
	}

	/*
	 * Allow GoTo on the List page
	 */
	public boolean gotoOk() {
		return true;
	}

	public String gotoDisplayName() {
		return "SR #: ";
	}

	public String gotoKeyName() {
		return "sr_no";
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

	public boolean listColumnHasSelector(int c) {
		// the status column (#2) has a selector, other fields do not
		if (c == 2 || c == 3 || c == 4 || c == 4)
			return true;
		else
			return false;
	}

	
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2) {

			return getListSelector("FilterStatus", "", "Status?", sm
					.getCodes("REMEDY"));
		}

		// filter Remedy group

		if (columnNumber == 3) {

			return getListSelector("FilterGroup", "", "Remedy Group?", sm
					.getCodesAlt("RMDYGRPS"));
		}

		/*
		 * Filter unique Install Dates!
		 */
		if (columnNumber == 4) {

			String dateCast = null;
			String qry = null;

			dateCast = " (FormatDateTime(end_dt, 'mm/dd/yy')) ";
			
			qry = " select distinct " + dateCast + "," + dateCast + ","
					+ dateCast + " from tremedy where end_dt != ''";

			// MYSQL
			// String qry = "select distinct date_format(review_date,
			// '%Y-%m-%d') , date_format(review_date, '%Y-%m-%d') ,
			// date_format(review_date, '%m/%d/%Y') as 'review_date' from
			// trfc s ";

			Hashtable dates = new Hashtable();

			try {
				dates = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterReview", "O", "Install Date?", dates);

		}

		/*
		 * get unique install date
		 */
		if (columnNumber == 8) {

			String dateCast = null;
			String qry = null;

			dateCast = " ( " + this.dbprefix
					+ " FormatDateTime(remedy_end_dt, 'mm/dd/yy')) ";
			qry = " select distinct "
					+ dateCast
					+ ","
					+ dateCast
					+ ","
					+ dateCast
					+ " from trfc where remedy_end_dt != '' AND status_cd <> 'Clo' ";

			Hashtable dates = new Hashtable();

			try {
				dates = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterInstall", "O", "Install On?", dates);

		}

		// impossible!
		return getListSelector("dummy", "", "", sm.getCodes(""));

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// for NCF Cab.. show all, Else for local cab, then filter on
		// mstr_suite_cd

		SimpleDateFormat sdf_from_browser = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat sdf_to_query = new SimpleDateFormat("MM/dd/yyyy");

		if ((sm.Parm("FilterGroup").length() > 0)
				&& (!sm.Parm("FilterGroup").equalsIgnoreCase("0"))) {
			sb.append(" AND remedy_grp_tx = '" + sm.Parm("FilterGroup") + "'");
		}

		if (sm.Parm("FilterFromDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterFromDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND remedy_end_dt >= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("TCAB - error parsing install FROM date : "
						+ sm.Parm("FilterStartDate"));
			}
		}

		if (sm.Parm("FilterToDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterToDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND remedy_end_dt <= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("TCAB - error parsing install TO date : "
						+ sm.Parm("FilterStartDate"));
			}
		}

		// Filter Remedy status - default to no filter

		if (sm.Parm("FilterStatus").length() == 0) {

		} else {
			if (sm.Parm("FilterStatus").equalsIgnoreCase("ZZZ")) {
				sb
						.append(" AND (status_cd != 'CLO' AND tsr.status_cd != 'RES' ) ");
			} else {
				if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
					sb.append(" AND status_cd = '" + sm.Parm("FilterStatus")
							+ "'");
				}
			}
		}

		// filter remedy_end_dt / insall date

		// .. tricky one : remedy_end_dt also has time portion

		if (sm.Parm("FilterInstall").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterInstall").equalsIgnoreCase("0")) {

				sb.append(" AND (remedy_end_dt >= '" + sm.Parm("FilterInstall")
						+ "' AND '" + sm.Parm("FilterInstall")
						+ "' > dateadd(day,-1,remedy_end_dt)) ");

			}
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = (parmMode.equalsIgnoreCase("add") || getDataFormName()
				.equalsIgnoreCase("Add")) ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("sr_id"), db.getText("title_nm"));
		}

		if (addMode & sm.Parm("FilterRemedyMode").equalsIgnoreCase("Initiate")) {
			return putAddFields(ht);
		}
		
		// save the tremedy.remedy_id 
		ht.put("remedyid", new WebFieldHidden("remedyid", addMode ? "0" : db.getText("remedy_id")));
		
		
		// TODO.. FIX THIS.
		ht.put("remedy_item_cd", new WebFieldDisplay("remedy_item_cd", ""));

		ht.put("prefix", new WebFieldString("prefix", "", 12, 12));

		ht.put("tieline", new WebFieldString("tieline", "", 12, 12));

		ht.put("floor", new WebFieldString("floor", "", 12, 12));

		/***********************************************************************
		 * 
		 * SOAP Interface to Remedy !
		 * 
		 */

		// Put user message
		ht.put("msg", new WebFieldDisplay("msg", remedy_result));

		String[][] notify_cd = new String[][] {
				{ "PH", "EM", "TR", "CM", "TM" },
				{ "Phone", "EMail", "Training", "Communication", "Tempate" } };

		ht.put("trigger_cd", new WebFieldSelect("trigger_cd", addMode ? "" : db
				.getText("trigger_cd"), sm.getCodesAlt("TRIGGER")));

		ht.put("notify_cd", new WebFieldSelect("notify_cd", addMode ? "" : db
				.getText("notify_cd"), notify_cd));

		ht.put("all_instance_cd", new WebFieldSelect("all_instance_cd",
				addMode ? "" : db.getText("all_instance_cd"), sm
						.getCodes("YESNO")));

		ht.put("bsns_init_flag", new WebFieldSelect("bsns_init_flag",
				addMode ? "" : db.getText("bsns_init_flag"), sm
						.getCodes("YESNO")));

		ht.put("tested_cd", new WebFieldSelect("tested_cd", addMode ? "" : db
				.getText("tested_cd"), sm.getCodes("YESNO")));

		ht.put("intrusive_cd", new WebFieldSelect("intrusive_cd", addMode ? ""
				: db.getText("intrusive_cd"), sm.getCodes("YESNO")));

		ht.put("sizing_cd", new WebFieldSelect("sizing_cd", addMode ? "" : db
				.getText("sizing_cd"), sm.getCodesAlt("HIGHMEDLOW")));

		ht.put("rtn_maint_cd", new WebFieldSelect("rtn_maint_cd", addMode ? ""
				: db.getText("rtn_maint_cd"), sm.getCodes("YESNO")));

		/*
		 * Secured Fields
		 */

		if (sm.userIsExecutive()) {

			ht.put("received_date", new WebFieldDate("received_date", db
					.getText("received_date")));

			ht.put("fcab_review_cd", new WebFieldSelect("fcab_review_cd", db
					.getText("fcab_review_cd"), sm.getCodes("STATREVIEW")));

			ht.put("compliant_cd", new WebFieldSelect("compliant_cd",
					addMode ? "" : db.getText("compliant_cd"), sm
							.getCodes("YESNO")));

			ht.put("suite_review_cd", new WebFieldSelect("suite_review_cd", db
					.getText("suite_review_cd"), sm.getCodes("STATREVIEW")));

			ht.put("suite_decision_date", new WebFieldDate(
					"suite_decision_date", addMode ? "" : db
							.getText("suite_decision_date")));

			ht.put("resolution_blob", new WebFieldText("resolution_blob", db
					.getText("resolution_blob"), 3, 60));

			// tricky one... fields without "_" are not auto-populated to database
			
			ht.put("comment-blob", new WebFieldText("comment-blob", db
					.getText("comment_blob"), 5, 100));

			ht.put("tech_notes_blob", new WebFieldText("tech_notes_blob", db
					.getText("tech_notes_blob"), 5, 100));

			ht.put("bsns_depend_blob", new WebFieldText("bsns_depend_blob", db
					.getText("bsns_depend_blob"), 5, 100));

		} else {

			ht.put("compliant_cd", new WebFieldDisplay("compliant_cd", db
					.getText("compliant_status")));
			ht.put("fcab_review_cd", new WebFieldDisplay("fcab_review_cd", db
					.getText("fcabstatus")));
			ht.put("suite_decision_date", new WebFieldDisplay(
					"suite_decision_date", db.getText("suite_decision_date")));
			ht.put("suite_review_cd", new WebFieldDisplay("suite_review_cd", db
					.getText("suitestatus")));
			ht.put("resolution_blob", new WebFieldDisplay(db
					.getText("resolution_blob")));
			ht.put("comment_blob", new WebFieldDisplay(db
					.getText("comment_blob")));
			ht.put("received_date", new WebFieldDisplay(db
					.getText("received_date")));

		}

		/*
		 * Codes
		 */

		ht.put("ac_stat_cd", new WebFieldSelect("ac_stat_cd", (addMode ? ""
				: db.getText("ac_stat_cd")), sm.getCodes("KPACSTAT")));

		ht.put("safety_cd", new WebFieldSelect("safety_cd", (addMode ? "" : db
				.getText("safety_cd")), sm.getCodes("YESNO")));

		ht.put("suite_cd", new WebFieldSelect("suite_cd", addMode ? "" : db
				.getText("suite_cd"), sm.getCodes("SUITES")));

		ht.put("release_cd", new WebFieldSelect("release_cd", db
				.getText("release_cd"), sm.getCodes("KRLSE"), "Release?"));

		/*
		 * Impacts
		 */

		ht.put("test_sign_off_cd", new WebFieldSelect("test_sign_off_cd",
				addMode ? "" : db.getText("test_sign_off_cd"), sm
						.getCodes("YESNO")));

		ht.put("interface_sign_off_cd", new WebFieldSelect(
				"interface_sign_off_cd", addMode ? "" : db
						.getText("interface_sign_off_cd"), sm
						.getCodes("YESNONA")));

		ht.put("security_sign_off_cd", new WebFieldSelect(
				"security_sign_off_cd", addMode ? "" : db
						.getText("security_sign_off_cd"), sm
						.getCodes("YESNONA")));

		ht.put("asm_impact_cd", new WebFieldSelect("asm_impact_cd",
				addMode ? "" : db.getText("asm_impact_cd"), sm
						.getCodes("YESNONA")));

		ht.put("epic_only_cd", new WebFieldSelect("epic_only_cd", addMode ? ""
				: db.getText("epic_only_cd"), sm.getCodes("YESNONA")));

		ht.put("x_suite_cd", new WebFieldSelect("x_suite_cd", addMode ? "" : db
				.getText("x_suite_cd"), sm.getCodes("YESNONA")));

		ht.put("x_epic_prod_cd", new WebFieldSelect("x_epic_prod_cd",
				addMode ? "" : db.getText("x_epic_prod_cd"), sm
						.getCodes("YESNONA")));

		/*
		 * Remedy Fields
		 */

		if (true) {

			ht.put("release_related_cd", new WebFieldSelect(
					"release_related_cd", (addMode ? "" : db
							.getText("release_related_cd")), sm
							.getCodes("YESNO"), false, false));

			ht.put("hotlist_cd", new WebFieldSelect("hotlist_cd", (addMode ? ""
					: db.getText("hotlist_cd")), sm.getCodes("YESNO"), false,
					false));

			ht.put("regulatory_cd", new WebFieldSelect("regulatory_cd",
					(addMode ? "" : db.getText("regulatory_cd")), sm
							.getCodes("YESNO"), false, false));

			ht.put("escalated_cd", new WebFieldSelect("escalated_cd",
					(addMode ? "" : db.getText("escalated_cd")), sm
							.getCodes("YESNO"), false, false));

			ht.put("priority_cd", new WebFieldSelect("priority_cd",
					addMode ? "" : db.getText("priority_cd"), sm
							.getCodes("PRIORITY")));

			ht.put("emergency_cd", new WebFieldSelect("emergency_cd",
					(addMode ? "" : db.getText("emergency_cd")), sm
							.getCodes("YESNO"), false, false));

			ht.put("impact_cd", new WebFieldSelect("impact_cd", (addMode ? ""
					: db.getText("impact_cd")), sm.getCodes("SEVERITY"), false,
					false));

			ht.put("urgency_cd", new WebFieldSelect("urgency_cd", (addMode ? ""
					: db.getText("urgency_cd")), sm.getCodes("SEVERITY"),
					false, false));

			ht.put("expedited_cd", new WebFieldSelect("expedited_cd",
					(addMode ? "" : db.getText("expedited_cd")), sm
							.getCodes("YESNO"), false, false));

			ht.put("status_cd", new WebFieldDisplay("status_cd", addMode ? ""
					: db.getText("remedy_status")));

			ht.put("worklog_blob", new WebFieldDisplay("worklog_blob",
					(addMode ? "" : db.getText("worklog_blob"))));

			ht.put("remedy_effort_tx", new WebFieldDisplay("remedy_effort_tx",
					(addMode ? "" : db.getText("remedy_effort_tx"))));

			ht.put("remedy_requested_completion_dt", new WebFieldDisplay(
					"remedy_requested_completion_dt", addMode ? "" : db
							.getText("fmt_remedy_requested_completion_dt")));

			ht.put("remedy_create_dt", new WebFieldDisplay("remedy_create_dt",
					addMode ? "" : db.getText("remedy_create_dt")));

			ht.put("remedy_cat_tx", new WebFieldDisplay("remedy_cat_tx",
					(addMode ? "" : db.getText("remedy_cat_tx"))));

			ht.put("remedy_owner_tx", new WebFieldDisplay("remedy_owner_tx",
					(addMode ? "" : db.getText("remedy_owner_tx"))));

			ht.put("remedy_type_tx", new WebFieldDisplay("remedy_type_tx",
					(addMode ? "" : db.getText("remedy_type_tx"))));

			ht.put("remedy_grp_tx", new WebFieldDisplay("remedy_grp_tx",
					(addMode ? "" : db.getText("remedy_grp_tx"))));

			ht.put("remedy_approve_cd", new WebFieldSelect("remedy_approve_cd",
					(addMode ? "" : db.getText("remedy_approve_cd")), sm
							.getCodes("RMDYAPRV"), false, false));

			ht.put("closure_cd", new WebFieldSelect("closure_cd", addMode ? ""
					: db.getText("closure_cd"), sm.getCodes("RMDYRSOLV"),
					false, false));

			ht.put("outage_cd", new WebFieldSelect("outage_cd", (addMode ? ""
					: db.getText("outage_cd")), sm.getCodes("YESNO"), false,
					false));

			debug("service req.. build lead");

			ht.put("build_lead_uid", new WebFieldSelect("build_lead_uid",
					addMode ? new Integer("0") : db
							.getInteger("build_lead_uid"), sm.getLeaderHT(),
					true));

			debug("service req.. build owner");

			ht.put("builder_owner_uid", new WebFieldSelect("builder_owner_uid",
					addMode ? new Integer("0") : db
							.getInteger("builder_owner_uid"), sm.getLeaderHT(),
					true));

			debug("service req.. pm");

			ht.put("pm_uid", new WebFieldSelect("pm_uid",
					addMode ? new Integer("0") : db.getInteger("pm_uid"), sm
							.getUserHT()));

			ht.put("product_cd", new WebFieldSelect("product_cd", addMode ? sm
					.getUserProduct() : db.getText("product_cd"), sm
					.getCodes("PRODUCTS"), true));

			debug("service req.. submitter");

			ht.put("submitter_uid",
					new WebFieldSelect("submitter_uid", addMode ? new Integer(
							"0") : db.getInteger("submitter_uid"), sm
							.getContactHT(), true, false));

			ht.put("owner-uid", new WebFieldSelect("owner-uid",
					addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
							.getUserHT()));

			ht.put("requester_uid",
					new WebFieldSelect("requester_uid", addMode ? new Integer(
							"0") : db.getInteger("requester_uid"), sm
							.getContactHT(), true, false));

		}

		/*
		 * 
		 */

		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getApplicationId() : (Integer) db
						.getObject("application_id"),
				sm.getApplicationFilter(), true));

		ht.put("test_approve_uid", new WebFieldSelect("test_approve_uid",
				addMode ? new Integer("0") : db.getInteger("test_approve_uid"),
				sm.getTestorHT(), "-- Pending Approval --"));

		ht.put("build_track_no", new WebFieldString("build_track_no",
				(addMode ? "" : db.getText("build_track_no")), 6, 6));

		/*
		 * Strings
		 */

		ht.put("tech_status_tx", new WebFieldString("tech_status_tx",
				(addMode ? "" : db.getText("tech_status_tx")), 65, 128));

		ht.put("tech_person_tx", new WebFieldString("tech_person_tx",
				(addMode ? "" : db.getText("tech_person_tx")), 45, 45));

		ht.put("bsns_submitter_nm", new WebFieldString("bsns_submitter_nm",
				(addMode ? "" : db.getText("bsns_submitter_nm")), 45, 45));

		ht.put("bsns_grp_nm", new WebFieldString("bsns_grp_nm", (addMode ? ""
				: db.getText("bsns_grp_nm")), 45, 45));

		ht.put("bsns_proj_nm", new WebFieldString("bsns_proj_nm", (addMode ? ""
				: db.getText("bsns_proj_nm")), 100, 128));

		ht.put("title-nm", new WebFieldString("title-nm", (addMode ? "" : db
				.getText("title_nm")), 100, 128));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("assoc_rfc_tx", new WebFieldString("assoc_rfc_tx", (addMode ? ""
				: db.getText("assoc_rfc_tx")), 45, 45));

		ht.put("sizing_tx", new WebFieldString("sizing_tx", (addMode ? "" : db
				.getText("sizing_tx")), 32, 32));

		ht.put("off_cycle_rsn_tx", new WebFieldString("off_cycle_rsn_tx",
				(addMode ? "" : db.getText("off_cycle_rsn_tx")), 100, 255));

		ht.put("adhoc_tx", new WebFieldString("adhoc_tx", (addMode ? "" : db
				.getText("adhoc_tx")), 100, 255));

		// ----------------- Remedy fields ---------------------------*

		// for appending text to Remedy....see the absRemedyPlugin that passes
		// this to Remedy
		ht.put("worklog", new WebFieldString("worklog", (addMode ? "" : ""),
				80, 255));

		ht.put("status_cd", new WebFieldDisplay("status_cd", (addMode ? "" : db
				.getText("remedy_status"))));

		ht.put("bsns_need_blob", new WebFieldText("bsns_need_blob", db
				.getText("bsns_need_blob"), 5, 100));

		ht.put("description-blob", new WebFieldText("description-blob", db
				.getText("description_blob"), 5, 100));

		// -------------- end of remedy fields -------------*

		ht.put("tested_nm", new WebFieldString("tested_nm", (addMode ? "" : db
				.getText("tested_nm")), 32, 32));

		ht.put("remdedyno", new WebFieldString("remedyno",  (addMode ? "0" : db
				.getText("sr_no")), 8, 8));

		ht.put("remedy_asof_date", new WebFieldDisplay("remedy_asof_date",
				(addMode ? "" : db.getText("remedy_asof_date"))));

		ht.put("remedy_item_tx", new WebFieldDisplay("remedy_item_tx",
				(addMode ? "" : db.getText("remedy_item_tx"))));

		ht.put("remedy_grp_tx", new WebFieldDisplay("remedy_grp_tx",
				(addMode ? "" : db.getText("remedy_grp_tx"))));

		ht.put("defect_nm", new WebFieldString("defect_nm", (addMode ? "" : db
				.getText("defect_nm")), 8, 8));

		ht.put("problem_nm", new WebFieldString("problem_nm", (addMode ? ""
				: db.getText("problem_nm")), 16, 16));

		ht.put("related_nm", new WebFieldString("related_nm", (addMode ? ""
				: db.getText("related_nm")), 16, 16));

		ht.put("release_tx", new WebFieldString("release_tx", (addMode ? ""
				: db.getText("release_tx")), 32, 32));

		/*
		 * Dates
		 */

		ht.put("bsns_submit_dt", new WebFieldDate("bsns_submit_dt",
				addMode ? "" : db.getText("bsns_submit_dt")));

		ht.put("bsns_expect_dt", new WebFieldDate("bsns_expect_dt",
				addMode ? "" : db.getText("bsns_expect_dt")));

		ht.put("est_complete_dt", new WebFieldDate("est_complete_dt",
				addMode ? "" : db.getText("est_complete_dt")));

		ht.put("ncab_review_date", new WebFieldDate("ncab_review_date",
				addMode ? "" : db.getText("ncab_review_date")));

		ht.put("fcab_review_date", new WebFieldDate("fcab_review_date",
				addMode ? "" : db.getText("fcab_review_date")));

		ht.put("lcab_review_date", new WebFieldDate("lcab_review_date",
				addMode ? "" : db.getText("lcab_review_date")));

		ht.put("assign_date", new WebFieldDate("assign_date", addMode ? "" : db
				.getText("assign_date")));

		ht.put("created_date", new WebFieldDate("created_date", addMode ? ""
				: db.getText("created_date")));

		ht.put("request_date", new WebFieldDate("request_date", addMode ? ""
				: db.getText("request_date")));

		ht.put("required_date", new WebFieldDate("required_date", addMode ? ""
				: db.getText("required_date")));

		ht.put("end_dt", new WebFieldDisplay("end_dt",
				addMode ? "" : db.getText("fmt_remedy_end_dt")));

		ht.put("suite_review_date", new WebFieldDate("suite_review_date",
				addMode ? "" : db.getText("suite_review_date")));

		ht.put("review_date", new WebFieldDate("review_date", addMode ? "" : db
				.getText("review_date")));

		ht.put("install_hours_tx", new WebFieldDate("install_hours_tx",
				addMode ? "" : db.getText("install_hours_tx")));

		/*
		 * Blobs
		 */

		ht.put("release_blob", new WebFieldText("release_blob", addMode ? ""
				: db.getText("release_blob"), 3, 100));

		ht.put("user_blob", new WebFieldText("user_blob", addMode ? "" : db
				.getText("user_blob"), 5, 100));

		ht.put("ancillary_blob", new WebFieldText("ancillary_blob",
				addMode ? "" : db.getText("ancillary_blob"), 5, 100));

		ht.put("impact_blob", new WebFieldText("impact_blob", addMode ? "" : db
				.getText("impact_blob"), 5, 100));

		ht.put("test_blob", new WebFieldText("test_blob", addMode ? "" : db
				.getText("test_blob"), 5, 100));

		/*
		 * Return
		 */

		return ht;

	}
	
	// get the remedy data via web service call in 'pre-display' processing...
	// and
	// save to database.

	// send the request to Remedy
	public boolean beforeAdd(Hashtable ht) {

		// put the mstr_suite_cd (R=RipCab or A=Amb cab)

		// OBSOLETE I THINK
		
		String mstr_suite = ""; // like r=rev capture, a=ambulatory, maybe later
		// i=ip
		
		// should not be adding from NCF-CAB plugin.. for now.
		if (mstr_suite.length() < 3) {
			ht.put("mstr_suite_cd", new DbFieldString("mstr_suite_cd",
					mstr_suite));

		} else {
			// default to RIP CAB for now
			ht.put("mstr_suite_cd", new DbFieldString("mstr_suite_cd", "R"));
		}

		// only add RFC/SR if in Initiate mode

		if (!sm.Parm("FilterMode").equalsIgnoreCase("Initiate")) {
			return true;
		}

		debug("adding to Remedy!!!!");

		RemedyChangeSubmit req = new RemedyChangeSubmit(sm.getRemedyUserid(),
				sm.getRemedyPassword(), sm.getRemedyURL());

		// look-up the remedy item text from the code
		String remedyItem = sm.getCodeDescription(sm.getCodes("RMYSRITEMS"), sm
				.Parm("remedy_item_cd"));

		String remedyCTI_Type = "KPHC "
				+ sm.getCodeDescription(sm.getCodesAlt("RMYSRITEMS"), sm
						.Parm("remedy_item_cd"));

		String sql = "select category_nm, item_nm from tRemedyCTI where entry_id = "
				+ sm.Parm("remedy_item_cd");

		try {
			ResultSet rs = db.getRS(sql);

			if (rs.next() == true) {

				remedyItem = rs.getString("category_nm");
				remedyCTI_Type = rs.getString("item_nm");
			}
		} catch (Exception e) {

		}
		// probably a better way would be to send each parm individually

		/*
		 * Preferred way to call... using properties via setter methods or
		 * default
		 */

		req.setRemedyCategory("End User Applications");
		req.setRemedyType(remedyCTI_Type);
		req.setRemedyItem(remedyItem);

		String case_id = req.submitChange("SR", sm.getHandle(), sm
				.getFirstName()
				+ " " + sm.getLastName());

		if (!case_id.equalsIgnoreCase("0")) {

			//ht.put(",
			//		new DbFieldString(remedyKey, case_id.substring(4)));
			return true;

		} else {
			remedy_result = req.getErrorText();
			remedyAddError = true; // set up to force the add page over again
			// in getDataFormName
			System.out.println("\nError ==> " + req.getErrorText());
			return false;
		}
	}
	
	/*
	 * Put out initial Remedy fields for an add
	 */

	public Hashtable putAddFields(Hashtable ht) {

		boolean redo = remedyAddError;

		ht.put("msg", new WebFieldDisplay("msg", remedy_result));

		ht.put("buildlink", new WebFieldDisplay("buildlink", ""));

		ht.put("requester_uid",
				new WebFieldSelect("requester_uid", redo ? new Integer(sm
						.Parm("requester_uid")) : new Integer("0"), sm
						.getContactHT(), true));

		ht.put("title_nm", new WebFieldString("title_nm", redo ? sm
				.Parm("title_nm") : "", 100, 128));

		ht.put("suite_cd", new WebFieldSelect("suite_cd", redo ? sm
				.Parm("suite_cd") : "", sm.getCodes("SUITES"), true, true));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", redo ? sm
				.Parm("priority_cd") : "L", sm.getCodes("PRIORITY")));

		ht.put("application_id", new WebFieldSelect("application_id",
				redo ? new Integer(sm.Parm("application_id")) : sm
						.getApplicationId(), sm.getApplicationFilter(), true));

		String remedy_qry = new String(
				"select order_by, code_value, (code_desc2 + ' / ' + code_desc) as thedesc from tcodes where code_type_id =128");

		String remedy_qry2 = new String(
				"SELECT entry_id, entry_id, (type_nm + ' / '  + item_nm) as thedesc from tRemedyCTI where application = 'MAINCHANGE'");

		Hashtable rmy_ht = new Hashtable();
		try {
			rmy_ht = db.getLookupTable(remedy_qry2);
		} catch (services.ServicesException e) {

		}

		ht.put("remedy_item_cd", new WebFieldSelect("remedy_item_cd", redo ? sm
				.Parm("remedy_item_cd") : "", rmy_ht));

		ht.put("remedy_item_tx", new WebFieldString("remedy_item_tx",
				(redo ? sm.Parm("remedy_item_tx") : db
						.getText("remedy_item_tx")), 64, 64));

		ht.put("safety_cd", new WebFieldSelect("safety_cd", redo ? sm
				.Parm("safety_cd") : "N", sm.getCodes("YESNO")));

		ht.put("mcv_std_cd", new WebFieldDisplay("mcv_std_cd", ""));

		ht.put("impact_cd",
				new WebFieldSelect("impact_cd", redo ? sm.Parm("impact_cd")
						: "L", sm.getCodes("SEVERITY"), false, false));

		ht.put("regulatory_cd", new WebFieldSelect("regulatory_cd", redo ? sm
				.Parm("regulatory_cd") : "N", sm.getCodes("YESNO"), false,
				false));

		ht.put("hotlist_cd", new WebFieldSelect("hotlist_cd", redo ? sm
				.Parm("hotlist_cd") : "N", sm.getCodes("YESNO"), false, false));

		ht.put("emergency_cd",
				new WebFieldSelect("emergency_cd", redo ? sm
						.Parm("emergency_cd") : "N", sm.getCodes("YESNO"),
						false, false));

		ht.put("release_related_cd", new WebFieldSelect("release_related_cd",
				redo ? sm.Parm("release_related_cd") : "N", sm
						.getCodes("YESNO"), false, false));

		ht.put("expedited_cd",
				new WebFieldSelect("expedited_cd", redo ? sm
						.Parm("expedited_cd") : "N", sm.getCodes("YESNO"),
						false, false));

		ht.put("outage_cd", new WebFieldSelect("outage_cd", redo ? sm
				.Parm("outage_cd") : "N", sm.getCodes("YESNO"), false, false));

		ht.put("urgency_cd", new WebFieldSelect("urgency_cd", redo ? sm
				.Parm("urgency_cd") : "L", sm.getCodes("SEVERITY"), false,
				false));

		ht.put("remedy_start_dt", new WebFieldString("remedy_start_dt",
				redo ? sm.Parm("remedy_start_dt") : "", 12, 12));

		ht.put("remedy_end_dt", new WebFieldString("remedy_end_dt", redo ? sm
				.Parm("remedy_end_dt") : "", 12, 12));

		ht.put("install_date", new WebFieldString("install_date", redo ? sm
				.Parm("install_date") : "", 12, 12));

		ht.put("description_blob", new WebFieldText("description_blob",
				redo ? sm.Parm("description_blob") : "", 5, 100));

		ht.put("bsns_need_blob", new WebFieldText("bsns_need_blob", redo ? sm
				.Parm("bsns_need_blob") : "", 5, 100));

		ht.put("worklog_blob", new WebFieldText("worklog_blob", redo ? sm
				.Parm("worklog_blob") : "", 5, 100));

		ht.put("implementation_blob", new WebFieldText("implementation_blob",
				redo ? sm.Parm("implementation_blob") : "", 5, 100));

		ht.put("test_blob", new WebFieldText("test_blob", redo ? sm
				.Parm("test_blob") : "", 5, 100));

		ht.put("ac_comment_blob", new WebFieldText("ac_comment_blob", redo ? sm
				.Parm("ac_comment_blob") : "", 5, 100));

		ht.put("backout_blob", new WebFieldText("backout_blob", redo ? sm
				.Parm("backout_blob") : "", 5, 100));

		// transient fields.. just for remedy, not saved

		ht.put("prefix", new WebFieldString("prefix", redo ? sm.Parm("prefix")
				: "", 4, 4));

		ht.put("tieline", new WebFieldString("tieline", redo ? sm
				.Parm("tieline") : "", 12, 12));

		ht.put("floor", new WebFieldString("floor", redo ? sm.Parm("floor")
				: "", 2, 2));

		return ht;
	}

}
