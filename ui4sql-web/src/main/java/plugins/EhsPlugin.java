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
 * SQL Notes:
 * 	division_id is defaulting to value 1 on an insert
 * 
 * 
 */
public class EhsPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public EhsPlugin() throws services.ServicesException {
		super();
		this.setTableName("tehs");
		this.setKeyName("ehs_id");
		this.setListOrder ("ehs_id; ");
				//"list_order";
		
		/*
		 * child form
		 */
		this.setHasDetailForm (true);
		this.setDetailTarget ("EhsHL7");
		this.setDetailTargetLabel ("HL7");

		this.setTargetTitle("EHS Events");

		this.setListHeaders( new String[] { "Date", "Status", "Instance", "Interface", "Owner" });

		this.setMoreListJoins(new  String[] {
				" left join tinterface i  on tehs.interface_id = i.interface_id",
				" left join tcodes s on tehs.status_cd = s.code_value and s.code_type_id  = 45 ",
				" left join tuser u on tehs.assigned_uid = u.user_id " });

		this.setMoreListColumns(new  String[] {
				"cast(error_date as char(11)) as error_date",
				"s.code_desc as status_desc", "instance_cd",
				"interface_nm", "u.last_name", "error_date as list_order" });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 1 || columnNumber == 4 )
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 1)
			return getListSelector("FilterStatus", "O", "All Status", sm
					.getCodes("STATUS1"));

		
		if (columnNumber == 4) {
			// bridges interface
	
			return getListSelector("FilterAssigned", new Integer("0"),
					"All Assignees", sm.getUserHT());
		}
		
		// unreachable code

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
		if (sm.Parm("FilterAssigned").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterAssigned").equalsIgnoreCase("0")) {
				sb.append(" AND tehs.assigned_uid = "
						+ sm.Parm("FilterAssigned"));
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

		
		// save my key info for the Stage plugin
		if (parmMode.equalsIgnoreCase("show")) {
			sm
					.setParentId(db.getInteger("ehs_id"), "blah");
		}
		
		
		Hashtable interfaces = sm
				.getTable(
						"tinterface",
						"select title_nm, interface_id, title_nm from tinterface  "   +
						" union " +
						" select ' Multiple' as 'a', 0 as 'b' , ' Multiple' as 'c' " +  
						" order by 1 ");

		ht.put("interface_id", new WebFieldSelect("interface_id",
				addMode ? new Integer("0") : db.getInteger("interface_id"),
				interfaces, true));

		Hashtable bridges = sm
		.getTable("bridges_staff",
				"select last_name, user_id, last_name from tuser where default_appl_id = 13  order by last_name ");
		
		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? sm.getUserId() : db.getInteger("assigned_uid"),
				bridges));
		
		
		/*
		 * Strings
		 */


		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("error_tx", new WebFieldDisplay("error_tx", db.getText("error_tx")));


		ht.put("error_file_nm", new WebFieldDisplay("error_file_nm", db.getText("error_file_nm")));

		
		ht.put("interface_nm", new WebFieldDisplay("interface_nm", db.getText("interface_nm")));
		
		ht.put("queue_nm", new WebFieldDisplay("queue_nm", db.getText("queue_nm")));
		
		ht.put("broker_nm", new WebFieldDisplay("broker_nm", db.getText("broker_nm")));
		
		ht.put("resubmit_queue_nm", new WebFieldDisplay("resubmit_queue_nm", db.getText("resubmit_queue_nm")));
			
		ht.put("mrn_tx", new WebFieldDisplay("mrn_tx", db.getText("mrn_tx")));

		
		/*
		 * Dates
		 */

		ht.put("error_date", new WebFieldDisplay("error_date", db
				.getText("error_date")));

		ht.put("time_tx", new WebFieldDisplay("time_tx", db.getText("time_tx")));

		
		/*
		 * Codes
		 */

		ht.put("instance_cd", new WebFieldDisplay("instance_cd", db.getText("instance_cd")));
		

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("ISSUESTAT")));

	
	
		/*
		 * Blobs
		 */


		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("impact_blob", new WebFieldText("impact_blob",
				(addMode ? "" : db.getText("impact_blob")), 5, 100));
		
		ht.put("cause_blob", new WebFieldText("cause_blob",
				(addMode ? "" : db.getText("cause_blob")), 5, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 100));

		ht.put("followup_blob", new WebFieldText("followup_blob", (addMode ? "" : db
				.getText("followup_blob")), 5, 100));
		
		ht.put("resolution_blob", new WebFieldText("resolution_blob", (addMode ? "" : db
				.getText("resolution_blob")), 5, 100));
		/*
		 * Return ht
		 */

		return ht;

	}

}
