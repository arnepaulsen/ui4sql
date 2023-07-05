/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import services.ServicesException;
import forms.*;

/**
 * Interface Plugin 3/25/04 - new page 4/19/05 - remove unused 'application
 * name' from select list
 * 
 * Change Log: 9/21/06 - para ht, use table 'suites' instead of static array,
 * re-order list with reference nm first
 * 
 */

public class InterfacePlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public InterfacePlugin() throws services.ServicesException {
		super();

		this.setTableName("tinterface");
		this.setKeyName("interface_id");
		this.setTargetTitle("Interface");
		this.setListOrder ("tinterface.title_nm");
		
		this.setDeleteOk(false);

		this.setListHeaders( new String[] { "Reference", "Title", "Suite",
				"Critical", "Ancillary", "Direction", "Volume"});

		this.setMoreListColumns(new  String[] { "tinterface.reference_nm",
				"tinterface.title_nm", "suite.code_desc as suite_desc",
				"stat.code_desc as status_desc", "app.appl_cd",
				"direction.code_desc as direct_desc", "volume_no" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes stat on tinterface.critical_cd = stat.code_value and stat.code_type_id  = 3 ",
				" left join tcodes direction on tinterface.direction_cd = direction.code_value and direction.code_type_id  = 21 ",
				" left join tcodes suite on tinterface.suite_cd = suite.code_value and suite.code_type_id  = 95 ",
				" left join tapplications app on tinterface.other_application_id = app.application_id ",
				" left join tproject on tinterface.project_id = tproject.project_id " });

	}

	

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#3)
		if (columnNumber == 2 || columnNumber == 3 || columnNumber == 4
				|| columnNumber == 5)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		/*
		 * Suite
		 */

		if (columnNumber == 2)
			return getListSelector("FilterSuite", "O", "Suite?", sm
					.getCodes("SUITES"));

		/*
		 * Critical Y/N
		 */

		if (columnNumber == 3)
			return getListSelector("FilterStatus", "", "Critical?", sm
					.getCodes("YESNO"));

		/*
		 * 
		 */
		if (columnNumber == 4) {

			String qry = "select distinct  application_id, application_id , appl_cd from tapplications  order by appl_cd ";

			Hashtable appls = new Hashtable();

			try {
				appls = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}

			WebFieldSelect wf = new WebFieldSelect("FilterAppl", (sm.Parm(
					"FilterAppl").length() == 0 ? new Integer("0") : new Integer(sm.Parm("FilterAppl"))),
					appls, "Ancillaries?");
				wf.setDisplayClass("listform");
			return wf;
		}

		/*
		 * Direction In/Out
		 */

		if (columnNumber == 5)
			return getListSelector("FilterDirection", "", "Direction?", sm
					.getCodes("DIRECTION"));

		// will never get here

		return getListSelector("dummy", new Integer(""), "badd..",
				new Hashtable());

	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		// default status to 'Production' if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			// sb.append(" AND stat.code_value = 'P'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND stat.code_value = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		// default status to 'Production' if no filter present
		if (sm.Parm("FilterSuite").length() == 0) {

		}

		else {
			if (!sm.Parm("FilterSuite").equalsIgnoreCase("0")) {
				sb.append(" AND suite.code_value = '" + sm.Parm("FilterSuite")
						+ "'");
			}
		}

		// Filter Application
		if (sm.Parm("FilterAppl").length() == 0) {

		}

		else {
			if (!sm.Parm("FilterAppl").equalsIgnoreCase("0")) {
				sb.append(" AND other_application_id = "
						+ sm.Parm("FilterAppl") );
			}
		}

		// default status to 'Production' if no filter present
		if (sm.Parm("FilterDirection").length() == 0) {

		}

		else {
			if (!sm.Parm("FilterDirection").equalsIgnoreCase("0")) {
				sb.append(" AND direction.code_value = '"
						+ sm.Parm("FilterDirection") + "'");
			}
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		// these are set for WebFielddisplay in 'view' mode, or a html selector
		// in add/update mode

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Ids
		 */

		ht.put("other_application_id", new WebFieldSelect(
				"other_application_id", addMode ? sm.getApplicationId()
						: (Integer) db.getObject("other_application_id"), sm
						.getAncillaryList()));

		ht.put("project_id", new WebFieldSelect("project_id", addMode ? sm
				.getProjectId() : (Integer) db.getObject("project_id"), sm
				.getProjectFilter(), true));

		ht.put("owner_uid", new WebFieldSelect("owner_uid", addMode ? sm
				.getProjectId() : (Integer) db.getObject("owner_uid"), sm
				.getUserHT(), true));

		ht.put("version_id", new WebFieldString("version_id", db
				.getText("version_id"), 8, 16));

		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", db
				.getText("reference_nm"), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", db
				.getText("title_nm"), 64, 64));

		ht.put("contact_info", new WebFieldString("contact_info", db
				.getText("contact_info"), 64, 64));

		ht.put("ancillary_queue_nm", new WebFieldString("ancillary_queue_nm",
				db.getText("ancillary_queue_nm"), 32, 64));

		ht.put("local_quue_nm", new WebFieldString("local_quue_nm", db
				.getText("local_quue_nm"), 32, 64));

		/*
		 * Integers
		 * 
		 */
		ht.put("volume_no", new WebFieldString("volume_no", (addMode ? "0" : db
				.getText("volume_no")), 6, 6));

		/*
		 * codes
		 */

		ht.put("volume_freq_cd", new WebFieldSelect("volume_freq_cd", db
				.getText("volume_freq_cd"), sm.getCodes("FREQUENCY")));

		ht.put("suite_cd", new WebFieldSelect("suite_cd", db
				.getText("suite_cd"), sm.getCodes("SUITES")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("LIVESTAT")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("DATAFORM")));

		ht.put("direction_cd", new WebFieldSelect("direction_cd",
				addMode ? "I" : db.getText("direction_cd"), sm
						.getCodes("DIRECTION")));

		
		ht.put("transfer_meth_cd", new WebFieldSelect("transfer_meth_cd",
				addMode ? "" : db.getText("transfer_meth_cd"), sm
						.getCodes("XMITMETHOD")));

		ht.put("freq_cd", new WebFieldSelect("freq_cd", addMode ? "DLY" : db
				.getText("freq_cd"), sm.getCodes("FREQUENCY")));

		ht.put("critical_cd", new WebFieldSelect("critical_cd", addMode ? "N"
				: db.getText("critical_cd"), sm.getCodes("YESNO")));

		ht.put("mq_cd", new WebFieldSelect("mq_cd", addMode ? "N" : db
				.getText("mq_cd"), sm.getCodes("YESNO")));

		ht.put("ieve_cd", new WebFieldSelect("ieve_cd", addMode ? "N" : db
				.getText("ieve_cd"), sm.getCodes("YESNO")));

		ht.put("p2p_cd", new WebFieldSelect("p2p_cd", addMode ? "N" : db
				.getText("p2p_cd"), sm.getCodes("YESNO")));

		ht.put("group_cd", new WebFieldSelect("group_cd", addMode ? "1" : db
				.getText("group_cd"), sm.getCodes("GROUP20")));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", db
				.getText("desc_blob"), 5, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", db
				.getText("notes_blob"), 5, 80));

		ht.put("procedure_blob", new WebFieldText("procedure_blob", db
				.getText("procedure_blob"), 5, 80));

		ht.put("sla_blob", new WebFieldText("sla_blob", db.getText("sla_blob"),
				5, 80));

		ht.put("version_blob", new WebFieldText("version_blob", db
				.getText("version_blob"), 5, 80));

		return ht;

	}

}
