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
 */
public class CyclePlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public CyclePlugin() throws services.ServicesException {
		super();
		this.setTableName("tcycle");
		this.setKeyName("cycle_id");
		this.setTargetTitle("Test Cycle");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Test");
		this.setDetailTargetLabel("Tests");

		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"s.code_desc as status_desc",
				"concat(u.last_name, ',', u.first_name)",
				"s.code_value" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes s on tcycle.status_cd = s.code_value and s.code_type_id  = 93 ",
						" left join tuser u on tcycle.assigned_uid = u.user_id " });

		this.setListHeaders(new String[] { "Reference", "Title", "Status",
				"Test Lead" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
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

		// default the status to open when starting new list
		WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
				"FilterStatus").length() == 0 ? "" : sm.Parm("FilterStatus")),
				sm.getCodes("STATOIP"), "All Status");
		wf.setDisplayClass("listform");
		return wf;

	}

	public String getListAnd() {
		/*
		 * Only filtering on status
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			// sb.append(" AND s.code_value = 'O'");
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
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	@SuppressWarnings("unchecked")
	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		// save key info
		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("cycle_id"), db.getText("title_nm"));
		}

		/*
		 * Ids
		 */

		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));

		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("logical_day_nm", new WebFieldString("logical_day_nm",
				(addMode ? "" : db.getText("logical_day_nm")), 2, 2));

		ht.put("simulation_date", new WebFieldDate("simulation_date",
				(addMode ? "" : db.getText("simulation_date"))));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 128));

		/*
		 * Codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("STATOIP")));

		ht.put("phase_cd", new WebFieldSelect("phase_cd", addMode ? "O" : db
				.getText("phase_cd"), sm.getCodes("TESTPHASE")));

		/*
		 * Dates
		 */
		ht.put("plan_start_date", new WebFieldDate("plan_start_date",
				(addMode ? "" : db.getText("plan_start_date"))));

		ht.put("actual_start_date", new WebFieldDate("actual_start_date",
				(addMode ? "" : db.getText("actual_start_date"))));

		ht.put("plan_end_date", new WebFieldDate("plan_end_date", (addMode ? ""
				: db.getText("plan_end_date"))));

		ht.put("actual_end_date", new WebFieldDate("actual_end_date",
				(addMode ? "" : db.getText("actual_end_date"))));
		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		ht.put("objectives_blob", new WebFieldText("objectives_blob",
				addMode ? "" : db.getText("objectives_blob"), 3, 80));

		return ht;

	}

}
