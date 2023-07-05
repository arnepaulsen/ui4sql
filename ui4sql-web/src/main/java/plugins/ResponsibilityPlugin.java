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
 * Responsibility Matrix
 * 
 * Associates a user with various roles.
 * 
 * This Data Manager is unique in that tresponsibility is a sub-table of
 * tdeliverable. There is a one-many between deliverable and tresponsibility.
 * 
 * "tresponsibility" does not have a project_id field. It is a child of
 * tdeliverable, and that is where the project_id context is.
 * 
 * The list and select queries insert the tdeliverable table between after
 * tresponsibility and before tproject
 * 
 * Errors: 1. The user has not selected a project filter 2. There are no
 * deliverables for the selected project
 * 
 * if either of these are true: - set the error message field on the form - set
 * 'ok_to_save' equal false, then pass that back in 'saveOK() which is called
 * from the add form driver.
 * 
 * Change Log:
 * 
 * 2/15 added mySql 8/24/05 added 'deliverable' filter on list header 8/28 first
 * select a deliverable on add, no changes after that.
 * 
 * 
 ******************************************************************************/
public class ResponsibilityPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private boolean ok_to_save = true;

	public ResponsibilityPlugin() throws services.ServicesException {
		super();

		this.setIsStepChild(true);
		this.setIsDetailForm(true);
		this.setParentTarget("Deliverable");

		this.setTableName("tresponsibility");
		this.setKeyName("responsibility_id");
		this.setTargetTitle("Responsibility Matrix");

		this.setListOrder("tdeliverable.title_nm");
	}

	public void init(SessionMgr parmSm) {

		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		this.setListHeaders(new String[] { "Deliverable Name",
				"Deliverable Type", "Person", "Area", "Role", "Status" });

		this
				.setMoreListColumns(new String[] {
						"tdeliverable.title_nm",
						"delv_codes.code_desc as delv_type",
						"concat(t.first_name, '&nbsp;&nbsp;', t.last_name) as the_name",
						"area_desc.code_desc as area_name",
						"resp_role.code_desc as resp_name",
						"stat.code_desc as stat_desc",
						"stat.code_value as stat_value" });

		this.setMoreListJoins (new String[] {
				" join tuser as t on tresponsibility.party_uid = t.user_id ",
				" join tdeliverable on tresponsibility.deliverable_id = tdeliverable.deliverable_id ",
				" join tproject on tdeliverable.project_id = tproject.project_id and tproject.project_id = "
						+ sm.getProjectId().toString(),
				" left join tcodes as area_desc on tresponsibility.party_type_cd = area_desc.code_value and area_desc.code_type_id  = 31 ",
				" left join tcodes as delv_codes on tdeliverable.deliverable_cd =  delv_codes.code_value and delv_codes.code_type_id  = 10   ",
				" left join tcodes as resp_role on tresponsibility.responsibility_cd = resp_role.code_value and resp_role.code_type_id  = 32 ",
				" left join tcodes as stat on tresponsibility.status_cd =  stat.code_value and stat.code_type_id  = 51 " });

		this.setMoreSelectColumns(new String[] { "tproject.project_name",
				"tproject.project_id", "stat.code_desc as status_desc ",
				"tdeliverable.title_nm as deliverable_nm" });

		this.setMoreSelectJoins (this.moreListJoins);
	}

	/***************************************************************************
	 * 
	 * 8/22 : List Override for the 'Deliverable' id
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the deliverable id in offset 0, status in offset 5
		if (columnNumber == 0 || columnNumber == 5)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	
	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {

		// Filter on Deliverable
		case 0: {

			String sDeliverables = "SELECT  title_nm, deliverable_id, title_nm "
					+ " from tdeliverable "
					+ " where tdeliverable.project_id = "
					+ sm.getProjectId().toString();

			// passing 1. field id, 2. Integer deliverable id, dbinterface,
			// query, prompt
			return new WebFieldSelect("FilterDeliverable", sm.Parm(
					"FilterDeliverable").length() == 0 ? new Integer("0")
					: new Integer(sm.Parm("FilterDeliverable")), db,
					sDeliverables, "-All Deliverables-");

		}

			// Filter on Status
		default: {
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "O" : sm
					.Parm("FilterStatus")), sm.getCodes("SIGNOFFSTATUS"),
					"-All Status-");
			wf.setDisplayClass("listform");
			return wf;
		}

		}

	}

	public String getListAnd() {
		/*
		 * Limit the list to a specific deliverable, show nothing first time.
		 */

		StringBuffer sb = new StringBuffer();

		// filter on deliverable

		if ((!sm.Parm("FilterDeliverable").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterDeliverable").length() > 0)) {
			sb.append(" AND tresponsibility.deliverable_id = "
					+ sm.Parm("FilterDeliverable"));
		}

		// sql filter for Status
		if ((!sm.Parm("FilterStatus").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterStatus").length() > 0)) {
			sb.append(" AND stat.code_value = '" + sm.Parm("FilterStatus")
					+ "'");
		}

		return sb.toString();
		// return "";

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public boolean saveOk() {
		return ok_to_save;
	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		if (addMode) {
			return showNewPage();
		} else {
			return showUpdatePage();
		}

	}

	/*
	 * 
	 * Return with just list of deliverables for this project, or error message
	 * if there are none.
	 * 
	 */

	private Hashtable showNewPage() throws services.ServicesException {

		Hashtable ht = new Hashtable();

		String deliverQuery = ("Select title_nm as odor, deliverable_id, title_nm from tdeliverable where project_id = " + sm
				.getProjectId().toString());

		Hashtable deliverables = db.getLookupTable(deliverQuery);

		if (deliverables.size() == 0) {
			ok_to_save = false;
			WebFieldDisplay wfMessage = new WebFieldDisplay("message",
					"There are no deliverables for this project.");
			ht.put("message", wfMessage);
			return ht;
		}

		ht.put("deliverable_id", new WebFieldSelect("deliverable_id",
				new Integer("0"), deliverables, true));

		ht.put("party_uid", new WebFieldSelect("party_uid", new Integer("0"),
				sm.getUserHT()));

		ht.put("message", new WebFieldDisplay("message",
				"Select a deliverable, then select Save-Edit."));

		return ht;

	}

	/*
	 * Update mode cannot change the deliverable id
	 * 
	 */
	public Hashtable showUpdatePage() throws services.ServicesException {

		WebFieldDisplay wfDeliverable = new WebFieldDisplay("deliverable_id",
				db.getText("deliverable_nm"));

		WebFieldSelect wfType = new WebFieldSelect("party_type_cd", db
				.getText("party_type_cd"), sm.getCodes("PARTYTYPE"));

		WebFieldSelect wfRole = new WebFieldSelect("responsibility_cd", db
				.getText("responsibility_cd"), sm.getCodes("DLVRROLE"), true);

		WebFieldDisplay wfStatus = new WebFieldDisplay("status_cd", db
				.getText("status_desc"));

		WebFieldSelect wfUser = new WebFieldSelect("party_uid", db
				.getInteger("party_uid"), sm.getUserHT());

		WebFieldText wfNotes = new WebFieldText("notes_blob", db
				.getText("notes_blob"), 4, 80);

		WebFieldString wfStatText = new WebFieldString("status_tx", db
				.getText("status_tx"), 64, 128);

		WebField[] wfs = { wfUser, wfRole, wfDeliverable, wfType, wfNotes,
				wfStatus, wfStatText };

		return webFieldsToHT(wfs);

	}
}
