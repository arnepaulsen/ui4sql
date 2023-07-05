/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import router.SessionMgr;
import db.*;

import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Collections;
import java.util.Iterator;
import plugins.Plugin;

/**
 * @author Arne Paulsen write out a table list page ... needs to have
 *         columnNames and set first 3/13 tracking From Form 4/20/05 add
 *         division as possible filter
 * 
 * Change Log: - Add option to return list to another list
 * 
 * 8/22 - show Selector in column headers. 8/25/ - Add button for Parent Form if
 * a stepChild 9/11 for filter selectors, the getSelectHTML only forces the
 * 'submit/click' option and the 'please choose' is defaulted to the original
 * constructor. .. it used to always force 'please choose; ... added new
 * getSelectHTML option in WebFieldSelect to receive just the 'submit/click' ..
 * and use the original 'please choose' option saved at constructions. 10/05 -
 * handle 'change mode' by putting back the 'returnTo' parm as user chooses
 * different filter options, then finally they pick a project.
 * 
 * 
 * 4/26/06 add call to plugin.listCenterHTML to center the column
 * 
 * 10/6/06 use forms instead of hyperlinks to showRow 12/12/06 add 'refresh'
 * button, remove 'list' literal on the list header. Detail forms can add if
 * they want 12/20/06 use new getListTitle() instead of static string (see Tcab
 * plugin)
 * 
 * 7/21/07 force column 2, usually title, to width=100... 8/16/07 listOnly()
 * prevents drill-down to form 9/16/07 Check of 'plugin.listOnly() before
 * writing "New" button on list page
 * 
 * 10/5/07 - Add funtion to force Action from 'New' to 'GoTo' when hitting enter
 * if there is a key value in the GoTo field, otherwise it stays as 'New'
 * 
 * 10/9/07 - Add excel download print option 1/30/08 - remove 'other excel info'
 * garbage from right-side of list header 2/4/08 - Change default RowKey
 * value=xx to rowkey value=> 2/24/08 - make false the check for goto value that
 * forces action to goto
 * 
 * 5/3/08 - add ability to show a list "Row-Number" . SQL Server 2005 has a
 * row_number() function, but not in 2000. so fake it here
 * 
 * 3/15/09 - remove width=850 from title row,
 * 
 * 4/30 - add hook to plugin to fetch html onLoad javascript
 * 
 */

public class FormListWriter extends FormDataWriter {

	public String[] columnNames;

	// *****************
	// CONSTRUCTOR
	// *****************

	// ********************************************************************
	// this writer doesn't have a RequestDispatcher for a custom template
	// ... it just uses a generic table to list the rows
	// ********************************************************************

