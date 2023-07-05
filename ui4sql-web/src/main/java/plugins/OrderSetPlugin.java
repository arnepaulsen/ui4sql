/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.util.Hashtable;

import org.apache.poi.hssf.util.HSSFColor;

import router.SessionMgr;
import services.ExcelWriter;
import forms.*;

/**
 * 
 * 2/15 added mySql 3/10 use tcodes 3/13 as 'target' to list query 4/10 set
 * projectid on add, rearrange
 * 
 * 5/28/06 make 'Testcase' a child form
 * 
 * 
 * 11/4/09 - default prod_flag = N
 * 
 * 12/21/09 - replace reference_nm with order_set_no
 * 
 * 6/13/10 - use standard view names 
 */

public class OrderSetPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public OrderSetPlugin() throws services.ServicesException {
		super();

		this.setTableName("korder_set");
		this.setKeyName("order_set_id");
		this.setTargetTitle("Order&nbsp;Sets");
		this.setListOrder("title_nm");

		
		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("OrderSetVers");
		this.setDetailTargetLabel("Paper&nbsp;Versions");
		this.setShowAuditSubmitApprove(false);

		this.setUpdatesLevel("inpatient");
		this.setExcelOk(true);
		this.setSubmitOk(false);
		this.setCopyOk(false);

		this.setListViewName("vorderset_list");

		this.setListHeaders(new String[] { "No", "Name", "Domain",
				"SUIT", "Released" });

