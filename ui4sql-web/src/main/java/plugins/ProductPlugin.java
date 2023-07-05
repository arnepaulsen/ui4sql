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
 * Product Data Manager
 * 
 * Change Log:
 * 
 * 5/19/05 Take out getDbFields!!
 * 
 * 
 */
public class ProductPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ProductPlugin() throws services.ServicesException {
		super();
		this.setTableName("tproduct");
		this.setKeyName("product_id");
		this.setTargetTitle("Products");

		this.setListOrder ("tproduct.title_nm");

		this.setMoreListColumns(new  String[] { "tproduct.title_nm",
				"s.code_desc as status_desc",
				"concat(u.last_name, ',', u.first_name)" });

		this.setMoreListJoins(new  String[] {
				"  left join tcodes s on tproduct.status_cd = s.code_value and s.code_type_id  = 60 ",
				" left join tuser u on tproduct.owner_id = u.user_id " });

		this.setListHeaders( new String[] { "Title", "Status", "Owner" });

	}

	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		this.setUpdatesOk(sm.userIsExecutive());
	}
	
	

	

	/*
	 * Re-cache the hashtable after insert, because other plugins (like goal)
	 * fetch it from sm. .. TODO: should do update and delete also
	 */
	public void afterAdd(Integer rowKey) throws services.ServicesException {

		sm.getTable("tproduct",
				"select title_nm, product_id, title_nm from tproduct where division_id = "
						+ sm.getDivisionId().toString());
	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Codes
		 */
		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? "O"
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("FUNCTIONTYPE"));

		WebFieldSelect wfAssign = new WebFieldSelect(
				"owner_id",
				addMode ? new Integer("0") : (Integer) db.getObject("owner_id"),
				sm.getUserHT());

		/*
		 * Strings
		 */
		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldString wfMemo = new WebFieldString("memo_tx", (addMode ? ""
				: db.getText("memo_tx")), 64, 255);

		/*
		 * Blobs
		 */

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebFieldText wfHistory = new WebFieldText("history_blob", addMode ? ""
				: db.getText("history_blob"), 3, 80);

		WebFieldText wfPriority = new WebFieldText("priority_blob",
				addMode ? "" : db.getText("priority_blob"), 3, 80);

		WebFieldText wfInitiatives = new WebFieldText("initatives_blob",
				addMode ? "" : db.getText("initatives_blob"), 3, 80);

		WebField[] wfs = { wfDesc, wfMemo, wfTitle, wfStatus, wfAssign, wfRefr,
				wfType, wfNotes, wfHistory, wfPriority, wfInitiatives };

		return webFieldsToHT(wfs);

	}

}
