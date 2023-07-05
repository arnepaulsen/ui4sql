/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import db.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import services.ExcelWriter;
import services.ServicesException;

import org.apache.poi.hssf.util.HSSFColor;

import router.SessionMgr;

/**
 * Build Plugin:
 * 
 * Information about code builds that are going to production.
 * 
 * the build_no is unique within a product_cd. this plug-in automatically
 * assigns the next sequential # by doing a look-up on the existing last #.
 * 
 * Change log:
 * 
 * Keywords :
 * 
 * 
 * 7/24/09 allow 'executives' to edit any BT 7/27/09 send the prod_rfc_no to the
 * 'new' link
 * 
 * 8/18/09 fix call to get last build_no - handle MySqL syntax
 * 
 * 9/11/09 vBuildTracker2 - use ItemNo instead of Item# for MySQL
 * 
 * 12/22/09 remove 'category_cd", add "maintenance_type_cd" for rfc rouitine
 * maintenance, non-routine, routine-by exception
 * 
 * 1/18/10 - Point back to "RFC" pluging, not the RIPCAB plugin, they've been
 * merged
 * 
 * 3/16/10 - Don't show rfc link if the exception tpe (routine_maintenance_cd)
 * is "Exception" they wont have or create sr/rfc for those
 * 
 * 6/9/10 - fix product_cd query on add, so that it get products for the users's
 * suite, `not from database record ..bcz there is no current record on an add
 * 
 * 6/13/10 use standard view names
 * 
 * 12/19/10 AP Show hyper-link to SR if driver_no is a valid sr
 * 
 * 1/7/10 AP Remove debug statements (no in a catch block)
 * 
 * 3/11/11 Don't give link to RFC page if Prod RFC # is 0 Don't allow edit by
 * non-priv users if the production RFC is closed (this value is from trfc
 * linked to the prod rfc)
 * 
 * 
 * 
 * 
 */

public class BuildPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public BuildPlugin() throws services.ServicesException {
		super();

		this.setContextSwitchOk(false);

		this.setTableName("tbuild");
		this.setKeyName("build_id");
		this.setListOrder("product_cd, build_no");

		this.setTargetTitle("Build Tracker");

		this.setCopyOk(false);
		this.setDeleteLevel("leader");
		this.setExcelOk(true);
		this.setSubmitOk(false);
		this.setNextOk(false);
		this.setShowAuditSubmitApprove(false);

		this.setListHeaders(
				new String[] { "#", "Status", "Type", "Suite", "Product", "Trigger", "Owner", "Description" }); // new
																												// type

		this.setListSelectorColumnFlags(new boolean[] { false, true, true, true, true, true, true, false });

		this.setListViewName("vbuild_list");

		// testing new view

		// this.setSelectViewName("vdetailbuild");

