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
 * 2/15 added mySql 3/13 as 'target' to list query 3/23 fix list query 9/17 add
 * nuid
 */

public class ContactPlugin extends AbsDivisionPlugin {

	private static String[] extraJoins = { " left join tcodes  on tcontact.contact_type_cd = tcodes.code_value and tcodes.code_type_id  = 29 " };

	private static String[] extraColumns = { "tcodes.code_desc as contact_desc " };

	public ContactPlugin() throws services.ServicesException {
		super();

		this.setTableName("tcontact");
		this.setKeyName("contact_id");
		this.setTargetTitle("Contact");
		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction(true);

		this.setListHeaders(new String[] { "Last Name", "First Name", "NUID",
				"Type" });

		this.setMoreListColumns(new String[] { "last_name", "first_name",
				"nuid_nm", "tcodes.code_desc" });

		this.setListOrder("last_name");

		// this.setMoreSelectColumns ( extraColumns;
		// this.moreSelectJoins = extraJoins;
		this.setMoreListJoins(extraJoins);

		this.setRemedyOk(true);
		this.setUpdatesLevel("administrator");
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

		ht.put("first_name", new WebFieldString("first_name", addMode ? "" : db
				.getText("first_name"), 32, 32));

		ht.put("last_name", new WebFieldString("last_name", addMode ? "" : db
				.getText("last_name"), 32, 32));

		ht.put("nuid_nm", new WebFieldString("nuid_nm", addMode ? "" : db
				.getText("nuid_nm"), 7, 7));

		ht.put("email_address", new WebFieldString("email_address",
				addMode ? "" : db.getText("email_address"), 32, 32));

		ht.put("active_flag", new WebFieldCheckbox("active_flag", addMode ? "N"
				: db.getText("active_flag"), ""));

		ht.put("contact_type_cd", new WebFieldSelect("contact_type_cd",
				addMode ? "" : db.getText("contact_type_cd"), sm
						.getCodes("CONTACTTYPE")));

		return ht;

	}
}
