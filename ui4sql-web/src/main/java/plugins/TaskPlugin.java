/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Task Plugin
 * 
 * Change log:
 * 
 * 2/15 added mySql 3/13 as 'target' to list query
 * 
 */
public class TaskPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public TaskPlugin() throws services.ServicesException {
		super();
		this.setTableName("ttask");
		this.setKeyName("task_id");
		this.setTargetTitle("Next Steps");
		this.setListHeaders( new String[] { "Reference", "Title", "Status", "Assigned",
				 "Plan&nbsp;End",	"Actual&nbsp;End" });
		
		this.setMoreListColumns(new  String[] { "reference_nm", "title_nm",
				"code_desc", "assigned.last_name",  "ttask.plan_end_date",
			 "ttask.act_end_date" });
		
		this.setMoreListJoins(new  String[] { " left join tcodes on ttask.status_cd = tcodes.code_value and tcodes.code_type_id  = 5 ",
								" left join tuser assigned on ttask.assigned_uid = assigned.user_id"});

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

		
		// TODO: cache the deliverables
		Hashtable deliverables = db
				.getLookupTable("select title_nm as odor, deliverable_id, title_nm from tdeliverable ");

		/*
		 * id's
		 */
		ht.put("deliverable_id", new WebFieldSelect("deliverable_id",
				addMode ? new Integer("0") : db.getInteger("deliverable_id"),
				deliverables));
		ht.put("assigned_uid",  new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));
		
		/*
		 * dates
		 */
		
		ht.put("plan_start_date", new WebFieldDate("plan_start_date",
				addMode ? "" : db.getText("plan_start_date")));

		ht.put("plan_end_date", new WebFieldDate("plan_end_date",
				addMode ? "" : db.getText("plan_end_date")));

		ht.put("act_start_date", new WebFieldDate("act_start_date",
				addMode ? "" : db.getText("act_start_date")));

		ht.put("act_end_date",  new WebFieldDate("act_end_date", addMode ? ""
				: db.getText("act_end_date")));

		
		/*
		 * codes
		 */
		ht.put("status_cd",  new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("STATUS")));
		
		ht.put("priority_cd", new WebFieldSelect("priority_cd",
				addMode ? "" : db.getText("priority_cd"), sm
						.getCodes("PRIORITY")));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 12, 12));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32));

		/*
		 * blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("resolution_blob",  new WebFieldText("resolution_blob",
				addMode ? "" : db.getText("resolution_blob"), 3, 60));

		


		/*
		 * amts
		 */
		ht.put("est_qty",  new WebFieldString("est_qty", addMode ? "0"
				: db.getText("est_qty"), 6, 6));

		ht.put("act_qty", new WebFieldString("act_qty", addMode ? "0"
				: db.getText("act_qty"), 6, 6));

		return ht;

	}

}
