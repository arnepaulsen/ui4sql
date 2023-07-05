/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import services.ServicesException;
import forms.WebField;
import forms.WebFieldCheckbox;
import forms.WebFieldDate;
import forms.WebFieldDisplay;
import forms.WebFieldSelect;
import forms.WebFieldString;
import forms.WebFieldText;

/*******************************************************************************
 * Change Approval Board - Review
 * 
 * Keywords - sql server only
 * 
 * Change Log:
 * 
 * 12/19/06
 * 
 * Wow! First plugin to use /a date filter on the list bar
 * 
 */

public class ServiceRequestPriorityPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private static String[] sListHeaders = { "SR#", "Title",
			"--remedy staytus--", "--remedy Gruup", "Install&nbsp;Date",
			"--Requestor--" };

	private static String[] extraListJoins = {
			" left join tcontact on tsr.requester_uid = tcontact.contact_id",
			" left join tcodes status on tsr.status_cd = status.code_value and status.code_type_id  = 118 ",
			" left join tcodes suite_stat on tsr.suite_review_cd = suite_stat.code_value and suite_stat.code_type_id  = 105 ",
			" left join tcodes fcab_stat on tsr.fcab_review_cd = fcab_stat.code_value and fcab_stat.code_type_id  = 105 ",
			" left join tcodes install on tsr.install_cd = install.code_value and install.code_type_id  = 3 ",
			" left join tcodes sfty on tsr.safety_cd = sfty.code_value and sfty.code_type_id  = 3 ",
			" left join tcodes compliant on tsr.compliant_cd = compliant.code_value and compliant.code_type_id  = 3 ",
			" left join tapplications on tsr.application_id = tapplications.application_id ",
			" left join tcodes rem_stat on tsr.status_cd = rem_stat.code_value and rem_stat.code_type_id = 118 ", };

	public ServiceRequestPriorityPlugin() throws services.ServicesException {
		super();
		//this.remedyType = "SR";
		//this.remedyKey = "sr_no";

		this.setTableName("tsr");
		this.setKeyName("sr_id");
		this.setListOrder("sr_no");

		this
				.setMoreListColumns(new String[] {
						"rem_stat.code_desc  as remedy_status ",
						this.dbprefix
								+ "FormatDateTime(remedy_requested_completion_dt, 'mm/dd/yyyy') as fmt_remedy_requested_completion_dt",
						this.dbprefix
								+ "FormatDateTime(remedy_end_dt, 'mm/dd/yyyy') as fmt_remedy_end_dt" });

		this.setMoreListJoins(extraListJoins);
		this.setMoreSelectJoins(extraListJoins);
		this.setListHeaders(sListHeaders);
		this.setMoreSelectColumns(this.moreListColumns);
		this.setTargetTitle("Service Request Prioritization");

		this.setTemplateName("ServiceRequestPriority.html");
		this.setUpdatesOk(false);

		this.setGotoOk(true);
		this.setGotoDisplayName("SR #: ");
		this.setGotoKeyName("sr_no");

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

			dateCast = " ( " + this.dbprefix + "FormatDateTime(remedy_end_dt, 'mm/dd/yy')) ";
			qry = " select distinct " + dateCast + "," + dateCast + ","
					+ dateCast + " from tsr where remedy_end_dt != ''";

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

			dateCast = " ( " + this.dbprefix + "FormatDateTime(remedy_end_dt, 'mm/dd/yy')) ";
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
			sb.append(" AND remedy_grp_cd = '" + sm.Parm("FilterGroup") + "'");
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
						.append(" AND (tsr.status_cd != 'CLO' AND tsr.status_cd != 'RES' ) ");
			} else {
				if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
					sb.append(" AND tsr.status_cd = '"
							+ sm.Parm("FilterStatus") + "'");
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

		/***********************************************************************
		 * 
		 * SOAP Interface to Remedy !
		 * 
		 */

		// Put user message
		ht.put("msg", new WebFieldDisplay("msg", "Enter priority info."));

		/*
		 * Secured Fields
		 */

		if (sm.userIsExecutive()) {

		} else {

			ht.put("comment_blob", new WebFieldDisplay(db
					.getText("comment_blob")));

		}

		/*
		 * Codes
		 */

		debug("codes1");

		ht.put("fac_priority_cd", new WebFieldSelect("fac_priority_cd",
				(addMode ? "" : db.getText("fac_priority_cd")), sm
						.getCodes("RANK10")));

		ht.put("safety_cd", new WebFieldSelect("safety_cd", (addMode ? "" : db
				.getText("safety_cd")), sm.getCodes("YESNO")));

		ht.put("suite_cd", new WebFieldSelect("suite_cd", addMode ? "" : db
				.getText("suite_cd"), sm.getCodes("SUITES")));

		String[][] comm_cd = new String[][] { { "1", "2", "3", "S" },
				{ "Class I", "Class II", "Class III", "SSS Only" } };

		String[][] priorties = new String[][] { { "C", "H", "M", "W" },
				{ "Critical", "High/Med", "Low", "Wish List" } };

		String[][] loe = new String[][] { { "B", "S", "N" },
				{ "Big", "Small", "N/A" } };

		String[][] approved = new String[][] { { "P", "A", "Denied" },
				{ "Pending", "Approved", "Denied" } };

		String[][] approvers = new String[][] { { "NA", "BW", "BH" },
				{ " ", "Bettianne Wiessler", "Brian Hoberman" } };

		ht.put("comm_class_cd", new WebFieldSelect("comm_class_cd",
				addMode ? "" : db.getText("comm_class_cd"), comm_cd));

		ht.put("comm_class_leader_cd", new WebFieldSelect(
				"comm_class_leader_cd", addMode ? "" : db
						.getText("comm_class_leader_cd"), comm_cd));

		ht.put("epic_loe_cd", new WebFieldSelect("epic_loe_cd", addMode ? ""
				: db.getText("epic_loe_cd"), loe));

		ht.put("config_loe_cd", new WebFieldSelect("config_loe_cd",
				addMode ? "" : db.getText("config_loe_cd"), loe));

		ht.put("recomend_priority_cd", new WebFieldSelect(
				"recomend_priority_cd", addMode ? "" : db
						.getText("recomend_priority_cd"), priorties));

		ht.put("ip_apv_cd", new WebFieldSelect("ip_apv_cd", addMode ? "" : db
				.getText("ip_apv_cd"), approved));

		ht.put("ip_leader_apv_cd", new WebFieldSelect("ip_leader_apv_cd",
				addMode ? "" : db.getText("ip_leader_apv_cd"), approved));

		ht.put("ip_approver_cd", new WebFieldSelect("ip_approver_cd",
				addMode ? "" : db.getText("ip_approver_cd"), approvers));

		/*
		 * Remedy
		 */

		ht.put("status_cd", new WebFieldDisplay("status_cd", (addMode ? "" : db
				.getText("remedy_status"))));

		/*
		 * 
		 */

		debug("appl id");
		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getApplicationId() : (Integer) db
						.getObject("application_id"),
				sm.getApplicationFilter(), true));

		/*
		 * Strings
		 */

		ht.put("sr_no", new WebFieldDisplay("sr_no", db.getText("sr_no")));

		ht.put("train_hrs_tx", new WebFieldString("train_hrs_tx", (addMode ? ""
				: db.getText("train_hrs_tx")), 12, 12));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 100, 128));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("sizing_tx", new WebFieldString("sizing_tx", (addMode ? "" : db
				.getText("sizing_tx")), 32, 32));

		/*
		 * Flags
		 * 
		 */

		ht.put("domain_apv_flag", new WebFieldCheckbox("domain_apv_flag",
				addMode ? "" : db.getText("domain_apv_flag"), ""));

		debug("flags2");
		ht.put("reg_domain_apv_flag", new WebFieldCheckbox(
				"reg_domain_apv_flag", addMode ? "" : db
						.getText("reg_domain_apv_flag"), ""));

		/*
		 * Blobs
		 */

		ht.put("notes_blob", new WebFieldText("notes_blob", db
				.getText("notes_blob"), 5, 100));

		ht.put("priority_comment_blob", new WebFieldText(
				"priority_comment_blob", db.getText("priority_comment_blob"),
				5, 100));

		ht.put("configuration_blob", new WebFieldText("configuration_blob", db
				.getText("configuration_blob"), 5, 100));

		ht.put("epic_code_blob", new WebFieldText("epic_code_blob", db
				.getText("epic_code_blob"), 5, 100));

		ht.put("priority_rationale_blob", new WebFieldText(
				"priority_rationale_blob", db
						.getText("priority_rationale_blob"), 5, 100));

		/*
		 * Return
		 */

		return ht;

	}

}
