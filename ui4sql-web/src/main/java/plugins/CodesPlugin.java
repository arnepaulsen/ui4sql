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

public class CodesPlugin extends Plugin {

	/*
	 * 
	 * Change Log:
	 * 
	 * 8/25/06 - SQLSERVER : remove 'select', plugin will put SELECT TOP 1 if
	 * sql-server
	 * 
	 * 12/02/08 - Enforce table "locked" flag is only updateable by root user
	 * 
	 * 
	 */
	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public CodesPlugin() throws services.ServicesException {
		super();
		this.setDataType("Code");
		this.setTableName("tcodes");
		this.setKeyName("code_id");
		this.setTargetTitle("Code Descriptions");
		
		/*
		 * this plugin inherits from root plugin class, and the logic in getListQuery and getSelectQuery
		 * does not have all the "view' logic in it as the abstract classes do, so need to override both the Query and the View name
		 */
		
		this.setSelectQuery(" * from vcodes_show");
		this.setSelectViewName("vcodes_show");
		
		this.setListQuery(" select * from vcodes_list where 1 = 1");
		this.setListViewName("vcodes_list");
		
		this.setListOrder("order_by"); // * change 6/13/05
		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction(true);
		this.setIsDetailForm(true);
		this.setParentTarget("CodeType");

		this.setListHeaders(new String[] { "Code", "Alt Desc", "Display As",
				"Order" });

	}

	public String getSubTitle() {
		// for show mode, we already have a project name
		if (this.getHasRow()) {
			//sm.setCodeTypeName(db.getText(("code_name")));
			return "Code: " + db.getText(("code_name"));
		} else if (sm.getParentName() != null) {
			return "Code: " + sm.getParentName();
		}
		// never set the project filter from the default, so get the name


		return ("" );
	}

	/***************************************************************************
	 * 
	 * List Page Abstracts
	 * 
	 */


	public String getListAnd() {
		return " and vcodes_list.code_type_id = " + sm.getParentId().toString();

	}


	/***************************************************************************
	 * 
	 * Filter Stuff
	 * 
	 **************************************************************************/

	// get a Hashtable of the projects the current user is permitted to
	public Hashtable getFilter() {
		return sm.getCodeTypeFilter();
	}
	

	/***************************************************************************
	 * 
	 * After Add/Update to remove old cache
	 * 
	 */

	public void afterUpdate(Integer rowKey) throws services.ServicesException {
		removeCodeCache();
		return;
	}

	public void afterAdd(Integer rowKey) throws services.ServicesException {

		removeCodeCache();
		return;
	}

	/*
	 * Remove the code from cache .. but first get code name using the row id.
	 */
	private void removeCodeCache() {

		try {
			// change to use generic parent id
			sm.removeServletCodes(sm.getScratch());

		} catch (Exception e) {
				debug("removeCodeCache: " + e.toString());
		}

		return;
	}

	public void init(SessionMgr parmSm) {

		super.init(parmSm);

		this.setUpdatesOk(editOk());

	}

	private boolean editOk() {

		if (sm.isTableLocked()) {
			return sm.userIsRoot();
		}
		return (sm.userIsAdministrator());

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		ht.put("active_flag", new WebFieldSelect("active_flag", addMode ? "Y"
				: db.getText("active_flag"), sm.getCodes("YESNO")));

		ht.put("code_value", new WebFieldString("code_value", (addMode ? ""
				: db.getText("code_value")), 8, 8));

		ht.put("code_desc", new WebFieldString("code_desc", (addMode ? "" : db
				.getText("code_desc")), 64, 64));

		ht.put("code_desc2", new WebFieldString("code_desc2", (addMode ? ""
				: db.getText("code_desc2")), 15, 15));

		ht.put("order_by", new WebFieldString("order_by", (addMode ? "0" : db
				.getText("order_by")), 3, 3));

		return ht;

	}

}
