/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/*******************************************************************************
 * System Interfaces
 * 
 * 
 * Change Log:
 * 
 * 2/15 added mySql
 * 
 * 
 ******************************************************************************/

public class SystemInterfacePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public SystemInterfacePlugin() throws services.ServicesException {
		super();
		this.setTableName("tsystem_interface");
		this.setKeyName("interface_id");
		this.setTargetTitle("System Interface");

		this.setListHeaders( new String[] { "Reference", "Title" });
		this.setMoreListColumns(new  String[] { "reference_id", "title" });
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		// these are set for WebFielddisplay in 'view' mode, or a html selector
		// in add/update mode

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"),
				sm.getProjectFilter(), true);

		WebFieldSelect wfOtherApp = new WebFieldSelect("other_application_id",
				addMode ? new Integer("0") : (Integer) db
						.getObject("other_application_id"), sm
						.getApplicationFilter());

		WebFieldString wfRefr = new WebFieldString("reference_id",
				(addMode ? "" : db.getText("reference_id")), 32, 32);

		WebFieldString wfTitle = new WebFieldString("title", (addMode ? "" : db
				.getText("title")), 64, 64);

		WebFieldSelect wfType = new WebFieldSelect("interface_type_cd",
				addMode ? "" : db.getText("interface_type_cd"), sm
						.getCodes("DATAFORM"));

		WebFieldSelect wfDirection = new WebFieldSelect("direction_cd",
				addMode ? "IN" : db.getText("direction_cd"), sm
						.getCodes("DIRECTION"));

		WebFieldSelect wfTransfer = new WebFieldSelect("transfer_meth_cd",
				addMode ? "" : db.getText("transfer_meth_cd"), sm
						.getCodes("XMITMETHOD"));

		WebFieldSelect wfFreq = new WebFieldSelect("freq_cd", addMode ? "DLY"
				: db.getText("freq_cd"), sm.getCodes("FREQUENCY"));

		WebFieldText wfDesc = new WebFieldText("blob_description", addMode ? ""
				: db.getText("blob_description"), 6, 100);

		WebFieldText wfNotes = new WebFieldText("blob_notes", addMode ? "" : db
				.getText("blob_notes"), 6, 100);

		WebFieldText wfProcedure = new WebFieldText("blob_spec_procedure",
				addMode ? "" : db.getText("blob_spec_procedure"), 6, 100);

		WebFieldString wfContact = new WebFieldString("contact_info",
				addMode ? "" : db.getText("contact_info"), 64, 64);

		WebField[] wfs = { wfProject, wfOtherApp, wfRefr,
				wfDesc, wfTitle, wfDirection, wfFreq, wfType, wfTransfer,
				wfNotes, wfContact, wfProcedure };

		return webFieldsToHT(wfs);

	}

}
