/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import remedy.RemedyChangeModify;
import services.EMPILookup;
import forms.*;

/**
 * 
 * 2/15 added mySql 3/13 as 'target' to list query 3/23 fix list query
 */

public class GetHomePlugin extends AbsDivisionPlugin {

	/*
	 * Update the user's preferences on tUser table .. can only update, and only
	 * update user's own table, no others
	 */

	// *******************
	// CONSTRUCTORS
	// *******************

	String mrn = "";
	String home = "";
	
	public GetHomePlugin() throws services.ServicesException {
		setDefaults();
	}

	private void setDefaults() {
		this.setTableName("tdummy");
		this.setKeyName("dummy_id");
		this.setTargetTitle("MRN getHome");
		this.setShowAuditSubmitApprove (false);
		this.setContextSwitchOk (false);

		this.setUpdatesOk(false);
		this.setNextOk(false);
		this.setListOk(false);
		
	
	}

	public String getListQuery() {
		return " SELECT * from tdummy	";
	}

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	public void beforeUpdate(Hashtable ht) {

		debug("calling getHome");
		
		EMPILookup empi = new EMPILookup("PROD", "getHome");

		home = empi.getInstance(sm.Parm("mrn"));
		
		debug("that was easy ");
		
	
		return;
	}
	
	public Hashtable<String, WebField> getWebFields(String parmMode) {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * get the table of Forms
		 */

		if (addMode) {

			ht.put("msg", new WebFieldDisplay("msg", "What MRN?"));
		} else {
			ht.put("msg", new WebFieldDisplay("msg", "That was easy."));
		}

		
		ht.put("mrn", new WebFieldString("mrn", (sm.Parm("mrn")), 12, 12));

		ht.put("home", new WebFieldDisplay("home", home));

		/*
		 * Return
		 */

		return ht;

	}

	
}
