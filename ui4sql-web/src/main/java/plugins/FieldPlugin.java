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
 * 
 * 6/13 expand subform to 4 bytes
 * 8/25/06 - SQLSERVER : remove 'select', plugin will put SELECT TOP 1 if sql-server
 */

public class FieldPlugin extends Plugin {

	// the AddedDate and UpdatedDate are defaulted by the db

	// *******************
	// CONSTRUCTORS
	// *******************

	public FieldPlugin() throws services.ServicesException {
		super();
		this.setDataType ("Form");
		this.setTableName("tfield");
		this.setKeyName("field_id");
		this.setListOrder ("field_nm");
		this.setListHeaders( new String[] { "Field Name", "Type" });
		
		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction (true);
		
		this.setIsDetailForm(true);
		this.setParentTarget ("Form");
		
	}
	
	/*
	 * Remember... any use of sm needs to be done in the init() method.
	 */
	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		
		this.setTargetTitle( sm.getParentName() + " - Fields");
		this.setUpdatesOk(!sm.getApplicationRole().equalsIgnoreCase("brw"));
		this.setSubTitle("Form: " + sm.getParentName());
	}
	
	public String getListQuery() {
		return "select 'Field' as target, field_id, field_nm, field_type_cd  "
		+ " from tfield where 1=1 " ;
	}

	public String getListAnd() {
		return  " and tfield.form_id =  " + sm.getFormId().toString();
	}
	public String getSelectQuery() {
		return userSelectQuery;
	}

	

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	
	
	private static String userSelectQuery = " tfield.* ,  "
			+ " concat(a.first_name, '  ', a.last_name) as added_by, "
			+ " concat(u.first_name, '  ', u.last_name) as updated_by "
			+ " from tfield "
			+ " left join tuser as a on tfield.added_uid = a.user_id "
			+ " left join tuser as u on tfield.updated_uid = u.user_id ";

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();
		
		/*
		 * Strings
		 */
		
		ht.put("field_nm", new WebFieldString("field_nm", db
				.getText("field_nm"), 32, 32));

		ht.put ("field_length", new WebFieldString("field_length", addMode ? "0"
				: db.getText("field_length"), 3, 3));

		ht.put ("html_prompt_nm", new WebFieldString("html_prompt_nm",
				addMode ? "" : db.getText("html_prompt_nm"), 32, 32));

		ht.put ("form_subgroup",new WebFieldString("form_subgroup",
				addMode ? "" : db.getText("form_subgroup"), 4, 4));

		ht.put ("html_prompt_tx",new WebFieldString("html_prompt_tx",
				addMode ? "" : db.getText("html_prompt_tx"), 64, 64));

		ht.put ("db_field_nm", new WebFieldString("db_field_nm",
				addMode ? "" : db.getText("db_field_nm"), 64, 64));
		

		/*
		 * Codes
		 * 
		 */
		
		ht.put ("form_id",  new WebFieldSelect("form_id",
				addMode ? new Integer("1") : (Integer) db.getObject("form_id"),
				sm.getFormFilter(), true, true));
				
		ht.put ("field_type_cd", new WebFieldSelect("field_type_cd",
				addMode ? "" : db.getText("field_type_cd"), sm
						.getCodes("DATATYPES")));

		ht.put ("code_type_nm", new WebFieldSelect("code_type_nm",
				addMode ? "" : db.getText("code_type_nm"), sm
						.getCodes("CODETYPES"), "--Lookup Table--"));

		/*
		 * Return
		 */
		
		return ht;
	}

}
