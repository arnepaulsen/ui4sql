/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Hashtable;
import forms.*;
import java.util.Date;
import java.util.Calendar;

import router.SessionMgr;
import db.DbField;
import db.DbFieldDate;
import db.DbFieldString;

/**
 * epiccr Plugin
 * 
 */
/*******************************************************************************
 * Service Request Plugin
 * 
 * Change Log:
 * 
 * 6/12/07 using new 'blobs' on progress_blob
 * 
 * Notes: This is specific to  Epic
 * 
 * 
 * This plugin is unique in that it computes about 10 dates during the save
 * process. Each date has an 'actual' flag associated with it, and those dates
 * that are marked as 'actual; are left alone. the other dates have various
 * formulas that are applied, and the computed value is used to replace what
 * comes from the web page. this all happens during the 'before update' phase.
 * 
 * 
 * Change log:
 * 
 * 10/2 - order by cr_no - add logical 'N.Calif' + 'N. & S. Calif' to region
 * selector - expand short name - progress notes on list screen
 * 
 * 9/8/08 - add DbField[] to listBgColor
 * 
 ******************************************************************************/
public class EpiccrPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public EpiccrPlugin() throws services.ServicesException {
		super();
		this.setTableName("tepic_cr");
		this.setKeyName("epiccr_id");
		this.setKeyName("cr_id");
		this.setTargetTitle("Vendor Change Request");
	
		this.setListOrder ("tepic_cr.cr_no");

		this.setListHeaders( new String[] { "CR #", "Title", "Requested",
				"Estimated", "Revised", "Status", "Type", "Application",
				"Progress.." });

		this.setMoreListJoins(new  String[] {
				" left join tcodes stat on tepic_cr.status_cd = stat.code_value and stat.code_type_id = 73 ",
				" left join tcodes region on tepic_cr.region_cd = region.code_value and region.code_type_id = 108 ",
				" left join tapplications appl on tepic_cr.application_id = appl.application_id " });

		this.setMoreSelectJoins (new String[] {
				" left join tcontact epic on tepic_cr.epic_uid = epic.contact_id ",
				" left join tuser requestor on tepic_cr.requestor_uid = requestor.user_id ",
				" left join tuser leader on tepic_cr.leader_uid = leader.user_id ",
				" left join tuser implementor on tepic_cr.implementor_uid = implementor.user_id " });

