/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;

import db.DbFieldInteger;
import forms.*;

/**
 * Constraint Data Manager
 * 
 * Change Log:
 * 
 * 9/10/05 New
 * 
 * ToDo: -
 * 
 * 
 */

public class SegmentPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public SegmentPlugin() throws services.ServicesException {
		super();
		this.setTableName("tsegment");
		this.setKeyName("segment_id");
		this.setTargetTitle("Segment");

		this.setIsDetailForm(true);
		this.setParentTarget("Message");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Element");
		this.setDetailTargetLabel("Elements");

		this.setListHeaders(new String[] { "Message", "Segment", "Reference",
				"Version", "Status", "Project" });

		this.setMoreListColumns(new String[] {
				"tmessage.title_nm as MessageTitle",
				"tsegment.title_nm as SegmentTitle", "tsegment.reference_nm",
				"tsegment.version_id", "s.code_desc as status_desc",
				"project_name" });

	}

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

		this
				.setMoreListJoins(new String[] {

						" join tmessage on tsegment.message_id = tmessage.message_id "
								+ ((sm.Parm("FilterMessage").length() == 0)
										|| (sm.Parm("FilterMessage")
												.equalsIgnoreCase("0")) ? ""
										: " and tmessage.message_id ="
												+ sm.Parm("FilterMessage")),
						" left join tcodes s on tsegment.status_cd = s.code_value and s.code_type_id  = 60 ",
						" left join tproject on tsegment.project_id = tproject.project_id " });

		this.setAddOk(myAddOk());

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	// turn off add if no message selected on list page
	public boolean myAddOk() {

		if (this.formWriterType.equalsIgnoreCase("list")) {

			// only allow add if there is a module selected
			return sm.Parm("FilterMessage").length() > 0
					&& !sm.Parm("FilterMessage").equalsIgnoreCase("0");
		} else
			return true;
	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("message_id", new DbFieldInteger("message_id", new Integer(sm
				.Parm("FilterMessage"))));

		return true;
	}

	public boolean listColumnHasSelector(int columnNumber) {
		// column 0 = message id, 4 = status
		if (columnNumber == 0 || columnNumber == 4)
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {
		case 0: {

			String sQuery = "Select title_nm, message_id, title_nm from tmessage where application_id = "
					+ sm.getApplicationId().toString();

			return new WebFieldSelect("FilterMessage", sm.Parm("FilterMessage")
					.length() == 0 ? new Integer("0") : new Integer(sm
					.Parm("FilterMessage")), db, sQuery, "- All Messages -");
		}

		default: {

		}
			// default the status to open when starting new list
			WebFieldSelect wf = new WebFieldSelect("FilterStatus", (sm.Parm(
					"FilterStatus").length() == 0 ? "0" : sm
					.Parm("FilterStatus")), sm.getCodes("LIVESTAT"),
					"All Status");
			wf.setDisplayClass("listform");
			return wf;
		}

	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		if ((sm.Parm("FilterStatus").length() > 0)
				&& (!sm.Parm("FilterStatus").equalsIgnoreCase("0"))) {
			sb.append(" AND s.code_value = '" + sm.Parm("FilterStatus") + "'");
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		sm.setStructureType("Segment"); // leave a cookie so the Step manager
		// knows what kind of a step to take

		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("segment_id"), db.getText("title_nm"));
		}

		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("project_id"), sm.getProjectFilter(), true);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfVersionNo = new WebFieldString("version_id",
				(addMode ? "0" : db.getText("version_id")), 4, 4);

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? ""
				: db.getText("status_cd"), sm.getCodes("LIVESTAT"));

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("MSGTYPE"), true);

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);

		WebFieldText wfVersion = new WebFieldText("version_blob", addMode ? ""
				: db.getText("version_blob"), 3, 80);

		WebFieldText wfSecurity = new WebFieldText("security_blob",
				addMode ? "" : db.getText("security_blob"), 3, 80);

		WebFieldText wfContents = new WebFieldText("contents_blob",
				addMode ? "" : db.getText("contents_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebField[] wfs = { wfProject, wfVersion, wfType, wfDesc, wfVersionNo,
				wfStatus, wfTitle, wfRefr, wfSecurity, wfContents, wfNotes };

		return webFieldsToHT(wfs);

	}

}
