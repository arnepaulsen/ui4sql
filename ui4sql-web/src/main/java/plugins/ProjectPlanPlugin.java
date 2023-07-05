/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import mpxj.ProjectTaskLoad.*;

/**
 * Project Project - Watch out Microsoft !!!
 * 
 * 
 * 
 */
public class ProjectPlanPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public ProjectPlanPlugin() throws services.ServicesException {
		super();
		this.setTableName("tproject_plan");
		this.setKeyName("project_plan_id");
		this.setTargetTitle("Project Plan");
		this.setListHeaders( new String[] { "Reference", "Title", "Status",
				"Owner" });

		this.setMoreListColumns(new  String[] { "reference_nm", "title_nm",
				"stat.code_desc as StatDesc",
				"concat(o.last_name, ',', o.first_name)", });

		this.setMoreListJoins(new  String[] {
				" left join tcodes stat on tproject_plan.status_cd = stat.code_value and stat.code_type_id  = 5 ",
				" left join tuser o on tproject_plan.owner_uid = o.user_id " });

		this.setHasDetailForm (true);
		this.setDetailTarget ("ProjectPlanTask");
		this.setDetailTargetLabel ("Tasks");

	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Id's
		 */

		if (parmMode.equalsIgnoreCase("show")) {

			sm.setParentId(db.getInteger("project_plan_id"), db
					.getText("title_nm"));
		}

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT()));

		ht.put("seq_no", new WebFieldString("seq_no", addMode ? "" : db
				.getText("seq_no"), 5, 5));

		/*
		 * Codes
		 * 
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("STATUS")));

		/*
		 * Strings
		 */

		ht.put("title_nm", new WebFieldString("title_nm", addMode ? "" : db
				.getText("title_nm"), 64, 64));

		ht.put("reference_nm", new WebFieldString("reference_nm", addMode ? ""
				: db.getText("reference_nm"), 32, 32));

		ht.put("version_nm", new WebFieldString("version_nm", addMode ? "" : db
				.getText("version_nm"), 16, 16));

		/*
		 * Dates
		 */

		ht.put("final_date", new WebFieldDate("final_date", addMode ? "" : db
				.getText("final_date")));

		/*
		 * Blobs
		 */

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 80));

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 4, 80));

		ht.put("issues_blob", new WebFieldText("issues_blob", addMode ? "" : db
				.getText("issues_blob"), 4, 80));

		return ht;

	}

	/*
	 * Called by the show routine
	 * 
	 * 
	 */
	public void customAction() {
		debug("ProjectPlan.. uploading a file...");
		debug(" file name is " + sm.Parm("projectFile"));

		mpxj.ProjectTaskLoad project = new mpxj.ProjectTaskLoad(sm
				.getConnection());

		try {
			Integer projectPlanId = db.getInteger("project_plan_id");
			debug("calling loadFile: project plan id " + projectPlanId.toString());
			
			project.loadFile(sm.Parm("projectFile"));

			debug("calling uploadtasks ");
			
			project.uploadTasks(projectPlanId);

		} catch (Exception e) {

		}

		return;
	}

}
