/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * epiccr Plugin
 * 
 */
/*******************************************************************************
 * Service Request Plugin
 * 
 * Change Log:
 * 
 * 12/14/05 New
 * 
 * Notes: This is specific to Epic
 * 
 * 
 ******************************************************************************/
public class EpiccrInitPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public EpiccrInitPlugin() throws services.ServicesException {
		super();
		this.setTableName("tepic_cr");
		this.setKeyName("cr_id");
		this.setTargetTitle("Vendor Change Request");
		this.setListOrder("tepic_cr.cr_id");

		String dateCast1 = "(cast(month(target_date) as char(2)) + '/' + cast(dayofmonth(target_date) as char(2)) + '/' + cast(year(target_date) as char(4)) )";

		this.setListHeaders(new String[] { "CR #", "Title", "Target", "Status",
				"Type", "Application" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes stat on tepic_cr.status_cd = stat.code_value and stat.code_type_id = 73 ",
						" left join tcodes region on tepic_cr.region_cd = region.code_value and region.code_type_id = 108 ",
						" left join tapplications appl on tepic_cr.application_id = appl.application_id " });

		this
				.setMoreSelectJoins(new String[] {
						" left join tcontact epic on tepic_cr.epic_uid = epic.contact_id ",
						" left join tcontact requestor on tepic_cr.requestor_uid = requestor.contact_id ",
						" left join tcontact leader on tepic_cr.leader_uid = leader.contact_id ",
						" left join tcontact implementor on tepic_cr.implementor_uid = implementor.contact_id " });

		this.setMoreSelectColumns(new String[] {
				"epic.email_address as epic_email_nm",
				"epic.phone_nm as epic_phone_nm",
				"requestor.email_address as requestor_email_nm",
				"requestor.phone_nm as requestor_phone_nm",
				"leader.email_address as leader_email_nm",
				"leader.phone_nm as leader_phone_nm",
				"implementor.email_address as implementor_email_nm",
				"implementor.phone_nm as implementor_phone_nm" });

		this.setMoreListColumns(new String[] { "tepic_cr.cr_no ",
				"title_nm", dateCast1, "stat.code_desc as StatDesc",
				"region.code_desc as RegionDesc", "application_name" });
		
		this.setGotoOk(true);
		this.setGotoDisplayName("CR #: ");
		this.setGotoKeyName("cr_no");
		this.setUpdatesLevel("executive");
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

