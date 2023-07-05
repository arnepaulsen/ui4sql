/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import db.DbField;
import db.DbFieldDate;
import services.RemedyProblemReport;
import services.ServicesException;
import remedy.RemedyHelpDeskQuery;
import remedy.RemedyHelpDeskModify;
import router.SessionMgr;
import forms.*;
import db.DbFieldString;

import services.ExcelWriter;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Incident :
 * This is really the "patient safety" tracker, whose identify it stole
 * because that's what it originally resembeled.  Most patient safety issues derive 
 * from a Remedy PR, or incident. 
 * 
 * It should probably be renamed 'patient safety' tracker!  
 *   
 * 
 * Change Log:
 * 
 * 9/20/06 'title nm' was ht.put twice. 1/13/07 key in not auto-increment (as in
 * from external system)
 * 
 * KEYWORDS : SQLSERVER ... watch for 'cast' on mySQL
 * 
 * Change Log:
 * 
 * 5/30/08 Put incident # in col. 2, and make it a link 9/3/08 - add color_cd,
 * and return to list writer
 * 
 * 7/2/10 Standarize view names
 * 
 * 7/31/10 convert to division plugin
 * 
 * 
 * 
 */
public class IncidentPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	String remedyKey = "incident_no"; // must be "rfc_no", or "sr_no"

	String remedy_result = "";

	public IncidentPlugin() throws services.ServicesException {
		super();
	}

	public void init(SessionMgr parmSm) {
		super.init(parmSm);

		this.setTableName("tincident");
		this.setKeyName("incident_id");
		this.setTargetTitle("Patient Safety Issues");

		this.setGotoKeyName("incident_no");
		this.setGotoDisplayName("PR #");

		this.setListHeaders(new String[] { "1", "PR#", "Next Step", "Title",
				"Group", "ID Date", "Suite", "Status" });

		this.setListViewName("vincident_list");
		this.setSelectViewName("vincident");
		
		if (sm.getPatientSafetyLevel().equalsIgnoreCase("L")) {
			this.setTemplateName("IncidentLimited.html");
		}

		// plugin characteristics
		this.setRemedyOk(false);
		this.setCopyOk(false);
		this.setExcelOk(true);
		this.setGotoOk(true);
		this.setReviewOk(false);

		this.setShowAuditSubmitApprove(false); // can turn off the

		boolean ok = sm.getPatientSafetyLevel().equalsIgnoreCase("A");

		// role-based flags
		this.setEditOk(ok);
		this.setDeleteOk(ok);
		this.setAddOk(ok);
		this.setReviewOk(false);

	}

	// return bgcolor=yellow if install date < today
	public String listBgColor(int columnNumber, String value, DbField[] fields) {

		if (columnNumber < 900) {
			if (value != null)
				return "bgcolor=" + fields[10].getText();
			else
				return "";
		} else
			return "";
	}


	private String formatProblemNo(int incident_no) {

		String search_string = "" + incident_no;
		while (search_string.length() < 13)
			search_string = "0" + search_string;
		search_string = "HD" + search_string;

		return search_string;
	}

	public String getRemedy() {

		int rowKey = Integer.parseInt(sm.Parm("RowKey"));

		// todo: push the rfc_no out on the list page
		int incident_no = getIncident_no(sm.Parm("RowKey"));

		if (incident_no == 0)
			return "No incident.";

		// build the exact remedy match, lie 'HD00000000000012' - Note Remedy
		// PR's are "HD" + 13 digits

		String search_string = formatProblemNo(incident_no);

		// connect to remedy with id/pw
		RemedyHelpDeskQuery problem = new RemedyHelpDeskQuery(sm
				.getRemedyUserid(), sm.getRemedyPassword(), sm.getRemedyURL());

		// get rfc info in xml form
		String xml = problem.getCaseInfo(search_string); // get rfc to xml

		if (problem.getSuccess() == false) {
			remedy_result = problem.getErrorText();
			return remedy_result;
		}

		if (xml != null) {

			// debug("creating incident object from xml");

			RemedyProblemReport pr = new RemedyProblemReport(xml);

			if (pr.getChangeCount() == 1) {
				// debug("sending rfc to database");
				pr.saveToDatabase(sm.getConnection(), tableName, keyName,
						rowKey);
			} else {
				remedy_result = "Ticket not found in Remedy.";

			}
		}
		
		return "Okay.";
	}

	public void beforeUpdate(Hashtable<String, DbField> ht) {

		/*
		 * force in date stamp on the next step if not already there!
		 */
		String nextStep = sm.Parm("next_step_tx");
		SimpleDateFormat mySimpleDate = new SimpleDateFormat("MM/dd/yy");

		if (nextStep != null) {

			if (nextStep.length() > 2) {

				if (!nextStep.substring(2, 3).equalsIgnoreCase("/")) {
					Date d = new Date();
					String myFormatDate = mySimpleDate.format(d);
					nextStep = myFormatDate + ": " + nextStep;

					try {
						ht.remove("next_step_tx");
						ht.put("next_step_tx", new DbFieldString(
								"next_step_tx", nextStep));

					} catch (Exception e) {
						debug("Incident.plugin: exception remove-put "
								+ e.toString());
					}

					// debug("Exception updating med_ctr_rv_date : " +
					// e.toString());
				}
			}
		}

		if (sm.Parm("worklog").length() < 2)
			return;

		int pr_no = getIncident_no(sm.Parm("RowKey"));

		if (pr_no == 0)
			return;

		String formatted_chg_id = formatProblemNo(pr_no);

		RemedyHelpDeskModify remedy = new RemedyHelpDeskModify(sm
				.getRemedyUserid(), sm.getRemedyPassword(), sm.getRemedyURL());

		remedy.updateWorklog(formatted_chg_id, sm.Parm("worklog") + "\nBy: "
				+ sm.getFirstName() + "" + sm.getLastName());

		return;
	}

	// get the remedy # (sr or rfc) from the row #
	private int getIncident_no(String row_id) {

		int incident_no = 0;

		String sql_fetch = "select " + remedyKey + " from " + tableName
				+ " where " + keyName + " = " + row_id;

		// System.out.println(" remedy key : " + remedyKey);
		// System.out.println(" this remedy key : " + this.remedyKey);

		System.out.println("incident # fetch sql : " + sql_fetch);

		try {
			ResultSet rs = db.getRS(sql_fetch);
			if (rs.next() == true) {
				incident_no = rs.getInt(remedyKey);
				// debug("got remedy # from db : " + incident_no);
			}
		} catch (services.ServicesException se) {
			debug("services exception getting rfc : " + se.toString());

		} catch (SQLException sql_ex) {
			debug("sql exception getting rfc : " + sql_ex.toString());
		}

		// debug("incident no from table : " + incident_no);

		return incident_no;
	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#3) has a selector, other fields do not
		if (columnNumber > 3)
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 6)
			return getListSelector("FilterSuite", "", "Owner?", sm
					.getCodes("SUITEPS"));

		if (columnNumber == 7) {
			// bridges interface

			return getListSelector("FilterStatus", "O", "Status?", sm
					.getCodes("OPENCLOSE"));
		}

		if (columnNumber == 4) {
			// bridges interface

			return getListSelector("FilterGroup", "", "Group?", sm
					.getCodes("PSGROUP"));
		}

		/*
		 * Filter unique Install Dates!
		 */
		if (columnNumber == 5) {

			String dateCast = null;
			String dateCast2 = null;
			String qry = null;

			dateCast = " ( " + dbprefix
					+ "FormatDateTime(incident_date, 'mm/yy')) ";
			dateCast2 = " ( " + dbprefix
					+ "FormatDateTime(incident_date, 'yyyy/mm')) ";

			qry = " select distinct " + dateCast2 + "," + dateCast + ","
					+ dateCast
					+ " from tincident where incident_date != '' order by 1";

			Hashtable dates = new Hashtable();

			try {
				dates = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterOpen", "O", "ID Month?", dates);

		}

		// unreachable code

		return getListSelector("dummy", new Integer(""), "badd..",
				new Hashtable());

	}

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
		 */

		StringBuffer sb = new StringBuffer();

		/*
		 * add securty for pat safety depending on tuser.pat_sfty_cd
		 */

		if (!(sm.getPatientSafetyLevel().equalsIgnoreCase("A")
				|| sm.getPatientSafetyLevel().equalsIgnoreCase("U") || sm
				.getPatientSafetyLevel().equalsIgnoreCase("L"))) {

			sb.append("AND 1 = 0 ");
		}

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND ncal_stat_cd = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND ncal_stat_cd = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		// filter on interface
		if (sm.Parm("FilterSuite").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterSuite").equalsIgnoreCase("0")) {
				sb.append(" AND suite_cd = '" + sm.Parm("FilterSuite") + "'");
			}
		}

		if (sm.Parm("FilterOpen").length() == 0) {

		} else {
			if (!sm.Parm("FilterOpen").equalsIgnoreCase("0")) {
				String filterMonth = sm.Parm("FilterOpen").substring(0, 2);
				String filterYear = "20"
						+ sm.Parm("FilterOpen").substring(3, 5);

				sb.append(" AND datepart(month, incident_date) = "
						+ filterMonth + " AND datepart(yyyy, incident_date) = "
						+ filterYear);
			}

		}

		if (sm.Parm("FilterGroup").length() == 0) {

		} else {
			if (!sm.Parm("FilterGroup").equalsIgnoreCase("0")) {
				sb.append(" AND grp_cd = '" + sm.Parm("FilterGroup") + "'");
			}
		}

		return sb.toString();
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();
		/*
		 * Ids
		 */

		Hashtable interfaces = sm
				.getTable(
						"tinterface",
						"select title_nm, interface_id, title_nm from tinterface  "
								+ " union "
								+ " select ' Multiple' as 'a', 0 as 'b' , ' Multiple' as 'c' "
								+ " order by 1 ");

		ht.put("interface_id", new WebFieldSelect("interface_id",
				addMode ? new Integer("0") : db.getInteger("interface_id"),
				interfaces));

		/*
		 * Strings
		 */

		ht.put("msg", new WebFieldDisplay("msg", remedy_result));

		ht.put("incident_no", new WebFieldString("incident_no", (addMode ? ""
				: db.getText("incident_no")), 8, 8));

		ht.put("rfc_no", new WebFieldString("rfc_no", (addMode ? "" : db
				.getText("rfc_no")), 8, 8));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 100, 255));

		ht.put("part_status_tx", new WebFieldText("part_status_tx",
				(addMode ? "" : db.getText("part_status_tx")), 5, 100));

		ht.put("epic_contact_tx", new WebFieldString("epic_contact_tx",
				(addMode ? "" : db.getText("epic_contact_tx")), 64, 64));

		ht.put("test_status_tx", new WebFieldString("test_status_tx",
				(addMode ? "" : db.getText("test_status_tx")), 64, 128));

		ht.put("dlg_tx", new WebFieldString("dlg_tx", (addMode ? "" : db
				.getText("dlg_tx")), 64, 64));

		ht.put("rn_tx", new WebFieldString("rn_tx", (addMode ? "" : db
				.getText("rn_tx")), 64, 64));

		ht.put("next_step_tx", new WebFieldString("next_step_tx", (addMode ? ""
				: db.getText("next_step_tx")), 100, 255));

		ht.put("suites_tx", new WebFieldString("suites_tx", (addMode ? "" : db
				.getText("suites_tx")), 64, 64));

		ht.put("adhoc_tx", new WebFieldString("adhoc_tx", (addMode ? "" : db
				.getText("adhoc_tx")), 16, 32));

		ht.put("adhoc_comments_tx", new WebFieldString("adhoc_comments_tx",
				(addMode ? "" : db.getText("adhoc_comments_tx")), 32, 64));

		ht.put("kpkg_tx", new WebFieldString("kpkg_tx", (addMode ? "" : db
				.getText("kpkg_tx")), 20, 64));

		ht.put("kpkg_comments_tx", new WebFieldString("kpkg_comments_tx",
				(addMode ? "" : db.getText("kpkg_comments_tx")), 64, 64));

		ht.put("act_hours_tx", new WebFieldString("act_hours_tx", (addMode ? ""
				: db.getText("act_hours_tx")), 6, 6));

		/*
		 * Dates
		 */

		ht.put("cab_date", new WebFieldDate("cab_date", addMode ? "" : db
				.getText("cab_date")));

		ht.put("start_date", new WebFieldDisplay("start_date", addMode ? ""
				: db.getText("start_date")));

		ht.put("target_date", new WebFieldDate("target_date", addMode ? "" : db
				.getText("target_date")));

		ht.put("incident_date", new WebFieldDate("incident_date", addMode ? ""
				: db.getText("incident_date")));

		ht.put("closed_date", new WebFieldDate("closed_date", addMode ? "" : db
				.getText("closed_date")));

		ht.put("kpkg_date", new WebFieldDate("kpkg_date", addMode ? "" : db
				.getText("kpkg_date")));

		ht.put("time_tx", new WebFieldString("time_tx", (addMode ? "" : db
				.getText("time_tx")), 8, 8));

		/*
		 * Flags
		 */

		ht.put("prev_rel_flag", new WebFieldCheckbox("prev_rel_flag",
				addMode ? "N" : db.getText("prev_rel_flag"), ""));

		ht.put("spr07_rel_flag", new WebFieldCheckbox("spr07_rel_flag",
				addMode ? "N" : db.getText("spr07_rel_flag"), ""));

		ht.put("spr08_rel_flag", new WebFieldCheckbox("spr08_rel_flag",
				addMode ? "N" : db.getText("spr08_rel_flag"), ""));

		ht.put("regn_ex_flag", new WebFieldCheckbox("regn_ex_flag",
				addMode ? "N" : db.getText("regn_ex_flag"), ""));

		ht.put("regn_ncal_flag", new WebFieldCheckbox("regn_ncal_flag",
				addMode ? "N" : db.getText("regn_ncal_flag"), ""));

		ht.put("regn_scal_flag", new WebFieldCheckbox("regn_scal_flag",
				addMode ? "N" : db.getText("regn_scal_flag"), ""));

		ht.put("regn_hi_flag", new WebFieldCheckbox("regn_hi_flag",
				addMode ? "N" : db.getText("regn_hi_flag"), ""));

		ht.put("regn_co_flag", new WebFieldCheckbox("regn_co_flag",
				addMode ? "N" : db.getText("regn_co_flag"), ""));

		ht.put("regn_nw_flag", new WebFieldCheckbox("regn_nw_flag",
				addMode ? "N" : db.getText("regn_nw_flag"), ""));

		ht.put("regn_oh_flag", new WebFieldCheckbox("regn_oh_flag",
				addMode ? "N" : db.getText("regn_oh_flag"), ""));

		ht.put("regn_ma_flag", new WebFieldCheckbox("regn_ma_flag",
				addMode ? "N" : db.getText("regn_ma_flag"), ""));

		ht.put("ets_pass_flag", new WebFieldCheckbox("ets_pass_flag",
				addMode ? "N" : db.getText("ets_pass_flag"), ""));

		/*
		 * Codes
		 */

		ht.put("color_cd", new WebFieldSelect("color_cd", addMode ? "" : db
				.getText("color_cd"), sm.getCodesAlt("RYG"), "No Color"));

		ht.put("grp_cd", new WebFieldSelect("grp_cd", addMode ? "" : db
				.getText("grp_cd"), sm.getCodes("PSGROUP")));

		ht.put("patient_safety_flag", new WebFieldSelect("patient_safety_flag",
				addMode ? "" : db.getText("patient_safety_flag"), sm
						.getCodes("YESNO")));

		String[][] fix_cd = new String[][] { { "Code", "Config", "RA" },
				{ "Code", "Config", "RA" } };

		String[][] ncal_cd = new String[][] { { "NA", "IM", "NCI" },
				{ "N/A", "Impact Imminent", "NCI" } };

		ht.put("fix_cd", new WebFieldSelect("fix_cd", addMode ? "" : db
				.getText("fix_cd"), fix_cd));

		ht.put("instance_cd", new WebFieldSelect("instance_cd", addMode ? ""
				: db.getText("instance_cd"), sm.getCodes("INSTANCE")));

		ht.put("suite_cd", new WebFieldSelect("suite_cd", addMode ? "" : db
				.getText("suite_cd"), sm.getCodes("SUITEPS")));

		ht.put("ncal_stat_cd", new WebFieldSelect("ncal_stat_cd", addMode ? ""
				: db.getText("ncal_stat_cd"), sm.getCodes("OPENCLOSE")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("INCIDENTTYPE")));

		ht.put("rn_stat_cd", new WebFieldSelect("rn_stat_cd", addMode ? "N"
				: db.getText("rn_stat_cd"), sm.getCodes("TESTSTAT2")));

		ht.put("dlg_stat_cd", new WebFieldSelect("dlg_stat_cd", addMode ? ""
				: db.getText("dlg_stat_cd"), sm.getCodes("TESTSTAT2")));

		ht.put("adhoc_stat_cd", new WebFieldSelect("adhoc_stat_cd",
				addMode ? "" : db.getText("adhoc_stat_cd"), sm
						.getCodes("TESTSTAT2")));

		ht.put("severity_cd", new WebFieldSelect("severity_cd", addMode ? ""
				: db.getText("severity_cd"), sm.getCodes("PATSAFESV")));

		ht.put("likelihood_cd", new WebFieldSelect("likelihood_cd",
				addMode ? "" : db.getText("likelihood_cd"), sm
						.getCodes("PATSAFELK")));

		ht.put("breadth_cd", new WebFieldSelect("breadth_cd", addMode ? "" : db
				.getText("breadth_cd"), sm.getCodes("PATSAFESC")));

		ht.put("rank_cd", new WebFieldSelect("rank_cd", addMode ? "" : db
				.getText("rank_cd"), sm.getCodes("RANK10")));

		ht.put("work_around_cd", new WebFieldSelect("work_around_cd",
				addMode ? "" : db.getText("work_around_cd"), sm
						.getCodes("YESNONA")));

		ht.put("impact_cd", new WebFieldSelect("impact_cd", addMode ? "" : db
				.getText("impact_cd"), sm.getCodes("HIGHMEDLOW"), true, false));

		ht.put("urgency_cd", new WebFieldSelect("urgency_cd", (addMode ? ""
				: db.getText("urgency_cd")), sm.getCodes("SEVERITY"), false,
				false));

		/*
		 * Remedy Fields
		 */

		// for appending text to Remedy....see the absRemedyPlugin that passes
		// this to Remedy
		ht.put("worklog", new WebFieldString("worklog", (addMode ? "" : ""),
				80, 255));

		ht.put("worklog_blob", new WebFieldDisplay("worklog_blob",
				(addMode ? "" : db.getText("worklog_blob"))));

		ht.put("remedy_asof_date", new WebFieldDisplay("remedy_asof_date",
				(addMode ? "" : db.getText("remedy_asof_date"))));

		ht.put("remedy_cat_tx", new WebFieldDisplay("remedy_cat_tx",
				(addMode ? "" : db.getText("remedy_cat_tx"))));

		ht.put("remedy_type_tx", new WebFieldDisplay("remedy_type_tx",
				(addMode ? "" : db.getText("remedy_type_tx"))));

		ht.put("remedy_item_tx", new WebFieldDisplay("remedy_item_tx",
				(addMode ? "" : db.getText("remedy_item_tx"))));

		ht.put("remedy_grp_tx", new WebFieldDisplay("remedy_grp_tx",
				(addMode ? "" : db.getText("remedy_grp_tx"))));

		ht.put("owner_uid", new WebFieldDisplay("owner_uid", addMode ? "" : db
				.getText("remedyOwner")));

		ht.put("requester_uid", new WebFieldSelect("requester_uid",
				addMode ? new Integer("0") : db.getInteger("requester_uid"), sm
						.getContactHT(), true, false));

		ht.put("description_blob", new WebFieldText("description_blob",
				addMode ? "" : db.getText("description_blob"), 5, 100));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("REMEDY"), false, false));

		ht.put("escalated_cd", new WebFieldSelect("escalated_cd", (addMode ? ""
				: db.getText("escalated_cd")), sm.getCodes("YESNO"), false,
				false));

		/*
		 * Blobs
		 */

		ht.put("files_tx", new WebFieldText("files_tx", (addMode ? "" : db
				.getText("files_tx")), 5, 100));

		ht.put("next_step_hist_blob", new WebFieldText("next_step_hist_blob",
				(addMode ? "" : db.getText("next_step_hist_blob")), 5, 100));

		ht.put("resolution_blob", new WebFieldText("resolution_blob",
				(addMode ? "" : db.getText("resolution_blob")), 5, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 100));

		ht.put("cleanup_blob", new WebFieldText("cleanup_blob", (addMode ? ""
				: db.getText("cleanup_blob")), 5, 100));

		ht.put("mitigation_blob", new WebFieldText("mitigation_blob",
				(addMode ? "" : db.getText("mitigation_blob")), 5, 100));

		ht.put("workaround_blob", new WebFieldText("workaround_blob",
				(addMode ? "" : db.getText("workaround_blob")), 5, 100));

		ht.put("followup_blob", new WebFieldText("followup_blob", (addMode ? ""
				: db.getText("followup_blob")), 5, 100));

		ht.put("eta_blob", new WebFieldText("eta_blob", (addMode ? "" : db
				.getText("eta_blob")), 5, 100));

		ht.put("communication_blob", new WebFieldText("communication_blob",
				(addMode ? "" : db.getText("communication_blob")), 5, 100));

		ht.put("mitigation_blob", new WebFieldText("mitigation_blob",
				(addMode ? "" : db.getText("mitigation_blob")), 5, 100));

		ht.put("cause_blob", new WebFieldText("cause_blob", (addMode ? "" : db
				.getText("cause_blob")), 5, 100));

		/*
		 * Protected Fields
		 */

		if (sm.getPatientSafetyLevel().equalsIgnoreCase("L")) {

			ht
					.put("status_tx", new WebFieldDisplay("status_tx",
							"-protected-"));

			ht.put("ncal_impact_cd", new WebFieldDisplay("ncal_impact_cd",
					"--protected--"));

			ht.put("cause_blob", new WebFieldDisplay("cause_blob",
					"--protected"));

			ht.put("workaround_status_tx", new WebFieldDisplay(
					"workaround_status_tx", "-protected"));

			ht.put("justification_tx", new WebFieldDisplay("justification_tx",
					"-protected-"));

			ht.put("impact_tx", new WebFieldDisplay("impact_tx",
					"--protected--"));

			ht.put("email_activity_tx", new WebFieldDisplay(
					"email_activity_tx", "--protected--"));

			ht.put("call_duration_tx", new WebFieldDisplay("call_duration_tx",
					"--protected--"));

			ht.put("assigned_by_tx", new WebFieldDisplay("assigned_by_tx",
					"--protected--"));

		} else {

			ht.put("ncal_impact_cd", new WebFieldSelect("ncal_impact_cd",
					addMode ? "" : db.getText("ncal_impact_cd"), ncal_cd));

			ht.put("cause_blob", new WebFieldText("cause_blob", (addMode ? ""
					: db.getText("cause_blob")), 5, 100));

			ht.put("workaround_status_tx", new WebFieldString(
					"workaround_status_tx", (addMode ? "" : db
							.getText("workaround_status_tx")), 64, 128));

			ht.put("assigned_by_tx", new WebFieldString("assigned_by_tx",
					(addMode ? "" : db.getText("assigned_by_tx")), 64, 64));

			ht.put("status_tx", new WebFieldString("status_tx", (addMode ? ""
					: db.getText("status_tx")), 128, 128));

			ht.put("justification_tx", new WebFieldString("justification_tx",
					(addMode ? "" : db.getText("justification_tx")), 128, 128));

			ht.put("impact_tx", new WebFieldString("impact_tx", (addMode ? ""
					: db.getText("impact_tx")), 128, 128));

			ht
					.put("email_activity_tx", new WebFieldString(
							"email_activity_tx", (addMode ? "" : db
									.getText("email_activity_tx")), 128, 128));

			ht.put("call_duration_tx", new WebFieldString("call_duration_tx",
					(addMode ? "" : db.getText("call_duration_tx")), 128, 128));

		}

		return ht;

	}

	/*
	 * Excel Interface
	 */

	// create Excel from ResultSet and save to the path in the web.xml config
	// file
	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "Incident.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;

		int columns = 33;
		short startRow = 1;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath, sm
				.getLastName()
				+ "_Patient_Safety", getExcelResultSet(), startRow, columns);

	}

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vincident_excel WHERE 1=1 ");

		// default status to open if no filter present
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND NCAL_Status = 'O'");
		}

		else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
				sb.append(" AND NCAL_Status = '" + sm.Parm("FilterStatus")
						+ "'");
			}
		}

		// filter on interface
		if (sm.Parm("FilterSuite").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterSuite").equalsIgnoreCase("0")) {
				sb.append(" AND SUITE = '" + sm.Parm("FilterSuite") + "'");
			}
		}

		// filter on interface

		if (sm.Parm("FilterGroup").length() == 0) {
		}

		else {

			if (!sm.Parm("FilterGroup").equalsIgnoreCase("0")) {
				sb.append(" AND GroupCode = '" + sm.Parm("FilterGroup") + "'");
			}
		}

		if (sm.Parm("FilterOpen").length() == 0) {

		} else {
			if (!sm.Parm("FilterOpen").equalsIgnoreCase("0")) {
				String filterMonth = sm.Parm("FilterOpen").substring(0, 2);
				String filterYear = "20"
						+ sm.Parm("FilterOpen").substring(3, 5);

				sb.append(" AND datepart(month, incident_date) = "
						+ filterMonth + " AND datepart(yyyy, incident_date) = "
						+ filterYear);
			}

		}

		sb.append(" ORDER BY IDd_Date");
		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

}
