/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Enumeration;
import java.util.Hashtable;

import services.ServicesException;
import forms.*;

/**
 * 
 * 2/15 added mySql 3/13 as 'target' to list query 3/23 fix list query
 * 10/20/10 - add clearTable("users") when adding a new user
 */

public class PreferencePlugin extends AbsDivisionPlugin {

	/*
	 * Update the user's preferences on tUser table .. can only update, and only
	 * update user's own table, no others
	 * 
	 * 
	 * 7/21/09 perDavid Ortiz only admins and ip_leaders can change their
	 * default_product_cd
	 */

	// *******************
	// CONSTRUCTORS
	// *******************
	public PreferencePlugin() throws services.ServicesException {
		setDefaults();
	}

	private void setDefaults() {
		this.setTableName("tuser");
		this.setKeyName("user_id");
		this.setTargetTitle("Preference");
		this.setShowAuditSubmitApprove(false);
		this.setContextSwitchOk(false);
		this.setDeleteOk(false);
		this.setAddOk(false);
		this.setCopyOk(false);
		this.setListOk(false);
		this.setNextOk(false);

		this
				.setListHeaders(new String[] { "Handle", "First Name",
						"Last Name" });
		this.setMoreListColumns(new String[] { "handle", "first_name",
				"last_name" });
	}

	public String getListQuery() {
		return " SELECT * from tuser where user_id = "
				+ sm.getUserId().toString() + " WHERE 1 = 1 ";
	}

	// *******************
	// WEB PAGE
	// *******************

	public void afterAdd(Integer newKey) {
		sm.clearTable("users");
		sm.cacheUserHT();
	}

	public void afterUpdate(Integer rowKey) throws services.ServicesException {
		System.out.println("prefere ce.. setting session style to "
				+ sm.Parm("style_nm"));
		sm.setStyle(sm.Parm("style_nm"));

		sm.setUserProductCode(sm.Parm("default_prod_cd"));
		

	}

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	public Hashtable<String, WebField> getWebFields(String parmMode) {

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * get the table of Forms
		 */

		String qry = "select c.order_by, c.code_value, code_desc "
			+ " from tcodes c "
			+ " where c.code_desc2 = '" + db.getText("user_suite_cd") + "' and code_type_id = 121 order by order_by ";
		
	
		Hashtable products = new Hashtable();

		try {
			products = db.getLookupTable(qry);
		} catch (ServicesException e) {
		}
		
			
		String query = new String(
				"select form_nm, plugin_nm, form_nm from tform ");
		Hashtable forms = sm.getTable("texposure", query);
		
		

		ht.put("first_name", new WebFieldString("first_name", db
				.getText("first_name"), 32, 32));

		ht.put("last_name", new WebFieldString("last_name", db
				.getText("last_name"), 32, 32));

		ht.put("handle", new WebFieldDisplay("handle", db.getText("handle")));

		ht.put("email_address", new WebFieldString("email_address", db
				.getText("email_address"), 32, 32));

		/*
		 * Selectors
		 */

		ht.put("user_suite_cd", new WebFieldSelect("user_suite_cd", db
				.getText("user_suite_cd"), sm.getCodes("SUITES"), true, false));

		ht.put("default_project_id", new WebFieldSelect("default_project_id",
				db.getInteger("default_project_id"), sm.getProjectFilter(),
				true));

		ht.put("style_nm", new WebFieldSelect("style_nm", db
				.getText("style_nm"), sm.getCodes("STYLES")));

		// 6/1 users can change their product code
		
		ht.put("default_prod_cd", new WebFieldSelect("default_prod_cd", db
				.getText("default_prod_cd"), products, true,
				true));

		ht.put("home_page_nm", new WebFieldSelect("home_page_nm", db
				.getText("home_page_nm"), forms));

		/*
		 * Return
		 */

		return ht;

	}

	


}
