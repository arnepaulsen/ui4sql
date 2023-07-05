/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * CommPlan Plugin
 * 
 * Note... this Mapper uses custome fields, because everyone will have their own
 * ideas of what goes into a comm plan
 * 
 * Change log:
 * 
 * 
 * 
 */
public class CommPlanPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public CommPlanPlugin() throws services.ServicesException {
		super();
		this.setTableName("tcomm_plan");
		this.setKeyName("comm_plan_id");
		this.setTargetTitle("Communication Plan");

		this.setListHeaders(new String[] { "Title", "Reference", "Status" });
		this.setMoreListColumns(new String[] { "title_nm", "reference_nm",
				"code_desc" });

		this
				.setMoreListJoins(new String[] { " left join tcodes on tcomm_plan.status_cd = tcodes.code_value and tcodes.code_type_id  = 5 " });
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

		WebFieldSelect wfOwnerId = new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), userHt);

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("STATUS"));

		WebFieldString wfKey = new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32);

		WebFieldString wfVersion = new WebFieldString("version_nm",
				(addMode ? "" : db.getText("version_nm")), 16, 16);

		WebFieldString wfComplete = new WebFieldString("final_date",
				(addMode ? "" : db.getText("final_date")), 10, 10);

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebField[] wfs = { wfStatus, wfComplete, wfVersion, wfOwnerId, wfDesc,
				wfTitle, wfKey, wfNotes };

		return webFieldsToHT(wfs);

	}

}
