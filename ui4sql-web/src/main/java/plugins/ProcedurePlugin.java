/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Job Plugin
 * 
 */
public class ProcedurePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ProcedurePlugin() throws services.ServicesException {
		super();
		this.setTableName("tprocedure");
		this.setKeyName("procedure_id");
		this.setTargetTitle("Procedure");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Step");
		this.setDetailTargetLabel("Steps");

		this.setListHeaders(new String[] { "Reference", "Title", "Type",
				"As-Of" });

		// fix convert char(11) 10/17/09 for mySQL
		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"tt.code_desc as type_cod",
				"convert(as_of_date, char(11)) as as_of_dt" });

		this
				.setMoreListJoins(new String[] { " left join tcodes tt on tprocedure.type_cd = tt.code_value and tt.code_type_id  = 115" });

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

		sm.setStepKind("Procedure"); // leave a cookie so the Step manager
		// knows what kind of a step to take

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("proc_desc_blob", new WebFieldText("proc_desc_blob",
				addMode ? "" : db.getText("proc_desc_blob"), 5, 100));

		ht.put("alternate_proc_blob", new WebFieldText("alternate_proc_blob",
				(addMode ? "" : db.getText("alternate_proc_blob")), 5, 100));

		ht.put("notification_blob", new WebFieldText("notification_blob",
				(addMode ? "" : db.getText("notification_blob")), 5, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 100));

		ht.put("trigger_blob", new WebFieldText("trigger_blob", (addMode ? ""
				: db.getText("trigger_blob")), 5, 100));

		ht.put("as_of_date", new WebFieldDate("as_of_date", addMode ? "" : db
				.getText("as_of_date")));

		/*
		 * codes
		 */

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("PROCEDTYPE")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("JOBSTATUS")));

		ht.put("frequency_cd", new WebFieldSelect("frequency_cd", addMode ? ""
				: db.getText("frequency_cd"), sm.getCodes("FREQUENCY")));

		return ht;

	}

}
