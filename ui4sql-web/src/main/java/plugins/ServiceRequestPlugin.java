/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.util.Hashtable;
import db.DbField;
import db.DbFieldString;
import router.SessionMgr;
import forms.BeanFieldSelect;
import forms.BeanWebField;
import forms.WebField;
import forms.WebFieldDate;
import forms.WebFieldDisplay;
import forms.WebFieldHidden;
import forms.WebFieldSelect;
import forms.WebFieldString;
import forms.WebFieldText;

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
 * 9/29/10 Add Excel Export
 * 
 * 9/30/10 Paulsen - Add SR # in BT Child Title
 * 
 * 10/1/10 Paulsen - fix the filter for 'RCTS' to show contacts from
 * tremedy.requester_uid
 * 
 * 10/12 Vidya and Arne add logic in beforeAdd() to check for duplicate sr_no
 * use the vsr to fetch, instead of tremedy
 * 
 * 12/8 Vidya and Arne Add logic to getRemedy info after adding new SR this is
 * run whether user selected "Save" or "Save and Edit" Add logic to clean up
 * tRemedy table after deleting an SR Rename hidden field user_type to usertype,
 * so framework doesn't try up date database (form variables with underscores
 * are assumed to be table columns)
 * 
 * 
 * 1/6/2011 - Fixed status_custom_filter to match on view column
 * 'openClosedStatus = 'OPEN' "
 * 
 * 2/2/2010 = Add "Customer Request Date" to the Remedy Display
 * 
 * 2/14/11 add qualifier for remedy_id > 1 to prevent SQL errors on delete
 * 
 * SR Status Codes Table moved to new id
 */

