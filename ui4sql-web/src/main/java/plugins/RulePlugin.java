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
 * 	3/11 add join on project Permissions
 * */

/**
 * Business Rule Plugin
 * 
 * 
 * 
 */

public class RulePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Consutructor
	 * 
	 **************************************************************************/
	public RulePlugin() throws services.ServicesException {
		super();
		this.setTargetTitle("Business Rule");
		this.setTableName("trule");
		this.setKeyName("rule_id");

		this.setMoreListColumns(new  String[] { "reference_nm", "title_nm",
				"active_flag", "project_name" });

		this.setListHeaders( new String[] { "Title", "Reference", "Active" , "Last Impact Project"});
		
		this.setMoreListJoins(new  String[] { " left join tproject on trule.project_id = tproject.project_id " });


	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/
	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Ids
		 */
		WebFieldSelect wfProjectId = new WebFieldSelect("project_id",
				addMode ? new Integer("1") : db.getInteger("project_id"), sm
						.getProjectFilter());

		/*
		 * Flags
		 */
		WebFieldCheckbox wfActive = new WebFieldCheckbox("active_flag",
				addMode ? "N" : db.getText("active_flag"), "");

		/*
		 * Dates
		 */
		
		WebFieldString wfEffDate = new WebFieldString("effect_date",
				(addMode ? "" : db.getText("effect_date")), 10, 10);
		
		/*
		 * Strings
		 */
		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfSeq = new WebFieldString("seq_no", (addMode ? "" : db
				.getText("seq_no")), 6, 6);

		

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		/*
		 * Blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);
		
		WebFieldText wfChange = new WebFieldText("change_blob", addMode ? "" : db
				.getText("change_blob"), 3, 80);

		WebFieldString wfVer = new WebFieldString("version_nm", addMode ? ""
				: db.getText("version_nm"), 16, 16);

		WebFieldText wfInputs = new WebFieldText("inputs_blob", addMode ? ""
				: db.getText("inputs_blob"), 3, 80);

		WebFieldText wfOutputs = new WebFieldText("outcomes_blob", addMode ? ""
				: db.getText("outcomes_blob"), 3, 80);

		WebFieldText wfRules = new WebFieldText("rules_blob", addMode ? "" : db
				.getText("rules_blob"), 3, 80);

		WebFieldText wfResults = new WebFieldText("results_blob", addMode ? ""
				: db.getText("results_blob"), 3, 80);

		WebFieldText wfTrigger = new WebFieldText("trigger_blob", addMode ? ""
				: db.getText("trigger_blob"), 3, 80);

		/*
		 * Return 
		 */
		
		WebField[] wfs = { wfProjectId, wfVer, wfEffDate, wfTrigger, wfActive, wfChange,
				wfDesc, wfSeq, wfTitle, wfRefr, wfRules, wfInputs, wfOutputs,
				wfResults };
		return webFieldsToHT(wfs);

	}

}
