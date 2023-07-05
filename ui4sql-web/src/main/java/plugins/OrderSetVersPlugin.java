/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.util.Hashtable;

import services.ExcelWriter;

import db.DbFieldInteger;
import db.DbFieldString;
import forms.*;

/**
 * 
 * 2/15 added mySql 3/10 use tcodes 3/13 as 'target' to list query 4/10 set
 * projectid on add, rearrange
 * 
 * 5/28/06 make 'Testcase' a child form
 * 
 * 6/13/10 - use standard view names
 */

public class OrderSetVersPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/
	public OrderSetVersPlugin() throws services.ServicesException {

		super();
		this.setTableName("korder_set_vers");
		this.setKeyName("order_set_vers_id");
		this.setTargetTitle("Order&nbsp;Set&nbsp;Paper&nbsp;Versions");

		this.setShowAuditSubmitApprove(false);
		this.setIsDetailForm(true);
		this.setParentTarget("OrderSet");
		this.setSubmitOk(false);
		this.setCopyOk(false);
		this.setExcelOk(true);
		this.setListViewName("vordersetversion_list");
		this.setSelectViewName("vordersetversion");

		this.setListHeaders(new String[] { "Version", "Order Set",
				"Change Title", "Posted", "Owner", "Domain" });

	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("order_set_id", new DbFieldInteger("order_set_id", sm
				.getParentId()));

		return true;

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
		 */

		StringBuffer sb = new StringBuffer();

		sb.append(" and order_set_id = " + sm.getParentId());

		return sb.toString();
	}

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));

		/*
		 * codes
		 */

		String[][] rx = { { "Nguyen", "Chun", "Foo", "Lee" },
				{ "Minh Nguyen", "Thomas Chun", "Emily Foo", "Jung Lee" } };

		ht.put("rx_reviewer_cd", new WebFieldSelect("rx_reviewer_cd",
				addMode ? "" : db.getText("rx_reviewer_cd"), rx));

		String[][] priorities = { { "S", "1", "2", "3", "B" },
				{ "Stage", "1", "2", "3", "Batch" } };

		ht.put("paper_priority_cd", new WebFieldSelect("paper_priority_cd",
				addMode ? "" : db.getText("paper_priority_cd"), priorities));

		String[][] screens = {
				{ "Stage", "Prod", "Train", "WIT2", "WIT3", "REGN" },
				{ "Stage", "Prod", "Train", "WIT2", "WIT3", "Regnncm" } };

		ht.put("screen_shot_loc_cd", new WebFieldSelect("screen_shot_loc_cd",
				addMode ? "" : db.getText("screen_shot_loc_cd"), screens));

		ht.put("screen_shot_cd", new WebFieldSelect("screen_shot_cd",
				addMode ? "" : db.getText("screen_shot_cd"), sm
						.getCodes("YESNO"), true));

		ht.put("cl_domain_cd", new WebFieldSelect("cl_domain_cd", addMode ? ""
				: db.getText("cl_domain_cd"), sm.getCodes("OSDOMAIN")));

		String[][] domains = new String[][] {
				{ "AS", "ANS", "ED", "MAT", "PED", "PERI", "INT" },
				{ "Adult Services", "Anesthesia", "ED", "Pediatrics",
						"Perioperative", "Interventional" } };

		ht.put("cl_sub_domain_cd", new WebFieldSelect("cl_sub_domain_cd",
				addMode ? "" : db.getText("cl_sub_domain_cd"), sm
						.getCodes("OSSUITES")));

		/*
		 * Numbers (put as strings)
		 */
		ht.put("benefit_flt", new WebFieldString("benefit_flt", (addMode ? ""
				: db.getText("benefit_flt")), 6, 6));

		/*
		 * strings
		 */

		ht.put("os_title_nm", new WebFieldDisplay("os_title_nm", addMode ? sm.getParentName()
				: db.getText("os_title_nm")));

		ht.put("os_reference_nm", new WebFieldDisplay("os_reference_nm",
				addMode ? "" : db.getText("os_reference_nm")));

		ht.put("tracker_screen_shot_loc", new WebFieldDisplay(
				"tracker_screen_shot_loc", addMode ? "" : db
						.getText("tracker_screen_shot_loc")));

		ht.put("midyear_rv_pharm_tx", new WebFieldString("midyear_rv_pharm_tx",
				(addMode ? "" : db.getText("midyear_rv_pharm_tx")), 12, 12));

		ht.put("midyear_rv_clinic_tx", new WebFieldString(
				"midyear_rv_clinic_tx", (addMode ? "" : db
						.getText("midyear_rv_clinic_tx")), 12, 12));

		ht.put("created_by_tx", new WebFieldString("created_by_tx",
				(addMode ? "NCAL KPHealthConnect Domain Group" : db
						.getText("created_by_tx")), 64, 64));

		ht.put("owner_tx", new WebFieldString("owner_tx", (addMode ? "Tina HH"
				: db.getText("owner_tx")), 12, 12));

		ht.put("paper_version_no", new WebFieldString("paper_version_no",
				(addMode ? "" : db.getText("paper_version_no")), 4, 4));

		ht
				.put("clinic_con_rv_tx", new WebFieldString("clinic_con_rv_tx",
						(addMode ? "Tina HH" : db.getText("clinic_con_rv_tx")),
						32, 32));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("online_vers_tx", new WebFieldString("online_vers_tx",
				(addMode ? "" : db.getText("online_vers_tx")), 8, 8));

		ht.put("paper_vers_tx", new WebFieldString("paper_vers_tx",
				(addMode ? "" : db.getText("paper_vers_tx")), 8, 8));

		/*
		 * Dates
		 */

		ht.put("cl_post_date", new WebFieldDate("cl_post_date", (addMode ? ""
				: db.getText("cl_post_date"))));

		ht.put("rx_review_date", new WebFieldDate("rx_review_date",
				(addMode ? "" : db.getText("rx_review_date"))));

		ht.put("rx_done_date", new WebFieldDate("rx_done_date", (addMode ? ""
				: db.getText("rx_done_date"))));

		ht.put("clinic_con_rv_date", new WebFieldDate("clinic_con_rv_date",
				(addMode ? "" : db.getText("clinic_con_rv_date"))));

		ht.put("clinic_con_done_date", new WebFieldDate("clinic_con_done_date",
				(addMode ? "" : db.getText("clinic_con_done_date"))));

		ht.put("docushare_post_date", new WebFieldDate("docushare_post_date",
				(addMode ? "" : db.getText("docushare_post_date"))));

		/*
		 * blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 4, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 80));

		ht.put("problem_blob", new WebFieldText("problem_blob", addMode ? ""
				: db.getText("problem_blob"), 4, 80));

		ht.put("alternatives_blob", new WebFieldText("alternatives_blob",
				addMode ? "" : db.getText("alternatives_blob"), 4, 80));

		return ht;

	}

	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "OrderSetVers.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName().replace(" ", "")
				+ "_OrderSetVersions";
		int columns = 32;
		short startRow = 2;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);
	}

	public ResultSet getExcelResultSet() {

		String query = "SELECT * FROM vordersetversion_excel WHERE 1=1  ORDER BY order_set_no";

		ResultSet rs = null;

		try {
			rs = db.getRS(query);
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

}
