/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;

/**
 * 
 * Impact Data Manager
 * 
 * Change Log:
 * 
 * 3/10/04 tcodes instead of zimpact_status_codes 8/22/06 support SQL Server for
 * null dates, use CASE stmt, MySQL uses if date = '0000-00-00' convert to ht
 * 
 */
public class ImpactPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructors
	 * 
	 **************************************************************************/

	public boolean copyOk() {
		debug("impact... okay to copy");
		return true;
	}

	public ImpactPlugin() throws services.ServicesException {
		super();
		this.setTableName("timpact");
		this.setKeyName("impact_id");
		this.setTargetTitle("Impacts");
		this.setListOrder("title_nm");
		
		this.setListHeaders(new String[] { "Reference", "Title", "Type",
				"Delay", "Level", "Approved" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes i on timpact.type_cd = i.code_value and i.code_type_id =  1 ",
						" left join tcodes delay on timpact.project_delay_cd = delay.code_value and delay.code_type_id =  3 ",
						" left join tcodes level on timpact.level_cd = level.code_value and level.code_type_id =  55 " });

		
		this
		.setMoreListColumns(new String[] { "reference_nm",
				"title_nm", "i.code_desc as type_desc",
				"delay.code_desc as delay",
				"level.code_desc as Level ",
				" if (timpact.reviewed_date =  '0000-00-00' , 'No' ,  'Yes' )  as Reviwed " });
		

		// mysql : this
		//.setMoreListColumns(new String[] {
			//	"reference_nm",
		//		"title_nm",
			//	"i.code_desc as type_desc",
			//	"delay.code_desc as delay",
			//	"level.code_desc as Level",
			//	" CASE isDate(timpact.reviewed_date) WHEN 1 THEN 'Yes' ELSE 'No' END as Reviewed " });

		
	}

	

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 */

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Ids
		 */

		ht.put("owner_id",
				new WebFieldSelect("owner_id", addMode ? new Integer("0")
						: (Integer) db.getObject("owner_id"), sm.getUserHT()));

		/*
		 * Codes
		 */

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("IMPACT")));

		ht.put("level_cd", new WebFieldSelect("level_cd", addMode ? "" : db
				.getText("level_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("project_delay_cd", new WebFieldSelect("project_delay_cd",
				addMode ? "" : db.getText("project_delay_cd"), sm
						.getCodes("YESNO")));

		ht.put("accepted_cd", new WebFieldSelect("accepted_cd", addMode ? ""
				: db.getText("accepted_cd"), sm.getCodes("YESNO")));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("impact_area", new WebFieldString("impact_area", (addMode ? ""
				: db.getText("impact_area")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("mitigation_blob", new WebFieldText("mitigation_blob",
				addMode ? "" : db.getText("mitigation_blob"), 3, 80));

		/*
		 * Return ht
		 */

		return ht;

	}

}
