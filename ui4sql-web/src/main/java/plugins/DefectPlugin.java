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
 * 5/19/05 Take out getDbFields!! 1/13/07 key in not auto-increment (as in from
 * external system)
 * 
 * 
 */
public class DefectPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public DefectPlugin() throws services.ServicesException {
		super();
		this.setTableName("tdefect");
		this.setKeyName("defect_id");
		this.setTargetTitle("Test Defect");

		this.setKeyAutoIncrement(false);

		this.setMoreListColumns(new String[] { "tdefect.defect_id", "title_nm",
				"s.code_desc as status_desc", "p.code_desc as priority_desc",
				"concat(u.last_name, ',', u.first_name)",
				"s.code_value", "p.code_value", "tdefect.assigned_uid" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes s on tdefect.status_cd = s.code_value and s.code_type_id  = 35 ",
						" left join tcodes p on tdefect.priority_cd = p.code_value and p.code_type_id  = 112 ",
						" left join tuser u on tdefect.assigned_uid = u.user_id " });

		this
				.setMoreSelectJoins(new String[] { " left join ttestcase tc on tdefect.testcase_id = tc.testcase_id  " });
		this
				.setMoreSelectColumns(new String[] { "tc.title_nm as testcase_title" });

		this.setListHeaders(new String[] { "Defect&nbsp;#", "Title", "Status",
				"Priority", "Owner" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
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

		if (columnNumber == 2) {
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "O" : sm
					.Parm("FilterStatus")), sm.getCodes("DEFECTSTATUS"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		} else {
			if (columnNumber == 3) {

				WebFieldSelect wf = new WebFieldSelect("FilterPriority", sm
						.Parm("FilterPriority"), sm.getCodes("SEVERITY4"),
						"All Severity");
				wf.setDisplayClass("listform");
				return wf;
			} else {
				debug("building wfSelect for Users:");
				debug("parm userid : " + sm.Parm("FilterUser"));

				WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
						"FilterUser").length() == 0 ? new Integer("0")
						: new Integer(sm.Parm("FilterUser"))), sm.getUserHT(),
						"All Users");
				wf.setDisplayClass("listform");
				return wf;

			}
		}

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

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

		if ((!sm.Parm("FilterPriority").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterPriority").length() > 0)) {
			sb
					.append(" AND p.code_value = '" + sm.Parm("FilterPriority")
							+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND tdefect.assigned_uid = " + sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public boolean copyOk() {
		return false;
	}

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
		// boolean showMode = parmMode.equalsIgnoreCase("show") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * Ids
		 */

		Hashtable testCases = db.getLookupTable("ttestcase", "testcase_id",
				"title_nm");

		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getApplicationId() : (Integer) db
						.getObject("application_id"),
				sm.getApplicationFilter(), true));

		ht.put("testcase_id", new WebFieldSelect("testcase_id",
				addMode ? new Integer("0") : db.getInteger("testcase_id"),
				testCases));

		ht.put("closed_uid", new WebFieldSelect("closed_uid",
				addMode ? new Integer("0") : db.getInteger("closed_uid"), sm
						.getUserHT(), true, true));

		ht
				.put("assigned_uid", new WebFieldSelect("assigned_uid",
						addMode ? new Integer("0") : (Integer) db
								.getObject("assigned_uid"), sm.getUserHT(),
						true, true));

		/*
		 * Strings
		 */

		ht.put("defect_id", new WebFieldString("defect_id", (addMode ? "" : db
				.getText("defect_id")), 8, 8));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 128));

		/*
		 * Codes
		 */
		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("SEVERITY4")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("DEFECTSTATUS")));

		/*
		 * Dates
		 */
		ht.put("closed_date", new WebFieldDate("closed_date", (addMode ? ""
				: db.getText("closed_date"))));
		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				addMode ? "" : db.getText("resolution_blob"), 3, 80));

		ht.put("root_cause_blob", new WebFieldText("root_cause_blob",
				addMode ? "" : db.getText("root_cause_blob"), 3, 80));

		return ht;

	}

}
