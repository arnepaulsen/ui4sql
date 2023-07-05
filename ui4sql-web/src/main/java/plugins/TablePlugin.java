/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.*;

import db.DbFieldInteger;
import forms.*;
import router.SessionMgr;

/**
 * 
 * Database Table Plugin
 * 
 * child of Database parent of Element
 * 
 */
public class TablePlugin extends AbsApplicationPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public TablePlugin() throws services.ServicesException {
		super();
		this.setTableName("ttable");
		this.setKeyName("table_id");
		this.setListHeaders(new String[] { "Table", "Database",
				"Version", "Status" });
		this.setTargetTitle("Table");

		this.setIsDetailForm(true);
		this.setParentTarget("Database");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Element");
		this.setDetailTargetLabel("Elements");

		this.setMoreListColumns(new String[] {
				"ttable.reference_nm as thereferrence_nm",
				"tdatabase.title_nm as database_nm",
				"ttable.version_id", 
				"s.code_desc as status_desc" });

		this
				.setMoreSelectJoins(new String[] { " join tdatabase on ttable.database_id = tdatabase.database_id " });

		this
				.setMoreSelectColumns(new String[] { "tdatabase.title_nm as db_nm " });

	}
	
	// "tdatabase.title_nm as database_nm",
	

	// need to set moreListJoins in the init() method because we need the sm
	// .. which is not available in the constructor
	
	
	// called on the Show page, when returning to the list page
	public Integer getParentKey() {

		return db.getInteger("database_id");
	}
	

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		this.setMoreListJoins(new  String[] {

				" join tdatabase on ttable.database_id = tdatabase.database_id "
						+ ((sm.Parm("FilterDatabase").length() == 0)
								|| (sm.Parm("FilterDatabase")
										.equalsIgnoreCase("0")) ? ""
								: " and tdatabase.database_id ="
										+ sm.Parm("FilterDatabase")),
				" left join tcodes s on ttable.status_cd = s.code_value and s.code_type_id  = 60 " });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	
	public boolean beforeAdd(Hashtable ht) {

		ht.put("datbase_id", new DbFieldInteger("database_id", new Integer(sm
				.Parm("FilterDatabase"))));
		return true;

	}

	

	public boolean listColumnHasSelector(int columnNumber) {
		// column 0 = message id, 4 = status
		if (columnNumber ==  3 || columnNumber == 3)
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {
		case 10: {

			String sQuery = "Select title_nm, database_id, title_nm as the_title_nm from tdatabase where application_id = "
					+ sm.getApplicationId().toString();

			return new WebFieldSelect("FilterDatabase", sm.Parm(
					"FilterDatabase").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterDatabase")), db, sQuery,
					"- All Databases -");
		}

		default: {

		}
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "0" : sm
					.Parm("FilterStatus")), sm.getCodes("LIVESTAT"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		}

	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		if ((sm.Parm("FilterStatus").length() > 0)
				&& (!sm.Parm("FilterStatus").equalsIgnoreCase("0"))) {
			sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus") + "'");
		}
		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		sm.setStructureType("Table"); // leave a cookie so the Step manager
		// knows what kind of a step to take

		// show the database name, if add mode, have to look it up using the
		// FilterDatabase parm.
		WebFieldDisplay wfDatabaseName;
		if (addMode) {
	
			String query = " select title_nm from tdatabase where database_id = "
					+ sm.Parm("FilterDatabase");

			String answer = db.getColumn(query);
			
			debug ( " query is : " + query + " database is : " + answer);
			
			wfDatabaseName = new WebFieldDisplay("db_nm", answer);

		} else {
			
			wfDatabaseName = new WebFieldDisplay("db_nm", db
					.getText("db_nm"));
		}



		/*
		 * Codes
		 */
		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));

		/*
		 * Strings
		 */

		WebFieldString wfVersionNo = new WebFieldString("version_id",
				(addMode ? "0" : db.getText("version_id")), 4, 4);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldString wfSize = new WebFieldString("size_tx", (addMode ? ""
				: db.getText("size_tx")), 64, 100);

		/*
		 * Blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 70);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 70);

		WebFieldText wfVersion = new WebFieldText("version_blob", addMode ? ""
				: db.getText("version_blob"), 3, 70);

		WebFieldText wfUsage = new WebFieldText("usage_blob", addMode ? "" : db
				.getText("usage_blob"), 3, 70);

		/*
		 * Return
		 */

		WebField[] wfs = { wfDesc, wfTitle, wfStatus, wfRefr, wfNotes,
				wfVersion, wfVersionNo, wfDatabaseName, wfUsage,
				wfSize };

		return webFieldsToHT(wfs);

	}

}
