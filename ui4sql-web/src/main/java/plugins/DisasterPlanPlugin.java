/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Business Plan Plugin
 * 
 * Change log:
 * 
 * 6/13 this form custom fields for most of the data input, as business plans
 * will be very unique for each customer
 * 
 * database fields are q01_blob.... q30_blob
 * 
 */
public class DisasterPlanPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	boolean addMode;

	public DisasterPlanPlugin() throws services.ServicesException {
		super();
		this.setTableName("tdisaster_plan");
		this.setKeyName("disaster_plan_id");
		this.setTargetTitle("Disaster Plans");
		this.setListOrder("section_nm");

		this.setListHeaders(new String[] { "Section", "Version", "Status" });

		this.setMoreListColumns(new String[] {
				"section.code_desc as section_nm", "version_nm",
				"stat.code_desc as status_nm" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes as stat on tdisaster_plan.status_cd = stat.code_value and stat.code_type_id  = 5 ",
						" left join tcodes as section on tdisaster_plan.section_cd = section.code_value and section.code_type_id  = 42 " });

		this
				.setMoreSelectColumns(new String[] { "section.code_desc as section_nm " });

		this
				.setMoreSelectJoins(new String[] { " left join tcodes as section on tdisaster_plan.section_cd = section.code_value and section.code_type_id  = 42 " });

	}

	/*
	 * used by the Plugin driver to filter the custom questions down to a
	 * sub-set .. in this case, we filter by Business Plan 'Section'.
	 */
	public String getCustomSubForm() {
		return db.getText("section_cd");
	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht;

		if (addMode) {
			ht = showNewPage();
			this.setAddCustomFields(false);
		} else {
			this.setAddCustomFields(true);
			ht = showUpdatePage();
		}
		return ht;

	}

	private Hashtable showNewPage() {

		Hashtable ht = new Hashtable();

		ht.put("dmaic_cd", new WebFieldSelect("section_cd", "S201", sm
				.getCodes("DSTRPLAN")));

		ht
				.put(
						"msg",
						new WebFieldDisplay("msg",
								"Please select a project and a plan section, then choose Save-Edit."));

		return ht;

	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable showUpdatePage() throws services.ServicesException {

		/*
		 * the project name will not return in the web form, because it is not
		 * in <INPUT> type element!
		 */
		WebFieldDisplay wfSection = new WebFieldDisplay("section_cd", db
				.getText("section_nm"));

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", db
				.getText("status_cd"), sm.getCodes("STATUS"));

		WebFieldString wfVersion = new WebFieldString("version_nm", db
				.getText("version_nm"), 6, 6);

		WebField[] wfs = { wfStatus, wfSection, wfVersion, };

		return webFieldsToHT(wfs);

	}

}
