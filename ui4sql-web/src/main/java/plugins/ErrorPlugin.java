/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import services.ServicesException;

/**
 * Job Plugin
 * 
 */
public class ErrorPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ErrorPlugin() throws services.ServicesException {
		super();
		this.setTableName("terror");
		this.setKeyName("error_id");
		this.setTargetTitle("Error Codes");

		this.setIsDetailForm (false);
		this.setParentTarget("Module");

		this.setListHeaders(new String[] { "Title", "Reference", "Level",
				"Interface" });
		this.setMoreListColumns(new String[] { "terror.title_nm",
				"terror.reference_nm", "err_level.code_desc",
				"tinterface.title_nm as interface_title" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes as err_level on terror.error_level_cd = err_level.code_value and err_level.code_type_id  = 56 ",
						" left join tinterface on terror.module_id = tinterface.interface_id " });

		// this.moreSelectJoins = this.moreListJoins;

	}

	/*
	 * 
	 * List Selectors for Module and Error Level
	 * 
	 */

	public boolean listColumnHasSelector(int columnNumber) {
		// the deliverable id in column 1
		if (columnNumber > 1)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2) {

			// * column 2 = Error Level Filter
			WebFieldSelect wf = new WebFieldSelect("FilterLevel",
					(sm.Parm("FilterLevel").length() == 0 ? "" : sm
							.Parm("FilterLevel")), sm.getCodes("ERRORLVL"),
					"-All Levels-");
			wf.setDisplayClass("listform");
			return wf;
		} else {

			// column 3 = Module filter

			Hashtable modules = new Hashtable();

			try {
				modules = db
						.getLookupTable("Select title_nm, interface_id, title_nm from tinterface");
			} catch (ServicesException se) {

			}

			WebFieldSelect wf = new WebFieldSelect("FilterModule", (sm.Parm(
					"FilterModule").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterModule"))), modules,
					"-All Interfaces-");
			wf.setDisplayClass("listform");
			return wf;

		}
	}

	public String getListAnd() {
		/*
		 * Limit the list to a specific module and level
		 */

		StringBuffer sb = new StringBuffer();

		// add sql filter for Error Level

		if (!sm.Parm("FilterLevel").equalsIgnoreCase("0")
				&& sm.Parm("FilterLevel").length() > 0) {
			sb.append(" AND err_level.code_value = '" + sm.Parm("FilterLevel")
					+ "'");
		}

		// add sql fitler for Module Id
		if ((!sm.Parm("FilterModule").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterModule").length() > 0)) {
			sb.append(" AND terror.module_id = " + sm.Parm("FilterModule"));
		}

		return sb.toString();
		// return "";

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		// todo: cache the modules ht

		Hashtable interfaces = new Hashtable();

		try {
			interfaces = db
					.getLookupTable("Select tinterface.title_nm, interface_id, tinterface.title_nm from tinterface"
							+ " join tapplications on tinterface.application_id = tapplications.application_id "
							+ " where tapplications.division_id = "
							+ sm.getDivisionId().toString());

		} catch (ServicesException se) {

		}

		WebFieldSelect wfModule = new WebFieldSelect("module_id",
				addMode ? new Integer("0") : db.getInteger("module_id"),
				interfaces);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 10, 10);

		WebFieldSelect wfType = new WebFieldSelect("error_level_cd",
				addMode ? "" : db.getText("error_level_cd"), sm
						.getCodes("ERRORLVL"));

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldText wfDesc = new WebFieldText("blob_desc", addMode ? "" : db
				.getText("blob_desc"), 5, 100);

		WebFieldText wfInstructions = new WebFieldText(
				"blob_spec_instructions", (addMode ? "" : db
						.getText("blob_spec_instructions")), 5, 100);

		WebFieldText wfRecovery = new WebFieldText("blob_recovery_notes",
				(addMode ? "" : db.getText("blob_recovery_notes")), 5, 100);

		WebFieldText wfNotification = new WebFieldText("blob_notification",
				(addMode ? "" : db.getText("blob_notification")), 5, 100);

		WebField[] wfs = { wfRefr, wfDesc, wfModule, wfInstructions, wfTitle,
				wfType, wfRecovery, wfNotification };

		return webFieldsToHT(wfs);

	}
}
