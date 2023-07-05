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
public class AlertPlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTOR
	// *******************

	public AlertPlugin() throws services.ServicesException {
		super();
		this.setTableName("talert");
		this.setKeyName("alert_id");

		this.setTargetTitle("Alert");

		this.setListSelectorColumnFlags(new boolean[] { false, false, true,
				true, true, false, false });

		this.setListHeaders(new String[] { "Reference", "Title", "Status",
				"Production", "Patient<br>Safety", "Owner" });

		this
				.setMoreListColumns(new String[] { "talert.reference_nm",
						"talert.title_nm", "status.code_desc as confirm_yn",
						"production.code_desc as production_yn",
						"safety.code_desc as safety_yn",
						"concat(own.last_name, ',', own.first_name) as theOwner" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes safety on talert.safety_cd = safety.code_value and safety.code_type_id  = 3  ",
						" left join tcodes production on talert.production_cd = production.code_value and production.code_type_id  = 3  ",
						" left join tcodes status on talert.status_cd = status.code_value and status.code_type_id  = 58  ",
						" left join tcodes priority on talert.priority_cd = priority.code_value and priority.code_type_id  = 81  ",
						" left join tuser own on talert.owner_uid = own.user_id " });

	}

	/*
	 * List Selectors
	 * 
	 */

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND status.code_value = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND status.code_value = '"
						+ sm.Parm("FilterStatus") + "'");
			}
		}

		return sb.toString();

	}

	public WebField getListSelector(int columnNumber) {

		// default the status to open when starting new list
		WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
				"FilterStatus").length() == 0 ? "O" : sm.Parm("FilterStatus")),
				sm.getCodes("OPENCLOSE"), "All Status");
		wf.setDisplayClass("listform");
		return wf;

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

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("OPENCLOSE")));

		ht.put("production_cd", new WebFieldSelect("production_cd",
				addMode ? "" : db.getText("production_cd"), sm
						.getCodes("YESNO")));

		ht.put("safety_cd", new WebFieldSelect("safety_cd", addMode ? "" : db
				.getText("safety_cd"), sm.getCodes("YESNO")));

		/*
		 * Dates
		 */

		ht.put("begin_dttm", new WebFieldDate("begin_dttm", addMode ? "" : db
				.getText("begin_dttm")));

		ht.put("end_dttm", new WebFieldDate("end_dttm", addMode ? "" : db
				.getText("end_dttm")));

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

	// *********************************
	// Web Fields for Display
	// *********************************

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 2)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	

}
