/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import db.DbFieldInteger;
import forms.*;
import router.SessionMgr;

/*******************************************************************************
 * Stage Plugin
 * 
 * This is a child of Process Stages.
 * 
 * It's a great grand-child : - division - process - stage (both a parent and a
 * child) - stageDeliverable!
 * 
 * 
 * 
 * 
 * Change Log:
 * 
 * 9/8 added
 * 
 * 
 ******************************************************************************/
public class StageDeliverablePlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public StageDeliverablePlugin() throws services.ServicesException {
		super();

		this.setTableName("tstage_deliverable");
		this.setKeyName("stage_deliverable_id");
		this.setTargetTitle("Stage Deliverables");

		this.setIsStepChild (true);
		this.setIsDetailForm(true);
		this.setParentTarget ("Stage");

		this.setContextSwitchOk (false); // can't change down here

	}

	public void init(SessionMgr parmSm) {

		this.sm = parmSm;
		this.db = sm.getDbInterface(); // has an open connection

		this.setTargetTitle("Stage : " + sm.getStageName() + " - Deliverables");

		this.setListHeaders( new String[] { "Deliverable", "Required" });

		this.setMoreListColumns(new  String[] { "d.code_desc",
				"tstage_deliverable.required_flag" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes d on tstage_deliverable.type_cd = d.code_value and d.code_type_id  = 10 ",
				" join tstage on tstage_deliverable.stage_id = tstage.stage_id",
				" join tprocess on tstage.process_id = tprocess.process_id ",
				" join tdivision on tprocess.division_id = tdivision.division_id " });

		this.setMoreSelectJoins (this.moreListJoins);
		this.setMoreSelectColumns (new String[] { "tdivision.division_id" });

		this.setUpdatesOk(sm.userIsExecutive());
		
	}

	
	/*
	 * 
	 * List Heading Selectors
	 * 
	 */

	
	/***************************************************************************
	 * 
	 * update closed-by date and close-by-uid
	 * 
	 **************************************************************************/

	// save the parent key
	public boolean beforeAdd(Hashtable ht) {

		ht.put("stage_id", new DbFieldInteger("stage_id", sm.getStageId()));
		return true;

	}

	// limit the list to this process.
	public String getListAnd() {

		debug("getListAnd ... stage id = " + sm.getStageId().toString());

		return " AND tstage_deliverable.stage_id = "
				+ sm.getStageId().toString();
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/
	/*
	 * data manager calls this just before passing the ht to the db for insert
	 * ... we can insert the 'parent_kind_cd' now,
	 */

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * strings
		 * 
		 */
		
		/*
		 * to do.. fix double session names
		 */

		ht.put("processName", new WebFieldDisplay("processName", sm
				.getParentName()));
		
		ht.put("stageName", new WebFieldDisplay("stageName", sm
				.getParentName()));

		

		/*
		 * flags
		 * 
		 */
		ht.put("required_flag", new WebFieldCheckbox("required_flag",
				addMode ? "" : db.getText("required_flag"), ""));

		/*
		 * codes
		 * 
		 */
		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("DELIVERABLE")));

		/*
		 * blobs
		 */
		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 80));

		ht.put("exclusions_blob", new WebFieldText("exclusions_blob", addMode ? "" : db
				.getText("exclusions_blob"), 4, 80));
		
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 4, 80));

		return ht;

	}

}
