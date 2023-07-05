/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;

import db.DbFieldInteger;
import forms.*;

/**
 * Record Plugin - child of File
 * 
 * Change Log:
 * 
 * 9/10/05 New - Cloned from SegmentPlug
 * 
 * ToDo: -
 * 
 * 
 */

public class RecordPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public RecordPlugin() throws services.ServicesException {
		super();
		this.setTableName("trecord");
		this.setKeyName("record_id");
		this.setTargetTitle("Record");

		this.setIsDetailForm(true);
		this.setParentTarget("File");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Element");
		this.setDetailTargetLabel("Elements");

		this.setListHeaders(new String[] { "File", "Record", "Reference",
				"Version", "Status", "Project" });

		this.setMoreListJoins(new String[] {

				" left join tfile on trecord.file_id = tfile.file_id ",
				" left join tcodes s on trecord.status_cd = s.code_value and s.code_type_id  = 60 ",
				" left join tproject on trecord.project_id = tproject.project_id " });

		
		this.setMoreListColumns(new String[] { "tfile.title_nm as FileTitle",
				"trecord.title_nm as RecordTitle", "trecord.reference_nm",
				"trecord.version_id", "s.code_desc as status_desc",
				"project_name" });

		this
				.setMoreSelectJoins(new String[] { " join tfile on trecord.file_id = tfile.file_id " });

		this
				.setMoreSelectColumns(new String[] { "tfile.title_nm as fileName " });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean beforeAdd(Hashtable ht) {

		// this value comes from the list page,
		ht.put("file_id", new DbFieldInteger("file_id", new Integer(sm
				.Parm("FilterFile"))));
		return true;

	}

	public boolean listColumnHasSelector(int columnNumber) {
		// column 0 = file id, 4 = status
		if (columnNumber == 0 || columnNumber == 4)
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {
		case 0: {

			String sQuery = "Select title_nm, file_id, title_nm from tfile where application_id = "
					+ sm.getApplicationId().toString();

			return new WebFieldSelect("FilterFile", sm.Parm("FilterFile")
					.length() == 0 ? new Integer("0") : new Integer(sm
					.Parm("FilterFile")), db, sQuery, "- All Files -");
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
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		sm.setStructureType("Record"); // 

		// show the file name, if add mode, have to look it up using the
		// FilterFile parm.
		WebFieldDisplay wfFileName;
		if (addMode) {

			String query = " select title_nm from tfile where file_id = "
					+ sm.Parm("FilterFile");

			String answer = db.getColumn(query);
			wfFileName = new WebFieldDisplay("fileName", answer);
		} else {
			wfFileName = new WebFieldDisplay("fileName", db.getText("fileName"));
		}

		/*
		 * Id's
		 */
		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"), sm.getProjectFilter(), true);

		/*
		 * Strings
		 */
		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfVersionNo = new WebFieldString("version_id",
				(addMode ? "0" : db.getText("version_id")), 4, 4);

		/*
		 * Codes
		 */
		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("RECORDTYPES"), true);

		/*
		 * Blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);

		WebFieldText wfVersion = new WebFieldText("version_blob", addMode ? ""
				: db.getText("version_blob"), 3, 80);

		WebFieldText wfSecurity = new WebFieldText("security_blob",
				addMode ? "" : db.getText("security_blob"), 3, 80);

		WebFieldText wfContents = new WebFieldText("contents_blob",
				addMode ? "" : db.getText("contents_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		/*
		 * Return
		 */

		WebField[] wfs = { wfProject, wfVersion, wfType, wfDesc, wfVersionNo,
				wfFileName, wfStatus, wfTitle, wfRefr, wfSecurity, wfContents,
				wfNotes };

		return webFieldsToHT(wfs);

	}

}
