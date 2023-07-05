/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * GUI Data Manager
 * 
 * Change Log:
 * 
 * 5/19/05 Take out getDbFields!!
 * 
 * 
 * 
 */
public class DesignPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public DesignPlugin() throws services.ServicesException {
		super();
		this.setTableName("tdesign");
		this.setKeyName("design_id");
		this.setTargetTitle("Design Overviews");

		this.setMoreListColumns(new  String[] { "tdesign.title_nm",
				"i.code_desc as impact_desc", "s.code_desc as status_desc",
				"concat(u.last_name, ',', u.first_name)", "project_name" });

		this.setMoreListJoins(new  String[] {
				" left join tproject on tdesign.project_id = tproject.project_id ",
				"  left join tcodes i on tdesign.impact_cd = i.code_value and i.code_type_id  = 55 ",
				"  left join tcodes s on tdesign.status_cd = s.code_value and s.code_type_id  = 5 ",
				" left join tuser u on tdesign.assigned_uid = u.user_id " });

		this.setListHeaders( new String[] { "Title", "Impact", "Status", "Owner",
				"Last Project Impact" });

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		/*
		 * Id's
		 */
		ht.put("project_id", new WebFieldSelect("project_id",
				addMode ? new Integer("1") : db.getInteger("project_id"), sm
						.getProjectFilter()));

		// ht.put("workflow_id", new WebFieldSelect("workflow_id", addMode ? new
		// Integer("0")
		// : db.getInteger("workflow_id"), db.getLookupTable("tworkflow",
		// "workflow_id", "title_nm")));

		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));

		/*
		 * Codes
		 */
		ht.put("impact_cd", new WebFieldSelect("impact_cd", addMode ? "L" : db
				.getText("impact_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("STATUS")));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("diagram_url", new WebFieldString("diagram_url", (addMode ? ""
				: db.getText("diagram_url")), 64, 127));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 4, 100));

		ht.put("change_blob", new WebFieldText("change_blob", addMode ? "" : db
				.getText("change_blob"), 4, 100));

		ht.put("impact_blob", new WebFieldText("impact_blob", addMode ? "" : db
				.getText("impact_blob"), 4, 100));

		ht.put("limitation_blob", new WebFieldText("limitation_blob",
				addMode ? "" : db.getText("limitation_blob"), 4, 100));

		ht.put("consideration_blob", new WebFieldText("consideration_blob",
				addMode ? "" : db.getText("consideration_blob"), 4, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 100));

		ht.put("review_blob", new WebFieldText("review_blob", addMode ? "" : db
				.getText("review_blob"), 4, 100));
		
		ht.put("follow_up_blob", new WebFieldText("follow_up_blob", addMode ? "" : db
				.getText("follow_up_blob"), 4, 100));
		
		ht.put("changes_blob", new WebFieldText("changes_blob", addMode ? "" : db
				.getText("changes_blob"), 4, 100));
		/*
		 * Return
		 */
		return ht;

	}

}
