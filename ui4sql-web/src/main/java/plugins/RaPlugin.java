/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import services.ServicesException;
import router.SessionMgr;
import forms.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import services.ExcelWriter;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * 
 * Systems Integration - Request Authorization
 * 
 * Note.. the "Bridges" didn't get used by the team, its saved off as RaBridges
 * 
 * To Do: change test for nuid to some other way to distinguish manager in SI
 * 
 * Change log:
 * 
 * 7/10/10 use vra_excel_limited to bloc the Epic code field if user not in a
 * limited set add 'priv_user() boolean function to return list of priv users.
 * 
 */
public class RaPlugin extends AbsDivisionPlugin {

	/*
	 * static
	 */

	private static String[][] issue_types = {
			{ "P", "F", "CC", "D", "DC", "O", "PS", "REV", "CLN", "G" },
			{ "Performance", "Fix", "Custom Code", "Data", "Debug Code",
					"Other", "Pt.Safety", "Revenue", "Clean-Up", "Gen. Help" } };

	private static String[][] fix_types = { { "F", "E", "D", "NA", "O" },
			{ "Fix", "Enhancement", "Debug", "Not Applicable", "Other" } };

	private static String[][] envs = { { "CLNT", "SERV", "OTHR", "NA" },
			{ "Client", "Server", "Other", "N/A" } };

	private static String[] listHeaders = { "RA#", "Title", "Status",
			"Product", "Fix Type", "Issue Type", "Environment", "Author" };

	private static String[][] approvers = {
			{ "D112011", "F117112", "F327190", "G336027", "Q613048", "T177767",
					"X514008", "X746116" },
			{ "Galvan,Melanie", "Hare,Shane", "Huang,Zheng", "Chua,George",
					"Gall,Susan", "Green,Jeremy", "Lin,Margaret", "Wei,Sun" } };

	/*
	 * filters
	 */

	private BeanFieldSelect filterStatus = new BeanFieldSelect(2,
			"FilterStatus", "status_cd", "O", "O", "Status?", "STATUS1");

	private BeanFieldSelect filterFix = new BeanFieldSelect(4, "FilterFix",
			"fix_type_cd", "", "", "Fix Type?", "");

	private BeanFieldSelect filterIssue = new BeanFieldSelect(5, "FilterIssue",
			"issue_type_cd", "", "", "Issue Type?", "");

	private BeanFieldSelect filterEnv = new BeanFieldSelect(6, "FilterEnv",
			"env_cd", "", "", "Env?", "");

	/*
	 * web fields
	 */

	private BeanWebField code_field = null; // holder for eiher the display or
	// edit box, depending on who user
	// is

	private BeanFieldDate issue_date = new BeanFieldDate("issue_date");

	private BeanFieldString reference_nm = new BeanFieldString("reference_nm",
			8, 8);

	private BeanFieldString title_nm = new BeanFieldString("title_nm", 64, 64);

	private BeanFieldString product_nm = new BeanFieldString("product_nm", 16,
			16);

	private BeanFieldString memo_tx = new BeanFieldString("memo_tx", 80, 255);

	private BeanFieldLink doc_link_tx = new BeanFieldLink("doc_link_tx", 75,
			128);

	private BeanFieldSelect status_cd = new BeanFieldSelect("status_cd", "O",
			"STATUS1");

	private BeanFieldSelect faxed_cd = new BeanFieldSelect("faxed_cd", "",
			"YESNO");

	private static BeanFieldSelect issue_type_cd = new BeanFieldSelect(
			"issue_type_cd", "", "");

	private BeanFieldSelect reviewer_cd = new BeanFieldSelect(
			"reviewer_nuid_cd", "", "");

	private BeanFieldString author_nm = new BeanFieldString("author_nm", 32, 32);

	private BeanFieldSelect reviewer_nuid_cd = new BeanFieldSelect(
			"reviewer_nuid_cd", "", "");

	private BeanFieldString slg_num_tx = new BeanFieldString("slg_num_tx", 12,
			12);

	private BeanFieldString region_tx = new BeanFieldString("region_tx", 64, 64);

	private BeanFieldSelect fix_type_cd = new BeanFieldSelect("fix_type_cd",
			"", "");

	private BeanFieldSelect env_cd = new BeanFieldSelect("env_cd", "", "");

	private BeanFieldString env_tx = new BeanFieldString("env_tx", 32, 32);

	private BeanFieldText desc_blob = new BeanFieldText("desc_blob", 4, 100);

	private BeanFieldText epic_code_blob = new BeanFieldText("epic_code_blob",
			4, 100);

	// if user is not executive. then blank out the epic code

	private BeanFieldDisplay epic_code_noshow = new BeanFieldDisplay(
			"epic_code_blob", "just_a_blank");

	/*
	 * linked rfc display info
	 */

