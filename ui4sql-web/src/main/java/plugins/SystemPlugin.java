/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import db.DbField;
import forms.*;

/**
 * 
 * 5/5/08 New System plugin - used to set real-time options, not saved on
 * database - 1st use case is to block the 'add' buttton on rfc page
 * 
 * 2/19/09 Fix ht.put for ambBlock.. was using wrong param
 * 
 * 
 */

public class SystemPlugin extends Plugin {

	// the AddedDate and UpdatedDate are defaulted by the db

	// *******************
	// CONSTRUCTORS
	// *******************

	public String getSubTitle() {
		return "System Options";
	}

	// public void setSessionFilterKey(String s) {
	// return;
	// }

	// public void setSessionFilterKey() {
	// return;
	// }

	public SystemPlugin() throws services.ServicesException {
		super();
		this.setTableName("toptions");
		this.setKeyName("options_id");
		this.setListOrder("list_not_valid");
		this.setListHeaders(userColumnHeaders);
		this.setTargetTitle("Options");

		this.setShowAuditSubmitApprove(false);
		// this.setIsAdminFunction (true);
		this.setShowVCR(false);

		this.setAddOk(false);
		this.setDeleteOk(false);
		this.setNextOk(false);
		this.setCopyOk(false);
		this.setSubmitOk(false);
		this.setListOk(false);
		this.setEditLevel("administrator");

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

	private static String userListQuery = "No list ..  ";

	private static String userListColumns[] = { "No List" };

	private static String userColumnHeaders[] = { "No List" };

	/*
	 * the forms are cache'd by the sm, so must re-cache to show on form list
	 */
	public void afterAdd(Integer rowkey) throws services.ServicesException {

	}

	/*
	 * hook into the screen update button to save block flag into servlet
	 * context.
	 * 
	 */
	public void beforeUpdate(Hashtable ht) {

		sm.setRfcBlock(sm.Parm("blockRfcAdd").equalsIgnoreCase("Y") ? true
				: false);

		sm.setRipBlock(sm.Parm("blockRipAdd").equalsIgnoreCase("Y") ? true
				: false);

		sm.setAmbBlock(sm.Parm("blockAmbAdd").equalsIgnoreCase("Y") ? true
				: false);

		sm.setSystemMessage(sm.Parm("systemMessage"));

		return;
	}

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	private static String userSelectQuery = "'Options' as target, toptions.*, concat(a.first_name, '  ', a.last_name) as added_by, concat(u.first_name, '  ', u.last_name) as updated_by from toptions left join tuser as a  on toptions.added_uid = a.user_id left join tuser as u on toptions.updated_uid = u.user_id";

	public Hashtable getWebFields(String parmMode) {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		ht.put("blockRfcAdd", new WebFieldSelect("blockRfcAdd", addMode ? ""
				: (sm.getRfcBlock() ? "Y" : "N"), sm.getCodes("YESNO")));

		ht.put("blockRipAdd", new WebFieldSelect("blockRipAdd", addMode ? ""
				: (sm.getRipBlock() ? "Y" : "N"), sm.getCodes("YESNO")));

		ht.put("blockAmbAdd", new WebFieldSelect("blockAmbAdd", addMode ? ""
				: (sm.getAmbBlock() ? "Y" : "N"), sm.getCodes("YESNO")));

		ht.put("systemMessage", new WebFieldString("systemMessage", sm
				.getSystemMessage(), 128, 255));

		return ht;

	}
}
