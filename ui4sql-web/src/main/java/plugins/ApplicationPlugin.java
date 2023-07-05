/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Date;
import java.util.Hashtable;

import db.*;
import forms.*;

/*******************************************************************************
 * Implements the Plug abstract class to display a database table
 * 
 * @author Arne S. Paulsen
 * 
 * See the <a href="{@docRoot}/copyright.html">Copyright</a>.
 * 
 * 
 * Change Log: 2/15 added mySql 5/15/06 order by application name 6/26/06
 * cacheAncillyList after add 8/30 allow annomyous access to all applications
 * 
 * 9/11/09  - set delete level to administrator from blanket false
 * 
 * 
 ******************************************************************************/

public class ApplicationPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ApplicationPlugin() throws services.ServicesException {
		super();

		this.setTableName("tapplications");
		this.setKeyName("application_id");
		this.setTargetTitle("Application");
		this.setIsRootTable(false);

		this.setListOrder("application_name");

		this.setListHeaders(new String[] { "Name", "Mnemonic", "Division" });

		this.setDeleteOk(false);
		this.setDeleteLevel("administrator");
		
		this.setShowAuditSubmitApprove(false);

	}

	// we have to wait until the sm is init'ed before we can find the
	// sm.getUserId()
	public String getListQuery() {

		if (sm.userIsAdministrator())

			return "select 'Application' as target, tapplications.application_id, tapplications.application_name ,  "
					+ " tapplications.appl_cd,  tdivision.div_name "
					+ " from tapplications "
					+ " join tdivision on tapplications.division_id = tdivision.division_id and tapplications.division_id = "
					+ sm.getDivisionId().toString();

		else
			return "select 'Application' as target, tapplications.application_id, tapplications.application_name ,  "
					+ " tapplications.appl_cd,  tdivision.div_name "
					+ " from tapplications "
					// + " join tuser_application on
					// tapplications.application_id =
					// tuser_application.application_id and
					// tuser_application.user_id = "
					// + sm.getUserId().toString()
					+ " join tdivision on tapplications.division_id = tdivision.division_id and tapplications.division_id = "
					+ sm.getDivisionId().toString();

	}

	public String getListAnd() {
		return " ";

	}

	/*
	 * Set Division
	 */

	public void afterUpdate(Integer rowKey) throws services.ServicesException {

		sm.cacheApplicationFilter();
		sm.cacheAncillaryList();

		// update the application name for the session if changed current
		// application
		if (sm.getApplicationId().compareTo(rowKey) == 0) {
			sm.setApplicationId(rowKey);

		}
	}

	/*
	 * in case the name changes... but otherwise over-kill.
	 */
	public boolean beforeAdd(Hashtable<String, DbField> ht) {

		ht.put("division_id", new DbFieldInteger("division_id", sm
				.getDivisionId()));
		return true;
	}

	public void afterAdd(Integer newRowKey) throws services.ServicesException {
		// if adding a new Project... must set up a permission, else the
		// person who just added cannot even see it

		String javaDate = dateFormat.format(new Date()); // returns

		String sql = new String(
				"insert into tuser_application (application_id, user_id, role_cd, added_uid, added_date) "
						+ " values( "
						+ newRowKey.toString()
						+ ","
						+ sm.getUserId().toString()
						+ ",'UPD', "
						+ sm.getUserId().toString()
						+ ", '"
						+ db.flipToYYYYMMDD_HHMM(javaDate) + "')");

		debug("adding permission : " + sql);
		db.runQuery(sql);
		sm.cacheApplicationFilter();
		sm.setApplicationId(newRowKey);
		// WHY-WHY sm.setFilterId("ProjectId", newRowKey);
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
		 * Strings
		 */
		ht.put("application_name", new WebFieldString("application_name",
				(addMode ? "" : db.getText("application_name")), 24, 50));

		ht.put("appl_cd", new WebFieldString("appl_cd", (addMode ? "" : db
				.getText("appl_cd")), 6, 6));

		/*
		 * Codes
		 */

		ht.put("group_cd", new WebFieldSelect("group_cd", addMode ? ""
				: db.getText("group_cd"), sm.getCodes("SUITES")));

		ht.put("language_cd", new WebFieldSelect("language_cd", addMode ? ""
				: db.getText("language_cd"), sm.getCodes("LANGUAGE")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("LIVESTAT")));

		ht.put("database_cd", new WebFieldSelect("database_cd", addMode ? ""
				: db.getText("database_cd"), sm.getCodes("DATABASE")));

		ht.put("server_cd", new WebFieldSelect("server_cd", addMode ? "" : db
				.getText("server_cd"), sm.getCodes("SERVERTYPE")));

		/*
		 * Dates
		 */

		ht.put("release_date", new WebFieldDate("release_date", (addMode ? ""
				: db.getText("release_date"))));

		/*
		 * Id's
		 */
		ht.put("manager_uid", new WebFieldSelect("manager_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("manager_uid"), sm.getUserHT()));

		ht.put("technical_uid", new WebFieldSelect("technical_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("technical_uid"), sm.getUserHT()));

		ht.put("business_uid", new WebFieldSelect("business_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("business_uid"), sm.getContactHT()));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", (addMode ? "" : db
				.getText("desc_blob")), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 3, 80));

		ht.put("problems_blob", new WebFieldText("problems_blob", (addMode ? ""
				: db.getText("problems_blob")), 3, 80));

		/*
		 * Return
		 */

		return ht;

	}

}
