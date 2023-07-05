/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;
import db.DbFieldInteger;

/**
 * 
 *   2/15 added mySql
 * 3/13 as 'target' to list query 
 * */

/*******************************************************************************
 * Test Specification TableManager
 * 
 * Change Log:
 * 
 * 9/16/05 change name to lowercase to be consistent with detail form naming
 * conventions 5/24/06 - add 'Requirement' as parent form
 * 
 * TODO : add logic to turn off reviewed by and reviewed date when pass_flag
 * turned off 3/10 convert to tcodes
 * 
 * 5/24/06 - add trequirement as the parrent
 * 
 */

public class TestcasePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public TestcasePlugin() throws services.ServicesException {
		super();

		this.setTableName("ttestcase");
		this.setKeyName("testcase_id");
		this.setTargetTitle("Test Case");

		this.setIsDetailForm(true);
		this.setParentTarget("Requirement");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Step");
		this.setDetailTargetLabel("Steps");

		this.setListHeaders(new String[] { "Reference Id", "Title",
				"Requirement", "Status", "Approved" });
	}

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		this
				.setMoreListJoins(new String[] {
						" left join tcodes on ttestcase.status_cd = tcodes.code_value and tcodes.code_type_id  =  5 ",
						" left join trequirement on ttestcase.requirement_id = trequirement.requirement_id " });

		if (sm.isSQLServer()) {
			this
					.setMoreListColumns(new String[] {
							"ttestcase.reference_nm",
							"ttestcase.title_nm as case_title ",
							"trequirement.title_nm",
							"tcodes.code_desc as status_desc",
							" CASE isDate(ttestcase.approved_date ) WHEN 1 THEN 'Yes' ELSE 'No' END  as 'Approved' " });
		} else {
			this
					.setMoreListColumns(new String[] {
							"ttestcase.reference_nm",
							"ttestcase.title_nm as case_title ",
							"trequirement.title_nm",
							"tcodes.code_desc as status_desc",
							" if (ttestcase.approved_date =  '0000-00-00' , 'No' ,  'Yes' )  as 'Approved' " });
		}

	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("requirement_id", new DbFieldInteger("requirement_id", sm
				.getParentId()));

		return true;
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		sm.setStepKind("Testcase"); // leave a cookie so the Step manager knows
		// what kind of a step to take

		Hashtable userHt = getLookupTable("tuser", "user_id", "last_name");

		/*
		 * Ids
		 */

		// get tuse_case table from cache via this query.
		String case_query = ("select title_nm, use_case_id, title_nm from tuse_case where application_id = " + sm
				.getApplicationId().toString());

		String reqr_query = ("select title_nm, requirement_id, title_nm from trequirement where application_id = " + sm
				.getApplicationId().toString());

		/*
		 * Codes
		 */
		WebFieldSelect wfStatus = new WebFieldSelect("status_cd",
				addMode ? "NEW" : db.getText("status_cd"), sm
						.getCodes("STATUS"));

		WebFieldSelect wfUseCase = new WebFieldSelect("use_case_id",
				addMode ? new Integer("0") : db.getInteger("use_case_id"), sm
						.getTable("tuse_case", case_query));

		WebFieldSelect wfReqr = new WebFieldSelect("requirement_id",
				addMode ? new Integer("0") : db.getInteger("requirement_id"),
				sm.getTable("requirement_id", reqr_query));

		/*
		 * Strings
		 */

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);

		WebFieldString wfReference = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfVersion = new WebFieldString("version_no",
				addMode ? "" : db.getText("version_no"), 8, 8);

		WebFieldString wfTestPlan = new WebFieldString("test_plan_ref",
				addMode ? "" : db.getText("test_plan_ref"), 32, 32);

		/*
		 * Blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 60);

		WebFieldText wfInput = new WebFieldText("input_spec_nm", addMode ? ""
				: db.getText("input_spec_nm"), 3, 60);

		WebFieldText wfOutput = new WebFieldText("output_spec_nm", addMode ? ""
				: db.getText("output_spec_nm"), 3, 60);

		WebFieldText wfEnv = new WebFieldText("test_env_nm", addMode ? "" : db
				.getText("test_env_nm"), 3, 60);

		WebFieldText wfDataInit = new WebFieldText("data_init_tx", addMode ? ""
				: db.getText("data_init_tx"), 3, 60);

		WebFieldText wfCaseDepend = new WebFieldText("case_dependencies_tx",
				addMode ? "" : db.getText("case_dependencies_tx"), 3, 60);

		/*
		 * Return ht
		 */

		WebField[] wfs = { wfReqr, wfTitle, wfReference, wfStatus, wfUseCase,
				wfVersion, wfTestPlan, wfDesc, wfInput, wfOutput, wfEnv,
				wfDataInit, wfCaseDepend };

		return webFieldsToHT(wfs);

	}

}
