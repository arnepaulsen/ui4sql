/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * GUI Data Manager
 * 
 * Change Log:
 * 
 * 5/19/05 Take out getDbFields!!
 * 
 * 
 */
public class GuiPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public GuiPlugin() throws services.ServicesException {
		super();
		this.setTableName("tgui");
		this.setKeyName("gui_id");
		this.setTargetTitle("User Interface");

		this.setListHeaders( new String[] { "Title", "Reference", "Version",
				"Status", "Project" });

		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm",
				"version_nm", "s.code_desc as status_desc", "project_name" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tgui.status_cd = s.code_value and s.code_type_id  = 60 ",
				" left join tproject on tgui.project_id = tproject.project_id " });
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
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		/*
		 * Ids
		 */

		ht.put("project_id", new WebFieldSelect("project_id", addMode ? sm
				.getProjectId() : (Integer) db.getObject("project_id"), sm
				.getProjectFilter(), true));

		/*
		 * Flags
		 */
		ht.put("confirmed_flag", new WebFieldCheckbox("confirmed_flag",
				addMode ? "N" : db.getText("confirmed_flag"), ""));

		/*
		 * Strings
		 * 
		 */

		ht.put("version_nm", new WebFieldString("version_nm", db
				.getText("version_nm"), 4, 4));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * Codes
		 */
		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("LIVESTAT")));

		/*
		 * Blobs
		 */
		ht.put("blob_description", new WebFieldText("blob_description",
				addMode ? "" : db.getText("blob_description"), 3, 80));

		ht.put("blob_purpose", new WebFieldText("blob_purpose", addMode ? ""
				: db.getText("blob_purpose"), 3, 80));

		ht.put("blob_trigger", new WebFieldText("blob_trigger", addMode ? ""
				: db.getText("blob_trigger"), 3, 80));

		ht.put("blob_rules", new WebFieldText("blob_rules", addMode ? "" : db
				.getText("blob_rules"), 3, 80));

		ht.put("blob_edits", new WebFieldText("blob_edits", addMode ? "" : db
				.getText("blob_edits"), 3, 80));

		ht.put("blob_notes", new WebFieldText("blob_notes", addMode ? "" : db
				.getText("blob_notes"), 3, 80));

		ht.put("version_blob", new WebFieldText("version_blob", db
				.getText("version_blob"), 3, 80));

		return ht;

	}

}
