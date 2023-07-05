/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import db.DbInterface;
import forms.*;

public class CodeTypePlugin extends Plugin {

	/*
	 * Change Log :
	 * 
	 * 8/25/06 - SQLSERVER : remove 'select', plugin will put SELECT TOP 1 if
	 * sql-server 8/28/06 - SQLSERVER : add all select columns to GROUP BY
	 * clause
	 * 
	 * 12/3/08 - only root user can change table locked flag
	 * 
	 * 10/25/10 - Paulsen 
	 * 1. Remove afterAdd and afterUpdate to clear cache, Only
	 * need to do that in the actual CodesPlugin Code type changes do not affect
	 * the display values.
	 * 2. Replace get/setCodeTypeId() with get/setParentId
	 * 3. Using views for list and show pages
	 * 
	 */

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public CodeTypePlugin() throws services.ServicesException {
		super();
		this.setDataType("Code");
		this.setTableName("tcode_types");
		this.setKeyName("code_type_id");
		this.setTargetTitle("Code Types");
		this.setListOrder("vcode_types_list.title_nm"); // * change 6/13/05" +
		
		
		/*
		 * this plugin inherits from root plugin class, and the view logic in getListQuery and getSelectQuery
		 * does not have all the "view' logic in it as the abstract classes do, so need to override both the Query and the View name
		 */
		this.setSelectQuery(" * from vcode_types");
		this.setSelectViewName("vcode_types");
		
		this.setListQuery(" select * from vcode_types_list where 1 = 1");
		this.setListViewName("vcode_types_list");

		this.setShowAuditSubmitApprove(false);
		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Codes");
		this.setDetailTargetLabel("Codes");
		this.setIsRootTable(true);
		this.setIsAdminFunction(true);

		this.setListHeaders(new String[] { "Title", "Tag", "Locked", "Code #",
				"Count" });

	}

	public String getSubTitle() {
		// for show mode, we already have a project name

		// we're alredy at the root, so no where else to go but select something
		if (sm.Parm("Action").equalsIgnoreCase("list")) {
			return "Select Code Type";
		}

		if (this.getHasRow()) {
			sm.setParentId(db.getInteger("code_type_id"),
					db.getText("title_nm"));
			return "Code: " + db.getText(("title_nm"));
		} else {
			return "Code: " + sm.getParentName();
		}
	
	}

	/***************************************************************************
	 * 
	 * List Page Abstracts
	 * 
	 */

		
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
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public boolean afterGet() {

		boolean ok_to_edit;

		if (db.getText("locked_flag").equalsIgnoreCase("Y")) {
			ok_to_edit = sm.userIsRoot();
		} else {
			ok_to_edit = sm.userIsAdministrator();
		}

		this.setEditOk(ok_to_edit);
		this.setDeleteOk(ok_to_edit);
		this.setAddOk(ok_to_edit);
		this.setCopyOk(ok_to_edit);

		return true;
	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable();

		ht.put("isRoot",
				new WebFieldHidden("isRoot", (addMode ? ""
						: sm.userIsRoot() ? "Y" : "N")));

		// used by the 'codes' edit/list pages to know if this table is locked
		// or not
		if (parmMode.equalsIgnoreCase("Show")) {
			sm.setTableLocked(db.getText("locked_flag"));
			sm.setScratch(db.getText("code_type"));
		}

		if (sm.userIsRoot()) {
			ht.put("locked_flag",
					new WebFieldSelect("locked_flag", addMode ? "N" : db
							.getText("locked_flag"), sm.getCodes("YESNO")));
		} else {
			ht.put("locked_flag", new WebFieldDisplay("locked_flag",
					addMode ? "No" : db.getText("locked_flag")
							.equalsIgnoreCase("Y") ? "Yes" : "No"));
		}

		ht.put("code_type",
				new WebFieldString("code_type", (addMode ? "" : db
						.getText("code_type")), 16, 16));

		ht.put("title_nm",
				new WebFieldString("title_nm", (addMode ? "" : db
						.getText("title_nm")), 64, 64));

		ht.put("desc_blob",
				new WebFieldText("desc_blob", (addMode ? "" : db
						.getText("desc_blob")), 4, 80));

		return ht;

	}
}
