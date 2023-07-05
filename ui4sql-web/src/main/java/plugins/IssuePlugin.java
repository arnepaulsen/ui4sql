/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import db.*;

import java.util.*;

import router.SessionMgr;

/**
 * Issue Plugin
 * 
 * Change log:
 * 
 * 6/20 added logic in 'beforeUpdate' to set the closed_by_uid and closed_date
 * 
 * 8/22 added list selector for status column
 * 
 * 4/20/09 = Wow ! convert to a Spring bean !!
 * 
 * 6/14/10 change views to standard names 
 *  (be sure to create view vissue !
 *  
 * 
 */

public class IssuePlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	/*
	 * List Page Filters
	 */

	private static String subProjectQuery = "select c.order_by as odor, code_value, code_desc "
			+ " from tproject p join tcodes c on p.chng_req_nm = c.code_desc2 "
			+ " where p.project_id = %PROJECTID% and c.code_type_id = 157  ";

	// note the symbolic %PROJECTID% is resolved at run-time!

	private static BeanFieldSelect filterStatus = new BeanFieldSelect(2,
			"FilterStatus", "status_cd", "O", "O", "Status?", "ISSUESTAT");

	private static BeanFieldSelect filterPriority = new BeanFieldSelect(3,
			"FilterPriority", "priority_cd", "L", "", "Priority?", "PRIORITY");

	// 99 = code word for userId
	private static BeanFieldSelect filterSubProject = new BeanFieldSelect(4,
			"FilterSubproject", "sub_cd	", "", "", "Topic ?", "SQL",
			subProjectQuery);
	
	// 99 = code word for userId
	private static BeanFieldSelect filterUser = new BeanFieldSelect(5,
			"FilterUser", "assigned_uid", 99, 0, "Owner?", "userHT");



	/*
	 * Web Beans
	 */

	private static BeanFieldString reference_nm = new BeanFieldString(
			"reference_nm", 32, 32);

	private static BeanFieldString requestor_nm = new BeanFieldString(
			"requestor_nm", 32, 32);

	private static BeanFieldSelect assigned_uid = new BeanFieldSelect(
			"assigned_uid", 99, "userHT"); // secret code for sm.getUserId()

	private static BeanFieldSelect status_cd = new BeanFieldSelect("status_cd",
			"New", "ISSUESTAT");

	private static BeanFieldSelect type_cd = new BeanFieldSelect("type_cd", "",
			"ISSUTYPE");

	private static BeanFieldSelect priority_cd = new BeanFieldSelect(
			"priority_cd", "", "PRIORITY");

	private static BeanFieldSelect sub_cd = new BeanFieldSelect("sub_cd", "",
			"SQL", subProjectQuery);

	private static BeanFieldString title_nm = new BeanFieldString("title_nm",
			64, 64);

	private static BeanFieldDisplay closed_date = new BeanFieldDisplay(
			"closed_date");

	private static BeanFieldText desc_blob = new BeanFieldText("desc_blob", 4,
			100);

	private static BeanFieldText notes_blob = new BeanFieldText("notes_blob",
			4, 100);

	private static BeanFieldText impact_blob = new BeanFieldText("impact_blob",
			4, 100);

	private static BeanFieldDisplay closed_by = new BeanFieldDisplay(
			"closed_by", "last_name");

	private static BeanFieldDate install_date = new BeanFieldDate(
			"install_date");

	private static BeanFieldDate id_date = new BeanFieldDate("id_date");

	private static BeanFieldString prod_rfc_no = new BeanFieldString(
			"prod_rfc_no", 8, 8);

	private static BeanFieldString defect_no = new BeanFieldString("defect_no",
			8, 8);

	private static BeanFieldString problem_no = new BeanFieldString(
			"problem_no", 8, 8);

	public IssuePlugin() throws services.ServicesException {
		super();

		this.setWebFieldBeans(new BeanWebField[] { reference_nm, assigned_uid,
				status_cd, type_cd, priority_cd, sub_cd, title_nm, closed_date,
				desc_blob, notes_blob, impact_blob, closed_by, install_date,
				id_date, prod_rfc_no, problem_no, requestor_nm, defect_no });

		this.setTableName("tissue");
		this.setKeyName("issue_id");
		this.setTargetTitle("Issue");

		this.setListHeaders(new String[] { "Reference", "Title", "Status",
				"Priority", "Type", "Owner" });

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("IssueLog");
		this.setDetailTargetLabel("History");

		this.setSelectViewName("vissue");
		this.setListViewName("vissue_list");


		this.setListOrder("reference_nm");

		this.setExcelOk(true);
		this.setExcelTemplate("vissue_excel", "Issue.xls", 1, 11);
		// this.setUpdatesLevel("administrator");

		this.setSubmitOk(false);

		this.setShowAuditSubmitApprove(false);

		this.setListFilters(new BeanFieldSelect[] { filterStatus,
				filterPriority, filterSubProject, filterUser });

	}

	

	/***************************************************************************
	 * 
	 * update closed-by date and close-by-uid
	 * 
	 **************************************************************************/
	public void beforeUpdate(Hashtable<String, DbField> ht) {
		// * set the closed fields if new status is closed

		if (sm.Parm("status_cd").equalsIgnoreCase("c")
				&& !sm.Parm("oldstatuscode").equalsIgnoreCase("c")) {
			ht.put("closed_date", new DbFieldDate("closed_date", new Date()));
			ht.put("closed_by", new DbFieldInteger("closed_by_uid", sm
					.getUserId()));
		}

		// clear the closed fields if the issue is re-opened
		if (!sm.Parm("status_cd").equalsIgnoreCase("c")
				&& sm.Parm("oldstatuscode").equalsIgnoreCase("c")) {
			ht.put("closed_date", new DbFieldDate("closed_date", ""));
			ht.put("closed_by", new DbFieldInteger("closed_by_uid",
					new Integer("0")));
		}
	}
}
