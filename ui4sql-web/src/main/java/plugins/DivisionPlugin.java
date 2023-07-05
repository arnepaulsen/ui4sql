/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import forms.WebFieldSelect;
import forms.WebFieldText;
import forms.WebFieldDisplay;
import forms.WebFieldString;
import java.util.Hashtable;

import router.SessionMgr;
import services.ServicesException;

/*******************************************************************************
 * Divsion Plugin
 * 
 * AKA "Program" now
 * 
 * 
 * Change Log:
 * 
 * 2/15 added mySql
 * 
 * 
 ******************************************************************************/

public class DivisionPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public DivisionPlugin() throws services.ServicesException {
		super();
		this.setTableName("tdivision");
		this.setKeyName("division_id");
		this.setTargetTitle("Program");
		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction (true);
		this.setIsRootTable(true);

		this.setListHeaders( new String[] { "Program" });
		this.setMoreListColumns(new  String[] { "div_name" });

		this.setMoreSelectColumns (new String[] { "project_name" });
		this.setMoreSelectJoins (new String[] { " left join tproject on tdivision.default_project_id = tproject.project_id " });

		this.setUpdatesLevel("administrator");
		this.setDeleteLevel("root");
	}


	

	/***************************************************************************
	 * 
	 * Miscelleanous Calls
	 * 
	 **************************************************************************/

	public void afterUpdate() throws services.ServicesException {

		// only in case they change the division name
		sm.cacheDivisionFilter();

	}

	public void afterAdd() throws services.ServicesException {

		sm.cacheDivisionFilter();
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * EFFICIENT: only do this on a change!!!
		 */
		if (parmMode.equalsIgnoreCase("show")) {

			// reset sm if the division id has changed.
			if (!sm.getDivisionId().equals(db.getInteger("division_id")) || true) {

				debug("division .. setDivisionId");
				sm.setDivisionId(db.getInteger("division_id"), db
						.getText("div_name"));
				
				debug("division .. setProjectId");
				sm.setProjectId(db.getInteger("default_project_id"));
				debug("division .. cacheProjectFilter");
				sm.cacheProjectFilter();
				debug("division .. cache Applicationfilter");
				sm.cacheApplicationFilter();
			}

		}

		/*
		 * Ids
		 */

		ht.put("div_mgr_id", new WebFieldSelect("div_mgr_id",
				addMode ? new Integer("0") : db.getInteger("div_mgr_id"), sm
						.getUserHT()));

		if (parmMode.equalsIgnoreCase("edit")) {

			Hashtable div_projects = new Hashtable();

			try {
				div_projects = db
						.getLookupTable("Select project_name, project_id, project_name from tproject where division_id = "
								+ db.getInteger("division_id").toString());

			} catch (ServicesException se) {

			}

			ht.put("default_project_id", new WebFieldSelect(
					"default_project_id", db.getInteger("default_project_id"),
					div_projects));
		}

		if (parmMode.equalsIgnoreCase("show")) {
			ht.put("default_project_id", new WebFieldDisplay(
					"default_project_id", db.getText("project_name")));
		}

		/*
		 * Strings
		 */
		ht.put("div_desc", new WebFieldText("div_desc", (addMode ? "" : db
				.getText("div_desc")), 4, 64));

		ht.put("div_name", new WebFieldString("div_name", (addMode ? "" : db
				.getText("div_name")), 64, 64));

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		return ht;

	}

}
