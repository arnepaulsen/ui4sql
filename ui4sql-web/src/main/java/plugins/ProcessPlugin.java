/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;

/**
 * Process Plugin
 * 
 * Change log:
 * 
 * 9/14/2005 - Exact close of Policies
 * 
 */

public class ProcessPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public ProcessPlugin() throws services.ServicesException {
		super();

		this.setContextSwitchOk (false);

		this.setTableName("tprocess");
		this.setKeyName("process_id");
		this.setTargetTitle("Process");

		this.setHasDetailForm (true);
		this.setDetailTarget ("Stage");
		this.setDetailTargetLabel ("Stages");

		
		this.setListHeaders( new String[] { "Reference", "Title", "Status",
				"Author" });

		// columns after last header are not shown! used to match list filters
		// only
		this.setMoreListColumns(new  String[] { "tprocess.reference_nm",
				"tprocess.title_nm", "s.code_desc as status_desc",
				"concat(u.last_name, ',', u.first_name)", "s.code_value",
				"tprocess.author_uid" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tprocess.status_cd = s.code_value and s.code_type_id  = 72 ",
				" left join tuser u on tprocess.author_uid = u.user_id " });

		// this.setMoreSelectJoins (new String[] { "left join tuser as c on
		// tprocess.closed_by_uid = c.user_id " };
		// this.setMoreSelectColumns (new String[] { "c.last_name" };

	}

	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		this.setUpdatesOk(sm.userIsExecutive());
	}
	


	
	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2) {
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", sm
					.Parm("FilterStatus"), sm.getCodes("POLICYSTATUS"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		} else {

			WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
					"FilterUser").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterUser"))), sm.getUserHT(),
					"All Authors");
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

		if ((!sm.Parm("FilterStatus").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterStatus").length() > 0)) {
			sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus") + "'");
		}

		if ((!sm.Parm("FilterType").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterType").length() > 0)) {
			sb
					.append(" AND ptype.code_value = '" + sm.Parm("FilterType")
							+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND tprocess.author_uid = " + sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();


		// save my key info for the Stage plugin
		if (parmMode.equalsIgnoreCase("show")) {
			sm
					.setParentId(db.getInteger("process_id"), db
							.getText("title_nm"));
		}

		/*
		 * codes
		 * 
		 */


		ht.put("status_cd" ,  new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("POLICYSTATUS")));

		ht.put("type_cd" ,  new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("PROCESSTYPE")));

		ht.put("menu_cd" ,  new WebFieldSelect("menu_cd", addMode ? "" : db
				.getText("menu_cd"), sm.getCodes("MENUS")));

		ht.put("priority_cd" ,  new WebFieldSelect("priority_cd",
				addMode ? "" : db.getText("priority_cd"), sm
						.getCodes("PRIORITY")));

		/*
		 * Text
		 */
		ht.put("reference_nm" ,  new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("target_tx" ,  new WebFieldString("target_tx", (addMode ? ""
				: db.getText("target_tx")), 64, 64));

		ht.put("title_nm" ,  new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64));

		ht.put("url_tx" ,  new WebFieldString("url_tx", (addMode ? "" : db
				.getText("url_tx")), 64, 99));

		/*
		 * dates
		 */

		ht.put("effective_date" , new WebFieldDate("effective_date",
				(addMode ? "" : db.getText("effective_date"))));

		/*
		 * Amounts
		 */

		ht.put("max_fte_no" ,  new WebFieldString("max_fte_no", (addMode ? ""
				: db.getText("max_fte_no")), 3, 6));

		ht.put("max_program_no" , new WebFieldString("max_program_no",
				(addMode ? "" : db.getText("max_program_no")), 3, 6));

		ht.put("max_function_point_no" ,  new WebFieldString("max_function_point_no",
				(addMode ? "" : db.getText("max_function_point_no")), 3, 6));

		ht.put("max_structure_no",  new WebFieldString("max_structure_no",
				(addMode ? "" : db.getText("max_structure_no")), 3, 6));

		ht.put("max_interface_no" , new WebFieldString("max_interface_no",
				(addMode ? "" : db.getText("max_interface_no")), 3, 6));

		ht.put("max_dollar_k_amt" ,  new WebFieldString("max_dollar_k_amt",
				(addMode ? "" : db.getText("max_dollar_k_amt")), 3, 6));

		/*
		 * blobs
		 */
		ht.put("desc_blob" ,  new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("notes_blob" ,  new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		ht.put("criteria_blob" ,  new WebFieldText("criteria_blob",
				addMode ? "" : db.getText("criteria_blob"), 3, 80));

		ht.put("exclusion_blob" ,  new WebFieldText("exclusion_blob",
				addMode ? "" : db.getText("exclusion_blob"), 3, 80));

		/*
		 * id's
		 */
		
		ht.put("author_uid" ,  new WebFieldSelect("author_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("author_uid"), sm.getUserHT()));

		return ht;

	}

}
