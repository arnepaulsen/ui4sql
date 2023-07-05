/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.util.Hashtable;
import forms.*;

import org.apache.poi.hssf.util.HSSFColor;

import db.DbField;

import services.ExcelWriter;
import services.ServicesException;

/*******************************************************************************
 * Change Request Plugin
 * 
 * 3/22 New Page
 * 
 * 9/15 - Change List headers, default 'A' for status, and
 * 
 * 10/15/08 - Use a WebFieldString in the list Filter for partial file names
 * 
 * caution... if change view.. be sure to update the offset in the col return
 * "bgcolor=" + fields[18].getText();
 * 
 * 9/21/09 - set update access level to 'chg_approver', like RIP-CAB approvers
 * 
 * 11/17/09 - add "status_tx"
 * 
 * 6/13/09 - change to standard view names
 */

public class AdhocPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private static String[] sListHeaders = {
			"SEQ",
			"Auth<br>Code",
			"Product/Suite",
			"Title&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			"File",
			"Status",
			"Status Info&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			"Owner", "Conf.", "Outage", "PSUP", "WITS", "WIT3", "STGN",
			"RIP&nbsp;CAB", "NCF&nbsp;CAB", "&nbsp;Install" };

	public AdhocPlugin() throws services.ServicesException {
		super();
		this.setTableName("tadhoc");
		this.setTargetTitle("Ad-Hoc Request");
		this.setKeyName("adhoc_id");

		this.setListOrder("ra_no");

		this.setListViewName("vadhoc_list");
		

		this.setUpdatesLevel("chg_aprv"); // user must be "chg_aprv_flag=Y to
											// approve;

		// this.setListColumnWidths(new String[] { "", "", "", "400", "", "",
		// "",
		// "", "", "", "", "", "", "", "", "", "", "", "", "" });

		this.setListHeaders(sListHeaders);

		this.setShowAuditSubmitApprove(false);

		// selectors for columns: 2-Product, 4-File, 5-Status, 6-Owner,
		// 9-Received

		this
				.setListSelectorColumnFlags(new boolean[] { false, false,
						false, false, true, true, false, true, false, false,
						false, false, false, false, false, false, false, false,
						false, false, false });

		this.setExcelOk(true);
		this.setGotoOk(true);
		this.setGotoDisplayName("Auth Code: ");
		this.setGotoKeyName("ra_no");

	}

	/***************************************************************************
	 * 
	 * List Filters
	 * 
	 **************************************************************************/

	// return bgcolor=yellow if install date < today
	public String listBgColor(int columnNumber, String value, DbField[] fields) {

		if (columnNumber < 900) {
			if (fields[19].getText() != null)
				if (fields[19].getText().length() > 2) {
					return "bgcolor=" + fields[19].getText();
				} else
					return "";
			else
				return "";
		} else
			return "";
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

	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 200) {
			return getListSelector("FilterProduct", "", "All Products", sm
					.getCodes("PRODUCTSADHOC"));
		}

		if (columnNumber == 300) {
			return new WebFieldString("FilterTitle", "Title:"
					+ sm.Parm("FilterTitle").replaceAll("Title:", ""), 60, 60);
		}

		if (columnNumber == 4) {
			return new WebFieldString("FilterFile", "File:"
					+ sm.Parm("FilterFile").replaceAll("File:", ""), 30, 40);
		}

		if (columnNumber == 5)
			return getListSelector("FilterStatus", "A", "All Status", sm
					.getCodes("ADHOCSTAT"));

		// Just get contracts that actually have an adHoc assigned
		if (columnNumber == 7) {
			// TODO.. Resource HOG!!! It queries every time. Let the db manager
			// cache the data.
			String qry = new String(
					"select distinct "
							+ "concat(c.last_name, ', ', c.first_name) as a , "
							+ "c.contact_id as b, "
							+ "concat(c.last_name, ', ',	c.first_name) as c "
							+ " from tadhoc join tcontact c on tadhoc.assigned_uid = c.contact_id ");

			Hashtable contacts = new Hashtable();

			try {
				contacts = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}
			return getListSelector("FilterOwner", Integer.valueOf(0), "Owner ? ",
					contacts);
		}

		/*
		 * Filter unique Install Dates!
		 * 
		 * removed.. never called
		 */
		if (columnNumber == 9) {

			String dateCast = null;
			String dateCast2 = null;
			String qry = null;

			dateCast = " ( " + dbprefix
					+ "FormatDateTime(received_date, 'mm/yy')) ";
			dateCast2 = " ( " + dbprefix
					+ "FormatDateTime(received_date, 'yyyy/mm')) ";

			qry = " select distinct " + dateCast2 + "," + dateCast + ","
					+ dateCast
					+ " from tadhoc where received_date != '' order by 1";

			Hashtable dates = new Hashtable();

			try {
				dates = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterReceived", "O", "Received In?", dates);

		}

		// will never get here
		Hashtable ht = new Hashtable();
		return getListSelector("dummy", Integer.valueOf(0), "badd..", ht);

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// COLUMN 4 = Patient Safety

		if (sm.Parm("FilterSafety").length() == 0) {

		} else {
			if (!sm.Parm("FilterSafety").equalsIgnoreCase("0")) {
				sb.append(" AND safety_cd = '" + sm.Parm("FilterSafety") + "'");
			}
		}

		// COLUMN 5 = Region = NCAL

		// sb.append(" AND tadhoc.region_cd = 'NCAL'");

		if (sm.Parm("FilterRegion").length() == 0) {
			// sb.append(" AND tadhoc.region_cd = 'NCAL'");
		}

		else {
			if (!sm.Parm("FilterRegion").equalsIgnoreCase("0")) {
				sb.append(" AND reg.code_value = '" + sm.Parm("FilterRegion")
						+ "'");
			}
		}

		String fileName = sm.Parm("FilterFile").replaceAll("File:", "");
		fileName.replaceFirst(" ", "");

		if (sm.Parm("FilterFile").length() > 0) {
			sb.append(" AND file_nm like('%" + fileName + "%')");
		}

		// Filter on Date Range for "received_date"... tricky one!!

		// removed... 8/23

		if (sm.Parm("FilterReceived").length() == 0) {

		} else {
			if (!sm.Parm("FilterReceived").equalsIgnoreCase("0")) {
				String filterMonth = sm.Parm("FilterReceived").substring(0, 2);
				String filterYear = "20"
						+ sm.Parm("FilterReceived").substring(3, 5);

				sb.append(" AND datepart(month, received_date) = "
						+ filterMonth + " AND datepart(yyyy, received_date) = "
						+ filterYear);
			}

		}

		// COLUMN 6 = Status

		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND status_cd = 'A'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND status_cd = '" + sm.Parm("FilterStatus") + "'");
			}
		}

		// filter on owner
		if (sm.Parm("FilterOwner").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterOwner").equalsIgnoreCase("0")) {
				sb.append(" AND assigned_uid = " + sm.Parm("FilterOwner"));
			}
		}

		// filter on application
		if (sm.Parm("FilterProduct").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterProduct").equalsIgnoreCase("0")) {
				sb.append(" AND product_cd = '" + sm.Parm("FilterProduct")
						+ "'");
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
		 * Codes using internal arrays
		 */

		String[][] aClient = { { "C", "S" }, { "Client", "Server" } };

		String[][] aEpicVersion = { { "S06", "SP07" },
				{ "Spring 06", "Spring 07" } };

		String[][] aTestMig = {
				{ "D", "R", "A" },
				{
						"Install in testing environment as soon as the code is delivered.",
						"The problem need to be replicated before the code installation.",
						"Ad Hoc owner will inform testing team when to install the code" } };

		ht.put("configurable_cd", new WebFieldSelect("configurable_cd",
				addMode ? "t" : db.getText("configurable_cd"), sm
						.getCodes("YESNOTBD")));

		ht.put("color_cd", new WebFieldSelect("color_cd", addMode ? "" : db
				.getText("color_cd"), sm.getCodesAlt("RYG"), "No Color"));

		ht.put("product_cd", new WebFieldSelect("product_cd", addMode ? "" : db
				.getText("product_cd"), sm.getCodes("PRODUCTSADHOC"), true));

		ht.put("cost_cd", new WebFieldSelect("cost_cd", addMode ? "" : db
				.getText("cost_cd"), sm.getCodes("YESNO")));

		ht.put("client_cd", new WebFieldSelect("client_cd", addMode ? "C" : db
				.getText("client_cd"), aClient));

		ht.put("test_mig_cd", new WebFieldSelect("test_mig_cd", addMode ? ""
				: db.getText("test_mig_cd"), aTestMig));

		ht.put("region_cd", new WebFieldSelect("region_cd", addMode ? "C" : db
				.getText("region_cd"), sm.getCodes("REGION")));

		ht.put("epic_vers_cd", new WebFieldSelect("epic_vers_cd", addMode ? ""
				: db.getText("epic_vers_cd"), aEpicVersion));

		/*
		 * Codes using look-up tables
		 */

		ht.put("test_env_cd", new WebFieldSelect("test_env_cd", addMode ? "New"
				: db.getText("test_env_cd"), sm.getCodes("TESTREG")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "New"
				: db.getText("status_cd"), sm.getCodes("ADHOCSTAT")));

		ht.put("test_stat_cd", new WebFieldSelect("test_stat_cd",
				addMode ? "New" : db.getText("test_stat_cd"), sm
						.getCodes("ADHOCSTAT")));

		ht.put("work_around_cd", new WebFieldSelect("work_around_cd",
				addMode ? "" : db.getText("work_around_cd"), sm
						.getCodes("YESNO")));

		ht.put("authorize_cd", new WebFieldSelect("authorize_cd",
				addMode ? "New" : db.getText("authorize_cd"), sm
						.getCodes("KAUTHCAT")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		ht.put("outage_cd", new WebFieldSelect("outage_cd", addMode ? "T" : db
				.getText("outage_cd"), sm.getCodes("YESNOTBD")));

		ht.put("version_cd", new WebFieldSelect("version_cd", addMode ? "" : db
				.getText("version_cd"), sm.getCodes("KRLSE")));

		ht.put("safety_cd", new WebFieldSelect("safety_cd", addMode ? "" : db
				.getText("safety_cd"), sm.getCodes("YESNO")));

		ht.put("track_cd", new WebFieldSelect("track_cd", addMode ? "" : db
				.getText("track_cd"), sm.getCodes("KTRACK")));

		/*
		 * Index / Referenece
		 */

		ht.put("ra_no", new WebFieldString("ra_no", addMode ? "" : db
				.getText("ra_no"), 6, 6));

		/*
		 * Id's
		 */

		Hashtable package_list = sm
				.getTable("trelease_5",
						"select title_nm, release_id, title_nm from trelease where type_cd = 'K'");

		ht.put("release_id", new WebFieldSelect("release_id",
				addMode ? new Integer("0") : db.getInteger("release_id"),
				package_list, true));

		ht.put("requestor_uid", new WebFieldSelect("requestor_uid",
				addMode ? new Integer("0") : db.getInteger("requestor_uid"), sm
						.getUserHT(), true));

		//debug("assigned uid");
		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db.getObject("assigned_uid"), sm
						.getContactHT(), true));

		//debug("applciaton id");
		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getApplicationId() : (Integer) db
						.getObject("application_id"),
				sm.getApplicationFilter(), true));

		/*
		 * Strings
		 */

		//debug("strings");

		ht.put("notify_person_nm", new WebFieldString("notify_person_nm",
				(addMode ? "" : db.getText("notify_person_nm")), 32, 32));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 32, 32));

		ht.put("test_est_tx", new WebFieldString("test_est_tx", (addMode ? ""
				: db.getText("test_est_tx")), 32, 32));

		ht.put("applications_tx", new WebFieldString("applications_tx",
				(addMode ? "" : db.getText("applications_tx")), 64, 64));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("psup_rfc_nm", new WebFieldString("psup_rfc_nm", (addMode ? ""
				: db.getText("psup_rfc_nm")), 8, 8));

		ht.put("test_rfc_nm", new WebFieldString("test_rfc_nm", (addMode ? ""
				: db.getText("test_rfc_nm")), 8, 8));

		ht.put("tested_nm", new WebFieldString("tested_nm", (addMode ? "" : db
				.getText("tested_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 90, 127));

		ht.put("package_nm", new WebFieldString("package_nm", (addMode ? ""
				: db.getText("package_nm")), 32, 32));

		ht.put("prod_rfc_nm", new WebFieldString("prod_rfc_nm", (addMode ? ""
				: db.getText("prod_rfc_nm")), 8, 8));

		ht.put("rfc_manifest_nm", new WebFieldString("rfc_manifest_nm",
				(addMode ? "" : db.getText("rfc_manifest_nm")), 8, 8));

		ht.put("dlg_nm", new WebFieldString("dlg_nm", (addMode ? "" : db
				.getText("dlg_nm")), 32, 32));

		ht.put("bsns_driver_tx", new WebFieldString("bsns_driver_tx",
				(addMode ? "" : db.getText("bsns_driver_tx")), 64, 100));

		ht.put("epic_contact_tx", new WebFieldString("epic_contact_tx",
				(addMode ? "" : db.getText("epic_contact_tx")), 64, 100));

		/*
		 * Dates
		 */

		//debug("dates ..");
		ht.put("wits_date", new WebFieldDate("wits_date", addMode ? "" : db
				.getText("wits_date")));

		ht.put("wit3_date", new WebFieldDate("wit3_date", addMode ? "" : db
				.getText("wit3_date")));

		ht.put("stgn_date", new WebFieldDate("stgn_date", addMode ? "" : db
				.getText("stgn_date")));

		ht.put("psup_date", new WebFieldDate("psup_date", addMode ? "" : db
				.getText("psup_date")));

		ht.put("rip_cab_date", new WebFieldDate("rip_cab_date", addMode ? ""
				: db.getText("rip_cab_date")));

		ht.put("ncf_cab_date", new WebFieldDate("ncf_cab_date", addMode ? ""
				: db.getText("ncf_cab_date")));

		ht.put("expire_date", new WebFieldDate("expire_date", addMode ? "" : db
				.getText("expire_date")));

		ht.put("ra_approve_date", new WebFieldDate("ra_approve_date",
				addMode ? "" : db.getText("ra_approve_date")));

		ht.put("cab_approve_date", new WebFieldDate("cab_approve_date",
				addMode ? "" : db.getText("cab_approve_date")));

		ht.put("required_date", new WebFieldDate("required_date", addMode ? ""
				: db.getText("required_date")));

		ht.put("test_started_date", new WebFieldDate("test_started_date",
				addMode ? "" : db.getText("test_started_date")));

		ht.put("received_date", new WebFieldDate("received_date", addMode ? ""
				: db.getText("received_date")));

		ht.put("psup_start_date", new WebFieldDate("psup_start_date",
				addMode ? "" : db.getText("psup_start_date")));

		ht.put("delivery_date", new WebFieldDate("delivery_date", addMode ? ""
				: db.getText("delivery_date")));

		ht.put("desire_date", new WebFieldDate("desire_date", addMode ? "" : db
				.getText("desire_date")));

		ht.put("install_date", new WebFieldDate("install_date", addMode ? ""
				: db.getText("install_date")));

		/*
		 * Blobs
		 */

		ht.put("bsns_driver_blob", new WebFieldText("bsns_driver_blob",
				addMode ? "" : db.getText("bsns_driver_blob"), 5, 100));

		ht.put("authorize_reason_blob", new WebFieldText(
				"authorize_reason_blob", addMode ? "" : db
						.getText("authorize_reason_blob"), 5, 100));

		ht.put("cost_desc_blob", new WebFieldText("cost_desc_blob",
				addMode ? "" : db.getText("cost_desc_blob"), 5, 100));

		ht.put("release_comments_blob", new WebFieldText(
				"release_comments_blob", addMode ? "" : db
						.getText("release_comments_blob"), 5, 100));

		ht.put("description_blob", new WebFieldText("description_blob",
				addMode ? "" : db.getText("description_blob"), 5, 100));

		ht.put("work_around_blob", new WebFieldText("work_around_blob",
				addMode ? "" : db.getText("work_around_blob"), 5, 100));

		ht.put("file_nm", new WebFieldText("file_nm", addMode ? "" : db
				.getText("file_nm"), 3, 100));

		/*
		 * Return
		 */

		//debug("done getWebFields");

		return ht;

	}

	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();
		String templateName = "adhoc.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName() + "_Adhoc";
		int columns = 16; // need better way to figure out column count
		short startRow = 1;

		String excelFileName = excel.appendWorkbook(sm.getExcelPath(),
				templatePath, filePrefix, getExcelResultSet(), startRow,
				columns);

		return excelFileName;

	}

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vadhoc_excel WHERE 1=1 ");

		// filter Remedy group
		if ((!sm.Parm("FilterStatus").equalsIgnoreCase("0"))) {
			sb.append(" AND status_cd = '" + sm.Parm("FilterStatus") + "'");
		}

		// filter on owner
		if (sm.Parm("FilterOwner").length() == 0) {
		} else {
			if (!sm.Parm("FilterOwner").equalsIgnoreCase("0")) {
				sb.append(" AND owner_id = " + sm.Parm("FilterOwner"));
			}
		}

		// Received Date

		// Filter on Date Range for "received_date"... tricky one!!

		if (sm.Parm("FilterReceived").length() == 0) {

		} else {

			if (!sm.Parm("FilterReceived").equalsIgnoreCase("0")) {
				String filterMonth = sm.Parm("FilterReceived").substring(0, 2);
				String filterYear = "20"
						+ sm.Parm("FilterReceived").substring(3, 5);

				sb.append(" AND datepart(month, received_date) = "
						+ filterMonth + " AND datepart(yyyy, received_date) = "
						+ filterYear);
			}

		}

		sb.append(" ORDER BY AuthCode");

		debug("excel sql: " + sb.toString());

		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
		}
		return rs;

	}

}
