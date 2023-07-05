/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * 
 *   2/15 added mySql
 * 3/13 as 'target' to list query 
 * */

/**
 * 
 * Reports Manager -
 * 
 */
public class ReportSpecPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ReportSpecPlugin() throws services.ServicesException {
		super();
		this.setTableName("treport");
		this.setKeyName("report_id");
		this.setTargetTitle("Report Specification");

		this.setListHeaders( new String[] { "Title", "Reference" });
		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm" });
		this.setMoreSelectColumns (new String[] { "tproject.project_name" });
		this.setAddOk(false);
	}

	

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
	
		WebFieldDisplay wfDesc = new WebFieldDisplay("desc_blob", db
				.getText("desc_blob"));
		
		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : db
						.getInteger("project_id"), sm
						.getProjectFilter());

		WebFieldDisplay wfRefr = new WebFieldDisplay("reference_nm",
				(addMode ? "" : db.getText("reference_nm")));

		WebFieldDisplay wfTitle = new WebFieldDisplay("title_nm", (addMode ? ""
				: db.getText("title_nm")));

		WebFieldText wfSelection = new WebFieldText("selection_criteria_blob",
				(addMode ? "" : db.getText("selection_criteria_blob")), 3, 80);

		WebFieldText wfData = new WebFieldText("data_source_blob", addMode ? ""
				: db.getText("data_source_blob"), 3, 80);
		
		WebFieldText wfSort = new WebFieldText("sorting_blob", addMode ? ""
				: db.getText("sorting_blob"), 3, 80);
		
		WebFieldText wfLogic = new WebFieldText("logic_blob", addMode ? ""
				: db.getText("logic_blob"), 3, 80);
		
		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? ""
				: db.getText("notes_blob"), 3, 80);
		
		WebField[] wfs = { wfProject, wfTitle, wfRefr, wfDesc, wfNotes, wfLogic, wfSort, wfSelection, wfData };

		return webFieldsToHT(wfs);

	}

}
