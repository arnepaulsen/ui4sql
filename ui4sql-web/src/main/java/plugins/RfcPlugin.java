/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import forms.WebField;
import forms.WebFieldDate;
import forms.WebFieldDisplay;
import forms.WebFieldHidden;
import forms.WebFieldSelect;
import forms.WebFieldString;
import forms.WebFieldText;
import org.apache.poi.hssf.util.HSSFColor;

import db.DbField;
import db.DbFieldString;

import router.SessionMgr;
import services.ExcelWriter;
import services.ServicesException;

/*******************************************************************************
 * Change Approval Board - Review
 * 
 * Keywords - sql server only
 * 
 * Change Log:
 * 
 * 12/19/06
 * 
 * Wow! First plugin to use /a date filter on the list bar
 * 
 * Shares abstract withg ServiceRequestPlugin... they are essentially the same,
 * but different data tables.
 * 
 * inprogress:
 * 
 * from Danny spreasheet: remedy priority - does it exist in remedy ? or is
 * urgency ? remedy effort ? map to reffort__hours_no save create_date to
 * database
 * 
 * 12/22/07 add forceSaveEdit option to force user back to edit after new/save
 * 3/6/08 add table name to list order to prevent ambq. column error
 * 
 * 10/13/08 fix bug in WebFieldDispla("comment_blob", db.getText("comment_blob")
 * need both parameters!
 * 
 * 1/2/09 Replace string table with tcodes for SUITERIP
 * 
 * 6/4/10 - Block edit if facb_status_cd is 'submitted'
 * 
 * 6/8/10 - new ncf-cab excel export. added one column, and changed the
 * spreadsheet names to lower-case
 * 
 * 7/20/10 - fix "security_sign_off_tx" and interface_sign_off_tx
 * WebFieldDisplay constructor
 * 
 * 12/16/10 VD & AP - Force pull Remedy after the adding new RFC.
 * 		and check for duplicates before an add.
 * 
 * 1/24/11 Change ncf-cutoff to greater then 1/25/11
 * 
 * 2/2/11 - Allow single-row Excel export from the detail page
 * 
 * 2/10/11 - Expand "release_tx" to 250 characters max
 * 
 * 3/18/11 - increase suite/cab excel export to 25 columns
 *  
 *  
 * 
 */

