/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Issue Plugin
 * 
 * Change log:
 * 
 * 2/15 added mySql 3/13 as 'target' to list query 11/8 add phase to list page
 * 
 */
public class SizingPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public SizingPlugin() throws services.ServicesException {
		super();
		this.setTableName("tsizing");
		this.setKeyName("sizing_id");
		this.setTargetTitle("Project Sizing");

		this.setListHeaders(new String[] { "Title", "Department", "Phase",
				"UOM", "Size", "Review", "Total" });
		this.setMoreListColumns(new String[] { "title_nm", "dept_name",
				"l.code_desc as phase_d", "s.code_desc as UOM", "effort_flt",
				"review_flt", "effort_flt + review_flt" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes s on tsizing.qty_cd = s.code_value and s.code_type_id = 34 ",
						" left join tcodes l on tsizing.type_cd = l.code_value and l.code_type_id = 41 ",
						" left join tdepartment on tsizing.dept_id = tdepartment.dept_id" });
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		/*
		 * Id's
		 */
		ht.put("dept_id", new WebFieldSelect("dept_id", addMode ? new Integer(
				"0") : (Integer) db.getObject("dept_id"), getLookupTable(
				"tdepartment", "dept_id", "dept_name")));

		/*
		 * Codes
		 */
		ht.put("qty_cd", new WebFieldSelect("qty_cd", addMode ? "" : db
				.getText("qty_cd"), sm.getCodes("SIZING")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("LIFECYCLE")));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * Blobs
		 */

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 80));

		/*
		 * Floats
		 */
		ht.put("effort_flt", new WebFieldString("effort_flt", (addMode ? ""
				: db.getText("effort_flt")), 3, 6));

		ht.put("review_flt", new WebFieldString("review_flt", (addMode ? ""
				: db.getText("review_flt")), 3, 6));

		/*
		 * Return
		 */
		return ht;

	}

}
