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
 * 	3/10  use tcodes
 * */

/**
 * ProjectTableManager
 * 
 * Glue that the FormDriver uses to connect: the web ProjectFormTemplate.jsp to
 * the database WebFields are used to popuate the jsp, and DbFields are used to
 * pull the input from the form and push to the database.
 */
public class ServiceLevelPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ServiceLevelPlugin() throws services.ServicesException {
		super();
		this.setTableName("tservice_level");
		this.setKeyName("sla_id");
		this.setTargetTitle("Service Level");

		this.setListHeaders( new String[] { "Reference", "Title", "Type" });
		this.setMoreListColumns(new  String[] { "reference_id", "title",
				"code_desc as sla_desc" });
		this.setMoreSelectColumns (new String[] { "code_desc as sla_desc" });
		this.setMoreListJoins(new  String[] { " left join tcodes on tservice_level.sla_type_cd = tcodes.code_value and tcodes.code_type_id = 28 " });
		this.moreSelectJoins = this.moreListJoins;

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = getLookupTable("tuser", "user_id",
				" concat(first_name, ' ', last_name) as the_name ");

		WebFieldSelect wfSLA = new WebFieldSelect("sla_type_cd", addMode ? ""
				: db.getText("sla_type_cd"), sm.getCodes("SLATYPE"));

		WebFieldString wfRefr = new WebFieldString("reference_id",
				(addMode ? "" : db.getText("reference_id")), 32, 32);

		WebFieldString wfTitle = new WebFieldString("title", (addMode ? "" : db
				.getText("title")), 64, 64);

		WebFieldCheckbox wfConfirm = new WebFieldCheckbox("confirmed_flag",
				addMode ? "N" : db.getText("confirmed_flag"), "");

		WebFieldDate wfRealize = new WebFieldDate("begin_date", addMode ? ""
				: db.getText("begin_date"));

		WebFieldSelect wfStakeholder = new WebFieldSelect(
				"owner_id",
				addMode ? new Integer("0") : (Integer) db.getObject("owner_id"),
				userHt, true);

		WebFieldSelect wfVerified = new WebFieldSelect("result_verified_by_id",
				addMode ? new Integer("0") : (Integer) db
						.getObject("result_verified_by_id"), userHt, true);

		WebFieldText wfDesc = new WebFieldText("blob_sla_desc", addMode ? ""
				: db.getText("blob_sla_desc"), 3, 60);

		WebFieldText wfSuccess = new WebFieldText("blob_success_desc",
				addMode ? "" : db.getText("blob_success_desc"), 3, 60);

		WebFieldText wfResult = new WebFieldText("blob_result_desc",
				addMode ? "" : db.getText("blob_result_desc"), 3, 60);

		WebField[] wfs = { wfSLA, wfStakeholder, wfSuccess, wfRealize,
				wfResult, wfConfirm, wfVerified, wfDesc, wfTitle, wfRefr };

		return webFieldsToHT(wfs);

	}

}
