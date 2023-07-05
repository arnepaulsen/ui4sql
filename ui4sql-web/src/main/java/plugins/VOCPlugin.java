/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/*******************************************************************************
 * Voice of Customer
 * 
 * 
 * Change Log:
 * 
 * 
 ******************************************************************************/

public class VOCPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public VOCPlugin() throws services.ServicesException {
		super();
		this.setTableName("tvoc");
		this.setKeyName("voc_id");
		this.setTargetTitle("Voice of The Customer");

		this.setListHeaders( new String[] { "Title", "Status", "Owner",  "Version" });
		
		this.setMoreListColumns(new  String[] { "title_nm", "code_desc",
				"concat(u.last_name, ',', u.first_name) as theOwner",
				"version_nm", });
		
		this.setMoreListJoins(new  String[] {
				" left join tcodes on tvoc.status_cd = tcodes.code_value and tcodes.code_type_id  = 5 ",
				" left join tuser u on tvoc.owner_uid = u.user_id " });

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebFieldString wfTitle = new WebFieldString("title_nm", addMode ? ""
				: db.getText("title_nm"), 64, 128);

		WebFieldString wfRefr = new WebFieldString("reference_nm", addMode ? ""
				: db.getText("reference_nm"), 32, 32);

		WebFieldString wfVersion = new WebFieldString("version_nm",
				addMode ? "" : db.getText("version_nm"), 4, 4);

		WebFieldDate wfVerDate = new WebFieldDate("version_date", addMode ? ""
				: db.getText("version_date"));

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("STATUS"));

		WebFieldSelect wfOwner = new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT());

		/*
		 * Blobs
		 */

		WebFieldText wfSource = new WebFieldText("blob_back_source",
				addMode ? "" : db.getText("blob_back_source"), 3, 60);

		WebFieldText wfEnv = new WebFieldText("blob_back_environment",
				addMode ? "" : db.getText("blob_back_environment"), 3, 60);

		WebFieldText wfAudience = new WebFieldText("blob_plan_scope_audience",
				addMode ? "" : db.getText("blob_plan_scope_audience"), 3, 60);

		WebFieldText wfPopulation = new WebFieldText(
				"blob_plan_scope_population", addMode ? "" : db
						.getText("blob_plan_scope_population"), 3, 60);

		WebFieldText wfLocation = new WebFieldText("blob_plan_scope_location",
				addMode ? "" : db.getText("blob_plan_scope_location"), 3, 60);

		WebFieldText wfCommunication = new WebFieldText(
				"blob_plan_approach_comm", addMode ? "" : db
						.getText("blob_plan_approach_comm"), 3, 60);

		WebFieldText wfLogistics = new WebFieldText(
				"blob_plan_approach_logistics", addMode ? "" : db
						.getText("blob_plan_approach_logistics"), 3, 60);

		WebFieldText wfRecord = new WebFieldText(
				"blob_plan_approach_record_data", addMode ? "" : db
						.getText("blob_plan_approach_record_data"), 3, 60);

		WebFieldText wfAnalysis = new WebFieldText(
				"blob_plan_approach_analysis", addMode ? "" : db
						.getText("blob_plan_approach_analysis"), 3, 60);

		WebFieldText wfTimeline = new WebFieldText("blob_plan_timeline",
				addMode ? "" : db.getText("blob_plan_timeline"), 3, 60);

		WebFieldText wfStaff = new WebFieldText("blob_plan_resources_staff",
				addMode ? "" : db.getText("blob_plan_resources_staff"), 3, 60);

		WebFieldText wfBudget = new WebFieldText("blob_plan_resources_budget",
				addMode ? "" : db.getText("blob_plan_resources_budget"), 3, 60);

		WebFieldText wfOther = new WebFieldText("blob_plan_other", addMode ? ""
				: db.getText("blob_plan_other"), 3, 60);

		WebFieldText wfProblem = new WebFieldText("blob_need_problem_desc",
				addMode ? "" : db.getText("blob_need_problem_desc"), 3, 60);

		WebFieldText wfEnhance = new WebFieldText("blob_followup_enhancements",
				addMode ? "" : db.getText("blob_followup_enhancements"), 3, 60);

		WebField[] wfs = { wfOwner, wfStatus, wfTitle, wfRefr, wfVersion,
				wfVerDate, wfSource, wfEnv, wfAudience, wfPopulation,
				wfLocation, wfCommunication, wfLogistics, wfRecord, wfAnalysis,
				wfTimeline, wfStaff, wfBudget, wfOther, wfProblem, wfEnhance };

		return webFieldsToHT(wfs);
	}

}
