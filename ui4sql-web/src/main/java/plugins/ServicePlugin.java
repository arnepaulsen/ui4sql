/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Web Service Plugin 
 *  9/7/05
 *  
 *  to do: need more things that describe a web service.
 */

public class ServicePlugin extends AbsApplicationPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public ServicePlugin() throws services.ServicesException {
		super();

		this.setTableName("tservice");
		this.setKeyName("service_id");
		this.setTargetTitle("Web Service");

		this.setListHeaders( new String[] { "Title", "Reference", "Version",
				"Status", "Project" });

		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm",
				"version_id", "s.code_desc as status_desc", "project_name" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tservice.status_cd = s.code_value and s.code_type_id  = 60 ",
				" left join tproject on tservice.project_id = tproject.project_id " });

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

		// these are set for WebFielddisplay in 'view' mode, or a html selector
		// in add/update mode

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"),
				sm.getProjectFilter(), true);

		WebFieldString wfVersionId = new WebFieldString("version_id", db
				.getText("version_id"), 4, 4);

		WebFieldString wfRefr = new WebFieldString("reference_nm", db
				.getText("reference_nm"), 32, 32);

		WebFieldString wfTitle = new WebFieldString("title_nm", db
				.getText("title_nm"), 64, 64);

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("DATAFORM"));

		WebFieldText wfDesc = new WebFieldText("desc_blob", db
				.getText("desc_blob"), 5, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", db
				.getText("notes_blob"), 5, 80);
		
		WebFieldText wfWSDL = new WebFieldText("wsdl_blob", db
				.getText("wsdl_blob"), 5, 80);
		

		WebFieldText wfVersion = new WebFieldText("version_blob", db
				.getText("version_blob"), 3, 80);

		WebField[] wfs = { wfRefr, wfDesc, wfTitle, wfVersion, wfVersionId, wfWSDL, 
				wfProject, wfType, wfNotes, wfStatus, };

		return webFieldsToHT(wfs);

	}

}
