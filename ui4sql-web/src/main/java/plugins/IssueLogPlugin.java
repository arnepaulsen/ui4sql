/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import db.*;

import java.util.*;

import router.SessionMgr;

/**
 * Issue Plugin
 * 
 * Change log:
 * 
 * 6/20 added logic in 'beforeUpdate' to set the closed_by_uid and closed_date
 * 
 * 8/22 added list selector for status column
 * 
 */

public class IssueLogPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public IssueLogPlugin() throws services.ServicesException {
		super();

		this.setTableName("tissue_log");
		this.setKeyName("issue_log_id");
		this.setTargetTitle("Issue Progress");

		this.setShowAuditSubmitApprove(false);
		this.setIsStepChild(true);
		this.setIsDetailForm(true);
		this.setParentTarget("Issue");

		this.setSubmitOk(false); 
		
	}

	public void init(SessionMgr parmSm) {

		this.sm = parmSm;
		this.db = sm.getDbInterface(); // has an open connection
	
		
		// columns after last header are not shown! used to match list filters
		// only

		this.setListHeaders(new String[] { "Reference", "Title", "Started",
				"Status", "Originator", "Owner" });

		this
				.setMoreListColumns(new String[] {
						"tissue_log.reference_nm",
						"tissue_log.title_nm",
						dbprefix + "FormatDateTime(tissue_log.added_date, 'mm/dd/yy') as added_disp",
						"stat.code_desc as status",
						"concat(a.last_name, ',', a.first_name) addedBy",
						"concat(owner.last_name, ',', owner.first_name) OwnerPerson" });

		this
				.setMoreListJoins(new String[] {
						" join tissue on tissue_log.parent_id = tissue.issue_id ",
						" join tproject on tissue.project_id = tproject.project_id and tproject.project_id = "
								+ sm.getProjectId().toString(),
						" left join tcodes p on tissue.priority_cd = p.code_value and p.code_type_id  = 7 ",
						" left join tuser a on tissue_log.added_uid = a.user_id ",
						" left join tuser u on tissue_log.updated_uid = u.user_id ",
						" left join tuser owner on tissue_log.assigned_uid = owner.user_id ",
						" left join tcontact on tissue_log.token_uid = tcontact.contact_id ",
						" left join tcodes stat on tissue.status_cd = stat.code_value and stat.code_type_id  = 45 " });

		this
				.setMoreSelectJoins(new String[] {
						" join tissue on tissue_log.parent_id = tissue.issue_id ",
						" join tproject on tissue.project_id = tproject.project_id and tproject.project_id = "
								+ sm.getProjectId().toString() });

		this.setMoreSelectColumns(new String[] { "tproject.project_name",
				"tproject.project_id", "tissue.title_nm as issue_nm" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not

		return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */

	
	public boolean beforeAdd(Hashtable ht) {

		ht.put("parent_id", new DbFieldInteger("parent_id", new Integer(sm
				.getParentId())));

		return true;
	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		debug("0");

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * id's
		 */

		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));

		ht.put("token_uid", new WebFieldSelect("token_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("token_uid"), sm.getUserHT()));

		/*
		 * codes
		 */


		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "New"
				: db.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		String[][] types = { { "Q", "S", "T", "R", "O" },
				{ "Question", "Stop", "Task", "Risk", "Other" } };

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), types));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		/*
		 * text
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 12, 12));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("next_tx", new WebFieldString("next_tx", (addMode ? "" : db
				.getText("next_tx")), 64, 64));

		ht.put("bottleneck_tx", new WebFieldString("bottleneck_tx",
				(addMode ? "" : db.getText("bottleneck_tx")), 64, 64));

		/*
		 * dates
		 */

		/*
		 * Blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		/*
		 * save the old status code... do not put any under-scores in the web
		 * name so it won't get treated as a db update column
		 */

		return ht;

	}

}
