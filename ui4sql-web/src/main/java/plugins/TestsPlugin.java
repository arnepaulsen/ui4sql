/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * GUI Data Manager
 * 
 * Change Log:
 * 
 * 5/19/05 Take out getDbFields!!
 * 
 * 
 */
public class TestsPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public TestsPlugin() throws services.ServicesException {
		super();
		this.setTableName("ttest");
		this.setKeyName("test_id");
		this.setTargetTitle("Test");

		this.setMoreListColumns(new String[] { "ttest.title_nm as TestTitle",
				"ttest.reference_nm", "tcycle.title_nm as CycleTitle",
				"s.code_desc as status_desc", "p.code_desc as result_desc",
				"concat(u.last_name, ',', u.first_name)",
				"ttest.status_cd", "ttest.result_cd", "ttest.assigned_uid" });

		this
				.setMoreListJoins(new String[] {
						" join tcycle on ttest.cycle_id = tcycle.cycle_id",
						" left join tcodes s on ttest.status_cd = s.code_value and s.code_type_id  = 45 ",
						" left join tcodes p on ttest.result_cd = p.code_value and p.code_type_id  = 92 ",
						" left join tuser u on ttest.assigned_uid = u.user_id " });

		this
				.setMoreSelectJoins(new String[] { " left join ttestcase tc on ttest.testcase_id = tc.testcase_id  " });
		this
				.setMoreSelectColumns(new String[] { "tc.title_nm as testcase_title" });

		this.setListHeaders(new String[] { "Title", "Reference", "Cycle",
				"Status", "Result", "Owner" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
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

		case 2: {
			Hashtable cycles = new Hashtable();
			try {
				cycles = db
						.getLookupTable(" select title_nm, cycle_id, title_nm from tcycle where project_id = "
								+ sm.getProjectId().toString());
			} catch (services.ServicesException e) {

			}
			WebFieldSelect wf = new WebFieldSelect("FilterCycle", (sm.Parm(
					"FilterCycle").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterCycle"))), cycles,
					"All Cycles");
			wf.setDisplayClass("listform");
			return wf;

		}

		case 3: {

			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "O" : sm
					.Parm("FilterStatus")), sm.getCodes("ISSUESTAT"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		}

		case 4: {
			WebFieldSelect wf = new WebFieldSelect("FilterResult", sm
					.Parm("FilterResult"), sm.getCodes("RESULT"), "All Results");
			wf.setDisplayClass("listform");
			return wf;
		}

		case 5:
		default: {

			WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
					"FilterUser").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterUser"))), sm.getUserHT(),
					"All Testors");
			wf.setDisplayClass("listform");
			return wf;

		}
		}

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		if ((!sm.Parm("FilterCycle").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterCycle").length() > 0)) {
			sb.append(" AND ttest.cycle_id = " + sm.Parm("FilterCycle"));
		}

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND ttest.status_cd = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND ttest.status_cd = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		if ((!sm.Parm("FilterResult").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterResult").length() > 0)) {
			sb.append(" AND ttest.result_cd = '" + sm.Parm("FilterResult")
					+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND ttest.assigned_uid = " + sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Ids
		 */

		debug("get env ...");

		Hashtable envs = sm
				.getTable(
						"tenvironment",
						"select title_nm, environment_id, title_nm from tenvironment where division_id = "
								+ sm.getDivisionId().toString()
								+ " order by title_nm");

		debug("get cycles ...");

		Hashtable cycles = sm.getTable("tcycle",
				" select title_nm, cycle_id, title_nm from tcycle where project_id = "
						+ sm.getProjectId().toString());

		debug("1 - env id ...");
		ht.put("env_id", new WebFieldSelect("env_id",
				addMode ? new Integer("0") : db.getInteger("env_id"), envs));

		debug("2");

		// redundant because env_id is part of cycle
		ht
				.put("cycle_id", new WebFieldSelect("cycle_id",
						addMode ? new Integer("0") : db.getInteger("cycle_id"),
						cycles));

		debug("3");

		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));

		debug("4");

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("owner_uid"), sm.getUserHT()));

		debug("5");

		ht.put("testcase_id",
				new WebFieldSelect("testcase_id", addMode ? new Integer("0")
						: db.getInteger("testcase_id"), db.getLookupTable(
						"ttestcase", "testcase_id", "title_nm")));

		/*
		 * Strings
		 */

