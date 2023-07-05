/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import services.ServicesException;
import forms.*;

/**
 * 
 * 2/15 added mySql 3/10 use tcodes
 * 
 * 6/13/10 - change to standard view names
 */

public class BsnsQualFormPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public BsnsQualFormPlugin() throws services.ServicesException {

		super();
		this.setTableName("tbqf");
		this.setKeyName("bqf_id");
		this.setTargetTitle("Business Qualifying Form");

		this.setDeleteOk(false);

		this.setListHeaders(new String[] { "Reference", "Title", "Status",
				"Reason", "Requestor", "National AC" });

		// select 2-Status, 3-Reason, 4-Requestor
		
		this.setListSelectorColumnFlags(new boolean[] { false, false, true,
				true, true, false });

		this.setListViewName("vbsnsqualform_list");

	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {

		case 2: {
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", sm
					.Parm("FilterStatus"), sm.getCodes("STATUS"), "All Status");
			wf.setDisplayClass("listform");
			return wf;
		}
		case 3: {
			WebFieldSelect wf = new WebFieldSelect("FilterProduct", sm
					.Parm("FilterProduct"), sm.getCodes("EPICPRODS"),
					"Product?");
			wf.setDisplayClass("listform");
			return wf;
		}

		default: {
			// TODO.. Resource HOG!!! It queries every time. Let the db manager
			// cache the data.
			String qry = new String(
					"select distinct concat(c.last_name, ', ', c.first_name) as a , "
							+ "c.contact_id as b, concat(c.last_name, ', ',	c.first_name) as c "
							+ " from tbqf left join tcontact c on tbqf.requestor_uid = c.contact_id "
							+ " where isnull(requestor_uid, 0) > 0");

			Hashtable contacts = new Hashtable();

			try {
				contacts = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}
			return getListSelector("FilterRequestor", new Integer("0"),
					"Requestor? ", contacts);

		}

		}

	}

	public String getListAnd() {
	
		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present

		if ((!sm.Parm("FilterStatus").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterStatus").length() > 0)) {
			sb.append(" AND status_cd = '" + sm.Parm("FilterStatus") + "'");
		}

		if ((!sm.Parm("FilterProduct").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterProduct").length() > 0)) {
			sb.append(" AND product_cd = '" + sm.Parm("FilterProduct") + "'");
		}

		if ((!sm.Parm("FilterRequestor").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterRequestor").length() > 0)) {
			sb.append(" AND requestor_uid = " + sm.Parm("FilterRequestor"));
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
		 * link to the twin (same record) Impact Analysis
		 */
		String href = new String("");

		if (parmMode.equalsIgnoreCase("show")) {

			href = "<A href=Router?Target=BsnsQualFormImpact&Action=Show&Relation=this&RowKey="
					+ db.getText("bqf_id") + ">Impact Analysis</A>";

		}

		ht.put("impactlink", new WebFieldDisplay("impactlink", href));

		/*
		 * Ids
		 */

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("owner_uid"), sm.getUserHT(), true));

		ht.put("requestor_uid", new WebFieldSelect("requestor_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("requestor_uid"), sm.getContactHT(), true));

		ht.put("product_cd", new WebFieldSelect("product_cd", addMode ? "" : db
				.getText("product_cd"), sm.getCodes("EPICPRODS"), true));

		/*
		 * Codes
		 */

		ht.put("cmt_only_cd", new WebFieldSelect("cmt_only_cd", addMode ? ""
				: db.getText("cmt_only_cd"), sm.getCodes("YESNO")));

		ht.put("reason_cd", new WebFieldSelect("reason_cd", addMode ? "" : db
				.getText("reason_cd"), sm.getCodes("OBJECTIVE")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("STATUS")));

		ht.put("size_cd", new WebFieldSelect("size_cd", addMode ? "S" : db
				.getText("size_cd"), sm.getCodes("PROCESSTYPE")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? "S"
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		ht.put("budgeted_cd", new WebFieldSelect("budgeted_cd", addMode ? "Y"
				: db.getText("budgeted_cd"), sm.getCodes("YESNO")));

		/*
		 * Strings
		 */

		ht.put("rfc_no", new WebFieldString("rfc_no", (addMode ? "" : db
				.getText("rfc_no")), 8, 8));

		ht.put("other_teams_nm", new WebFieldString("other_teams_nm",
				(addMode ? "" : db.getText("other_teams_nm")), 64, 64));

		ht.put("epic_ini_tx", new WebFieldString("epic_ini_tx", (addMode ? ""
				: db.getText("epic_ini_tx")), 32, 32));

		ht.put("regions_tx", new WebFieldString("regions_tx", (addMode ? ""
				: db.getText("regions_tx")), 32, 32));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 100));

		ht.put("version_nm", new WebFieldString("version_nm", (addMode ? ""
				: db.getText("version_nm")), 8, 8));

		ht.put("sponsor_nm", new WebFieldString("sponsor_nm", (addMode ? ""
				: db.getText("sponsor_nm")), 32, 32));

		ht.put("natl_ac_nm", new WebFieldString("natl_ac_nm", (addMode ? ""
				: db.getText("natl_ac_nm")), 32, 32));

		/*
		 * Flags
		 */
		ht.put("accepted_flag", new WebFieldCheckbox("accepted_flag",
				addMode ? "N" : db.getText("accepted_flag"), ""));

		/*
		 * Dates
		 */
		ht.put("proposal_dt", new WebFieldDate("proposal_dt", addMode ? "" : db
				.getText("proposal_dt")));

		ht.put("request_dt", new WebFieldDate("request_dt", addMode ? "" : db
				.getText("request_dt")));

		ht.put("required_dt", new WebFieldDate("required_dt", addMode ? "" : db
				.getText("required_dt")));

		ht.put("accept_dt", new WebFieldDate("accept_dt", addMode ? "" : db
				.getText("accept_dt")));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("impact_not_do_blob", new WebFieldText("impact_not_do_blob",
				addMode ? "" : db.getText("impact_not_do_blob"), 5, 100));

		ht.put("team_impacts_blob", new WebFieldText("team_impacts_blob",
				addMode ? "" : db.getText("team_impacts_blob"), 5, 100));

		ht.put("areas_blob", new WebFieldText("areas_blob", addMode ? "" : db
				.getText("areas_blob"), 5, 100));

		ht.put("data_blob", new WebFieldText("data_blob", addMode ? "" : db
				.getText("data_blob"), 5, 100));

		ht.put("goals_blob", new WebFieldText("goals_blob", addMode ? "" : db
				.getText("goals_blob"), 5, 100));

		ht.put("process_blob", new WebFieldText("process_blob", addMode ? ""
				: db.getText("process_blob"), 5, 100));

		ht.put("current_blob", new WebFieldText("current_blob", addMode ? ""
				: db.getText("current_blob"), 5, 100));

		ht.put("costs_blob", new WebFieldText("costs_blob", addMode ? "" : db
				.getText("costs_blob"), 5, 100));

		ht.put("impacts_blob", new WebFieldText("impacts_blob", addMode ? ""
				: db.getText("impacts_blob"), 5, 100));

		ht.put("solution_blob", new WebFieldText("solution_blob", addMode ? ""
				: db.getText("solution_blob"), 5, 100));

		ht.put("reason_blob", new WebFieldText("reason_blob", addMode ? "" : db
				.getText("reason_blob"), 5, 100));

		ht.put("alternative_blob", new WebFieldText("alternative_blob",
				addMode ? "" : db.getText("alternative_blob"), 5, 100));

		ht.put("qualify_blob", new WebFieldText("qualify_blob", addMode ? ""
				: db.getText("qualify_blob"), 5, 100));

		ht.put("rejection_blob", new WebFieldText("rejection_blob",
				addMode ? "" : db.getText("rejection_blob"), 5, 100));

		/*
		 * Return
		 */
		return ht;

	}
}
