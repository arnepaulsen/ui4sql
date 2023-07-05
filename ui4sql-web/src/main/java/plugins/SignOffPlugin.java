/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;

/*******************************************************************************
 * Sign Off
 * 
 * This only updates tResponsibility table. It's like the Responsibility data
 * manager, but it only updates the status code and notes
 * 
 * No add or delete allowed... use the Responsibility matrix for that.
 * 
 * Change Log:
 * 
 * 8/11 remove getListAnd method, so all show
 * 
 * 
 * 
 * 
 ******************************************************************************/
public class SignOffPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private boolean ok_to_save = true;


	private static String[] sSelectJoins = new String[] {
			" left join tuser as t on tresponsibility.party_uid = t.user_id ",
			" left join tdeliverable on tresponsibility.deliverable_id = tdeliverable.deliverable_id ",
			" left join tproject on tdeliverable.project_id = tproject.project_id ",
			" left join tcodes as area_desc on tresponsibility.party_type_cd = area_desc.code_value and area_desc.code_type_id  = 31 ",
			" left join tcodes as delv_codes on tdeliverable.deliverable_cd =  delv_codes.code_value and delv_codes.code_type_id  = 10   ",
			" left join tcodes as resp_role on tresponsibility.responsibility_cd = resp_role.code_value and resp_role.code_type_id  = 32 " };

	private static String[] sMoreListColumns = new String[] {
			"delv_codes.code_desc as delv_type",
			"concat(t.first_name, '&nbsp;&nbsp;', t.last_name) as the_name",
			"area_desc.code_desc as area_name",
			"resp_role.code_desc as resp_desc",
			"stat.code_desc as curr_status " };

	private static String[] sMoreSelectColumns = new String[] { "project_name",
			"tproject.project_id", "area_desc.code_desc as area_name",
			"tdeliverable.title_nm",
			"concat(t.first_name, '&nbsp;&nbsp;', t.last_name) as the_name", };

	private static String[] sListHeaders = new String[] { "Deliverable",
			"Person", "Area", "Role", "Status" };

	// can turn off the

	public SignOffPlugin() throws services.ServicesException {
		super();
		this.setIsStepChild (true);
		

		this.setUpdatesOk(false);
	
		this.setTableName("tresponsibility");
		this.setKeyName("responsibility_id");
		this.setTargetTitle("Sign Off");
		this.setShowAuditSubmitApprove(false);
		this.setListOrder ("tdeliverable.title_nm");
		this.setListHeaders(sListHeaders);

	}

	public void init(SessionMgr parmSm) {

		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection
		
		this.setMoreListJoins(new  String[] {
				" join tuser as t on tresponsibility.party_uid = t.user_id ",
				" join tdeliverable on tresponsibility.deliverable_id = tdeliverable.deliverable_id ",
				" join tproject on tdeliverable.project_id = tproject.project_id and tproject.project_id = " + sm.getProjectId().toString(),
				" left join tcodes as area_desc on tresponsibility.party_type_cd = area_desc.code_value and area_desc.code_type_id  = 31 ",
				" left join tcodes as delv_codes on tdeliverable.deliverable_cd =  delv_codes.code_value and delv_codes.code_type_id  = 10   ",
				" left join tcodes as stat on tresponsibility.status_cd =  stat.code_value and stat.code_type_id  = 51 ",
				" left join tcodes as resp_role on tresponsibility.responsibility_cd = resp_role.code_value and resp_role.code_type_id  = 32 " });
		
		this.setMoreSelectJoins (sSelectJoins);
		this.setMoreListColumns (sMoreListColumns);
		this.setMoreSelectColumns ( sMoreSelectColumns);
		
		
	}
	
	/***************************************************************************
	 * 
	 * Navigation buttons
	 * 
	 **************************************************************************/

	

	public boolean afterGet() {
		this.setEditOk(db.getInteger("party_uid").toString().equalsIgnoreCase(
				sm.getUserId().toString()));
		
		return true;
	}

	
	

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * 
		 * Never in Add mode
		 * 
		 */

		WebField wfStatus;
		WebField wfReject;

		WebFieldDisplay wfProject = new WebFieldDisplay("project", db
				.getText("project_name"));

		WebFieldDisplay wfDeliverable = new WebFieldDisplay("deliverable_id",
				db.getText("title_nm"));

		WebFieldDisplay wfRole = new WebFieldDisplay("responsibility_cd",
				"Approver");

		/*
		 * Only update to status and reject notes if the log-on user = the
		 * approver
		 * 
		 */
		if (db.getInteger("party_uid").toString().equalsIgnoreCase(
				sm.getUserId().toString())) {
			wfStatus = new WebFieldSelect("status_cd", db.getText("status_cd"),
					sm.getCodes("SIGNOFFSTATUS"), true);
			wfReject = new WebFieldText("reject_tx", addMode ? "" : db
					.getText("reject_tx"), 4, 80);

		} else {
			wfStatus = new WebFieldDisplay("status_cd", db
					.getText("curr_status"));
			wfReject = new WebFieldDisplay("reject_tx", db
					.getText("curr_status"));
		}

		WebFieldDisplay wfUser = new WebFieldDisplay("party_uid", db
				.getText("the_name"));

		WebFieldDisplay wfArea = new WebFieldDisplay("party_type_cd", db
				.getText("area_name"));

		WebFieldText wfNotes = new WebFieldText("notes_blob", db
				.getText("notes_blob"), 4, 80);

		WebField[] wfs = { wfUser, wfProject, wfArea, wfStatus, wfRole,
				wfDeliverable, wfNotes, wfReject };

		return webFieldsToHT(wfs);

	}
}
