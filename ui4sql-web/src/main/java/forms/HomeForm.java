/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import db.DbInterface;
import java.sql.ResultSet;

import router.SessionMgr;

/**
 * @author PAULSEAR
 * 
 * Home form for showing user items from multiple table that are assigned to
 * him/her.
 * 
 * TODO: It only does issues and deliverables, but it needs to be expanded to
 * other tables that have ownership
 * 
 * There is no Plugin for this form...
 * 
 * 
 * 3/21/2005 - use class = bg0 and bg1 for 6/11/05 - remove 'reference' from the
 * selection list 8/23/05 - add in Selectors on the column headings.
 * 
 */
public class HomeForm extends FormWriter {

	public HomeForm(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);
		this.formName = "LoginForm";
		this.setSubTitle(mySetSubTitle());
	}

	public String getMenuName () {
		return "large";
	}
	
	public boolean isAdminFunction() {
		return false;
	}

	public void writeMenu() {
		// out.println("<table width=100%><tr><td
		// width=350>hi</td></tr></table>");
		return;
	};

	public String mySetSubTitle() {

		
		if (sm.Parm("FilterProject").length()== 0) {
			return ("Project:" + sm.getProjectName() + "&nbsp;&nbsp;My Role: " + sm.getProjectRoleName());
		}
		if (sm.Parm("FilterProject").equalsIgnoreCase("0") ) {
			return "Project: All";
		}
		
		// they changed project on the selector, so reset it, and match the subTitle to the new selection
		sm.setProjectId(new Integer(sm.Parm("FilterProject")));
				
		return "Project: " + sm.getProjectName() + "&nbsp;&nbsp;My Role: " + sm.getProjectRoleName();
		
	}

	public void writeForm() {

		DbInterface db = sm.getDbInterface();

		/*
		 * Build Selector Boxes
		 */
		String[][] aType = { { "I", "D", "P", "T" },
				{ "Issues", "Deliverables", "Problem Report", "Defect" } };

		WebFieldSelect wfType = new WebFieldSelect("FilterType", sm
				.Parm("FilterType"), aType);
		wfType.setDisplayClass("listform");
		wfType.setPleaseSelect(true);
		wfType.setSubmitOnClick(false);
		
		wfType.setSelectPrompt("-All Types-");
		wfType
				.setOnChangeJavaScript(" onchange=\"document.forms[0].submit(); \"");

		WebFieldSelect wfProject = new WebFieldSelect("FilterProject", (sm
				.Parm("FilterProject").length() == 0 ? sm.getProjectId()
				: new Integer(sm.Parm("FilterProject"))), sm
				.getProjectFilter(), true, true);

		wfProject
				.setOnChangeJavaScript(" onchange=\"document.forms[0].submit(); \"");
		wfProject.setSelectPrompt("- All Projects -");
		wfProject.setDisplayClass("listform");

		/*
		 * 
		 * Query Strings
		 * 
		 */
		StringBuffer sb = new StringBuffer();

		// qualify the permissions in case user is removed from project after
		// the assignment is made
		String selectIssues = "select 'Issue' as target, issue_id, title_nm, project_name, code_desc as status_desc  "
				+ " from tissue  "
				+ " left join tproject on tissue.project_id = tproject.project_id  "
				+ " left join tcodes on tissue.status_cd = tcodes.code_value and tcodes.code_type_id  = 45 "
				+ " left join tuser_project on tissue.project_id = tuser_project.project_id "
				+ " where tissue.assigned_uid = "
				+ sm.getUserId().toString()
				+ " and tissue.status_cd <> 'C' "
				+ " and tuser_project.user_id = " + sm.getUserId().toString();

		String selectDeliverables = "select 'Deliverable' as target, deliverable_id, title_nm as title, project_name, code_desc as status_desc  "
				+ " from tdeliverable  "
				+ " left join tproject on tdeliverable.project_id = tproject.project_id  "
				+ " left join tcodes on tdeliverable.deliverable_status_cd = tcodes.code_value and tcodes.code_type_id  = 5 "
				+ " left join tuser_project on tdeliverable.project_id = tuser_project.project_id "
				+ " where tdeliverable.owner_id = "
				+ sm.getUserId().toString()
				+ " and tdeliverable.deliverable_status_cd <> 'CMP' "
				+ " and tdeliverable.deliverable_status_cd <> 'CAN' "
				+ " and tuser_project.user_id = " + sm.getUserId().toString();

		String selectDefects = "select 'Defect' as target, defect_id, title_nm as title, project_name, code_desc as status_desc  "
				+ " from tdefect  "
				+ " left join tproject on tdefect.project_id = tproject.project_id  "
				+ " left join tcodes on tdefect.status_cd = tcodes.code_value and tcodes.code_type_id  = 35 "
				+ " left join tuser_project on tdefect.project_id = tuser_project.project_id "
				+ " where tdefect.assigned_uid = "
				+ sm.getUserId().toString()
				+ " and tdefect.status_cd <> 'C' "
				+ " and tdefect.status_cd <> 'X' "
				+ " and tuser_project.user_id = " + sm.getUserId().toString();

		String selectProblems = "select 'Problem' as target, problem_id, title_nm, project_name, code_desc as status_desc  "
				+ " from tproblem  "
				+ " left join tproject on tproblem.project_id = tproject.project_id  "
				+ " left join tcodes on tproblem.status_cd = tcodes.code_value and tcodes.code_type_id  = 45 "
				+ " left join tuser_project on tproblem.project_id = tuser_project.project_id "
				+ " where tproblem.assigned_to_uid = "
				+ sm.getUserId().toString()
				+ " and tproblem.status_cd <> 'C' "
				+ " and tuser_project.user_id = " + sm.getUserId().toString();

		/*
		 * build out the query depending on the selections
		 */

		if (sm.Parm("FilterType").equalsIgnoreCase("T")) {
			sb.append(selectDefects);
		}

		if (sm.Parm("FilterType").equalsIgnoreCase("D")) {
			sb.append(selectDeliverables);
		}

		if (sm.Parm("FilterType").equalsIgnoreCase("P")) {
			sb.append(selectProblems);
		}
		
		//  default is Issues if no filter value
		if (sm.Parm("FilterType").equalsIgnoreCase("I")
				|| sm.Parm("FilterType").length() == 0) {
			sb.append(selectIssues);
		}

		

		if ((sm.Parm("FilterProject").length() > 0)
				&& (!sm.Parm("FilterProject").equalsIgnoreCase("0"))) {
			sb
					.append(" AND tproject.project_id = "
							+ sm.Parm("FilterProject"));
		}

		try {
			ResultSet rs = db.getRS(sb.toString());

			// out.println("<br>");

			out
					.println("<FORM NAME=HomeForm METHOD=POST ACTION='Router?Target=Home'>");

			/*
			 * Heading
			 */
			out.println("<table >");
			out
					.println("<tr class=bg0><td colspan=4 align=center>Home - My Assigned Items</td></tr>");

			out.println("<tr class=\"bg0\"><td width=50>");

			// old out.println(wfType.getSelectHTML_Array(true, false));
			//out.println(wfType.getHTML(true, false));
			out.println(wfType.getHTML());
			

			out
					.println("</td><td width=200 align=left class=bg2>Title</td><td width=150 align=left>");


						
			wfProject.setSubmitOnClick(true);
			wfProject.setPleaseSelect(true);
			out.println(wfProject.getHTML());

			out.println("</td><td width=50 align=left>Status</td></tr>");

			out.println("</FORM>");

			/*
			 * Detail Lines
			 */
			while (rs.next()) {
				out.println("<tr class=\"bg2\"><td align=left>"
						+ rs.getString(1) + "</td><td>"
						+ "<A CLASS='listButton' HREF=Router?Target="
						+ rs.getString(1)
						+ "&Action=Show&Relation=this&RowKey=" + rs.getInt(2)
						+ ">" + rs.getString(3) + "</A></TD><td>"
						+ rs.getString(4) + "</td><td>" + rs.getString(5)
						+ "</td></tr>");
			}

			rs.close();
			out.println("</table>");

		} catch (Exception ioe) {
		}

		return;

	}


	public void preDisplay() {
	};

	// public void setTableManager(Plugin tm) {
	// }

	public String getBrowserTitle() {
		return new String("In-Basket");
	}

	public String getJavaScript() {
		return new String("function tmInit() {}");
	}
}