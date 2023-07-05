/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import forms.*;
import db.*;
import remedy.RemedyChangeQuery;
import router.SessionMgr;
import services.ExcelWriter;
import services.RemedyChangeRequest;

import java.util.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;

/**
 * 
 *   2/15 added mySql
 *  3/11 am another getDisplayOrSelect (.... ht)
 *  3/12 setSessionFilterKey on row update and add
 * 	3/12 add insert to project permission
 *  4/18/05 - add getFilter for datatype division
 *  11/24/05 - Remove getDisplayOrSelect
 *  12/5/05 - change fuzzy logic to test endsWith(_date) instead of anywhere in string
 *  6/12/07 - add dbFieldBinary to fuzzy logic for "globs"
 *  8/29/07 - put back added_uid and added_date into addRow(), taken out by mistake
 *  8/23/09 - add column widths for list page
 *  2/6/10 - add 'hrs' to fuzzy logic to save int
 *  
 *  4/2/10 - turn of "nextOk" by default.  it goes to next physical record, not logical as per list page
 * 
 * */

/**
 * @author PAULSEAR
 * 
 *         4/23/05 - Add property for 'approve button' 6/10/5 - add
 *         getCustomSubForm to getQuestions rs query 6/13/05 don't do
 *         customFields if skipCustomFields is true
 * 
 *         6/26/05 add getWhereAnd to the list query, usually empty. used by
 *         Sign-Off to limit the list
 * 
 *         8/22 : Add overrides for filtering columns on the list page"
 * 
 *         10/11 : add "limit 1" on set row to prevent > 1 row villians are
 *         tclosure and tpoint that could have > 1 entry per project
 * 
 *         10/18 : add copyRow method 4.26.06 : add boolean for centering list
 *         items 5/17/06 : add 'cancelOk' for Register form 5/17/06 : add
 *         'printOk'
 * 
 *         9/12/06 : add ht declarations <String, DbField> .. add hack for
 *         splitTable on vwork
 * 
 *         10/5/06 : add helper functions for getListSelector
 * 
 *         10/13/06 : editOK, deleteOk, copyOk ... do: return
 *         !getUserRoleCode().equalsIgnoreCase("brw"); 1/12/07 bKeyAutoIncrement
 *         for user-defined keys
 * 
 *         1/12/07 turn skipCustomFields = true, individual plugins must turn it
 *         on!!!
 * 
 *         9/9/07 default newRowKey to -1 in case add fails..CAUTION, will cause
 *         problems with tables that do not have a -1 record, but problem exists
 *         anyway if newRowKey isn't set.
 * 
 *         10/9/07 Add excelOk for list page
 * 
 *         7/14/08 Allow Strings as gotoKeys
 * 
 *         9/4/08 listBgColor - also pass DbField array
 * 
 *         10/8/08 huge -- allow for audit log of all updates to a table ! using
 *         logHistory flag, and fields for the sequence # and parent/audit set !
 * 
 *         11/20/08 make handles and passwords uppercase, for Rmedy
 * 
 *         3/13/09 add views for list processing
 * 
 *         1/19/09 - preDisplay is now called from edit/new pages, so 1st check
 *         for db.hasRow() .. before reference to the record.
 * 
 *         reverted .. not needed 2/10/09 add constructor for getListSelector
 *         for the string prompt
 * 
 *         7/31 ap don't create sql update for fields that start with 'remedy'
 *         ... that will be done by /services/RemedyChangeRequest
 * 
 *         8/11 add get/setListButtonsCenter .. wide list pages push the buttons
 *         off the screen, so this allows them to be centered ... see
 *         FormListWriter
 * 
 *         12/8 add afterDelete method so plugins can clean up.
 * 
 *         12/19 don't do afterAdd if bad rc from beforeAdd
 * 
 *         1/7 AP remove debug statements
 * 
 *         2/2/11 - Allow Excel export of single record from detail page.
 *         get/setter methods to excelDetailOk
 * 
 * 
 */

public abstract class Plugin {

	// also see DbFieldDate for mysql switch
	public static String dbprefix = ""; // sql server: "apaulsen."; //
	// "apaulsen."; //
	public String sql_len = "len"; // msqserver = "len"

	public String getListQuery() {
		return listQuery;
	}

	public String getSelectQuery() {
		return selectQuery;
	}

	public String mode = "";
	public String remedy_result = "&nbsp;";

	/*
	 * Spring Upgrade : new style of list selectors, arrays built by the
	 * constructor with no session info, then are converted to WebFields when
	 * list page is built!
	 */
	private BeanWebField[] listFilters = null;
	private BeanWebField[] webBeans = null;
	private forms.BeanList beanList = null;

	// 1/14/10 allow any kind of BeanField
	public void setListFilters(BeanWebField[] listFilters) {
		this.listFilters = listFilters;
	}

	public void setBeanList(BeanList beanList) {
		this.beanList = beanList;
	}

	public BeanList getBeanList() {
		return this.beanList;
	}

	public void setWebFieldBeans(BeanWebField[] beanList) {
		this.webBeans = beanList;
	}

	public Hashtable <String, WebField> getWebFields(String mode)
			throws services.ServicesException {

		Hashtable <String, WebField> ht = new  Hashtable();

		if (beanList != null) {
			//System.out.println("Plugin.java loading XML beans plugin");
			webBeans = beanList.getBeanFieldArray();
		}

		if (webBeans != null) {
			debug("loading ht from web XML beans");
			
			//debug (" the mode is ... ");

			//debug (mode);
			
			//debug(" ... should be " + webBeans.length);
			
			
			for (int x = 0; x < webBeans.length; x++) {
				//debug (" on bean # " + x);
				
				//debug(" .. plugin getWebFields.getWebField "
				// + webBeans[x].getWebFieldName());
				
				ht.put(webBeans[x].getWebFieldName(),
						webBeans[x].getWebField(sm, db, mode));
			
				//debug ("  bean # " + x + "done.");
				
			}
		}

		//debug("done get webfields");

		return ht;

	}

	/***************************************************************************
	 * 
	 * 
	 * STATIC FIELDS
	 * 
	 */

	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm");

	private static String[][] dummy_array = { { "A" }, { "Dummy" } };

	private static String constLimit = " "; // / LIMIT 1";

	/***************************************************************************
	 * 
	 * ALL SHOULD BE PRIVATE
	 * 
	 */

	public String keyName;

	public String tableName;

	private String selectQuery;

	private String listQuery;

	/*
	 * these permission don't have to do with user rights
	 */
	private boolean gotoOk = false;
	private boolean listOk = true;
	private boolean saveOk = true;
	private boolean cancelOk = true;
	private boolean excelOk = false;
	private boolean excelDetailOk = false;
	private boolean nextOk = false; // turn off by default
	private boolean remedyOk = false;
	private boolean reviewOk = true;
	private boolean printOk = false;
	private boolean listOnly = false;
	private boolean listPageButtonsCentered = false;

