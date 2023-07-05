/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import db.DbFieldInteger;

import router.SessionMgr;
import forms.*;

/*******************************************************************************
 * Attachment
 * 
 * Associates a user with various roles.
 * 
 * This Data Manager is unique in tath tattachment is a sub-table of
 * tdeliverable. There is a one-many between deliverable and tattachment.
 * 
 * "tattachment" does not have a project_id field. It is a child of
 * tdeliverable, and that is where the project_id context is.
 * 
 * The list and select queries insert the tdeliverable table between after
 * tattachment and before tproject
 * 
 * Errors: 1. The user has not selected a project filter 2. There are no
 * deliverables for the selected project
 * 
 * if either of these are true: - set the error message field on the form - set
 * 'ok_to_save' equal false, then pass that back in 'saveOK() which is called
 * from the add form driver.
 * 
 * Change Log:
 * 
 * 2/15 added mySql
 * 
 * 
 ******************************************************************************/
public class AttachmentPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private boolean ok_to_save = true;

	public AttachmentPlugin() throws services.ServicesException {
		super();
		this.setTableName("tattachment");
		this.setKeyName("attachment_id");
		this.setTargetTitle("Attachments");

		this.setIsStepChild(true);
		this.setIsDetailForm(true);
		this.setParentTarget("SBAR");
		this.setUpdatesOk(true);
		this.setNextOk(false);
		this.setShowAuditSubmitApprove(false);

		this.setListHeaders(new String[] { "Id", "Title", "File" });

		// this
		// .setMoreListColumns(new String[] { "tattachment.type_cd as
		// attach_type", "tattachment.title_nm" });
		// this
		this.setMoreListColumns(new String[] { "tattachment.reference_nm",
				"tattachment.title_nm", "tattachment.file_nm" });

	}

	public void init(SessionMgr parmSm) {

		super.init(parmSm);

		this.setMoreListJoins(new String[] {
				" left join tuser as t on tattachment.party_uid = t.user_id ",
				" join " + this.getParentTable()
						+ " on tattachment.parent_id =  " + sm.getParentTable()
						+ "." + sm.getParentKeyName()
						+ " left join tdivision on " + this.getParentTable()
						+ "." + "division_id = " + "tdivision.division_id" });

		this.setMoreSelectColumns(new String[] { "tsbar.division_id" });

		this.setMoreSelectJoins(this.moreListJoins);

		if (sm.getIssueTriageLevel().equalsIgnoreCase("U")) {

			this.setEditOk(true);
			this.setCopyOk(true);
			this.setExcelOk(true);
			this.setDeleteOk(false);

		} else {
			if (sm.getIssueTriageLevel().equalsIgnoreCase("A")) {
				this.setEditOk(true);
				this.setCopyOk(true);
				this.setExcelOk(true);
				this.setDeleteOk(true);

			} else {
				this.setDeleteOk(false);
				this.setEditOk(false);
				this.setCopyOk(false);
				this.setExcelOk(false);
				this.setAddOk(false);
			}
		}

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public boolean saveOk() {
		return ok_to_save;
	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("parent_id", new DbFieldInteger("parent_id", this.sm
				.getParentId()));

		return true;
	}

	public String getListAnd() {
		StringBuffer sb = new StringBuffer();

		// TO DO : GENERALIZE FOR ANY ATTACHMENT CHILD, NOT JUST SBAR

		sb.append(" AND tsbar.sbar_id = " + sm.getParentId().toString());

		// debug (" ATTACHMENT QUERY .. " + sb.toString());

		return sb.toString();
	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		int port = sm.getServerPort();
		String host = sm.getHost();
		String tomcat = sm.getTomcatName();

		// this value is used by the StarTeamUpload servlet to save the
		// filename into the tattachment record

		if (!addMode) {
			sm.setAttachmentId(this.rowId);
		}

		/*
		 * 
		 * if in add mode, look up the Project Name using the latest filter id
		 * els... get the project name from the tattachment rs
		 * 
		 */

		String errorMessage = new String("");

		WebFieldDisplay wfMessage;

		/*
		 * 
		 * Get list of deliverables for this project
		 * 
		 */

		String deliverQuery = ("Select title_nm as odor, deliverable_id,title_nm from tdeliverable where project_id = " + sm.getProjectId().toString());

		Hashtable ht = db.getLookupTable(deliverQuery);
		// skip this for now.. only using attachments for SBAR, not delive
		if (ht.size() == 0 ) {
			ok_to_save = false;
			errorMessage = "There are no deliverables for this project.";
			wfMessage = new WebFieldDisplay("message", errorMessage);
			WebField[] wfs = { wfMessage };
			return webFieldsToHT(wfs);
		}

		wfMessage = new WebFieldDisplay("message", "");

		WebFieldDisplay wfHost = new WebFieldDisplay("host", host + ":" + port);
		;

		WebFieldDisplay wfTomcat = new WebFieldDisplay("tomcat_name", sm
				.getTomcatName());

		// WebFieldSelect wfDeliverable = new WebFieldSelect("deliverable_id",
		// addMode ? new Integer("1") : (Integer) db
		// .getObject("deliverable_id"), ht, true);

		WebFieldDisplay wfRowId = new WebFieldDisplay("rowid", addMode ? "0"
				: this.rowId.toString());

		WebFieldHidden wfMode = new WebFieldHidden("mode", parmMode);
		// ... other fields

		// hide the url upload link if file has already been up loaded
		WebFieldDisplay wfLinkText = new WebFieldDisplay("linktext",
				(addMode ? "" : (db.getText("file_nm").length() > 1) ? ""
						: "Upload attachment to Star Team"));

		WebFieldDisplay wfLinkText2 = new WebFieldDisplay("linktext2",
				(addMode ? "" : (db.getText("file_nm").length() == 0) ? ""
						: "Download"));

		WebFieldString wfReference = new WebFieldString("reference_nm",
				addMode ? "" : db.getText("reference_nm"), 12, 12);

		WebFieldString wfTitle = new WebFieldString("title_nm", addMode ? ""
				: db.getText("title_nm"), 64, 64);

		WebFieldDisplay wfFile = new WebFieldDisplay("file_nm", addMode ? ""
				: db.getText("file_nm"));

		WebFieldString wfUrl = new WebFieldString("url_tx", addMode ? "" : db
				.getText("url_tx"), 128, 128);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 80);

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 4, 80);

		WebField[] wfs = { wfMessage, wfUrl, wfNotes, wfReference, wfTitle,
				wfFile, wfDesc, wfTomcat, wfHost, wfMode, wfRowId, wfLinkText,
				wfLinkText2 };

		return webFieldsToHT(wfs);

	}
}
