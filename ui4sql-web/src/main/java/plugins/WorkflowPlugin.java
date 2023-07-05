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
 * Workflow Plugin
 * 
 * 
 * Change Log: remove seq_no,, it's now in the detail Step form
 * 
 */
public class WorkflowPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Consutructor
	 * 
	 **************************************************************************/
	public WorkflowPlugin() throws services.ServicesException {
		super();

		this.setTargetTitle("Workflows");
		this.setTableName("tworkflow");
		this.setKeyName("workflow_id");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Step");
		this.setDetailTargetLabel("Steps");

		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"active_flag", "tproject.project_name " });

		this
				.setMoreListJoins(new String[] { " left join tproject on tworkflow.project_id = tproject.project_id " });

		this.setListHeaders(new String[] { "Title", "Reference", "Active",
				"Last Project Impact" });

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/
	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		sm.setStepKind("Workflow"); // leave a cookie so the Step manager knows
		// what kind of a step to take

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Id's
		 */

		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"), sm.getProjectFilter(), true);

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
		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfVer = new WebFieldString("version_nm", addMode ? ""
				: db.getText("version_nm"), 16, 16);

		/*
		 * Blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfChange = new WebFieldText("change_blob", addMode ? ""
				: db.getText("change_blob"), 3, 80);

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
		WebField[] wfs = { wfVer, wfEffDate, wfTrigger, wfActive, wfDesc,
				wfProject, wfChange, wfTitle, wfRefr, wfRules, wfInputs,
				wfOutputs, wfResults };

		return webFieldsToHT(wfs);

	}

}