		this.setSelectViewName("vbuild");

	}

	public boolean getEditOk() {

		/*
		 * 7/21/09 David Oritiz allow edit if either 1. user is owner. 2. user is a
		 * "change approver" and the product of the BT is same as the user default
		 * product code
		 */

		// debug("buildplugin..getEditOk ... approver..." +
		// sm.userIsChgApprover()
		// + " bt prod .." + db.getText("product_cd") + " user prod:: "
		// + sm.userDefaultProduct());
		if ((db.getInteger("owner_uid").equals(sm.getUserId())
				&& !(db.getText("prod_rfc_status").equalsIgnoreCase("clo")))
				|| (sm.userIsChgApprover() && db.getText("product_cd").equalsIgnoreCase(sm.getUserProduct()))
				|| sm.userIsExecutive()) {
			return true;

		}
		return false;
	}

	private String getProduct() {

		if (sm.Parm("FilterProduct").length() < 1) {
			return sm.getUserProduct();
		} else
			return sm.Parm("FilterProduct");
	}

	public boolean beforeAdd(Hashtable<String, DbField> ht) {

		// force the status to "new" on an add
		ht.put("status_cd", new DbFieldString("status_cd", "N"));

		// ht.put("product_cd", new DbFieldString("product_cd", getProduct()));

		ht.put("owner_uid", new DbFieldInteger("owner_uid", new Integer(sm.getUserId())));

		/*
		 * automatically assign the build # as the next sequential within the product
		 * cd.
		 */
		String query = "SELECT build_no  from tbuild where product_cd = '" + sm.Parm("product_cd")
				+ "' ORDER BY build_no DESC  LIMIT 1";
		try {
			ResultSet rs = db.getRS(query);

			if (rs.next() == true) {

				Integer i = rs.getInt("build_no");
				int build_no = i.intValue() + 1;

				ht.put("build_no", new DbFieldInteger("build_no", build_no));
			}
		} catch (SQLException s) {
			debug("beforeAdd:get build no : " + s.toString());
		}

		catch (services.ServicesException s) {

		}

		return true;
	}

	/***************************************************************************
	 * 
	 * 8/22 : List Overrides
	 * 
	 **************************************************************************/

	/*
	 * 8/22 : only the status column will be called, so no need to check the column
	 * #
	 */
	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 1) {
			// debug("filter status");
			// default to 'all open' = NO new (n) + in progress (o)
			return getListSelector("FilterStatus", "NOP", "Status?", sm.getCodes("WORKSTATUS2"));
		}

		// allow multiple selects on the config-type

		if (columnNumber == 2) {

			String[][] maint = { { "RE", "RM", "NR", "XX" }, { "Exception", "Routine", "Non-Routine", "RTM/NonRTM" } };

			WebFieldSelect wfMaint = new WebFieldSelect("FilterMaintenance",
					sm.Parm("FilterMaintenance").length() == 0 ? "" : sm.Parm("FilterMaintenance"), maint);

			wfMaint.setDisplayClass("listform");
			wfMaint.setSelectPrompt("Type?");
			wfMaint.setPleaseSelect(true);
			return wfMaint;

		}

		if (columnNumber == 3) {
			// debug("filter type");
			return getListSelector("FilterType", "", "Trigger?", sm.getCodesAlt("TRIGGER"));
		}

		if (columnNumber == 4) {
			// debug("filter product");

			WebFieldSelect wfSuite = new WebFieldSelect("FilterSuite",
					(sm.Parm("FilterSuite").length() > 1) ? sm.Parm("FilterSuite") : sm.getUserSuite(),
					sm.getCodesAlt("SUITES"));

			wfSuite.setDisplayClass("listform");
			wfSuite.setPleaseSelect(false);
			return wfSuite;

		}

		if (columnNumber == 5) {

			String qry = "select c.order_by, c.code_value, code_desc " + " from tcodes c " + " where c.code_desc2 = '"
					+ ((sm.Parm("FilterSuite").length() > 0) ? sm.Parm("FilterSuite") : sm.getUserSuite())
					+ "' and code_type_id = 121 order by order_by ";

			Hashtable products = new Hashtable();

			try {
				products = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}

			// debug("filter product");
			return getListSelector("FilterProduct", sm.getUserProduct(), "Product?", products);

		}

		// Just get contracts that actually have an adHoc assigned
		// TODO.. Resource HOG!!! It queries every time. Let the db manager
		// cache the data.
		String qry = new String("select distinct " + "concat(c.last_name, ', ', c.first_name) as a , "
				+ "c.user_id as b, " + "concat(c.last_name, ', ',	c.first_name) as c "
				+ " from tbuild join tuser c on tbuild.owner_uid = c.user_id ");

		Hashtable contacts = new Hashtable();

		try {
			contacts = db.getLookupTable(qry);
		} catch (ServicesException e) {
		}
		return getListSelector("FilterOwner", new Integer("0"), "Owner ? ", contacts);

	}

	public String getListAnd() {
		/*
		 * todo: cheating... need to map the code value to the description
		 */

		StringBuffer sb = new StringBuffer();

		// default status to open if no filter present

		if (!sm.Parm("FilterProduct").equalsIgnoreCase("0")) {
			sb.append(" AND product_cd = '" + getProduct() + "'");
		}

		if (sm.Parm("FilterSuite").length() < 1)

			sb.append(" AND suite_cd = '" + sm.getUserSuite() + "'");
		else
			sb.append(" AND suite_cd = '" + sm.Parm("FilterSuite") + "'");

		// trick-one .... filter on status for all 'open' = new + in-progress
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND (status_cd = 'O' OR status_cd = 'N' OR status_cd = 'P') ");

		} else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {

				if (sm.Parm("FilterStatus").equalsIgnoreCase("NOP"))
					sb.append(" AND (status_cd = 'N' OR status_cd = 'O' OR status_cd = 'P') ");
				else

					sb.append(" AND status_cd = '" + sm.Parm("FilterStatus") + "'");
			}
		}

		if ((!sm.Parm("FilterType").equalsIgnoreCase("0")) && (sm.Parm("FilterType").length() > 0)) {
			sb.append(" AND driver_cd = '" + sm.Parm("FilterType") + "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0")) && (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND owner_uid = " + sm.Parm("FilterUser"));
		}

		if ((!sm.Parm("FilterMaintenance").equalsIgnoreCase("0")) && (sm.Parm("FilterMaintenance").length() > 0)) {

			// XX is a puedo code for Rtn and Non_Rtn maintenance
			if (sm.Parm("FilterMaintenance").equalsIgnoreCase("XX")) {
				sb.append(" AND maintenance_type_cd IN ('RM', 'NR')");
			} else {

				sb.append(" AND maintenance_type_cd ='" + sm.Parm("FilterMaintenance") + "' ");
			}
		}

		// filter on owner
		if (sm.Parm("FilterOwner").length() == 0) {
		} else {
			if (!sm.Parm("FilterOwner").equalsIgnoreCase("0")) {
				sb.append(" AND owner_uid = " + sm.Parm("FilterOwner"));
			}
		}

		return sb.toString();

	}

	/*
	 * todo: move to plugin
	 * 
	 * also see BuildPlugin
	 */

	private void filterMultiSelect(StringBuffer sb, String filterName, String columnName) {
		String[] array = sm.ParmArray(filterName);
		if (array != null) {
			if (array.length > 0) {
				String x = array[0];
				if (!x.equalsIgnoreCase("0") && !x.equalsIgnoreCase("")) {
					sb.append(" AND " + columnName + " in ('" + array[0] + "'");
					for (int i = 1; i < array.length; i++) {
						sb.append(",'" + array[i] + "'");
					}
					sb.append(")");
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode) throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;
		boolean showMode = parmMode.equalsIgnoreCase("show") ? true : false;
		boolean editMode = parmMode.equalsIgnoreCase("edit") ? true : false;

		Hashtable ht = new Hashtable();

		/*
		 * rfc links
		 */

		//System.out.println("Build getWebFields... starting");

		ht.put("mode", new WebFieldHidden("mode", parmMode));

		//System.out.println("Build getWebFields... rfc links");

		String prod_rfc_link = new String("");
		String stgn_rfc_link = new String("");
		String wits_rfc_link = new String("");
		String wit3_rfc_link = new String("");
		String psup_rfc_link = new String("");
		String sr_link = addMode ? "" : db.getText("driver_no");

		boolean prod_rfc_is_new = false;

		String sr_this_link = "<A href=Router?Target=ServiceRequest&Action=Show&Relation=this&RowKey=";

		String cab_this_link = "<A href=Router?Target=Rfc&Action=Show&Relation=this&RowKey=";

		System.out.println("Build getWebFields... cab link");

		String cab_new_link = "<A href=Router?Target=Rfc&Action=Add&trigger_cd=" + db.getText("driver_cd")
				+ "&problem_nm=" + db.getText("driver_no") + "&rfc_no=" + db.getText("rfc_prod_no") + "&build_no="
				+ db.getText("build_no") + "&build_track_suite_cd=" + db.getText("product_cd") + ">";

		if (showMode) {

			// System.out.println("Build getWebFields... show mode, putting links" );

			// System.out.println("Build getWebFields... 20 sr_id");

			if (db.getInteger("sr_id").intValue() > 0) {
				sr_link = sr_this_link + db.getText("sr_id") + ">" + db.getText("driver_no") + "</A>";
			}

			// System.out.println("Build getWebFields... 21 rfc_id");

			if (db.getText("wits_rfc_id").equalsIgnoreCase("0")) {
				wits_rfc_link = cab_new_link + db.getText("rfc_wits_no") + "</a>";
			} else {
				wits_rfc_link = cab_this_link + db.getText("wits_rfc_id") + ">" + db.getText("rfc_wits_no") + "</A>";
			}

			// System.out.println("Build getWebFields... 22 wit3 rfc id ");

			if (db.getText("wit3_rfc_id").equalsIgnoreCase("0")) {
				wit3_rfc_link = cab_new_link + db.getText("rfc_wit3_no") + "</a>";
			} else {
				wit3_rfc_link = cab_this_link + db.getText("wit3_rfc_id") + ">" + db.getText("rfc_wit3_no") + "</A>";
			}

			//debug("Build getWebFields... 24 rfc stgn no ");

			if (db.getText("stgn_rfc_id").equalsIgnoreCase("0")) {
				stgn_rfc_link = cab_new_link + db.getText("rfc_stgn_no") + "</a>";
			} else {
				stgn_rfc_link = cab_this_link + db.getText("stgn_rfc_id") + ">" + db.getText("rfc_stgn_no") + "</A>";
			}

			if (db.getText("psup_rfc_id").equalsIgnoreCase("0")) {
				psup_rfc_link = cab_new_link + db.getText("rfc_psup_no") + "</a>";
			} else {
				psup_rfc_link = cab_this_link + db.getText("psup_rfc_id") + ">" + db.getText("rfc_psup_no") + "</A>";
			}

			if (db.getText("prod_rfc_id").equalsIgnoreCase("0")) {
				prod_rfc_is_new = true;
				prod_rfc_link = "0";
				// cab_new_link + db.getText("rfc_prod_no") + "</a>";
			} else {
				prod_rfc_is_new = false;
				prod_rfc_link = cab_this_link + db.getText("prod_rfc_id") + ">" + db.getText("rfc_prod_no") + "</A>";
			}

		}

		/*
		 * codes
		 */

		System.out.println("Build getWebFields... putting codes");

		Hashtable databases = sm.getServletTable("tdatabase",
				"select reference_nm, database_id, reference_nm from tdatabase join tapplications on tdatabase.application_id = tapplications.application_id where tdatabase.division_id = "
						+ sm.getDivisionId().toString() + " order by title_nm");

		/*
		 * can update if: - new - blank - change approver
		 */
		if (addMode || (!addMode && db.getText("maintenance_type_cd").length() < 1) || sm.userIsChgApprover()) {

			ht.put("maintenance_type_cd", new WebFieldSelect("maintenance_type_cd",
					addMode ? "" : db.getText("maintenance_type_cd"), sm.getCodes("MAINTENANCE"), true));

			ht.put("suite_cd", new WebFieldSelect("suite_cd", db.getText("suite_cd"), sm.getCodes("SUITES"), true));

			ht.put("maintenance", new WebFieldHidden("maintenance", "YES"));

		} else {

			System.out.println("Build getWebFields... putting maintenance codes");

			ht.put("maintenance_type_cd", new WebFieldDisplay("maintenance_type_cd", db.getText("maint_desc")));
			ht.put("maintenance", new WebFieldHidden("maintenance", "NO"));

		}

		System.out.println("Build getWebFields... 10 database id ");

		ht.put("database_id", new WebFieldSelect("database_id",
				addMode ? new Integer("0") : db.getInteger("database_id"), databases, true));

		ht.put("database2_id", new WebFieldSelect("database2_id",
				addMode ? new Integer("0") : db.getInteger("database2_id"), databases, "-Optional-"));

		ht.put("database3_id", new WebFieldSelect("database3_id",
				addMode ? new Integer("0") : db.getInteger("database3_id"), databases, "-Optional-"));

		ht.put("database4_id", new WebFieldSelect("database4_id",
				addMode ? new Integer("0") : db.getInteger("database4_id"), databases, "-Optional-"));

		if (addMode) {
			// 6/4/2010 Give user choice of products for their suite only

			String qry = "select c.order_by, c.code_value, code_desc " + " from tcodes c " + " where c.code_desc2 = '"
					+ sm.getUserSuite() + "' and code_type_id = 121 order by order_by ";

			Hashtable products = new Hashtable();

			try {
				products = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}

			ht.put("product_cd", new WebFieldSelect("product_cd",
					addMode ? sm.getUserProduct() : db.getText("product_cd"), products, true));
		} else {
			ht.put("product_cd", new WebFieldDisplay("product_cd", db.getText("prod_desc")));
		}
		ht.put("driver_cd", new WebFieldSelect("driver_cd", addMode ? "" : db.getText("driver_cd"),
				sm.getCodesAlt("TRIGGER"), true));

		// force status to "new" on an add
		if (addMode) {
			ht.put("status_cd", new WebFieldDisplay("status_cd", "New"));
		} else {
			ht.put("status_cd", new WebFieldSelect("status_cd", db.getText("status_cd"), sm.getCodes("WORKSTATUS")));
		}

		ht.put("all_instance_cd", new WebFieldSelect("all_instance_cd", addMode ? "N" : db.getText("all_instance_cd"),
				sm.getCodes("YESNO")));

		/*
		 * Id's
		 */
		// on add, force the owner_id to the logged in user, cannot change

		System.out.println("ids");

		if (addMode)
			// on add, always show current user
			ht.put("owner_uid", new WebFieldDisplay("owner_uid", sm.getLastName() + ", " + sm.getFirstName()));

		else {
			if (db.getText("owner_nm").length() > 0) {
				// this was an uploaded bt, so show this if there is no real
				// owneer
				if (db.getText("owner_uid").toString().equalsIgnoreCase("0")) {
					ht.put("owner_uid", new WebFieldDisplay("owner_uid", db.getText("owner_nm")));
				} else {
					if (sm.userIsChgApprover()) {
						ht.put("owner_uid",
								new WebFieldSelect("owner_uid", db.getInteger("owner_uid"), sm.getUserHT(), true));
					} else {
						ht.put("owner_uid", new WebFieldDisplay("owner_uid", db.getText("owner_nm")));
					}
				}
			} else {
				if (sm.userIsChgApprover()) {
					ht.put("owner_uid",
							new WebFieldSelect("owner_uid", db.getInteger("owner_uid"), sm.getUserHT(), true));
				} else {
					ht.put("owner_uid", new WebFieldDisplay("owner_uid", db.getText("owner_name")));
				}
			}
		}

		/*
		 * Numbers
		 */

		System.out.println("numbers");

		ht.put("rfc_wits_no", new WebFieldString("rfc_wits_no", (addMode ? "" : db.getText("rfc_wits_no")), 8, 8));

		ht.put("rfc_wit3_no", new WebFieldString("rfc_wit3_no", (addMode ? "" : db.getText("rfc_wit3_no")), 8, 8));

		ht.put("rfc_stgn_no", new WebFieldString("rfc_stgn_no", (addMode ? "" : db.getText("rfc_stgn_no")), 8, 8));

		ht.put("rfc_train_no", new WebFieldString("rfc_train_no", (addMode ? "" : db.getText("rfc_train_no")), 8, 8));

		/*
		 * tricky one.. block the link over to
		 */

		System.out.println("add mode");

		if (addMode) {
			ht.put("rfc_prod_no", new WebFieldDisplay("rfc_prod_no", ""));
		} else {
			ht.put("rfc_prod_no", new WebFieldString("rfc_prod_no",
					((!editMode) && (!db.getText("maintenance_type_cd").equalsIgnoreCase("RE"))
							&& ((!sm.getRipBlock() || sm.userIsChgApprover()) || (!prod_rfc_is_new)) ? prod_rfc_link
									: db.getText("rfc_prod_no")),
					8, 8));
		}

		ht.put("rfc_psup_no", new WebFieldString("rfc_psup_no", (addMode ? "" : db.getText("rfc_psup_no")), 8, 8));

		ht.put("rfc_no", new WebFieldString("rfc_no", (addMode ? "" : db.getText("rfc_no")), 8, 8));

		ht.put("driver_no", new WebFieldString("driver_no", (addMode ? "" : sr_link), 8, 8));

		/*
		 * Text
		 */

		System.out.println("build no");

		ht.put("build_no", new WebFieldDisplay("build_no", (addMode ? "" : db.getText("build_no"))));

		ht.put("cancel_reason_tx",
				new WebFieldString("cancel_reason_tx", (addMode ? "" : db.getText("cancel_reason_tx")), 128, 255));

		ht.put("dependencies_tx",
				new WebFieldString("dependencies_tx", (addMode ? "" : db.getText("dependencies_tx")), 64, 125));

		ht.put("cat_list_nm", new WebFieldString("cat_list_nm", (addMode ? "" : db.getText("cat_list_nm")), 64, 128));

		ht.put("owner_nm", new WebFieldDisplay("owner_nm", (addMode ? "" : db.getText("owner_nm"))));

		/*
		 * 
		 * Blobs
		 */

		System.out.println("blobs");

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db.getText("desc_blob"), 5, 100));

		ht.put("comments_blob", new WebFieldText("comments_blob", addMode ? "" : db.getText("comments_blob"), 5, 100));

		ht.put("impacts_blob", new WebFieldText("impacts_blob", addMode ? "" : db.getText("impacts_blob"), 5, 100));

		/*
		 * wits3
		 */

		System.out.println("wit3");

		ht.put("wit3_ca_dt", new WebFieldDate("wit3_ca_dt", (addMode ? "" : db.getText("wit3_ca_dt"))));

		ht.put("wit3_cc_dt", new WebFieldDate("wit3_cc_dt", (addMode ? "" : db.getText("wit3_cc_dt"))));

		ht.put("wit3_gg_dt", new WebFieldDate("wit3_gg_dt", (addMode ? "" : db.getText("wit3_gg_dt"))));

		ht.put("wit3_eb_dt", new WebFieldDate("wit3_eb_dt", (addMode ? "" : db.getText("wit3_eb_dt"))));

		ht.put("wit3_nb_dt", new WebFieldDate("wit3_nb_dt", (addMode ? "" : db.getText("wit3_nb_dt"))));

		ht.put("wit3_sb_dt", new WebFieldDate("wit3_sb_dt", (addMode ? "" : db.getText("wit3_sb_dt"))));

		ht.put("wit3_nc_dt", new WebFieldDate("wit3_nc_dt", (addMode ? "" : db.getText("wit3_nc_dt"))));

		/*
		 * train
		 */

		System.out.println("train");

		ht.put("train_ca_dt", new WebFieldDate("train_ca_dt", (addMode ? "" : db.getText("train_ca_dt"))));

		ht.put("train_cc_dt", new WebFieldDate("train_cc_dt", (addMode ? "" : db.getText("train_cc_dt"))));

		ht.put("train_gg_dt", new WebFieldDate("train_gg_dt", (addMode ? "" : db.getText("train_gg_dt"))));

		ht.put("train_eb_dt", new WebFieldDate("train_eb_dt", (addMode ? "" : db.getText("train_eb_dt"))));

		ht.put("train_nb_dt", new WebFieldDate("train_nb_dt", (addMode ? "" : db.getText("train_nb_dt"))));

		ht.put("train_sb_dt", new WebFieldDate("train_sb_dt", (addMode ? "" : db.getText("train_sb_dt"))));

		ht.put("train_nc_dt", new WebFieldDate("train_nc_dt", (addMode ? "" : db.getText("train_nc_dt"))));

		/*
		 * wits
		 */

		System.out.println("wits");

		ht.put("wits_nc_dt", new WebFieldDate("wits_nc_dt", (addMode ? "" : db.getText("wits_nc_dt"))));

		ht.put("wits_ca_dt", new WebFieldDate("wits_ca_dt", (addMode ? "" : db.getText("wits_ca_dt"))));

		ht.put("wits_cc_dt", new WebFieldDate("wits_cc_dt", (addMode ? "" : db.getText("wits_cc_dt"))));

		ht.put("wits_eb_dt", new WebFieldDate("wits_eb_dt", (addMode ? "" : db.getText("wits_eb_dt"))));

		ht.put("wits_gg_dt", new WebFieldDate("wits_gg_dt", (addMode ? "" : db.getText("wits_gg_dt"))));

		ht.put("wits_nb_dt", new WebFieldDate("wits_nb_dt", (addMode ? "" : db.getText("wits_nb_dt"))));

		ht.put("wits_sb_dt", new WebFieldDate("wits_sb_dt", (addMode ? "" : db.getText("wits_sb_dt"))));

		/*
		 * 
		 */

		ht.put("stgn_ca_dt", new WebFieldDate("stgn_ca_dt", (addMode ? "" : db.getText("stgn_ca_dt"))));

		ht.put("stgn_cc_dt", new WebFieldDate("stgn_cc_dt", (addMode ? "" : db.getText("stgn_cc_dt"))));

		ht.put("stgn_eb_dt", new WebFieldDate("stgn_eb_dt", (addMode ? "" : db.getText("stgn_eb_dt"))));

		ht.put("stgn_gg_dt", new WebFieldDate("stgn_gg_dt", (addMode ? "" : db.getText("stgn_gg_dt"))));

		ht.put("stgn_nb_dt", new WebFieldDate("stgn_nb_dt", (addMode ? "" : db.getText("stgn_nb_dt"))));

		ht.put("stgn_sb_dt", new WebFieldDate("stgn_sb_dt", (addMode ? "" : db.getText("stgn_sb_dt"))));

		ht.put("stgn_nc_dt", new WebFieldDate("stgn_nc_dt", (addMode ? "" : db.getText("stgn_nc_dt"))));

		/*
		 * PSUP
		 */

		ht.put("psup_ca_dt", new WebFieldDate("psup_ca_dt", (addMode ? "" : db.getText("psup_ca_dt"))));

		ht.put("psup_cc_dt", new WebFieldDate("psup_cc_dt", (addMode ? "" : db.getText("psup_cc_dt"))));

		ht.put("psup_eb_dt", new WebFieldDate("psup_eb_dt", (addMode ? "" : db.getText("psup_eb_dt"))));

		ht.put("psup_gg_dt", new WebFieldDate("psup_gg_dt", (addMode ? "" : db.getText("psup_gg_dt"))));

		ht.put("psup_nb_dt", new WebFieldDate("psup_nb_dt", (addMode ? "" : db.getText("psup_nb_dt"))));

		ht.put("psup_sb_dt", new WebFieldDate("psup_sb_dt", (addMode ? "" : db.getText("psup_sb_dt"))));

		ht.put("psup_nc_dt", new WebFieldDate("psup_nc_dt", (addMode ? "" : db.getText("psup_nc_dt"))));

		/*
		 * PROD
		 */

		if (addMode) {

			ht.put("prod_ca_dt", new WebFieldDisplay("prod_ca_dt", ""));

			ht.put("prod_cc_dt", new WebFieldDisplay("prod_ca_dt", ""));

			ht.put("prod_eb_dt", new WebFieldDisplay("prod_ca_dt", ""));

			ht.put("prod_gg_dt", new WebFieldDisplay("prod_ca_dt", ""));

			ht.put("prod_sb_dt", new WebFieldDisplay("prod_ca_dt", ""));

			ht.put("prod_nb_dt", new WebFieldDisplay("prod_ca_dt", ""));

			ht.put("prod_nc_dt", new WebFieldDisplay("prod_ca_dt", ""));

		} else {
			ht.put("prod_ca_dt", new WebFieldDate("prod_ca_dt", db.getText("prod_ca_dt")));

			ht.put("prod_cc_dt", new WebFieldDate("prod_cc_dt", db.getText("prod_cc_dt")));

			ht.put("prod_eb_dt", new WebFieldDate("prod_eb_dt", db.getText("prod_eb_dt")));

			ht.put("prod_gg_dt", new WebFieldDate("prod_gg_dt", db.getText("prod_gg_dt")));

			ht.put("prod_nb_dt", new WebFieldDate("prod_nb_dt", db.getText("prod_nb_dt")));

			ht.put("prod_sb_dt", new WebFieldDate("prod_sb_dt", db.getText("prod_sb_dt")));

			ht.put("prod_nc_dt", new WebFieldDate("prod_nc_dt", db.getText("prod_nc_dt")));
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

		String templateName = "build.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName() + "_Build_Tracker_";
		int columns = 51;
		short startRow = 2;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath, filePrefix, getExcelResultSet(), startRow,
				columns);

	}

	private ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vbuild_excel WHERE 1=1 ");

		// NOT always have a product

		if (sm.Parm("FilterSuite").length() < 1)

			sb.append(" AND suite_cd = '" + sm.getUserSuite() + "'");
		else
			sb.append(" AND suite_cd = '" + sm.Parm("FilterSuite") + "'");

		if ((sm.Parm("FilterProduct").length() > 0) && (!sm.Parm("FilterProduct").equalsIgnoreCase("0"))) {
			sb.append(" AND product_cd = '" + getProduct() + "'");
		}

		if ((!sm.Parm("FilterUser").equalsIgnoreCase("0")) && (sm.Parm("FilterUser").length() > 0)) {
			sb.append(" AND owner_uid = " + sm.Parm("FilterUser"));
		}

		if ((!sm.Parm("FilterType").equalsIgnoreCase("0")) && (sm.Parm("FilterType").length() > 0)) {
			sb.append(" AND TriggerType = '" + sm.Parm("FilterType") + "'");
		}

		if ((!sm.Parm("FilterOwner").equalsIgnoreCase("0")) && (sm.Parm("FilterOwner").length() > 0)) {
			sb.append(" AND assigned_uid = " + sm.Parm("FilterOwner"));
		}

		if ((!sm.Parm("FilterMaintenance").equalsIgnoreCase("0")) && (sm.Parm("FilterMaintenance").length() > 0)) {
			sb.append(" AND maintenance_type_cd= '" + sm.Parm("FilterMaintenance") + "' ");
		}

		// trick-one .... filter on status for all 'open' = new + in-progress
		if (sm.Parm("FilterStatus").length() == 0) {
			sb.append(" AND (status_cd = 'O' OR status_cd = 'N' OR status_cd = 'P') ");

		} else {
			if (!sm.Parm("FilterStatus").equalsIgnoreCase("0")) {

				if (sm.Parm("FilterStatus").equalsIgnoreCase("NOP"))
					sb.append(" AND (status_cd = 'N' OR status_cd = 'O' OR status_cd = 'P') ");
				else

					sb.append(" AND status_cd = '" + sm.Parm("FilterStatus") + "'");
			}
		}

		sb.append(" ORDER BY product, ItemNo");
		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}
		return rs;

	}

}
