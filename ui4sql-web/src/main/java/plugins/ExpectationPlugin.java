/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * 
 * 2/15 added mySql 3/10 use tcodes
 */

public class ExpectationPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ExpectationPlugin() throws services.ServicesException {

		super();
		this.setTableName("texpectation");
		this.setKeyName("expectation_id");
		this.setTargetTitle("Expectations");

		this.setListHeaders(new String[] { "Title", "Reference", "Type",
				"Owner" });

		this
				.setMoreListColumns(new String[] { "title_nm", "reference_nm",
						"code_desc as obj_type",
						"concat(o.last_name, ',', o.first_name) as theOwner " });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes on texpectation.measure_cd = tcodes.code_value and tcodes.code_type_id = 13 ",
						" left join tuser o on texpectation.owner_uid = o.user_id " });

		this
				.setMoreSelectColumns(new String[] { " tcodes.code_desc as measure_desc " });
		this
				.setMoreSelectJoins(new String[] { " join tcodes on texpectation.measure_cd = tcodes.code_value and tcodes.code_type_id = 13 " });

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		/*
		 * Ids
		 */

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("owner_uid"), sm.getUserHT(), true));

		ht.put("verified_by_id", new WebFieldSelect("verified_by_id",
				addMode ? new Integer("0") : (Integer) db
						.getObject("verified_by_id"), sm.getUserHT(), true));

		/*
		 * Codes
		 */
		ht.put("measure_cd", new WebFieldSelect("measure_cd", addMode ? "" : db
				.getText("measure_cd"), sm.getCodes("OBJECTIVE")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("STATUS")));

		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * Flags
		 */
		ht.put("confirmed_flag", new WebFieldCheckbox("confirmed_flag",
				addMode ? "N" : db.getText("confirmed_flag"), ""));

		/*
		 * Dates
		 */
		ht.put("realize_dt", new WebFieldDate("realize_dt", addMode ? "" : db
				.getText("realize_dt")));

		ht.put("verified_dt", new WebFieldDate("verified_dt", addMode ? "" : db
				.getText("verified_dt")));

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("success_blob", new WebFieldText("success_blob", addMode ? ""
				: db.getText("success_blob"), 3, 80));

		ht.put("result_blob", new WebFieldText("result_blob", addMode ? "" : db
				.getText("result_blob"), 3, 80));

		/*
		 * Return
		 */
		return ht;

	}
}
