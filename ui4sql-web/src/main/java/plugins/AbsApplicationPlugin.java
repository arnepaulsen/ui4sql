/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

/*******************************************************************************
 * / * 5/1/05 HUGE... now : 1. application select queries are automatically
 * built 2. DBInterface doesn't need a 'selectColumns', it uses rs metadata
 * 
 * 
 * Notes... dataManagers which are application-based, that is, relate to a
 * application, can use this as the super class to take all the default settings
 * for a application-type form.
 * 
 * The sql query is built using all fields from the target table, plus the audit
 * fields (updated_by, added_by, submitted_by, reviewed_by)
 * 
 * 10/26 qualify the list and select query on tapplications.app_id = sm.app_id,
 * because Plugin has the filter logic removed.... that was so 2004
 * 
 * Change Log:
 * 
 * 5/31/06 no change 8/25/06 - SQLSERVER : remove 'select', plugin will put
 * SELECT TOP 1 if sql-server
 * 
 */

public class AbsApplicationPlugin extends Plugin {

	public AbsApplicationPlugin() throws services.ServicesException {

		super();
		this.setDataType("Application");
		this.setDeleteOk(false);
		// System.out.println("absApplicationPlug constructor.. setting
		// datatype");
	}

	// limits table select to only those permitted users
	public String getNavigationFilter() {
		return "";
	}

	public String getSubTitle() {

		if (sm == null) {
			debug("Error SM is null");
		}

		String projectName = "Project: Don't Know";
		String projectLink = "";

		// for not add mode, we've already got everything
		if (this.getHasRow() && !sm.Parm("Action").equalsIgnoreCase("list")) {

			// sm.setProjectId(db.getInteger("project_id"), db
			// .getText("project_name"), db.getText("menu_cd"));

			if (db.getText(("project_name")) == null || db.getText(("project_name")).length() < 2) {
				projectName = sm.getProjectName();
			} else {
				projectName = db.getText(("project_name"));
			}

		} else if (sm.getProjectName() != null) {
			projectName = sm.getProjectName();
		}

		if (this.getIsRootTable() || !this.getContextSwitchOk()) {
			projectLink = "<span class=regtext>Project:&nbsp;&nbsp;"
					+ projectName + "&nbsp;&nbsp;My Role: "
					+ sm.getProjectRoleName() + "</span>";
		} else {
			projectLink = "<span class=regtext>Project:&nbsp;&nbsp;</span><A class=blackButton HREF=Router?Target=Project&Action=List&ReturnTo="
					+ sm.getTarget()
					+ "&Context=Project>"
					+ projectName
					+ "</a>"
					+ "&nbsp;&nbsp;My Role: "
					+ sm.getProjectRoleName();

		}

		String titleName = "Application: Not set.";

		// for not add mode, we've already got everything
		if (this.getHasRow()) {
			titleName = db.getText(("application_name"));
		} else if (sm.getApplicationName() != null) {
			titleName = sm.getApplicationName();
		}

		if (this.getIsRootTable() || !this.getContextSwitchOk()) {
			return projectLink
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Application: "
					+ titleName + "</span>";
		} else {
			return projectLink
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Application: </span> <A class=blackButton HREF=Router?Target=Application&Action=List&ReturnTo="
					+ sm.getTarget() + "&Context=Application>" + titleName
					+ "</a>";

		}

	}

	// todo: qualifiy by user_id instead of 'distinct'
	public String getListQuery() {

		// use the ListView if available
		if (this.getListViewName() != null) {
			return ("SELECT * from " + this.getListViewName()
					+ " WHERE application_id = " + sm.getApplicationId()
					.toString());
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

		if (!this.getIsStepChild() && !this.getIsRootTable()) {

			sb
					.append(" join tapplications on "
							+ this.tableName
							+ ".application_id = tapplications.application_id and tapplications.application_id =  "
							+ sm.getApplicationId().toString());

		}

		if (moreListJoins != null) {
			for (int s = 0; s < moreListJoins.length; s++) {
				sb.append(moreListJoins[s]);
			}
		}

		sb.append(" WHERE 1=1 ");

		return sb.toString();

	}

	public String getSelectQuery() {

		if (this.getSelectViewName() != null) {
			return " * from " + this.getSelectViewName();
		}

		StringBuffer sb = new StringBuffer();

		sb.append(this.tableName + ".*, application_name "  
				+ ",concat(a.first_name, '  ', a.last_name) as added_by" 
				+ ",concat(u.first_name, '  ', u.last_name) as updated_by ");

		if (this.getShowAuditSubmitApprove()) {
			sb.append(",concat(r.first_name, '  ', r.last_name) as reviewed_by, "
					+ "concat(s.first_name, '  ', s.last_name) as submitted_by ");
		}

		if (moreSelectColumns != null) {
			for (int s = 0; s < moreSelectColumns.length; s++) {
				sb.append("," + moreSelectColumns[s]);
			}
		}

		sb.append("	from " + this.tableName);

		if (!this.getIsStepChild() && !this.getIsRootTable()) {
			sb
					.append(" join tapplications on "
							+ this.tableName
							+ ".application_id = tapplications.application_id and tapplications.application_id = "
							+ sm.getApplicationId().toString());
		}

		if (moreSelectJoins != null) {
			for (int s = 0; s < moreSelectJoins.length; s++) {
				sb.append(moreSelectJoins[s]);
			}
		}

		sb.append(" left join tuser as a  on " + this.tableName
				+ ".added_uid = a.user_id left join tuser as u on "
				+ this.tableName + ".updated_uid = u.user_id ");

		if (this.getShowAuditSubmitApprove()) {
			sb.append(" left join tuser as r  on " + this.tableName
					+ ".reviewed_uid = r.user_id "
					+ " left join tuser as s  on " + this.tableName
					+ ".submitted_uid = s.user_id ");
		}

		return sb.toString();
	}

}
