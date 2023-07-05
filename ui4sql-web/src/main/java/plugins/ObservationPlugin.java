/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Note Plugin
 * 
 * Change log:
 * 
 * 2/15 added mySql 3/13 as 'target' to list query
 * 
 */
public class ObservationPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public ObservationPlugin() throws services.ServicesException {
		super();
		this.setTableName("tobservation");
		this.setKeyName("observation_id");
		this.setShowAuditSubmitApprove(false);

		this.setTargetTitle("Observation");

		this.setListHeaders( new String[] { "Title", "Keywords", "Type" , "Last Project Impact"});
		
		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm",
				"code_desc", "project_name"});

		this.setMoreListJoins(new  String[] {
				" left join tproject on tobservation.project_id = tproject.project_id ",
				" left join tcodes on tobservation.type_cd = tcodes.code_value and tcodes.code_type_id  = 44 " });
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = sm.getUserHT();

		/*
		 * Ids
		 */
		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"),
				sm.getProjectFilter(), true);

		/*
		 * Strings
		 */
		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfVersion = new WebFieldString("version_tx",
				(addMode ? "" : db.getText("version_tx")), 4, 4);

		/*
		 * Codes
		 */
		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("OBSERVATIONTYPE"));

		/*
		 * dates
		 */
		WebFieldDate wfDate = new WebFieldDate("observation_date",
				(addMode ? "" : db.getText("observation_date")));

		/*
		 * Blobs
		 */

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebFieldText wfChange = new WebFieldText("change_blob", addMode ? ""
				: db.getText("change_blob"), 3, 80);

		/*
		 * Return
		 */
		WebField[] wfs = { wfRefr, wfProject, wfChange, wfVersion, wfDate,
				wfType, wfTitle, wfDesc, wfNotes };

		return webFieldsToHT(wfs);

	}

}
