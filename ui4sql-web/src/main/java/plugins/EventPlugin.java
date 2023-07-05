/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import services.ServicesException;
import forms.*;

/**
 * Job Plugin
 * 
 * Change Log:
 * 
 * 9/20/06 'title nm' was ht.put twice.
 * 
 * 
 * 
 * KEYWORDS  : SQLSERVER ... watch for 'cast' on mySQL
 * 
 */
public class EventPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public EventPlugin() throws services.ServicesException {
		super();
		this.setTableName("tevent");
		this.setKeyName("event_id");
		this.setListOrder ("list_order");
		
		this.setTargetTitle("Production Event");

		this.setListHeaders( new String[] { "Date", "Title", "Status",
				 "Type", "Interface", "Owner" });

		this.setMoreListJoins(new  String[] {
				" left join tinterface i  on tevent.interface_id = i.interface_id",
				" left join tcodes s on tevent.status_cd = s.code_value and s.code_type_id  = 45 ",
				" left join tcodes t on tevent.type_cd = t.code_value and t.code_type_id  = 82 ",
				" left join tuser u on tevent.owner_uid = u.user_id " });

		this.setMoreListColumns(new  String[] {
				"cast(start_date as char(11)) as start_date",
				"tevent.title_nm", "s.code_desc as status_desc",
				"t.code_desc as type_desc",
				"i.title_nm as interface_name", "u.last_name + ',' + substring(first_name,1,1)", "start_date as list_order" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 2 || columnNumber == 4  || columnNumber == 5)
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

		
		if (columnNumber == 4) {
			// bridges interface
			Hashtable interface_ht = sm
					.getTable(
							"tinterface",
							"select title_nm, interface_id, title_nm from tinterface order by title_nm ");
			return getListSelector("FilterInterface", new Integer("0"),
					"All Interfaces", interface_ht);
		}
		
		// unreachable code
		
		if (columnNumber == 5) {
			// TODO.. Resource HOG!!! It queries every time. Let the db manager
			// cache the data.
			String qry = new String(
					"select distinct concat(u.last_name, ', ', substring(u.first_name,1,1)) as a , "
							+ "u.user_id as b, concat(u.last_name, ', ',	substring(u.first_name,1,1)) as c "
							+ " from tevent left join tuser u on tevent.owner_uid = u.user_id "
							+ " where isnull(owner_uid, 0) > 0 and len(u.first_name) > 1 and len(u.last_name) > 1");

			Hashtable users = new Hashtable();

			try {
				users = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}
			return getListSelector("FilterOwner", new Integer("0"), "Owner ? ",
					users);
		}
		

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
		
		//		 filter on interface
		if (sm.Parm("FilterInterface").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterInterface").equalsIgnoreCase("0")) {
				sb.append(" AND tevent.interface_id = "
						+ sm.Parm("FilterInterface"));
			}
		}
		
		
//		 filter on interface
		if (sm.Parm("FilterOwner").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterOwner").equalsIgnoreCase("0")) {
				sb.append(" AND tevent.owner_uid = "
						+ sm.Parm("FilterOwner"));
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

		Hashtable interfaces = sm
				.getTable(
						"tinterface",
						"select title_nm, interface_id, title_nm from tinterface  "   +
						" union " +
						" select ' Multiple' as 'a', 0 as 'b' , ' Multiple' as 'c' " +  
						" order by 1 ");

		ht.put("interface_id", new WebFieldSelect("interface_id",
				addMode ? new Integer("0") : db.getInteger("interface_id"),
				interfaces));

		Hashtable bridges = sm
		.getTable("bridges_staff1",
				"select last_name, user_id, last_name + ',' + substring(last_name,1,1) from tuser where default_appl_id = 13  or sox_role_flag = 'Y' order by last_name ");
		
		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? sm.getUserId() : db.getInteger("owner_uid"),
				bridges, true));
		
		
		/*
		 * Strings
		 */


		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("act_hours_tx", new WebFieldString("act_hours_tx", (addMode ? ""
				: db.getText("act_hours_tx")), 6, 6));
		
		ht.put("pr_id", new WebFieldString("pr_id", (addMode ? ""
				: db.getText("pr_id")), 8, 8));
		/*
		 * Dates
		 */

		ht.put("start_date", new WebFieldDate("start_date", addMode ? "" : db
				.getText("start_date")));

		ht.put("time_tx", new WebFieldString("time_tx", (addMode ? ""
				: db.getText("time_tx")), 8, 8));

		
		/*
		 * Codes
		 */

		ht.put("instance_cd", new WebFieldSelect("instance_cd", addMode ? "" : db
				.getText("instance_cd"), sm.getCodes("INSTANCE")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("ISSUESTAT")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("INCIDENTTYPE")));

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
