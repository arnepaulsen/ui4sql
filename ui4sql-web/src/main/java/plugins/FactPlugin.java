/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Note Plugin
 * 
 * Change log:
 * 
 * 2/15 added mySql 3/13 as 'target' to list query
 * 
 */
public class FactPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public FactPlugin() throws services.ServicesException {
		super();
		this.setTableName("tfact");
		this.setKeyName("fact_id");
		this.setShowAuditSubmitApprove(false);
		
		this.setTargetTitle("Fact");
		
		this.setListHeaders( new String[] { "Title", "Keywords", "Type" });
		this.setMoreListColumns(new  String[] {  "title_nm", "reference_nm",
				"code_desc" });
		
		this.setMoreListJoins(new  String[] { " left join tcodes on tfact.type_cd = tcodes.code_value and tcodes.code_type_id  = 44 " });
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

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("FACTTYPE"));

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);
		
		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);
		
			
		WebField[] wfs = { wfRefr, wfType, wfTitle, wfDesc, wfNotes  };

		return webFieldsToHT(wfs);

	}

}
