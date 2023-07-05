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
 * 2/15 added mySql 3/13 as 'target' to list query 3/23 fix list query
 */

public class PasswordPlugin extends AbsDivisionPlugin {

	/*
	 * Update the user's password
	 */

	public PasswordPlugin() throws services.ServicesException {
		setDefaults();
	}

	private void setDefaults() {
		this.setTableName("tuser");
		this.setKeyName("user_id");
		this.setTargetTitle("Change Password");
		this.setShowAuditSubmitApprove(false);
		this.setContextSwitchOk (false);
		
		this.setUpdatesOk(false);
		this.setNextOk(false);
		this.setListOk(false);
		
	}
	

	
	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	public Hashtable getWebFields(String parmMode) {

		Hashtable ht = new Hashtable();
		
		if (sm.Parm("Action").equalsIgnoreCase("edit")) {
			ht.put ("label1", new WebFieldDisplay("lable1","New password"));
			ht.put ("label2", new WebFieldDisplay("label2","Re-enter password"));
			ht.put ("pwcheck", new WebFieldPassword("pwcheck", ""));
		}
		else {
			ht.put ("label1", new WebFieldDisplay("lable1","Password"));
			
		}
		
		ht.put("password_nm", new WebFieldPassword("password_nm", db
				.getText("password_nm")));

		return ht;

	}
}
