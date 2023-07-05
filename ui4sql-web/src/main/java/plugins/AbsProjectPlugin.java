/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import forms.WebField;

/*******************************************************************************
 * / * 5/1/05 HUGE... now : 1. project select queries are automatically built 2.
 * DBInterface doesn't need a 'selectColumns', it uses rs metadata
 * 
 * 
 * Notes... dataManagers which are project-based, that is, relate to a project,
 * can use this as the super class to take all the default settings for a
 * project-type form.
 * 
 * The sql query is built using all fields from the target table, plus the audit
 * fields (updated_by, added_by, submitted_by, approved_by)
 * 
 * 
 * Change Log: 8/11 use isStepChild to skip joins on tproject instead of
 * hard-code table name
 * 
 * 10/26 getListQuery does a join, instead of left join to tproject so we ..
 * don't get them all in the list.. removal of plugin.java logic for getFilter
 * brought out this bug.
 * 
 * Change: remove tprocess from select query, remove menu_cd from select items.
 * 
 * 5/15/06 Move the join on 'tuser_project' after the plugin list. ... may be a
 * difference between sql v4 and v5. v5 is more strict!!! 8/25/06 - SQLSERVER :
 * remove 'select', plugin will put SELECT TOP 1 if sql-server 10/12/06 - allow
 * browse users with no permission... . remove qualify for tuser_project, and do
 * 'left' join on tuser project..
 * 
 * ... never abend remove deleteOk() = true
 * 
 */

public class AbsProjectPlugin extends Plugin {

	public AbsProjectPlugin() throws services.ServicesException {

		super();
		this.setDataType("Project");
	}

	public String getSubTitle() {

		String projectName = "Project: Don't Know";

		// for not add mode, we've already got everything
		if (this.getHasRow() && !sm.Parm("Action").equalsIgnoreCase("list")) {

			// sm.setProjectId(db.getInteger("project_id"), db
			// .getText("project_name"), db.getText("menu_cd"));
			projectName = db.getText(("project_name"));
		} else if (sm.getProjectName() != null) {
			projectName = sm.getProjectName();
		}

		if (this.getIsRootTable() || !this.getContextSwitchOk()) {
			return "<span class=regtext>Project:&nbsp;&nbsp;" + projectName
					+ "&nbsp;&nbsp;My Role: " + sm.getProjectRoleName()
					+ "</span>";
		} else {
			return "<span class=regtext>Project:&nbsp;&nbsp;</span><A class=blackButton HREF=Router?Target=Project&Action=List&ReturnTo="
					+ sm.getTarget()
					+ "&Context=Project>"
					+ projectName
					+ "</a>"
					+ "&nbsp;&nbsp;My Role: " + sm.getProjectRoleName();

		}

	}

	// *******************
	// List Stuff
	// *******************

	// limits table select to only those permitted users
	public String getNavigationFilter() {

		return "";

		// return " and tuser_project.user_id = " + sm.getUserId().toString();
	}

	// todo: qualifiy by user_id instead of 'distinct'
	public String getListQuery() {

		// use the ListView if available
		if (this.getListViewName() != null) {
			return ("SELECT * from " + this.getListViewName()
					+ " WHERE project_id =" + sm.getProjectId()
					.toString() + " and user_id = " + sm.getUserId().toString());
		}
		

		StringBuffer sb = new StringBuffer();

		sb.append("select  '" + sm.getTarget() + "' as target, ");
		sb.append(this.keyName);

		if (moreListColumns != null) {
			for (int s = 0; s < moreListColumns.length; s++) {
				sb.append("," + moreListColumns[s]);
			}
		}

		sb.append(" from " + this.tableName);

		// tpermit projects joins to projects table through it parent
		// tdeliverable project
		if (!this.getIsStepChild()) {

			sb
					.append(" join tproject on "
							+ this.tableName
							+ ".project_id = tproject.project_id and tproject.project_id  = "
							+ sm.getProjectId().toString());
		}

		if (moreListJoins != null) {
			for (int s = 0; s < moreListJoins.length; s++) {
				sb.append(moreListJoins[s]);
			}
		}

		sb.append( " WHERE 1=1 ");
		
		return sb.toString();

	}

	public String getSelectQuery() {

		// use the ListView if available
		if (this.getSelectViewName() != null) {
			return (" * from " + this.getSelectViewName());
		}

		StringBuffer sb = new StringBuffer();

		sb
				.append(this.tableName
						+ ".*, project_name,  "
						+ " concat(a.first_name, '  ', a.last_name) as added_by,"
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

		if ((!this.tableName.equalsIgnoreCase("tproject"))
				& (!this.getIsStepChild())) {
			sb.append(" join tproject on " + this.tableName
					+ ".project_id = tproject.project_id ");
		}

		if (moreSelectJoins != null) {
			for (int s = 0; s < moreSelectJoins.length; s++) {
				sb.append(moreSelectJoins[s]);
			}
		}

		/*
		 * 10/12/06 make tuser_project a 'left' join to get records for 'browse'
		 * user
		 */
		if (!this.tableName.equalsIgnoreCase("tuser_project")) {
			sb
					.append(" left join tuser_project on tproject.project_id = tuser_project.project_id ");
		}

		// to get menu_cd
		// sb
		// .append(" left join tprocess on tproject.process_id =
		// tprocess.process_id ");

		sb.append(" left join tuser as a  on " + this.tableName
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
