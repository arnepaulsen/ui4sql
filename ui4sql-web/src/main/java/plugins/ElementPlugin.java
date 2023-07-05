/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import router.SessionMgr;
import db.DbFieldString;
import db.DbFieldInteger;

/*******************************************************************************
 * Procedure Element Plugin (cloned from StepPlugin)
 * 
 * Step and Element Plugins are more complicate than most detail forms because
 * the can multiple parents, distinguished by parent_kind_cd and parent-key
 * columns.
 * 
 * Unlike StepPlugin, Element parents are always type Application, so some of
 * the logic in here is not necessary, like the 'grandParentName', we know it's
 * always an Application, but left this stuff in here.
 * 
 * Elements is used by:
 * 
 * 1. message, 2. file 3. table
 * 
 * 
 * Change Log:
 * 
 * 9/8 added 8/25/06 - SQLSERVER : remove 'select', plugin will put SELECT TOP 1
 * if sql-server
 * 
 * 
 ******************************************************************************/
public class ElementPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	/*
	 * these are
	 */
	private String elementKindCd = ""; // the kind of element, like

	// WorkFlow(W),

	private String parentTableName = ""; // like tworkflow

	private String grandParentTableName = ""; // always 'tapplications'

	private String grandParentKeyName = "";

	private String parentKeyName = ""; // like workflow_id

	private String filterName = ""; // like 'FilterWorkflow'

	private String rootTitle = "";

	public ElementPlugin() throws services.ServicesException {
		super();

		this.setTableName("telement");
		this.setKeyName("element_id");
		this.setTargetTitle("Elements");

		this.setIsStepChild(true);
		this.setIsDetailForm(true);
		this.setListOrder("seq_no");
		this.setContextSwitchOk(false); // can't change down here

	}

	public void init(SessionMgr parmSm) {

		/*
		 * we have to wait for the FormData to run init to push the sm into the dm
		 * object
		 * 
		 */

		// debug("ElementPlugin:init()");

		this.sm = parmSm;
		this.db = sm.getDbInterface(); // has an open connection

		elementKindCd = sm.getStructureType().substring(0, 1);
		this.setParentTarget(sm.getStructureType());
		filterName = "Filter" + getParentTarget();
		parentKeyName = sm.getStructureType().toLowerCase() + "_id";
		parentTableName = "t" + sm.getStructureType().toLowerCase();

		System.out.println("ElementPlugin:Init - sm.getStructureType: " + sm.getStructureType().toLowerCase());

		this.grandParentTableName = "tapplications";
		this.grandParentKeyName = "application_id";
		rootTitle = "application_name";

		this.setAddOk(myAddOk());

		this.setListHeaders(new String[] { "Seq", "Title", "Version", "Status", getParentTarget() });

		this.setMoreListColumns(new String[] { "seq_no", "telement.title_nm", "telement.version_id",
				"s.code_desc as status_desc", parentTableName + ".title_nm as thing_name" });

		this.setMoreListJoins(
				new String[] { " left join tcodes s on telement.status_cd = s.code_value and s.code_type_id  = 60 ",
						"  join " + parentTableName + " on telement.parent_id = " + parentTableName + "."
								+ parentKeyName,
						" left join " + grandParentTableName + " on " + parentTableName + "." + grandParentKeyName
								+ " = " + grandParentTableName + "." + grandParentKeyName + " " });

		this.setMoreSelectColumns(new String[] { parentTableName + ".title_nm as thing_name",
				grandParentTableName + "." + grandParentKeyName });

		this.setMoreSelectJoins(new String[] {
				"  join " + parentTableName + " on telement.parent_id = " + parentTableName + "." + parentKeyName,
				" left join " + grandParentTableName + " on " + parentTableName + "." + grandParentKeyName + " = "
						+ grandParentTableName + "." + grandParentKeyName + " " });

	}

	/*
	 * turn off the 'New' button on the list page if there is no parent selected
	 */
	private boolean myAddOk() {

		if (sm.getApplicationRole().equalsIgnoreCase("brw"))
			return false;

		if (this.formWriterType.equalsIgnoreCase("list")) {

			return (sm.Parm(filterName).length() > 0) && (!sm.Parm(filterName).equals("0"));
		} else {
			return true;
		}
	}

	/*
	 * 
	 * List Heading Selectors
	 * 
	 */

	// called on the Show page, when returning to the list page
	public Integer getParentKey() {

		return db.getInteger("parent_id");
	}

	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 4)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : get list of parent Workflows
	 */
	public WebField getListSelector(int columnNumber) {

		String sQuery = "select " + parentKeyName + "," + parentKeyName + ", title_nm   from " + parentTableName
				+ " where " + parentTableName + "." + grandParentKeyName + " = " + sm.getApplicationId().toString();

		return new WebFieldSelect(filterName,
				sm.Parm(filterName).length() == 0 ? new Integer("0") : new Integer(sm.Parm(filterName)), db, sQuery,
				"- Select " + this.getParentTarget() + " -");

	}

	/*
	 * Limit the list to a specific workflow, show nothing first time.
	 */
	public String getNavigationFilter() {

		StringBuffer sb = new StringBuffer();

		if (sm.Parm("Relation").equalsIgnoreCase("Next")) {
			sb.append(" AND parent_id = " + sm.Parm(filterName));
		}

		// default status to open if no filter present
		sb.append(" AND telement.parent_kind_cd ='" + elementKindCd + "'");

		return sb.toString();
	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		if ((!sm.Parm(filterName).equalsIgnoreCase("0")) && (sm.Parm(filterName).length() > 0)) {
			sb.append(" AND telement.parent_id = " + sm.Parm(filterName));
		} else {
			return new String(" AND telement.parent_id = 0");
			// return "";
		}

		sb.append(" AND telement.parent_kind_cd ='" + elementKindCd + "'");

		return sb.toString();
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/
	/*
	 * data manager calls this just before passing the ht to the db for insert ...
	 * we can insert the 'parent_kind_cd' now,
	 */
	public boolean beforeAdd(Hashtable ht) {

		ht.put("parent_kind_cd", new DbFieldString("parent_kind_cd", elementKindCd));

		ht.put("parent_id", new DbFieldInteger("parent_id", new Integer(sm.Parm(filterName))));

		return true;

	}

	public Hashtable getWebFields(String parmMode) throws services.ServicesException {

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		if (addMode) {
			ht.put("parentName", new WebFieldDisplay("parentName", getParentName()));

		} else {
			ht.put("parentName", new WebFieldDisplay("parentName", db.getText("thing_name")));
		}

		ht.put("kind", new WebFieldDisplay("kind", getParentTarget()));

		/*
		 * Filters
		 */

		/*
		 * in case they hit cancel, allows the list page to restore the prior selector
		 */

		ht.put("Filter" + getParentTarget(), new WebFieldHidden("Filter" + getParentTarget(), elementKindCd));

		/*
		 * text fields
		 * 
		 */

		ht.put("title_nm", new WebFieldString("title_nm", db.getText("title_nm"), 64, 64));

		ht.put("reference_nm", new WebFieldString("reference_nm", db.getText("reference_nm"), 32, 32));

		ht.put("example_tx", new WebFieldString("example_tx", db.getText("example_tx"), 64, 255));

		ht.put("format_tx", new WebFieldString("format_tx", db.getText("format_tx"), 64, 255));

		ht.put("values_tx", new WebFieldString("values_tx", db.getText("values_tx"), 64, 255));

		/*
		 * numbers
		 */


		ht.put("version_id", new WebFieldString("version_id", (addMode ? "0" : db.getText("version_id")), 4, 4));

		ht.put("seq_no", new WebFieldString("seq_no", db.getText("seq_no"), 4, 4));

		ht.put("length_no", new WebFieldString("length_no", (addMode ? "0" : db.getText("length_no")), 4, 4));

		/*
		 * flags
		 * 
		 */
		ht.put("required_flag", new WebFieldCheckbox("required_flag", db.getText("required_flag"), ""));

		/*
		 * codes
		 * 
		 */
		ht.put("type_cd",
				new WebFieldSelect("type_cd", addMode ? "" : db.getText("type_cd"), sm.getCodes("DATATYPES")));

		ht.put("status_cd",
				new WebFieldSelect("status_cd", addMode ? "" : db.getText("status_cd"), sm.getCodes("LIVESTAT")));

		/*
		 * blobs
		 */
		ht.put("notes_blob", new WebFieldText("notes_blob", db.getText("notes_blob"), 4, 80));

		ht.put("desc_blob", new WebFieldText("desc_blob", db.getText("desc_blob"), 4, 80));

		ht.put("version_blob", new WebFieldText("version_blob", db.getText("version_blob"), 4, 80));

		return ht;

	}

	/*
	 * 
	 * return a selector for the Parent Id
	 * 
	 */

	private String getParentName() {

		String procQuery = ("Select title_nm from " + parentTableName + " where " + parentKeyName + " = "
				+ sm.Parm(filterName));

		String parentTitle = "";

		Hashtable parents = new Hashtable();

		try {
			parentTitle = db.getColumn(procQuery);
		} catch (services.ServicesException e) {
		}

		return parentTitle;
	}

	/*
	 * temp overrides to use extend Plugin
	 * 
	 */
	public String getSubTitle() {

		if (this.getHasRow()) {
			return "Application: " + db.getText(("application_name"));
		} else if (sm.getApplicationName() != null) {
			return "Application: " + sm.getApplicationName();
		}
		return "?";
	}

	// public void setSessionFilterKey() {
	// return;
	// }

	// public void setSessionFilterKey(String s) {
	// return;
	// }

	public String getListQuery() {
		StringBuffer sb = new StringBuffer();

		sb.append("select  'Element' as target, element_id ");

		if (moreListColumns != null) {
			for (int s = 0; s < moreListColumns.length; s++) {
				sb.append("," + moreListColumns[s]);
			}
		}

		sb.append(" from telement ");

		if (moreListJoins != null) {
			for (int s = 0; s < moreListJoins.length; s++) {
				sb.append(moreListJoins[s]);
			}
		}

		sb.append(" WHERE 1=1 ");

		sb.append(getListAnd());

		return sb.toString();

	}

	public String getSelectQuery() {

		StringBuffer sb = new StringBuffer();

		sb.append(this.tableName + ".*, " + rootTitle + "," + " concat(a.first_name, '  ', a.last_name) as added_by,"
				+ " concat(u.first_name, '  ', u.last_name) as updated_by ");

		if (this.getShowAuditSubmitApprove()) {
			sb.append(",concat(r.first_name, '  ', r.last_name) as reviewed_by "
					+ ",concat(s.first_name, '  ', s.last_name) as submitted_by ");
		}

		if (moreSelectColumns != null) {
			for (int s = 0; s < moreSelectColumns.length; s++) {
				sb.append("," + moreSelectColumns[s]);
			}
		}

		sb.append("	from " + this.tableName);

		if (moreSelectJoins != null) {
			for (int s = 0; s < moreSelectJoins.length; s++) {
				sb.append(moreSelectJoins[s]);
			}
		}

		sb.append("left join tuser as a  on " + this.tableName + ".added_uid = a.user_id " + " left join tuser as u on "
				+ this.tableName + ".updated_uid = u.user_id ");

		/*
		 * only add in the submitted and approved columns if doing
		 * auditShowApprovedSubmitted
		 */

		if (this.getShowAuditSubmitApprove()) {
			sb.append(" left join tuser as r  on " + this.tableName + ".reviewed_uid = r.user_id "
					+ " left join tuser as s  on " + this.tableName + ".submitted_uid = s.user_id ");
		}

		return sb.toString();
	}

}
