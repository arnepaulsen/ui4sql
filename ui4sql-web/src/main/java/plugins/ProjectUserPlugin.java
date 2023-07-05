/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import services.ServicesException;
import forms.*;
import db.*;

/*******************************************************************************
 * ProjectUserTableManager
 * 
 * 
 * Change Log:
 * 
 * 7/21/09 Fix where clause on user query, add space after where division_id =
 * 1and
 * 
 * 
 * 
 ******************************************************************************/

public class ProjectUserPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructors
	 * 
	 **************************************************************************/

	public ProjectUserPlugin() throws services.ServicesException {
		setDefaults();
	}

	public ProjectUserPlugin(DbInterface parmDb)
			throws services.ServicesException {
		this.db = parmDb;
		setDefaults();
	}

	private void setDefaults() {
		this.setTableName("tuser_project");
		this.setKeyName("user_project_id");
		this.setTargetTitle("Project Resources");
		this.setIsAdminFunction(false);
		this.setShowAuditSubmitApprove(false);

		this.setListOrder("permit.last_name");

		this.setListHeaders(new String[] { "Name", "Role" });

		this
				.setMoreListColumns(new String[] {
						"concat(permit.first_name, ' ', permit.last_name) as user_name",
						"tcodes.code_desc as role_desc" });

		this
				.setMoreListJoins(new String[] {
						" left join tuser as permit on tuser_project.user_id = permit.user_id ",
						" left join tcodes on tuser_project.role_cd = tcodes.code_value and tcodes.code_type_id = 14 " });

		this
				.setMoreSelectColumns(new String[] {
						"project_name",
						"concat(permit.first_name, ' ', permit.last_name) as user_name ",
						"permit.phone_tx as phone_tx",
						"permit.email_address as email_tx" });

		this
				.setMoreSelectJoins(new String[] {
						" left join tuser as permit on tuser_project.user_id = permit.user_id ",
						" left join tcodes on tuser_project.role_cd = tcodes.code_value and tcodes.code_type_id = 14 " });

	}

	/***************************************************************************
	 * 
	 * Options
	 * 
	 */

	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		this.setUpdatesOk(sm.userIsAdministrator());
		this.setCopyOk(false);
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode) {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		ht.put("role_cd", new WebFieldSelect("role_cd", addMode ? "BRW" : db
				.getText("role_cd"), sm.getCodes("ROLE")));

		ht.put("project_id", new WebFieldDisplay("project_id", sm
				.getProjectName()));

		if (addMode) {

			String userQuery;

			try {

				userQuery = "select distinct  concat(last_name, ',', first_name) , tuser.user_id , concat(last_name, ',', first_name) as user_name"
						+ " from tuser  where tuser.division_id = "
						+ sm.getDivisionId().toString()
						+ " and tuser.user_id not in (select user_id from tuser_project where project_id = "
						+ sm.getProjectId().toString() + ")";

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

		ht.put("phone_tx", new WebFieldDisplay("phone_tx", addMode ? "" : db
				.getText("phone_tx")));

		ht.put("email_tx", new WebFieldDisplay("email_tx", addMode ? "" : db
				.getText("email_tx")));

		ht.put("role_blob", new WebFieldText("role_blob", addMode ? "" : db
				.getText("role_blob"), 5, 100));

		ht.put("summary_tx", new WebFieldString("summary_tx", addMode ? "" : db
				.getText("summary_tx"), 64, 125));

		return ht;

	}
}
