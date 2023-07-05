/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import remedy.RemedyPeopleInfo;
import remedy.RemedyXMLParser;
import router.SessionMgr;
import forms.*;
import db.*;

/**
 * 
 * 2/15 added mySql 3/13 as 'target' to list query 3/23 fix list query 5/15/06 -
 * fix default integer new("0") on add model for default_project_id, also
 * "style-nm"
 * 
 * Change Log :
 * 
 * 9/18/06 add testor_flag 1/25/07 add type_cd and leader_flag 9/17/07 add
 * remedy look-up for user info! 2/22/08 add reviewer flag
 * 
 * 12/7/08 - add logic to view-only admin flag,
 * 
 * 3/14/09 - add sox role flag
 * 
 * 10/20/10 - add clearTable("users") when adding a new user
 * 
 * 11/1/10 - Add SR_Tracker security level
 * 
 * 11/9/10 - Force call to Remedy after new user added.
 * 				save handle in sm.scratch at display time,
 * 				so SQL call to fetch user handle from tuser.user_id is not necessary.   
 */

public class UserPlugin extends AbsDivisionPlugin {

	// the AddedDate and UpdatedDate are defaulted by the db

	// *******************
	// CONSTRUCTORS
	// *******************

	private String user_message = "Okay.";

	String[][] sr_levels = new String[][] { { "VW", "BP", "AC", "ED" },
			{ "View", "Bus.Partner", "AC", "Edit All" } };

	/*
	 * filters
	 */
	private BeanFieldSelect filterType = new BeanFieldSelect(3, "FilterType",
			"type_cd", "", "", "Type?", "USERTYPE");

	private BeanFieldSelect filterSuite = new BeanFieldSelect(4, "FilterSuite",
			"user_suite_cd", "", "", "Suite?", "SUITES");

	private BeanFieldSelect filterActive = new BeanFieldSelect(5,
			"FilterActive", "active_flag", "", "", "Active?", "YESNO");

	private BeanFieldSelect filterExecutive = new BeanFieldSelect(6,
			"FilterExecutive", "executive_flag", "", "", "Executive?", "YESNO");

	private BeanFieldSelect filterAdmin = new BeanFieldSelect(7, "FilterAdmin",
			"administrator_flag", "", "", "Administrator?", "YESNO");

	public UserPlugin() throws services.ServicesException {
		setDefaults();
	}

	public UserPlugin(DbInterface parmDb) throws services.ServicesException {
		this.db = parmDb;
		setDefaults();
	}

	/*
	 * 8/21 pass the Session to the Init
	 */
	public UserPlugin(DbInterface parmDb, String parmHandle,
			router.SessionMgr parmSm) throws services.ServicesException {
		this.db = parmDb;
		this.sm = parmSm;
		setDefaults();
		setRowWhere(" WHERE handle = '" + parmHandle + "'");
	}

	public UserPlugin(DbInterface parmDb, String parmHandle)
			throws services.ServicesException {
		this.db = parmDb;
		setDefaults();
		setRowWhere(" WHERE handle = '" + parmHandle + "'");
	}

	public UserPlugin(DbInterface parmDb, Integer parmUserId)
			throws services.ServicesException {
		this.db = parmDb;
		setDefaults();
		setRowWhere(" WHERE user_id = " + parmUserId.toString());
	}

	private void setDefaults() {
		this.setIsAdminFunction(true);
		this.setTableName("tuser");
		this.setKeyName("user_id");
		this.setTargetTitle("User");
		this.setShowAuditSubmitApprove(false);
		this.setListOrder("last_name");
		this.setListViewName("vuser_list");
		this.setSelectViewName("vuser");

		this.setListFilters(new BeanWebField[] { filterType, filterSuite,
				filterActive, filterExecutive, filterAdmin });

		this.setUpdatesLevel("administrator");
		this.setRemedyOk(true);

		this.setDeleteOk(false);

		this.setListHeaders(new String[] { "Login Id.", "Last Name",
				"First Name", "Type", "Suite", "Active", "Executive", "Admin",
				"Phone" });

		/*
		 * 11/6 so the session manager can get default project and process
		 * (menu) info
		 */
		// this.setMoreSelectColumns (new String [] {"t"};
		// this.moreSelectJoins = new String [] { " left join tproject on
		// tuser.default_project_id = tproject.project_id ",
		// " left join tprocess on tproject.process_id = tprocess.process_id "};
	}

	public void afterAdd(Integer newKey) {

		sm.clearTable("users");
		sm.cacheUserHT();

		// 11/10/20 apaulsen - force Remedy sync

		getRemedy(sm.Parm("handle"), this.rowId.toString());

	}