	public FormListWriter(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);
	}

	public void init(Plugin parmPlugin) {
		plugin = parmPlugin;
		columnNames = plugin.getListHeaders();
		this.formWriterType = "list";
		this.plugin.formWriterType = "list";

	}

	/*
	 * 
	 * This handles the 'change' link in the sub-title
	 */
	public void preDisplay() {

		if (sm.Parm("ContextProject").length() > 0) {

			sm.setProjectId(new Integer(sm.Parm("ContextProject")));
		}

		if (sm.Parm("ContextApplication").length() > 0) {

			sm.setApplicationId(new Integer(sm.Parm("ContextApplication")));
		}

		if (sm.Parm("ContextDivision").length() > 0) {

			String divName;

			DbInterface db = sm.getDbInterface();
			try {
				divName = db
						.getColumn(" Select div_name from tdivision where division_id = "
								+ sm.Parm("ContextDivision"));
				// set both the new id and new name
				sm.setDivisionId(new Integer(sm.Parm("ContextDivision")),
						divName);

			} catch (services.ServicesException se) {
				divName = "FormListWriter:preDislay - Division not found";
			}
		}

		plugin.preDisplay();
		
	}

	public String getDisplayMessage() {
		return new String("List complete.");
	}

	/*
	 * 8/22 change form name 8/25 add button for parent form on stepChilds
	 * 
	 */
	public String getButtons() {

		StringBuffer sb = new StringBuffer();

		/*
		 * Up button
		 */
		if (plugin.getIsDetailForm()) {
			if ((sm.Parm("Filter" + plugin.getParentTarget()).length() > 0)
					&& (!sm.Parm("Filter" + plugin.getParentTarget())
							.equalsIgnoreCase("0"))) {
				// Go 'Up' to the Show page if there is a filter, else go up to
				// the list page
				sb
						.append("<INPUT type=button Name=ParentButton onClick=\"setAction('show');setTarget('"
								+ plugin.getParentTarget()
								+ "');document.forms[0].submit();\" class='stdformtopbutton' value='Up'>&nbsp;&nbsp;");
			} else {
				sb
						.append("<INPUT type=button Name=ParentButton onClick=\"setAction('list');setTarget('"
								+ plugin.getParentTarget()
								+ "');document.forms[0].submit();\" class='stdformtopbutton' value='Up'>&nbsp;&nbsp;");

			}

		}

		if (true) {
			sb
					.append("<INPUT type=button Name=RefreshButton onClick='setAction(\"list\");document.forms[0].submit();' class='stdformtopbutton' value='Refresh'>&nbsp;&nbsp;");
		}

		if (plugin.getExcelOk()) {

			sb
					.append("<INPUT type=button Name=RefreshButton onClick='setAction(\"excel\");document.forms[0].submit();' class='stdformtopbutton' value='Excel'>&nbsp;&nbsp;");
		}

		/*
		 * New Button
		 */

		if (plugin.getAddOk() && (!plugin.getListOnly())
				&& (!plugin.tableName.equalsIgnoreCase("ProjectTOC"))) {
			sb
					.append("<INPUT type=button Name=NewButton onClick='setAction(\"add\");document.forms[0].submit();' class='stdformtopbutton' value='New'>&nbsp;&nbsp;");
		}

		if (plugin.getGotoOk()) {
			sb
					.append("<INPUT type=button Name=GotoButton onClick='setAction(\"goto\");document.forms[0].submit();' class='stdformtopbutton' value='Go To "
							+ plugin.getGotoDisplayName()
							+ "'>&nbsp;<input name=gotoKey id=gotoKey type=text size=7 maxlength=7 value = ''>&nbsp;");
		}

		return sb.toString();

	}

	/*
	 * 8/22 update for List Selector:
	 */
	public String getJavaScript() {

		StringBuffer sb = new StringBuffer();

		sb.append("\n\nfunction tmInit(){" + plugin.getScriptInit() + "}");

		// 8/25 add function to change the target
		// this is for the filter buttons, return back to the list page.
		sb
				.append("\nfunction setAction(x){"
						+ "\ndocument.forms[0].Action.value=x;}"
						+ "\n\nfunction showRow(rowNum){"
						+ "\nsetAction('Show');"
						+ "\ndocument.forms[0].RowKey.value=rowNum;"
						+ "\ndocument.forms[0].submit();return false;}"
						+ "\n\nfunction setTarget(x){"
						+ "\ndocument.forms[0].Target.value=x;}"
						+ "\n\nfunction checkGoTo(){"
						+ "\nif (document.forms[0].gotoKey.value > 999) {setAction('goto');}}");

		return sb.toString();
	}

	public void setColumnNames(String[] parmColumnNames) {
		columnNames = parmColumnNames;
	}

	public boolean isAdminFunction() {
		return plugin.getIsAdminFunction();
	}

	@SuppressWarnings("unchecked")
	public void writeForm() throws services.ServicesException {
		try {

			DbField[] attrs;
			// = new DbField[3];
			Object rowId;
			// the HastTable an entry for each row, key is the RowKey,
			// the object is an array of DbFields for each column

			Hashtable ht = plugin.getList();
			SortedSet set = Collections.synchronizedSortedSet(new TreeSet(ht
					.keySet()));
			// Enumeration en = ht.keys();
			Iterator it = set.iterator();

			out.println("<table border=0 cellpadding=0>");

			/*
			 * Title Heading
			 */

			// 10/3/07 ... added </td? between </table></tr?
			out
					.println("<FORM onSubmit='checkGoTo();' NAME=ListForm METHOD=POST ACTION=Router>");

			if (sm.Parm("Action").equalsIgnoreCase("excel")) {

				try {

					String excelFileName = plugin.makeExcelFile();

					String encoded = URLEncoder.encode(excelFileName, "UTF-8");

					out.println("<tr class='bg1'><td colspan="
							+ (columnNames.length )
							+ "><TABLE WIDTH=100%><TR><TD ALIGN=LEFT>&nbsp;"
							+ "<a href=reports/" + excelFileName + ">"
							+ excelFileName + "</a>"
							+ "&nbsp;</td><TD ALIGN=RIGHT>" + ""
							+ "</TD></TR></TABLE></td></tr>");
				} catch (Exception e) {
					debug("FormListWriter : Error creating excel file in "
							+ e.toString());
				}
			}

			// 10/11 - Allow button to move to center on wide pages
			
			out.println("<tr class='bg1'><td  colspan=" + (columnNames.length )
					+ "><TABLE WIDTH=100%><TR><TD ALIGN=LEFT>&nbsp;"
					+ plugin.getListTitle() + "&nbsp;</td><TD " + (plugin.getListPageCenterButtons() ? ">" : "ALIGN=RIGHT>")
					+ getButtons() + "</TD></TR></TABLE></td></tr>");

			/*
			 * column headings
			 */

			out.println("<tr class='bg1'>");

			String space = "";
			
			String width = "";
			
			

			// TODO.. Fix the widht of the title / reference field

			for (int x = 0; x < columnNames.length; x++) {

				if (x == 1) {
					space = "&nbsp;";
				}

				if (plugin.listColumnHasSelector(x) == true) {
					out.println("<td " + space + "> "
							+ plugin.getListSelector(x).getHTML("edit")
							+ "</td>");
					// out.println("<td>" + "&nbsp;" + columnNames[x] +
					// "&nbsp;</td>");
				} else {
					boolean center = plugin.getListColumnCenterOn(x);
					width = (plugin.getListColumnWidth(x).length() > 0 ) ? " WIDTH=\"" + plugin.getListColumnWidth(x) + "\""  : "";
					
					if (center) {

						out.println("<td " + width + " align=center>"
								+ "&nbsp;" + columnNames[x] + "&nbsp;</td>");
					} else {

						out.println("<td " +  width + ">" + "&nbsp;"
								+ columnNames[x] + "&nbsp;</td>");
					}
				}
			}
			out.println("</tr>");


			// move From parameters here.

			// 10/10/05 when in 'change' mode, the is user selecting a new
			// project,
			// application, etc. we need a way to keep stay in that mode when
			// the change the iterate on the list page by changing the filters

			if (sm.Parm("ReturnTo").length() > 0) {
				out.println("<input Type=Hidden Name=ReturnTo value="
						+ sm.Parm("ReturnTo") + ">");

			}

			// 10/6 onClick will change the RowKey value

			out.println("<input Type=Hidden Name=Relation Value=this>");

			out.println("<input Type=Hidden Name=Action Value=add>");

			out.println("<input Type=Hidden Name=Target Value="
					+ sm.getTarget() + ">");
			out.println("<input Type=Hidden Name=From Value=list" + ">");

			// for the 'Up' button, go up to the parent 'Show' page if there is
			// a filter,
			// . otherwise go up to the list page with no filter. -- All
			// something --
			if (plugin.getIsDetailForm()) {

				if ((sm.Parm("Filter" + plugin.getParentTarget()).length() > 0)
						&& (!sm.Parm("Filter" + plugin.getParentTarget())
								.equalsIgnoreCase("0"))) {

					// out.println("<input Type=Hidden Name=Relation Value=this"
					// / + ">");
					out.println("<input Type=Hidden Name=RowKey Value="
							+ sm.Parm("Filter" + plugin.getParentTarget())
							+ ">");

					out.println("<input Type=Hidden Name=Filter"
							+ plugin.getParentTarget() + " Value="
							+ sm.Parm("Filter" + plugin.getParentTarget())
							+ ">");

				} else {
					out.println("<input Type=Hidden Name=RowKey Value=>");
				}
			} else {
				out.println("<input Type=Hidden Name=RowKey Value=>");
			}

			out.println("</form>");

			/*
			 * print each row
			 */
			int rowCount = 0;

			while (it.hasNext()) {
				rowId = (Object) it.next();
				attrs = (DbField[]) ht.get(rowId);
				showRow(out, attrs, rowCount);
				rowCount++;
			}

			out.println("<tr bgcolor=white><td colspan=" + columnNames.length
					+ ">&nbsp;</td></tr>");

			out.println("</table>");

			/*
			 * that's it
			 */

		} catch (services.ServicesException se) {
			System.out.println("FormListWriter:writeForm - list exception : "
					+ se.toString());
			throw new services.ServicesException(se.toString());
		} catch (Exception e) {
			System.out
					.println("FormListWriter:writeForm - generic exception : "
							+ e.toString());
			throw new services.ServicesException(e.toString());
		}
		return;

	}

	/*
	 * Print the columns in a single row
	 * 
	 */

	public void showRow(WebLineWriter out, DbField[] fields, int rowNum) {

		out.println("<TR class='bg2'>");

		// 5/3/2008 allow display of the rownum if the column value is 'ROWNUM'
		if ((sm.Parm("ReturnTo").length() > 0)) {
			out.println("<TD align=left>"
					+ "<A CLASS='listButton' HREF=Router?Target="
					+ sm.Parm("ReturnTo") + "&Action=List&Context"
					+ sm.Parm("Context") + "=" + fields[1].getText() + ">"
					+ fields[2].getText() + "</A></TD>");
		} else {
			// don't allow drill-down to template/form if listOnly == false
			if (plugin.getListOnly()) {
				out.println("<TD align=left>" + fields[2].getText() + "</TD>");
			} else {
				// 7/27/09 show color in first column
				out
						.println("<TD align=left "
								+ plugin.listBgColor(0, "", fields)
								+ ">"
								+ "<A CLASS='listButton' HREF=# onClick=showRow("
								+ fields[1].getText()
								+ ");>"
								+ (fields[2].getText().equalsIgnoreCase(
										"ROWNUM") ? ("" + (rowNum + 2))
										: fields[2].getText()) + "</A></TD>");
			}

		}

		// 8/22 using header column count instead of data length, as some
		// columns are hidden
		// for (int x = 3; x < fields.length; x++) {

		String color = "";
		String align = "";
		String width  = "";
		

		for (int x = 3; x < columnNames.length + 2; x++) {
			align = plugin.getListColumnCenterOn(x - 2) ? " align=center " : "";
			color = plugin.listBgColor(x - 2, fields[x].getText(), fields);
			width = (plugin.getListColumnWidth(x-2).length() > 0 ) ? " WIDTH=\"" + plugin.getListColumnWidth(x-2) + "\"" : "";


			out.println("<TD " + align + color + width +  ">" + fields[x].getText()
					+ "</TD>");

		}

		out.println("</TR>");
		return;
	}

}
