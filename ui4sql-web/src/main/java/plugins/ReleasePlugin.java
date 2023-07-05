/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Release Plugin
 * 
 */
/*******************************************************************************
 * Release Data Manager
 * 
 * Change Log:
 * 
 * 2/15 added mySql 5/8/05 Extentded from AbsApplicationPlugin
 * 
 * 
 ******************************************************************************/
public class ReleasePlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ReleasePlugin() throws services.ServicesException {
		super();
		this.setTableName("trelease");
		this.setKeyName("release_id");
		this.setTargetTitle("Releases ");
		this.setShowAuditSubmitApprove(false);
		this.setListOrder ("install_date, trelease.reference_nm");
		this.setUpdatesLevel("executive");
				

		this.setListHeaders( new String[] { "Reference", "Title","Testing", "Review By",
				"Install", "Status", "Type", "Outage Hours" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes stat on trelease.status_cd = stat.code_value and stat.code_type_id = 22 ",
				" left join tcodes rlse on trelease.type_cd = rlse.code_value and rlse.code_type_id = 23 " });

		this.setMoreListColumns(new  String[] { "trelease.reference_nm", "trelease.title_nm",
				dbprefix + "FormatDatetime(test_start_date , 'mm/dd/yy') as test_start_date",
				dbprefix + "FormatDatetime(last_review_date , 'mm/dd/yy') as reviewDate",
				dbprefix + "FormatDatetime(install_date , 'mm/dd/yy') as installDate",
				"stat.code_desc as StatDesc", "rlse.code_desc as TypeDesc",
				"outage_hours_no" , "install_date"});

	}

	
	/*
	 * List Selector Controls
	 */

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 5 || columnNumber == 6)
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

		switch (columnNumber) {

		case 5: {
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", sm
					.Parm("FilterStatus"), sm.getCodes("RLSESTATUS"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		}
		default: {
			WebFieldSelect wf = new WebFieldSelect("FilterType", sm
					.Parm("FilterType"), sm.getCodes("RLSETYPE"), "All Types");
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

		if ((!sm.Parm("FilterStatus").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterStatus").length() > 0)) {
			sb.append(" AND stat.code_value = '" + sm.Parm("FilterStatus")
					+ "'");
		}

		if ((!sm.Parm("FilterType").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterType").length() > 0)) {
			sb.append(" AND rlse.code_value = '" + sm.Parm("FilterType") + "'");
		}

		return sb.toString();

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

		String query = "select title_nm, release_id, title_nm from trelease "
				+ " join tapplications on trelease.application_id = tapplications.application_id "
				+ " join tdivision on tapplications.division_id = tdivision.division_id and tdivision.division_id = "
				+ sm.getDivisionId().toString() + " ";

		ht.put("super_id", new WebFieldSelect("super_id",
				addMode ? new Integer("0") : db.getInteger("super_id"), sm
						.getTable("trelease", query)));
		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 100));

		
		ht.put("bsns_days_tx", new WebFieldString("bsns_days_tx", (addMode ? "" : db
				.getText("bsns_days_tx")), 5, 5));

		
		/*
		 * Numbers
		 */
		ht.put("outage_hours_no", new WebFieldString("outage_hours_no",
				(addMode ? "" : db.getText("outage_hours_no")), 4, 4));
		/*
		 * Dates
		 */
		ht.put("install_date", new WebFieldDate("install_date", addMode ? ""
				: db.getText("install_date")));

		ht.put("test_start_date", new WebFieldDate("test_start_date",
				addMode ? "" : db.getText("test_start_date")));

		ht.put("last_review_date", new WebFieldDate("last_review_date",
				addMode ? "" : db.getText("last_review_date")));

		ht.put("test_end_date", new WebFieldDate("test_end_date",
				addMode ? "" : db.getText("test_end_date")));

		ht.put("test_install_date", new WebFieldDate("test_install_date",
				addMode ? "" : db.getText("test_install_date")));

		ht.put("code_receipt_date", new WebFieldDate("code_receipt_date",
				addMode ? "" : db.getText("code_receipt_date")));

		ht.put("release_note_rv_start_date", new WebFieldDate("release_note_rv_start_date",
				addMode ? "" : db.getText("release_note_rv_start_date")));

		ht.put("release_note_rv_end_date", new WebFieldDate("release_note_rv_end_date",
				addMode ? "" : db.getText("release_note_rv_end_date")));
				
		ht.put("test_frz_start_date", new WebFieldDate("test_frz_start_date",
				addMode ? "" : db.getText("test_frz_start_date")));
		
		ht.put("test_frz_end_date", new WebFieldDate("test_frz_end_date",
				addMode ? "" : db.getText("test_frz_end_date")));
		
		
		
		/*
		 * Ids
		 */

		ht.put("manager_uid", new WebFieldSelect("manager_uid",
				addMode ? new Integer("0") : db.getInteger("manager_uid"), sm
						.getUserHT(), true, true));

		ht.put("technician_uid", new WebFieldSelect("technician_uid",
				addMode ? new Integer("0") : db.getInteger("technician_uid"),
				sm.getUserHT(), true, true));

		ht.put("operations_uid", new WebFieldSelect("operations_uid",
				addMode ? new Integer("0") : db.getInteger("operations_uid"),
				sm.getUserHT(), true, true));

		/*
		 * Codes
		 */
		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("RLSESTATUS")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("RLSETYPE")));

		String[][] aPlan = { { "P", "E" }, { "Planned", "Emergency" } };
		
		ht.put("plan_emerg_cd", new WebFieldSelect("plan_emerg_cd", addMode ? "" : db
				.getText("plan_emerg_cd"), aPlan));

		ht.put("outage_cd", new WebFieldSelect("outage_cd", addMode ? "" : db
				.getText("outage_cd"), sm.getCodes("YESNO")));

		ht.put("test_env_cd", new WebFieldSelect("test_env_cd", addMode ? "New"
				: db.getText("test_env_cd"), sm.getCodes("TESTREG")));

		/*
		 * Blobs
		 */
		ht.put("blob_desc", new WebFieldText("blob_desc", addMode ? "" : db
				.getText("blob_desc"), 5, 100));

		ht.put("blob_install_notes", new WebFieldText("blob_install_notes",
				(addMode ? "" : db.getText("blob_install_notes")), 5, 100));

		ht.put("blob_install_problems", new WebFieldText(
				"blob_install_problems", (addMode ? "" : db
						.getText("blob_install_problems")), 5, 100));

		return ht;
	}
}
