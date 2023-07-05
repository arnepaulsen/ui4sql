/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Patient Safety
 * 
 * Keywords:
 * 
 * 
 * Change Log:
 * 
 * Point to same tincident table as IncidentPlugin.. but manage different elements
 * 
 * 
 */
public class SafetyPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public SafetyPlugin() throws services.ServicesException {
		super();
		this.setTableName("tincident");
		this.setKeyName("incident_id");
		this.setTargetTitle("Patient Safety Incident");

		this.setListHeaders( new String[] { "PR#", "Title",  "Status", "Type",
				"Owner", "Suite" });

		this.setMoreListJoins(new  String[] {
				" left join tapplications on tincident.application_id = tapplications.application_id ",
				" left join tcodes s on tincident.status_cd = s.code_value and s.code_type_id  = 45 ",
				" left join tcodes t on tincident.type_cd = t.code_value and t.code_type_id  = 99 ",
				" left join tuser u on tincident.owner_uid = u.user_id " });

		this.setMoreListColumns(new  String[] {"incident_id", "tincident.title_nm",
				 "s.code_desc as status_desc",
				"t.code_desc as type_desc",
				"concat(u.last_name, ',', u.first_name)",
				" tapplications.application_name as theSuite " });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Filters
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// only the status column (#2)
		if (columnNumber == 2)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
				"FilterStatus").length() == 0 ? "O" : sm.Parm("FilterStatus")),
				sm.getCodes("ISSUESTAT"), "All Status");
			wf.setDisplayClass("listform");
		return wf;

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();
		
		sb.append(" AND patient_safety_flag = 'y' ");

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND s.code_value = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable <String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Local look-up tables
		 */

		String[][] envs = { { "T", "P", "O" }, { "Test", "Production", "Other" } };

		/*
		 * Ids
		 */

		Hashtable <String, WebField> ht = new Hashtable <String, WebField>();

		/*
		 * Ids
		 */

	
		
		ht.put("owner_uid",
				new WebFieldSelect("owner_uid", addMode ? new Integer("0")
						: (Integer) db.getObject("owner_uid"), sm.getUserHT(),
						false, true));

		if (true)
		ht.put("contact_uid", new WebFieldSelect("contact_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("contact_uid"), sm.getContactHT(), false,
				true));

		if (true)
		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getApplicationId() : db
						.getInteger("application_id"), sm
						.getApplicationFilter()));

		/*
		 * Strings
		 */
		ht.put("incident_id", new WebFieldString("incident_id", (addMode ? ""
				: db.getText("incident_id")), 8, 8));

		ht.put("rfc_id", new WebFieldString("rfc_id", (addMode ? ""
				: db.getText("rfc_id")), 8, 8));
		
		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 100, 127));
		
		ht.put("help_tx", new WebFieldString("help_tx", (addMode ? "" : db
				.getText("help_tx")), 100, 127));
		

		/*
		 * Dates
		 */
		ht.put("start_date", new WebFieldDate("start_date", addMode ? ""
				: db.getText("start_date")));

		ht.put("end_date", new WebFieldDate("end_date", addMode ? "" : db
				.getText("end_date")));

		ht.put("target_date", new WebFieldDate("target_date", addMode ? "" : db
				.getText("target_date")));

		/*
		 * Codes
		 */

		
		
		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		ht.put("complexity_cd", new WebFieldSelect("complexity_cd", addMode ? ""
				: db.getText("complexity_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("SAFETY")));

		ht.put("cause_found_flag", new WebFieldSelect(
				"cause_found_flag", addMode ? "N" : db
						.getText("cause_found_flag"), sm
						.getCodes("YESNO")));

		ht.put("cause_fixed_flag", new WebFieldSelect(
				"cause_fixed_flag", addMode ? "N" : db
						.getText("cause_fixed_flag"), sm
						.getCodes("YESNO")));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				(addMode ? "" : db.getText("resolution_proc_blob")), 5, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 100));

		ht.put("followup_blob", new WebFieldText("followup_blob", (addMode ? ""
				: db.getText("followup_blob")), 5, 100));

		ht.put("cleanup_blob", new WebFieldText("cleanup_blob", (addMode ? ""
				: db.getText("cleanup_blob")), 5, 100));

		ht.put("workaround_blob", new WebFieldText("workaround_blob",
				(addMode ? "" : db.getText("workaround_blob")), 5, 100));

		/*
		 * Return ht
		 */

		return ht;

	}

}
