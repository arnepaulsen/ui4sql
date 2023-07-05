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
public class EstimatePlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public EstimatePlugin() throws services.ServicesException {
		super();
		this.setTableName("testimate");
		this.setKeyName("estimate_id");
		this.setTargetTitle("Project Estimates");
			
		this.setListHeaders( new String[] {  "Title", "Reference" });
		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm"});
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
				.getText("type_cd"), sm.getCodes("LIFECYCLE"));
		
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
		 * Floats
		 */
		WebFieldString wfEffort = new WebFieldString("effort_amt", (addMode ? "" : db
				.getText("effort_amt")), 3, 6);
		
		WebFieldString wfReview = new WebFieldString("review_amt", (addMode ? "" : db
				.getText("review_amt")), 3, 6);
		

		/*
		 * Return 
		 */
		WebField[] wfs = { wfDeptId, wfType, wfQty, wfNotes, wfTitle,
				wfRefr, wfEffort, wfReview };

		return webFieldsToHT(wfs);

	}

}
