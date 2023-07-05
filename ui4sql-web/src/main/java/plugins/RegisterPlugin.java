/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import db.DbFieldInteger;
import db.DbField;
import java.sql.ResultSet;
import forms.*;

/**
 * 
 * 5/16/06 Register... allows user to self-register thier userId 5/17/06 take
 * off 'default-project_id, there's nothing available anyway ... hack: change
 * database to default it .. to do: put default_project_id on the division table
 * 
 * 9/20/06 - change handle to 7 bytes, add ht parm string
 * 
 * 8/27/07 - change default application_id to 1 9/16/07 - switch NewRowKey to
 * rowId
 * 
 * 4/18/08 - after register, switch user context to their new user id via sm login function
 * 
 * 9/21/09 - default project is 27 HC General and  def. appl is 95 Epic general
 * 
 * 10/20/10 - add clearTable("users") when adding a new user
 * 
 */

public class RegisterPlugin extends AbsDivisionPlugin {

	// the AddedDate and UpdatedDate are defaulted by the db

	// *******************
	// CONSTRUCTORS
	// *******************

	private String user_message = "User added. Please logout, and sign in with your new user id.";

	
	

	public RegisterPlugin() throws services.ServicesException {
		setDefaults();
	}

	public String getMenuName(String action) {
		return "empty";
	}

	private void setDefaults() {
		this.setIsAdminFunction (true);
		this.setTableName("tuser");
		this.setKeyName("user_id");
		this.setTargetTitle("Register");
		this.setShowAuditSubmitApprove (false);

		this.setListHeaders (new String[] { "Login Id.", "First Name",
				"Last Name", "Active" });
		
		this.setMoreListColumns (new String[] { "handle", "first_name",
				"last_name", "active_flag" });

		
		this.setDeleteOk(false);
		this.setCancelOk(false);
		this.setAddOk(false);
		this.setListOk(false);
		this.setNextOk(false);
		this.setCopyOk(false);
		
		
		
		/*
		 * 11/6 so the session manager can get default project and process
		 * (menu) info
		 */
		// this.setMoreSelectColumns (new String [] {"t"};
		// this.moreSelectJoins = new String [] { " left join tproject on
		// tuser.default_project_id = tproject.project_id ",
		// " left join tprocess on tproject.process_id = tprocess.process_id "};
	}

	// *******************
	// WEB PAGE
	// *******************





	public void afterAdd(Integer newKey) {
		
		sm.clearTable("users");
		sm.cacheUserHT();
		
		sm.login(db.getText("handle"), db.getText("password_nm"));
		
		
	}

	public boolean beforeAdd(Hashtable<String, DbField> ht) {

		/*
		 * check for duplicate
		 */

		boolean rc = true;

		String sql = "SELECT * from tuser where handle ='" + sm.Parm("handle")
				+ "'";

		debug("checkingn handle");
		try {

			ResultSet rs = db.getRS(sql);
			if (rs.next()) {
				rc = false;
				this.setEditOk(false);
				
				// java 1.5 this.rowId = rs.getInt("user_id");
				this.rowId = new Integer(rs.getInt("user_id")); // java 1.5
				user_message = "This NUID already exists.";

			} else {
				debug("no match on handle");
			}

		} catch (services.ServicesException e) {
			debug("sql error on handle check " + e.toString());

		} catch (java.sql.SQLException se) {
			debug("sql error on handle check " + se.toString());
		}

		
		ht.put("default_project_id", new DbFieldInteger("default_project_id",
				new Integer("27")));  // HC General

		ht.put("default_appl_id", new DbFieldInteger("default_appl_id",
				new Integer("94")));  // epic general

		return rc;

	}

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	public Hashtable<String, WebField> getWebFields(String parmMode) {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();
		/*
		 * Strings
		 */

		/*
		 * Form list
		 */
		String query = new String(
				"select form_nm, plugin_nm, form_nm from tform ");
		Hashtable forms = sm.getTable("texposure", query);

		if (addMode) {

			ht.put("msg", new WebFieldDisplay("msg",
					"Please enter your user information, and press 'Save'."));
		} else {
			ht.put("msg", new WebFieldDisplay("msg", user_message));
		}

		ht.put("first_name", new WebFieldString("first_name", addMode ? "" : db
				.getText("first_name"), 32, 32));

		ht.put("last_name", new WebFieldString("last_name", addMode ? "" : db
				.getText("last_name"), 32, 32));

		ht.put("handle", new WebFieldString("handle", addMode ? "" : db
				.getText("handle"), 7, 7));

		ht.put("email_address", new WebFieldString("email_address",
				addMode ? "" : db.getText("email_address"), 32, 32));

		ht.put("password_nm", new WebFieldPassword("password_nm", addMode ? ""
				: db.getText("password_nm")));

		/*
		 * Codes
		 */

		ht.put("user_suite_cd", new WebFieldSelect("user_suite_cd", addMode ? "" : db
				.getText("user_suite_cd"), sm.getCodes("SUITES"),true));

		
		ht.put("default_prod_cd", new WebFieldSelect("default_prod_cd", addMode ? "" : db
				.getText("default_prod_cd"), sm.getCodes("PRODUCTS"),true));

		
		ht.put("home_page_nm", new WebFieldSelect("home_page_nm",
				addMode ? sm.getDefaultHomePage() : db.getText("home_page_nm"), forms, true));

		// ht.put("default_project_id", new WebFieldSelect("default_project_id",
		// / addMode ? new Integer("0") : db
		// .getInteger("default_project_id"), sm
		// .getProjectFilter(), true));

		ht.put("style_nm", new WebFieldSelect("style_nm", addMode ? "" : db
				.getText("style_nm"), sm.getCodes("STYLES")));

		/*
		 * Return
		 */

		return ht;

	}

}
