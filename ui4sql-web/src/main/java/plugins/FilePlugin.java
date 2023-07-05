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
 * File Plugin
 * 
  * 9/9/05 - using 'Element' as child form. 
 */
public class FilePlugin extends AbsApplicationPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public FilePlugin() throws services.ServicesException {
		super();
		this.setTableName("tfile");
		this.setKeyName("file_id");
		this.setTargetTitle("File");
		
		this.setHasDetailForm (true); // detail is the Codes form
		this.setDetailTarget ("Record");
		this.setDetailTargetLabel ("Records");
		
		
		this.setListHeaders( new String[] { "Title", "Reference", "Version",
				"Status","Project"});
		

		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm",
				"version_id", "s.code_desc as status_desc" , "project_name"});

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tfile.status_cd = s.code_value and s.code_type_id  = 60 " ,
				" left join tproject on tfile.project_id = tproject.project_id "});
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
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		sm.setStructureType("File");  // leave a cookie so the Step manager knows what kind of a step to take
				
		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("file_id"), db
					.getText("tfile.title_nm"));
		}
		
		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"),
				sm.getProjectFilter(), true);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfVersionNo = new WebFieldString("version_id",
				(addMode ? "0" : db.getText("version_id")), 4, 4);

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));
		
		WebFieldSelect wfOS = new WebFieldSelect("os_type_cd", addMode ? ""
				: db.getText("os_type_cd"), sm.getCodes("SERVERTYPE"));
		

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);
		
		WebFieldString wfSize = new WebFieldString("size_tx", (addMode ? ""
				: db.getText("size_tx")), 64, 100);
		

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 70);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 70);

		WebFieldText wfVersion = new WebFieldText("version_blob", addMode ? ""
				: db.getText("version_blob"), 3, 70);
		
		WebFieldText wfUsage = new WebFieldText("usage_blob", addMode ? ""
				: db.getText("usage_blob"), 3, 70);

		WebField[] wfs = { wfDesc, wfTitle, wfStatus, wfRefr, wfNotes,
				wfVersion, wfVersionNo, wfProject, wfUsage, wfSize, wfOS};

		return webFieldsToHT(wfs);

	}

}
