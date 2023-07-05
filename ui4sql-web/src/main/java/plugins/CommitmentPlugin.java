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
 * Commitment Plugin
 * 
 * Change log:
 * 
 * 9/14/2005 - Exact close of Issues... possible to make a higher class? .. but
 * unlike issues which default the list to open, commitments don't have a
 * default status unlike issues, the status phase which resolved is d-delivered,
 * not c-closed as for issues.
 * 
 */

public class CommitmentPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public CommitmentPlugin() throws services.ServicesException {
		super();

		this.setContextSwitchOk(false);

		this.setTableName("tcommitment");
		this.setKeyName("commitment_id");
		this.setTargetTitle("Commitment");

		this.setUpdatesLevel("administrator");

		this.setSubmitOk(false);

		this.setListHeaders(new String[] { "Title", "Commit Date", "Status",
				"Priority", "Owner" });

		// columns after last header are not shown! used to match list filters
		// only
		this.setMoreListColumns(new String[] { "title_nm", "commit_date",
				"s.code_desc as status_desc", "p.code_desc as priority_desc",
				"concat(u.last_name, ',', u.first_name)",
				"s.code_value", "p.code_value", "tcommitment.assigned_uid" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes s on tcommitment.status_cd = s.code_value and s.code_type_id  = 67 ",
						" left join tcodes p on tcommitment.priority_cd = p.code_value and p.code_type_id  = 7 ",
						" left join tuser u on tcommitment.assigned_uid = u.user_id " });

		this
				.setMoreSelectJoins(new String[] { "left join tuser as c on tcommitment.closed_by_uid = c.user_id " });

		this.setMoreSelectColumns(new String[] { "c.last_name" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber > 1)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2) {
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", sm
					.Parm("FilterStatus"), sm.getCodes("COMMITSTATUS"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		} else {
			if (columnNumber == 3) {

				WebFieldSelect wf = new WebFieldSelect("FilterPriority", sm
						.Parm("FilterPriority"), sm.getCodes("PRIORITY"),
						"All Priorities");
				wf.setDisplayClass("listform");
				return wf;
			} else {

				WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
						"FilterUser").length() == 0 ? new Integer("0")
						: new Integer(sm.Parm("FilterUser"))), sm.getUserHT(),
						"All Owners");
				wf.setDisplayClass("listform");
				return wf;

			}
		}

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present

		if ((!sm.Parm("FilterStatus").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterStatus").length() > 0)) {
			sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus") + "'");
		}

		if ((!sm.Parm("FilterPriority").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterPriority").length() > 0)) {
			sb
					.append(" AND p.code_value = '" + sm.Parm("FilterPriority")
							+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND tcommitment.assigned_uid = "
					+ sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * update closed-by date and close-by-uid
	 * 
	 **************************************************************************/
	public void beforeUpdate(Hashtable ht) {
		// * set the closed fields if new status is closed
		if (sm.Parm("status_cd").equalsIgnoreCase("d")
				&& !sm.Parm("oldstatuscode").equalsIgnoreCase("d")) {
			ht.put("closed_date", new DbFieldDate("closed_date", new Date()));
			ht.put("closed_by", new DbFieldInteger("closed_by_uid", sm
					.getUserId()));
		}
		// clear the closed fields if the commitment is re-opened
		if (!sm.Parm("status_cd").equalsIgnoreCase("d")
				&& sm.Parm("oldstatuscode").equalsIgnoreCase("d")) {
			ht.put("closed_date", new DbFieldDate("closed_date", ""));
			ht.put("closed_by", new DbFieldInteger("closed_by_uid",
					new Integer("0")));
		}
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = sm.getUserHT();

		/*
		 * sm will cache on first call, so sql is only run first
		 */
		Hashtable htCustomers = sm
				.getTable("tfunction",
						"select cust_name, customer_id, cust_name from tcustomer where division_id = "
								+ sm.getDivisionId().toString()
								+ " order by cust_name");

		WebFieldSelect wfCustomers = new WebFieldSelect("customer_id",
				addMode ? new Integer("0") : db.getInteger("customer_id"),
				htCustomers);

		/*
		 * codes
		 * 
		 */
		WebFieldSelect wfStatus = new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("COMMITSTATUS"));

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("COMMITTYPE"));

		WebFieldSelect wfPriority = new WebFieldSelect("priority_cd",
				addMode ? "" : db.getText("priority_cd"), sm
						.getCodes("PRIORITY"));

		WebFieldSelect wfTracking = new WebFieldSelect("ryg_cd", addMode ? ""
				: db.getText("ryg_cd"), sm.getCodes("RYG"));

		/*
		 * Text
		 */
		WebFieldString wfKey = new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32);

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldDisplay wfCloseBy = new WebFieldDisplay("closed_by",
				(addMode ? "" : db.getText("last_name")));
		/*
		 * dates
		 */
		WebFieldDisplay wfCloseDate = new WebFieldDisplay("closed_date",
				(addMode ? "" : db.getText("closed_date")));

		WebFieldDate wfCommitDate = new WebFieldDate("commit_date",
				(addMode ? "" : db.getText("commit_date")));

		/*
		 * blobs
		 */
		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebFieldText wfReason = new WebFieldText("reason_blob", addMode ? ""
				: db.getText("reason_blob"), 3, 80);

		WebFieldText wfResolve = new WebFieldText("resolution_blob",
				addMode ? "" : db.getText("resolution_blob"), 3, 80);

		/*
		 * id's
		 */
		WebFieldSelect wfAssign = new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), userHt);

		/*
		 * save the old status code... do not put any under-scores in the web
		 * name so it won't get treated as a db update column
		 */
		WebFieldHidden wfOldStatus = new WebFieldHidden("oldstatuscode",
				(addMode ? "" : db.getText("status_cd")));

		WebField[] wfs = { wfStatus, wfType, wfPriority, wfDesc, wfTitle,
				wfCommitDate, wfNotes, wfReason, wfTracking, wfKey, wfResolve,
				wfCloseDate, wfCloseBy, wfAssign, wfOldStatus, wfCustomers };

		return webFieldsToHT(wfs);

	}

}