		debug("6");

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		debug("7");

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		debug("8");

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 128));

		debug("9");

		ht.put("chg_pkg_nm", new WebFieldString("chg_pkg_nm", (addMode ? ""
				: db.getText("chg_pkg_nm")), 64, 64));

		/*
		 * Dates
		 */

		debug("10");

		ht.put("closed_date", new WebFieldDate("closed_date", (addMode ? ""
				: db.getText("closed_date"))));

		/*
		 * Codes
		 */

		debug("11");
		ht.put("config_chg_reqr_cd", new WebFieldSelect("config_chg_reqr_cd",
				addMode ? "" : db.getText("config_chg_reqr_cd"), sm
						.getCodes("YESNO")));

		debug("12");
		ht.put("code_chg_reqr_cd", new WebFieldSelect("code_chg_reqr_cd",
				addMode ? "" : db.getText("code_chg_reqr_cd"), sm
						.getCodes("YESNO")));

		debug("13");

		ht.put("result_cd", new WebFieldSelect("result_cd", addMode ? "" : db
				.getText("result_cd"), sm.getCodes("RESULT")));

		debug("14");

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		debug("5");

		ht.put("env1_yn_cd", new WebFieldSelect("env1_yn_cd", addMode ? "" : db
				.getText("env1_yn_cd"), sm.getCodes("YESNO")));

		debug("16");

		ht.put("env2_yn_cd", new WebFieldSelect("env2_yn_cd", addMode ? "" : db
				.getText("env2_yn_cd"), sm.getCodes("YESNO")));

		debug("17");

		ht.put("env3_yn_cd", new WebFieldSelect("env3_yn_cd", addMode ? "" : db
				.getText("env3_yn_cd"), sm.getCodes("YESNO")));
		ht.put("env4_yn_cd", new WebFieldSelect("env4_yn_cd", addMode ? "" : db
				.getText("env4_yn_cd"), sm.getCodes("YESNO")));
		ht.put("env5_yn_cd", new WebFieldSelect("env5_yn_cd", addMode ? "" : db
				.getText("env5_yn_cd"), sm.getCodes("YESNO")));
		ht.put("env6_yn_cd", new WebFieldSelect("env6_yn_cd", addMode ? "" : db
				.getText("env6_yn_cd"), sm.getCodes("YESNO")));

		/*
		 * Blobs
		 */

		debug("30");

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				addMode ? "" : db.getText("resolution_blob"), 3, 80));

		ht.put("root_cause_blob", new WebFieldText("root_cause_blob",
				addMode ? "" : db.getText("root_cause_blob"), 3, 80));

		ht.put("execution_blob", new WebFieldText("execution_blob",
				addMode ? "" : db.getText("execution_blob"), 3, 80));

		ht.put("setup_blob", new WebFieldText("setup_blob", addMode ? "" : db
				.getText("setup_blob"), 3, 80));

		ht.put("validation_blob", new WebFieldText("validation_blob",
				addMode ? "" : db.getText("validation_blob"), 3, 80));

		ht.put("regression_blob", new WebFieldText("regression_blob",
				addMode ? "" : db.getText("regression_blob"), 3, 80));

		ht.put("fix_blob", new WebFieldText("fix_blob", addMode ? "" : db
				.getText("fix_blob"), 3, 80));

		ht.put("problem_blob", new WebFieldText("problem_blob", addMode ? ""
				: db.getText("problem_blob"), 3, 80));

		/*
		 * Ancillary fields
		 */

		debug("40");

		ht.put("anc_list_tx", new WebFieldString("anc_list_tx", (addMode ? ""
				: db.getText("anc_list_tx")), 64, 128));

		ht.put("anc_contact_tx", new WebFieldString("anc_contact_tx",
				(addMode ? "" : db.getText("anc_contact_tx")), 64, 128));

		ht.put("anc_setup_blob", new WebFieldText("anc_setup_blob",
				addMode ? "" : db.getText("anc_setup_blob"), 3, 80));

		ht.put("anc_steps_blob", new WebFieldText("anc_steps_blob",
				addMode ? "" : db.getText("anc_steps_blob"), 3, 80));

		ht.put("anc_valid_blob", new WebFieldText("anc_valid_blob",
				addMode ? "" : db.getText("anc_valid_blob"), 3, 80));

		/*
		 * Return
		 */
		return ht;

	}
}
