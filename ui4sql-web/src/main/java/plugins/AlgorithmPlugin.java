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
 * 3/13 as 'target' to list query 9/13/06 just convert to ht.puts
 * 
 * 
 */

public class AlgorithmPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public AlgorithmPlugin() throws services.ServicesException {
		super();
		this.setTableName("talgorithm");
		this.setKeyName("algorithm_id");
		this.setTargetTitle("Algorithm");

		this.setListHeaders(new String[] { "Reference", "Title", "Version",
				"Last Project Impact" });

		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"version_tx", "project_name" });

		this
				.setMoreListJoins(new String[] { " left join tproject on talgorithm.project_id = tproject.project_id " });

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	@SuppressWarnings("unchecked")
	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Id's
		 */

		ht.put("project_id", new WebFieldSelect("project_id", addMode ? sm
				.getProjectId() : (Integer) db.getObject("project_id"), sm
				.getProjectFilter(), true));

		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 32, 32));

		ht.put("version_tx", new WebFieldString("version_tx", (addMode ? ""
				: db.getText("version_tx")), 4, 4));

		/*
		 * Blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("change_blob", new WebFieldText("change_blob", addMode ? "" : db
				.getText("change_blob"), 3, 80));

		ht.put("inputs_blob", new WebFieldText("inputs_blob", addMode ? "" : db
				.getText("inputs_blob"), 3, 80));

		ht.put("outputs_blob", new WebFieldText("outputs_blob", addMode ? ""
				: db.getText("outputs_blob"), 3, 80));

		ht.put("computations_blob", new WebFieldText("computations_blob",
				addMode ? "" : db.getText("computations_blob"), 3, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		/*
		 * Return
		 */

		return ht;

	}

}
