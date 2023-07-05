/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import services.ServicesException;

import forms.WebField;
import forms.WebFieldDisplay;
import forms.WebFieldSelect;

/*******************************************************************************
 * ProjectTableManager
 * 
 * 
 * Change Log:
 * 
 * 2/15 added mySql
 * 
 * 7/20/10 Remove old reference 'db-supports-subqueries for pre MySQL 5.0 
 * 
 * 
 * 
 ******************************************************************************/

public class ApplicationUserPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructors
	 * 
	 **************************************************************************/

	public ApplicationUserPlugin() throws services.ServicesException {
		super();

		this.setTableName("tuser_application");
		this.setKeyName("user_application_id");
		this.setTargetTitle("Application Permission");
		this.setIsAdminFunction(true);
		this.setShowAuditSubmitApprove(false);

		this.setUpdatesLevel("executive");

		this.setListHeaders(new String[] { "Name", "Role" });

		this
				.setMoreListColumns(new String[] {
						"concat(permit.first_name, ' ', permit.last_name) as user_name ",
						"tcodes.code_desc as role_desc" });

		this
				.setMoreListJoins(new String[] {
						" join tuser as permit on tuser_application.user_id = permit.user_id ",
						" left join tcodes on tuser_application.role_cd = tcodes.code_value and tcodes.code_type_id = 14 " });

		this
				.setMoreSelectColumns(new String[] { " concat(permit.first_name, ' ', permit.last_name) as user_name " });
		this
				.setMoreSelectJoins(new String[] { " left join tuser as permit on tuser_application.user_id = permit.user_id " });
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

		ht.put("role_cd", new WebFieldSelect("role_cd", addMode ? "BRW" : db
				.getText("role_cd"), sm.getCodes("ROLE")));

		ht.put("application_id", new WebFieldDisplay("application_id", sm
				.getApplicationName()));

		/*
		 * For add, only return a list of users that don't already have a record
		 * for this application
		 */
		if (addMode) {

			try {

				String userQuery = "select distinct  last_name , tuser.user_id , concat(last_name, ',', first_name) as user_name"
						+ " from tuser  where tuser.division_id = "
						+ sm.getDivisionId().toString()
						+ "and tuser.user_id not in (select user_id from tuser_application where application_id = "
						+ sm.getApplicationId().toString() + ")";

				Hashtable users = db.getLookupTable(userQuery);
				ht.put("user_id", new WebFieldSelect("user_id",
						new Integer("0"), users));
			} catch (ServicesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			ht.put("user_id", new WebFieldDisplay("user_id", db
					.getText("user_name")));

		}

		return ht;

	}

}
