/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * GUI Data Manager
 * 
 * Change Log:
 * 
 * 5/19/05 Take out getDbFields!!
 * 
 * 
 */
public class MigratePlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public MigratePlugin() throws services.ServicesException {
		super();
		this.setTableName("tmigrate");
		this.setKeyName("migrate_id");
		this.setTargetTitle("Migration/Configuration Requests");

		this.setMoreListColumns(new  String[] { "tmigrate.title_nm", "tmigrate.reference_nm",
				"s.code_desc as status_desc", "r.code_desc as region_desc",
				"rlse.title_nm as RlseTitle", "s.code_value",
				"r.code_value"});

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tmigrate.status_cd = s.code_value and s.code_type_id  = 91 ",
				" left join tcodes r on tmigrate.region_cd = r.code_value and r.code_type_id  = 90 ",
				" left join trelease rlse on tmigrate.release_id = rlse.release_id " });

		//this.setMoreSelectJoins (new String[] { " left join trelease tc on tmigrate.release_id = tc.release_id  " };
		//this.setMoreSelectColumns (new String[] { "tc.title_nm as release_title" };

		this.setListHeaders( new String[] { "Title", "Reference", "Status",
				"Region", "Release" });

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
					"FilterStatus").length() == 0 ? "P" : sm
					.Parm("FilterStatus")), sm.getCodes("STATPC"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		} else {
			if (columnNumber == 3) {

				WebFieldSelect wf = new WebFieldSelect("FilterRegion", sm
						.Parm("FilterRegion"), sm.getCodes("TESTREG"),
						"All Regions");
				wf.setDisplayClass("listform");
				return wf;
			} else {
				debug("building wfSelect for Users:");
				debug("parm userid : " + sm.Parm("FilterUser"));

				Hashtable releases = sm.getTable("trelease", "select title_nm, release_id, title_nm from trelease");
				
				WebFieldSelect wf = new WebFieldSelect("FilterRelease", (sm.Parm(
						"FilterUser").length() == 0 ? new Integer("0")
						: new Integer(sm.Parm("FilterUser"))), releases,
						"All Releases");
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
			sb.append(" AND s.code_value = 'P'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		if ((!sm.Parm("FilterRegion").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterRegion").length() > 0)) {
			sb
					.append(" AND r.code_value = '" + sm.Parm("FilterRegion")
							+ "'");
		}

		if ((!sm.Parm("FilterRelease").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterRelease").length() > 0)) {
			sb.append(" AND tmigrate.release_id = " + sm.Parm("FilterRelease"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
				
		/*
		 * Ids
		 */

		ht.put("assigned_uid", new WebFieldSelect("assigned_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assigned_uid"), sm.getUserHT()));
		
		ht.put("closed_uid", new WebFieldSelect("closed_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("closed_uid"), sm.getUserHT()));
		

		ht.put("release_id", new WebFieldSelect("release_id", addMode ? new Integer("0") : db
				.getInteger("release_id"),  db.getLookupTable("trelease",
				"release_id", "title_nm")));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 128));

		/*
		 * Dates
		 */
		ht.put("closed_date", new WebFieldDate("closed_date", (addMode ? ""
				: db.getText("closed_date"))));

		/*
		 * Codes
		 */
		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("STATPC")));
		
		ht.put("region_cd", new WebFieldSelect("region_cd", addMode ? "O" : db
				.getText("region_cd"), sm.getCodes("TESTREG")));
		

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				addMode ? "" : db.getText("resolution_blob"), 3, 80));

		ht.put("dependency_blob", new WebFieldText("dependency_blob",
				addMode ? "" : db.getText("dependency_blob"), 3, 80));

		/*
		 * Return
		 */
		return ht;

	}
}
