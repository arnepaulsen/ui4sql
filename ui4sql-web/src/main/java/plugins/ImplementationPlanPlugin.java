/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Implementation Plan
 * 
 * 
 */
public class ImplementationPlanPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ImplementationPlanPlugin() throws services.ServicesException {
		super();
		this.setTableName("timplementation_plan");
		this.setKeyName("plan_id");
		this.setTargetTitle("Implementation Plan");

		this.setListHeaders(new String[] { "Reference", "Title", "Date",
				"Type", "Status", "Application" });

		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"start_dttm", "imp_type.code_desc as type_desc",
				"stat_cd.code_desc as stat_desc", "application_name" });

		this
				.setMoreListJoins(new String[] {
						" left join tapplications on timplementation_plan.application_id = tapplications.application_id ",
						" left join tcodes imp_type on timplementation_plan.type_cd = imp_type.code_value and imp_type.code_type_id  = 46 ",
						" left join tcodes stat_cd on timplementation_plan.status_cd = stat_cd.code_value and stat_cd.code_type_id  = 47 " });
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Ids
		 */

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("owner_uid"), sm.getUserHT(), true));

		ht
				.put("application_id", new WebFieldSelect("application_id",
						addMode ? sm.getApplicationId() : (Integer) db
								.getObject("application_id"), sm
								.getApplicationFilter()));

		/*
		 * Dates
		 */

		ht.put("start_dttm", new WebFieldDate("start_dttm", addMode ? "" : db
				.getText("start_dttm")));

		ht.put("end_dttm", new WebFieldDate("end_dttm", addMode ? "" : db
				.getText("end_dttm")));

		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 64));

		/*
		 * Codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("IMPLSTAT")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "R" : db
				.getText("type_cd"), sm.getCodes("IMPLEMENT")));

		ht.put("risk_cd", new WebFieldSelect("risk_cd", addMode ? "R" : db
				.getText("risk_cd"), sm.getCodes("HIGHMEDLOW")));

		/*
		 * Numbers
		 */
		ht.put("outage_hours_no", new WebFieldString("outage_hours_no",
				addMode ? "R" : db.getText("outage_hours_no"), 4, 4));

		/*
		 * Blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 80));

		ht.put("business_blob", new WebFieldText("business_blob", addMode ? ""
				: db.getText("business_blob"), 5, 80));

		ht.put("impacts_blob", new WebFieldText("impacts_blob", addMode ? ""
				: db.getText("impacts_blob"), 5, 80));

		ht.put("scope_blob", new WebFieldText("scope_blob", addMode ? "" : db
				.getText("scope_blob"), 5, 80));

		ht.put("support_blob", new WebFieldText("support_blob", addMode ? ""
				: db.getText("support_blob"), 5, 80));

		ht.put("validation_blob", new WebFieldText("validation_blob",
				addMode ? "" : db.getText("validation_blob"), 5, 80));

		ht.put("backout_blob", new WebFieldText("backout_blob", (addMode ? ""
				: db.getText("backout_blob")), 5, 80));

		ht.put("contingency_blob", new WebFieldText("contingency_blob",
				(addMode ? "" : db.getText("contingency_blob")), 5, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 80));

		ht.put("followup_blob", new WebFieldText("followup_blob", (addMode ? ""
				: db.getText("followup_blob")), 5, 80));

		/*
		 * Return
		 */

		return ht;

	}
}
