/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Constraint Data Manager
 * 
 * Change Log:
 * 
 * 3/13 as 'target' to list query
 * 
 * 
 */

public class EnvironmentPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public EnvironmentPlugin() throws services.ServicesException {
		super();
		this.setTableName("tenvironment");
		this.setKeyName("environment_id");
		this.setTargetTitle("Test Environment");
		this.setShowAuditSubmitApprove(false);

		this.setListHeaders( new String[] { "Reference", "Title"});

		this.setMoreListColumns(new  String[] { "tenvironment.reference_nm", "tenvironment.title_nm"  });


	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
		
		Hashtable ht = new Hashtable();

		/*
		 * Id's
		 */
		
		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("owner_uid"), sm.getUserHT()));
		
		ht.put("project_id", new WebFieldSelect("project_id",
				addMode ? new Integer("0") : (Integer) db
						.getObject("project_id"), sm.getProjectFilter()));
		
		
		/*
		 * Codes
		 */
		
		
		ht.put("status_cd", new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("ENVUSAGE")));

		
		/*
		 * Dates
		 */
		
		ht.put("available_date", new WebFieldString("available_date",
				(addMode ? "" : db.getText("available_date")),4,12));
		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32));


		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("usage_blob", new WebFieldText("usage_blob", addMode ? "" : db
				.getText("usage_blob"), 3, 80));

		
		/*
		 * Return
		 */

		return ht;
		
	}

}