		// filters on Domain, Suite, and Released(y/n) 
		this.setListSelectorColumnFlags(new boolean[] { false, false, true,
				true, true });
	}

	public void init(SessionMgr parmSm) {
		super.init(parmSm);

		if (sm.Parm("FilterAlpha").equalsIgnoreCase("Y"))
			this.setListOrder("title_nm");

		else
			this.setListOrder("order_set_no,title_nm ");

	}

	public boolean getListColumnCenterOn(int columnNumber) {
		if (columnNumber > 4 && columnNumber < 7)
			return true;
		else
			return false;
	}

	/***************************************************************************
	 * 
	 * List Processing
	 * 
	 **************************************************************************/

	public String getListTitle() {

		String checked = "";
		if (sm.Parm("FilterAlpha").equalsIgnoreCase("Y")) {
			checked = "checked";
		}

		return "Order Sets&nbsp;&nbsp;&nbsp;&nbsp;Alphabetic Sort&nbsp;<input type='checkbox' name='FilterAlpha' id='FilterAlpha' value=Y "
				+ checked + ">";
	}

	public WebField getListSelector(int columnNumber) {

		if (columnNumber ==2) {
			String qry = "SELECT code_value, code_value, code_desc from tcodes where code_type_id = 142 and code_desc2 like '%"
					+ "OS" + "%' ORDER BY 3 ";

			Hashtable domains = new Hashtable();

			domains = sm.getTable("IPDOMAINOS", qry);

			return getListSelector("FilterDomain", "", "Domain?", domains);
		}
		
		if (columnNumber == 3) {
			return getListSelector("FilterSUIT", "", "SUIT?", sm.getCodes("AMBSUIT"));
		}

		if (columnNumber == 4) {
			return getListSelector("FilterReleased", "", "Released?", sm.getCodes("YESNO"));
		}

		
		// will never get here

		return getListSelector("dummy", new Integer(""), "badd..",
				new Hashtable());

	}

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
		 */

		StringBuffer sb = new StringBuffer();

		if (sm.Parm("FilterDomain").length() == 0) {
		} else {
			if (!sm.Parm("FilterDomain").equalsIgnoreCase("0")) {
				sb.append(" AND os_domain_1_cd = '" + sm.Parm("FilterDomain") + "'");
			}
		}

		if (sm.Parm("FilterSUIT").length() == 0) {
		} else {
			if (!sm.Parm("FilterSUIT").equalsIgnoreCase("0")) {
				sb.append(" AND suite_cd = '" + sm.Parm("FilterSUIT") + "'");
			}
		}
		
		if (sm.Parm("FilterReleased").length() == 0) {
		} else {
			if (!sm.Parm("FilterReleased").equalsIgnoreCase("0")) {
				sb.append(" AND prod_flag = '" + sm.Parm("FilterReleased") + "'");
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
		 * Set up for detail form 'testcase'
		 */
		if (parmMode.equalsIgnoreCase("show")) {

			sm.setParentId(db.getInteger("order_set_id"), db
					.getText("title_nm"));
		}

		Hashtable htProcesses = db
				.getLookupTable("select title_nm as odor, workflow_id, title_nm from tworkflow");

		String qry = "SELECT code_value, code_value, code_desc from tcodes where code_type_id = 142 and code_desc2 like '%OS%' ORDER BY 3 ";

		Hashtable domains = new Hashtable();

		domains = sm.getTable("IPDOMAINOS", qry);

		/*
		 * id's
		 */

		ht.put("project_id", new WebFieldSelect("project_id",
				addMode ? new Integer("0") : db.getInteger("project_id"), sm
						.getProjectFilter()));

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));

		/*
		 * dates
		 */

		ht.put("release_date", new WebFieldDate("release_date", (addMode ? ""
				: db.getText("release_date"))));

		ht.put("chimp_paper_rv_date", new WebFieldDate("chimp_paper_rv_date",
				(addMode ? "" : db.getText("chimp_paper_rv_date"))));

		ht.put("chimp_screen_rv_date", new WebFieldDate("chimp_screen_rv_date",
				(addMode ? "" : db.getText("chimp_screen_rv_date"))));

		ht.put("hc_1_1_rv_date", new WebFieldDate("hc_1_1_rv_date",
				(addMode ? "" : db.getText("hc_1_1_rv_date"))));

		ht.put("amb_phar_rv_date", new WebFieldDate("amb_phar_rv_date",
				(addMode ? "" : db.getText("amb_phar_rv_date"))));

		/*
		 * codes
		 */

		ht.put("suite_cd", new WebFieldSelect("suite_cd", addMode ? "" : db
				.getText("suite_cd"), sm.getCodes("AMBSUIT"), " "));

		ht.put("domain_cd", new WebFieldSelect("domain_cd", addMode ? "" : db
				.getText("domain_cd"), domains, true, true));

		ht.put("os_domain_1_cd", new WebFieldSelect("os_domain_1_cd",
				addMode ? "" : db.getText("os_domain_1_cd"), domains, true,
				true));
		ht.put("os_domain_2_cd", new WebFieldSelect("os_domain_2_cd",
				addMode ? "" : db.getText("os_domain_2_cd"), domains, ""));

		ht.put("os_domain_3_cd", new WebFieldSelect("os_domain_3_cd",
				addMode ? "" : db.getText("os_domain_3_cd"), domains, ""));

		ht.put("os_domain_4_cd", new WebFieldSelect("os_domain_4_cd",
				addMode ? "" : db.getText("os_domain_4_cd"), domains, ""));

		ht.put("os_domain_5_cd", new WebFieldSelect("os_domain_5_cd",
				addMode ? "" : db.getText("os_domain_5_cd"), domains, ""));

		ht.put("os_domain_6_cd", new WebFieldSelect("os_domain_6_cd",
				addMode ? "" : db.getText("os_domain_6_cd"), domains, ""));

		ht.put("os_domain_7_cd", new WebFieldSelect("os_domain_7_cd",
				addMode ? "" : db.getText("os_domain_7_cd"), domains, ""));

		ht.put("os_domain_8_cd", new WebFieldSelect("os_domain_8_cd",
				addMode ? "" : db.getText("os_domain_8_cd"), domains, ""));

		ht.put("update_chimp_comment_cd", new WebFieldSelect(
				"update_chimp_comment_cd", addMode ? "" : db
						.getText("update_chimp_comment_cd"), sm
						.getCodes("YESNO"), true));

		ht.put("nurse_rv_cd", new WebFieldSelect("nurse_rv_cd", addMode ? ""
				: db.getText("nurse_rv_cd"), sm.getCodes("YESNO"), true));

		String[][] prod = { { "Y", "N", "R" }, { "Yes", "No", "Ret" } };

		ht.put("prod_flag", new WebFieldSelect("prod_flag", addMode ? "N" : db
				.getText("prod_flag"), prod));

		/*
		 * strings
		 */

		ht.put("version_no", new WebFieldString("version_no", (addMode ? ""
				: db.getText("version_no")), 6, 6));

		ht.put("protocols_tx", new WebFieldString("protocols_tx", (addMode ? ""
				: db.getText("protocols_tx")), 80, 255));

		ht.put("core_measures_tx", new WebFieldString("core_measures_tx",
				(addMode ? "" : db.getText("core_measures_tx")), 80, 255));

		ht.put("order_set_no", new WebFieldString("order_set_no", (addMode ? ""
				: db.getText("order_set_no")), 8, 8));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("notes_tx", new WebFieldString("notes_tx", (addMode ? "" : db
				.getText("notes_tx")), 64, 128));

		ht.put("used_by_tx", new WebFieldString("used_by_tx", (addMode ? ""
				: db.getText("used_by_tx")), 8, 8));

		ht.put("suit_nm", new WebFieldString("suit_nm", (addMode ? "" : db
				.getText("suit_nm")), 8, 8));

		ht.put("limited_rlse_tx", new WebFieldString("limited_rlse_tx",
				(addMode ? "" : db.getText("limited_rlse_tx")), 32, 32));

		ht.put("elemetry_ip_tx", new WebFieldString("elemetry_ip_tx",
				(addMode ? "" : db.getText("elemetry_ip_tx")), 64, 64));

		ht.put("core_measure_cd", new WebFieldString("core_measure_cd",
				(addMode ? "" : db.getText("core_measure_cd")), 8, 8));

		ht.put("paper_done_tx", new WebFieldString("paper_done_tx",
				(addMode ? "" : db.getText("paper_done_tx")), 8, 8));

		ht.put("under_domain_rv_tx", new WebFieldString("under_domain_rv_tx",
				(addMode ? "" : db.getText("under_domain_rv_tx")), 12, 12));

		ht.put("cl_posting_tx", new WebFieldString("cl_posting_tx",
				(addMode ? "" : db.getText("cl_posting_tx")), 12, 12));

		ht.put("year_review_date", new WebFieldDate("year_review_date",
				(addMode ? "" : db.getText("year_review_date"))));

	
		ht.put("next_review_date", new WebFieldDate("next_review_date",
				(addMode ? "" : db.getText("next_review_date"))));

	
		
		
		return ht;

	}

	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "OrderSet.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName().replace(" ", "") + "_OrderSet";
		int columns = 17;
		short startRow = 1;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);
	}

	public ResultSet getExcelResultSet() {

		String query = "SELECT * FROM vorderset_excel WHERE 1=1 ORDER BY OrderSetName ";

		ResultSet rs = null;

		try {
			rs = db.getRS(query);
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

}
