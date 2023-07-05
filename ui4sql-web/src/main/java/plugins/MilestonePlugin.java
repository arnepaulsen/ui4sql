/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Milestone Plugin
 * 
 * Change log:
 * 
 * 2/15 added mySql 3/13 as 'target' to list query
 * 
 */
public class MilestonePlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public MilestonePlugin() throws services.ServicesException {
		super();
		this.setTableName("tmilestone");
		this.setKeyName("milestone_id");
		this.setTargetTitle("Milestone");
		

		this.setHasDetailForm (true); // detail is the Codes form
		this.setDetailTarget ("Step");
		this.setDetailTargetLabel ("Steps");

		this.setListHeaders( new String[] { "Reference", "Title" , "Owner", "Status", "Target Date",
				"Completed Date" });

		this.setMoreListColumns(new  String[] { "reference_nm", "title_nm", "concat(u.last_name, ',', u.first_name)",
				"code_desc",
				"target_date", "complete_date" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes on tmilestone.status_cd = tcodes.code_value and tcodes.code_type_id  = 5 ",
				" left join tuser u on tmilestone.responsible_uid = u.user_id " });

		this.setListOrder ("target_date");

	}
	
	
	public boolean getListColumnCenterOn(int i) {
		if (i == 4 || i == 5 ) return true;
		else return false;
		
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

		sm.setStepKind("Milestone"); // leave a cookie so the Step manager
		// knows what kind of a step to take

		/*
		 * Ids
		 */
		ht.put("responsible_uid", new WebFieldSelect("responsible_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("responsible_uid"), sm.getUserHT()));

		/*
		 * Codes
		 */
		ht.put("status_cd", new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("STATUS")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("MILESTONE")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd",
				addMode ? "" : db.getText("priority_cd"), sm
						.getCodes("PRIORITY")));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64));

	

		/*
		 * Dates
		 */
		
		ht.put("target_date", new WebFieldDate("target_date",
				(addMode ? "" : db.getText("target_date"))));
		
		ht.put("complete_date", new WebFieldDate("complete_date",
				(addMode ? "" : db.getText("complete_date"))));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("impact_blob", new WebFieldText("impact_blob", addMode ? ""
				: db.getText("impact_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		/*
		 * Return ht
		 */
		return ht;

	}

}
