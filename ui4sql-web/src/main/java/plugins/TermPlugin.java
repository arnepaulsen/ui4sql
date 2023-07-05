/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Division Area Plugin make a change
 */
public class TermPlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public TermPlugin() throws services.ServicesException {
		super();
		this.setTableName("tterm");
		this.setKeyName("term_id");
		this.setTargetTitle("Term");
		this.setAddCustomFields(false);

		this.setListHeaders( new String[] { "Name", "Abbreviations" });

		this.setMoreListColumns(new  String[] { "tterm.title_nm", "tterm.reference_nm" });
		
		this.setUpdatesOk(true);
	}

		
	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebFieldString wfName = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

	
		WebFieldString wfRef = new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32);

		
		WebFieldText wfDesc = new WebFieldText("desc_blob", (addMode ? "" : db
				.getText("desc_blob")), 4, 80);

	
		WebField[] wfs = { wfName, wfRef, wfDesc};
		return webFieldsToHT(wfs);

	}
	
}
