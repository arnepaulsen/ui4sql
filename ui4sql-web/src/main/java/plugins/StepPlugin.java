/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import router.SessionMgr;
import services.ServicesException;
import db.DbFieldString;
import db.DbFieldInteger;

/*******************************************************************************
 * Procedure Step Plugin
 * 
 * A list of steps ... used as detail form my multiple parents, including: -
 * 1. WorkFlow  type Project, kind = "W"
 * 2. Milestone type Project, kine = "M"
 * 3. Test Case test application, kind = "T"
 * 4. Procedure , type appliction, kind = "P"
 * 
 * The step table has a 'parent_id' and 'parent_kind_cd', which must be unique
 * by first letter of parent (w)orkflow, (m)ilestone, etc.
 * 
 * The parent key is established from the list screen before any new steps are
 * created. Parent key cannot be changed on the update or add pages.
 * 
 * 
 * Change Log:
 * 
 * 8/11 added
 * 5/31/06 fix order of joins for mySql 5.0
 * 8/25/06 - SQLSERVER : remove 'select', plugin will put SELECT TOP 1 if sql-server
 * 
 * 
 * 
 ******************************************************************************/
public class StepPlugin extends Plugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	/*
	 * these are
	 */
	private String stepKindCd = ""; // the kind of step, like WorkFlow(W),

	private String parentTableName = ""; // like tworkflow

	private String grandParentTableName = ""; // either tproject or

	// tapplications

	private String grandParentKeyName = "";

	private String permitTableName = "";

	private String parentKeyName = ""; // like workflow_id

	private String filterName = ""; // like 'FilterWorkflow'

	private String rootTitle = "";

	public StepPlugin() throws services.ServicesException {
		super();
	}

	public void init(SessionMgr parmSm) {

		/*
		 * we have to wait for the FormData to run init to push the sm into the
		 * dm object
		 * 
		 */
		this.sm = parmSm;
		this.db = sm.getDbInterface(); // has an open connection

		/*
		 * first determine what kind of step we are
		 */

		/*
		 * 
		 */
		this.setTableName("tstep");
		this.setKeyName("step_id");
		this.setTargetTitle("Steps");

		this.setIsStepChild (true);
		this.setIsDetailForm(true);
		this.setListOrder ("seq_no");
		this.setContextSwitchOk (false); // can't change down here
		this.setAddOk(myAddOk());

		stepKindCd = sm.getStepKind().substring(0, 1);
		
		this.setParentTarget (sm.getStepKind());
		filterName = "Filter" + getParentTarget();
		parentKeyName = sm.getStepKind().toLowerCase() + "_id"; // like
		// workflow_id
		parentTableName = "t" + sm.getStepKind().toLowerCase(); // like

		debug(" step starting... sm.stepKind : " + sm.getStepKind());

		if ((sm.getStepKind().equalsIgnoreCase("procedure")) || (sm.getStepKind().equalsIgnoreCase("testcase"))) {

			this.grandParentTableName = "tapplications";
			this.grandParentKeyName = "application_id";
			permitTableName = "tuser_application";
			//filterColumnName = "tapplications.application_id";
			rootTitle = "application_name";
			this.setDataType ("Application");
		} else {
			this.grandParentTableName = "tproject";
			this.grandParentKeyName = "project_id";
			this.setDataType("Project");
			permitTableName = "tuser_project";
			rootTitle = "project_name";
		}

		this.setListHeaders( new String[] { getParentTarget(), "Seq", "After",
				"Required", "Title" });

		this.setMoreListColumns(new  String[] {
				parentTableName + ".reference_nm as thing_name", "seq_no",
				"predecessor_no", "required_flag", "tstep.title_nm" });

		this.setMoreListJoins(new  String[] {
				"  join " + parentTableName + " on tstep.parent_id = "
						+ parentTableName + "." + parentKeyName,
				" left join " + grandParentTableName + " on " + parentTableName
						+ "." + grandParentKeyName + " = "
						+ grandParentTableName + "." + grandParentKeyName + " " });

		this.setMoreSelectColumns (new String[] {
				parentTableName + ".title_nm as thing_name",
				grandParentTableName + "." + grandParentKeyName });

		this.setMoreSelectJoins (this.moreListJoins);
	}

	/*
	 * turn off the 'New' button on the list page if there is no parent selected
	 */
	public boolean myAddOk() {

		if (this.formWriterType.equalsIgnoreCase("list")) {

			return (sm.Parm(filterName).length() > 0)
					&& (!sm.Parm(filterName).equals("0"));
		} else {
			return true;
		}
	}

	/*
	 * 
	 * List Heading Selectors
	 * 
	 */

	
	public boolean listColumnHasSelector(int columnNumber) {
		// the status column (#2) has a selector, other fields do not
		if (columnNumber == 0)
			// true causes getListSelector to be called for this column.
			return true;
		else
			return false;
	}

	/*
	 * 8/22 : get list of parent Workflows
	 */
	public WebField getListSelector(int columnNumber) {

		/*
		 * query to get the list of workflows todo: cache this query
		 */

		Integer grandParentKey = new Integer("0");

		if ((sm.getStepKind().equalsIgnoreCase("procedure")) || (sm.getStepKind().equalsIgnoreCase("testcase"))) {
			grandParentKey = sm.getApplicationId();
		} else {
			grandParentKey = sm.getProjectId();
		}

		// todo: cache it
		String workflowQuery = "select " + parentKeyName + "," + parentKeyName
				+ ", reference_nm from " + parentTableName + " where "
				+ parentTableName + "." + grandParentKeyName + " = "
				+ grandParentKey.toString();

		Hashtable ht = new Hashtable();

		try {
			ht = db.getLookupTable(workflowQuery);
		} catch (ServicesException e) {
			debug("Error on List Selector query;");

		}
		debug("debug StepDM building list selector .., filtername "
				+ filterName + " value : " + sm.Parm(filterName) + " ");

		WebFieldSelect wf = new WebFieldSelect(filterName, (sm.Parm(filterName)
				.length() == 0 ? new Integer("0") : new Integer(sm
				.Parm(filterName))), ht, "Select a " + getParentTarget());
		wf.setDisplayClass("listform");

		return wf;

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
		sb.append(" AND tstep.parent_kind_cd ='" + stepKindCd + "'");

		return sb.toString();
	}

	public String getListAnd() {

		debug("StepDM - getListAnd");

		StringBuffer sb = new StringBuffer();

		if ((!sm.Parm(filterName).equalsIgnoreCase("0"))
				&& (sm.Parm(filterName).length() > 0)) {
			sb.append(" AND tstep.parent_id = " + sm.Parm(filterName));
		} else {
			return new String(" AND tstep.parent_id = 0");
			// return "";
		}

		sb.append(" AND tstep.parent_kind_cd ='" + stepKindCd + "'");

		debug(" AND IS: " + sb.toString());
		return sb.toString();
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/
	/*
	 * data manager calls this just before passing the ht to the db for insert
	 * ... we can insert the 'parent_kind_cd' now,
	 */
	public boolean beforeAdd(Hashtable ht) {
		ht.put("parent_kind_cd",
				new DbFieldString("parent_kind_cd", stepKindCd));
		ht.put("parent_id", new DbFieldInteger("parent_id", new Integer(sm
				.Parm(filterName))));
		return true;

	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		debug("getWeb Fields starting");

		Hashtable ht = new Hashtable();

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebField wfParent;

		if (addMode) {
			ht.put("parentName", new WebFieldDisplay("parentName",
					getParentName()));

		} else {
			ht.put("parentName", new WebFieldDisplay("parentName", db
					.getText("thing_name")));
		}

		ht.put("kind", new WebFieldDisplay("kind", getParentTarget()));

		/*
		 * in case they hit cancel, allows the list page to restore the prior
		 * selector
		 */

		// warning... this doesn't do anything,  after Netscape, wf's are only referenced, not 
		// forced put .
		
		ht.put("Filter" + getParentTarget(), new WebFieldHidden("Filter"
				+ getParentTarget(), stepKindCd));

		debug("getWeb Fields starting 4");

		ht.put("title_nm", new WebFieldString("title_nm", db
				.getText("title_nm"), 64, 64));

		ht.put("skip_cond_tx", new WebFieldString("skip_cond_tx", db
				.getText("skip_cond_tx"), 64, 128));

		ht.put("notes_blob", new WebFieldText("notes_blob", db
				.getText("notes_blob"), 4, 80));

		ht.put("desc_blob", new WebFieldText("desc_blob", db
				.getText("desc_blob"), 4, 80));

		ht.put("seq_no", new WebFieldString("seq_no", db.getText("seq_no"), 4,
				4));

		ht.put("predecessor_no", getPrevNo(parmMode));

		ht.put("required_flag", new WebFieldCheckbox("required_flag", db
				.getText("required_flag"), ""));

		return ht;

	}

	/*
	 * 
	 * return list of previous steps
	 * 
	 */

	private WebField getPrevNo(String parmMode) {

		boolean showMode = parmMode.equalsIgnoreCase("show") ? true : false;

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebField wfPrior;

		if (showMode) {
			return new WebFieldDisplay("predecessor_no", db
					.getText("predecessor_no"));
		}

		/*
		 * get list of prior steps
		 */
		Hashtable steps = new Hashtable();

		try {
			steps = db
					.getLookupTable("select seq_no as odor , seq_no , title_nm  from tstep where parent_id = "
							+ sm.Parm(filterName)
							+ " AND parent_kind_cd = '"
							+ stepKindCd + "'");
		} catch (ServicesException e) {
		}

		debug("getting pred list selector");

		wfPrior = new WebFieldSelect("predecessor_no", addMode ? new Integer(
				"0") : db.getInteger("predecessor_no"), steps, "No prior step");

		return wfPrior;

	} /*
		 * 
		 * return a selector for the Parent Id
		 * 
		 */

	private String getParentName() {

		String procQuery = ("Select title_nm from " + parentTableName
				+ " where " + parentKeyName + " = " + sm.Parm(filterName));

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
	
	public boolean afterGet() {
		this.setSubTitle(myGetSubTitle());
		return true;
	}
	
	public String myGetSubTitle() {

		if (this.grandParentTableName.equalsIgnoreCase("tapplications")) {
			if (this.getHasRow()) {
				return "Application: " + db.getText(("application_name"));
			} else if (sm.getApplicationName() != null) {
				return "Application: " + sm.getApplicationName();
			}
		} else {
			if (this.getHasRow()) {
				return "Project: " + db.getText(("project_name"));
			} else if (sm.getProjectName() != null) {
				return "Project: " + sm.getProjectName();
			}
		}

		return "?";
	}

	//public void setSessionFilterKey() {
	//	return;
	//}

	//public void setSessionFilterKey(String s) {
	//	return;
	//}

	public String getListQuery() {
		StringBuffer sb = new StringBuffer();

		sb.append("select  'Step' as target, step_id ");

		if (moreListColumns != null) {
			for (int s = 0; s < moreListColumns.length; s++) {
				sb.append("," + moreListColumns[s]);
			}
		}

		sb.append(" from tstep ");

		if (moreListJoins != null) {
			for (int s = 0; s < moreListJoins.length; s++) {
				sb.append(moreListJoins[s]);
			}
		}

		sb.append(" WHERE 1=1 ");


		return sb.toString();

	}

	public String getSelectQuery() {
		debug("ProjectDM - building select query");
		StringBuffer sb = new StringBuffer();

		sb.append(this.tableName + ".*, " + rootTitle
				+ "," + " concat(a.first_name, '  ', a.last_name) as added_by,"
				+ " concat(u.first_name, '  ', u.last_name) as updated_by ");

		if (this.getShowAuditSubmitApprove()) {
			sb
					.append(",concat(r.first_name, '  ', r.last_name) as reviewed_by "
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

		// move permitTableName after parent and grand-parent tables
		sb.append(" join " + permitTableName + " on " + grandParentTableName
				+ "." + grandParentKeyName + " = " + permitTableName + "."
				+ grandParentKeyName + " ");

		
		sb.append("left join tuser as a  on " + this.tableName
				+ ".added_uid = a.user_id " + " left join tuser as u on "
				+ this.tableName + ".updated_uid = u.user_id ");

		/*
		 * only add in the submitted and approved columns if doing
		 * auditShowApprovedSubmitted
		 */

		if (this.getShowAuditSubmitApprove()) {
			sb.append(" left join tuser as r  on " + this.tableName
					+ ".reviewed_uid = r.user_id "
					+ " left join tuser as s  on " + this.tableName
					+ ".submitted_uid = s.user_id ");
		}

		return sb.toString();
	}

}
