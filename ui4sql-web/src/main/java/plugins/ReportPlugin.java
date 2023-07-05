/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
/**
 * 
 *   2/15 added mySql
 * 3/13 as 'target' to list query 
 * */

/**
 * 
 * Reports Manager -
 * 
 */
public class ReportPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ReportPlugin() throws services.ServicesException {
		super();
		this.setTableName("treport");
		this.setKeyName("report_id");
		this.setTargetTitle("Report");

		this.setListHeaders( new String[] { "Title" , "Reference"});
		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm"  });

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfTitle = new WebFieldString("title_nm",
				(addMode ? "" : db.getText("title_nm")), 64, 64);

		WebFieldText wfPurpose = new WebFieldText("purpose_blob",
				(addMode ? "" : db.getText("purpose_blob")), 3, 80);

		WebFieldText wfUser = new WebFieldText("audience_blob", (addMode ? ""
				: db.getText("audience_blob")), 3, 80);

		WebFieldString wfMedia = new WebFieldString("report_media",
				(addMode ? "" : db.getText("report_media")), 32, 32);

		WebFieldSelect wfFreq = new WebFieldSelect("frequency_cd", addMode ? ""
				: db.getText("frequency_cd"), sm.getCodes("FREQUENCY"));

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebField[] wfs = { wfTitle, wfRefr, wfDesc,
				wfPurpose, wfUser, wfMedia, wfFreq };
		
		return webFieldsToHT(wfs);

	}

	
}
