/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import forms.WebFieldString;
import forms.WebFieldSelect;
import forms.WebFieldText;
import forms.WebFieldDisplay;

/**
 * 
 * 
 * /** Deliverable Data eManager
 * 
 */
public class DeliverablePlugin extends AbsProjectPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public DeliverablePlugin() throws services.ServicesException {
		super();
		this.setTableName("tdeliverable");
		this.setKeyName("deliverable_id");
		this.setTargetTitle("Project Deliverable");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Responsibility");
		this.setDetailTargetLabel("Responsibilities");

		this.setListHeaders(new String[] { "Title", "DMAIC", "Owner", "Type",
				"Status" });
		;

		this.setMoreListColumns(new String[] { "title_nm",
				"dmaic_codes.code_desc as dmaic_desc",
				"concat(u.last_name, ',', u.first_name) as theOwner",
				"delv_codes.code_desc as deliv_type",
				"status_codes.code_desc as status_desc" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes as status_codes on tdeliverable.deliverable_status_cd = status_codes.code_value and status_codes.code_type_id   = 5 ",
						" left join tcodes as delv_codes on tdeliverable.deliverable_cd =  delv_codes.code_value and delv_codes.code_type_id  = 10   ",
						" left join tcodes as dmaic_codes on delv_codes.code_desc2 = dmaic_codes.code_value and dmaic_codes.code_type_id = 8 ",
						" left join tuser u on tdeliverable.owner_id = u.user_id " });

		this.setMoreSelectColumns(extraSelectColumns);
		this.setMoreSelectJoins(extraSelectJoins);

	}

	// *******************
	// LIST PAGE
	// *******************

	private static String deliverablesListQuery = "select 'Deliverable' as target,  tdeliverable.deliverable_id, "
			+ " title_nm, "
			+ " reference_id,"
			+ " dmaic_codes.code_desc as dmaic_desc, "
			+ " delv_codes.code_desc as deliv_type, "
			+ " status_codes.code_desc as status_desc"
			+ " from tdeliverable "
			+ " left join tproject on tdeliverable.project_id = tproject.project_id "
			+ " left join tcodes as status_codes on tdeliverable.deliverable_status_cd = status_codes.code_value and status_codes.code_type_id   = 5 "
			+ " left join tcodes as delv_codes on tdeliverable.deliverable_cd =  delv_codes.code_value and delv_codes.code_type_id  = 10   "
			+ " left join tcodes as dmaic_codes on delv_codes.code_desc2 = dmaic_codes.code_value and dmaic_codes.code_type_id = 8 "
			+ " left join tuser u on tdeliverable.owner_id = u.user_id ";

	// *******************
	// WEB PAGE
	// *******************

	private String[] extraSelectColumns = { " tcodes.code_desc as dmaic_desc ",
			"zdeliverable_codes.code_desc as deliverable_desc" };

	private String[] extraSelectJoins = {
			" left join tcodes as zdeliverable_codes  on tdeliverable.deliverable_cd =  zdeliverable_codes.code_value and zdeliverable_codes.code_type_id = 10   ",
			" left join tcodes on zdeliverable_codes.code_desc2 = tcodes.code_value and tcodes.code_type_id  = 8 " };

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();
		
		

		/*
		 * Strings
		 */

		ht.put("title_nm", new WebFieldString("title_nm", addMode ? "" : db
				.getText("title_nm"), 64, 64));

		ht.put("url_tx", new WebFieldString("url_tx", addMode ? "" : db
				.getText("url_tx"), 64, 255));

		ht.put("reference_nm", new WebFieldString("reference_nm", addMode ? ""
				: db.getText("reference_nm"), 32, 32));

		ht.put("dmaic_desc", new WebFieldDisplay("dmaic_desc", addMode ? ""
				: db.getText("dmaic_desc")));

		/*
		 * Ids
		 */

		ht.put("owner_id", new WebFieldSelect("owner_id",
				addMode ? new Integer("0") : db.getInteger("owner_id"), sm
						.getUserHT()));

		/*
		 * Codes
		 */

		ht.put("deliverable_cd", new WebFieldSelect("deliverable_cd",
				addMode ? "" : db.getText("deliverable_cd"), sm
						.getCodes("DELIVERABLE")));

		ht.put("deliverable_status_cd", new WebFieldSelect(
				"deliverable_status_cd", addMode ? "New" : db
						.getText("deliverable_status_cd"), sm
						.getCodes("STATUS")));

		/*
		 * Blobs
		 */

		ht.put("notes_tx", new WebFieldText("notes_tx", addMode ? "" : db
				.getText("notes_tx"), 3, 60));

		return ht;

	}
}
