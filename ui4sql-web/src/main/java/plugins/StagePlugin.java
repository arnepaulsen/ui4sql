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
 * This is a child of Process Stages. Stages is the only parent type.
 * 
 * Unique in that it is both a parent and a child
 * 
 * 
 * 
 * Change Log:
 * 
 * 9/8 added
 * 
 * 
 ******************************************************************************/
public class StagePlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public StagePlugin() throws services.ServicesException {
		super();

		this.setTableName("tstage");
		this.setKeyName("stage_id");
		this.setTargetTitle("Stages");

		this.setIsStepChild(true);
		this.setIsDetailForm(true);
		this.setHasDetailForm (true);
		this.setDetailTarget ("StageDeliverable");
		this.setDetailTargetLabel ("Deliverables");
		this.setParentTarget ("Process");

		this.setListOrder ("seq_no");
		this.setContextSwitchOk (false); // can't change down here

	}

	public void init(SessionMgr parmSm) {

		/*
		 * we have to wait for the FormData to run init to push the sm into the
		 * dm object
		 * 
		 */

		this.sm = parmSm;
		this.db = sm.getDbInterface(); // has an open connection

		/*
		 * 
		 */

		this.setTargetTitle(sm.getParentName() + " - Stages");

		this.setListHeaders( new String[] { "Seq", "Title", "Required", "Percent" });

		this.setMoreListColumns(new  String[] { "seq_no", "tstage.title_nm",
				"tstage.required_flag", "tstage.effort_pct" });

		this.setMoreListJoins(new  String[] {
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

		ht.put("process_id",
				new DbFieldInteger("process_id", sm.getParentId()));
		return true;

	}

	// limit the list to this process.
	public String getListAnd() {
		return " AND tstage.process_id = " + sm.getParentId().toString();
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

		debug("getWebFields ... starting");
		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		// save my key info for the StageDeliverable plugin
		if (parmMode.equalsIgnoreCase("show")) {
			sm.setStageId(db.getInteger("stage_id"), db.getText("title_nm"));
		}

		/*
		 * strings
		 * 
		 */

		ht.put("parentName", new WebFieldDisplay("parentName", sm
				.getParentName()));

		ht.put("title_nm", new WebFieldString("title_nm", addMode ? "" : db
				.getText("title_nm"), 64, 64));

	
		/*
		 * numbers
		 */

		ht.put("seq_no", new WebFieldString("seq_no", addMode ? "0" : db
				.getText("seq_no"), 4, 4));

		ht.put("effort_pct", new WebFieldString("effort_pct", (addMode ? ""
				: db.getText("effort_pct")), 3, 6));

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
				.getText("type_cd"), sm.getCodes("STAGES")));
		
		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("LIVESTAT")));

		/*
		 * blobs
		 */
		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 80));

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 4, 80));

		ht.put("inputs_blob", new WebFieldText("inputs_blob", addMode ? "" : db
				.getText("inputs_blob"), 4, 80));

		ht.put("outputs_blob", new WebFieldText("outputs_blob", addMode ? ""
				: db.getText("outputs_blob"), 4, 80));

		ht.put("entry_criteria_blob", new WebFieldText("entry_criteria_blob",
				addMode ? "" : db.getText("entry_criteria_blob"), 4, 80));

		ht.put("exit_criteria_blob", new WebFieldText("exit_criteria_blob",
				addMode ? "" : db.getText("exit_criteria_blob"), 4, 80));

		ht.put("measurements_blob", new WebFieldText("measurements_blob",
				addMode ? "" : db.getText("measurements_blob"), 4, 80));

		ht.put("considerations_blob", new WebFieldText("considerations_blob",
				addMode ? "" : db.getText("considerations_blob"), 4, 80));

		
		debug("getWebFields ... done");
		
		return ht;

	}

}
