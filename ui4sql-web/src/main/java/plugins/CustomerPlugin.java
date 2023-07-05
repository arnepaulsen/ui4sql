/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;
import db.DbFieldInteger;

/**
 * Division Area Plugin make a change
 */
public class CustomerPlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public CustomerPlugin() throws services.ServicesException {
		super();
		this.setTableName("tcustomer");
		this.setKeyName("customer_id");
		this.setTargetTitle("Customer");
		
		this.setUpdatesLevel("executive");
		this.setDeleteLevel("root");

		this.setShowAuditSubmitApprove(false);

		this.setListHeaders(new String[] { "Reference", "Name", "Internal",
				"Status", "Contact" });

		this
				.setMoreListColumns(new String[] { "cust_ref", "cust_name",
						"internal.code_desc as internal_desc",
						"stat.code_desc as status_desc",
						"concat(u.last_name, ',', u.first_name) as ContactName" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes stat on tcustomer.status_cd = stat.code_value and stat.code_type_id  = 3 ",
						" left join tcodes internal on tcustomer.internal_flag = internal.code_value and internal.code_type_id  = 3 ",
						" left join tuser u on tcustomer.contact_id = u.user_id " });
	}

	

	/*
	 * Set Division
	 */

	public boolean beforeAdd(Hashtable ht) {
		ht.put("division_id", new DbFieldInteger("division_id", sm
				.getDivisionId()));
		return true;
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
		 * Ids
		 */
		ht.put("contact_id", new WebFieldSelect("contact_id",
				addMode ? new Integer("0") : db.getInteger("contact_id"), sm
						.getContactHT()));

		/*
		 * Codes
		 */

		ht.put("internal_flag", new WebFieldSelect("internal_flag",
				addMode ? "" : db.getText("internal_flag"), sm
						.getCodes("YESNO")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("YESNO")));

		/*
		 * Strings
		 */
		ht.put("cust_name", new WebFieldString("cust_name", (addMode ? "" : db
				.getText("cust_name")), 64, 64));

		ht.put("cust_ref", new WebFieldString("cust_ref", (addMode ? "" : db
				.getText("cust_ref")), 32, 32));

		ht.put("cust_desc", new WebFieldText("cust_desc", (addMode ? "" : db
				.getText("cust_desc")), 4, 64));

		return ht;

	}

}
