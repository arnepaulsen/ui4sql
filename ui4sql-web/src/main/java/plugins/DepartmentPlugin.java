/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;

/**
 * Division Area Plugin make a change
 */
public class DepartmentPlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public DepartmentPlugin() throws services.ServicesException {
		super();
		this.setTableName("tdepartment");
		this.setKeyName("dept_id");
		this.setTargetTitle("Department");
		this.setUpdatesLevel("administrator");

		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction(true);

		this.setListHeaders(new String[] { "Department", "Reference" });

		this.setMoreListColumns(new String[] { "dept_name", "dept_ref" });
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

		/*
		 * Ids
		 */
		ht.put("dept_mgr_id", new WebFieldSelect("dept_mgr_id",
				addMode ? new Integer("0") : db.getInteger("dept_mgr_id"), sm
						.getUserHT()));

		ht.put("division_id", new WebFieldSelect("division_id", addMode ? sm
				.getDivisionId() : (Integer) db.getObject("division_id"), sm
				.getDivisionFilter()));

		/*
		 * Strings
		 */
		ht.put("dept_name", new WebFieldString("dept_name", (addMode ? "" : db
				.getText("dept_name")), 64, 64));

		ht.put("dept_ref", new WebFieldString("dept_ref", (addMode ? "" : db
				.getText("dept_ref")), 32, 32));

		ht.put("dept_desc", new WebFieldText("dept_desc", (addMode ? "" : db
				.getText("dept_desc")), 4, 64));

		return ht;

	}

}
