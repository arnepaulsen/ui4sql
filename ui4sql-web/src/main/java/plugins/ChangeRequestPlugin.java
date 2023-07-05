/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/*******************************************************************************
 * Change Request Plugin
 * 
 * 3/22 New Page
 * 
 */

public class ChangeRequestPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private static String[] sListHeaders = { "Title", "Status", "Work Effort", "Impact" };

	private static String[] extraListColumns = { "title_nm",
			"code_desc as status_desc", "effort_days_tx", "schedule_days_tx" };

	private static String[] extraListJoins = { " left join tcodes on tchange_request.status_cd = tcodes.code_value and tcodes.code_type_id  = 73 " };

	public ChangeRequestPlugin() throws services.ServicesException {
		super();
		this.setTableName("tchange_request");
		this.setTargetTitle("Change Request");
		this.setKeyName("change_req_id");

		this.setMoreListColumns (extraListColumns);
		this.setMoreListJoins (extraListJoins);
		this.setListHeaders (sListHeaders);
	}
	

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
		
		Hashtable ht = new Hashtable();

		/*
		 * Codes
		 */
		ht.put ("status_cd", new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("CRSTATUS")));

		ht.put ("priority_cd", new WebFieldSelect("priority_cd",
				addMode ? "" : db.getText("priority_cd"), sm
						.getCodes("PRIORITY")));

		/*
		 * Id's
		 */

		ht.put ("initiated_id",  new WebFieldSelect("initiated_id",
				addMode ? new Integer("0") : db.getInteger("initiated_id"), sm
						.getUserHT()));

		/*
		 * Strings
		 */
		ht.put ("reference_nm", new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32));

		ht.put ("change_req_nm",  new WebFieldString("change_req_nm",
				(addMode ? "" : db.getText("change_req_nm")), 32, 32));

		ht.put ("initiation_ref_nm",  new WebFieldString("initiation_ref_nm",
				(addMode ? "" : db.getText("initiation_ref_nm")), 32, 32));

		ht.put ("title_nm",  new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32));

		ht.put ("effort_days_tx",  new WebFieldString("effort_days_tx",
				(addMode ? "" : db.getText("effort_days_tx")), 32, 32));

		ht.put ("schedule_days_tx", new WebFieldString("schedule_days_tx",
				(addMode ? "" : db.getText("schedule_days_tx")), 32, 32));

		/*
		 * Dates
		 */
		ht.put ("required_date", new WebFieldDate("required_date", addMode ? ""
				: db.getText("required_date")));

		ht.put ("received_date",  new WebFieldDate("received_date", addMode ? ""
				: db.getText("received_date")));

		/*
		 * Blobs
		 */
		ht.put ("blob_disposition_notes",  new WebFieldText("blob_disposition_notes",
				addMode ? "" : db.getText("blob_disposition_notes"), 3, 60));

		ht.put ("blob_change_desc",  new WebFieldText("blob_change_desc", addMode ? ""
				: db.getText("blob_change_desc"), 5, 100));

		ht.put ("alternative_blob",  new WebFieldText("alternative_blob",
				addMode ? "" : db.getText("alternative_blob"), 5, 100));

		ht.put ("solution_blob",  new WebFieldText("solution_blob",
				addMode ? "" : db.getText("solution_blob"), 5, 100));

		/*
		 * Return
		 */
		
		return ht;

		

	}

}
