/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Constraint Data Manager
 * 
 * Change Log:
 * 
 * 6/14/05 New
 * 
 * ToDo: - add selector for type_cd, new code type 'message'
 * 
 * 
 */

public class MessagePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public MessagePlugin() throws services.ServicesException {
		super();
		this.setTableName("tmessage");
		this.setKeyName("message_id");
		this.setTargetTitle("Message");

		this.setHasDetailForm (true); // detail is the Codes form
		this.setDetailTarget ("Segment"); // figure out in show mode!
		this.setDetailTargetLabel ("Segments");

		this.setListHeaders( new String[] { "Title", "Reference", "Version",
				"Status", "Project" });

		this.setMoreListColumns(new  String[] { "title_nm" });

		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm",
				"version_id", "s.code_desc as status_desc", "project_name" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tmessage.status_cd = s.code_value and s.code_type_id  = 60 ",
				" left join tproject on tmessage.project_id = tproject.project_id " });
		;

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#3)
		if (columnNumber == 3)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		// default the status to open when starting new list
		WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
				"FilterStatus").length() == 0 ? "P" : sm.Parm("FilterStatus")),
				sm.getCodes("LIVESTAT"), "All Status");
		wf.setDisplayClass("listform");
		return wf;
	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		// default status to 'Production' if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND s.code_value = 'P'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus")
						+ "'");
			}
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

		sm.setStructureType("Message"); // leave a cookie so the Step manager
		// knows what kind of a step to take

		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("message_id"), db
					.getText("tmessage.title_nm"));
		}

		/*
		 * Numbers
		 */

		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"),
				sm.getProjectFilter(), true);

		WebFieldString wfVersionNo = new WebFieldString("version_id",
				(addMode ? "0" : db.getText("version_id")), 4, 4);

		/*
		 * Codes
		 */

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("MSGTYPE"), true);

		/*
		 * Strings
		 */
		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		/*
		 * Blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfVersion = new WebFieldText("version_blob", addMode ? ""
				: db.getText("version_blob"), 3, 80);

		WebFieldText wfSecurity = new WebFieldText("security_blob",
				addMode ? "" : db.getText("security_blob"), 3, 80);

		WebFieldText wfContents = new WebFieldText("contents_blob",
				addMode ? "" : db.getText("contents_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebField[] wfs = { wfProject, wfVersion, wfType, wfDesc, wfVersionNo,
				wfStatus, wfTitle, wfRefr, wfSecurity, wfContents, wfNotes };

		return webFieldsToHT(wfs);

	}
}
