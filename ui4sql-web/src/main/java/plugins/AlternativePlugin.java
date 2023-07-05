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

/**
 * Issue Plugin
 * 
 * Change log:
 * 
 * 1/13/07 New Plugin
 * 
 */

public class AlternativePlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public AlternativePlugin() throws services.ServicesException {
		super();

		this.setTableName("talternative");
		this.setKeyName("alternative_id");
		this.setTargetTitle("Alternative");

		this.setListHeaders(new String[] { "Reference", "Title", "Status",
				"Rank", "Owner" });

		// columns after last header are not shown! used to match list filters
		// only
		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"s.code_desc as status_desc", "p.code_desc as priority_desc",
				"concat(u.last_name, ',', u.first_name)",
				"s.code_value", "p.code_value", "talternative.assigned_uid" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes s on talternative.status_cd = s.code_value and s.code_type_id  = 45 ",
						" left join tcodes p on talternative.rank_cd = p.code_value and p.code_type_id  = 81 ",
						" left join tuser u on talternative.assigned_uid = u.user_id " });

		this
				.setMoreSelectJoins(new String[] { "left join tuser as c on talternative.closed_by_uid = c.user_id " });

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
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "O" : sm
					.Parm("FilterStatus")), sm.getCodes("ISSUESTAT"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		} else {
			if (columnNumber == 3) {

				WebFieldSelect wf = new WebFieldSelect("FilterRank", sm
						.Parm("FilterRank"), sm.getCodes("RANK10"), "All Ranks");
				wf.setDisplayClass("listform");
				return wf;
			} else {

				WebFieldSelect wf = new WebFieldSelect("FilterUser", (sm.Parm(
						"FilterUser").length() == 0 ? Integer.valueOf(0)
						: Integer.parseInt(sm.Parm("FilterUser"))), sm.getUserHT(),
						"All Users");
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
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND s.code_value = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		if ((!sm.Parm("FilterPriority").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterPriority").length() > 0)) {
			sb
					.append(" AND p.code_value = '" + sm.Parm("FilterPriority")
							+ "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND talternative.assigned_uid = "
					+ sm.Parm("FilterUser"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * update closed-by date and close-by-uid
	 * 
	 **************************************************************************/
	public void beforeUpdate(Hashtable<String, DbField> ht) {
		// * set the closed fields if new status is closed
		if (sm.Parm("status_cd").equalsIgnoreCase("c")
				&& !sm.Parm("oldstatuscode").equalsIgnoreCase("c")) {
			ht.put("closed_date", new DbFieldDate("closed_date", new Date()));
			ht.put("closed_by", new DbFieldInteger("closed_by_uid", sm
					.getUserId()));
		}
		// clear the closed fields if the issue is re-opened
		if (!sm.Parm("status_cd").equalsIgnoreCase("c")
				&& sm.Parm("oldstatuscode").equalsIgnoreCase("c")) {
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

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * id's
		 */

		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));

		/*
		 * codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "New"
				: db.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("ISSUTYPE")));

		ht.put("accepted_cd", new WebFieldSelect("accepted_cd", addMode ? ""
				: db.getText("accepted_cd"), sm.getCodes("YESNO")));

		ht.put("rank_cd", new WebFieldSelect("rank_cd", addMode ? "" : db
				.getText("rank_cd"), sm.getCodes("RANK10")));

		/*
		 * text
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * dates
		 */

		ht.put("closed_date", new WebFieldDisplay("closed_date", (addMode ? ""
				: db.getText("closed_date"))));

		/*
		 * Blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 5, 80));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				addMode ? "" : db.getText("resolution_blob"), 5, 80));

		ht.put("pros_blob", new WebFieldText("pros_blob", addMode ? "" : db
				.getText("pros_blob"), 5, 80));

		ht.put("cons_blob", new WebFieldText("cons_blob", addMode ? "" : db
				.getText("cons_blob"), 5, 80));

		ht.put("closed_by", new WebFieldDisplay("closed_by", (addMode ? "" : db
				.getText("last_name"))));

		/*
		 * save the old status code... do not put any under-scores in the web
		 * name so it won't get treated as a db update column
		 */
		ht.put("oldstatuscode", new WebFieldHidden("oldstatuscode",
				(addMode ? "" : db.getText("status_cd"))));

		return ht;

	}

}