	/*
	 * these permissions have to do with user rights
	 * 
	 * Access Codes y = always ok n = always not ok r = root e = executive a =
	 * admin i = in-patient l = leader x = use project/application roles blank
	 * is default to user's project/application/division role
	 */

	private String addProfileCode = "";
	private String deleteProfileCode = "";
	private String editProfileCode = "";
	private String copyProfileCode = "";

	private boolean addCustomFields = false;
	private boolean isRootTable = false; // means there is no where higher to
	private boolean bKeyAutoIncrement = true;
	private boolean gotoKeyisInteger = true;
	private boolean logHistory = false;
	private boolean hasDetailForm = false; // creates a 'Detail' button on the
	private boolean isDetailForm = false; // causes the 'up' button to be
	private boolean isStepChild = false; // controls building of the select
	// and
	private boolean hasCustomButton = false;
	private boolean adminFunction = false; // * determines which toolbar to
	private boolean contextSwitchOk = true; // some forms,like Preference, you\
	private boolean forceSaveEdit = false; // to force user back to edit after
	private boolean showAuditSubmitApprove = true; // can turn off the
	private boolean listAscending = true;
	private boolean gotoKeyIsInteger = true;
	private boolean blockListRows = false;

	private String detailTargetLabel;
	private String detailTarget; // if this has a detail form, then must have
	// a
	private String parentTarget; // if is a detail, then must have parent

	private String parentTable;

	// target

	// these values are blank if unless override
	private String submitOk = "";

	private boolean showVCR = true; // allows the vcr buttons to be turned off

	public int iMaxListRows = 1000; // max rows on the list

	private String listViewName;

	private String selectViewName;

	private String targetTitle;
	private String templateName;

	public String formWriterType = ""; // gets set by the FormDataWriter during

	private String[] listHeaders;

	public String[] moreSelectColumns;

	public String[] moreSelectJoins;

	public String[] moreListColumns;

	public String[] moreListJoins;

	private boolean[] listSelectorColumnFlags;

	private boolean[] listCenterColumnFlags;

	private String[] listColumnWidths;

	/*
	 * Excel variables
	 */
	private String excelTemplateName = null;
	private int excelStartRow;
	private int excelMaxColumns;
	private String excelViewName;

	/***************************************************************************
	 * Internal results
	 */

	private String dataType = new String("");

	public Integer rowId;

	private String listOrder = "";

	public String listGroupBy = "";

	private String subTitle;

	private String gotoDisplayName;
	private String gotoKeyName = "reference_nm";

	/*
	 * allows the plugin to set javascript run from html onLoad event.
	 */
	private String scriptInit = null;

	public void setScriptInit(String javascript) {
		scriptInit = javascript;
	}

	public String getScriptInit() {
		if (scriptInit == null)
			return "";
		else
			return scriptInit;

	}

	public SessionMgr sm; // loaded in the constructor

	public DbInterface db;

	/*
	 * these fields control the history / audit logging of prior versions of a
	 * table
	 */
	public String setColumnName = null; // this is the key of the current table
	// entry
	public String seqColumnName = null; // this is zero for the current table

	// entry, then decending from 99999

	/*
	 * 
	 * START OF BEAN GET METODS
	 */

	public void setListPageCenterButtons(boolean b) {
		listPageButtonsCentered = b;
	}

	public boolean getListPageCenterButtons() {
		return listPageButtonsCentered;
	}

	public void setDataType(String s) {
		this.dataType = s;
	}

	public void setAddCustomFields(boolean customFields) {

		this.addCustomFields = customFields;
	}

	public void setKeyAutoIncrement(boolean b) {
		bKeyAutoIncrement = b;
	}

	public void setShowAuditSubmitApprove(boolean b) {
		this.showAuditSubmitApprove = b;
	}

	public boolean getShowAuditSubmitApprove() {
		return showAuditSubmitApprove;
	}

	public void setListSelectorColumnFlags(boolean[] flagArray) {
		listSelectorColumnFlags = flagArray;
	}

	public void setForceEditOnSave(boolean forceEditOnSave) {
		this.forceSaveEdit = forceEditOnSave;
	}

	public void setBlockList(boolean b) {
		this.blockListRows = b;
	}

	public void setHtmlCenterFlag(boolean[] flagArray) {
		listCenterColumnFlags = flagArray;
	}

	public void setListColumnWidths(String[] widths) {
		listColumnWidths = widths;
	}

	public void setIsRootTable(boolean b) {
		isRootTable = b;
	}

	public void setIsDetailForm(boolean b) {
		isDetailForm = b;
	}

	public void setTargetTitle(String s) {
		targetTitle = s;
	}

	public void setRemedyOk(boolean b) {
		remedyOk = b;
	}

	public void setDeleteOk(boolean b) {
		deleteProfileCode = b ? "Y" : "N";
	}

	public void setAddOk(boolean b) {
		addProfileCode = b ? "Y" : "N";
	}

	public void setPrintOk(boolean b) {
		printOk = b;
	}

	public boolean getPrintOk() {
		return printOk;
	}

	public void setUpdatesOk(boolean b) {
		addProfileCode = b ? "Y" : "N";
		deleteProfileCode = b ? "Y" : "N";
		copyProfileCode = b ? "Y" : "N";
		editProfileCode = b ? "Y" : "N";
	}

	public void setUpdatesLevel(String level) {
		deleteProfileCode = level;
		editProfileCode = level;
		copyProfileCode = level;
		addProfileCode = level;
	}

	public void setEditLevel(String l) {
		editProfileCode = l;
	}

	public void setDeleteLevel(String l) {
		deleteProfileCode = l;
	}

	public void setCopyLevel(String l) {
		copyProfileCode = l;
	}

	public void setSubmitOk(boolean submitOk) {
		if (submitOk)
			this.submitOk = "t";
		else
			this.submitOk = "f";

	}

	public void setShowVCR(boolean b) {
		showVCR = b;
	}

	public boolean getShowVCR() {
		return showVCR;
	}

	public void setHasDetailForm(boolean b) {
		hasDetailForm = b;
	}

	public void setContextSwitchOk(boolean b) {
		contextSwitchOk = b;
	}

	public void setGotoOk(boolean b) {
		gotoOk = b;
	}

	public void setIsStepChild(boolean b) {
		isStepChild = b;
	}

	public void setEditOk(boolean b) {
		editProfileCode = b ? "Y" : "N";
	}

	public void setCopyOk(boolean b) {
		copyProfileCode = b ? "Y" : "N";
	}

	public void setListOk(boolean b) {
		listOk = b;
	}

	public void setNextOk(boolean b) {
		nextOk = b;
	}

	public void setCancelOk(boolean b) {
		cancelOk = b;
	}

	public void setLogHistory(boolean b) {
		logHistory = b;
	}

