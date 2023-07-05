/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.util.Hashtable;

import org.apache.poi.hssf.util.HSSFColor;

import forms.*;
import router.SessionMgr;
import services.ExcelWriter;
import services.ServicesException;

/**
 * 2/15 added mySql - 3/13 as 'target' to list query - 3/23 fix list query 9/17
 * add nuid 1/13/09 - change sort order of domains list
 * 
 * 6/14/10 standardize view names
 */

public class HCContactPlugin extends AbsDivisionPlugin {

	public HCContactPlugin() throws services.ServicesException {
		super();

		this.setTableName("thc_contact");
		this.setKeyName("hc_contact_id");
		this.setTargetTitle("Health Connect Contact");

		this.setListViewName("vhc_contact_list");
		this.setSelectViewName("vhc_contact");

		this.setShowAuditSubmitApprove(false);
		this.setExcelOk(true);
		this.setUpdatesLevel("inpatient");

		this.setListHeaders(new String[] { "Name", "Suffix", "Domain",
				"Regional Domain Role", "Sub Domain", "Facility", "Tie-Line",
				"Phone" });

	}

	public void init(SessionMgr parmSm) {
		super.init(parmSm);

		if (sm.Parm("FilterAlpha").equalsIgnoreCase("Y"))
			this.setListOrder("last_nm, first_nm");

		else
			this.setListOrder("HDDomain, regDomain, last_nm");

	}

	/***************************************************************************
	 * 
	 * List Page Options
	 * 
	 * 
	 */

