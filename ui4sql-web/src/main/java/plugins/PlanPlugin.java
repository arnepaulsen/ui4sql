/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * General Plans
 * 
 * 
 * Plan types are from codes table 'PLANTYPES'.
 * 
 * Examples. ... like Training, Communication, Education.
 * 
 * All plans are stored in tplan table, and the questions come from custom
 * fields depending on the plan types.
 * 
 * This creates a more general questionaire, but allows administrators ability
 * to set up any time of plan.
 * 
 * So the tool bar just has 'General Plans' then gets a list of all the general
 * plans, regardless of type.
 * 
 * Could add a filter for plan type, but there won't be that many for a project
 * that you would need to filter. Just show all plans on the list, and thier
 * type.
 * 
 * For add mode, first user has to pick what type of plan, then the questions
 * are presented.
 * 
 * 
 * 
 */
public class PlanPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public PlanPlugin() throws services.ServicesException {
		super();
		this.setTableName("tplan");
		this.setKeyName("plan_id");
		this.setTargetTitle("Plan");
		this.setListHeaders(new String[] { "Type", "Title", "Reference",
				"Status" });

		this.setMoreListColumns(new String[] { "kind.code_desc as KindDesc",
				"title_nm", "reference_nm", "stat.code_desc as StatDesc" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes stat on tplan.status_cd = stat.code_value and stat.code_type_id  = 5 ",
						" left join tcodes kind on tplan.type_cd = kind.code_value and kind.code_type_id  = 79 " });

		this
				.setMoreSelectColumns(new String[] { "kind.code_desc as PlanType" });

		this
				.setMoreSelectJoins(new String[] { " left join tcodes kind on tplan.type_cd = kind.code_value and kind.code_type_id  = 79 " });

	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	// this limits the custom fields to just those for the plan type
	public String getCustomSubForm() {
		return db.getText("type_cd");
	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht;

		if (addMode) {
			this.setAddCustomFields(false);
			ht = showNewPage();
		} else {
			ht = showUpdatePage();
		}
		return ht;

	}

	private Hashtable showNewPage() {

		Hashtable ht = new Hashtable();

		ht.put("type_cd", new WebFieldSelect("type_cd", "C", sm
				.getCodes("PLANTYPES")));

		ht.put("title_nm", new WebFieldString("title_nm", db
				.getText("title_nm"), 64, 64));

		ht.put("msg", new WebFieldDisplay("msg",
				"Please  select a plan type, then Save-Edit."));

		return ht;

	}

	public Hashtable showUpdatePage() throws services.ServicesException {

		Hashtable ht = new Hashtable();

		/*
		 * Id's
		 */
		ht.put("owner_uid", new WebFieldSelect("owner_uid", db
				.getInteger("owner_uid"), sm.getUserHT()));

		/*
		 * Display the plan type, not allowed to change here.
		 */

		ht.put("type_cd",
				new WebFieldDisplay("type_cd", db.getText("PlanType")));

		/*
		 * Codes
		 * 
		 */
		ht.put("status_cd", new WebFieldSelect("status_cd", db
				.getText("status_cd"), sm.getCodes("STATUS")));

		/*
		 * Strings
		 */

		ht.put("title_nm", new WebFieldString("title_nm", db
				.getText("title_nm"), 64, 64));

		ht.put("reference_nm", new WebFieldString("reference_nm", db
				.getText("reference_nm"), 32, 32));

		ht.put("version_nm", new WebFieldString("version_nm", db
				.getText("version_nm"), 16, 16));

		/*
		 * Dates
		 */
		ht.put("final_date", new WebFieldString("final_date", db
				.getText("final_date"), 10, 10));

		return ht;

	}
}
