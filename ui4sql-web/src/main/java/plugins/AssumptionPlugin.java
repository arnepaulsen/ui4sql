/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * 
 *   2/15 added mySql
 * 	3/11 add join on project Permissions
 * */

/**
 * ProjectTableManager
 * 
 * Glue that the FormDriver uses to connect: the web ProjectFormTemplate.jsp to
 * the database WebFields are used to popuate the jsp, and DbFields are used to
 * pull the input from the form and push to the database.
 */
public class AssumptionPlugin extends AbsProjectPlugin {

	// *******************
	// CONSTRUCTOR
	// *******************

	public AssumptionPlugin() throws services.ServicesException {
		super();
		this.setTableName("tassumption");
		this.setKeyName("assumption_id");
		this.setTargetTitle("Assumption");

		this.setListHeaders(new String[] { "Title", "Reference", "Confirmed",
				"Rank", "Impacts<br>Delivery", "Impacts<br>Costs", "Owner" });

		this
				.setMoreListColumns(new String[] { "title_nm", "reference_nm",
						"confirm.code_desc as confirm_yn",
						"priority.code_desc", "time.code_desc as time_yn",
						"costs.code_desc as costs_yn",
						"concat(own.last_name, ',', own.first_name) as theOwner" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes confirm on tassumption.confirmed_cd = confirm.code_value and confirm.code_type_id  = 12  ",
						" left join tcodes time on tassumption.time_cd = time.code_value and time.code_type_id  = 12  ",
						" left join tcodes costs on tassumption.costs_cd = costs.code_value and costs.code_type_id  = 12  ",
						" left join tcodes priority on tassumption.priority_cd = priority.code_value and priority.code_type_id  = 81  ",
						" left join tuser own on tassumption.owner_uid = own.user_id " });

	}

	// *********************************
	// Web Fields for Display
	// *********************************

	// center the confirmed flag
	public boolean getListColumnCenterOn(int x) {
		if (x == 2 || x == 3 || x == 4 || x == 5)
			return true;
		else
			return false;
	}

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Ids
		 */

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));

		/*
		 * Flags / codes
		 */

		ht.put("confirmed_cd", new WebFieldSelect("confirmed_cd", addMode ? ""
				: db.getText("confirmed_cd"), sm.getCodes("YESNO")));

		ht.put("costs_cd", new WebFieldSelect("costs_cd", addMode ? "" : db
				.getText("costs_cd"), sm.getCodes("YESNO")));

		ht.put("time_cd", new WebFieldSelect("time_cd", addMode ? "" : db
				.getText("time_cd"), sm.getCodes("YESNO")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("RANK10")));

		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 32, 32));

		/*
		 * Blob
		 */

		ht.put("impact_blob", new WebFieldText("impact_blob", addMode ? "" : db
				.getText("impact_blob"), 3, 80));

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("outcome_blob", new WebFieldText("outcome_blob", addMode ? ""
				: db.getText("outcome_blob"), 3, 80));

		ht.put("contingency_blob", new WebFieldText("contingency_blob",
				addMode ? "" : db.getText("contingency_blob"), 3, 80));

		return ht;

	}

}