		this.setMoreSelectColumns (new String[] {
				"epic.email_address as epic_email_nm",
				"'phone' as epic_phone_nm",
				"requestor.email_address as requestor_email_nm",
				"requestor.phone_tx as requestor_phone_nm",
				"leader.email_address as leader_email_nm",
				"leader.phone_tx as leader_phone_nm",
				"implementor.email_address as implementor_email_nm",
				"implementor.phone_tx as implementor_phone_nm" });


	}

	public void init(SessionMgr parmSm) {
		this.sm = parmSm;
		this.db = this.sm.getDbInterface(); // has an open connection
		this.setUpdatesOk(sm.userIsLeader());
		this.setSubmitOk(false);
		this.setGotoOk(true);
		this.setGotoDisplayName("CR #: ");
		this.setGotoKeyName("cr_no");
		

		this.setMoreListColumns(new  String[] {
				" tepic_cr.cr_no as theKey",
				" title_nm",
				dbprefix + "FormatDateTime(requested_date, 'mm/dd/yy') as requested_show",
				dbprefix + "FormatDateTime(estimated_date, 'mm/dd/yy') as estimated_show",
				dbprefix + "FormatDateTime(revised_date, 'mm/dd/yy') as revised_show",
				" stat.code_desc as StatDesc",
				"region.code_desc as RegionDesc", " application_name",
				" substring(progress_blob, 1, 10) as theProgress",
				" tepic_cr.status_cd" });
		
	}
	
	/*
	 * Permissions
	 */

	


	public boolean getListColumnCenterOn(int column) {
		if (column == 2 || column == 3 || column == 4)
			return true;
		else
			return false;
	}

	
	// return bgcolor=yellow if install date < today
	public String listBgColor(int columnNumber, String value, DbField[] fields) {
		if (columnNumber == 3) {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
			try {
				Date date = sdf.parse(value);
				if (date.before(new Date())) {
					return "bgcolor=yellow";
				}
			} catch (ParseException e) {
				return "";
			}
			return "";
		} else
			return "";
	}

	/*
	 * List Selector Controls
	 */

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 5 || columnNumber == 6 || columnNumber == 7)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : only the status column will be called, so no need to check the
	 * column #
	 */
	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {

		case 5: {

			return getListSelector("FilterStatus", "OP", "All Status",
					getStatHT());

		}
		case 6: {

			return getListSelector("FilterRegion", "NCAL", "Region?",
					getRegionHT());

		}

		default: {
			// application filter

			return getListSelector("FilterApplication", new Integer("0"),
					"All Applications", sm.getApplicationFilter());

		}
		}
	}

	/*
	 * Dates that are not flagged as 'actual' are computed here. The user input
	 * is overridden are replaced with the computed values.
	 * 
	 */
	public void beforeUpdate(Hashtable<String, DbField> ht) {

		// get the requested date.. it drives many of the others

		Date requested = new Date();

		try {
			DbFieldDate epic_dt = (DbFieldDate) ht.get("requested_date");
			requested = (Date) epic_dt.fieldValue;

		} catch (Exception e) {
			debug("Exception getting requested date : " + e.toString());
		}

		// 1 Sponsored Date = Request Date + 7

		try {
			DbFieldString f = (DbFieldString) ht.get("sponsored_dt_flag");
			String flag = f.getText();

			if (!flag.equalsIgnoreCase("Y")) {
				Date date = addDays(requested, 7);
				try {
					ht.remove("sponsored_date");
				} catch (Exception e) {
					debug("error removing sponsored date");
				}
				ht.put("sponsored_date",
						new DbFieldDate("sponsored_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating sponsored date : " + e.toString());
		}

		// 2 NATL Assign CR DAte = Request Date + 8

		try {
			DbFieldString f = (DbFieldString) ht.get("cr_assign_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				Date date = addDays(requested, 8);
				try {
					ht.remove("cr_assign_date");
				} catch (Exception e) {
				}
				ht.put("cr_assign_date",
						new DbFieldDate("cr_assign_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating sponsored date : " + e.toString());
		}

		// 3 Epic-Pre Approval Date = requied_date + 11

		try {
			DbFieldString f = (DbFieldString) ht
					.get("epic_preapproval_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				Date date = addDays(requested, 11);
				try {
					ht.remove("epic_preapproval_date");
				} catch (Exception e) {

				}
				ht.put("epic_preapproval_date", new DbFieldDate(
						"epic_preapproval_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating epic_preapproval date: " + e.toString());
		}

		// 4 NCAL1 Approval Date = monday after epic_preapproval_date

		try {
			DbFieldString f = (DbFieldString) ht.get("ncal1_approval_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht
						.get("epic_preapproval_date");
				Date d = (Date) epic_dt.fieldValue;
				Date nextMonday = nextMonday(d);
				try {
					ht.remove("ncal1_approval_date");
				} catch (Exception e) {
				}
				ht.put("ncal1_approval_date", new DbFieldDate(
						"ncal1_approval_date", nextMonday));
			}
		} catch (Exception e) {
			debug("Exception updating ncal1 approval date: " + e.toString());
		}

		// 5 ECB 1 Approval Date = Wednesday after next Sunday of NCAL
		// 

		try {
			DbFieldString f = (DbFieldString) ht.get("ecb1_approval_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht
						.get("ncal1_approval_date");
				Date d = (Date) epic_dt.fieldValue;
				Date nextWed = wednesdayAfterSunday(d);
				try {
					ht.remove("ecb1_approval_date");
				} catch (Exception e) {

				}
				ht.put("ecb1_approval_date", new DbFieldDate(
						"ecb1_approval_date", nextWed));
			}
		} catch (Exception e) {
			debug("Exception updating ecb1 approval date: " + e.toString());
		}

		// 6 Epic Prov Est. = ECB Approval 1 date + 14 days

		try {
			DbFieldString f = (DbFieldString) ht.get("epic_prov_est_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht
						.get("ecb1_approval_date");
				Date ecb1 = (Date) epic_dt.fieldValue;
				Date date = addDays(ecb1, 14);
				try {
					ht.remove("epic_prov_est_date");
				} catch (Exception e) {
				}
				ht.put("epic_prov_est_date", new DbFieldDate(
						"epic_prov_est_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating epic prov est date: " + e.toString());
		}

		// 7 NCAL Approval 2 = Monday after 6. Epic Prov. Est

		try {
			DbFieldString f = (DbFieldString) ht.get("ncal2_approval_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht
						.get("epic_prov_est_date");
				Date epic = (Date) epic_dt.fieldValue;
				Date date = nextMonday(epic);
				try {
					ht.remove("ncal2_approval_date");
				} catch (Exception e) {
				}
				ht.put("ncal2_approval_date", new DbFieldDate(
						"ncal2_approval_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating ncal 2 date: " + e.toString());
		}

		// 8 ECB Approval 2 = Wed after next Sunday from NCAL 2 date

		try {
			DbFieldString f = (DbFieldString) ht.get("ecb2_approval_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht
						.get("ncal2_approval_date");
				Date ncal2 = (Date) epic_dt.fieldValue;
				Date date = wednesdayAfterSunday(ncal2);
				try {
					ht.remove("ecb2_approval_date");
				} catch (Exception e) {
				}
				ht.put("ecb2_approval_date", new DbFieldDate(
						"ecb2_approval_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating ecb 2 date: " + e.toString());
		}

		// 9 Code to National = ECB approval 2 + 45 days

		try {
			DbFieldString f = (DbFieldString) ht.get("release_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht
						.get("ecb2_approval_date");
				Date ecb2 = (Date) epic_dt.fieldValue;
				Date date = addDays(ecb2, 45);
				try {
					ht.remove("release_date");
				} catch (Exception e) {
				}
				ht.put("release_date", new DbFieldDate("release_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating release date: " + e.toString());
		}

		// 10 KPHC Testing = Code To National + 14 days

		try {
			DbFieldString f = (DbFieldString) ht.get("test2_dt_flag");
			String flag = f.getText();
			debug("test 2 date flag : " + flag);
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht.get("release_date");
				Date release = (Date) epic_dt.fieldValue;
				Date date = addDays(release, 14);
				try {
					ht.remove("test2_date");
				} catch (Exception e) {
				}
				ht.put("test2_date", new DbFieldDate("test2_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating Test 2 date: " + e.toString());
		}

		// 11 Estimated Production Date = Required (by Suite) Date

		try {
			DbFieldString f = (DbFieldString) ht.get("estimated_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht.get("required_date");
				Date date = (Date) epic_dt.fieldValue;

				try {
					ht.remove("estimated_date");
				} catch (Exception e) {

				}
				ht.put("estimated_date",
						new DbFieldDate("estimated_date", date));
			}

		} catch (Exception e) {
			debug("Exception updating estimated date: " + e.toString());
		}

		// 14 Actual Production Date = testing date 2 + 30

		try {
			DbFieldString f = (DbFieldString) ht.get("golive_dt_flag");
			String flag = f.getText();
			if (!flag.equalsIgnoreCase("Y")) {
				DbFieldDate epic_dt = (DbFieldDate) ht.get("test2_date");
				Date test2 = (Date) epic_dt.fieldValue;
				Date date = addDays(test2, 30);
				try {
					ht.remove("golive_date");
				} catch (Exception e) {
				}
				ht.put("golive_date", new DbFieldDate("golive_date", date));
			}
		} catch (Exception e) {
			debug("Exception updating golive/production date: " + e.toString());
		}

	}

	private Date addDays(Date date, int days) {
		Calendar c = getCalendar(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

	private Date nextMonday(Date today) {

		Calendar c = getCalendar(today);

		int dayofweek = c.get(Calendar.DAY_OF_WEEK);

		int days_to_monday = 1; // for Sunday to Monday

		if (dayofweek != 1)
			days_to_monday = 9 - dayofweek;

		c.add(Calendar.DATE, days_to_monday);

		return c.getTime();

	}

	// computes the Wednesday after the following Sunday
	private Date wednesdayAfterSunday(Date today) {

		Calendar c = getCalendar(today);
		int dayofweek = c.get(Calendar.DAY_OF_WEEK);
		c.add(Calendar.DATE, 7 - dayofweek + 4);
		return c.getTime();
	}

	private Calendar getCalendar(Date d) {

		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(d);
		return cal;
	}

	/*
	 * Region codes, plus an abstraction of N.Calif + N.S. Calif .. then the
	 * list query has to check for the abstraction of 'N. Calf' or 'N.Calif or
	 * 'N.& S. Calif.'
	 */

	private Hashtable getRegionHT() {

		String sql = " select order_by as odor, code_value, code_desc from tcodes "
				+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
				+ " where tcodes.code_type_id  = tcode_types.code_type_id  and  tcode_types.code_type = 'REGION' "
				+ " UNION select 200 as odor , 'ALLNCAL' as y, 'N.Cal. + N&S' z "
				+ " ORDER BY 1 ";

		return sm.getTable("CR_REGION_2", sql);
	}

	/*
	 * 
	 * 
	 * This selector table is the Change Request Status codes, plus an
	 * abstraction of all open, all closed, all deferred !
	 * 
	 * ... then the query has to sort it out ... note the order by slices in the
	 * 'All Closed', etc just before the start of the closed list, etc. ... so
	 * the user can mess it up if they change the order numbers A FIRST HERE...
	 * 
	 */

	private Hashtable getStatHT() {

		String sql = " select order_by as odor, code_value, code_desc from tcodes "
				+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
				+ " where tcodes.code_type_id  = tcode_types.code_type_id  and  tcode_types.code_type = 'CRSTATUS' "
				+ " UNION select 3 as odor , 'ALLCLS' as y, 'All Closed' z "
				+ " UNION select 37 as odor , 'ALLDEF' as y, 'All Deferred' z "
				+ " UNION select 41 as odor , 'ALLOPE' as y, 'All Open' z "
				+ " ORDER BY 1 ";

		return sm.getTable("CR_STAT_EXP", sql);

	
	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present

		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND tepic_cr.status_cd LIKE 'OP%'");
		}

		else {

			// either filter status on a generic class, like "all open", "all
			// closed", "all deferred"
			// or a specific code !

			if (sm.Parm("FilterStatus").length() > 1) {

				if (sm.Parm("FilterStatus").startsWith("ALL")) {
					sb.append(" AND tepic_cr.status_cd LIKE '"
							+ sm.Parm("FilterStatus").substring(3) + "%'");
				} else {
					if (sm.Parm("FilterStatus").length() > 1) {
						sb.append(" AND tepic_cr.status_cd = '"
								+ sm.Parm("FilterStatus") + "'");
					}
				}
			}
		}

		// filter region

		if (sm.Parm("FilterRegion").length() == 0) {
			sb.append(" AND region.code_value = 'NCAL'");
		} else {
			// watch out for the abstraction of (N.Calif) or (N.& S. Calif.)
			if (sm.Parm("FilterRegion").equals("ALLNCAL")) {
				sb
						.append(" AND (region.code_value = 'NCAL' OR region.code_value = 'CAL') ");
			} else
				sb.append(" AND region.code_value = '"
						+ sm.Parm("FilterRegion") + "'");

		}

		// filter on application
		if (sm.Parm("FilterApplication").length() == 0) {
		}

		else {
			if (!sm.Parm("FilterApplication").equalsIgnoreCase("0")) {
				sb.append(" AND tepic_cr.application_id = "
						+ sm.Parm("FilterApplication"));
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
		 * Error Message
		 */

		ht.put("msg", new WebFieldDisplay("msg", " "));

		/*
		 * flags
		 */

		String[] dateFlags = { "Actual", "Est." };
		String[] emptyFlags = { "", "" };

		// for checkboxes, don't display if the corresponding date is not
		// entered

		ht.put("revised_dt_flag", new WebFieldCheckbox("revised_dt_flag",
				addMode ? "N" : db.getText("revised_dt_flag"), "Actual", (db
						.getText("revised_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("release_dt_flag", new WebFieldCheckbox("release_dt_flag",
				addMode ? "N" : db.getText("release_dt_flag"), "Actual", (db
						.getText("release_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("sponsored_dt_flag", new WebFieldCheckbox("sponsored_dt_flag",
				addMode ? "N" : db.getText("sponsored_dt_flag"), "Actual", (db
						.getText("sponsored_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("epic_prov_est_dt_flag", new WebFieldCheckbox(
				"epic_prov_est_dt_flag", addMode ? "N" : db
						.getText("epic_prov_est_dt_flag"), "Actual",
				(db.getText("epic_prov_est_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("cr_assign_dt_flag", new WebFieldCheckbox("cr_assign_dt_flag",
				addMode ? "N" : db.getText("cr_assign_dt_flag"), "Actual", (db
						.getText("cr_assign_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("estimated_dt_flag", new WebFieldCheckbox("estimated_dt_flag",
				addMode ? "N" : db.getText("estimated_dt_flag"), "Actual", (db
						.getText("estimated_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("rcc_dt_flag", new WebFieldCheckbox("rcc_dt_flag", addMode ? "N"
				: db.getText("rcc_dt_flag"), "Actual", (db.getText("rcc_date")
				.length() == 0) ? emptyFlags : dateFlags));

		ht.put("golive_dt_flag", new WebFieldCheckbox("golive_dt_flag",
				addMode ? "N" : db.getText("golive_dt_flag"), "Actual", (db
						.getText("golive_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("test2_dt_flag", new WebFieldCheckbox("test2_dt_flag",
				addMode ? "N" : db.getText("test2_dt_flag"), "Actual", (db
						.getText("test2_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht
				.put("epic_preapproval_dt_flag",
						new WebFieldCheckbox("epic_preapproval_dt_flag",
								addMode ? "N" : db
										.getText("epic_preapproval_dt_flag"),
								"Actual", (db.getText("epic_preapproval_date")
										.length() == 0) ? emptyFlags
										: dateFlags));

		ht.put("ncal1_approval_dt_flag", new WebFieldCheckbox(
				"ncal1_approval_dt_flag", addMode ? "N" : db
						.getText("ncal1_approval_dt_flag"), "Actual",
				(db.getText("ncal1_approval_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("ncal2_approval_dt_flag", new WebFieldCheckbox(
				"ncal2_approval_dt_flag", addMode ? "N" : db
						.getText("ncal2_approval_dt_flag"), "Actual",
				(db.getText("ncal2_approval_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("ecb1_approval_dt_flag", new WebFieldCheckbox(
				"ecb1_approval_dt_flag", addMode ? "N" : db
						.getText("ecb1_approval_dt_flag"), "Actual",
				(db.getText("ecb1_approval_date").length() == 0) ? emptyFlags
						: dateFlags));

		ht.put("ecb2_approval_dt_flag", new WebFieldCheckbox(
				"ecb2_approval_dt_flag", addMode ? "N" : db
						.getText("ecb2_approval_dt_flag"), "Actual",
				(db.getText("ecb2_approval_date").length() == 0) ? emptyFlags
						: dateFlags));

		/*
		 * Numbers
		 */

		ht.put("rfc_no", new WebFieldString("rfc_no", (addMode ? "" : db
				.getText("rfc_no")), 8, 8));

		ht.put("rfc_prod_no", new WebFieldString("rfc_prod_no", (addMode ? ""
				: db.getText("rfc_prod_no")), 8, 8));

		ht.put("rfc_test_no", new WebFieldString("rfc_test_no", (addMode ? ""
				: db.getText("rfc_test_no")), 8, 8));

		ht.put("pr_no", new WebFieldString("pr_no", (addMode ? "" : db
				.getText("pr_no")), 8, 8));

		ht.put("sr_no", new WebFieldString("sr_no", (addMode ? "" : db
				.getText("sr_no")), 8, 8));

		/*
		 * Strings
		 */

		ht.put("estimate_tx", new WebFieldString("estimate_tx", (addMode ? ""
				: db.getText("estimate_tx")), 32, 64));

		ht.put("prequestor_nm", new WebFieldString("requestor_nm",
				(addMode ? "" : db.getText("requestor_nm")), 32, 32));

		ht.put("requestor_nm", new WebFieldString("requestor_nm", (addMode ? ""
				: db.getText("requestor_nm")), 32, 32));

		ht.put("cr_no", new WebFieldString("cr_no", (addMode ? "" : db
				.getText("cr_no")), 6, 6));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 128, 128));

		ht.put("status_tx", new WebFieldString("status_tx", (addMode ? "" : db
				.getText("status_tx")), 64, 100));

		ht.put("region_nm", new WebFieldString("region_nm", (addMode ? "" : db
				.getText("region_nm")), 32, 32));

		ht.put("other_region_nm", new WebFieldString("other_region_nm",
				(addMode ? "" : db.getText("other_region_nm")), 32, 32));

		ht.put("team_nm", new WebFieldString("team_nm", (addMode ? "" : db
				.getText("team_nm")), 32, 32));

		ht.put("release_nm", new WebFieldString("release_nm", (addMode ? ""
				: db.getText("release_nm")), 16, 32));

		ht.put("implementor_team_tx", new WebFieldString("implementor_team_tx",
				(addMode ? "" : db.getText("implementor_team_tx")), 45, 45));

		ht.put("implementor_reg_tx", new WebFieldString("implementor_reg_tx",
				(addMode ? "" : db.getText("implementor_reg_tx")), 45, 45));

		ht.put("sec_products_tx", new WebFieldString("sec_products_tx",
				(addMode ? "" : db.getText("sec_products_tx")), 45, 45));

		ht.put("clinical_team_tx", new WebFieldString("clinical_team_tx",
				(addMode ? "" : db.getText("clinical_team_tx")), 45, 45));

		ht.put("billing_team_tx", new WebFieldString("billing_team_tx",
				(addMode ? "" : db.getText("billing_team_tx")), 45, 45));

		ht.put("foundation_team_tx", new WebFieldString("foundation_team_tx",
				(addMode ? "" : db.getText("foundation_team_tx")), 45, 45));

		/*
		 * Dates
		 */

		// 1.
		ht.put("sponsored_date", new WebFieldDate("sponsored_date",
				addMode ? "" : db.getText("sponsored_date")));

		// 2.
		ht.put("cr_assign_date", new WebFieldDate("cr_assign_date",
				addMode ? "" : db.getText("cr_assign_date")));

		// 3.
		ht.put("epic_preapproval_date", new WebFieldDate(
				"epic_preapproval_date", addMode ? "" : db
						.getText("epic_preapproval_date")));

		// 4. ncal approval 1 - compute as nextMonday from date 3

		ht.put("ncal1_approval_date", new WebFieldDate("ncal1_approval_date",
				addMode ? "" : db.getText("ncal1_approval_date")));

		ht.put("rcc_date", new WebFieldDate("rcc_date", addMode ? "" : db
				.getText("rcc_date")));

		ht.put("required_date", new WebFieldDate("required_date", addMode ? ""
				: db.getText("required_date")));

		ht.put("ncal2_approval_date", new WebFieldDate("ncal2_approval_date",
				addMode ? "" : db.getText("ncal2_approval_date")));

		ht.put("epic_prov_est_date", new WebFieldDate("epic_prov_est_date",
				addMode ? "" : db.getText("epic_prov_est_date")));

		ht.put("ecb1_approval_date", new WebFieldDate("ecb1_approval_date",
				addMode ? "" : db.getText("ecb1_approval_date")));

		ht.put("ecb2_approval_date", new WebFieldDate("ecb2_approval_date",
				addMode ? "" : db.getText("ecb2_approval_date")));

		ht.put("delivery_date", new WebFieldDate("delivery_date", addMode ? ""
				: db.getText("delivery_date")));

		ht.put("estimated_date", new WebFieldDate("estimated_date",
				addMode ? "" : db.getText("estimated_date")));

		ht.put("requested_date", new WebFieldDate("requested_date",
				addMode ? "" : db.getText("requested_date")));

		ht.put("expected_date", new WebFieldDate("expected_date", addMode ? ""
				: db.getText("expected_date")));

		ht.put("release_date", new WebFieldDate("release_date", addMode ? ""
				: db.getText("release_date")));

		ht.put("revised_date", new WebFieldDate("revised_date", addMode ? ""
				: db.getText("revised_date")));

		ht.put("golive_date", new WebFieldDate("golive_date", addMode ? "" : db
				.getText("golive_date")));

		ht.put("test1_date", new WebFieldDate("test1_date", addMode ? "" : db
				.getText("test1_date")));

		ht.put("test2_date", new WebFieldDate("test2_date", addMode ? "" : db
				.getText("test2_date")));

		/*
		 * Ids
		 */

		ht.put("requestor_uid", new WebFieldSelect("requestor_uid",
				addMode ? new Integer("0") : db.getInteger("requestor_uid"), sm
						.getContactHT(), true));

		ht.put("leader_uid", new WebFieldSelect("leader_uid",
				addMode ? new Integer("0") : db.getInteger("leader_uid"), sm
						.getContactHT(), true));

		ht.put("implementor_uid", new WebFieldSelect("implementor_uid",
				addMode ? new Integer("0") : db.getInteger("implementor_uid"),
				sm.getContactHT(), true));

		ht.put("epic_uid", new WebFieldSelect("epic_uid",
				addMode ? new Integer("0") : db.getInteger("epic_uid"), sm
						.getVendorHT(), true));

		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? new Integer("0") : db.getInteger("application_id"),
				sm.getApplicationFilter()));

		/*
		 * Codes
		 */

		ht.put("delay_cd", new WebFieldSelect("delay_cd", addMode ? "" : db
				.getText("delay_cd"), sm.getCodes("DELAY")));

		ht.put("patient_safety_cd", new WebFieldSelect("patient_safety_cd",
				addMode ? "" : db.getText("patient_safety_cd"), sm
						.getCodes("YESNO")));

		ht.put("region_cd", new WebFieldSelect("region_cd", addMode ? "" : db
				.getText("region_cd"), sm.getCodes("REGION")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "DRAFT"
				: db.getText("status_cd"), sm.getCodes("CRSTATUS")));

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("CRTYPES"), true));

		ht.put("emergency_cd", new WebFieldSelect("emergency_cd", addMode ? ""
				: db.getText("emergency_cd"), sm.getCodes("YESNO")));

		ht.put("severity_cd", new WebFieldSelect("severity_cd", addMode ? ""
				: db.getText("severity_cd"), sm.getCodes("RANK10")));

		ht.put("eeg_scope_cd", new WebFieldSelect("eeg_scope_cd", addMode ? ""
				: db.getText("eeg_scope_cd"), sm.getCodes("YESNO")));

		ht.put("eeg_decision_cd", new WebFieldSelect("eeg_decision_cd",
				addMode ? "" : db.getText("eeg_decision_cd"), sm
						.getCodes("YESNO")));

		/*
		 * Blobs
		 */

		ht.put("release_tx", new WebFieldText("release_tx", addMode ? "" : db
				.getText("release_tx"), 2, 32));

		ht.put("std_custom_tx", new WebFieldText("std_custom_tx", addMode ? ""
				: db.getText("std_custom_tx"), 2, 32));

		ht.put("progress_blob", new WebFieldText("progress_blob", addMode ? ""
				: db.getText("progress_blob"), 10, 100));

		ht.put("co_tx", new WebFieldText("co_tx", addMode ? "" : db
				.getText("co_tx"), 5, 100));

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("benefits_blob", new WebFieldText("benefits_blob", addMode ? ""
				: db.getText("benefits_blob"), 5, 100));

		ht.put("alternatives_blob", new WebFieldText("alternatives_blob",
				addMode ? "" : db.getText("alternatives_blob"), 5, 100));

		ht.put("risks_blob", new WebFieldText("risks_blob", addMode ? "" : db
				.getText("risks_blob"), 5, 100));

		ht.put("regn_analysis_blob", new WebFieldText("regn_analysis_blob",
				addMode ? "" : db.getText("regn_analysis_blob"), 5, 100));

		ht.put("regn_impact_blob", new WebFieldText("regn_impact_blob",
				addMode ? "" : db.getText("regn_impact_blob"), 5, 100));

		ht.put("epic_use_blob", new WebFieldText("epic_use_blob", addMode ? ""
				: db.getText("epic_use_blob"), 5, 100));

		ht.put("epic_comment_blob", new WebFieldText("epic_comment_blob",
				addMode ? "" : db.getText("epic_comment_blob"), 5, 100));

		ht.put("eeg_comments_blob", new WebFieldText("eeg_comments_blob",
				addMode ? "" : db.getText("eeg_comments_blob"), 5, 100));

		ht.put("epic_recommendation_blob", new WebFieldText(
				"epic_recommendation_blob", addMode ? "" : db
						.getText("epic_recommendation_blob"), 5, 100));

		/*
		 * Ids
		 */
		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("application_id"),
				sm.getApplicationFilter(), true));

		if (!addMode) {
			putSelectFields(ht);
		}

		return ht;
	}

	/*
	 * these fields are taken from joins....off the tuser table
	 */

	private void putSelectFields(Hashtable<String, WebField> ht) {

		ht.put("epic_email_nm", new WebFieldDisplay("epic_email_nm", (db
				.getText("epic_email_nm"))));

		ht.put("epic_phone_nm", new WebFieldDisplay("epic_phone_nm", (db
				.getText("epic_phone_nm"))));

		ht.put("requestor_email_nm", new WebFieldDisplay("requestor_email_nm",
				(db.getText("requestor_email_nm"))));

		ht.put("requestor_phone_nm", new WebFieldDisplay("requestor_phone_nm",
				(db.getText("requestor_phone_nm"))));

		ht.put("leader_email_nm", new WebFieldDisplay("leader_email_nm", (db
				.getText("leader_email_nm"))));

		ht.put("leader_phone_nm", new WebFieldDisplay("leader_phone_nm", (db
				.getText("leader_phone_nm"))));

		ht.put("implementor_email_nm", new WebFieldDisplay(
				"implementor_email_nm", (db.getText("implementor_email_nm"))));

		ht.put("implementor_phone_nm", new WebFieldDisplay(
				"implementor_phone_nm", (db.getText("implementor_phone_nm"))));

	}

}