public class ServiceRequestPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	// private static String status_custom_filter =
	// "OpenClosedStatus in (select distinct code_desc2 from tcodes where code_type_id = 166 ) ";

	private static String status_custom_filter = "OpenClosedStatus = 'OPEN' ";

	private boolean ac_ok;
	private boolean bp_ok;

	private Integer tRemedyKey;

	private String user_message = "Okay.";

	// the spelling doesn't matter on the filter column, bcz it gets replaced
	// with the Selector Box!
	private static String[] sListHeaders = {
			"SR#",
			"Summary",
			"Remedy&nbsp;Description&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			"Office", "Status", "Release", "Team-Project", "Product",
			"Build Leader", "AC", "HC PM", "Sponsor", "Requestor",
			"INTEGRATED-pm", "Validator", "Routine", "Funded", "Comm",
			"Workfow" };

	private String leader_sql = "select concat(last_name, ',',first_name) as odor, cast(user_id "
			+ " as signed "
			+ " ) as user_id, concat(last_name, ',',first_name) as user_name from tuser where leader_flag = 'Y' AND user_suite_cd = 'RC'";

	private String pm_sql = "select concat(last_name, ',',first_name) as odor, cast(user_id "
			+ " as signed "
			+ " ) as user_id, concat(last_name, ',',first_name) as user_name from tuser where type_cd = 'PM' AND user_suite_cd = 'RC'";

	// ap 12/20 allow a psuedo status of all-open
	// SR Status Codes Table moved to new id
	
	// code type was 166 but it did't carry forward in old sql restore
	private String sr_status_sql = " SELECT 0 as order_by, 'ALLOPEN' , '--All Open--', 'x' UNION SELECT order_by, code_value, code_desc, 'x' from tcodes where code_type_id = 168 order by 4";

	private String[][] office = new String[][] { { "F", "B" },
			{ "Front", "Back" } };

	// VD added Routine by Exception
	private String[][] routine = new String[][] { { "R", "N", "E" },
			{ "RTM", "NonRTM", "EXCP" } };

	private BeanFieldSelect filterOffice = new BeanFieldSelect(3,
			"FilterOffice", "back_office_cd", "", "", "Office?", "");

	private BeanFieldSelect filterStatus = new BeanFieldSelect(4,
			"FilterStatus", "sr_status_cd", "ALLOPEN", "ALLOPEN", "Status?",
			"SQL", sr_status_sql);

	private BeanFieldSelect filterRelease = new BeanFieldSelect(5,
			"FilterRelease", "release_cd", "", "", "Build Month?", "SRRLSE");

	private BeanFieldSelect filterTeam = new BeanFieldSelect(6, "FilterTeam",
			"team_cd", "", "", "Project?", "TEAM");

	private BeanFieldSelect filterProduct = new BeanFieldSelect(7,
			"FilterProduct", "product_cd", "", "", "KPHC Team?", "PRODUCTS");

	private BeanFieldSelect filterBuildLead = new BeanFieldSelect(8,
			"FilterBuildLead", "build_lead_uid", new Integer("0"), new Integer(
					"0"), "Build Lead?", "SQL", leader_sql);

	private BeanFieldSelect filterAC = new BeanFieldSelect(
			9,
			"FilterAC",
			"hc_ac_uid",
			new Integer("0"),
			new Integer("0"),
			"KPHC AC?",
			"SQL",
			"select distinct concat(u.last_name, ', ', u.first_name) as a , u.user_id as b, concat(u.last_name, ', ',u.first_name) as c from tsr join tuser u on tsr.hc_ac_uid = u.user_id");

	private BeanFieldSelect filterPM = new BeanFieldSelect(
			10,
			"FilterPM",
			"pm_uid",
			new Integer("0"),
			new Integer("0"),
			"KPHC PM?",
			"SQL",
			"select distinct concat(u.last_name, ', ', u.first_name) as a , u.user_id as b, concat(u.last_name, ', ',u.first_name) as c from tsr join tuser u on tsr.pm_uid = u.user_id");

	private BeanFieldSelect filterSponsor = new BeanFieldSelect(
			11,
			"FilterSponsor",
			"sponsor_uid",
			new Integer("0"),
			new Integer("0"),
			"Sponsor?",
			"SQL",
			"select distinct concat(u.last_name, ', ', u.first_name) as a , u.user_id as b, concat(u.last_name, ', ',u.first_name) as c from tsr join tuser u on tsr.sponsor_uid = u.user_id");

	private BeanFieldSelect filterRequestor = new BeanFieldSelect(
			12,
			"FilterRequestor",
			"requester_uid",
			new Integer("0"),
			new Integer("0"),
			"Requestor?",
			"SQL",
			"select distinct concat(u.last_name, ', ', u.first_name) as a , u.contact_id as b, concat(u.last_name, ', ',u.first_name) as c from tremedy join tcontact u on tremedy.requester_uid = u.contact_id");

	private BeanFieldSelect filterPM2 = new BeanFieldSelect(
			13,
			"FilterPM2",
			"pm2_uid",
			new Integer("0"),
			new Integer("0"),
			"Integrated PM?",
			"SQL",
			"select distinct concat(u.last_name, ', ', u.first_name) as a , u.user_id as b, concat(u.last_name, ', ',u.first_name) as c from tsr join tuser u on tsr.pm2_uid = u.user_id");

	private BeanFieldSelect filterValidator = new BeanFieldSelect(
			14,
			"FilterValidator",
			"validator_uid",
			new Integer("0"),
			new Integer("0"),
			"Validator?",
			"SQL",
			"select distinct concat(u.last_name, ', ', u.first_name) as a , u.user_id as b, concat(u.last_name, ', ',u.first_name) as c from tsr join tuser u on tsr.validator_uid = u.user_id");

	private BeanFieldSelect filterRoutine = new BeanFieldSelect(15,
			"FilterRoutine", "rtn_maint_cd", "", "", "Routine?", "");

	private BeanFieldSelect filterFunding = new BeanFieldSelect(16,
			"FilterFunding", "funding_type_cd", "", "", "Funding?", "FUNDING");

	private BeanFieldSelect filterNotify = new BeanFieldSelect(17,
			"FilterNotify", "notify_cd", "", "", "Communication?", "YESNO");

	private BeanFieldSelect filterWorkflow = new BeanFieldSelect(18,
			"FilterWorkflow", "workflow_cd", "", "", "Workflow?", "YESNO");

	public ServiceRequestPlugin() throws services.ServicesException {
		super();

		this.setTableName("tsr");
		this.setKeyName("sr_id");
		this.setListOrder("sr_no");

		// this.remedyType = "SR";
		// this.remedyKey = "sr_no";

		this.setExcelOk(true);
		this.setExcelTemplate("vsr_excel", "sr.xls", 1, 45);

		this.setListHeaders(sListHeaders);
		this.setSubmitOk(false);
		this.setNextOk(false);
		this.setListViewName("vsr_list");
		this.setSelectViewName("vsr");
		this.setListPageCenterButtons(true);
		this.setRemedyOk(true);

		// goto shortcut:
		this.setGotoOk(true);
		this.setGotoDisplayName("SR #: ");
		this.setGotoKeyName("sr_no");

		// link to child BT
		setHasDetailForm(true);
		setTargetTitle("Service Requests");
		setDetailTarget("ServiceRequestChild");
		setDetailTargetLabel("BT's");

	}

	public void init(SessionMgr parmSm) {
		super.init(parmSm);

		filterOffice.setChoiceArray(office);

		filterRoutine.setChoiceArray(routine);

		// filterTeam.setPleaseSelect(false);

		bp_ok = sm.getSR_Tracker_Level().equalsIgnoreCase("BP")
				|| sm.getSR_Tracker_Level().equalsIgnoreCase("ED")
				|| sm.userIsLeader() || sm.userIsAdministrator();

		ac_ok = sm.getSR_Tracker_Level().equalsIgnoreCase("AC")
				|| sm.getSR_Tracker_Level().equalsIgnoreCase("ED")
				|| sm.userIsLeader() || sm.userIsAdministrator();

		if (!ac_ok) {
			this.setAddOk(false);
			this.setCopyOk(false);
			// / this.setRemedyOk(false); AP&VD 12/16/10 Leave Remedy button on
			// always
		}

		// don't allow any edit if not an AC or BP or Leader or Administrator
		if ((!ac_ok) && (!bp_ok)) {
			this.setEditOk(false);
		}

		if ((!sm.userIsLeader()) && (!sm.userIsAdministrator())) {
			this.setDeleteOk(false);
		}

		// AP 12/19/10 allow a psuedo status of 'all open'

		if (sm.Parm("FilterStatus").equalsIgnoreCase("ALLOPEN")
				|| (sm.Parm("FilterStatus").length() < 1)) {
			filterStatus.setCustomFilter(status_custom_filter);
		}
		this.setListFilters(new BeanWebField[] { filterSponsor, filterOffice,
				filterStatus, filterTeam, filterProduct, filterWorkflow,
				filterAC, filterPM, filterRequestor, filterPM2, filterRoutine,
				filterFunding, filterNotify, filterRelease, filterBuildLead,
				filterValidator });

	}

	// need to override because it is using the 'tremedy' table, not the plugin
	// target table

	public String getRemedy() {

		this.remedy_result = getRemedy("tremedy", "remedy_id",
				Integer.parseInt(sm.Parm("remedyno")),
				Integer.parseInt(sm.Parm("remedyid")));

		/*
		 * 3/22/11 Update build month from Remedy plan_end_date
		 * 
		 */
		
		String sql = "update tsr set release_cd = (select concat(year(remedy_end_dt), if (month(remedy_end_dt)<10,'0','') , month(remedy_end_dt)) from tremedy where remedy_no =" +
				sm.Parm("remedyno") + ") where sr_id = " +  sm.Parm("RowKey");
				
		try {
			db.runQuery(sql);
		}
		catch (Exception e) {
			debug("Error on Build-Month update from Remedy");
		}
		
		return remedy_result;
	}

	/*
	 * Allow GoTo on the List page
	 */
	public boolean gotoOk() {
		return true;
	}

	public String gotoDisplayName() {
		return "SR #: ";
	}

	public String gotoKeyName() {
		return "sr_no";
	}

	/***
	 * /**
	 * *************************************************************************
	 * * * HTML Field Mapping *
	 * *************************************************************************
	 */

	public void afterAdd(Integer rowKey) throws services.ServicesException {

		this.remedy_result = getRemedy("tremedy", "remedy_id",
				Integer.parseInt(sm.Parm("sr_no")), tRemedyKey);

		return;
	}

	/*
	 * 12/8/10 clean up tremedy table after deleting tsr (non-Javadoc)
	 * 2/14/11 add qualifier for remedy_id > 1 to prevent SQL errors on delete
	 * 
	 * @see plugins.Plugin#afterDelete(java.lang.Integer)
	 */
	public void afterDelete(Integer rowKey) throws services.ServicesException {

		// the rowKey above is key to tsr table, not tremedy table!

		String sql = "DELETE FROM tremedy where remedy_no = "
				+ sm.Parm("remedyno") + "  AND remedy_id > 1 LIMIT 5";
		try {
			db.runQuery(sql);
		} catch (Exception e) {
			System.out.println("Error deleteing tremedy after tsr");
		}
		return;
	}

	public boolean beforeAdd(Hashtable<String, DbField> ht) {

		/*
		 * check for duplicate
		 */

		String sql = "SELECT sr_id, sr_no  from vsr where sr_no ="
				+ sm.Parm("sr_no");

		try {

			ResultSet rs = db.getRS(sql);

			if (rs.next()) {
				user_message = "This SR " + sm.Parm("sr_no")
						+ " number already exists.";
				this.rowId = rs.getInt("sr_id"); // java 1.5
				return false;
			} else {

				sql = "insert into tremedy (remedy_no) values("
						+ sm.Parm("sr_no") + ")";

				try {
					db.runQuery(sql);

				} catch (Exception e) {
					System.out.println("Error inserting tRemedy row.");
				}
				try {
					tRemedyKey = db.getNewRowKey();
				} catch (Exception e) {
					System.out.println("Error fetching remedy key");
				}
			}

		} catch (services.ServicesException e) {
			debug("ServiceRequestPlugin: SQL error on SR number check "
					+ e.toString());

		} catch (java.sql.SQLException se) {
			debug("ServiceRequestPlugin: SQL error on SR Number check "
					+ se.toString());
		}

		// default suite_cd to RC - Revenue Capture

		ht.put("suite_cd", new DbFieldString("suite_cd", "RC"));

		return true;
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

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = (parmMode.equalsIgnoreCase("add") || getDataFormName()
				.equalsIgnoreCase("Add")) ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		if (parmMode.equalsIgnoreCase("show")) {
			sm.setParentId(db.getInteger("sr_id"), db.getText("sr_no")
					+ "&nbsp;&nbsp;&nbsp;" + db.getText("title_nm"));
		}

		/*
		 * SR Security Levels
		 */

		if (addMode) {

			ht.put("msg1", new WebFieldDisplay("msg1", user_message));
		} else {
			ht.put("msg1", new WebFieldDisplay("msg1", user_message));
		}

		System.out.println("at 1");
		
		// save the tremedy.remedy_id
		ht.put("remedyid",
				new WebFieldHidden("remedyid", addMode ? "0" : db
						.getText("remedy_id")));

		ht.put("usertype", new WebFieldHidden("usertype", ac_ok ? "ED" : "VW"));

		// TODO.. FIX THIS.
		ht.put("remedy_item_cd", new WebFieldDisplay("remedy_item_cd", ""));

		ht.put("prefix", new WebFieldString("prefix", "", 12, 12));

		ht.put("tieline", new WebFieldString("tieline", "", 12, 12));

		ht.put("floor", new WebFieldString("floor", "", 12, 12));

		ht.put("remedyno",
				new WebFieldHidden("remedyno", addMode ? "0" : db
						.getText("sr_no")));

		ht.put("mode", new WebFieldHidden("mode", parmMode));

		/***********************************************************************
		 * 
		 * SOAP Interface to Remedy !
		 * 
		 */
		
		
		System.out.println("at 2");

		// Put user message
		ht.put("msg", new WebFieldDisplay("msg", remedy_result));

		String[][] sr_type = new String[][] { { "B", "T", "O" },
				{ "Business", "Technical", "Other" } };

		System.out.println("at BACK OFFICE");
				
		ht.put("back_office_cd", new WebFieldSelect("back_office_cd",
				addMode ? "" : db.getText("back_office_cd"), office, true,
				ac_ok));

		ht.put("notify_cd",
				new WebFieldSelect("notify_cd", addMode ? "" : db
						.getText("notify_cd"), sm.getCodes("YESNO"), true,
						ac_ok));

		System.out.println("at  FUNDING CD");
		ht.put("funding_type_cd",
				new WebFieldSelect("funding_type_cd", addMode ? "" : db
						.getText("funding_type_cd"), sm.getCodes("FUNDING"),
						true, ac_ok));

		System.out.println("at sr type");
		ht.put("sr_type_cd",
				new WebFieldSelect("sr_type_cd", addMode ? "" : db
						.getText("sr_type_cd"), sr_type, true, ac_ok));

		
		System.out.println("at method");
		
		ht.put("test_method_cd",
				new WebFieldSelect("test_method_cd", addMode ? "" : db
						.getText("test_method_cd"), sm.getCodes("TESTGROUP"),
						true, ac_ok));

		System.out.println("at tested");
		ht.put("tested_cd",
				new WebFieldSelect("tested_cd", addMode ? "" : db
						.getText("tested_cd"), sm.getCodes("TESTSTAT"), true,
						ac_ok));

		ht.put("workflow_cd", new WebFieldSelect("workflow_cd", addMode ? ""
				: db.getText("workflow_cd"), sm.getCodes("YESNO"), true, ac_ok));

		ht.put("funded_cd",
				new WebFieldSelect("funded_cd", addMode ? "" : db
						.getText("funded_cd"), sm.getCodes("YESNO"), true,
						ac_ok));

		ht.put("bsns_init_flag",
				new WebFieldSelect("bsns_init_flag", addMode ? "" : db
						.getText("bsns_init_flag"), sm.getCodes("YESNO"), true,
						ac_ok));

		// todo why here twice
		//ht.put("tested_cd",
			//	new WebFieldSelect("tested_cd", addMode ? "" : db
				//		.getText("tested_cd"), sm.getCodes("TESTSTAT"), true,
					//	ac_ok));

		ht.put("sizing_cd",
				new WebFieldSelect("sizing_cd", addMode ? "" : db
						.getText("sizing_cd"), sm.getCodesAlt("HIGHMEDLOW"),
						true, ac_ok));

		ht.put("rtn_maint_cd", new WebFieldSelect("rtn_maint_cd", addMode ? ""
				: db.getText("rtn_maint_cd"), routine, true, ac_ok));

		/*
		 * Codes
		 */
		
		System.out.println("at prod_cd");

		ht.put("prod_cd",
				new WebFieldSelect("prod_cd", (addMode ? "" : db
						.getText("prod_cd")), sm.getCodes("YESNO"), true, ac_ok));

		System.out.println("at sr_status ");
		
		ht.put("sr_status_cd", new WebFieldSelect("sr_status_cd", (addMode ? ""
				: db.getText("sr_status_cd")), sm.getCodes("STATUSSR"), true,
				ac_ok));

		System.out.println("at suite ");
		ht.put("suite_cd", new WebFieldDisplay("suite_cd", "Revenue Capture"));


		// 2/9/11 only get active codes on new sr
		
		System.out.println("at  release");
		
		ht.put("release_cd", new WebFieldDisplay("release_cd", db.getText("BuildMonth")));
		
		//		new WebFieldSelect("release_cd", addMode ? "" : db
		//				.getText("release_cd"), addMode ? sm
		//				.getActiveCodes("SRRLSE") : sm.getCodes("SRRLSE"), true,
		//				ac_ok));

		
		System.out.println("at remedy effort");
		ht.put("remedy_effort_tx",
				new WebFieldDisplay("remedy_effort_tx", (addMode ? "" : db
						.getText("remedy_effort_tx"))));

		System.out.println("at sox attr");
		
		ht.put("sox_attribute_tx",
				new WebFieldDisplay("sox_attribute_tx", (addMode ? "" : db
						.getText("sox_attribute_tx"))));
		
		System.out.println("at customer req dt");
		
		ht.put("customer_rqst_date_tx",
				new WebFieldDisplay("customer_rqst_date_tx", (addMode ? "" : db
						.getText("customer_rqst_date_tx"))));

		System.out.println("at remedy comment blob");
		
		ht.put("remedy_comment_blob",
				new WebFieldDisplay("remedy_comment_blob", (addMode ? "" : db
						.getText("remedy_comment_blob"))));

		System.out.println("at  ac uid");
		ht.put("hc_ac_uid",
				new WebFieldSelect("hc_ac_uid", addMode ? new Integer("0") : db
						.getInteger("hc_ac_uid"), sm.getUserHT(), true, ac_ok));

		System.out.println("at sponsor uid ");
		ht.put("sponsor_uid",
				new WebFieldSelect("sponsor_uid", addMode ? new Integer("0")
						: db.getInteger("sponsor_uid"), sm.getUserHT(), true,
						bp_ok));

		ht.put("build_lead_uid", new WebFieldSelect("build_lead_uid",
				addMode ? new Integer("0") : db.getInteger("build_lead_uid"),
				sm.getServletTable("srleaders", leader_sql), true, ac_ok));

		ht.put("builder_owner_uid",
				new WebFieldSelect("builder_owner_uid", addMode ? new Integer(
						"0") : db.getInteger("builder_owner_uid"), sm
						.getLeaderHT(), true, ac_ok));

		ht.put("pm_uid",
				new WebFieldSelect("pm_uid", addMode ? new Integer("0") : db
						.getInteger("pm_uid"), sm.getServletTable("progmgrs",
						pm_sql), true, ac_ok));

		/*
		 * BP-Only Fields
		 */

		System.out.println("at BP ONLY FIELDS");
		
		ht.put("validator_uid",
				new WebFieldSelect("validator_uid", addMode ? new Integer("0")
						: db.getInteger("validator_uid"), sm.getUserHT(), true,
						bp_ok));

		ht.put("pm2_uid", new WebFieldSelect("pm2_uid", addMode ? new Integer(
				"0") : db.getInteger("pm2_uid"), sm.getUserHT(), true, bp_ok));

		if (bp_ok) {

			ht.put("tech_notes_blob", new WebFieldText("tech_notes_blob",
					(addMode ? "" : db.getText("tech_notes_blob")), 4, 80));

		} else {

			ht.put("tech_notes_blob", new WebFieldDisplay("tech_notes_blob",
					addMode ? "" : db.getText("tech_notes_blob")));

		}

		
		System.out.println("at TEAM CD");
		ht.put("team_cd",
				new WebFieldSelect("team_cd", addMode ? "" : db
						.getText("team_cd"), sm.getCodes("TEAM"), true, ac_ok));

		/*
		 * 9/30 Protect changing product/team value to only team leads if not
		 * team leader, then display it, not edit if team lead, allow changing
		 * to any value regardless of user suite
		 * 
		 * 2/14/11, this field has unique parameters to allow editing, so
		 * the html page needs to know if the field is editable in order
		 * to know if it should check for empty or 'please select' non-entry
		 * 
		 */
		
		
		System.out.println("at IS LEADER ");
		
		if (sm.userIsLeader()) {
			ht.put("productedit", new WebFieldHidden("productedit", "Y"));

			ht.put("product_cd",
					new WebFieldSelect("product_cd", addMode ? sm
							.getUserProduct() : db.getText("product_cd"),
							addMode ? sm.getSubCodes("PRODUCTS",
									sm.getUserSuite()) : sm
									.getCodes("PRODUCTS"), "?"));
		}

		else {
			if (addMode) {
				ht.put("productedit", new WebFieldHidden("productedit", "Y"));
				ht.put("product_cd",
						new WebFieldSelect("product_cd", addMode ? sm
								.getUserProduct() : db.getText("product_cd"),
								sm.getSubCodes("PRODUCTS", sm.getUserSuite()),
								"?"));
			} else {
				ht.put("productedit", new WebFieldHidden("productedit", "N"));
				ht.put("product_cd", new WebFieldDisplay("product_cd",
						addMode ? "" : db.getText("product_disp")));
			}

		}

		ht.put("remedy_requester", new WebFieldDisplay("remedy_requester",
				addMode ? "" : db.getText("remedy_requester")));
		/*
		 * Strings
		 */

		if (addMode) {
			ht.put("sr_no", new WebFieldString("sr_no", "", 8, 8));
		} else {
			ht.put("sr_no", new WebFieldDisplay("sr_no", db.getText("sr_no")));
		}

		if (ac_ok) {

			ht.put("sr_title_nm", new WebFieldString("sr_title_nm",
					(addMode ? "" : db.getText("sr_title_nm")), 100, 128));

			ht.put("reference_nm", new WebFieldString("reference_nm",
					(addMode ? "" : db.getText("reference_nm")), 32, 32));

			ht.put("off_cycle_rsn_tx", new WebFieldString("off_cycle_rsn_tx",
					(addMode ? "" : db.getText("off_cycle_rsn_tx")), 100, 255));

			ht.put("cmnt_comm_tx", new WebFieldString("cmnt_comm_tx",
					(addMode ? "" : db.getText("cmnt_comm_tx")), 100, 255));
			ht.put("cmnt_loe_tx", new WebFieldString("cmnt_loe_tx",
					(addMode ? "" : db.getText("cmnt_loe_tx")), 100, 255));
			ht.put("cmnt_funding_tx", new WebFieldString("cmnt_funding_tx",
					(addMode ? "" : db.getText("cmnt_funding_tx")), 100, 255));
			ht.put("cmnt_proj_tx", new WebFieldString("cmnt_proj_tx",
					(addMode ? "" : db.getText("cmnt_proj_tx")), 100, 255));

			ht.put("comment_blob", new WebFieldText("comment_blob",
					(addMode ? "" : db.getText("comment_blob")), 4, 80));

			ht.put("audit_blob", new WebFieldText("audit_blob", (addMode ? ""
					: db.getText("audit_blob")), 4, 80));

		}

		else {
			ht.put("sr_title_nm", new WebFieldDisplay("sr_title_nm",
					(addMode ? "" : db.getText("sr_title_nm"))));

			ht.put("reference_nm", new WebFieldDisplay("reference_nm",
					(addMode ? "" : db.getText("reference_nm"))));

			ht.put("off_cycle_rsn_tx", new WebFieldDisplay("off_cycle_rsn_tx",
					(addMode ? "" : db.getText("off_cycle_rsn_tx"))));

			ht.put("cmnt_comm_tx", new WebFieldDisplay("cmnt_comm_tx",
					(addMode ? "" : db.getText("cmnt_comm_tx"))));

			ht.put("cmnt_loe_tx", new WebFieldDisplay("cmnt_loe_tx",
					(addMode ? "" : db.getText("cmnt_loe_tx"))));

			ht.put("cmnt_funding_tx", new WebFieldDisplay("cmnt_funding_tx",
					(addMode ? "" : db.getText("cmnt_funding_tx"))));

			ht.put("cmnt_proj_tx", new WebFieldDisplay("cmnt_proj_tx",
					(addMode ? "" : db.getText("cmnt_proj_tx"))));

			ht.put("comment_blob", new WebFieldDisplay("comment_blob",
					(addMode ? "" : db.getText("comment_blob"))));

			ht.put("audit_blob", new WebFieldDisplay("audit_blob",
					(addMode ? "" : db.getText("audit_blob"))));

		}

		// //VD added Start

		ht.put("baod_reqr_cd",
				new WebFieldSelect("baod_reqr_cd", addMode ? "" : db
						.getText("baod_reqr_cd"), sm.getCodes("YESNO"), true,
						ac_ok));

		ht.put("remedy_start_dt", new WebFieldDisplay("remedy_start_dt",
				(addMode ? "" : db.getText("fmt_remedy_start_dt"))));

		ht.put("remedy_end_dt", new WebFieldDisplay("remedy_end_dt",
				addMode ? "" : db.getText("fmt_remedy_end_dt")));

		ht.put("end_dt",
				new WebFieldDisplay("end_dt", addMode ? "" : db
						.getText("fmt_remedy_end_dt")));

		// //VD added end

		// ----------------- Remedy fields ---------------------------*

		ht.put("remedy_status", new WebFieldDisplay("remedy_status",
				(addMode ? "" : db.getText("remedy_status"))));

		ht.put("remedy_grp_tx", new WebFieldDisplay("remedy_grp_tx",
				(addMode ? "" : db.getText("remedy_grp_tx"))));

		ht.put("title_nm",
				new WebFieldDisplay("title_nm", addMode ? "" : db
						.getText("title_nm")));

		ht.put("description_blob", new WebFieldDisplay("description_blob",
				addMode ? "" : db.getText("description_blob")));

		// -------------- end of remedy fields -------------*

		ht.put("remedy_asof_date", new WebFieldDisplay("remedy_asof_date",
				(addMode ? "" : db.getText("remedy_asof_date"))));

		/*
		 * Dates
		 */

		if (ac_ok) {
			ht.put("build_dt",
					new WebFieldDate("build_dt", addMode ? "" : db
							.getText("build_dt")));

			ht.put("prod_build_dt", new WebFieldDate("prod_build_dt",
					addMode ? "" : db.getText("prod_build_dt")));

			ht.put("bsns_submit_dt", new WebFieldDate("bsns_submit_dt",
					addMode ? "" : db.getText("bsns_submit_dt")));

			ht.put("bsns_expect_dt", new WebFieldDate("bsns_expect_dt",
					addMode ? "" : db.getText("bsns_expect_dt")));

			ht.put("est_complete_dt", new WebFieldDate("est_complete_dt",
					addMode ? "" : db.getText("est_complete_dt")));

			ht.put("assign_date", new WebFieldDate("assign_date", addMode ? ""
					: db.getText("assign_date")));

			ht.put("created_date", new WebFieldDate("created_date",
					addMode ? "" : db.getText("created_date")));

			ht.put("request_date", new WebFieldDate("request_date",
					addMode ? "" : db.getText("request_date")));

			ht.put("required_date", new WebFieldDate("required_date",
					addMode ? "" : db.getText("required_date")));

			ht.put("end_dt",
					new WebFieldDisplay("end_dt", addMode ? "" : db
							.getText("fmt_remedy_end_dt")));

			ht.put("test_start_tx", new WebFieldString("test_start_tx",
					addMode ? "" : db.getText("test_start_tx"), 100, 255));

		} else {
			ht.put("build_dt", new WebFieldDisplay("build_dt", addMode ? ""
					: db.getText("build_dt")));

			ht.put("prod_build_dt", new WebFieldDisplay("prod_build_dt",
					addMode ? "" : db.getText("prod_build_dt")));

			ht.put("bsns_submit_dt", new WebFieldDisplay("bsns_submit_dt",
					addMode ? "" : db.getText("bsns_submit_dt")));

			ht.put("bsns_expect_dt", new WebFieldDisplay("bsns_expect_dt",
					addMode ? "" : db.getText("bsns_expect_dt")));

			ht.put("est_complete_dt", new WebFieldDisplay("est_complete_dt",
					addMode ? "" : db.getText("est_complete_dt")));

			ht.put("assign_date", new WebFieldDisplay("assign_date",
					addMode ? "" : db.getText("assign_date")));

			ht.put("created_date", new WebFieldDisplay("created_date",
					addMode ? "" : db.getText("created_date")));

			ht.put("request_date", new WebFieldDisplay("request_date",
					addMode ? "" : db.getText("request_date")));

			ht.put("required_date", new WebFieldDisplay("required_date",
					addMode ? "" : db.getText("required_date")));

			ht.put("test_start_tx", new WebFieldDisplay("test_start_tx",
					addMode ? "" : db.getText("test_start_tx")));
		}

		/*
		 * Blobs
		 */

		if (ac_ok) {
			ht.put("release_blob", new WebFieldText("release_blob",
					addMode ? "" : db.getText("release_blob"), 3, 100));

			ht.put("user_blob",
					new WebFieldText("user_blob", addMode ? "" : db
							.getText("user_blob"), 5, 100));

			ht.put("ancillary_blob", new WebFieldText("ancillary_blob",
					addMode ? "" : db.getText("ancillary_blob"), 5, 100));

			ht.put("impact_blob", new WebFieldText("impact_blob", addMode ? ""
					: db.getText("impact_blob"), 5, 100));

			ht.put("test_blob",
					new WebFieldText("test_blob", addMode ? "" : db
							.getText("test_blob"), 5, 100));
		} else {
			ht.put("release_blob", new WebFieldDisplay("release_blob",
					addMode ? "" : db.getText("release_blob")));

			ht.put("user_blob", new WebFieldDisplay("user_blob", addMode ? ""
					: db.getText("user_blob")));

			ht.put("ancillary_blob", new WebFieldDisplay("ancillary_blob",
					addMode ? "" : db.getText("ancillary_blob")));

			ht.put("impact_blob", new WebFieldDisplay("impact_blob",
					addMode ? "" : db.getText("impact_blob")));

			ht.put("test_blob", new WebFieldDisplay("test_blob", addMode ? ""
					: db.getText("test_blob")));

		}

		/*
		 * Return
		 */

		return ht;

	}
}
