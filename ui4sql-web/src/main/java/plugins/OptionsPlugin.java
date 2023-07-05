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
 * 6/11 change to allow field update, show 'active' flag instead of 'custom
 * fields' flag 8/25/06 - SQLSERVER : remove 'select', plugin will put SELECT
 * TOP 1 if sql-server
 * 
 */

public class OptionsPlugin extends Plugin {

	// the AddedDate and UpdatedDate are defaulted by the db

	// *******************
	// CONSTRUCTORS
	// *******************

	private static String selectQuery = "'Options' as target, toptions.*, concat(a.first_name, '  ', a.last_name) as added_by, concat(u.first_name, '  ', u.last_name) as updated_by from toptions left join tuser as a  on toptions.added_uid = a.user_id left join tuser as u on toptions.updated_uid = u.user_id";

	public OptionsPlugin() throws services.ServicesException {
		super();
		this.setTableName("toptions");
		this.setKeyName("options_id");
		this.setListOrder("list_not_valid");
		this.setTargetTitle("Options");
		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction(true);
		this.setShowVCR(false);
		this.setSubTitle("System Options");
		
		this.setSelectQuery(selectQuery);
		
		this.setListOk(false);
		this.setUpdatesOk(false);
		this.setEditLevel("administrator");

	}

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	public Hashtable getWebFields(String parmMode) {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		ht.put("return_url", new WebFieldString("return_url", addMode ? "" : db
				.getText("return_url"), 30, 30));

		ht.put("pw_strength_cd", new WebFieldSelect("pw_strength_cd",
				addMode ? "W" : db.getText("pw_strength_cd"), sm
						.getCodes("PASSWORD")));

		ht.put("timeout_minutes_no", new WebFieldString("timeout_minutes_no",
				addMode ? "0" : db.getText("timeout_minutes_no"), 4, 4));

		ht.put("pw_days_expire_no", new WebFieldString("pw_days_expire_no",
				addMode ? "0" : db.getText("pw_days_expire_no"), 4, 4));

		return ht;

	}
}
