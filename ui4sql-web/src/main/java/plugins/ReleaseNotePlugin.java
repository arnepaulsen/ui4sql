/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import router.SessionMgr;
import services.ServicesException;

/**
 * Job Plugin
 * 
 */
public class ReleaseNotePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ReleaseNotePlugin() throws services.ServicesException {
		super();
		
		this.setUpdatesOk(false);
		
		this.setTableName("treleasenote");
		this.setKeyName("release_note_id");
		this.setTargetTitle("Release Note");

		this.setKeyAutoIncrement(false);
		
		
		this.setListHeaders( new String[] { "Note Id#", "Title", "Type",
				"Release" , "Deloy", "Owner"});

		this.setMoreListColumns(new  String[] { "release_note_id", "title_nm",
				"fix.code_desc as fix_desc", "rlse.code_desc as rlse_desc" , "deploy.code_desc as deploy_yn", "u.last_name as last_name" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes fix on treleasenote.type_cd = fix.code_value and fix.code_type_id =  106 ",
				" left join tcodes deploy on treleasenote.deploy_cd = deploy.code_value and deploy.code_type_id =  3 ",
				" left join tcodes rlse on treleasenote.release_cd = rlse.code_value and rlse.code_type_id =  98 ",
				" left join tuser u on treleasenote.owner_uid = u.user_id",});

	}
	
	/*
	 * 
	 * List Selectors for Module and Error Level
	 * 
	 */

	public boolean getListColumnCenterOn(int columnNumber) {
		if (columnNumber == 3)
			return false;
		else
			return false;
	}

	public boolean listColumnHasSelector(int columnNumber) {
		// the deliverable id in column 1
		if (columnNumber == 2 || columnNumber == 3 || columnNumber == 4 || columnNumber == 5  )
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		// Change Type : Fix/Enhancement
		if (columnNumber == 2) {

			return getListSelector("FilterType", "E", "Type (All)", sm
					.getCodes("CHANGETYPE"));
		}

		// Relase : Spring 06, Summer 05,etc.
		if (columnNumber == 3) {

			return getListSelector("FilterRelease", "SP06", "Releases (All)",
					sm.getCodes("KRLSE"));
		}

		// Deploy Y/N
		if (columnNumber == 4) {

			return getListSelector("FilterDeploy", "Y", "Deploy (Y/N)",
					sm.getCodes("YESNO"));
		}
		

		// Owner
		if (columnNumber == 5) {
			
			WebFieldSelect wf = new WebFieldSelect(
					"FilterOwner",
					(sm.Parm("FilterOwner").length() == 0 && !sm.Parm(
							"FilterOwner").equalsIgnoreCase("0")) ? new Integer("0")	: new Integer(sm.Parm("FilterOwner")),
					sm.getUserHT(), "Owner (All)");

			wf.setDisplayClass("listform");
			return wf;
		}	

		return getListSelector("dummy", "", "All Suites", sm.getCodes("SUITES"));

	}

	public String getListAnd() {
		/*
		 * Limit the list to a specific module and level
		 */

		StringBuffer sb = new StringBuffer();

		// ChangeType (Enhancement/Fix) defaults to 'Enhancement' to narrow list

		if (sm.Parm("FilterType").length() == 0) {
			sb.append(" AND treleasenote.type_cd = 'E'");
		}

		else {
			if (!sm.Parm("FilterType").equalsIgnoreCase("0")) {
				sb.append(" AND treleasenote.type_cd = '" + sm.Parm("FilterType") + "'");
			}
		}

		
		// Release / default to 'SP06' to narrow list

		if (sm.Parm("FilterRelease").length() == 0) {
			sb.append(" AND release_cd = 'SP06'");
		}

		else {
			if (!sm.Parm("FilterRelease").equalsIgnoreCase("0")) {
				sb.append(" AND release_cd = '" + sm.Parm("FilterRelease")
						+ "'");
			}
		}

		// Deploy Y/N  - default Y 

		if (sm.Parm("FilterDeploy").length() == 0) {
			sb.append(" AND deploy_cd = 'Y'");
		}

		else {
			if (!sm.Parm("FilterDeploy").equalsIgnoreCase("0")) {
				sb.append(" AND deploy_cd = '" + sm.Parm("FilterDeploy") + "'");
			}
		}

		
		if (sm.Parm("FilterOwner").length() > 0) {
			if (!sm.Parm("FilterOwner").equalsIgnoreCase("0"))

				sb.append(" AND treleasenote.owner_uid  = "
						+ sm.Parm("FilterOwner"));
		}

		
		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		
		/*
		 *  Ids
		 */
		
		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"),
				sm.getUserHT()));

		
		/*
		 * Strings
		 */

		ht.put("release_note_id", new WebFieldString("release_note_id", (addMode ? ""
				: db.getText("release_note_id")), 8, 8));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("interface_tx", new WebFieldString("interface_tx", (addMode ? ""
				: db.getText("interface_tx")), 64, 64));

		ht.put("logs_tx", new WebFieldString("logs_tx", (addMode ? "" : db
				.getText("logs_tx")), 64, 64));

		ht.put("client_server_cd", new WebFieldString("client_server_cd",
				(addMode ? "" : db.getText("client_server_cd")), 64, 64));

		ht.put("setup_tx", new WebFieldString("setup_tx", (addMode ? "" : db
				.getText("setup_tx")), 8, 8));

		ht.put("record_type_tx", new WebFieldString("record_type_tx",
				(addMode ? "" : db.getText("record_type_tx")), 64, 128));

		ht.put("appl_nm", new WebFieldString("appl_nm", (addMode ? "" : db
				.getText("appl_nm")), 63, 63));

		ht.put("products_tx", new WebFieldString("products_tx", (addMode ? ""
				: db.getText("products_tx")), 63, 63));
		
		/*
		 * Strings
		 */

		ht.put("deploy_tx", new WebFieldString("deploy_tx", (addMode ? ""
				: db.getText("deploy_tx")), 63, 63));
		
		ht.put("priority_tx", new WebFieldString("priority_tx", (addMode ? ""
				: db.getText("priority_tx")), 63, 63));
		

		ht.put("testing_tx", new WebFieldString("testing_tx", (addMode ? ""
				: db.getText("testing_tx")), 63, 63));

		ht.put("training_tx", new WebFieldString("training_tx", (addMode ? ""
				: db.getText("training_tx")), 63, 63));

		ht.put("config_tx", new WebFieldString("config_tx", (addMode ? ""
				: db.getText("config_tx")), 63, 63));

		
		/*
		 * Codes
		 */

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("CHANGETYPE")));

		ht.put("release_cd", new WebFieldSelect("release_cd", (addMode ? "SP06"
				: db.getText("release_cd")), sm.getCodes("KRLSE")));


		ht.put("priority_cd", new WebFieldSelect("priority_cd", (addMode ? "Y"
				: db.getText("priority_cd")), sm.getCodes("PRIORITY")));

		ht.put("deploy_cd", new WebFieldSelect("deploy_cd", (addMode ? "Y"
				: db.getText("deploy_cd")), sm.getCodes("YESNO")));


		ht.put("config_cd", new WebFieldSelect("config_cd", (addMode ? "Y"
				: db.getText("config_cd")), sm.getCodes("YESNO")));

		ht.put("testing_cd", new WebFieldSelect("testing_cd", (addMode ? "Y"
				: db.getText("testing_cd")), sm.getCodes("YESNO")));

		ht.put("training_cd", new WebFieldSelect("training_cd", (addMode ? "Y"
				: db.getText("training_cd")), sm.getCodes("YESNO")));

		
		/*
		 * blobs
		 */
		ht.put("desc_tx", new WebFieldText("desc_tx", addMode ? "" : db
				.getText("desc_tx"), 5, 100));

		ht.put("notes_tx", new WebFieldText("notes_tx", (addMode ? "" : db
				.getText("notes_tx")), 5, 100));

		return ht;

	}
}
