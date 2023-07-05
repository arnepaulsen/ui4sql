/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Measurements
 * 
 */

public class MeasurementPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public MeasurementPlugin() throws services.ServicesException {
		super();
		this.setTableName("tmeasurement");
		this.setKeyName("measurement_id");
		this.setListHeaders( new String[] { "Reference", "Title", "Status",
				"Type", "Target", "Actual", "Owner" });
		this.setTargetTitle("Measurement");

		this.setMoreListColumns(new  String[] { "reference_nm", "title_nm",
				"s.code_desc as StatusDesc", "t.code_desc as TypeDesc",
				"target_flt", "actual_flt",
				"concat(owner.last_name, ',',owner.first_name) as OwnerName" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes s on tmeasurement.status_cd = s.code_value and s.code_type_id = 5 ",
				" left join tcodes t on tmeasurement.type_cd = t.code_value and t.code_type_id = 13 ",
				" left join tuser owner on tmeasurement.owner_uid = owner.user_id " });

		this.setMoreSelectJoins (new String[] {
				" left join tcodes res on tmeasurement.result_cd = res.code_value and res.code_type_id = 94 ",
				" left join tuser aprv on tmeasurement.reviewed_uid = aprv.user_id " });

		this.setMoreSelectColumns (new String[] { "res.code_desc as result_desc",
				"concat(aprv.last_name, ',',aprv.first_name) as approver_name" });
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
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT()));

		/*
		 * Dates
		 */

		ht.put("target_date", new WebFieldDate("target_date", (addMode ? ""
				: db.getText("target_date"))));
		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 32, 32));

		ht.put("note_tx", new WebFieldString("note_tx", (addMode ? "" : db
				.getText("note_tx")), 64, 100));

		/*
		 * Codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("STATUS")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("OBJECTIVE")));

		/*
		 * Floats
		 */

		ht.put("target_flt", new WebFieldString("target_flt", (addMode ? ""
				: db.getText("target_flt")), 32, 32));

		ht.put("mf_driver", new WebFieldString("mf_driver", (addMode ? "" : db
				.getText("mf_driver")), 32, 32));

		ht.put("mf_target", new WebFieldString("mf_target", (addMode ? "" : db
				.getText("mf_target")), 32, 32));

		ht.put("mf_ruler", new WebFieldString("mf_ruler", (addMode ? "" : db
				.getText("mf_ruler")), 32, 32));

		/*
		 * Blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 60));

		/*
		 * Approver fields, only display if not approver or reviewer
		 */

		if (sm.getProjectRole().equalsIgnoreCase("rev")) {

			ht.put("result_blob", new WebFieldText("result_blob", addMode ? ""
					: db.getText("result_blob"), 3, 60));

			ht.put("actual_flt", new WebFieldString("actual_flt", (addMode ? ""
					: db.getText("actual_flt")), 32, 32));

			ht.put("reviewed_uid", new WebFieldSelect("reviewed_uid",
					addMode ? new Integer("0") : db.getInteger("reviewed_uid"),
					sm.getUserHT()));

			ht.put("result_cd", new WebFieldSelect("result_cd", addMode ? ""
					: db.getText("result_cd"), sm.getCodes("STATUSPF")));
		} else {

			ht.put("result_blob", new WebFieldDisplay("result_blob",
					addMode ? "" : db.getText("result_blob")));

			ht.put("actual_flt", new WebFieldDisplay("actual_flt",
					(addMode ? "" : db.getText("actual_flt"))));

			ht.put("reviewed_uid", new WebFieldDisplay("reviewed_uid", db
					.getText("approver_name")));

			ht.put("result_cd", new WebFieldDisplay("result_cd", db
					.getText("result_desc")));

		}

		return ht;

	}

}
