/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import remedy.RemedyChangeSubmit;
import router.SessionMgr;
import db.DbFieldString;
import forms.*;

/**
 * Test Request to NCAL BIO
 * 
 * 
 * 6/13/10 change to standard view names
 * 
 * 1/25/11 - must set "Change Summar" to some value
 *  
 * 
 * 
 */

public class TestPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private static BeanFieldString title_nm = new BeanFieldString("title_nm",
			64, 64);

	private static BeanFieldString template_nm = new BeanFieldString(
			"template_nm", 64, 64);

	private static BeanFieldString sr_no = new BeanFieldString("sr_no", 8, 8);

	private static BeanFieldString cab_no = new BeanFieldString("cab_sr_no", 8,
			8);

	private static BeanFieldString anc_list_tx = new BeanFieldString(
			"anc_list_tx", 64, 255);

	private static BeanFieldString epic_products_tx = new BeanFieldString(
			"epic_products_tx", 64, 255);

	private static BeanFieldString test_env_tx = new BeanFieldString(
			"test_env_tx", 64, 255);

	/*
	 * codes
	 */

	private static BeanFieldSelect priority_cd = new BeanFieldSelect(
			"priority_cd", "", "PRIORITY");

	private static BeanFieldSelect status_cd = new BeanFieldSelect("status_cd",
			"", "STATOIP", true);
	// true forces sm.getCodesAlt(table-name);

	/*
	 * links to other pages
	 */
	private static BeanFieldDisplay sr_link = new BeanFieldDisplay("srlink");

	private static BeanFieldDisplay plan_link = new BeanFieldDisplay("planlink");

	/*
	 * dates
	 */

	private static BeanFieldDate install_dt = new BeanFieldDate("install_dt");

	private static BeanFieldDate cab_dt = new BeanFieldDate("cab_dt");

	private static BeanFieldDate test_start_date = new BeanFieldDate(
			"test_start_date");

	private static BeanFieldDate test_end_date = new BeanFieldDate(
			"test_end_date");

	/*
	 * id's
	 */

	// 99 = code word for userId
	private static BeanFieldSelect owner_uid = new BeanFieldSelect("owner_uid",
			99, "userHT"); // secret code for sm.getUserId()

	private static BeanFieldSelect baod_uid = new BeanFieldSelect("baod_uid",
			0, "userHT"); // secret code for sm.getUserId()

	private static BeanFieldSelect manager_uid = new BeanFieldSelect(
			"manager_uid", 0, "userHT"); // secret code for sm.getUserId()

	private static BeanFieldSelect builder_uid = new BeanFieldSelect(
			"builder_uid", 0, "userHT"); // secret code for sm.getUserId()

	private static BeanFieldCheckbox ip_uat_flag = new BeanFieldCheckbox(
			"ip_uat_flag", "N", "");

	private static BeanFieldCheckbox ecp_reqr_flag = new BeanFieldCheckbox(
			"ecp_reqr_flag", "N", "");

	/*
	 * blobs
	 */

	private static BeanFieldText plan_ecp_blob = new BeanFieldText(
			"plan_ecp_blob", 4, 100);

	private static BeanFieldText desc_blob = new BeanFieldText("desc_blob", 4,
			100);

	private static BeanFieldText benefits_blob = new BeanFieldText(
			"benefits_blob", 4, 100);

	private static BeanFieldText execution_blob = new BeanFieldText(
			"execution_blob", 4, 100);

	private static BeanFieldText test_data_blob = new BeanFieldText(
			"test_data_blob", 4, 100);

	private static BeanFieldText data_regr_blob = new BeanFieldText(
			"data_regr_blob", 4, 100);
	private static BeanFieldText other_reqr_blob = new BeanFieldText(
			"other_reqr_blob", 4, 100);

	/*
	 * list filters
	 */

	private BeanFieldSelect filterStatus = new BeanFieldSelect(3,
			"FilterStatus", "status_cd", "O", "O", "Status?", "STATOIP");

	private static BeanFieldSelect filterPriority = new BeanFieldSelect(2,
			"FilterPriority", "priority_cd", "M", "", "Priority?", "PRIORITY");

	/*
	 * Remedy
	 */

	BeanFieldDisplay wfRemedyEndDt = new BeanFieldDisplay("fmt_remedy_end_dt");

	BeanFieldDisplay wfRemedyRequestDt = new BeanFieldDisplay(
			"fmt_remedy_requested_completion_dt");

	BeanFieldDisplay wfOwner = new BeanFieldDisplay("owner_uid", "remedyOwner");

	BeanFieldDisplay wfRequestor = new BeanFieldDisplay("requester_uid",
			"remedyRequester");

	BeanFieldDisplay wfUrgency = new BeanFieldDisplay("remedy_urgency");
	
	BeanFieldDisplay wfStatus = new BeanFieldDisplay("remedy_status");

	BeanFieldDisplay wfRemedAsOf = new BeanFieldDisplay("remedy_asof_date");

	BeanFieldDisplay wfHost = new BeanFieldDisplay("host");

	BeanFieldDisplay wfTomcat = new BeanFieldDisplay("tomcat_name");

	/*
	 * Constructor
	 * 
	 */

	public TestPlugin() throws services.ServicesException {
		super();
		
		//this.remedyKey = "sr_no";   // todo ...may setter in absRemedy
		
		
		this.setTableName("ttest");
		this.setKeyName("test_id");
		this.setTargetTitle("Test Request");


		this.setListOrder("");

		//this.remedyKey = "sr_no";
		
		this.setRemedyOk(true);
		
		

		filterStatus.setAltCodeSet(true);

		this.setListFilters(new BeanFieldSelect[] { filterStatus,
				filterPriority });

		this.setListHeaders(new String[] { "SR No.", "Title", "Cycle",
				"Status", "Requestor" });

		this.setListViewName("vtest_list");

		this.setSelectViewName("vtest");

		this.setListOrder("sr_no");

		this.setSubmitOk(false);

		this.setShowAuditSubmitApprove(false);
	}

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		wfHost.setDisplayValue(sm.getHost() + ":" + sm.getServerPort());
		wfTomcat.setDisplayValue(sm.getTomcatName());

		this.setWebFieldBeans(new BeanWebField[] { title_nm, template_nm,
				sr_no, anc_list_tx, epic_products_tx, test_env_tx, priority_cd,
				install_dt, cab_dt, test_start_date, test_end_date,
				plan_ecp_blob, anc_list_tx, epic_products_tx, test_env_tx,
				test_start_date, desc_blob, benefits_blob, execution_blob,
				test_data_blob, data_regr_blob, other_reqr_blob, baod_uid,
				owner_uid, sr_link, plan_link, cab_no, builder_uid,
				manager_uid, ecp_reqr_flag, ip_uat_flag, wfRemedyEndDt,
				wfRemedyRequestDt, wfOwner, wfRequestor, wfRemedAsOf, wfHost,
				wfTomcat, wfUrgency, wfStatus });
		
	}

	public boolean afterGet() {
		sm.setRfcNo(db.getText("sr_no"), db.getText("title_nm"));
		return true;
		
	}
	
	public boolean beforeAdd(Hashtable ht) {

		//debug("Test SR Request - adding to Remedy!!!!");

		// construct the interface, with the Web access user-id and pw
		RemedyChangeSubmit req = new RemedyChangeSubmit(sm.getRemedyUserid(),
				sm.getRemedyPassword(), sm.getRemedyURL());

		// add it.

		req.setRemedyCategory("Organizational Services");
		req.setRemedyType("NCAL Testing");
		req.setRemedyItem("Testing");

		req.setChangeDescription(sm.Parm("title_nm"));
		req.setChangeSummary(sm.Parm("title_nm"));

		req.setStartDate(sm.Parm("test_start_date"));
		req.setEndDate(sm.Parm("test_end_date"));

		StringBuffer summary = new StringBuffer();
		summary.append("CAB Date: " + sm.Parm("cab_dt"));
		summary.append("\nTarget Complete Date: " + sm.Parm(""));
		summary.append("\nEpic Products: " + sm.Parm("epic_products_tx"));

		// bypass for testing.

		String case_id = req.submitChange("SR", sm.getHandle(), sm
				.getFirstName()
				+ " " + sm.getLastName());

		//debug("Remedy Case id " + case_id);

		String sr_no = case_id.substring(5);
		//debug(" Case # " + sr_no);

		try {
			ht.remove("sr_no");
		} catch (Exception e) {

		}

		ht.put("sr_no", new DbFieldString("sr_no", sr_no));

		return true;

	}

}
