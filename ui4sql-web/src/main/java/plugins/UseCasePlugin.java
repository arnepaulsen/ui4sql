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

/*******************************************************************************
 * Test Specification TableManager
 * 
 * Change Log:
 * 
 * TODO : add logic to turn off reviewed by and reviewed date when pass_flag
 * turned off 3/10 convert to tcodes
 * 
 */

public class UseCasePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public UseCasePlugin() throws services.ServicesException {
		super();

		this.setTableName("tuse_case");
		this.setKeyName("use_case_id");
		this.setTargetTitle("Use Cases");

		this.setListHeaders( new String[] { "Reference Id", "Title", "Type" , "Last Impect Project"});

		this.setMoreListColumns(new  String[] { "reference_nm", "title_nm",
				"tcodes.code_desc as case_type", "project_name" });

		this.setMoreListJoins(new  String[] { " left join tcodes on tuse_case.case_type_cd = tcodes.code_value and tcodes.code_type_id = 37 ",
				" left join tproject on tuse_case.project_id = tproject.project_id " });

		
	}
	
	/*
	 * This table is cached... so clear it after add.
	 */
	public void afterAdd(Integer rowKey) throws services.ServicesException {
		sm.removeCache("tuse_case");
		return;
	}
	

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = getLookupTable("tuser", "user_id", "last_name");

		/*
		 * Id's
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

		WebFieldText wfActor = new WebFieldText("actors_tx", addMode ? "" : db
				.getText("actors_tx"), 3, 80);

		WebFieldString wfReference = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		/*
		 * Codes
		 */
		WebFieldSelect wfType = new WebFieldSelect("case_type_cd",
				addMode ? "NEW" : db.getText("case_type_cd"), sm
						.getCodes("USECASE"));

		/*
		 * Blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfChange = new WebFieldText("change_blob", addMode ? ""
				: db.getText("change_blob"), 3, 80);

		WebField[] wfs = { wfTitle, wfReference, wfType, wfChange, wfDesc, wfProject,
				wfActor };
		return webFieldsToHT(wfs);

	}

}
