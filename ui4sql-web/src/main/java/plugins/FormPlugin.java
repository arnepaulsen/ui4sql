/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;

import db.DbInterface;
import forms.*;

/**
 * 
 * 6/11 change to allow field update, show 'active' flag instead of 'custom
 * fields' flag 7/19 fix sub_title
 * 
 * 8/25/06 - SQLSERVER : remove 'select', plugin will put SELECT TOP 1 if
 * sql-server 10/12/06 add all columns to group_by statement
 * 
 */

public class FormPlugin extends Plugin {

	// the AddedDate and UpdatedDate are defaulted by the db

	// *******************
	// CONSTRUCTORS
	// *******************

	public String getSubTitle() {

		debug("Action " + sm.Parm("Action"));

		if (sm.Parm("Action").equalsIgnoreCase("list")) {
			return "Please select a form.";
		}
		if (this.getHasRow()) {
			sm.setParentId(db.getInteger("form_id"), db.getText("form_nm"));
			return "Form: " + db.getText(("form_nm"));
		} else if (sm.getParentName() != null) {
			return "Form: " + sm.getFormName();
		}

		// filter has never been set, so get it

		String formName;
		DbInterface db = sm.getDbInterface();
		try {
			formName = db
					.getColumn(" Select formName from tform where form_id = "
							+ sm.getParentId().toString());
			sm.setFormName(formName);
		} catch (services.ServicesException se) {
			formName = "Not Found";
		}

		return ("Code: " + formName);

	}

	// public void setSessionFilterKey(String s) {
	// return;
	// }

	// public void setSessionFilterKey() {
	// return;
	// }

	public FormPlugin() throws services.ServicesException {
		super();
		this.setDataType("Form");
		this.setTableName("tform");
		this.setKeyName("form_id");

		this.setListOrder("tform.form_nm");

		this.listGroupBy = "tform.form_id, tform.form_nm, tform.table_nm, tform.plugin_nm";

		
		this.setListHeaders(userColumnHeaders);
		this.setTargetTitle("Forms");
		this.setShowAuditSubmitApprove(false);
		this.setSubmitOk(false); // not needed ?
		this.setIsAdminFunction(true);

		this.setIsRootTable(true);
		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Field");
		this.setDetailTargetLabel("Fields");

		this.setUpdatesLevel("root");

	}

	public String getListQuery() {
		return userListQuery;
	}

	public String getSelectQuery() {
		return userSelectQuery;
	}

	// *******************
	// LIST PAGE
	// *******************
	// ... just a dummy.. never used because no filter on user!
	public Hashtable getFilter() {
		return sm.getFormFilter();
	}

	/*
	 * Somewhat of a hack here without sub-query.. it returns 1 if no records, ..
	 * so use a case statement to convert 1s to 0s... ... chances are they won't
	 * have a form with just one custom field.
	 * 
	 */
	private static String userListQuery = "select DISTINCT 'Form' as target, tform.form_id, form_nm , "
			+ " table_nm, plugin_nm,"
			+ " case Count(*) WHEN 1 THEN 0 ELSE Count(*) END   as sub_count  "
			+ " FROM  tform "
			+ " left outer join tfield on tform.form_id = tfield.form_id  WHERE 1=1 ";

	// this way requires mySQL v4.1
	// private static String userListQuery_SubQuery = "select 'Form' as target,
	// form_id, form_nm , "
	// + " (select Count(*) from tfield where tfield.form_id = tform.form_id )
	// as sub_count "
	// + " from tform order by tform.form_name ";

	private static String userColumnHeaders[] = { "Form", "Table", "Plugin",
			"Custom<br>Fields" };

	/*
	 * the forms are cache'd by the sm, so must re-cache to show on form list
	 */
	public void afterAdd(Integer rowkey) throws services.ServicesException {
		sm.cacheFormFilter();
	}

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	private static String userSelectQuery = "'Form' as target, tform.* ,  concat(a.first_name, '  ', a.last_name) as added_by, concat(u.first_name, '  ', u.last_name) as updated_by "
			+ " from tform "
			+ " left join tuser as a  on tform.added_uid = a.user_id "
			+ " left join tuser as u on tform.updated_uid = u.user_id";

	public Hashtable getWebFields(String parmMode) {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		if (!addMode) {
			sm.setParentId(db.getInteger("form_id"), db.getText("form_nm"));
		}

		WebFieldString wfFormName = new WebFieldString("form_nm", addMode ? ""
				: db.getText("form_nm"), 64, 64);

		WebFieldString wfTable = new WebFieldString("table_nm", addMode ? ""
				: db.getText("table_nm"), 64, 64);

		WebFieldString wfPlugin = new WebFieldString("plugin_nm", addMode ? ""
				: db.getText("plugin_nm"), 64, 64);

		WebFieldString wfKey = new WebFieldString("key_nm", addMode ? "" : db
				.getText("key_nm"), 32, 32);

		WebFieldSelect wfType = new WebFieldSelect("data_type_cd", addMode ? ""
				: db.getText("data_type_cd"), sm.getCodes("CMMDATATYPE"));

		WebFieldCheckbox wfActive = new WebFieldCheckbox("active_flag",
				addMode ? "N" : db.getText("active_flag"), "");

		WebField[] wfs = { wfFormName, wfType, wfTable, wfPlugin, wfKey,
				wfActive };
		return webFieldsToHT(wfs);

	}
}
