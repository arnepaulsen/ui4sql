/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.util.Hashtable;

import db.DbFieldString;

import services.ExcelWriter;
import services.ServicesException;
import forms.*;

import remedy.RemedyChangeSubmit;

/**
 * GUI Data Manager
 * 
 * Change Log:
 * 
 * 5/19/05 Take out getDbFields!! 1/13/07 key in not auto-increment (as in from
 * external system
 * 
 * 6/14/10 change to standard sql view names
 * 
 */
public class PatientPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public PatientPlugin() throws services.ServicesException {
		super();
		this.setTableName("tpatient");
		this.setKeyName("patient_id");
		this.setTargetTitle("Patient Validator");
		this.setExcelOk(true);

		this.setListOrder("admitted_date");
		this.setListViewName("vpatient_list");
		this.setSelectViewName("vpatient");
		this.setShowAuditSubmitApprove(false);
		this.setSubmitOk(false);
		this.setReviewOk(false);
		this.setNextOk(false);

		this.setListHeaders(new String[] { "1", "Admitted Date", "Status",
				"Location", "MRN", "Name", "HAR", "Requestor" });

		this.setListSelectorColumnFlags(new boolean[] { false, false, true,
				false, false, false, false, true });

	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 2) {
			return getListSelector("FilterStatus", "O", "Status?", sm
					.getCodes("OPENCLOSE"));
		}

		// Just get contracts that actually have an adHoc assigned
		if (columnNumber == 7) {
			// TODO.. Resource HOG!!! It queries every time. Let the db manager
			// cache the data.
			String qry = new String(
					"select distinct concat(u.last_name, ', ', u.first_name) as a , "
							+ "u.user_id b, "
							+ "concat(u.last_name, ', ',	u.first_name) as theUser "
							+ " from tpatient join tuser u on tpatient.requestor_uid = u.user_id");

			Hashtable users = new Hashtable();

			try {
				users = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}
			return getListSelector("FilterRequestor", new Integer("0"),
					"Requestor? ", users);
		}

		// will never get here
		Hashtable ht = new Hashtable();
		return getListSelector("dummy", new Integer(""), "badd..", ht);

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND status_cd = 'O'");
		} else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND status_cd = '" + sm.Parm("FilterStatus") + "'");
			}
		}

		// filter on owner
		if (sm.Parm("FilterRequestor").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterRequestor").equalsIgnoreCase("0")) {
				sb.append(" AND requestor_uid = " + sm.Parm("FilterRequestor"));
			}
		}

		return sb.toString();
	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
		// boolean showMode = parmMode.equalsIgnoreCase("show") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		/*
		 * codes
		 */

		ht.put("requestor_uid", new WebFieldSelect("requestor_uid",
				addMode ? sm.getUserId() : db.getInteger("requestor_uid"), sm
						.getUserHT(), true));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "O" : db
				.getText("status_cd"), sm.getCodes("OPENCLOSE")));

		/*
		 * Strings
		 */

		ht.put("mrn_tx", new WebFieldString("mrn_tx", (addMode ? "" : db
				.getText("mrn_tx")), 12, 12));

		ht.put("location_tx", new WebFieldString("location_tx", (addMode ? ""
				: db.getText("location_tx")), 50, 50));

		ht.put("patient_nm", new WebFieldString("patient_nm", (addMode ? ""
				: db.getText("patient_nm")), 32, 64));

		ht.put("har_tx", new WebFieldString("har_tx", (addMode ? "" : db
				.getText("har_tx")), 32, 32));

		ht.put("need_tx", new WebFieldString("need_tx", (addMode ? "" : db
				.getText("need_tx")), 64, 255));

		ht.put("admit_person_tx", new WebFieldString("admit_person_tx",
				(addMode ? "" : db.getText("admit_person_tx")), 32, 64));

		ht.put("backout_verified_tx", new WebFieldString("backout_verified_tx",
				(addMode ? "" : db.getText("backout_verified_tx")), 32, 32));

		ht.put("admitted_date", new WebFieldString("admitted_date",
				(addMode ? "" : db.getText("admitted_date")), 12, 12));

		if (addMode) {
			ht.put("sr_no", new WebFieldDisplay("sr_no", ""));
		} else {
			ht.put("sr_no", new WebFieldString("sr_no", (addMode ? "" : db
					.getText("sr_no")), 8, 8));

		}

		ht.put("admit_tm_tx", new WebFieldString("admit_tm_tx", (addMode ? ""
				: db.getText("admit_tm_tx")), 32, 32));

		ht.put("backout_date", new WebFieldString("backout_date", (addMode ? ""
				: db.getText("backout_date")), 12, 12));

		return ht;

	}

	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "Patient.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName().replace(" ", "")
				+ "_Patient_Validator";
		int columns = 13;
		short startRow = 1;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);
	}

	public boolean beforeAdd(Hashtable ht) {

		debug("Patient Validator - adding to Remedy!!!!");

		// construct the interface, with the Web access user-id and pw
		RemedyChangeSubmit req = new RemedyChangeSubmit(sm.getRemedyUserid(),
				sm.getRemedyPassword(), sm.getRemedyURL());

		// add it.

		String remedyCategory = "Organizational Services"; // "Organazational
		// Services";
		String remedyType = "Revenue Capture Prod Support"; // "KPHC PROD
		// SUPPORT";
		String remedyItem = "Validation Patient"; // "Validation Pt";

		req.setRemedyCategory(remedyCategory);
		req.setRemedyType(remedyType);
		req.setRemedyItem(remedyItem);

		// bypass for testing.

		// String case_id = req.submitChange("SR", sm.getHandle(),
		// sm.getFirstName() + " "
		// + sm.getLastName());

		String case_id = "CHG00000000";

		debug("Remedy Case id " + case_id);

		//String sr_no = case_id.substring(5);
		//debug(" Case # " + sr_no);

		try {
			//ht.remove("sr_no");
		} catch (Exception e) {

		}

		//ht.put("sr_no", new DbFieldString("sr_no", sr_no));

		return true;

	}

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vpatient_excel WHERE 1=1  ");

		if (sm.Parm("FilterStatus").length() != 0
				&& !sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
			sb.append(" AND status_cd = '" + sm.Parm("FilterStatus") + "'");
		}

		// filter on owner
		if (sm.Parm("FilterRequestor").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterRequestor").equalsIgnoreCase("0")) {
				sb.append(" AND requestor_uid = " + sm.Parm("FilterRequestor"));
			}
		}

		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

}
