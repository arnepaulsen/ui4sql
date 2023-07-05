/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import services.ExceptionWriter;
import services.ServicesException;
import db.DbFieldString;
import db.DbFieldInteger;

/**
 * 
 *   2/15 added mySql
 * */

/*******************************************************************************
 * Process Audit Plugin
 * 
 * Checks if the rules for a process associated with a project have been
 * violated.
 * 
 * 
 * 
 * When adding, calls services.WriteExceptions to check the acutal project
 * metrics as compared the process rule. Specifically:
 * 
 * compare rules vs. actuals for : program count interface count, fte count vs
 * staffing ramps ,function point counts dollar amount vs funding ramps
 * 
 * WriteExceptions has a method for each type of rule (maxPrograms, modules,
 * interfaces, etc). and returns an int [2] array with the allowed and actual
 * values for that rule type.
 * 
 * 
 * Change Log:
 * 
 * 
 ******************************************************************************/

public class AuditPlugin extends AbsProjectPlugin {

	public AuditPlugin() throws services.ServicesException {
		super();

		this.setDeleteOk(false);
		this.setTableName("taudit");
		this.setKeyName("audit_id");

		this.setTargetTitle("Compliance Audits");

		this.setHasDetailForm(true);
		this.setDetailTarget("Exception");
		this.setDetailTargetLabel("Exceptions");

		this.setListHeaders(new String[] { "Audit Date", "Stage", "Status" });

		this.setMoreListColumns(new  String[] {
				"left(taudit.added_date,10) as added_date",
				"stage.title_nm as StageName", "stat.code_desc as Status" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes stat on taudit.status_cd = stat.code_value and stat.code_type_id  = 5 ",
						" left join tuser owner on taudit.owner_id = owner.user_id ",
						" left join tstage stage on taudit.stage_id = stage.stage_id " });

		this.setMoreSelectJoins (new String[] {
				" left join tprocess on tproject.process_id = tprocess.process_id ",
				" left join tstage stage on taudit.stage_id = stage.stage_id " });

		this
				.setMoreSelectColumns(new String[] { "tprocess.title_nm as processName, stage.title_nm as stageName " });

	}

	

	/*
	 * Web Page Mapping
	 * 
	 */
	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		if (addMode) {
			this.setAddCustomFields(true);
			showNewPage(ht);
		} else {
			showUpdatePage(ht, parmMode);
		}
		return ht;

	}

	/*
	 * Add just ask's user to confirm... because a lot of work is about to
	 * begin.
	 */
	private void showNewPage(Hashtable ht) {

		ht
				.put(
						"message",
						new WebFieldDisplay("message",
								"Click Save-Edit to run the compliance audit, or Cancel to abort."));

		return;

	}

	public boolean beforeAdd(Hashtable ht) {

		// the constructor fetches the process rules attached to this project
		/*
		 * the ExceptionWrite puts the texception records, which as a parent key
		 * of the audit_id we haven't created yet... so need to predict it!!!
		 * WARNING;
		 * 
		 */

		String auditQuery = "select  audit_id from taudit  order  by audit_id DESC  limit 1";
		Integer auditKey = new Integer("0");

		try {
			auditKey = db.getColumnInt(auditQuery);
		} catch (ServicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int newKey = auditKey.intValue();
		newKey++;

		ExceptionWriter ew = new ExceptionWriter(this.db, this.sm, sm
				.getProjectId(), new Integer(newKey));

		ht.put("stage_id", new DbFieldInteger("stage_id", ew.getStageId()));

		/*
		 * Check the deliverrables are present for one required in this stage
		 */

		ht.put("deliverable_exception_flag", new DbFieldString(
				"deliverable_exception_flag",
				(ew.auditDeliverables() == true) ? "N" : "Y"));

		/*
		 * Check the interface count (true means ok, no error)
		 */
		ht.put("interface_exception_flag", new DbFieldString(
				"interface_exception_flag",
				(ew.auditInterfaces() == true) ? "N" : "Y"));

		/*
		 * Check the module count
		 */
		ht.put("module_exception_flag", new DbFieldString(
				"module_exception_flag", (ew.auditModules() == true) ? "N"
						: "Y"));

		/*
		 * Check the effort count
		 */

		ht.put("effort_exception_flag",
				new DbFieldString("effort_exception_flag",
						(ew.auditEffort() == true) ? "N" : "Y"));

		/*
		 * Check the function poinst
		 */

		ht.put("function_point_exception_flag", new DbFieldString(
				"function_point_exception_flag",
				(ew.auditFunctionPoints() == true) ? "N" : "Y"));

		/*
		 * Check the function poinst
		 */

		ht.put("data_exception_flag", new DbFieldString("data_exception_flag",
				(ew.auditStructures() == true) ? "N" : "Y"));

		return true;

	}

	/*
	 * 
	 * This is always Update or Display, no need to worry about add ( no value
	 * in the dbInterface rs)
	 */
	private void showUpdatePage(Hashtable ht, String parmMode)
			throws services.ServicesException {

		/* save off the type_cd when processing updates */

		if (parmMode.equalsIgnoreCase("show")) {

			sm.setParentId(db.getInteger("audit_id"), db.getText("title_nm"));
		}

		/*
		 * 
		 * Integers
		 */

		ht.put("owner_id", new WebFieldSelect("owner_id", db
				.getInteger("owner_id"), sm.getUserHT()));

		ht.put("process_id", new WebFieldDisplay("process_id", db
				.getText("processName")));

		/*
		 * Codes
		 * 
		 */
		ht.put("status_cd", new WebFieldSelect("status_cd", db
				.getText("status_cd"), sm.getCodes("STATUS")));

		ht.put("stageName", new WebFieldDisplay("stageName", db
				.getText("stageName")));

		/*
		 * Text
		 */

		/*
		 * Display-Only
		 */

		ht.put("audit_date", new WebFieldDisplay("added_date", db
				.getText("added_date")));

		ht.put("audit_seq_no", new WebFieldDisplay("audit_seq_no", db
				.getText("audit_id")));

		ht.put("interface_exception_flag",
				new WebFieldDisplay("interface_exception_flag",
						db.getText("interface_exception_flag")
								.equalsIgnoreCase("Y") ? "Yes" : "No"));

		ht.put("module_exception_flag", new WebFieldDisplay(
				"module_exception_flag", db.getText("module_exception_flag")
						.equalsIgnoreCase("Y") ? "Yes" : "No"));

		ht.put("effort_exception_flag", new WebFieldDisplay(
				"effort_exception_flag", db.getText("effort_exception_flag")
						.equalsIgnoreCase("Y") ? "Yes" : "No"));

		ht.put("function_point_exception_flag", new WebFieldDisplay(
				"function_point_exception_flag",
				db.getText("function_point_exception_flag").equalsIgnoreCase(
						"Y") ? "Yes" : "No"));

		ht.put("data_exception_flag", new WebFieldDisplay(
				"data_exception_flag", db.getText("data_exception_flag")
						.equalsIgnoreCase("Y") ? "Yes" : "No"));

		ht.put("deliverable_exception_flag",
				new WebFieldDisplay("deliverable_exception_flag",
						db.getText("deliverable_exception_flag")
								.equalsIgnoreCase("Y") ? "Yes" : "No"));

		/*
		 * blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", db
				.getText("desc_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", db
				.getText("notes_blob"), 3, 80));

		ht.put("resolution_blob", new WebFieldText("resolution_blob", db
				.getText("resolution_blob"), 3, 80));

		/*
		 * dates
		 */

	}

}