public class RfcPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * List and database overrides
	 * 
	 **************************************************************************/

	// default Excel to suite-level
	public String excelView = "vrfc_excel_suite";
	String excelTemplate = "rfc_suite_cab.xls";

	static String ncf_cab_cutoff_date = "01/25/2011";

	public boolean ncf_cab = false; // sub-class NCF sets to true.

	private static String[] sListHeaders = { "1&nbsp;&nbsp;&nbsp;", "RFC#",
			"Title", "Group", "Suite", "--review status--", "--Suite Status--",
			"FCAB Status", "--Suite Date Filter--", " --NCF Date Filter- ",
			"Release" };
	// removed , "Added Date Filter" };

	private static String[] sListHeaders_ncf = { "1&nbsp;&nbsp;&nbsp;", "RFC#",
			"Title", "Group", "Suite", "--review status--", "--Suite Status--",
			"Seq", "--NCF Review Date--", "Release", "Routine",
			"Online/Offline" };

	public RfcPlugin() throws services.ServicesException {
		super();

	}

	public void init(SessionMgr parmSm) {

		super.init(parmSm);

		this.setTableName("trfc");
		this.setKeyName("rfc_id");
		this.setListAscending(true); // to sort list decending by install
		// date

		// this.remedyType = "RFC";
		// this.remedyKey = "rfc_no";

		this.setSelectViewName("vrfc");
		this.setListViewName("vrfc_list_suite");
		this.setListOrder("suite_review_date, rfc_no");
		
		this.setExcelDetailOk(true);

		this.setGotoOk(true);
		this.setExcelOk(true);
		this.setRemedyOk(true);
		this.setSubmitOk(false);
		this.setShowAuditSubmitApprove(false);
		this.setCopyOk(false);
		this.setDeleteLevel("administrator");
		this.setNextOk(false);
		this.setShowAuditSubmitApprove(false);

		this.setGotoDisplayName("RFC #: ");
		this.setGotoKeyName("rfc_no");

		this.setListHeaders(sListHeaders);

		// ncf-cab doenst' have suite-cab and added-on dates.

		this.setListSelectorColumnFlags(new boolean[] { false, false, false,
				true, true, true, true, !this.ncf_cab, true, true, true, true });

		this.setForceEditOnSave(true); // to force user back to edit after
		// new/savep

		if (this.ncf_cab) {
			this.setListOrder("call_seq_tx, remedy_end_dt");
			this.setListHeaders(sListHeaders_ncf);
			this.setListViewName("vrfc_list_fcab");
			this.setTemplateName("RfcNcfCab.html");

			this.setAddOk(false);
			this.setCopyOk(false);
			this.setDeleteOk(false);

			// only 'executives' can edit
			this.setEditOk(sm.userIsExecutive());

			if (sm.Parm("FilterCallSort").equalsIgnoreCase("Y"))
				this.setListOrder("call_seq_tx, remedy_end_dt");

			else
				this.setListOrder("rfc_no");
		}

	}

	public void preDisplay() {
		super.preDisplay();

		this.setCopyOk(false);

		if (!this.ncf_cab) {

			// turn off add if blocked and user is RC

			// debug( " not facb, user suite : " + sm.getUserSuite() + " chg
			// approver : " + sm.userIsChgApprover());

			if (sm.getUserSuite().equalsIgnoreCase("RC") && sm.getRipBlock()
					&& !(sm.userIsChgApprover())) {

				this.setAddOk(false);
			}

			if (db.hasRow()) {

				// check to see if the use Suite matches the RFC / record
				// 3/11/11 don't allow edit by non-priv user if rfc is closed

				boolean ok = ((db.getText("suite_cd").equalsIgnoreCase(
						sm.getUserSuite())) && !(db.getText("status_cd").equalsIgnoreCase("clo")))
						|| sm.userIsChgApprover();

				// but block for ac's if submitted

				if (db.getText("fcab_review_cd").equalsIgnoreCase("S")
						&& !(sm.userIsChgApprover() || sm.userIsExecutive())) {
					ok = false;
				}

				this.setEditOk(ok);

			}
			/*
			 * merge to single page else {
			 * 
			 * // new mode, match template to user profile
			 * 
			 * if (sm.getUserSuite().equalsIgnoreCase("RC")) {
			 * this.setTemplateName("RfcRipCab.html"); } // new record, no need
			 * to set edit or add okay
			 * 
			 * }
			 */

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.Plugin#afterAdd(java.lang.Integer)
	 * 
	 * 12/16 VD & AP - Force pull Remedy after the adding new RFC
	 */

	public void afterAdd(Integer rowKey) throws services.ServicesException {

		this.remedy_result = getRemedy("trfc", "rfc_id",
				Integer.parseInt(sm.Parm("rfc_no")), this.rowId);

		return;
	}

	// force the user back to the list page if they enter an invalid 'goto' cr
	//
	public String getDataFormName() {
		if (sm.Parm("Action").equalsIgnoreCase("goto")
				&& rowId.equals(new Integer("-1"))) {
			return "list";
		}
		return "";
	}

	/*
	 * List Processing
	 */

	/*
	 * need to return dynamic title using session Parm data
	 */
	public String getListTitle() {

		// this list title line also has input boxes for install data ranges.
		// to do.. add input for rfc/sr # too.

		if (this.ncf_cab) {
			String checked = "";
			if (sm.Parm("FilterCallSort").equalsIgnoreCase("Y")) {
				checked = "checked";
			}

			return ("NCF CAB")
					+ "&nbsp;&nbsp;&nbsp;&nbsp;Install Start:&nbsp;<input type='Text' name='FilterFromDate' id='FilterFromDate' maxlength='11' size='11' value="
					+ sm.Parm("FilterFromDate")
					+ ">&nbsp;<a href=\"javascript:NewCal('FilterFromDate','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;End:&nbsp;&nbsp;<input type='Text' name='FilterToDate' id='FilterToDate' maxlength='11' size='11'value="
					+ sm.Parm("FilterToDate")
					+ ">&nbsp;<a href=\"javascript:NewCal('FilterToDate','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>"
					+ "&nbsp;&nbsp;Call Sequence:&nbsp;<input type='checkbox' name='FilterCallSort' id='FilterCallSort' value=Y "
					+ checked + ">";

		} else {
			return ("RFC Suite Review")
					+ "&nbsp;&nbsp;&nbsp;&nbsp;Install Start:&nbsp;<input type='Text' name='FilterFromDate' id='FilterFromDate' maxlength='11' size='11' value="
					+ sm.Parm("FilterFromDate")
					+ ">&nbsp;<a href=\"javascript:NewCal('FilterFromDate','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;End:&nbsp;&nbsp;<input type='Text' name='FilterToDate' id='FilterToDate' maxlength='11' size='11'value="
					+ sm.Parm("FilterToDate")
					+ ">&nbsp;<a href=\"javascript:NewCal('FilterToDate','mmddyyyy',false,24)\"><img src=\"images/cal.gif\" width=16 height=16 border=0 alt=\"Pick a date\"></a>";

		}
	}

	public boolean getListColumnCenterOn(int columnNumber) {
		// center Patient Safety y/n and the install date
		if (columnNumber > 6)
			return true;
		else
			return false;
	}

	public String listBgColor(int columnNumber, String value, DbField[] fields) {

		// tricky one .. if the list view is changed.. need to change color
		// column here to match
		int color_field = (this.ncf_cab ? 20 : 24);

		if (value != null) {
			return "bgcolor=" + fields[color_field].getText();
		} else
			return "";
	}

	@SuppressWarnings("unchecked")
	public WebField getListSelector(int columnNumber) {

		// filter Remedy group

		if (columnNumber == 3) {

			return getListSelector("FilterGroup", "", "Remedy Group?",
					sm.getCodesAlt("RMDYGRPS"));
		}

		if (columnNumber == 4) {

			String sql = " select order_by as odor, code_value, code_desc2 from tcodes "
					+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
					+ " where tcodes.code_type_id  = tcode_types.code_type_id  and  tcode_types.code_type = 'SUITES' "
					+ " UNION select 201 as odor , 'RCINGENIX' as y, 'RC+Ingenix' z "
					+ " ORDER BY 1 ";

			Hashtable ht = sm.getTable("rfc_suites2", sql);

			// 6/1/10 Don't filter on Suite if NCF_CAB view
			return getListSelector("FilterSuite",
					(this.ncf_cab ? "" : sm.getUserSuite()), "Suite?", ht);
		}

		// filter Remedy group
		if (columnNumber == 5) {

			String sql = " select order_by as odor, code_value, code_desc from tcodes "
					+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
					+ " where tcodes.code_type_id  = tcode_types.code_type_id  and  tcode_types.code_type = 'REMEDY' "
					+ " UNION select 10 as odor , 'ZZZ' as y, 'All Open' z "
					+ " ORDER BY 1 ";

			Hashtable ht = sm.getTable("REMEDY_SELECT_STAT2", sql);

			// 5/30 remove 'zzz' from default
			return getListSelector("FilterStatus", "", "Remedy Status?", ht);
		}

		// review CAB status
		if (columnNumber == 6 && !this.ncf_cab) {

			// instead of "0" for pending
			return getListSelector("FilterReviewStatus", "N", "Suite Status?",
					sm.getCodes("SUITESTAT"));
		}

		// CN-NCF-CAB status is column 6 on the NCF cab page, and 7 on the
		// suite-cab page
		if ((!this.ncf_cab && columnNumber == 7)
				|| (this.ncf_cab && columnNumber == 6)) {

			// 6/1 change default for NCF-CAB page from "New" to "Submitted"
			return getListSelector("FilterReviewFCAB",
					(this.ncf_cab ? "S" : ""), "FCAB Status?",
					sm.getCodes("STATREVIEW"));

		}

		/*
		 * Filter unique Suite Review Dates!
		 */
		if (columnNumber == 8 && !this.ncf_cab) {

			String dateCast1, dateCast2 = null;
			String qry = null;

			dateCast1 = " ( FormatDateTime(suite_review_date, 'yyyy/mm/dd')) ";
			dateCast2 = " ( FormatDateTime(suite_review_date, 'mm/dd/yy')) ";

			qry = " select distinct "
					+ dateCast1
					+ ","
					+ dateCast1
					+ ","
					+ dateCast2
					+ " from trfc where suite_review_date != '' and suite_review_date > '2008/12/31' ";

			Hashtable dates = new Hashtable();

			try {
				dates = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterSuiteDate", "O", "Suite Rv.?", dates);

		}

		/*
		 * Filter unique NCF Review Dates!
		 */
		if ((columnNumber == 9 && !ncf_cab) || ((columnNumber == 8 && ncf_cab))) {

			String dateCast1, dateCast2 = null;
			String qry = null;

			dateCast1 = " (FormatDateTime(review_date, 'yyyy/mm/dd')) ";
			dateCast2 = " (FormatDateTime(review_date, 'mm/dd/yy')) ";

			qry = " select distinct "
					+ dateCast1
					+ ","
					+ dateCast1
					+ ","
					+ dateCast2
					+ " from trfc where review_date != '' and review_date > '2008/12/31' ";

			Hashtable dates = new Hashtable();

			try {
				dates = db.getLookupTable(qry);
			} catch (ServicesException e) {

			}
			return getListSelector("FilterNCFDate", "O", "FCAB Date?", dates);

		}

		/*
		 * get unique install date
		 */

		if ((columnNumber == 10 && !ncf_cab)
				|| ((columnNumber == 9 && ncf_cab))) {

			return getListSelector("FilterRelease", "", "Release?",
					sm.getCodes("KRLSE"));

		}

		if ((columnNumber == 10 && ncf_cab)) {

			return getListSelector("FilterRoutine", "", "Non-Routine?",
					sm.getCodes("YESNO"));

		}

		if ((columnNumber == 11 && ncf_cab)) {

			return getListSelector("FilterOnline", "", "Offline?",
					sm.getCodes("YESNO"));

		}

		// impossible!
		return getListSelector("dummy", "", "", sm.getCodes(""));

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		if ((sm.Parm("FilterGroup").length() > 0)
				&& (!sm.Parm("FilterGroup").equalsIgnoreCase("0"))) {
			sb.append(" AND remedy_grp_cd = '" + sm.Parm("FilterGroup") + "'");
		}

		SimpleDateFormat sdf_from_browser = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat sdf_to_query = new SimpleDateFormat("yyyy/MM/dd");

		if (sm.Parm("FilterFromDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterFromDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND remedy_end_dt >= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("TCAB - error parsing install FROM date : "
						+ sm.Parm("FilterStartDate"));
			}
		}

		if (sm.Parm("FilterToDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterToDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND remedy_end_dt <= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("TCAB - error parsing install TO date : "
						+ sm.Parm("FilterStartDate"));
			}
		}

		// Filter Remedy status - default to no filter

		if (sm.Parm("FilterStatus").length() == 0) {

		} else {
			if (sm.Parm("FilterStatus").equalsIgnoreCase("ZZZ")) {
				sb.append(" AND (status_cd != 'CLO' AND status_cd != 'RES' ) ");
			} else {
				if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
					sb.append(" AND status_cd = '" + sm.Parm("FilterStatus")
							+ "'");
				}
			}
		}

		// Filter on review by suite ( maybe should do nc cab for rfc instaed)

		if (!this.ncf_cab) {

			if (sm.Parm("FilterReviewStatus").length() == 0) {
				sb.append(" AND review_cd = 'N'");
			} else {
				if (!sm.Parm("FilterReviewStatus").equalsIgnoreCase("0")) {
					sb.append(" AND review_cd = '"
							+ sm.Parm("FilterReviewStatus") + "'");
				}
			}
		}

		if (this.ncf_cab) {

			if (sm.Parm("FilterRoutine").length() == 0) {

			} else {
				if (!sm.Parm("FilterRoutine").equalsIgnoreCase("0")) {
					sb.append(" AND rtn_maint_cd = '"
							+ sm.Parm("FilterRoutine") + "'");
				}
			}

			if (sm.Parm("FilterOnline").length() == 0) {

			} else {
				if (!sm.Parm("FilterOnline").equalsIgnoreCase("0")) {
					sb.append(" AND on_offline_cd = '"
							+ sm.Parm("FilterOnline") + "'");
				}
			}
		}

		// 6/1 default from "New" to "Submitted"

		if (sm.Parm("FilterReviewFCAB").length() == 0) {
			if (this.ncf_cab) {
				sb.append(" AND fcab_review_cd = 'S'");
			}
		} else {
			if (!sm.Parm("FilterReviewFCAB").equalsIgnoreCase("0")) {
				sb.append(" AND fcab_review_cd = '"
						+ sm.Parm("FilterReviewFCAB") + "'");
			}
		}

		// Filter on Suite
		if (sm.Parm("FilterSuite").length() == 0) {
			if (!this.ncf_cab) {
				sb.append(" AND suite_cd = '" + sm.getUserSuite() + "'");
			}
		} else {
			if (!sm.Parm("FilterSuite").equalsIgnoreCase("0")) {
				if (sm.Parm("FilterSuite").equalsIgnoreCase("RCINGENIX"))
					// IN was old value, can be removed once prod code are
					// converted to ING
					sb.append(" AND suite_cd in ('RC','IN', 'ING')");

				else
					sb.append(" AND suite_cd = '" + sm.Parm("FilterSuite")
							+ "'");
			}
		}

		// filter review date

		if (sm.Parm("FilterSuiteDate").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterSuiteDate").equalsIgnoreCase("0")) {

				sb.append(" AND suite_review_date = '"
						+ sm.Parm("FilterSuiteDate") + "'");
			}
		}

		// filter review date
		if (sm.Parm("FilterNCFDate").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterNCFDate").equalsIgnoreCase("0")) {

				sb.append(" AND DATE(ncf_review_date) = '"
						+ sm.Parm("FilterNCFDate") + "'");

			}
		}

		// FilterRelease

		if (sm.Parm("FilterRelease").length() == 0) {

		} else {
			if (!sm.Parm("FilterRelease").equalsIgnoreCase("0")) {
				sb.append(" AND release_cd = '" + sm.Parm("FilterRelease")
						+ "'");
			}
		}

		// filter remedy_end_dt / insall date

		// Actual install dates not used.. for now

		if (false) {
			if (sm.Parm("FilterInstall").length() == 0) {
				// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
			} else {

				if (!sm.Parm("FilterInstall").equalsIgnoreCase("0")) {

					sb.append(" AND DATE(remedy_end_dt) = '"
							+ sm.Parm("FilterInstall") + "' ");
				}
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

		// save key info
		if (parmMode.equalsIgnoreCase("show")) {
			// debug("rfc plug in ... setrfcno " + db.getText("rfc_no"));

			sm.setRfcNo(db.getText("rfc_no"), db.getText("title_nm"));

			// debug("back from sm : " + sm.getRfcNo());

		}

		/*
		 * hidden fields
		 */

		int port = sm.getServerPort();
		String host = sm.getHost();
		String tomcat = sm.getTomcatName();

		ht.put("host", new WebFieldDisplay("host", host + ":" + port));

		ht.put("tomcat_name",
				new WebFieldDisplay("tomcat_name", sm.getTomcatName()));

		ht.put("reviewer",
				new WebFieldHidden("reviewer",
						((sm.userIsChgApprover()) && (!addMode)) ? "Y" : "N"));

		ht.put("oldstatus",
				new WebFieldHidden("oldstatus", addMode ? "N" : db
						.getText("suite_review_cd")));

		// add hidden field for tracking suite value
		ht.put("hiddenstatuscd",
				new WebFieldHidden("hiddenstatuscd", addMode ? sm
						.getUserSuite().toUpperCase() : db.getText("suite_cd")));
		
		ht.put("remedyno",
				new WebFieldHidden("remedyno", addMode ? "0" : db
						.getText("rfc_no")));

		ht.put("addmode", new WebFieldHidden("addmode", addMode ? "Y" : "N"));

		/*
		 * Codes
		 */

		/***********************************************************************
		 * 
		 * SOAP Interface to Remedy !
		 * 
		 */

		String[][] type_cd = new String[][] {
				{ "0", "Code", "Config", "GoLive", "Other" },
				{ "?", "Code", "Config", "Go-Live", "Other" } };

		/*
		 * links to other pages
		 */

		// don't read rs columns twice !!!
		String cabLink = new String("");
		String buildLink = new String("");

		String buildId = "0";

		String buildNo = null;

		if (addMode) {
			buildId = sm.Parm("build_id");
			buildNo = sm.Parm("build_no");
		} else {
			buildId = db.getInteger("build_id").toString();
			buildNo = db.getText("build_no");
		}

		if (parmMode.equalsIgnoreCase("show")) {
			if (buildId.equalsIgnoreCase("0")) {
				if (!buildNo.equalsIgnoreCase("0")) {
					buildLink = "(Build Tracker entry not found.)";
				}
			} else {
				buildLink = "<A href=Router?Target=Build&Action=Show&Relation=this&RowKey="
						+ buildId + ">Build Tracker</A>";
			}
		}

		if (parmMode.equalsIgnoreCase("show")) {
			// debug("review dt : " + db.getText("review_date"));

			// debug("review dt len : " + db.getText("review_date").length());

			if (this.ncf_cab) {
				cabLink = "<A href=Router?Target=Rfc&Action=Show&Relation=this&RowKey="
						+ db.getText("rfc_id") + ">SUITE CAB</A>";

			} else {

				if (db.getText("review_date").length() > 1) {

					try {
						SimpleDateFormat sdf_yyyymmdd = new SimpleDateFormat(
								"MM/dd/yyyy");
						Date rv = sdf_yyyymmdd.parse(db.getText("review_date"));
						Date cutoff = sdf_yyyymmdd.parse(ncf_cab_cutoff_date);

						// compare to 6/1/2010

						// 12/16/10 VD & AP - don't show link if FCAB status is
						// New
						if ((rv.after(cutoff))
								&& !(db.getText("fcab_review_cd")
										.equalsIgnoreCase("N"))) {
							cabLink = "<A href=Router?Target=RfcNcfCab&Action=Show&Relation=this&RowKey="
									+ db.getText("rfc_id") + ">NCF CAB</A>";
						} else {
							cabLink = "";

						}

					} catch (Exception e) {
						cabLink = "";
					}

				} else {
					cabLink = "";
				}
			}
		}

		// 6/1 Switching to universal product list..

		String qry = "select c.order_by, c.code_value, code_desc "
				+ " from tcodes c " + " where c.code_desc2 = '"
				+ sm.getUserSuite()
				+ "' and code_type_id = 121 order by order_by ";

		Hashtable products = new Hashtable();

		try {
			products = db.getLookupTable(qry);
		} catch (ServicesException e) {
		}

		ht.put("build_track_suite_cd",
				new WebFieldSelect("build_track_suite_cd", addMode ? sm
						.Parm("build_track_suite_cd") : db
						.getText("build_track_suite_cd"), products, "?"));

		ht.put("build_no", new WebFieldString("build_no", buildNo, 4, 8));
		ht.put("buildlink", new WebFieldDisplay("buildlink", buildLink));
		ht.put("cablink", new WebFieldDisplay("cablink", cabLink));

		// Put user message
		ht.put("msg", new WebFieldDisplay("msg", remedy_result));

		// ht.put("suite_cd", new WebFieldDisplay("suite_cd", addMode ? "" :
		// db.getText("suite_cd")));

		// Codes

		ht.put("type_cd",
				new WebFieldSelect("type_cd", addMode ? "" : db
						.getText("type_cd"), type_cd));

		if (addMode) {
			ht.put("suite_cd",
					new WebFieldDisplay("suite_cd", sm.getUserSuiteName()));
		} else {
			ht.put("suite_cd",
					new WebFieldSelect("suite_cd", db.getText("suite_cd"), sm
							.getCodes("SUITES"), false, false));
		}

		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? ""
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		// get Build Tracker info from tBuild if its there
		if (db.getText("driver_cd").equalsIgnoreCase("")) {
			ht.put("trigger_cd",
					new WebFieldSelect("trigger_cd", addMode ? sm
							.Parm("trigger_cd") : db.getText("trigger_cd"), sm
							.getCodesAlt("TRIGGER"), "?"));
		} else {
			ht.put("trigger_cd",
					new WebFieldDisplay("trigger_cd", db.getText("driver_cd")));
		}

		// get Build Tracker info from tBuild if its there
		// if (db.getText("driver_no").equalsIgnoreCase("0")) {

		if (true) {
			ht.put("problem_nm",
					new WebFieldString("problem_nm", (addMode ? sm
							.Parm("problem_nm") : db.getText("problem_nm")),
							16, 16));

		} else {
			ht.put("problem_nm",
					new WebFieldDisplay("problem_nm", db.getText("driver_no")));
		}

		ht.put("notify_cd",
				new WebFieldSelect("notify_cd", addMode ? "" : db
						.getText("notify_cd"), sm.getCodes("NOTIFY"), "?"));

		ht.put("all_instance_cd",
				new WebFieldSelect("all_instance_cd", addMode ? "" : db
						.getText("all_instance_cd"), sm.getCodes("YESNO")));

		ht.put("intrusive_cd", new WebFieldSelect("intrusive_cd", addMode ? ""
				: db.getText("intrusive_cd"), sm.getCodes("YESNO"), "?"));

		ht.put("rtn_maint_cd", new WebFieldSelect("rtn_maint_cd", addMode ? ""
				: db.getText("rtn_maint_cd"), sm.getCodes("YESNO"), "?"));

		/*
		 * Impacts
		 */

		ht.put("safety_cd",
				new WebFieldSelect("safety_cd", addMode ? "" : db
						.getText("safety_cd"), sm.getCodes("YESNO"), "?"));

		ht.put("asm_impact_cd",
				new WebFieldSelect("asm_impact_cd", addMode ? "" : db
						.getText("asm_impact_cd"), sm.getCodes("YESNO"), "?"));

		ht.put("x_suite_cd",
				new WebFieldSelect("x_suite_cd", addMode ? "" : db
						.getText("x_suite_cd"), sm.getCodes("YESNO"), "?"));

		ht.put("epic_only_cd", new WebFieldSelect("epic_only_cd", addMode ? ""
				: db.getText("epic_only_cd"), sm.getCodes("YESNONA")));

		ht.put("x_epic_prod_cd",
				new WebFieldSelect("x_epic_prod_cd", addMode ? "" : db
						.getText("x_epic_prod_cd"), sm.getCodes("YESNONA")));

		/*
		 * Id's
		 */

		ht.put("owner_uid",
				new WebFieldDisplay("owner_uid", addMode ? "" : db
						.getText("remedyOwner")));

		ht.put("requester_uid", new WebFieldDisplay("requester_uid",
				addMode ? "" : db.getText("remedyRequester")));

		/*
		 * don't use application filter anymore for ncf-cab, good ridance
		 */
		// ht.put("application_id", new WebFieldSelect("application_id",
		// addMode ? sm.getApplicationId() : (Integer) db
		// .getObject("application_id"),
		// sm.getApplicationFilter(), true));
		ht.put("worklog", new WebFieldString("worklog", "", 80, 255));

		/*
		 * display
		 */

		ht.put("buildstat",
				new WebFieldDisplay("buildstat", (addMode ? "" : db
						.getText("buildstatus"))));

		/*
		 * Strings
		 */

		// ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ?
		// ""
		// : db.getText("reference_nm")), 32, 32));
		ht.put("adhoc_tx",
				new WebFieldString("adhoc_tx", (addMode ? "" : db
						.getText("adhoc_tx")), 64, 128));

		ht.put("sizing_tx",
				new WebFieldString("sizing_tx", (addMode ? "" : db
						.getText("sizing_tx")), 32, 32));

		ht.put("user_notify_tx", new WebFieldString("user_notify_tx",
				(addMode ? "" : db.getText("user_notify_tx")), 32, 32));

		ht.put("chg_type_comment_tx", new WebFieldString("chg_type_comment_tx",
				(addMode ? "" : db.getText("chg_type_comment_tx")), 64, 64));

		ht.put("pat_safety_comment_tx",
				new WebFieldString("pat_safety_comment_tx", (addMode ? "" : db
						.getText("pat_safety_comment_tx")), 64, 64));

		ht.put("legacy_comment_tx", new WebFieldString("legacy_comment_tx",
				(addMode ? "" : db.getText("legacy_comment_tx")), 64, 64));

		ht.put("non_rtn_comment_tx", new WebFieldString("non_rtn_comment_tx",
				(addMode ? "" : db.getText("non_rtn_comment_tx")), 64, 64));

		ht.put("cross_suite_comment_tx",
				new WebFieldString("cross_suite_comment_tx", (addMode ? "" : db
						.getText("cross_suite_comment_tx")), 64, 64));

		ht.put("test_grp_comment_tx", new WebFieldString("test_grp_comment_tx",
				(addMode ? "" : db.getText("test_grp_comment_tx")), 64, 64));

		// ----------------- Remedy fields ---------------------------*

		// for appending text to Remedy....see the absRemedyPlugin that passes
		// this to Remedy
		ht.put("worklog", new WebFieldString("worklog", (addMode ? "" : ""),
				60, 60));


		ht.put("remedy_cat_tx", new WebFieldDisplay("remedy_cat_tx",
				(addMode ? "" : db.getText("remedy_cat_tx"))));

		ht.put("remedy_related_item_tx",
				new WebFieldDisplay("remedy_related_item_tx", (addMode ? ""
						: db.getText("remedy_related_item_tx"))));

		ht.put("remedy_start_dt",
				new WebFieldDisplay("remedy_start_dt", (addMode ? "" : db
						.getText("remedy_start_dt_tx").replace("T", " "))));

		ht.put("remedy_asof_date", new WebFieldDisplay("remedy_asof_date",
				(addMode ? "" : db.getText("remedy_asof_date"))));

		ht.put("remedy_effort_tx", new WebFieldDisplay("remedy_effort_tx",
				(addMode ? "" : db.getText("remedy_effort_tx"))));

		ht.put("remedy_owner_tx", new WebFieldDisplay("remedy_owner_tx",
				(addMode ? "" : db.getText("remedy_owner_tx"))));

		ht.put("remedy_type_tx", new WebFieldDisplay("remedy_type_tx",
				(addMode ? "" : db.getText("remedy_type_tx"))));

		ht.put("remedy_item_tx", new WebFieldDisplay("remedy_item_tx",
				(addMode ? "" : db.getText("remedy_item_tx"))));

		ht.put("remedy_grp_tx", new WebFieldDisplay("remedy_grp_tx",
				(addMode ? "" : db.getText("remedy_grp_tx"))));

		ht.put("outage_cd", new WebFieldDisplay("outage_cd", (addMode ? ""
				: YesNo(db.getText("outage_cd")))));

		ht.put("emergency_cd", new WebFieldSelect("emergency_cd", (addMode ? ""
				: db.getText("emergency_cd")), sm.getCodes("YESNO"), false,
				false));

		ht.put("remedy_approve_cd",
				new WebFieldSelect("remedy_approve_cd", (addMode ? "" : db
						.getText("remedy_approve_cd")),
						sm.getCodes("RMDYAPRV"), false, false));

		ht.put("impact_cd",
				new WebFieldSelect("impact_cd", (addMode ? "" : db
						.getText("impact_cd")), sm.getCodes("SEVERITY"), false,
						false));

		ht.put("urgency_cd", new WebFieldDisplay("urgency_cd", (addMode ? ""
				: db.getText("remedy_urgency"))));

		ht.put("status_cd",
				new WebFieldDisplay("status_cd", (addMode ? "" : db
						.getText("remedy_status"))));

		ht.put("expedited_cd", new WebFieldSelect("expedited_cd", (addMode ? ""
				: db.getText("expedited_cd")), sm.getCodes("YESNO"), false,
				false));

		// -------------- end of remedy fields -------------*

		ht.put("escalated_cd", new WebFieldSelect("escalated_cd", (addMode ? ""
				: db.getText("escalated_cd")), sm.getCodes("YESNO"), false,
				false));

		// 7/27/09 pick up rfc# in url from BT if present

		ht.put("rfc_no",
				new WebFieldString("rfc_no", (addMode ? (sm.Parm("rfc_no")
						.length() > 0 ? sm.Parm("rfc_no") : "0") : db
						.getText("rfc_no")), 8, 8));

		ht.put("release_tx", new WebFieldString("release_tx", (addMode ? ""
				: db.getText("release_tx")), 100, 250));

		/*
		 * Dates
		 */

		ht.put("install_date", new WebFieldDate("install_date", addMode ? ""
				: db.getText("install_date")));

		ht.put("created_date", new WebFieldDate("created_date", addMode ? ""
				: db.getText("created_date")));

		ht.put("request_date", new WebFieldDate("request_date", addMode ? ""
				: db.getText("request_date")));

		ht.put("required_date", new WebFieldDate("required_date", addMode ? ""
				: db.getText("required_date")));

		ht.put("remedy_end_dt", new WebFieldDisplay("remedy_end_dt",
				addMode ? "" : db.getText("fmt_remedy_end_dt")));

		ht.put("remedy_verifier_nm", new WebFieldDisplay("remedy_verifier_nm",
				addMode ? "" : db.getText("remedy_verifier_nm")));

		ht.put("remedy_start_dt", new WebFieldDisplay("remedy_start_dt",
				addMode ? "" : db.getText("fmt_remedy_start_dt")));

		ht.put("remedy_requested_completion_dt",
				new WebFieldDisplay("remedy_requested_completion_dt",
						addMode ? "" : db
								.getText("fmt_remedy_requested_completion_dt")));

		ht.put("remedy_create_dt", new WebFieldDisplay("remedy_create_dt",
				addMode ? "" : db.getText("remedy_create_dt")));

		ht.put("suite_review_date", new WebFieldDate("suite_review_date",
				addMode ? "" : db.getText("suite_review_date")));

		ht.put("review_date",
				new WebFieldDate("review_date", addMode ? "" : db
						.getText("review_date")));

		ht.put("install_hours_tx", new WebFieldString("install_hours_tx",
				addMode ? "" : db.getText("install_hours_tx"), 32, 64));

		ht.put("defect_nm",
				new WebFieldString("defect_nm", (addMode ? "" : db
						.getText("defect_nm")), 8, 8));

		ht.put("title_nm",
				new WebFieldDisplay("title_nm", (addMode ? "" : db
						.getText("title_nm"))));

		/*
		 * Secured Fields
		 */
		if (addMode && false) {

			addSecuredFields(ht, addMode, false);

			addTestorFields(ht, addMode, false);

		} else {

			// addSecuredFields(ht, addMode, sm.userIsChgApprover());

			// needs to be consistent with edit yes/no above
			addSecuredFields(ht, addMode, sm.userIsExecutive());

			addTestorFields(ht, addMode, sm.userIsTestor());
		}

		/*
		 * Blobs are last
		 */

		ht.put("ac_comment_blob", new WebFieldText("ac_comment_blob",
				addMode ? "" : db.getText("ac_comment_blob"), 3, 100));

		ht.put("release_blob", new WebFieldText("release_blob", addMode ? ""
				: db.getText("release_blob"), 3, 100));

		ht.put("user_blob",
				new WebFieldText("user_blob", addMode ? "" : db
						.getText("user_blob"), 5, 100));

		ht.put("ancillary_blob", new WebFieldText("ancillary_blob",
				addMode ? "" : db.getText("ancillary_blob"), 5, 100));

		ht.put("impact_blob",
				new WebFieldText("impact_blob", addMode ? "" : db
						.getText("impact_blob"), 5, 100));

		// make remedy-read-only
		ht.put("description_blob", new WebFieldDisplay("description_blob",
				addMode ? "" : db.getText("description_blob")));

		ht.put("remedy_planned_duration_tx",
				new WebFieldDisplay("remedy_planned_duration_tx", addMode ? ""
						: db.getText("remedy_planned_duration_tx")));

		ht.put("bsns_need_blob", new WebFieldDisplay("bsns_need_blob",
				addMode ? "" : db.getText("bsns_need_blob")));

		ht.put("worklog_blob", new WebFieldDisplay("worklog_blob",
				(addMode ? "" : db.getText("worklog_blob"))));

		return ht;
	}

	private void addSecuredFields(Hashtable<String, WebField> ht,
			boolean addMode, boolean executive)
			throws services.ServicesException {

		ht.put("boa_change_aprv_nm", new WebFieldString("boa_change_aprv_nm",
				(addMode ? "" : db.getText("boa_change_aprv_nm")), 64, 64));

		ht.put("test_approve_tx", new WebFieldString("test_approve_tx",
				(addMode ? "" : db.getText("test_approve_tx")), 64, 64));

		ht.put("boa_test_aprv_nm", new WebFieldString("boa_test_aprv_nm",
				(addMode ? "" : db.getText("boa_test_aprv_nm")), 64, 64));

		ht.put("release_cd",
				new WebFieldSelect("release_cd", db.getText("release_cd"), sm
						.getCodes("KRLSE"), "?"));

		if (executive && this.ncf_cab) {

			ht.put("tst_status_ncf_cmnt_tx",
					new WebFieldString("tst_status_ncf_cmnt_tx", (addMode ? ""
							: db.getText("tst_status_ncf_cmnt_tx")), 100, 255));

			ht.put("compliant_cd",
					new WebFieldSelect("compliant_cd", db
							.getText("compliant_cd"), sm.getCodes("YESNO"), "?"));

			ht.put("rfc_compliant_comment_tx",
					new WebFieldString("rfc_compliant_comment_tx",
							(addMode ? "" : db
									.getText("rfc_compliant_comment_tx")), 64,
							64));

			ht.put("fcab_review_cd",
					new WebFieldSelect("fcab_review_cd", db
							.getText("fcab_review_cd"), sm
							.getCodes("STATREVIEW"), "?"));

			ht.put("status_tx", new WebFieldString("status_tx", (addMode ? ""
					: db.getText("status_tx")), 64, 64));

			ht.put("interface_sign_off_cd",
					new WebFieldSelect("interface_sign_off_cd", addMode ? ""
							: db.getText("interface_sign_off_cd"), sm
							.getCodes("YESNO"), "?"));

			ht.put("security_sign_off_cd",
					new WebFieldSelect("security_sign_off_cd", addMode ? ""
							: db.getText("security_sign_off_cd"), sm
							.getCodes("YESNO"), "?"));

			ht.put("security_sign_off_tx",
					new WebFieldString("security_sign_off_tx", (addMode ? ""
							: db.getText("security_sign_off_tx")), 64, 128));

			ht.put("interface_sign_off_tx",
					new WebFieldString("interface_sign_off_tx", (addMode ? ""
							: db.getText("interface_sign_off_tx")), 64, 128));

			ht.put("final_review_cd",
					new WebFieldSelect("final_review_cd", db
							.getText("final_review_cd"),
							sm.getCodes("ENDSTAT"), "?"));

			ht.put("mcv_std_cd", new WebFieldSelect("mcv_std_cd", addMode ? ""
					: db.getText("mcv_std_cd"), sm.getCodes("YESNONA"), "?"));

			ht.put("received_date",
					new WebFieldDate("received_date", db
							.getText("received_date")));

			ht.put("color_cd",
					new WebFieldSelect("color_cd", db.getText("color_cd"), sm
							.getCodesAlt("RYG"), "None"));

			ht.put("call_seq_tx", new WebFieldString("call_seq_tx",
					(addMode ? "" : db.getText("call_seq_tx")), 6, 6));

			ht.put("fcab_decision_date", new WebFieldDate("fcab_decision_date",
					addMode ? "" : db.getText("fmt_fcab_decision_date")));

			ht.put("on_offline_cd",
					new WebFieldSelect("on_offline_cd", addMode ? "" : db
							.getText("on_offline_cd"), sm.getCodes("YESNO"),
							"?"));

			ht.put("resolution_blob",
					new WebFieldText("resolution_blob", db
							.getText("resolution_blob"), 5, 100));

		} else {

			ht.put("tst_status_ncf_cmnt_tx",
					new WebFieldDisplay("tst_status_ncf_cmnt_tx", (addMode ? ""
							: db.getText("tst_status_ncf_cmnt_tx"))));

			ht.put("compliant_cd",
					new WebFieldDisplay("compliant_cd",
							addMode ? "" : db.getText("compliant_cd")
									.equalsIgnoreCase("Y") ? "Yes" : "No"));

			ht.put("rfc_compliant_comment_tx",
					new WebFieldDisplay("rfc_compliant_comment_tx",
							(addMode ? "" : db
									.getText("rfc_compliant_comment_tx"))));

			ht.put("status_tx", new WebFieldDisplay("status_tx", (addMode ? ""
					: db.getText("status_tx"))));

			ht.put("interface_sign_off_cd",
					new WebFieldDisplay("interface_sign_off_cd", (addMode ? ""
							: YesNo(db.getText("interface_sign_off_cd")))));

			ht.put("security_sign_off_cd",
					new WebFieldDisplay("security_sign_off_cd", (addMode ? ""
							: YesNo(db.getText("security_sign_off_cd")))));

			ht.put("interface_sign_off_tx",
					new WebFieldDisplay("interface_sign_off_tx", addMode ? ""
							: db.getText("interface_sign_off_tx")));

			ht.put("security_sign_off_tx",
					new WebFieldDisplay("security_sign_off_tx", addMode ? ""
							: db.getText("security_sign_off_tx")));

			ht.put("received_date",
					new WebFieldDisplay(db.getText("received_date")));

			ht.put("compliant_cd",
					new WebFieldDisplay("compliant_cd",
							addMode ? "" : db.getText("compliant_cd")
									.equalsIgnoreCase("Y") ? "Yes" : "No"));

			ht.put("rfc_compliant_comment_tx",
					new WebFieldDisplay("rfc_compliant_comment_tx",
							(addMode ? "" : db
									.getText("rfc_compliant_comment_tx"))));

			ht.put("color_cd",
					new WebFieldSelect("color_cd", addMode ? "" : db
							.getText("color_cd"), sm.getCodesAlt("RYG"), false,
							false));

			ht.put("received_date",
					new WebFieldDisplay(db.getText("received_date")));

			ht.put("mcv_std_cd", new WebFieldDisplay("mcv_std_cd", addMode ? ""
					: db.getText("mcv_std_cd").equalsIgnoreCase("Y") ? "Yes"
							: "No"));

			ht.put("status_tx", new WebFieldDisplay("status_tx", (addMode ? ""
					: db.getText("status_tx"))));

			ht.put("call_seq_tx", new WebFieldDisplay("call_seq_tx",
					(addMode ? "" : db.getText("call_seq_tx"))));

			ht.put("fcab_decision_date", new WebFieldDisplay(
					"fcab_decision_date", db.getText("fmt_fcab_decision_date")));

			ht.put("on_offline_cd",
					new WebFieldDisplay("on_offline_cd", db
							.getText("on_offline_cd")));

			ht.put("final_review_cd",
					new WebFieldDisplay("final_review_cd", db
							.getText("fmt_final_review")));

			ht.put("resolution_blob",
					new WebFieldDisplay("resolution_blob", db
							.getText("resolution_blob")));

			String[][] limited_cab_codes = { { "N", "S" },
					{ "New", "Submitted" } };

						
			if (addMode) {
				// there wont actually be an fcab_review_cd in add mode, but
				// just in case
				ht.put("fcab_review_cd", new WebFieldDisplay("fcab_review_cd",
						""));

				ht.put("hiddenfcabstatuscd",
						new WebFieldHidden("hiddenfcabstatuscd", addMode ? ""  : "N"));
				
				
			} else {
				if (db.getText("fcab_review_cd").equalsIgnoreCase("N")) {
					ht.put("hiddenfcabstatuscd",
							new WebFieldHidden("hiddenfcabstatuscd", addMode ? ""  : "Y"));
			
					ht.put("fcab_review_cd", new WebFieldSelect(
							"fcab_review_cd", db.getText("fcab_review_cd"),
							limited_cab_codes));
				} else {
					ht.put("hiddenfcabstatuscd",
							new WebFieldHidden("hiddenfcabstatuscd", addMode ? ""  : "N"));
			
					ht.put("fcab_review_cd", new WebFieldDisplay(
							"fcab_review_cd", db.getText("fcabstatus")));

				}
			}

		}

		/*
		 * these aer only on the local suite pages
		 */
		if (executive) {

			ht.put("comment_blob",
					new WebFieldText("comment_blob",
							db.getText("comment_blob"), 5, 100));

			ht.put("suite_review_cd",
					new WebFieldSelect("suite_review_cd", db
							.getText("suite_review_cd"), sm
							.getCodes("SUITESTAT")));

			ht.put("suite_decision_date",
					new WebFieldDate("suite_decision_date", addMode ? "" : db
							.getText("fmt_suite_decision_date")));

		} else {

			ht.put("comment_blob",
					new WebFieldDisplay("comment_blob", db
							.getText("comment_blob")));

			ht.put("suite_decision_date",
					new WebFieldDisplay("suite_decision_date", db
							.getText("fmt_suite_decision_date")));

			ht.put("suite_review_cd",
					new WebFieldDisplay("suite_review_cd", db
							.getText("suitestatus")));

			/*
			 * tricky one.. if the fcab review code is "Pending" then allow AC
			 * to change it to "Submitted" only!!! Whoa
			 */

		}

		return;
	}

	private String YesNo(String code) {

		if (code == null) {
			return "";
		}
		if (code.equalsIgnoreCase("Y")) {
			return "Yes";
		} else {
			if (code.equalsIgnoreCase("N")) {
				return "No";
			}
		}
		return "";
	}

	private void addTestorFields(Hashtable<String, WebField> ht,
			boolean addMode, boolean testor) throws services.ServicesException {

		if (!addMode) {
			// debug("test group: " + db.getText("test_method_cd"));
		}

		/*
		 * obsolete i think
		 */
		if (false) {
			ht.put("test_blob",
					new WebFieldText("test_blob", addMode ? "" : db
							.getText("test_blob"), 5, 100));

			ht.put("tested_by_uid",
					new WebFieldSelect("tested_by_uid", addMode ? new Integer(
							"0") : db.getInteger("tested_by_uid"), sm
							.getTestorHT(), "-- Please Select --"));

			ht.put("test_approve_uid",
					new WebFieldSelect("test_approve_uid",
							addMode ? new Integer("0") : db
									.getInteger("test_approve_uid"), sm
									.getTestorHT(), "-- Pending Approval --"));

		}

		// 12/28/07 - Allow anyone to input testing fields
		// if (addMode
		// || testor
		// || (!addMode && db.getText("test_method_cd").equalsIgnoreCase(
		// "AC"))) {

		if (sm.userIsTestor()) {
			ht.put("testor", new WebFieldHidden("testor", "Y"));
		} else {
			ht.put("testor", new WebFieldHidden("testor", "N"));

		}

		if (false
				&& !addMode
				&& (!sm.userIsTestor() && !db.getText("test_method_cd")
						.equalsIgnoreCase("AC"))) {

			ht.put("EditTestStatus", new WebFieldHidden("EditTestStatus", "N"));

			ht.put("test_method_cd",
					new WebFieldDisplay("test_method_cd", addMode ? "" : sm
							.getCodeDescription(sm.getCodes("TESTGROUP"),
									db.getText("test_method_cd"))));

			ht.put("tested_cd",
					new WebFieldDisplay("tested_cd", addMode ? "" : sm
							.getCodeDescription(sm.getCodes("TESTSTAT"),
									db.getText("tested_cd"))));

			ht.put("tested_nm",
					new WebFieldDisplay(addMode ? "" : db.getText("tested_nm")));

		} else {

			ht.put("EditTestStatus", new WebFieldHidden("EditTestStatus", "Y"));

			ht.put("tested_cd", new WebFieldSelect("tested_cd", addMode ? ""
					: db.getText("tested_cd"), sm.getCodes("TESTSTAT"), "?"));

			ht.put("test_method_cd",
					new WebFieldSelect("test_method_cd", addMode ? "" : db
							.getText("test_method_cd"), sm
							.getCodes("TESTGROUP"), "?"));

			ht.put("tested_nm", new WebFieldString("tested_nm", (addMode ? ""
					: db.getText("tested_nm")), 32, 32));

		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see plugins.AbsRemedyPlugin#beforeUpdate(java.util.Hashtable)
	 * 
	 * important - force in the user's suite !!!
	 */
	public boolean beforeAdd(Hashtable ht) {

		/*
		 * check for duplicate
		 */

		String sql = "SELECT rfc_id, rfc_no  from trfc where rfc_no = "
				+ sm.Parm("rfc_no");

		try {

			ResultSet rs = db.getRS(sql);

			if (rs.next()) {
				remedy_result = "This RFC " + sm.Parm("rfc_no")
						+ " number already exists. Here it is.";
				this.rowId = rs.getInt("rfc_id"); // java 1.5
				return false;
			}

		} catch (services.ServicesException e) {
			debug("ServiceRequestPlugin: SQL error on SR number check "
					+ e.toString());

		} catch (java.sql.SQLException se) {
			debug("ServiceRequestPlugin: SQL error on SR Number check "
					+ se.toString());
		}

		try {
			ht.put("suite_cd", new DbFieldString("suite_cd", sm.getUserSuite()));

		} catch (Exception e) {
			debug("RFCPlugin: error inserting user suite into add fields."
					+ e.toString());
			return true;
		}
		return true;
	}

	// create Excel from ResultSet and save to the path in the web.xml config
	// file
	public String makeExcelFile() {
		
		String excelFileName = "";

		try {

			ExcelWriter excel = new ExcelWriter();

			String templatePath = sm.getWebRoot() + "excel/" + excelTemplate;

			String filePrefix = sm.getLastName() + "_" + "_CAB";
			int columns = 25;
			short startRow = 1;

			if (this.ncf_cab) {
				String templateName = "rfc_ncf_cab.xls";
				this.excelView = "vrfc_excel_fcab";
				templatePath = sm.getWebRoot() + "excel/" + templateName;
				filePrefix = sm.getLastName() + "_NCF_CAB";
				columns = 28; //  1/24/11 drop off 1 column
			}
			
			ResultSet rs = null;

			try {
				debug("Excel query : " + getExcelQuery());
				rs = db.getRS(getExcelQuery());
			} catch (services.ServicesException e) {
				debug("Excel RS Fetch Error : " + e.toString());
				return "Error fetching Excel data.";
			}

			excelFileName = excel.appendWorkbook(sm.getExcelPath(),
					templatePath, filePrefix, rs, startRow,
					columns);
			
			this.remedy_result = "<a href=reports/" + excelFileName + ">"
			+ excelFileName + "</a>";
			

		} catch (Exception e) {
			debug("excel excetpion: " + e.toString());
		}
		return excelFileName;
	}

	public String getExcelQuery() {

		
		// for making a single-row export off the detail page !
		
		if (mode.equalsIgnoreCase("show"))
			return ("SELECT * FROM " + excelView + " where " +   keyName + " = " + sm.Parm("RowKey") );
		

		StringBuffer sb = new StringBuffer();
		
		sb.append("SELECT * FROM " + excelView + " WHERE 1=1 ");

		// Filter Remedy status - default to no filter

		// filter Remedy group
		if ((!sm.Parm("FilterGroup").equalsIgnoreCase("0"))) {
			sb.append(" AND remedy_grp_cd = '" + sm.Parm("FilterGroup") + "'");
		}

		// Filter on Suite

		if ((sm.Parm("FilterSuite").length() > 0)) {
			if (!sm.Parm("FilterSuite").equalsIgnoreCase("0")) {
				sb.append(" AND suite_cd = '" + sm.Parm("FilterSuite") + "'");
			}
		}

		// filter Remedy status
		if (sm.Parm("FilterStatus").length() > 0) {
			if (sm.Parm("FilterStatus").equalsIgnoreCase("ZZZ")) {
				sb.append(" AND (status_cd != 'RES' AND status_cd != 'CLO' )");
			} else {
				if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {
					sb.append(" AND status_cd  = '" + sm.Parm("FilterStatus")
							+ "'");
				}
			}
		}

		// filter Suite status
		if ((!sm.Parm("FilterReviewStatus").equalsIgnoreCase("0"))
				&& (sm.Parm("FilterReviewStatus").length() != 0)) {
			sb.append(" AND review_cd  = '" + sm.Parm("FilterReviewStatus")
					+ "'");
		}

		if (this.ncf_cab) {

			if (sm.Parm("FilterRoutine").length() == 0) {

			} else {
				if (!sm.Parm("FilterRoutine").equalsIgnoreCase("0")) {
					sb.append(" AND rtn_maint_cd = '"
							+ sm.Parm("FilterRoutine") + "'");
				}
			}

			if (sm.Parm("FilterOnline").length() == 0) {

			} else {
				if (!sm.Parm("FilterOnline").equalsIgnoreCase("0")) {
					sb.append(" AND on_offline_cd = '"
							+ sm.Parm("FilterOnline") + "'");
				}
			}
		}

		// filter Suite status
		if ( this.ncf_cab) {

			if ((!sm.Parm("FilterReviewFCAB").equalsIgnoreCase("0"))
					&& (sm.Parm("FilterReviewFCAB").length() != 0)) {
				sb.append(" AND fcab_review_cd  = '"
						+ sm.Parm("FilterReviewFCAB") + "'");
			}
		}

		// filter review date
		if (sm.Parm("FilterSuiteDate").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterSuiteDate").equalsIgnoreCase("0")) {

				sb.append(" AND suite_review_date = '"
						+ sm.Parm("FilterSuiteDate") + "'");
			}
		}

		// filter review date
		if (sm.Parm("FilterNCFDate").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterNCFDate").equalsIgnoreCase("0")) {

				sb.append(" AND DATE(review_date) = '"
						+ sm.Parm("FilterNCFDate") + "'");

			}
		}

		if (sm.Parm("FilterInstall").length() > 0) {
			if (!sm.Parm("FilterInstall").equalsIgnoreCase("0")) {

				sb.append(" AND DATE(remedy_end_dt) = '"
						+ sm.Parm("FilterInstall") + "'");
			}
		}

		if (sm.Parm("FilterRelease").length() == 0) {
			// sb.append(" AND review_date >= dateadd(d, -3, getdate()) ");
		} else {

			if (!sm.Parm("FilterRelease").equalsIgnoreCase("0")) {

				sb.append(" AND release_cd = '" + sm.Parm("FilterRelease")
						+ "'");
			}
		}

		SimpleDateFormat sdf_from_browser = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat sdf_to_query = new SimpleDateFormat("yyyy/MM/dd");

		if (sm.Parm("FilterFromDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterFromDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND remedy_end_dt >= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("TCAB - error parsing install FROM date : "
						+ sm.Parm("FilterStartDate"));
			}
		}

		if (sm.Parm("FilterToDate").length() > 0) {

			try {
				Date d = sdf_from_browser.parse(sm.Parm("FilterToDate"));
				String dateQuery = sdf_to_query.format(d);

				sb.append(" AND remedy_end_dt <= '" + dateQuery + "'");

			} catch (ParseException e1) {
				debug("TCAB - error parsing install TO date : "
						+ sm.Parm("FilterStartDate"));
			}
		}

		// if ((sm.Parm("FilterSortAdded").equalsIgnoreCase("DATE") || sm.Parm(
		// "FilterSortAdded").equalsIgnoreCase(""))
		// && (!this.ncf_cab))
		// sb.append(" ORDER BY added_date, rfcno");
		// e/lse

		if (!this.ncf_cab) {
			sb.append(" ORDER BY suite_review_date, rfc_no");
		} else {
			if (sm.Parm("FilterCallSort").equalsIgnoreCase("Y"))
				sb.append(" ORDER BY call_seq_tx, remedy_end_dt, rfc_no");

			else
				sb.append(" ORDER BY rfc_no");
		}

		
		return sb.toString();

	}
}
