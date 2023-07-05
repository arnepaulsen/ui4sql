/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.*;
import forms.*;

/**
 * 
 *   2/15 added mySql
 * 	3/11 add join on project Permissions
 * */

/**
 * 
 * 3/11/07 - Change to 'Division' type so all can see across applications
 * 
 */
public class DatabasePlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public DatabasePlugin() throws services.ServicesException {
		super();
		this.setTableName("tdatabase");
		this.setKeyName("database_id");
		this.setListHeaders(new String[] { "Reference", "Title", "Product",
				"Status" });
		this.setTargetTitle("Database");
		
		this.setListOrder ("tdatabase.title_nm");

		this.setHasDetailForm (true); // detail is the Codes form
		this.setDetailTarget ("Table"); // figure out in show mode!
		this.setDetailTargetLabel ("Tables");

		this.setMoreListColumns(new  String[] { "tdatabase.reference_nm", "tdatabase.title_nm",
				"vendor.code_desc as vendor_desc",
				"status.code_desc as status_desc" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes vendor on tdatabase.type_cd = vendor.code_value and vendor.code_type_id  = 59 ",
				" left join tcodes status on tdatabase.status_cd = status.code_value and status.code_type_id  = 60 " });

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		sm.setStructureType("Database"); // leave a cookie so the Step
		// manager
		// knows what kind of a step to take

		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("database_id"), db
					.getText("tdatabase.title_nm"));
		}

		WebFieldCheckbox wfConfirm = new WebFieldCheckbox("confirmed_flag",
				addMode ? "N" : db.getText("confirmed_flag"), "");

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("DATABASE"));

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);

		WebFieldText wfDesc = new WebFieldText("blob_description", addMode ? ""
				: db.getText("blob_description"), 3, 70);

		WebFieldText wfNotes = new WebFieldText("blob_notes", addMode ? "" : db
				.getText("blob_notes"), 3, 70);

		WebFieldSelect wfAdmin = new WebFieldSelect("administrator_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("administrator_uid"), sm.getUserHT());

		WebField[] wfs = { wfConfirm, wfDesc, wfTitle, wfType, wfAdmin,
				wfStatus, wfRefr, wfNotes };

		return webFieldsToHT(wfs);

	}

}