	/*
	 * need to return dynamic title using session Parm data
	 */
	public String getListTitle() {

		String checked = "";
		if (sm.Parm("FilterAlpha").equalsIgnoreCase("Y")) {
			checked = "checked";
		}

		return "Domain Contacts   &nbsp;&nbsp;&nbsp;&nbsp;Alphabetic Sort&nbsp;<input type='checkbox' name='FilterAlpha' id='FilterAlpha' value=Y "
				+ checked + ">";
	}

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 1 || columnNumber == 2 || columnNumber == 3
				|| columnNumber == 4 || columnNumber == 5)
			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		if (columnNumber == 1) {

			WebFieldSelect suffixs = (WebFieldSelect) getListSelector(
					"FilterSuffix", "", "Suffix?", sm.getCodes("IPTITLE"));
			suffixs.allowMultiple();
			suffixs.setSelectedList(sm.ParmArray("FilterSuffix"));
			return suffixs;

		}

		if (columnNumber == 2) {

			String qry = "SELECT order_by as odor, code_value, code_desc from tcodes where code_type_id = 142 and code_desc2 like '%DC%' ORDER BY 1 ";

			Hashtable domains = new Hashtable();

			domains = sm.getTable("IPDOMAINDC", qry);

			WebFieldSelect selectDomains = (WebFieldSelect) getListSelector(
					"FilterDomain", "", "Domain/Group?", domains);
			selectDomains.allowMultiple();
			selectDomains.setSelectedList(sm.ParmArray("FilterFacility"));
			return selectDomains;

		}

		if (columnNumber == 3) {

			WebFieldSelect RegDomainRole = (WebFieldSelect) getListSelector(
					"FilterRegDomainRole", "", "Role?", sm.getCodes("REGROLE"));
			RegDomainRole.allowMultiple();
			RegDomainRole.setSelectedList(sm.ParmArray("FilterRegDomainRole"));
			return RegDomainRole;

		}

		if (columnNumber == 4) {

			WebFieldSelect subDomains = (WebFieldSelect) getListSelector(
					"FilterSubDomain", "", "Sub-Group?", sm
							.getCodes("SUBDOMAIN"));
			subDomains.allowMultiple();
			subDomains.setSelectedList(sm.ParmArray("FilterSubDomain"));
			return subDomains;

		}

		if (columnNumber == 5) {

			Hashtable facilities = sm
					.getTable(
							"tfacility",
							"select facility_cd, facility_cd as fac_code, facility_cd as theLabel from tfacility order by facility_cd ");

			WebFieldSelect selectFacility = (WebFieldSelect) getListSelector(
					"FilterFacility", "", "Facility?", facilities);
			selectFacility.allowMultiple();
			selectFacility.setSelectedList(sm.ParmArray("FilterFacility"));
			return selectFacility;
		}

		return getListSelector("FilterActive", "", "Active Y/N", sm
				.getCodes("YESNO"));
	}

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
		 */

		StringBuffer sb = new StringBuffer();

		filterMultiSelect(sb, "FilterSuffix", "suffix_nm");

		filterMultiSelect(sb, "FilterDomain", "hc_domain_cd");

		filterMultiSelect(sb, "FilterRegDomainRole", "reg_domain_role_cd");

		filterMultiSelect(sb, "FilterSubDomain", "hc_sub_domain_cd");

		filterMultiSelect(sb, "FilterFacility", "facility_cd");

		return sb.toString();
	}

	/*
	 * to do.. move this into plugin or html helper
	 * 
	 */
	private void filterMultiSelect(StringBuffer sb, String filterName,
			String columnName) {
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
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		// Strings

		ht.put("first_nm", new WebFieldString("first_nm", addMode ? "" : db
				.getText("first_nm"), 32, 32));

		ht.put("mi_nm", new WebFieldString("mi_nm", addMode ? "" : db
				.getText("mi_nm"), 4, 32));

		ht.put("last_nm", new WebFieldString("last_nm", addMode ? "" : db
				.getText("last_nm"), 32, 32));

		if (sm.userIsAdministrator()) {
			ht.put("nuid_nm", new WebFieldString("nuid_nm", addMode ? "" : db
					.getText("nuid_nm"), 7, 7));
		} else {
			ht.put("nuid_nm", new WebFieldDisplay("nuid_nm", "********"));
		}

		ht.put("email_tx", new WebFieldString("email_tx", addMode ? "" : db
				.getText("email_tx"), 32, 32));

		ht.put("address_tx", new WebFieldString("address_tx", addMode ? "" : db
				.getText("address_tx"), 64, 64));

		ht.put("phone_tx", new WebFieldString("phone_tx", addMode ? "" : db
				.getText("phone_tx"), 32, 32));

		ht.put("tieline_tx", new WebFieldString("tieline_tx", addMode ? "" : db
				.getText("tieline_tx"), 16, 16));

		ht.put("title_nm", new WebFieldString("title_nm", addMode ? "" : db
				.getText("title_nm"), 64, 64));

		ht.put("lotus_dist_tx", new WebFieldString("lotus_dist_tx",
				addMode ? "" : db.getText("lotus_dist_tx"), 64, 64));

		ht.put("notes_tx", new WebFieldString("notes_tx", addMode ? "" : db
				.getText("notes_tx"), 90, 128));

		// flags

		ht.put("active_flag", new WebFieldCheckbox("active_flag", addMode ? "N"
				: db.getText("active_flag"), ""));

		ht.put("chair_flag", new WebFieldCheckbox("chair_flag", addMode ? "N"
				: db.getText("chair_flag"), ""));

		ht.put("liaison_flag", new WebFieldCheckbox("liaison_flag",
				addMode ? "N" : db.getText("liaison_flag"), ""));

		ht.put("meeting_coord_flag", new WebFieldCheckbox("meeting_coord_flag",
				addMode ? "N" : db.getText("meeting_coord_flag"), ""));

		ht.put("primary_rsrc_flag", new WebFieldCheckbox("primary_rsrc_flag",
				addMode ? "N" : db.getText("primary_rsrc_flag"), ""));

		ht.put("local_domain_lead_flag", new WebFieldCheckbox(
				"local_domain_lead_flag", addMode ? "N" : db
						.getText("local_domain_lead_flag"), ""));

		ht.put("facility_lead_flag", new WebFieldCheckbox("facility_lead_flag",
				addMode ? "N" : db.getText("facility_lead_flag"), ""));

		// ids

		Hashtable facilities = sm
				.getTable(
						"tfacility2",
						"select facility_cd, facility_cd, (facility_cd + '-' + facility_nm) facility_nm from tfacility order by facility_cd ");

		ht.put("facility_cd", new WebFieldSelect("facility_cd", addMode ? ""
				: db.getText("facility_cd"), facilities, true, true));

		// Codes

		ht.put("suffix_nm", new WebFieldSelect("suffix_nm", addMode ? "" : db
				.getText("suffix_nm"), sm.getCodes("IPTITLE"), ""));

		ht.put("hc_sub_domain_cd", new WebFieldSelect("hc_sub_domain_cd",
				addMode ? "" : db.getText("hc_sub_domain_cd"), sm
						.getCodes("SUBDOMAIN"), true, true));

		ht.put("reg_domain_role_cd", new WebFieldSelect("reg_domain_role_cd",
				addMode ? "" : db.getText("reg_domain_role_cd"), sm
						.getCodes("REGROLE")));

		String qry = "SELECT order_by, code_value, code_desc from tcodes where code_type_id = 142 and code_desc2 like '%DC%' ORDER BY 1 ";

		Hashtable domains = new Hashtable();

		domains = sm.getTable("IPDOMAINDC", qry);

		ht.put("hc_domain_cd", new WebFieldSelect("hc_domain_cd", addMode ? ""
				: db.getText("hc_domain_cd"), domains));

		ht.put("suit_cd", new WebFieldSelect("suit_cd", addMode ? "" : db
				.getText("suit_cd"), sm.getCodes("SUIT-IP"), true));

		// dates

		ht.put("facl_live_date", new WebFieldDisplay("facl_live_date", db
				.getText("golive_date")));

		return ht;

	}

	// create Excel from ResultSet and save to the path in the web.xml config
	// file
	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "hc_contact.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = "RegionalContacts";
		int columns = 22;
		short startRow = 1;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);

	}

	/*
	 * No filtering.. just dump the whole table
	 * 
	 */

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vhc_contact_excel WHERE 1=1 ");

		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

}