	private BeanFieldString rfc1 = new BeanFieldString("rfc1_no", 8, 8);
	private BeanFieldDisplay rfc1_desc = new BeanFieldDisplay("rfc1_desc_blob");
	private BeanFieldDisplay rfc1_start_dt = new BeanFieldDisplay(
			"rfc1_start_dt");
	private BeanFieldDisplay rfc1_end_dt = new BeanFieldDisplay("rfc1_end_dt");
	private BeanFieldDisplay rfc1_owner = new BeanFieldDisplay("rfc1_owner");
	private BeanFieldDisplay rfc1_requester = new BeanFieldDisplay(
			"rfc1_requester");
	private BeanFieldDisplay rfc1_status = new BeanFieldDisplay("rfc1_status");

	private BeanFieldString rfc2 = new BeanFieldString("rfc2_no", 8, 8);
	private BeanFieldDisplay rfc2_desc = new BeanFieldDisplay("rfc2_desc_blob");
	private BeanFieldDisplay rfc2_start_dt = new BeanFieldDisplay(
			"rfc2_start_dt");
	private BeanFieldDisplay rfc2_end_dt = new BeanFieldDisplay("rfc2_end_dt");
	private BeanFieldDisplay rfc2_owner = new BeanFieldDisplay("rfc2_owner");
	private BeanFieldDisplay rfc2_requester = new BeanFieldDisplay(
			"rfc2_requester");
	private BeanFieldDisplay rfc2_status = new BeanFieldDisplay("rfc2_status");

	private BeanFieldString rfc3 = new BeanFieldString("rfc3_no", 8, 8);
	private BeanFieldDisplay rfc3_desc = new BeanFieldDisplay("rfc3_desc_blob");
	private BeanFieldDisplay rfc3_start_dt = new BeanFieldDisplay(
			"rfc3_start_dt");
	private BeanFieldDisplay rfc3_end_dt = new BeanFieldDisplay("rfc3_end_dt");
	private BeanFieldDisplay rfc3_owner = new BeanFieldDisplay("rfc3_owner");
	private BeanFieldDisplay rfc3_requester = new BeanFieldDisplay(
			"rfc3_requester");
	private BeanFieldDisplay rfc3_status = new BeanFieldDisplay("rfc3_status");

	private BeanFieldDisplay rfc1_link = new BeanFieldDisplay("rfc1_link");
	private BeanFieldDisplay rfc2_link = new BeanFieldDisplay("rfc2_link");
	private BeanFieldDisplay rfc3_link = new BeanFieldDisplay("rfc3_link");

	public RaPlugin() throws services.ServicesException {
		super();
		this.setTableName("tra");
		this.setKeyName("ra_id");
		this.setTargetTitle("Request Authorization");
		this.setListOrder("ra_num");
		this.setListViewName("vra_list");
		this.setSelectViewName("vra");
		this.setShowAuditSubmitApprove(false);

		this.setListFilters(new BeanWebField[] { filterStatus, filterEnv,
				filterFix, filterIssue });

		this.setGoToKeyIsInteger(false);
		this.setExcelOk(true);
		this.setGotoOk(true);
		this.setGotoDisplayName("RA #: ");
		this.setGotoKeyName("reference_nm");

		this.setListHeaders(listHeaders);

		fix_type_cd.setChoiceArray(fix_types);
		env_cd.setChoiceArray(envs);
		issue_type_cd.setChoiceArray(issue_types);
		reviewer_cd.setChoiceArray(approvers);
		reviewer_nuid_cd.setChoiceArray(approvers);

		filterEnv.setChoiceArray(envs);
		filterFix.setChoiceArray(fix_types);
		filterIssue.setChoiceArray(issue_types);

	}

	public void init(SessionMgr parmSm) {

		super.init(parmSm);

		// todo: get rid if hard-coded nuid's
		// Shane Hare, Susan Gall and George Chua

		if (priv_user()) {
			code_field = epic_code_blob;
			this.setExcelTemplate("vra_excel", "ra.xls", 1, 13);
		}

		else {
			this.setExcelTemplate("vra_excel_limited", "ra.xls", 1, 13);
			code_field = epic_code_noshow;
		}

		this.setWebFieldBeans(new BeanWebField[] { code_field, reference_nm,
				issue_date, title_nm, product_nm, doc_link_tx, status_cd,
				author_nm, reviewer_nuid_cd, slg_num_tx, region_tx,
				issue_type_cd, fix_type_cd, env_tx, desc_blob, faxed_cd, rfc1,
				rfc1_desc, rfc1_start_dt, rfc1_end_dt, rfc1_owner,
				rfc1_requester, rfc1_status, rfc2, rfc2_desc, rfc2_start_dt,
				rfc2_end_dt, rfc2_owner, rfc2_requester, rfc2_status, rfc3,
				rfc3_desc, rfc3_start_dt, rfc3_end_dt, rfc3_owner,
				rfc3_requester, rfc3_status, rfc1_link, rfc2_link, rfc3_link,
				reviewer_cd, env_cd, memo_tx });

	}

	private boolean priv_user() {

		if (sm.getHandle().equalsIgnoreCase("f117112")
				|| sm.getHandle().equalsIgnoreCase("Q613048")
				|| sm.getHandle().equalsIgnoreCase("A511811")
				|| sm.getHandle().equalsIgnoreCase("D576781"))
			return true;
		else
			return false;

	}

}