	public void setExcelOk(boolean b) {
		excelOk = b;
	}

	public void setExcelDetailOk(boolean b) {
		excelDetailOk = b;
	}

	public void setIsAdminFunction(boolean b) {
		adminFunction = b;
	}

	public void setListAscending(boolean b) {
		listAscending = b;
	}

	public void setTableName(String s) {
		tableName = s;
	}

	public void setSelectQuery(String s) {
		selectQuery = s;
	}

	public void setListQuery(String s) {
		listQuery = s;
	}

	public void setSelectViewName(String s) {
		selectViewName = s;
	}

	public void setKeyName(String s) {
		keyName = s;
	}

	public void setExcelTemplate(String viewName, String templateName,
			int startRow, int maxColumns) {
		this.excelViewName = viewName;
		this.excelTemplateName = templateName;
		this.excelStartRow = startRow;
		this.excelMaxColumns = maxColumns;
	}

	public String getExcelTemplate() {
		return this.excelTemplateName;
	}

	public void setExcelTemplate(String templateName) {
		this.excelTemplateName = templateName;
	}

	public void setExcelView(String excelView) {
		this.excelViewName = excelView;
	}

	public void setExcelStartRow(int excelStartRow) {
		this.excelStartRow = excelStartRow;
	}

	public void setExcelMaxColumns(int excelMaxColumns) {
		this.excelMaxColumns = excelMaxColumns;
	}

	/*
	 * master/detail processing
	 */

	// allow plugin to do things prior to screen painting
	public void preDisplay() {

		// apaulsen 2/4/11 only set parent when showing current record
		try {
			if (db.hasRow()) {
				if (this.hasDetailForm
						&& sm.Parm("Action").equalsIgnoreCase("Show")) {
					sm.setParentId(db.getInteger(keyName),
							db.getText("title_nm"));
				}
			}
		} catch (Exception e) {
			debug("Plugin:preDisplay Exeption: " + e.toString());

		}
	}

	public void setDetailTarget(String target) {
		this.detailTarget = target;
	}

	public void setDetailTargetLabel(String targetLabel) {
		this.detailTargetLabel = targetLabel;
	}

	public void setParentTarget(String parentTarget) {
		this.parentTarget = parentTarget;

	}

	public void setParentTable(String parentTable) {
		sm.setParentTable(parentTable);
	}

	/*
	 * list processing
	 */

	public void setListViewName(String s) {
		listViewName = s;
	}

