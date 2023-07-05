/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Note Plugin
 * 
 * Change log:
 * 
 * 2/15 added mySql 3/13 as 'target' to list query
 * 
 */
public class ExceptionPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public ExceptionPlugin() throws services.ServicesException {
		super();
		this.setTableName("texception");
		this.setKeyName("exception_id");
		this.setShowAuditSubmitApprove(true);

		this.setContextSwitchOk (false); // can't change down here

		this.setTargetTitle("Process Exception");

		this.setIsDetailForm(true);
		this.setParentTarget ("Audit");

		this.setListHeaders( new String[] { "Audit Date/Time", "Exception Type",
				"Rule", "Actual", "Status" });

		this.setMoreListColumns(new  String[] { "texception.added_date",
				"except.code_desc as ExceptionName", "rule_max_no",
				"actual_no", "stat.code_desc" });

		this.setListOrder ("texception.type_cd");

		this.setMoreListJoins(new  String[] {
				" left join tcodes stat on texception.status_cd = stat.code_value and stat.code_type_id  = 51 ",
				" left join tcodes except on texception.type_cd = except.code_value and except.code_type_id  = 76 " });

		this.setMoreSelectJoins (this.moreListJoins);
		this.setMoreSelectColumns (new String[] { "except.code_desc as ExceptionName" });

		this.setAddOk(false);
		
	}

	
	
	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		if ((!sm.Parm("FilterAudit").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterAudit").length() > 0)) {
			sb.append(" AND texception.audit_id = " + sm.Parm("FilterAudit"));
		}

		return sb.toString();
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = sm.getUserHT();

		/*
		 * Dates
		 */

		WebFieldDisplay wfAuditDate = new WebFieldDisplay("audit_date", db
				.getText("added_date"));

		/*
		 * Codes
		 */
		WebFieldSelect wfStatus = new WebFieldSelect("status_cd", addMode ? "P"
				: db.getText("status_cd"), sm.getCodes("SIGNOFFSTATUS"));

		WebFieldDisplay wfType = new WebFieldDisplay("type_cd", db
				.getText("ExceptionName"));

		/*
		 * Strings
		 */
		WebFieldDisplay wfTitle = new WebFieldDisplay("title_nm", (addMode ? ""
				: db.getText("title_nm")));

		/*
		 * Display
		 */

		WebFieldDisplay wfRuleNo = new WebFieldDisplay("rule_max_no",
				(addMode ? "" : db.getText("rule_max_no")));

		WebFieldDisplay wfActualNo = new WebFieldDisplay("actual_no",
				(addMode ? "" : db.getText("actual_no")));

		/*
		 * Blobs
		 */
		WebFieldText wfReason = new WebFieldText("reason_blob", addMode ? ""
				: db.getText("reason_blob"), 3, 80);

		WebFieldText wfDesc = new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		/*
		 * Return
		 */
		WebField[] wfs = { wfReason, wfStatus, wfType, wfTitle, wfRuleNo,
				wfAuditDate, wfActualNo, wfDesc, wfNotes };

		return webFieldsToHT(wfs);

	}

}
