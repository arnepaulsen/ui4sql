/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.*;

/*******************************************************************************
 * /
 * 
 * 
 * 
 * 
 * Notes... dataManagers which are division-based, that is, relate to a
 * division, can use this as the super class to take all the default settings
 * for a division-type form.
 * 
 * The sql query is built using all fields from the target table, plus the audit
 * fields (updated_by, added_by, submitted_by, reviewed_by)
 * 
 * 
 * Permissions:
 * 
 * this root table doesn't link to a permissions table, the
 * getDefaultPermission() will return false, so sub-classes must supply their
 * own permissions - like addOk(), deleteOk() , editOk, etc.
 * 
 * 
 * Change Log: 8/15/06 use myconcat 8/25/06 - SQLSERVER : remove 'select',
 * plugin will put SELECT TOP 1 if sql-server
 * 
 *	Change log:
 *  6/14/10 remove obsolete concat3 stmts for MS Sql Server
 * 
 */

public class AbsDivisionPlugin extends Plugin {

	public AbsDivisionPlugin() throws services.ServicesException {

		super();
		this.setDataType("Division");
		this.setDeleteOk(true);
	}

	public String getSubTitle() {

		String divisionName = "Division: Don't Know";

		// for not add mode, we've already got everything

		// debug("getSubTitle:");

		if (this.getHasRow()) {

			// debug("has a row");

			// debug("div id: " + db.getInteger("division_id").toString() + " "
			// + db.getText("div_name"));

			sm.setDivisionId(db.getInteger("division_id"), db
					.getText("div_name"));
			divisionName = db.getText(("div_name"));
		} else if (sm.getDivisionName() != null) {
			// debug("fetch div from sm.getDivision Name()");

			divisionName = sm.getDivisionName();
		}

		if (this.getIsRootTable() || !this.getContextSwitchOk()
				|| !sm.userIsAdministrator()
				|| sm.getLastName().equalsIgnoreCase("register"))
			return "<span class=regtext>Program: " + divisionName + "</span>";

		else
			return new String(
					"<span class=regtext>Program: </span><A class=blackButton HREF=Router?Target=Division&Action=List&ReturnTo="
							+ sm.getTarget()
							+ "&Context=Division>"
							+ divisionName + "</a>");

	}

	// *******************
	// List Stuff
	// *******************

	// limits table select to only those permitted users
	public String getNavigationFilter() {
		return " ";
	}

	// todo: qualifiy by user_id instead of 'distinct'
	public String getListQuery() {

		// use the ListView if available
		if (this.getListViewName() != null) {
			return ("SELECT * from " + this.getListViewName()
					+ " WHERE division_id = " + sm.getDivisionId().toString());
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
					.append(" join tdivision on "
							+ this.tableName
							+ ".division_id = tdivision.division_id  and tdivision.division_id = "
							+ sm.getDivisionId().toString());
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

		sb.append(this.tableName + ".*, div_name  ");

		if (!this.tableName.equalsIgnoreCase("tproject")) {
			sb.append(", 'BRW' as role_cd ");
		}

		sb.append(",concat(a.first_name, ' ' , a.last_name) as added_by " + 
				 ",concat(u.first_name, ' ' , u.last_name) as updated_by ");

		if (this.getShowAuditSubmitApprove()) {
			sb.append(",concat(r.first_name, ' ', r.last_name)  as reviewed_by, "
					+ "concat(s.first_name, ' ', s.last_name ) as submitted_by ");
		}

		if (moreSelectColumns != null) {
			for (int s = 0; s < moreSelectColumns.length; s++) {
				// debug(" adding " + moreSelectColumns[s]);
				sb.append("," + moreSelectColumns[s]);
			}
		}

		sb.append("	from " + this.tableName);

		if (!this.getIsStepChild() && !this.getIsRootTable()) {
			sb.append(" left join tdivision on " + this.tableName
					+ ".division_id = tdivision.division_id ");

		}

		// sb
		// .append(" join tpermitdivisions on tdivisions.division_id =
		// tpermitdivisions.division_id ");

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

		// debug("absDivision getSelectQjuer ..done");

		return sb.toString();
	}

}
