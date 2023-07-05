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
 * Job Plugin
 * 
 */
public class JobPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public JobPlugin() throws services.ServicesException {
		super();
		this.setTableName("tjob");
		this.setKeyName("job_id");
		this.setTargetTitle("Batch Job");

		this.setListHeaders(new String[] { "Title", "Reference", "Frequency",
				"Stream" });
		this.setMoreListColumns(new String[] { "title_nm", "reference_nm",
				"freq.code_desc as freq_desc", "stream_nm" });

		this
				.setMoreListJoins(new String[] { " left join tcodes as freq on tjob.frequency_cd = freq.code_value and freq.code_type_id  = 18 " });
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		// todo: cache the job ht

		Hashtable jobs = new Hashtable();

		try {
			jobs = db
					.getLookupTable("Select title_nm, job_id, title_nm from tjob");
		} catch (ServicesException se) {

		}

		WebFieldSelect wfJobs = new WebFieldSelect("dependent_job_id",
				addMode ? new Integer("0") : db.getInteger("dependent_job_id"),
				jobs);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldDate wfInstallDate = new WebFieldDate("install_date",
				addMode ? "" : db.getText("install_date"));

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("JOBSTATUS"));

		WebFieldSelect wfFrequency = new WebFieldSelect("frequency_cd",
				addMode ? "" : db.getText("frequency_cd"), sm
						.getCodes("FREQUENCY"));

		WebFieldSelect wfType = new WebFieldSelect("job_type_cd", addMode ? ""
				: db.getText("job_type_cd"), sm.getCodes("JOBTYPE"));

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldString wfStream = new WebFieldString("stream_nm", (addMode ? ""
				: db.getText("stream_nm")), 64, 64);

		WebFieldString wfSkip = new WebFieldString("skip_condition_tx",
				(addMode ? "" : db.getText("skip_condition_tx")), 64, 100);

		WebFieldString wfRc = new WebFieldString("max_allowed_rc_tx",
				(addMode ? "" : db.getText("max_allowed_rc_tx")), 12, 12);

		WebFieldText wfDesc = new WebFieldText("blob_job_desc", addMode ? ""
				: db.getText("blob_job_desc"), 5, 100);

		WebFieldText wfInstructions = new WebFieldText(
				"blob_spec_instructions", (addMode ? "" : db
						.getText("blob_spec_instructions")), 5, 100);

		WebFieldText wfRecovery = new WebFieldText("blob_recovery_notes",
				(addMode ? "" : db.getText("blob_recovery_notes")), 5, 100);

		WebFieldText wfNotification = new WebFieldText("blob_notification",
				(addMode ? "" : db.getText("blob_notification")), 5, 100);

		WebField[] wfs = { wfInstallDate, wfRefr, wfDesc, wfStream, wfSkip,
				wfJobs, wfRc, wfInstructions, wfFrequency, wfTitle, wfStatus,
				wfType, wfRecovery, wfNotification };

		return webFieldsToHT(wfs);

	}
}