	/**
	 * *************************************************************************
	 * * * HTML Field Mapping *
	 * *************************************************************************
	 */

	public boolean beforeAdd(Hashtable<String, DbField> ht) {

		/*
		 * check for duplicate
		 */

		boolean rc = true;

		String sql = "SELECT * from tuser where handle ='" + sm.Parm("handle")
				+ "'";

		try {

			ResultSet rs = db.getRS(sql);
			if (rs.next()) {
				rc = false;
				user_message = "This NUID already exists.";
				this.rowId = rs.getInt("user_id"); // java 1.5
				// this.rowId = new Integer(rs.getInt("user_id")); // java 1.4
			} else {

			}

		} catch (services.ServicesException e) {
			debug("UserPlugin: SQL error on handle check " + e.toString());

		} catch (java.sql.SQLException se) {
			debug("UserPlugin: SQL error on handle check " + se.toString());
		}

		return rc;
	}

	public Hashtable<String, WebField> getWebFields(String parmMode) {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * get the table of Forms
		 */

		String query = new String(
				"select form_nm, plugin_nm, form_nm from tform ");
		Hashtable forms = sm.getTable("texposure", query);

		// Put user message
		if (addMode) {

			ht.put("msg", new WebFieldDisplay("msg", "Ready."));
		} else {
			ht.put("msg", new WebFieldDisplay("msg", user_message));
		}

		/*
		 * 
		 * 
		 * Strings
		 */

		// save handle in scratch for later use in getRemedy()
		sm.setScratch(addMode ? sm.Parm("handle") : db.getText("handle"));

		ht.put("first_name",
				new WebFieldString("first_name", addMode ? "" : db
						.getText("first_name"), 32, 32));

		ht.put("last_name",
				new WebFieldString("last_name", addMode ? "" : db
						.getText("last_name"), 32, 32));

		ht.put("handle",
				new WebFieldString("handle", addMode ? "" : db
						.getText("handle"), 32, 32));

		ht.put("floor_tx",
				new WebFieldString("floor_tx", addMode ? "" : db
						.getText("floor_tx"), 2, 2));

		ht.put("mi_nm",
				new WebFieldString("mi_nm", addMode ? "" : db.getText("mi_nm"),
						1, 1));

		ht.put("tie_line_tx", new WebFieldString("tie_line_tx", addMode ? ""
				: db.getText("tie_line_tx"), 3, 3));

		ht.put("email_address", new WebFieldString("email_address",
				addMode ? "" : db.getText("email_address"), 32, 32));

		ht.put("phone_tx",
				new WebFieldString("phone_tx", addMode ? "" : db
						.getText("phone_tx"), 32, 32));

		ht.put("password_nm", new WebFieldPassword("password_nm", addMode ? ""
				: db.getText("password_nm")));

		/*
		 * Codes
		 */

		ht.put("user_suite_cd",
				new WebFieldSelect("user_suite_cd",
						db.getText("user_suite_cd"), sm.getCodes("SUITES")));

		ht.put("default_prod_cd",
				new WebFieldSelect("default_prod_cd", db
						.getText("default_prod_cd"), sm.getCodes("PRODUCTS"),
						true));

		ht.put("home_page_nm", new WebFieldSelect("home_page_nm",
				addMode ? "Work" : db.getText("home_page_nm"), forms));

		ht.put("default_project_id",
				new WebFieldSelect("default_project_id", addMode ? new Integer(
						"0") : db.getInteger("default_project_id"), sm
						.getProjectFilter(), true));

		ht.put("default_appl_id", new WebFieldSelect("default_appl_id",
				addMode ? new Integer("0") : db.getInteger("default_appl_id"),
				sm.getApplicationFilter(), true));

		ht.put("type_cd",
				new WebFieldSelect("type_cd", addMode ? "" : db
						.getText("type_cd"), sm.getCodes("USERTYPE")));

		ht.put("style_nm",
				new WebFieldSelect("style_nm", addMode ? "" : db
						.getText("style_nm"), sm.getCodes("STYLES")));

		ht.put("pat_sfty_cd", new WebFieldSelect("pat_sfty_cd", addMode ? "N"
				: db.getText("pat_sfty_cd"), sm.getCodes("SECURE")));

		ht.put("sr_tracker_level_cd", new WebFieldSelect("sr_tracker_level_cd",
				addMode ? "N" : db.getText("sr_tracker_level_cd"), sr_levels));

		/*
		 * only root, Neezar and Kathy Gavigan can update ip-issue-triage codes
		 */
		if (sm.userIsAdministrator()) {

			ht.put("ip_issue_triage_cd",
					new WebFieldSelect("ip_issue_triage_cd", addMode ? "N" : db
							.getText("ip_issue_triage_cd"), sm
							.getCodes("SECURE")));

		} else {

			ht.put("ip_issue_triage_cd",
					new WebFieldDisplay("ip_issue_triage_cd", addMode ? "" : db
							.getText("ip_security_level")));

		}
		/*
		 * Flags
		 */

		if (sm.userIsRoot() || sm.userCanGrant()) {
			ht.put("superUser", new WebFieldHidden("superUser", "Y"));
			ht.put("administrator_flag",
					new WebFieldCheckbox("administrator_flag", addMode ? "N"
							: db.getText("administrator_flag"), ""));

			ht.put("grantable_flag", new WebFieldCheckbox("grantable_flag",
					addMode ? "N" : db.getText("grantable_flag"), ""));

		} else {
			ht.put("superUser", new WebFieldHidden("superUser", "N"));
			ht.put("administrator_flag", new WebFieldDisplay(
					"administrator_flag", db.getText("administrator_flag")
							.equalsIgnoreCase("Y") ? "Yes" : "No"));

			ht.put("grantable_flag", new WebFieldDisplay("grantable_flag", db
					.getText("grantable_flag").equalsIgnoreCase("Y") ? "Yes"
					: "No"));
		}

		ht.put("sox_role_flag", new WebFieldCheckbox("sox_role_flag",
				addMode ? "N" : db.getText("sox_role_flag"), ""));

		ht.put("executive_flag", new WebFieldCheckbox("executive_flag",
				addMode ? "N" : db.getText("executive_flag"), ""));

		ht.put("chg_aprv_flag", new WebFieldCheckbox("chg_aprv_flag",
				addMode ? "N" : db.getText("chg_aprv_flag"), ""));

		ht.put("testor_flag", new WebFieldCheckbox("testor_flag", addMode ? "N"
				: db.getText("testor_flag"), ""));

		ht.put("leader_flag", new WebFieldCheckbox("leader_flag", addMode ? "N"
				: db.getText("leader_flag"), ""));

		ht.put("ip_leader_flag", new WebFieldCheckbox("ip_leader_flag",
				addMode ? "N" : db.getText("ip_leader_flag"), ""));

		ht.put("reviewer_flag", new WebFieldCheckbox("reviewer_flag",
				addMode ? "N" : db.getText("reviewer_flag"), ""));

		ht.put("active_flag", new WebFieldCheckbox("active_flag", addMode ? "Y"
				: db.getText("active_flag"), ""));

		/*
		 * Return
		 */

		return ht;

	}

