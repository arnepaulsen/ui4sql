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
 *   2/15 added mySql
 * */

/*******************************************************************************
 * Tollgate Manager
 * 
 * This data manager is unique in that the questions asked depend on the type
 * code value. The questions on the html page cannot be displayed until after
 * add mode is completed and a type code has been selected.
 * 
 * In display or update mode, the questions are obtained from table tfields
 * depending on the type code selected.
 * 
 * Change Log:
 * 
 * 
 ******************************************************************************/

public class ReviewPlugin extends AbsProjectPlugin {

	// *******************
	// *******************
	// CONSTRUCTORS *
	// *******************
	// *******************

	public String getCustomSubForm() {
		return db.getText("type_cd");
	}

	public ReviewPlugin() throws services.ServicesException {
		super();

		this.setTableName("treview");
		this.setKeyName("review_id");
		this.setTargetTitle("Reviews");

		this.setAddCustomFields(true);
		
		this.setListHeaders( new String[] { "Title", "Type", "Owner", "Result" });

		this.setMoreListColumns(new  String[] { "title_nm",
				"rev_type.code_desc as theType",
				"concat(owner.last_name, ',', owner.first_name)",
				"result_type.code_desc as theResult" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes rev_type on treview.type_cd = rev_type.code_value and rev_type.code_type_id  = 65 ",
				" left join tcodes result_type on treview.pass_cd = result_type.code_value and result_type.code_type_id  = 66 ",
				" left join tuser owner on treview.owner_uid = owner.user_id " });

		this.setMoreSelectColumns (new String[] { "rev_type.code_desc as theType" });
		this.moreSelectJoins = this.moreListJoins;

	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		if (addMode) {
			this.setAddCustomFields(false);
			showNewPage(ht);
		} else {
			showUpdatePage(ht);
		}
		return ht;

	}

	/*
	 * 
	 * New Tollgate pages just get the project and dmiac id,
	 * 
	 * because the questions to display depend on the phase.
	 */
	private void showNewPage(Hashtable ht) {

		ht.put("type_cd", new WebFieldSelect("type_cd", "C", sm
				.getCodes("REVIEWTYPES")));

		ht.put("msg", new WebFieldDisplay("msg",
				"Select a review type, then Save-Edit."));

		return;

	}

	/*
	 * 
	 * This is always Update or Display, no need to worry about add ( no value
	 * in the dbInterface rs)
	 */
	private void showUpdatePage(Hashtable ht) throws services.ServicesException {

		/* save off the type_cd when processing updates   FILTER-WHY-WHY???*/

		//sm.setFilterCode("type_cd", db.getText("type_cd"));

		String[][] passCodes = { { "U", "P", "F" },
				{ "Undecided", "Pass", "Fail" } };

		/*
		 * 
		 * Integers
		 */

		ht.put("leader_uid", new WebFieldSelect("leader_uid", db
				.getInteger("leader_uid"), sm.getUserHT()));

		ht.put("owner_uid", new WebFieldSelect("owner_uid", db
				.getInteger("owner_uid"), sm.getUserHT()));

		/*
		 * Codes
		 * 
		 */
		ht
				.put("type_cd", new WebFieldDisplay("type_cd", db
						.getText("theType")));

		ht.put("pass_cd", new WebFieldSelect("pass_cd", db.getText("pass_cd"),
				passCodes));

		/*
		 * Text
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", db
				.getText("reference_nm"), 32, 32));

		ht.put("session_nm", new WebFieldString("session_nm", db
				.getText("session_nm"), 4, 4));

		ht.put("title_nm", new WebFieldString("title_nm", db
				.getText("title_nm"), 64, 128));

		ht.put("participants_tx", new WebFieldString("participants_tx", db
				.getText("participants_tx"), 64, 128));
		/*
		 * blobs
		 */

		ht.put("notes_blob", new WebFieldText("notes_blob", db
				.getText("notes_blob"), 3, 80));

		ht.put("results_blob", new WebFieldText("results_blob", db
				.getText("results_blob"), 3, 80));

		/*
		 * dates
		 */

		ht.put("review_date", new WebFieldDate("review_date", db
				.getText("review_date")));

		return;

	}

}
