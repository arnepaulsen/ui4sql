/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Constraint Data Manager
 * 
 * Change Log:
 * 
 * 3/13 as 'target' to list query
 * 
 * 
 */

public class ConstraintPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ConstraintPlugin() throws services.ServicesException {
		super();
		this.setTableName("tconstraint");
		this.setKeyName("constraint_id");
		this.setTargetTitle("Constraints");

		this.setListHeaders(new String[] { "Reference", "Title", "Type",
				"Servity", "Owner" });

		this
				.setMoreListColumns(new String[] { "reference_nm", "title_nm",
						"typ.code_desc as type_desc",
						"serv.code_desc as serv_desc",
						"concat(own.last_name, ',', own.first_name) as theOwner" });

		this
				.setMoreListJoins(new String[] {
						" left join tcodes typ on tconstraint.type_cd = typ.code_value and typ.code_type_id  = 9  ",
						" left join tcodes serv on tconstraint.priority_cd = serv.code_value and serv.code_type_id  = 55 ",
						" left join tuser own on tconstraint.owner_uid = own.user_id " });

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable();

		/*
		 * Ids
		 */

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));

		/*
		 * codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("OPENCLOSE")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("CONSTRAINT")));

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("HIGHMEDLOW")));

		/*
		 * strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 32, 32));

		/*
		 * blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 60));

		ht.put("impact_blob", new WebFieldText("impact_blob", addMode ? "" : db
				.getText("impact_blob"), 3, 60));

		ht.put("mitigation_blob", new WebFieldText("mitigation_blob",
				addMode ? "" : db.getText("mitigation_blob"), 3, 60));

		return ht;

	}

}