	/*
	 * Update tuser name, phone etc from Remedy
	 * 
	 * normal Remedy call from User display page, which has NUID in sm.Scratch pad, and RowKey in Hidden Variable
	 */
	public String getRemedy() {

		// sm.Scratch has the handle from prior display, 
		// .. and User HTML has the tuser.user_id key in hidden variable "RowKey"
		
		return getRemedy(sm.getScratch(), sm.Parm("RowKey"));

	}

	/*
	 * 11/10/20 make a getRemedy call for a given nuid
	 */

	public String getRemedy(String nuid, String rowKey) {

		RemedyPeopleInfo people = new RemedyPeopleInfo(sm.getRemedyUserid(),
				sm.getRemedyPassword(), sm.getRemedyURL());

		// get rfc info in xml form
		String xml = people.SearchByNUID(nuid);

		if (people.getSuccess() == false) {

			user_message = people.getErrorText(xml);
			return user_message;
		}

		RemedyXMLParser parser = new RemedyXMLParser(xml,
				"SearchByNUIDResponse");

		if (parser.getElementCount() == 0) {
			return "NUID not found.";
		}
		String phone = parser.parseTextValue("phone");
		String email = parser.parseTextValue("email");
		String floor = parser.parseTextValue("floorNumber");
		String first_name = parser.parseTextValue("firstName");
		String last_name = parser.parseTextValue("lastName");
		String mi = parser.parseTextValue("mi");
		String tieLine = parser.parseTextValue("tieLine");

		Integer user_id = new Integer(rowKey);

		people.updateUser(sm.getDbInterface(), user_id, first_name, mi,
				last_name, phone, tieLine, email, floor);

		return "User updated with Remedy info.";
	}

	public boolean resetPassword(String newPassword) {

		String sql = "SET ROWCOUNT = 1 UPDATE tuser set password_nm = '"
				+ newPassword + "' WHERE tuser.user_id = " + sm.getUserId();

		try {
			db.runQuery(sql);
		} catch (services.ServicesException s) {

		}
		return true;
	}


}
