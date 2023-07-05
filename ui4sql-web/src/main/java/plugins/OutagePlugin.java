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

/**
 * Job Plugin
 * 
 * Change Log:
 * 
 * 9/20/06 'title nm' was ht.put twice.
 * 
 */
public class OutagePlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public OutagePlugin() throws services.ServicesException {
		super();
		this.setTableName("toutage");
		this.setKeyName("outage_id");
		this.setTargetTitle("Production Outage");
		this.setListOrder ("start_dttm");

		this.setListHeaders( new String[] { "Date/Time", "Title", "Status",
				"Full", "Hours", "Planned", "Reason", "Owner" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on toutage.status_cd = s.code_value and s.code_type_id  = 45 ",
				" left join tcodes t on toutage.type_cd = t.code_value and t.code_type_id  = 132 ",
				" left join tcodes p on toutage.planned_cd = p.code_value and p.code_type_id  = 3 ",
				" left join tcodes f on toutage.full_flag = f.code_value and f.code_type_id  = 102 ",
				" left join tuser u on toutage.owner_id = u.user_id " });

		this.setMoreSelectJoins (new String[] {
				" left join trfc on toutage.rfc_no = trfc.rfc_no ",
				" left join tcontact owner on trfc.owner_uid = owner.contact_id ",
				" left join tcodes rem_urgency  on trfc.urgency_cd = rem_urgency.code_value and rem_urgency.code_type_id = 2 ", 
				" left join tcodes rem_status  on trfc.status_cd = rem_status.code_value and rem_status.code_type_id = 118 "});

	
		this.setMoreListColumns(new  String[] { "start_dttm", "toutage.title_nm",
				"s.code_desc as status_desc", "f.code_desc as full_desc", "plan_hours_tx",
				"p.code_desc as plan_desc", "t.code_desc as type_desc",
				"concat(u.last_name, ',', u.first_name)" });

	}

	public void init(SessionMgr parmSm) {

		super.init(parmSm);
		
		this.setMoreSelectColumns (new String[] {
				"rem_urgency.code_desc as remedyUrgency ",
				"rem_status.code_desc as remedyStatus ",
				"FormatDateTime(trfc.remedy_end_dt, 'SHORTDATEANDTIME') as fmt_remedy_end_dt",
				"concat(owner.last_name , ',' , owner.first_name) as remedyOwner  ", });

		
		
	
	}
	
	
	public boolean getListColumnCenterOn(int i) {
		if (i > 1 && i < 5)
			return true;
		else
			return false;
	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 2)
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2)
			return getListSelector("FilterStatus", "O", "All Status", sm
					.getCodes("STATUS1"));

		// will never get here

		return getListSelector("dummy", new Integer(""), "badd..",
				new Hashtable());

	}

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
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

		return sb.toString();
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
		 * Ids
		 */

		Hashtable hosts = sm
				.getTable(
						"thosts1",
						"select host_nm, host_id, host_nm from thost "
								+ " union "
								+ " select ' Multiple' as 'a', 0 as 'b' , ' Multiple' as 'c' "
								+ " order by 1 ");

		Hashtable procedures = sm
				.getTable("tprocedure",
						"select title_nm, procedure_id, title_nm from tprocedure order by 1 ");

		ht.put("host_id", new WebFieldSelect("host_id", addMode ? new Integer(
				"0") : db.getInteger("host_id"), hosts));

		ht.put("procedure_id", new WebFieldSelect("procedure_id",
				addMode ? new Integer("0") : db.getInteger("procedure_id"),
				procedures));

		ht.put("owner_id",
				new WebFieldSelect("owner_id", addMode ? new Integer("0")
						: (Integer) db.getObject("owner_id"), sm.getUserHT(),
						false, true));

		ht
				.put("contact_uid", new WebFieldSelect("contact_uid",
						addMode ? new Integer("0") : (Integer) db
								.getObject("contact_uid"), sm.getUserHT(),
						false, true));

		/*
		 * Remedy
		 */

		ht.put("remedyEnd", new WebFieldDisplay("remedyEnd", (addMode ? ""
				: db.getText("fmt_remedy_end_dt"))));
		
		ht.put("remedyOwner", new WebFieldDisplay("remedyOwner", (addMode ? ""
				: db.getText("remedyOwner"))));

		ht.put("remedyStatus", new WebFieldDisplay("remedyStatus",
				(addMode ? "" : db.getText("remedyStatus"))));
		
		ht.put("remedyUrgency", new WebFieldDisplay("remedyUrgency",
				(addMode ? "" : db.getText("remedyUrgency"))));

		/*
		 * Strings
		 */

		ht.put("rfc_no", new WebFieldString("rfc_no", (addMode ? "" : db
				.getText("rfc_no")), 32, 32));

		ht.put("pr_no", new WebFieldString("pr_no", (addMode ? "" : db
				.getText("pr_no")), 32, 32));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("act_hours_tx", new WebFieldString("act_hours_tx", (addMode ? ""
				: db.getText("act_hours_tx")), 6, 6));

		ht.put("plan_hours_tx", new WebFieldString("plan_hours_tx",
				(addMode ? "" : db.getText("plan_hours_tx")), 6, 6));

		/*
		 * Dates
		 */

		ht.put("start_dttm", new WebFieldDateTime("start_dttm", addMode ? ""
				: db.getText("start_dttm")));

		ht.put("end_dttm", new WebFieldDate("end_dttm", addMode ? "" : db
				.getText("end_dttm")));

		/*
		 * Codes
		 */

		ht.put("full_flag", new WebFieldSelect("full_flag", addMode ? "" : db
				.getText("full_flag"), sm.getCodes("YESNONA")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("OUTAGES")));

		ht.put("planned_cd", new WebFieldSelect("planned_cd", addMode ? "" : db
				.getText("planned_cd"), sm.getCodes("YESNO")));

		ht.put("impact_cd", new WebFieldSelect("impact_cd", addMode ? "" : db
				.getText("impact_cd"), sm.getCodes("HIGHMEDLOW")));

		/*
		 * Blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				(addMode ? "" : db.getText("resolution_blob")), 5, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 100));

		ht.put("followup_blob", new WebFieldText("followup_blob", (addMode ? ""
				: db.getText("followup_blob")), 5, 100));

		ht.put("cause_blob", new WebFieldText("cause_blob", (addMode ? "" : db
				.getText("cause_blob")), 5, 100));

		ht.put("eta_blob", new WebFieldText("eta_blob", (addMode ? "" : db
				.getText("eta_blob")), 5, 100));

		ht.put("communication_blob", new WebFieldText("communication_blob",
				(addMode ? "" : db.getText("communication_blob")), 5, 100));

		ht.put("mitigation_blob", new WebFieldText("mitigation_blob",
				(addMode ? "" : db.getText("mitigation_blob")), 5, 100));

		ht.put("cause_blob", new WebFieldText("cause_blob", (addMode ? "" : db
				.getText("cause_blob")), 5, 100));
		/*
		 * Return ht
		 */

		return ht;

	}

}
