/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Release Plugin
 * 
 */
/*******************************************************************************
 * Release Data Manager
 * 
 * Change Log:
 * 
 * 2/15 added mySql 5/8/05 Extentded from
 * 
 * 
 ******************************************************************************/
public class StatusPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	static String[][] aLevels = { { "1", "2", "3" },
			{ "Manager", "SVP", "CTO" } };

	public StatusPlugin() throws services.ServicesException {
		super();

		this.setTableName("tstatus");
		this.setKeyName("status_id");
		this.setTargetTitle("Status Reports");

		this.setListHeaders( new String[] { "PM", "As Of", "Status", "Summary" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes stat on tstatus.status_cd = stat.code_value and code_type_id = 49 ",
				" left join tuser u on tstatus.added_uid = u.user_id " });

		this.setMoreListColumns(new  String[] { " u.last_name", "effective_dt",
				"stat.code_desc as Status", "tstatus.title_nm" });
	}

	/***************************************************************************
	 * 
	 * List Filters
	 * 
	 **************************************************************************/

	// filter on 'pm'
	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 0 || columnNumber == 2)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		String sql = " select distince added_uid, last_name from tstatus ";

		// Hashtable ht = sm.get

		if (columnNumber == 2) {
			// note.. FilterStatus (FilterPLUGINNAME is reserved)
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus1", (sm.Parm(
					"FilterStatus1").length() == 0 ? "O" : sm
					.Parm("FilterStatus1")), sm.getCodes("STATUSCOLOR"),
					"All Status");
				wf.setDisplayClass("listform");
			return wf;
		} else {

			WebFieldSelect wf = new WebFieldSelect("FilterPM", (sm.Parm(
					"FilterPM").length() == 0 ? new Integer("0") : new Integer(
					sm.Parm("FilterPM"))), sm.getUserHT(), "All PMs");
			wf.setDisplayClass("listform");
			return wf;
		}
	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present
		if (sm.Parm("FilterPM").length() > 0) {
			if (!sm.Parm("FilterPM").equalsIgnoreCase("0")) {
				sb.append(" AND tstatus.added_uid = " + sm.Parm("FilterPM"));
			}
		}

		// default status to open if no filter present
		if (sm.Parm("FilterStatus1").length() > 0) {

			if (!sm.Parm("FilterStatus1").equalsIgnoreCase("0")) {
				sb.append(" AND tstatus.status_cd ='" + sm.Parm("FilterStatus1") + "'");
			}
		}

		return sb.toString();
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
		 * Ids
		 */

		ht.put("stakeholder_id", new WebFieldSelect("stakeholder_id",
				addMode ? new Integer("0") : (Integer) db
						.getObject("stakeholder_id"), sm.getUserHT(), true));

		/*
		 * text fields
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * dates
		 */
		ht.put("effective_dt", new WebFieldDate("effective_dt", addMode ? ""
				: db.getText("effective_dt")));

		/*
		 * codes
		 */
		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("STATUSCOLOR")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("FREQUENCY")));

		/*
		 * blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("next_blob", new WebFieldText("next_blob", addMode ? "" : db
				.getText("next_blob"), 5, 100));

		ht.put("bottleneck_blob", new WebFieldText("bottleneck_blob",
				addMode ? "" : db.getText("bottleneck_blob"), 5, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 5, 100));

		return ht;

	}

}
