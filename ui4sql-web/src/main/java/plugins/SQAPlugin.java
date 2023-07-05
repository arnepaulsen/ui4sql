/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import router.SessionMgr;
import java.util.Hashtable;
import forms.*;

/*******************************************************************************
 * xx-TableManager
 * 
 * 
 * Change Log:
 * 
 * 
 ******************************************************************************/

public class SQAPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public SQAPlugin() throws services.ServicesException {
		super();
	}

	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		setDefaults();
	}

	private void setDefaults() {
		this.setTableName("tdeliverable");
		this.setKeyName("deliverable_id");
		this.setTargetTitle("SQA Artifact Review ");
		
		this.setUpdatesOk(false);
		

		this.setListHeaders( new String[] { "Reference", "DMAIC",
				"Deliverable Name", "Status" });

		this.setMoreListColumns(new  String[] { " reference_nm",
				" dmaic_codes.code_desc as dmaic_desc ",
				" delv_codes.code_desc as deliv_type ",
				" status_codes.code_desc as status_desc " });

		this.setMoreSelectJoins (new String[] {
				" left join tcodes as status_codes on tdeliverable.deliverable_status_cd = status_codes.code_value and status_codes.code_type_id   = 5 ",
				" left join tcodes as delv_codes on tdeliverable.deliverable_cd =  delv_codes.code_value and delv_codes.code_type_id  = 10  ",
				" left join tcodes as dmaic_codes on delv_codes.code_desc2 = dmaic_codes.code_value and dmaic_codes.code_type_id  = 8 " });

		this.setMoreListJoins (this.moreSelectJoins);

		this.setMoreSelectColumns (new String[] {
				" dmaic_codes.code_desc as dmaic_desc ",
				" delv_codes.code_desc as deliverable_desc ",
				" status_codes.code_desc as status_desc " });

	}

	
	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		// these are set for WebFielddisplay in 'view' mode, or a html selector
		// in add/update mode

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
		
		Hashtable ht = new Hashtable();
		
		/*
		 * Strings
		 */
		ht.put("project_name",  new WebFieldDisplay("project_name", db
				.getText("project_name")));

		ht.put("reference_nm",  new WebFieldDisplay("reference_nm", db
				.getText("reference_nm")));

		ht.put("deliverable_desc",  new WebFieldDisplay("deliverable_desc",
				db.getText("deliverable_desc")));

		ht.put("dmaic_desc",  new WebFieldDisplay("dmaic_desc", db
				.getText("dmaic_desc")));
	
		ht.put("sqa_sev_1_error_no",  new WebFieldString("sqa_sev_1_error_no",
				addMode ? "0" : db.getText("sqa_sev_1_error_no"), 3, 3));

		ht.put("sqa_sev_2_error_no",  new WebFieldString("sqa_sev_2_error_no",
				addMode ? "0" : db.getText("sqa_sev_2_error_no"), 3, 3));

		ht.put("sqa_sev_3_error_no",  new WebFieldString("sqa_sev_3_error_no",
				addMode ? "0" : db.getText("sqa_sev_3_error_no"), 3, 3));
		
		ht.put("sqa_review_hours_no",  new WebFieldString("sqa_review_hours_no",
				addMode ? "0" : db.getText("sqa_review_hours_no"), 3, 3));

		ht.put("sqa_review_team",  new WebFieldString("sqa_review_team",
				addMode ? "0" : db.getText("sqa_review_team"), 32, 32));

	


		/*
		 * Blobs
		 */
		
		ht.put("sqa_review_desc",  new WebFieldText("sqa_review_desc",
				addMode ? "" : db.getText("sqa_review_desc"), 6, 80));

		ht.put("sqa_major_concerns", new WebFieldText("sqa_major_concerns",
				addMode ? "" : db.getText("sqa_major_concerns"), 6, 80));

		ht.put("sqa_feedback_status",  new WebFieldText("sqa_feedback_status",
				addMode ? "" : db.getText("sqa_feedback_status"), 6, 80));
		
		/*
		 * Codes
		 */
		ht.put("deliverable_status_cd",  new WebFieldSelect(
				"deliverable_status_cd", addMode ? "New" : db
						.getText("deliverable_status_cd"), sm
						.getCodes("STATUS")));

		/*
		 * Flags
		 */
		ht.put("sqa_pass_flag",  new WebFieldCheckbox("sqa_pass_flag", db
				.getText("sqa_pass_flag"), "Passed"));

		/*
		 * Dates 
		 */
	
		ht.put("sqa_sent_sqar_date", new WebFieldDate("sqa_sent_sqar_date", addMode ? ""
				: db.getText("sqa_sent_sqar_date")));

		ht.put("sqa_close_date",  new WebFieldDate("sqa_close_date",
				addMode ? "" : db.getText("sqa_close_date")));

		ht.put("sqa_author_return_date",  new WebFieldDate("sqa_author_return_date",
				addMode ? "" : db.getText("sqa_author_return_date")));

		ht.put("sqa_action_date",  new WebFieldDate("sqa_action_date",
				addMode ? "" : db.getText("sqa_action_date")));

		/*
		 * Return
		 */
		return ht;

	}

}
