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
 * 2/15 added mySql 3/13 as 'target' to list query
 * 
 */
public class RampPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public RampPlugin() throws services.ServicesException {
		super();
		this.setTableName("tramp");
		this.setKeyName("ramp_id");
		this.setTargetTitle("Staffing Ramp");
			
		this.setListHeaders( new String[] {  "Title", "Reference", "Year" });
		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm", 
				"year_no" });
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = sm.getUserHT();

		/*
		 * Id's
		 */
		WebFieldSelect wfDeptId = new WebFieldSelect("dept_id",
				addMode ? new Integer("0") : (Integer) db.getObject("dept_id"),
				getLookupTable("tdepartment", "dept_id", "dept_name"));
		
		/*
		 * Codes
		 */
		WebFieldSelect wfQty = new WebFieldSelect("qty_cd", addMode ? "" : db
				.getText("qty_cd"), sm.getCodes("SIZING"));
		
		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("ESTIMATETYPE"));
		
		/*
		 * Strings
		 */
		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);
		
		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);
		/*
		 * Blobs
		 */
		WebFieldText wfNotes = new WebFieldText("notes_blob", (addMode ? ""
				: db.getText("notes_blob")), 5, 80);


		/*
		 * Numbers
		 */

		WebFieldString wfYear = new WebFieldString("year_no", (addMode ? ""
				: db.getText("year_no")), 3, 6);

		/*
		 * Floats
		 */
		
		WebFieldString wfQ1 = new WebFieldString("q1_flt", (addMode ? "" : db
				.getText("q1_flt")), 3, 6);
		WebFieldString wfQ2 = new WebFieldString("q2_flt", (addMode ? "" : db
				.getText("q2_flt")), 3, 6);
		WebFieldString wfQ3 = new WebFieldString("q3_flt", (addMode ? "" : db
				.getText("q3_flt")), 3, 6);
		WebFieldString wfQ4 = new WebFieldString("q4_flt", (addMode ? "" : db
				.getText("q4_flt")), 3, 6);
		WebFieldString wfQ5 = new WebFieldString("q5_flt", (addMode ? "" : db
				.getText("q5_flt")), 3, 6);
		WebFieldString wfQ6 = new WebFieldString("q6_flt", (addMode ? "" : db
				.getText("q6_flt")), 3, 6);
		WebFieldString wfQ7 = new WebFieldString("q7_flt", (addMode ? "" : db
				.getText("q7_flt")), 3, 6);
		WebFieldString wfQ8 = new WebFieldString("q8_flt", (addMode ? "" : db
				.getText("q8_flt")), 3, 6);
		WebFieldString wfQ9 = new WebFieldString("q9_flt", (addMode ? "" : db
				.getText("q9_flt")), 3, 6);
		WebFieldString wfQ10 = new WebFieldString("q10_flt", (addMode ? "" : db
				.getText("q10_flt")), 3, 6);
		WebFieldString wfQ11 = new WebFieldString("q11_flt", (addMode ? "" : db
				.getText("q11_flt")), 3, 6);
		WebFieldString wfQ12 = new WebFieldString("q12_flt", (addMode ? "" : db
				.getText("q12_flt")), 3, 6);

		
		/*
		 * Return
		 */
		WebField[] wfs = { wfDeptId, wfType, wfQty, wfYear, wfNotes, wfTitle,
				wfRefr, wfQ1, wfQ2, wfQ3, wfQ4, wfQ5, wfQ6, wfQ7, wfQ8, wfQ9,
				wfQ10, wfQ11, wfQ12 };

		return webFieldsToHT(wfs);

	}

}
