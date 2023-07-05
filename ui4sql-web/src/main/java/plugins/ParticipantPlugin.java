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

public class ParticipantPlugin extends AbsProjectPlugin {

	// *******************
	// CONSTRUCTORS
	// *******************

	public ParticipantPlugin() throws services.ServicesException {
		super();

		this.setTableName("tparticipant");
		this.setKeyName("participant_id");
		this.setTargetTitle("Project Participants");
		this.setShowAuditSubmitApprove(false);

		this.setListHeaders(new String[] { "First Name", "Last Name", });
		this.setMoreListColumns(new String[] { "first_name", "last_name" });

		this
				.setMoreSelectJoins(new String[] { " left join tuser part on tparticipant.user_id = part.user_id" });
		this
				.setMoreListJoins(new String[] { " left join tuser part on tparticipant.user_id = part.user_id" });

	}

	// *******************
	// WEB PAGE
	// *******************

	/**
	 * ************************************************************************* * *
	 * HTML Field Mapping *
	 * *************************************************************************
	 */

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht;

		if (addMode) {
			this.setAddCustomFields(false);
			ht = showNewPage();
		} else {
			ht = showUpdatePage();
		}
		return ht;

	}

	private Hashtable showNewPage() {

		Hashtable ht = new Hashtable();

		boolean addMode = true;

		ht.put("user_id", new WebFieldSelect("user_id", new Integer("0"), sm
				.getUserHT()));

		ht.put("msg", new WebFieldDisplay("msg",
				"Please  select a participant, then Save-Edit."));

		return ht;

	}

	public Hashtable showUpdatePage() throws services.ServicesException {

		Hashtable ht = new Hashtable();

		boolean addMode = false;

		/*
		 * Strings
		 */

		ht.put("first_name", new WebFieldDisplay("first_name", addMode ? ""
				: db.getText("first_name")));

		ht.put("last_name", new WebFieldDisplay("last_name", addMode ? "" : db
				.getText("last_name")));

		ht.put("email_address", new WebFieldDisplay("email_address",
				addMode ? "" : db.getText("email_address")));

		/*
		 * Dates
		 */

		return ht;

	}

}
