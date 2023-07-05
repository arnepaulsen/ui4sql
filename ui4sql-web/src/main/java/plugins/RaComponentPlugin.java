/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import services.ServicesException;

/**
 * Software components detail list for an Epic RA
 * 
 * Child of plugin Ra, child of table tra
 * 
 * Change log:
 * 
 * get ra_table from db instead of session.. in order to pick up new ra's just
 * added.
 * 
 * 11/27/06 don't cache ra table...
 * 
 * Keywords: RA , Epic
 * 
 * todo: move ra table up to the application level
 */

public class RaComponentPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public RaComponentPlugin() throws services.ServicesException {
		super();
		this.setTableName("tra_component");
		this.setKeyName("ra_component_id");
		this.setTargetTitle("RA Component");
		this.setShowAuditSubmitApprove(false);

		this.setIsDetailForm(true);
		this.setIsStepChild(true);
		this.setParentTarget("Ra");

		this.setListHeaders(new String[] { "RA", "Type", "Id", "RA Status" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes on tra_component.type_cd = tcodes.code_value and tcodes.code_type_id  = 103 ",
						" left join tra ra on tra_component.ra_id = ra.ra_id ",
						" left join tcodes stat on ra.status_cd = stat.code_value and stat.code_type_id = 101" });

		this.setMoreListColumns(new String[] { "ra.reference_nm as ra_nm",
				"type_cd", "component_id", "stat.code_desc" });

		this
				.setMoreSelectJoins(new String[] {
						" left join tra  on tra_component.ra_id = tra.ra_id ",
						" left join tapplications on tra.application_id = tapplications.application_id" });

		this.setMoreSelectColumns(new String[] { "tra.reference_nm as ra_nm",
				"application_name" });

	}

	/*
	 * 
	 * List Control
	 * 
	 * @see plugins.Plugin#listColumnHasSelector(int)
	 */
	public boolean listColumnHasSelector(int columnNumber) {

		return true;

	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		if ((sm.Parm("FilterRa").length() > 0)
				&& (!sm.Parm("FilterRa").equalsIgnoreCase("0"))) {
			sb.append(" AND ra.ra_id= " + sm.Parm("FilterRa"));
		}

		if ((sm.Parm("FilterType").length() > 0)
				&& (!sm.Parm("FilterType").equalsIgnoreCase("0"))) {
			sb.append(" AND tra_component.type_cd = '" + sm.Parm("FilterType")
					+ "'");
		}

		if (sm.Parm("FilterComponent").length() > 1) {
			sb.append(" AND tra_component.component_id = "
					+ sm.Parm("FilterComponent"));
		}

		if ((sm.Parm("FilterStatus").length() > 0)
				&& (!sm.Parm("FilterStatus").equalsIgnoreCase("0"))) {
			sb.append(" AND ra.status_cd = '" + sm.Parm("FilterStatus") + "' ");
		}

		return sb.toString();

	}

	public WebField getListSelector(int columnNumber) {

		String component_query = "";

		// Hashtable ras = sm.getTable("tRaComponent", query);

		if (columnNumber == 0) {
			Hashtable ras = new Hashtable();

			String ra_query = new String(
					"select reference_nm, ra_id, reference_nm from tra where application_id = "
							+ sm.getApplicationId().toString());
			try {
				ras = db.getLookupTable(ra_query);
			} catch (Exception e) {
			}
			return getListSelector("FilterRa", new Integer("0"), "-All RA-",
					ras);
		}

		if (columnNumber == 1) {
			return getListSelector("FilterType", "", "All Types", sm
					.getCodes("CACHETYPES"));
		}

		/*
		 * Filter AIP, AIC, etc.
		 */
		if (columnNumber == 2) {
			if (sm.Parm("FilterType").length() > 1)

				component_query = "select distinct component_id as theKey, component_id, cast(component_id as varchar(8)) as 'refer' from tra_component where type_cd = '"
						+ sm.Parm("FilterType") + "' order by component_id ";

			else
				component_query = "select distinct component_id, component_id, cast(component_id as varchar(8)) as 'refer'  from tra_component order by component_id ";

			try {
				return getListSelector("FilterComponent", new Integer("0"),
						"All Componnets", db.getLookupTable(component_query));
			} catch (ServicesException e) {
				return getListSelector("FilterComponent", new Integer("0"),
						"All Componnets", new Hashtable());
			}
		}

		/*
		 * Status Code
		 */
		if (columnNumber == 3) {
			return getListSelector("FilterStatus", "O", "All RA Status", sm
					.getCodes("STATUS1"));
		}

		/* can never get here */

		return getListSelector("FilterUnknown", new Integer("0"),
				"All Componnets", new Hashtable());

	}

	

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		if (addMode) {
			getAddFields(ht);
		}

		if (parmMode.equalsIgnoreCase("show")
				|| parmMode.equalsIgnoreCase("edit")) {

			getUpdateFields(ht);
		}

		return ht;

	}

	private void getAddFields(Hashtable<String, WebField> ht) {

		String query = "select reference_nm, ra_id, reference_nm from tra "
				+ " where application_id = " + sm.getApplicationId().toString();

		Hashtable ra = new Hashtable();
		try {
			ra = db.getLookupTable(query);
		}

		catch (ServicesException sql) {
			debug(sql.toString());
		}

		ht.put("ra_id", new WebFieldSelect("ra_id", new Integer(sm
				.Parm("FilterRa")), ra));

		ht.put("type_cd", new WebFieldSelect("type_cd", db.getText("type_cd"),
				sm.getCodes("CACHETYPES")));

		ht.put("component_id", new WebFieldString("component_id", db
				.getText("component_id"), 8, 8));

		ht.put("msg", new WebFieldDisplay("msg",
				"Please  select an RA, type, and component.  Then Save-Edit."));

		ht.put("desc_blob", new WebFieldText("desc_blob", db
				.getText("desc_blob"), 5, 100));

		return;

	}

	private void getUpdateFields(Hashtable<String, WebField> ht) {

		/*
		 * Display-only fields from tRa
		 */

		ht.put("ra_id", new WebFieldDisplay("ra_id", db.getText("ra_nm")));

		ht.put("component_id", new WebFieldDisplay("component_id", db
				.getText("component_id")));

		/*
		 * Codes
		 */

		ht.put("type_cd", new WebFieldSelect("type_cd", db.getText("type_cd"),
				sm.getCodes("CACHETYPES")));

		/*
		 * Blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", db
				.getText("desc_blob"), 5, 100));

		return;

	}

}
