/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Measurement Plugin
 * 
 */
public class ModulePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/
	public ModulePlugin() throws services.ServicesException {
		super();
		this.setTableName("tmodule");
		this.setKeyName("module_id");
		this.setTargetTitle("Module");

		this.setHasDetailForm (true); // detail is the Errors form
		this.setDetailTarget ("Usage");
		this.setDetailTargetLabel ("Usages");

		this.setListHeaders( new String[] { "Name", "Reference", "Type",
				"Language" });

		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm",
				"kind.code_desc as module_type",
				"lang.code_desc as module_lang" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes lang on tmodule.language_cd = lang.code_value and lang.code_type_id =  26 ",
				" left join tcodes kind on tmodule.type_cd = kind.code_value and kind.code_type_id =  57 " });

		this.setMoreSelectJoins (this.moreListJoins);

	}

	/*
	 * Authorizations
	 */

	public boolean copyOk() {
		return true;
	}

	/***************************************************************************
	 * List Page Selectors column 2 = type 3 = language
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2) {
			// filter on module type
			WebFieldSelect wf = new WebFieldSelect("FilterType", (sm.Parm(
					"FilterType").length() == 0 ? "" : sm.Parm("FilterType")),
					sm.getCodes("MODULETYPE"), "-All Types-");
				wf.setDisplayClass("listform");
			return wf;

		} else {
			// must be 3, which is the language filter
			WebFieldSelect wf = new WebFieldSelect("FilterLanguage", sm
					.Parm("FilterLanguage"), sm.getCodes("LANGUAGE"),
					"-All Languages-");
			wf.setDisplayClass("listform");
			return wf;
		}
	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		debug("Module ... getListAnd");
		
		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present

		debug("module filter type : " + sm.Parm("FilterType"));
		debug("module filter lang : " + sm.Parm("FilterLanguage"));
		
		if (!sm.Parm("FilterType").equalsIgnoreCase("0")
				&& sm.Parm("FilterType").length() > 0) {
			sb.append(" AND kind.code_value = '" + sm.Parm("FilterType") + "'");
		}

		if ((!sm.Parm("FilterLanguage").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterLanguage").length() > 0)) {
			sb.append(" AND lang.code_value = '" + sm.Parm("FilterLanguage")
					+ "'");
		}

		debug("where : " + sb.toString());
		
		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("module_id"), db.getText("title_nm"));
		}

		/*
		 * Ids
		 */

		String query = "select title_nm as odor, job_id, title_nm from tjob where application_id = "
				+ sm.getApplicationId().toString();

		Hashtable jobs = sm.getTable("tjob", query);

		ht.put("job_id", new WebFieldSelect("job_id",
				addMode ? new Integer("0") : db.getInteger("job_id"), jobs));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 128));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * Codes
		 */
		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("MODULESTATUS")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("MODULETYPE")));

		ht.put("language_cd", new WebFieldSelect("language_cd", addMode ? ""
				: db.getText("language_cd"), sm.getCodes("LANGUAGE")));

		/*
		 * Dates
		 */
		ht.put("status_date", new WebFieldDate("status_date", (addMode ? ""
				: db.getText("status_date"))));

		/*
		 * Blobs
		 */
		ht.put("blob_desc", new WebFieldText("blob_desc", addMode ? "" : db
				.getText("blob_desc"), 5, 100));

		ht.put("blob_process_logic", new WebFieldText("blob_process_logic",
				(addMode ? "" : db.getText("blob_process_logic")), 5, 100));

		ht.put("blob_inputs", new WebFieldText("blob_inputs", (addMode ? ""
				: db.getText("blob_inputs")), 5, 100));

		ht.put("blob_outputs", new WebFieldText("blob_outputs", (addMode ? ""
				: db.getText("blob_outputs")), 5, 100));

		ht.put("blob_bugs", new WebFieldText("blob_bugs", (addMode ? "" : db
				.getText("blob_bugs")), 5, 100));

		ht.put("blob_exceptions", new WebFieldText("blob_exceptions",
				(addMode ? "" : db.getText("blob_exceptions")), 5, 100));

		return ht;

	}

}
