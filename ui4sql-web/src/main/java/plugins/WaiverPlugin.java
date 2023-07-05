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
public class WaiverPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public WaiverPlugin() throws services.ServicesException {
		super();
		this.setTableName("twaiver");
		this.setKeyName("waiver_id");
		this.setShowAuditSubmitApprove(false);

		this.setTargetTitle("Standards Waiver");

		this.setListHeaders( new String[] { "Title", "Status", "Standard", "Type" });
		
		this.setMoreListColumns(new  String[] { "tstandard.title_nm",
				"stat.code_desc", "tstandard.title_nm" , "perm.code_desc as TempPerm"});

		this.setMoreListJoins(new  String[] {
				" left join tcodes stat on twaiver.status_cd = stat.code_value and stat.code_type_id  = 51 ",
				" left join tcodes perm on twaiver.type_cd = perm.code_value and perm.code_type_id  = 52 ",
				" left join tstandard on twaiver.standard_id = tstandard.standard_id " });
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

		WebFieldSelect wfApplicationId = new WebFieldSelect("application_id",
				addMode ? sm.getApplicationId() : db.getInteger("application_id"),
				sm.getApplicationFilter());

		Hashtable standards = db
				.getLookupTable("select title_nm as odor, standard_id, title_nm from tstandard");

		WebFieldSelect wfStandardId = new WebFieldSelect("standard_id",
				addMode ? new Integer("0") : db.getInteger("standard_id"),
				standards);

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("SIGNOFFSTATUS"));
		
		WebFieldSelect wfType = new WebFieldSelect("type_cd",
				addMode ? "New" : db.getText("type_cd"), sm
						.getCodes("PERMTEMP"));
		
		WebFieldDate wfExpire  = new WebFieldDate("expire_date",
				addMode ? "" : db.getText("expire_date"));
		
		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);
		
		WebFieldText wfReason = new WebFieldText("reason_blob", addMode ? "" : db
				.getText("reason_blob"), 3, 80);
		
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebField[] wfs = { wfReason, wfExpire, wfApplicationId, wfStatus, wfType, wfStandardId, wfTitle,
				wfDesc, wfNotes };

		return webFieldsToHT(wfs);

	}

}
