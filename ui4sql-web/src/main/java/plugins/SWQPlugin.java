/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/*******************************************************************************
 * Change Request Plugin
 * 
 * 3/22 New Page
 * 
 */

public class SWQPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private static String[] sListHeaders = { "#", "Title", "Type", "Status",
			"Region", "Severity" , "Drop Date"};

	private static String[] extraListColumns = { "track_no",
			"substring(title_nm,1,50) as theTitle ",
			"tt.code_desc as type_cod", "stat.code_desc as stat_cod",
			"reg.code_desc as theregion", "serv.code_desc as theseverity", "cast(act_drop_dt as varchar(11)) as dropdate" };

	private static String[] extraJoins = {
			" left join tcodes stat on tswq.status_cd = stat.code_value and stat.code_type_id  = 113  ",
			" left join tcodes serv on tswq.severity_cd = serv.code_value and serv.code_type_id  = 112  ",
			" left join tcodes reg on tswq.region_cd = reg.code_value and reg.code_type_id  = 108  ",
			" left join tcodes tt on tswq.track_type_cd = tt.code_value and tt.code_type_id  = 114  " };

	public SWQPlugin() throws services.ServicesException {
		super();
		this.setTableName("tswq");
		this.setTargetTitle("Single Work Queue");
		this.setKeyName("swq_id");

		this.setMoreListColumns (extraListColumns);
		this.setMoreListJoins (extraJoins);
		this.setListHeaders (sListHeaders);
	}

	/***************************************************************************
	 * 
	 * List Filters
	 * 
	 **************************************************************************/

	public boolean getListColumnCenterOn(int columnNumber) {
		if (columnNumber == 12 || columnNumber == 23 || columnNumber == 24)
			return false;
		else
			return false;
	}

	public boolean listColumnHasSelector(int columnNumber) {
		// just the status column
		if (columnNumber == 2 || columnNumber == 3 || columnNumber == 4
				|| columnNumber == 5)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2)
			return getListSelector("FilterType", "", "Type", sm
					.getCodes("SWQTYPE"));

		if (columnNumber == 3)
			return getListSelector("SWQFilterStatus", "", "Status", sm
					.getCodes("SWQSTAT"));

		if (columnNumber == 4)
			return getListSelector("FilterRegion", "NCAL", "Region", sm
					.getCodes("REGION"));

		if (columnNumber == 5)
			return getListSelector("FilterSeverity", "NCAL", " Severity", sm
					.getCodes("SEVERITY4"));

		// will never get here

		return getListSelector("dummy", new Integer(""), "badd..",
				new Hashtable());

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// COLUMN 4 = Patient Safety

		if (sm.Parm("FilterType").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterType").equalsIgnoreCase("0")) {
				sb.append(" AND tswq.track_type_cd = '" + sm.Parm("FilterType")
						+ "'");
			}
		}

		// COLUMN 5 = Region

		if (sm.Parm("FilterRegion").length() == 0) {
			sb.append(" AND tswq.region_cd = 'NCAL'");
		}

		else {
			if (!sm.Parm("FilterRegion").equalsIgnoreCase("0")) {
				sb.append(" AND tswq.region_cd = '" + sm.Parm("FilterRegion")
						+ "'");
			}
		}

		// COLUMN 6 = Status

		debug(("tswq: Filter status " + sm.Parm("SWQFilterStatus")));
		
		if (sm.Parm("SWQFilterStatus").length() == 0) {
			debug("tswq: defaulting status to not closed");
			sb.append(" AND tswq.status_cd <> '10' AND tswq.status_cd <> '08' ");
		}

		else {
			if (!sm.Parm("SWQFilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND tswq.status_cd = '" + sm.Parm("SWQFilterStatus")
						+ "'");
			}
		}

		if (sm.Parm("FilterSeverity").length() == 0) {

		}

		else {
			if (!sm.Parm("FilterSeverity").equalsIgnoreCase("0")) {
				sb.append(" AND tswq.severity_cd = '"
						+ sm.Parm("FilterSeverity") + "'");
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
		 * Codes using internal arrays
		 */

		String[][] aRelType = new String[][] { { "S", "NS", "CD" },
				{ "Sync", "Non-Sync", "C/E" } };

		ht.put("rel_type_cd", new WebFieldSelect("rel_type_cd", addMode ? ""
				: db.getText("rel_type_cd"), aRelType));

		ht.put("region_cd", new WebFieldSelect("region_cd", addMode ? "C" : db
				.getText("region_cd"), sm.getCodes("REGION")));

		ht.put("severity_cd", new WebFieldSelect("severity_cd", addMode ? ""
				: db.getText("severity_cd"), sm.getCodes("SEVERITY4")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "New"
				: db.getText("status_cd"), sm.getCodes("SWQSTATUS")));

		ht.put("track_type_cd", new WebFieldSelect("track_type_cd",
				addMode ? "" : db.getText("track_type_cd"), sm
						.getCodes("SWQTYPE")));

		

		/*
		 * Dates
		 */
		ht.put("est_drop_dt", new WebFieldDate("est_drop_dt",
				addMode ? "" : db.getText("est_drop_dt")));
		
		ht.put("act_drop_dt", new WebFieldDate("act_drop_dt",
				addMode ? "" : db.getText("act_drop_dt")));

		/*
		 * Strings
		 */

		
		ht.put("appl_contact_nm", new WebFieldString("appl_contact_nm",
				(addMode ? "" : db.getText("appl_contact_nm")), 32, 32));
		
		ht.put("originator_nm", new WebFieldString("originator_nm",
				(addMode ? "" : db.getText("originator_nm")), 32, 32));
		
		ht.put("product_cd", new WebFieldString("product_cd",
				(addMode ? "" : db.getText("product_cd")), 6, 6));
		
		ht.put("std_rlse_tx", new WebFieldString("std_rlse_tx",
				(addMode ? "" : db.getText("std_rlse_tx")), 8, 8));
		
		
		ht.put("retro_rlse_tx", new WebFieldString("retro_rlse_tx",
				(addMode ? "" : db.getText("retro_rlse_tx")), 32,32));
		
		ht.put("track_no", new WebFieldString("track_no",
				(addMode ? "" : db.getText("track_no")), 6, 6));
		
		ht.put("charge_order_tx", new WebFieldString("charge_order_tx",
				(addMode ? "" : db.getText("charge_order_tx")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("dlg_nm", new WebFieldString("dlg_nm", (addMode ? "" : db
				.getText("dlg_nm")), 32, 32));

		ht.put("server_pkg_cd", new WebFieldString("server_pkg_cd",
				(addMode ? "" : db.getText("server_pkg_cd")), 32, 32));

		ht.put("client_pkg_cd", new WebFieldString("client_pkg_cd",
				(addMode ? "" : db.getText("client_pkg_cd")), 32, 32));

		ht.put("adhoc_no", new WebFieldString("adhoc_no", (addMode ? "" : db
				.getText("adhoc_no")), 16, 16));

		ht.put("related_rn_tx", new WebFieldString("related_rn_tx", (addMode ? ""
				: db.getText("related_rn_tx")), 16, 16));

		/*
		 * Dates
		 */

		/*
		 * Blobs
		 */
		ht.put("notes_tx", new WebFieldText("notes_tx", addMode ? "" : db
				.getText("notes_tx"), 6, 120));

		/*
		 * Return
		 */

		return ht;

	}

}