	public void setSubTitle(String s) {
		subTitle = s;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setListHeaders(String[] listHeaders) {
		this.listHeaders = listHeaders;
	}

	public void setMoreListJoins(String[] listJoins) {
		this.moreListJoins = listJoins;
	}

	public void setMoreListColumns(String[] listColumns) {
		this.moreListColumns = listColumns;
	}

	public void setListOrder(String listOrder) {
		this.listOrder = listOrder;
	}

	/*
	 * Detail Page Processing
	 */

	public void setMoreSelectColumns(String[] moreSelectColumns) {
		this.moreSelectColumns = moreSelectColumns;
	}

	public void setMoreSelectJoins(String[] moreSelectJoins) {
		this.moreSelectJoins = moreSelectJoins;
	}

	/*
	 * 
	 * START OF BEAN GET METODS
	 */

	public boolean getContextSwitchOk() {
		return contextSwitchOk;
	}

	public String getParentTarget() {
		return this.parentTarget;
	}

	public String getParentTable() {
		return sm.getParentTable();
	}

	public String getDataType() {
		return dataType;
	}

	public String getDetailTarget() {
		return detailTarget;
	}

	public String getDetailTargetLabel() {
		return detailTargetLabel;
	}

	public String getKeyName() {
		return keyName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getListViewName() {
		return listViewName;
	}

	public String getSelectViewName() {
		return selectViewName;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public boolean getIsRootTable() {
		return isRootTable;
	}

	public boolean getKeyAutoIncrement() {
		return bKeyAutoIncrement;
	}

	public boolean getForceEditOnSave() {
		return forceSaveEdit;
	}

	public boolean getAddOk() {

		return validateLevel(addProfileCode);
	}

	public boolean getEditOk() {
		// debug("plugin getEdit Ok, profile code : " + editProfileCode);
		return validateLevel(editProfileCode);
	}

	public boolean getDeleteOk() {
		return validateLevel(deleteProfileCode);
	}

	public boolean getCopyOk() {
		return validateLevel(copyProfileCode);
	}

	public boolean validateLevel(String accessLevel) {

		// honor explicit y/n settings first

		if (accessLevel.equalsIgnoreCase("Y"))
			return true;

		if (accessLevel.equalsIgnoreCase("N"))
			return false;

		if (accessLevel.equalsIgnoreCase("")) {
			return !getUserRoleCode().equalsIgnoreCase("brw");
		}

		if (accessLevel.equalsIgnoreCase("r")
				|| accessLevel.equalsIgnoreCase("root")) {
			return sm.userIsRoot();
		}
		if (accessLevel.equalsIgnoreCase("a")
				|| accessLevel.equalsIgnoreCase("administrator")) {
			return sm.userIsAdministrator();
		}
		if (accessLevel.equalsIgnoreCase("e")
				|| accessLevel.equalsIgnoreCase("executive")) {
			return sm.userIsExecutive();
		}
		if (accessLevel.equalsIgnoreCase("i")
				|| accessLevel.equalsIgnoreCase("inpatient")) {
			return sm.userIsIP_Leader();
		}
		if (accessLevel.equalsIgnoreCase("l")
				|| accessLevel.equalsIgnoreCase("leader")) {
			return sm.userIsLeader();
		}

		if (accessLevel.equalsIgnoreCase("c")
				|| accessLevel.equalsIgnoreCase("chg_aprv")) {
			return sm.userIsChgApprover();
		}

		return true;
	}

	public boolean getRemedyOk() {
		return remedyOk;
	}

	public boolean getGotoOk() {
		return gotoOk;
	}

	public boolean getListOk() {
		return listOk;
	}

	public boolean getNextOk() {
		return nextOk;
	}

	public boolean getCancelOk() {
		return cancelOk;
	}

	public boolean getExcelDetailOk() {
		return excelDetailOk;
	}

	public boolean getExcelOk() {
		return excelOk;
	}

	public boolean getIsDetailForm() {
		return isDetailForm;
	}

	public boolean getIsStepChild() {
		return isStepChild;
	}

	public boolean getHasDetailForm() {
		return hasDetailForm;
	}

	public boolean getLogHistory() {
		return logHistory;
	}

	public boolean getIsAdminFunction() {
		return adminFunction;
	}

	public boolean getListAscending() {
		return listAscending;
	}

	public Integer getParentKey() {
		return sm.getParentId();
	}

	/*
	 * 
	 * LEGACY METHODS .. GET RID OF THESE
	 * 
	 * ... in favor of GET/SET
	 */

	public boolean listColumnHasSelector(int columnNumber) {
		if (listSelectorColumnFlags != null) {
			if (listSelectorColumnFlags.length < columnNumber)
				return false;
			else
				return listSelectorColumnFlags[columnNumber];
		}

		else {
			if (listFilters != null) {
				// look in the listFilters array to find a match
				for (int x = 0; x < listFilters.length; x++) {
					if (listFilters[x].getListColumn() == columnNumber) {
						return true;
					}
				}
			}
			return false;
		}

	}

	public boolean getListColumnCenterOn(int columnNumber) {

		if (listCenterColumnFlags != null) {
			if (listCenterColumnFlags.length < columnNumber)
				return false;
			else
				return listCenterColumnFlags[columnNumber];
		} else
			return false;
	}

	public String getListColumnWidth(int columnNumber) {
		if (listColumnWidths != null) {
			if (listColumnWidths[columnNumber] == null) {
				return "";
			} else {
				return listColumnWidths[columnNumber];
			}

		}
		return "";
	}

	public String listBgColor(int columnNumber, String value, DbField[] fields) {
		return "";
	}

	public final void setGoToKeyIsInteger(boolean b) {
		gotoKeyIsInteger = b;
	}

	public void setGotoDisplayName(String s) {
		gotoDisplayName = s;
	}

	public String getGotoDisplayName() {
		return gotoDisplayName;
	}

	public void setGotoKeyName(String s) {
		gotoKeyName = s;
	}

	public String getGotoKeyName() {
		return gotoKeyName;
	}

	// getListSelector must be overriden to return a WebFieldSelect if
	// listColumnHasSelector == true for a particual column

	public WebField getListSelector(int columnNumber) {

		// check new Spring bean-style array first !

		if (this.listFilters != null) {
			// find the right one

			for (int x = 0; x < listFilters.length; x++) {
				if (listFilters[x].getListColumn() == columnNumber) {
					return listFilters[x].getWebField(sm, db, "list");
				}
			}

		}
		return new WebFieldSelect("X", "X", dummy_array);
	}

	/*
	 * Helper methods for getListSelector
	 */

	public WebField getListSelector(String filterName, Integer defaultValue,
			String titleName, Hashtable ht) {

		WebFieldSelect wf = new WebFieldSelect(filterName, sm.Parm(filterName)
				.length() == 0 ? defaultValue
				: new Integer(sm.Parm(filterName)), ht, titleName);

		wf.setDisplayClass("listform");
		return wf;
	}

	public WebField getListSelector(String filterName, String defaultValue,
			String titleName, Hashtable ht) {

		WebFieldSelect wf = new WebFieldSelect(filterName, sm.Parm(filterName)
				.length() == 0 ? defaultValue : sm.Parm(filterName), ht,
				titleName);

		wf.setDisplayClass("listform");
		return wf;
	}

	public String getCustomButton() {
		return "";
	}

	// tresponsibility that are a sub-form
	// of a parent, they don't have a root
	// key like project_id

	// so List title name can be dynamic
	public String getListTitle() {
		return targetTitle;
	}

	/***************************************************************************
	 * each dataManager mus populate these
	 */

	public String getMenuName(String action) {

		if (this.adminFunction) {
			return "admin";
		}

		// application changes are always in the context of a project;
		// .. and the project was set before coming here.
		if (this.dataType.equalsIgnoreCase("Application")) {
			return sm.getProjectMenu();
		}

		//
		if (sm.Parm("Action").equalsIgnoreCase("list")) {
			return sm.getProjectMenu();
		}

		// type is project.. should have menu_cd in the rs.
		// ... and it will depend on which process_id is attached to the
		// projecct
		// if (this.dataType.equalsIgnoreCase("project") && hasRow()) {
		// debug("getting menu from project rs : " + db.getText("menu_cd"));
		// return db.getText("menu_cd");
		// }

		return "large";

	}

	/*
	 * get database product type from session,
	 */

	public String getDatabaseProduct() {
		return (String) sm.getDatabaseProduct();
	}

	// public String selectQuery;

	// realtime filter to limit the scrolling, and fine-tune the list page
	public String getNavigationFilter() {
		return "";
	}

	// used to fine-tune the list pages
	public String getListAnd() {

		// debug("plugin: gegtLisgtand()");

		if (this.listFilters != null) {
			// find the right one
			StringBuffer sb = new StringBuffer();

			for (int x = 0; x < listFilters.length; x++) {
				sb.append(listFilters[x].getListQueryFilter(sm));
			}

			// ALlow blocking of list
			if (this.blockListRows) {
				sb.append(" AND 1 = 0 ");
			}
			return sb.toString();
		}

		return "";
	}

	public String getCustomSubForm() {
		return new String("");
	}

	// Plugin must create the file, and return the file name (minus the path)

	public String makeExcelFile() {
		return getExcelFile();
	}

	// create Excel from ResultSet and save to the path in the web.xml config
	// file
	public String getExcelFile() {

		ExcelWriter excel = new ExcelWriter();
		short startRow = (short) excelStartRow;
		String filePrefix = sm.getLastName()
				+ "_"
				+ excelTemplateName
						.substring(0, excelTemplateName.length() - 4);

		String excelFileName = excel.appendWorkbook(sm.getExcelPath(),
				sm.getWebRoot() + "excel/" + excelTemplateName, filePrefix,
				getExcelRS(), startRow, excelMaxColumns);

		return excelFileName;

	}

	public ResultSet getExcelRS() {
		
		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM " + excelViewName + " WHERE 1=1 ");

		// just get single row
		
		if ( mode != null && mode.equalsIgnoreCase("show"))
			sb.append(" and " + keyName + " = " + sm.Parm("RowKey"));
		else
			sb.append(getListAnd());

		if (dataType.equalsIgnoreCase("project")) {
			sb.append(" and project_id = " + sm.getProjectId().toString());
		}

		// Filter Remedy status - default to no filter

		if (this.listOrder.length() > 2) {
			sb.append(" ORDER BY  " + this.listOrder);
		}

		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}

		return rs;

	}

	/*
	 * All the data managers implement this
	 */

	/*
	 * Web display pages (add, edit, show) call this to get 1. gets the
	 * hashtable of web fields from data manager 2. adds in the custom fields 3.
	 * converts ht back to array for the display page
	 */

	public WebField[] getWebFieldArray(String mode)
			throws services.ServicesException {

		Hashtable ht = getWebFieldHashtable(mode);

		/*
		 * .. then put it back to an arry for the display forms
		 */

		WebField[] wf = new WebField[ht.size()];
		Enumeration en = ht.elements(); // java 1.5

		int i = 0;

		while (en.hasMoreElements()) {
			// wf[i] = en.nextElement(); // java 1.5
			wf[i] = (WebField) en.nextElement(); // java 1.4
			i++;
		}
		return wf;
	}

	/*
	 * 
	 * 
	 */

	public Hashtable getWebFieldHashtable(String mode)
			throws services.ServicesException {
		/*
		 * data managers return a ht
		 */
		Hashtable ht = getWebFields(mode);

		/*
		 * .. so we can easily add custome fields to it
		 */
		if (addCustomFields) {
			addCustomFields(mode, ht);
		}
		return ht;
	}

	/***************************************************************************
	 * 
	 * Miscellenaous Methods for Sub-Class to Override
	 * 
	 * @return
	 */

	public boolean afterGet() {

		// setUpdatesOk(!getUserRoleCode().equalsIgnoreCase("brw"));

		return true;
	}

	public boolean beforeAdd(Hashtable<String, DbField> ht) {
		return true;
	}

	public void beforeUpdate(Hashtable<String, DbField> ht) {
		return;
	}

	public void afterUpdate(Integer rowKey) throws services.ServicesException {
		return;
	}

	public void afterAdd(Integer rowKey) throws services.ServicesException {
		return;
	}

	public void afterDelete(Integer rowKey) throws services.ServicesException {
		return;
	}

	// ********************
	// CONSTRUCTORS
	// ********************

	public Plugin() {

	}

	public void init(SessionMgr parmSm) {
		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection

	}

	// ***************************************
	// * METHODS FOR LIST PAGE FILTERS:
	// ***************************************

	// Does the use have access to any projects / application
	// false supresses 'New' on the List page

	// TODO : find a way to get the user role in list mode without a db call
	public String getUserRoleCode() {

		if (dataType.equalsIgnoreCase("project")) {
			return sm.getProjectRole();
		}

		if (dataType.equalsIgnoreCase("application")) {
			return sm.getApplicationRole();
		}

		// hack.. TODO: fix divison
		if (dataType.equalsIgnoreCase("division"))
			return "CON";

		if (sm.userIsAdministrator())
			return "ADM";

		return new String("BRW");

	}

	// this allows the plugin to override the form display sequence
	// for example, change sequence add/show to add/add if there is logical
	// error on form

	public String getDataFormName() {

		if (sm.Parm("Action").equalsIgnoreCase("goto")
				&& rowId.equals(new Integer("-1"))) {
			return "list";
		}
		return "";
	}

	public void setTemplateName(String template) {
		this.templateName = template;
	}

	public final String getTemplateName() {

		if (templateName == null) {
			return (String) sm.getTarget() + ".html";
		} else
			return templateName;

	}

	// ***************************************
	// * PUBLIC METHODS
	// ***************************************

	public boolean getSaveOk() {
		return saveOk;
	}

	public void setSaveOk(boolean b) {
		saveOk = b;
	}

	public String[] getListHeaders() {
		return this.listHeaders;
	}

	// prevent drill-downs from the list page to a template/form.
	public boolean getListOnly() {
		return listOnly;
	}

	public void setListOnly(boolean b) {
		listOnly = b;
	}

	public boolean listOk() {
		return listOk;
	}

	/*
	 * RULE: Can submit if user is not a browser, ... and not already submitted.
	 */

	public boolean getSubmitOk() {

		if (this.submitOk.equalsIgnoreCase("")) {
			// value never, so use this default logic
			if (!showAuditSubmitApprove) {
				return false;
			}
			return (!getUserRoleCode().equalsIgnoreCase("brw") && !getIsSubmitted());
		} else
			// value was set, so return that set value
			return this.submitOk.equalsIgnoreCase("t");

	}

	public void setReviewOk(boolean b) {
		reviewOk = b;
	}

	public boolean getReviewOk() {

		if (!showAuditSubmitApprove) {
			reviewOk = false;
		} else {
			reviewOk = ((getUserRoleCode().equalsIgnoreCase("rev")
					|| getUserRoleCode().equalsIgnoreCase("apr") || getUserRoleCode()
					.equalsIgnoreCase("adm")) && !isPassReview() && getIsSubmitted());
		}
		return reviewOk;
	}

	public boolean unReviewOk() {

		if (!showAuditSubmitApprove) {
			return false;
		}
		return (getUserRoleCode().equalsIgnoreCase("apr") || getUserRoleCode()
				.equalsIgnoreCase("rev")) && isPassReview();
	}

	/*
	 * Return true if record is approved (not null and not equal 0)
	 */

	public boolean getIsUpdated() {

		Integer i = db.getInteger("updated_uid");
		if (i == null) {
			return false;
		}
		if (i.compareTo(new Integer("0")) == 0) {
			return false;
		}
		return true;
	}

	public boolean getIsSubmitted() {

		if (!showAuditSubmitApprove) {
			return false;
		}
		Integer i = db.getInteger("submitted_uid");
		if (i == null) {
			return false;
		}
		if (i.compareTo(new Integer("0")) == 0) {
			return false;
		}
		return true;
	}

	public boolean getIsReviewed() {

		if (!showAuditSubmitApprove) {
			return false;
		}
		String s = db.getText("reviewed_flag");

		if (s == null)
			return false;

		if (s.equalsIgnoreCase("") || s.equalsIgnoreCase("n")
				|| s.equalsIgnoreCase("p"))
			return false;

		return true;
	}

	public boolean isPendingReview() {

		if (!showAuditSubmitApprove) {
			return false;
		}
		String s = db.getText("reviewed_flag");

		if (s == null)
			return false;

		if (s.equalsIgnoreCase("p"))
			return true;

		return false;
	}

	public boolean isPassReview() {

		if (!showAuditSubmitApprove) {
			return false;
		}
		String s = db.getText("reviewed_flag");

		if (s == null)
			return false;

		if (s.equalsIgnoreCase("Y"))
			return true;

		return false;
	}

	public boolean isFailReview() {

		if (!showAuditSubmitApprove) {
			return false;
		}
		String s = db.getText("reviewed_flag");

		if (s == null)
			return false;

		if (s.equalsIgnoreCase("R"))
			return true;

		return false;
	}

	public Integer setRow(String parmRowId) throws services.ServicesException {

		String query = getSelectWord() + getSelectQuery() + " where "
				+ (this.selectViewName == null ? tableName : selectViewName)
				+ "." + keyName + " = " + parmRowId + getLimitWord();
		return db.setRow(tableName, keyName, query);
	}

	public Integer setRowRelation(String parmRowKey, String parmRelation)
			throws services.ServicesException {

		String whereClause = buildWhereFromAction(parmRowKey, parmRelation);
		String selectQuery = getSelectWord() + getSelectQuery();

		Integer i = db.setRow(tableName, keyName, selectQuery + whereClause
				+ getLimitWord());

		afterGet();
		return i;

	}

	private String getSelectWord() {

		if (sm.isSQLServer())
			return " SELECT TOP 1 ";
		else
			return " SELECT ";
	}

	private String getLimitWord() {
		if (sm.isSQLServer())
			return " ";
		else
			return " LIMIT 1 ";

	}

	public String getSQl_null_date_lit() {
		if (sm.isSQLServer())
			return " NULL ";
		else
			return " '0000-00-00' ";

	}

	// used by UserPlugin
	public Integer setRowWhere(String parmWhereClause)
			throws services.ServicesException {
		return db.setRow(tableName, keyName, getSelectWord() + getSelectQuery()
				+ parmWhereClause);
	}

	public int deleteRow(String parmRowKey) throws services.ServicesException {
		int rc = db.deleteRow(tableName, keyName, parmRowKey);

		if (rc == 0) {
			afterDelete(Integer.parseInt(parmRowKey));
		}
		return rc;
	}

	// ********************
	// Public Get methods
	// ********************

	public boolean getHasRow() {
		return db.hasRow();
	}

	public String getText(String parmField) {
		return db.getText(parmField);
	}

	public Object getObject(String parmField) {
		return db.getObject(parmField);
	}

	public Integer getKey() {
		return db.getKey();
	}

	// returns a hashtable of rows to print on the list page
	public Hashtable getList() throws services.ServicesException {
		String listQuery = getListQuery();

		String groupBy = "";
		String orderBy = "";

		if (listGroupBy.length() > 0) {
			groupBy = " GROUP BY " + listGroupBy;

		}
		if (listOrder.length() > 0) {
			orderBy = " ORDER BY " + listOrder + (listAscending ? "" : " DESC");
		}

		return db.getList(tableName, keyName, listQuery + " and "
				+ ((this.listViewName == null) ? tableName : listViewName)
				+ "." + keyName + " > 0 " + getListAnd() + groupBy + orderBy);

	}

	public Hashtable getLookupTable(String parmTableName, String descColumn,
			String valueColumn) throws services.ServicesException {

		return db.getLookupTable(parmTableName, descColumn, valueColumn);
	}

	public Hashtable getLookupTable(String parmTableName, String descColumn,
			String valueColumn, String parmOrderBy)
			throws services.ServicesException {

		return db.getLookupTable(parmTableName, descColumn, valueColumn,
				parmOrderBy);
	}

	// ************************************
	// PUBLIC SET
	// ************************************

	/*
	 * this method allows a custom routin to be called...
	 * 
	 * Implemented in ProjectPlan to allow uploading files.
	 */
	public void customAction() {
		return;
	}

	public int runQuery(String query) throws services.ServicesException {
		return db.runQuery(query);
	}

	public void doCustomRoutine(SessionMgr sm) {

	}

	public String getRemedy() {

		remedy_result = getRemedy(this.tableName, this.keyName,
				Integer.parseInt(sm.Parm("remedyno")),
				Integer.parseInt(sm.Parm("RowKey")));

		return remedy_result;

	}

	// paulsen 7/26
	// new entry to handle tremedy data in separate table from target record

	public String getRemedy(String tableName, String keyName, int rfc_sr_no,
			int rowKey) {

		String formatted_chg_id = formatChangeId(rfc_sr_no);

		RemedyChangeQuery remedy = new RemedyChangeQuery(sm.getRemedyUserid(),
				sm.getRemedyPassword(), sm.getRemedyURL());

		String xml = remedy.GetChangeInfo(formatted_chg_id); // get rfc to

		if (remedy.getSuccess() == false) {
			return "Bad call to Remedy server.";
		}

		RemedyChangeRequest rfc = new RemedyChangeRequest(xml);

		if (rfc.getChangeCount() == 0) {
			return "Matching Remedy ticket not found.";
		} else {
			rfc.rfcToDatabase(sm.getConnection(), tableName, keyName, rowKey);
		}

		/*
		 * Elective Attributes
		 */

		remedy.RemedyAttributeInfo remedyAttributes = new remedy.RemedyAttributeInfo(
				sm.getRemedyUserid(), sm.getRemedyPassword(), sm.getRemedyURL());

		String xml2 = remedyAttributes.GetAttributeInfo(formatted_chg_id);

		rfc.initAttributeInfo(xml2, tableName);

		if (rfc.getAttributeCount() > 0) {
			debug("..updating elective attributes");
			rfc.attributesToDatabase(sm.getConnection(), tableName, keyName,
					rowKey);
		}

		/*
		 * Comments Log
		 */

		remedy.RemedyCommentQuery remedyComments = new remedy.RemedyCommentQuery(
				sm.getRemedyUserid(), sm.getRemedyPassword(), sm.getRemedyURL());

		String xml3 = remedyComments.GetCommentInfo(formatted_chg_id);

		rfc.initCommentInfo(xml3);

		rfc.commentsToDatabase(sm.getConnection(), tableName, keyName, rowKey);

		return "Remedy info saved.";

	}

	private String formatChangeId(int rfcNo) {

		// todo: push the rfc_no out on the list page
		String chg_id = "" + rfcNo;

		while (chg_id.length() < 12)
			chg_id = "0" + chg_id;

		chg_id = "CHG" + chg_id;

		return chg_id;

	}

	public Integer gotoRow() throws services.ServicesException {

		rowId = new Integer("-1");

		String sql = "";

		if (gotoKeyIsInteger) {
			sql = "SELECT " + keyName + " FROM " + tableName + " WHERE "
					+ gotoKeyName + " = " + sm.Parm("gotoKey");

		} else {
			sql = "SELECT " + keyName + " FROM " + tableName + " WHERE "
					+ gotoKeyName + " =  '" + sm.Parm("gotoKey") + "'";

		}

		rowId = db.getRSInt(sql);

		return rowId;

	}

	public Integer addRow() throws services.ServicesException {

		Hashtable<String, DbField> ht = getRequestParmHT();

		// CAUTION...9/9/07 default to placeholder record, only a few tables
		// have -1 seed value
		rowId = new Integer("-1");

		// sqlserver .. check for a separate table with audit fields

		ht.put("added_uid", new DbFieldInteger("added_uid", sm.getUserId()));
		ht.put("added_date", new DbFieldDateTime("added_date", new Date()));

		boolean rc = beforeAdd(ht);

		if (rc == true) {
			rowId = db.insertRow(tableName, keyName, ht, bKeyAutoIncrement);
		}

		/*
		 * Return -1 if duplicate
		 */

		if (rowId.equals(new Integer("-1"))) {
			return rowId;
		}

		// don't do afterAdd if bad rc.
		if (rc)
			afterAdd(rowId);

		// setRow is already done by either the edit page (save-then-edit
		// option),
		// or the show box (save from new screen). No need to do it twice!

		// setRow(newRowKey.toString());
		return rowId;
	}

	// set the approve date and approve by fields
	public Integer unSubmitRow() throws services.ServicesException {

		String sql = " update " + tableName
				+ " set submitted_uid = 0, submitted_date = "
				+ getSQl_null_date_lit() + "' where " + keyName + " = "
				+ sm.Parm("RowKey");
		db.runQuery(sql);
		return (new Integer("0"));

	}

	// set the approve date and approve by fields
	public Integer SubmitRow() throws services.ServicesException {

		String sql = " update " + tableName
				+ " set reviewed_flag = 'P',  reviewed_uid = 0  where "
				+ keyName + " = " + sm.Parm("RowKey");
		db.runQuery(sql);
		return (new Integer("0"));
	}

	// set the approve date and approve by fields
	public Integer unReviewRow() throws services.ServicesException {

		String sql = " update " + tableName
				+ " set reviewed_uid = 0, reviewed_date =  + "
				+ getSQl_null_date_lit() + " where " + keyName + " = "
				+ sm.Parm("RowKey");
		db.runQuery(sql);
		return (new Integer("0"));

	}

	public Integer doAction(String parmAction)
			throws services.ServicesException {

		if (parmAction.equalsIgnoreCase("list")) {
			return new Integer("0");
		}

		// first get the rowId from the parm, keep it
		try {
			rowId = new Integer(sm.Parm("RowKey"));
		} catch (NumberFormatException e) {
			rowId = new Integer(0);
		}

		// post the 'add' page, and fetch the new rowId
		if (parmAction.equalsIgnoreCase("goto")) {
			rowId = gotoRow();
		}

		// post the 'add' page, and fetch the new rowId
		if ((parmAction.equalsIgnoreCase("post"))
				|| (parmAction.equalsIgnoreCase("postThenEdit"))) {
			rowId = addRow();
		}

		// save the 'edit' pages
		if (parmAction.toLowerCase().endsWith("save")) {
			int updateRc = updateRow();
		}

		// copy to new row
		if (parmAction.equalsIgnoreCase("copy")) {
			rowId = copyRow();
		}

		// un-submit the record... gotcha... the rc must be the rowKey, not 0
		if (parmAction.equalsIgnoreCase("unsubmit")) {
			Integer ignoreIt = unSubmitRow();
		}

		// un-submit the record... gotcha... the rc must be the rowKey, not 0
		if (parmAction.equalsIgnoreCase("submitsave")) {
			Integer ignoreIt = SubmitRow();
		}

		// un-review the record... gotcha... the rc must be the rowKey, not 0
		if (parmAction.equalsIgnoreCase("unreview")) {
			Integer ignoreIt = unReviewRow();
		}

		if (parmAction.equalsIgnoreCase("delete")) {
			deleteRow(sm.Parm("RowKey"));
		}

		if (parmAction.equalsIgnoreCase("custom")) {
			doCustomRoutine(sm);
		}

		if (parmAction.equalsIgnoreCase("remedy")) {
			getRemedy();
		}

		return rowId;

	}

	/***************************************************************************
	 * 
	 * Update a row based on HTML input form
	 * 
	 * 1. get the ht of fields to update 2. add in the updated id and date 3.
	 * update the table 4. call the 'afterUpdate' routine to let data managers
	 * to other processing
	 * 
	 **************************************************************************/

	public int updateRow() throws services.ServicesException {

		if (logHistory) {
			db.logRow(tableName, keyName, sm.Parm("RowKey"), setColumnName,
					bKeyAutoIncrement);
		}

		// returns a ht of DbFields which are populated with values on the web
		// page
		Hashtable<String, DbField> ht = getRequestParmHT();

		ht.put("updated_uid", new DbFieldInteger("updated_uid", sm.getUserId()));
		ht.put("updated_date", new DbFieldDateTime("updated_date", new Date()));

		/*
		 * allow the plugin to add in optional other fields .. like
		 * closed_by_uid and close_date in issues
		 */
		beforeUpdate(ht);

		int rc = db.updateRow(tableName, keyName, sm.Parm("RowKey"), ht);

		afterUpdate(new Integer(sm.Parm("RowKey"))); // allow the sub-class

		return rc;
	}

	public Integer copyRow() throws services.ServicesException {

		rowId = db.copyRow(tableName, keyName, new Integer(sm.Parm("RowKey")),
				sm.getUserId(), bKeyAutoIncrement);

		afterAdd(rowId);

		return rowId;
	}

	/*
	 * 
	 * Loop through the form variables and construct a ht of DbField's
	 * 
	 * Fuzzy logic is used here to determine which fields are database columns,
	 * and which are just form fields, like action, target, rowkey.
	 * 
	 * 1. Form fields have no '_' in the tag name 2. Database fields usually
	 * have an '_', except 'title', 'notes', etc. 3. reference_id is really a
	 * string, will fix all someday 4. fields that end in '_id' and '_uid' are
	 * integers 5. everything else is string( varchar, blob, or char)
	 */
	private Hashtable<String, DbField> getRequestParmHT() {

		Hashtable<String, DbField> ht = new Hashtable<String, DbField>();

		Enumeration<String> en = sm.getRequestParmNames();

		while (en.hasMoreElements()) {

			String fieldName = (String) en.nextElement();
			// add the field to the ht... if it's a form element....

			if (fieldName.equalsIgnoreCase("submitted_by")) {
				ht.put("submitted_tx",
						new DbFieldString("submitted_tx", sm
								.Parm("submitted_by")));
				ht.put("submitted_uid",
						new DbFieldInteger("submitted_uid", sm.getUserId()));
				ht.put("submitted_date", new DbFieldDateTime("submitted_date",
						new Date()));

			} else {
				if (fieldName.equalsIgnoreCase("reviewed_by")) {

					ht.put("reviewed_tx",
							new DbFieldString("reviewed_tx", sm
									.Parm("reviewed_by")));
					ht.put("reviewed_uid", new DbFieldInteger("reviewed_uid",
							sm.getUserId()));
					ht.put("reviewed_date", new DbFieldDateTime(
							"reviewed_date", new Date()));
				} else {
					if (fieldName.equalsIgnoreCase("approved_by")) {

						ht.put("approved_tx", new DbFieldString("approved_tx",
								sm.Parm("approved_by")));
						ht.put("approved_uid", new DbFieldInteger(
								"approved_uid", sm.getUserId()));
						ht.put("approved_date", new DbFieldDateTime(
								"approved_date", new Date()));

					} else {
						// todo: append "_tx" to these column names
						if (fieldName.equalsIgnoreCase("handle")
								|| fieldName.equalsIgnoreCase("password")) {
							DbFieldString fs = new DbFieldString(fieldName);
							fs.setValue(sm.Parm(fieldName).toUpperCase());
							ht.put(fieldName, fs);
						} else {
							if ((fieldName.endsWith("_date"))
									|| (fieldName.endsWith("_dt"))) {
								DbFieldDate fd = new DbFieldDate(fieldName);
								// debug("date..sql server : " +
								// sm.isSQLServer());
								fd.setValue(sm.Parm(fieldName));
								ht.put(fieldName, fd);
							} else {
								if ((fieldName.indexOf("_") == -1)
										|| (fieldName.indexOf("remedy") != -1)) {
									// drop the field if no "_" - all db
									// field
									// names
									// should have an underscore !!!!
									// otherwise, the request parm came from
									// the
									// form
									// post,
									// and is not a database
									// field probably something like
									// 'target',
									// or
									// 'action'
								} else {
									if (fieldName.endsWith("_id")
											|| fieldName.endsWith("_uid")
											|| fieldName.endsWith("_hrs")
											|| fieldName.endsWith("_amt")
											|| fieldName.endsWith("_pct")
											|| fieldName.endsWith("_no")
											|| fieldName.endsWith("_qty")) {
										DbFieldInteger fi = new DbFieldInteger(
												fieldName);
										fi.setValue(sm.Parm(fieldName));
										ht.put(fieldName, fi);
									} else {
										if (fieldName.endsWith("_dttm")) {
											DbFieldDateTime fd = new DbFieldDateTime(
													fieldName);
											fd.setValue(sm.Parm(fieldName));
											ht.put(fieldName, fd);
										} else {
											if (fieldName.endsWith("_flt")) {
												DbFieldFloat flt = new DbFieldFloat(
														fieldName);
												flt.setValue(sm.Parm(fieldName));
												ht.put(fieldName, flt);
											} else {
												if (fieldName
														.endsWith("_gulp_not_used")) {
													DbFieldBinary bin = new DbFieldBinary(
															fieldName);
													bin.setValue(sm
															.Parm(fieldName));
													ht.put(fieldName, bin);
												} else {
													DbFieldString ds = new DbFieldString(
															fieldName);
													ds.setValue(sm
															.Parm(fieldName));
													ht.put(fieldName, ds);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return ht;

	}

	/*
	 * takes a WebFieldArray from the Plugin and puts it to a ht note... the ht
	 * key doesn't matter, just needs to be unique
	 * 
	 * this is here for legacy Plugins which returned WebField arrays instead of
	 * HashTables
	 */

	public Hashtable<String, WebField> webFieldsToHT(WebField[] pWebFieldArray) {

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		for (int i = 0; i < pWebFieldArray.length; i++) {
			ht.put(pWebFieldArray[i].webFieldId, pWebFieldArray[i]);
		}
		return ht;
	}

	/*
	 * Date routines
	 */

	// 9/18/08 use plugin 'listOrder' instead of key name on the next function
	private String buildWhereFromAction(String parmRowKey, String parmRelation) {

		String whereClause;
		switch (parmRelation.toLowerCase().charAt(0)) {

		case 't': // this record
		case 's': // show record
		case ' ': // default
			whereClause = new String(" WHERE "
					+ (this.selectViewName == null ? tableName
							: this.selectViewName) + "." + keyName + " = "
					+ parmRowKey + getNavigationFilter());
			break;
		case 'n': // next record
			whereClause = new String(" WHERE "
					+ (this.selectViewName == null ? tableName
							: this.selectViewName) + "." + keyName + " > "
					+ parmRowKey + getNavigationFilter() + " ORDER BY "
					+ (listOrder.length() > 0 ? listOrder : keyName)
					+ constLimit); // listOrder instaed of
			// 'keyName'
			break;
		case 'f': // first record
			whereClause = new String(" ORDER BY " + keyName + constLimit);
			break;
		case 'p': // previous record
			whereClause = new String(" WHERE " + tableName + "." + keyName
					+ " < " + parmRowKey + getNavigationFilter() + " ORDER BY "
					+ keyName + "  DESC " + constLimit);
			break;
		case 'l': // last record
			whereClause = new String(" WHERE 1 = 1 " + getNavigationFilter()
					+ " ORDER BY " + tableName + "." + keyName + "  DESC "
					+ constLimit);
			break;
		default: // invalid relation
			whereClause = new String("");
		}

		return whereClause;
	}

	public void debug(String debugMsg) {
		if (sm != null)
			sm.debug(debugMsg);
		else
			debug(debugMsg);
		// Logger.getLogger("ui4sql").debug(debugMsg);
	}

	/*
	 * 
	 * Routines to add custom fields to the ht
	 */

	/*
	 * Now get the phase-specific questions *
	 */

	public Hashtable addCustomFields(String parmMode, Hashtable ht)
			throws services.ServicesException {

		try {
			ResultSet rs = getCustomFields();

			while (rs.next()) {

				/*
				 * question rs: 1. db_field_nm 2. field_type_cd 3. field_length,
				 * 4. code_type_nm (only for selector types 's' 5.
				 * html_prompt_nm 6. html_prompt_tx
				 */

				if ((rs.getString(1).length() > 0)) {
					ht.put((String) rs.getString(1),
							getWebField(parmMode, rs.getString(1),
									rs.getString(2), rs.getString(4),
									rs.getInt(3)));
				}

				/*
				 * add in html promt field, if not null
				 */

				if ((rs.getString(5).length() > 0)) {
					ht.put(rs.getString(5), new WebFieldDisplay(
							rs.getString(5), rs.getString(6)));

				}

			}
			rs.close();
		} catch (Exception e) {
			debug("ht exception" + e.toString());
		}

		return ht;

	}

	/*
	 * return the right type of web field, as per the custom fields type, and
	 * populate with the web form data
	 */
	private WebField getWebField(String parmMode, String dbFieldName,
			String fieldType, String codeType, int fieldLength) {

		/*
		 * if web page is in add mode, then return empty string, don't fetch
		 * from rs (there is none)
		 */
		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		/*
		 * Select / Choice WebField
		 */
		if (fieldType.equalsIgnoreCase("c")) {
			return new WebFieldSelect(dbFieldName, addMode ? ""
					: db.getText(dbFieldName), sm.getCodes(codeType), false,
					true);
		}

		/*
		 * Blob
		 */

		if (fieldType.equalsIgnoreCase("b")) {
			return new WebFieldText(dbFieldName, addMode ? ""
					: db.getText(dbFieldName), 4, 80);
		}

		/*
		 * TODO: add logic for Integer, etc., as needed. Default is
		 * WebFieldSelect
		 */
		return new WebFieldString(dbFieldName, addMode ? ""
				: db.getText(dbFieldName), fieldLength, fieldLength);
	}

	private ResultSet getCustomFields() throws services.ServicesException {

		/*
		 * Get a rs of the custom form fields
		 */
		String customSubForm = new String("");
		if (getCustomSubForm().length() > 0) {
			customSubForm = " where form_subgroup = '" + getCustomSubForm()
					+ "' ";
		}

		ResultSet rs = db
				.getRS("select db_field_nm, field_type_cd, field_length, code_type_nm, html_prompt_nm, html_prompt_tx "
						+ " from tfield "
						+ " join tform on tfield.form_id = tform.form_id and tform.table_nm = '"
						+ this.tableName
						+ "' "
						+ customSubForm
						+ " ORDER BY html_prompt_nm");

		return rs;

	}

}