	/*
	 * List Selector Controls
	 */

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 3 || columnNumber == 4 || columnNumber == 5)
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

		switch (columnNumber) {

		case 3: {

			return getListSelector("FilterStatus", "DR", "All Status",
					getStatHT());

		}
		case 4: {

			return getListSelector("FilterRegion", "NCAL", "All Regions", sm
					.getCodes("REGION"));

		}

		default: {
			// application filter

			return getListSelector("FilterApplication", new Integer("0"),
					"All Applications", sm.getApplicationFilter());

		}
		}

	}

	/*
	 * This is an abstraction of more detail codes.
	 */
	private Hashtable getStatHT() {
		Hashtable<Object, Object[]> stats = new Hashtable<Object, Object[]>();
		Object[] obj1 = new Object[2];
		obj1[0] = "OP";
		obj1[1] = "Open";
		stats.put("OP", obj1);

		Object[] obj2 = new Object[2];
		obj2[0] = "CL";
		obj2[1] = "Closed";
		stats.put("CL", obj2);

		Object[] obj3 = new Object[2];
		obj3[0] = "DE";
		obj3[1] = "Deferred";
		stats.put("DE", obj3);

		Object[] obj4 = new Object[2];
		obj4[0] = "DR";
		obj4[1] = "Draft";
		stats.put("DR", obj4);

		return stats;

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present

		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND tepic_cr.status_cd LIKE 'DR%'");
		}

		else {

			if (sm.Parm("FilterStatus").length() > 1) {
				sb.append(" AND tepic_cr.status_cd LIKE '"
						+ sm.Parm("FilterStatus") + "%'");
			}
		}

		// filter region

		if (sm.Parm("FilterRegion").length() == 0) {
			sb.append(" AND region.code_value = 'NCAL'");
		}

		else {

			if (sm.Parm("FilterRegion").length() > 1) {
				sb.append(" AND region.code_value = '"
						+ sm.Parm("FilterRegion") + "'");
			}
		}

		// filter on application
		if (sm.Parm("FilterApplication").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterApplication").equalsIgnoreCase("0")) {
				sb.append(" AND tepic_cr.application_id = "
						+ sm.Parm("FilterApplication"));
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

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Error Message
		 */

		ht.put("msg", new WebFieldDisplay("msg", " "));

		ht.put("cr_no", new WebFieldString("cr_no", (addMode ? "" : db
				.getText("cr_no")), 6, 6));

		/*
		 * Strings
		 */

		ht.put("chg_order_tx", new WebFieldString("chg_order_tx", (addMode ? ""
				: db.getText("chg_order_tx")), 16, 32));

		ht.put("rate_tx", new WebFieldString("rate_tx", (addMode ? "" : db
				.getText("rate_tx")), 16, 32));

		ht.put("hours_tx", new WebFieldString("hours_tx", (addMode ? "" : db
				.getText("hours_tx")), 16, 32));

		ht.put("estimate_tx", new WebFieldString("estimate_tx", (addMode ? ""
				: db.getText("estimate_tx")), 16, 32));

		// shorten a little because it's expanding the screen
		ht.put("dependency_tx", new WebFieldString("dependency_tx",
				(addMode ? "" : db.getText("dependency_tx")), 24, 64));

		ht
				.put(
						"swq_tx",
						new WebFieldString(
								"swq_tx",
								(addMode ? "Did you search for CR duplicity in the SWQ or AE Log File?  Yes/No"
										: db.getText("swq_tx")), 64, 64));

		ht.put("requestor_nm", new WebFieldString("requestor_nm", (addMode ? ""
				: db.getText("requestor_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 100));

		ht.put("region_nm", new WebFieldString("region_nm", (addMode ? "" : db
				.getText("region_nm")), 32, 32));

		ht.put("other_region_nm", new WebFieldString("other_region_nm",
				(addMode ? "" : db.getText("other_region_nm")), 32, 32));

		ht.put("team_nm", new WebFieldString("team_nm", (addMode ? "" : db
				.getText("team_nm")), 32, 32));

		ht.put("release_nm", new WebFieldString("release_nm", (addMode ? ""
				: db.getText("release_nm")), 16, 32));

		ht.put("implementor_team_tx", new WebFieldString("implementor_team_tx",
				(addMode ? "" : db.getText("implementor_team_tx")), 45, 45));

		ht.put("implementor_reg_tx", new WebFieldString("implementor_reg_tx",
				(addMode ? "" : db.getText("implementor_reg_tx")), 24, 45));

		// shorten a little too
		ht.put("sec_products_tx", new WebFieldString("sec_products_tx",
				(addMode ? "" : db.getText("sec_products_tx")), 30, 45));

		ht.put("clinical_team_tx", new WebFieldString("clinical_team_tx",
				(addMode ? "" : db.getText("clinical_team_tx")), 45, 45));

		ht.put("billing_team_tx", new WebFieldString("billing_team_tx",
				(addMode ? "" : db.getText("billing_team_tx")), 45, 45));

		ht.put("foundation_team_tx", new WebFieldString("foundation_team_tx",
				(addMode ? "" : db.getText("foundation_team_tx")), 45, 45));

		/*
		 * Dates
		 */

		ht.put("target_date", new WebFieldDate("target_date", addMode ? "" : db
				.getText("target_date")));

		ht.put("test1_date", new WebFieldDate("test1_date", addMode ? "" : db
				.getText("test1_date")));

		ht.put("expected_date", new WebFieldDate("expected_date", addMode ? ""
				: db.getText("expected_date")));

		ht.put("vendor_accept_date", new WebFieldDate("vendor_accept_date",
				addMode ? "" : db.getText("vendor_accept_date")));

		ht.put("estimated_date", new WebFieldDate("estimated_date",
				addMode ? "" : db.getText("estimated_date")));

		/*
		 * Ids
		 */

		String query_rlse = "select title_nm as odor, release_id, title_nm from trelease where division_id = "
				+ sm.getDivisionId().toString();

		Hashtable releases = sm.getTable("trelease", query_rlse);

		String query_products = "select title_nm as odor, product_id, title_nm , group_cd from tproduct where division_id = "
				+ sm.getDivisionId().toString()
				+ " ORDER BY group_cd, title_nm";

		Hashtable htProducts = sm.getTable("tproduct", query_products);

		ht.put("product_id", new WebFieldSelect("product_id",
				addMode ? new Integer("0") : db.getInteger("product_id"),
				htProducts, true));

		ht.put("release_id", new WebFieldSelect("release_id",
				addMode ? new Integer("0") : db.getInteger("release_id"),
				releases, true));

		ht.put("vendor_accept_uid",
				new WebFieldSelect("vendor_accept_uid", addMode ? new Integer(
						"0") : db.getInteger("vendor_accept_uid"), sm
						.getVendorHT(), true));

		ht.put("requestor_uid", new WebFieldSelect("requestor_uid",
				addMode ? new Integer("0") : db.getInteger("requestor_uid"), sm
						.getContactHT(), true));

		ht.put("leader_uid", new WebFieldSelect("leader_uid",
				addMode ? new Integer("0") : db.getInteger("leader_uid"), sm
						.getContactHT(), true));

		ht.put("implementor_uid", new WebFieldSelect("implementor_uid",
				addMode ? new Integer("0") : db.getInteger("implementor_uid"),
				sm.getContactHT(), true));

		ht.put("epic_uid", new WebFieldSelect("epic_uid",
				addMode ? new Integer("0") : db.getInteger("epic_uid"), sm
						.getVendorHT(), true));

		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? new Integer("0") : db.getInteger("application_id"),
				sm.getApplicationFilter()));

		/*
		 * Codes
		 */

		String[][] estTypes = { { "STD", "CUST", "RETR" },
				{ "Standard", "Custom", "Retrofit" } };

		String[][] permanentType = { { "P", "I" }, { "Permanent", "Interim" } };

		String[][] eeg_developType = {
				{ "EEG", "KP" },
				{ "Within scope for EEG development.", "Epic development only" } };

		String[][] developmentType = {
				{ "STD", "FUTR", "KP", "NONE" },
				{ "Standard/Released as MU", "Future Standard Release",
						"Custom for KP", "No Development" } };

		String[][] approvalType = {
				{ "ASIS", "OKAY", "DATE", "CLNT", "PROG" },
				{ "As Requested", "No Contingencies",
						"Accepted with negotiaged delivery date",
						"Accpetance on new client funding",
						"Acceptance on new program funding" } };

		ht.put("vendor_approval_cd", new WebFieldSelect("vendor_approval_cd",
				addMode ? "" : db.getText("vendor_approval_cd"), approvalType));

		ht.put("clinical_dev_cd", new WebFieldSelect("clinical_dev_cd",
				addMode ? "" : db.getText("clinical_dev_cd"), developmentType));

		ht.put("rev_dev_cd", new WebFieldSelect("rev_dev_cd", addMode ? "" : db
				.getText("rev_dev_cd"), developmentType));

		ht.put("member_dev_cd", new WebFieldSelect("member_dev_cd",
				addMode ? "" : db.getText("member_dev_cd"), developmentType));

		ht.put("permanent_cd", new WebFieldSelect("permanent_cd", addMode ? ""
				: db.getText("permanent_cd"), permanentType));

		ht.put("est_type_cd", new WebFieldSelect("est_type_cd", addMode ? ""
				: db.getText("est_type_cd"), estTypes));

		ht.put("vendor_cd", new WebFieldSelect("vendor_cd", addMode ? "" : db
				.getText("vendor_cd"), sm.getCodes("VENDORS")));

		ht.put("funding_region_cd", new WebFieldSelect("funding_region_cd",
				addMode ? "NCAL" : db.getText("funding_region_cd"), sm
						.getCodes("REGION")));

		ht.put("region_cd", new WebFieldSelect("region_cd", addMode ? "NCAL"
				: db.getText("region_cd"), sm.getCodes("REGION")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "DRAFT"
				: db.getText("status_cd"), sm.getCodes("CRSTATUS")));

		ht.put("emergency_cd", new WebFieldSelect("emergency_cd", addMode ? ""
				: db.getText("emergency_cd"), sm.getCodes("YESNO")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("CRTYPES"), true));

		ht.put("severity_cd", new WebFieldSelect("severity_cd", addMode ? ""
				: db.getText("severity_cd"), sm.getCodesAlt("SEVERITY4")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		ht.put("eeg_scope_cd", new WebFieldSelect("eeg_scope_cd", addMode ? ""
				: db.getText("eeg_scope_cd"), eeg_developType));

		ht.put("eeg_decision_cd", new WebFieldSelect("eeg_decision_cd",
				addMode ? "" : db.getText("eeg_decision_cd"), eeg_developType));

		/*
		 * Blobs
		 */

		ht.put("release_tx", new WebFieldText("release_tx", addMode ? "" : db
				.getText("release_tx"), 2, 32));

		ht.put("std_custom_tx", new WebFieldText("std_custom_tx", addMode ? ""
				: db.getText("std_custom_tx"), 2, 32));

		ht.put("progress_blob", new WebFieldText("progress_blob", addMode ? ""
				: db.getText("progress_blob"), 5, 100));

		ht.put("co_tx", new WebFieldText("co_tx", addMode ? "" : db
				.getText("co_tx"), 5, 100));

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("benefits_blob", new WebFieldText("benefits_blob", addMode ? ""
				: db.getText("benefits_blob"), 5, 100));

		ht.put("alternatives_blob", new WebFieldText("alternatives_blob",
				addMode ? "" : db.getText("alternatives_blob"), 5, 100));

		ht.put("risks_blob", new WebFieldText("risks_blob", addMode ? "" : db
				.getText("risks_blob"), 5, 100));

		ht.put("regn_analysis_blob", new WebFieldText("regn_analysis_blob",
				addMode ? "" : db.getText("regn_analysis_blob"), 5, 100));

		ht.put("regn_impact_blob", new WebFieldText("regn_impact_blob",
				addMode ? "" : db.getText("regn_impact_blob"), 5, 100));

		ht
				.put(
						"epic_use_blob",
						new WebFieldText(
								"epic_use_blob",
								addMode ? "If this is a change that Epic customers might need, describes the analysis that confirms this."
										: db.getText("epic_use_blob"), 5, 100));

		ht.put("comments_blob", new WebFieldText("comments_blob", addMode ? ""
				: db.getText("comments_blob"), 5, 100));

		ht.put("epic_comment_blob", new WebFieldText("epic_comment_blob",
				addMode ? "" : db.getText("epic_comment_blob"), 5, 100));

		ht.put("vendor_comment_blob", new WebFieldText("vendor_comment_blob",
				addMode ? "" : db.getText("vendor_comment_blob"), 5, 100));

		ht.put("eeg_comments_blob", new WebFieldText("eeg_comments_blob",
				addMode ? "" : db.getText("eeg_comments_blob"), 5, 100));

		ht.put("epic_recommendation_blob", new WebFieldText(
				"epic_recommendation_blob", addMode ? "" : db
						.getText("epic_recommendation_blob"), 5, 100));

		/*
		 * Ids
		 */

		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("application_id"),
				sm.getApplicationFilter(), true));

		if (!addMode) {
			putSelectFields(ht);
		}

		return ht;
	}

	/*
	 * these fields are taken from joins....off the tuser table
	 */

	private void putSelectFields(Hashtable<String, WebField> ht) {

		ht.put("epic_email_nm", new WebFieldDisplay("epic_email_nm", (db
				.getText("epic_email_nm"))));

		ht.put("epic_phone_nm", new WebFieldDisplay("epic_phone_nm", (db
				.getText("epic_phone_nm"))));

		ht.put("requestor_email_nm", new WebFieldDisplay("requestor_email_nm",
				(db.getText("requestor_email_nm"))));

		ht.put("requestor_phone_nm", new WebFieldDisplay("requestor_phone_nm",
				(db.getText("requestor_phone_nm"))));

		ht.put("leader_email_nm", new WebFieldDisplay("leader_email_nm", (db
				.getText("leader_email_nm"))));

		ht.put("leader_phone_nm", new WebFieldDisplay("leader_phone_nm", (db
				.getText("leader_phone_nm"))));

		ht.put("implementor_email_nm", new WebFieldDisplay(
				"implementor_email_nm", (db.getText("implementor_email_nm"))));

		ht.put("implementor_phone_nm", new WebFieldDisplay(
				"implementor_phone_nm", (db.getText("implementor_phone_nm"))));

	}

}
