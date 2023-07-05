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
 * Release Plugin
 * 
 */
/*******************************************************************************
 * Release Data Manager
 * 
 * Change Log:
 * 
 * 2/15 added mySql 5/8/05 Extentded from AbsApplicationPlugin
 * 
 * 
 ******************************************************************************/
public class StandardPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	static String[][] aLevels = { { "1", "2", "3" },
			{ "Manager", "SVP", "CTO" } };

	public StandardPlugin() throws services.ServicesException {
		super();

		this.setContextSwitchOk (false); // don't allow to change division here.

		this.setTableName("tstandard");
		this.setKeyName("standard_id");
		this.setTargetTitle("Standards ");

		this.setListHeaders( new String[] { "Reference", "Title", "Type",  "Effective Date" });

		this.setMoreListJoins(new  String[] { " left join tcodes levels on tstandard.type_cd = levels.code_value and code_type_id = 66 " });
		
		this.setMoreListColumns(new  String[] {  "tstandard.reference_nm", "tstandard.title_nm",
				"levels.code_desc as theType",
				"effective_dt" });

	}


	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		this.setUpdatesOk(sm.userIsAdministrator());
	}
	

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * text fields
		 */
		ht.put("reference_nm" , new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32));

		ht.put("title_nm" , new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64));

		/*
		 * dates
		 */
		ht.put("effective_dt" , new WebFieldDate("effective_dt",
				addMode ? "" : db.getText("effective_dt")));

		/*
		 * codes
		 */
		ht.put("status_cd" ,  new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		ht.put("type_cd" ,  new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("STANDARDTYPES")));

		ht.put("override_level_cd" ,  new WebFieldSelect("override_level_cd",
				addMode ? "" : db.getText("override_level_cd"), aLevels));

		/*
		 * blobs
		 */

		ht.put("desc_blob" ,  new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("notes_blob" ,  new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 5, 100));

		return ht;
		

	}

}
