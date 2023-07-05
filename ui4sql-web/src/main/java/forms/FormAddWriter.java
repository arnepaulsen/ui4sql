/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import services.ServicesException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.text.SimpleDateFormat;

import plugins.Plugin;
import router.SessionMgr;

/**
 * @author Arne Paulsen
 * 
 * Change Log:
 * 
 * 5/16/05 Remove hide_updated_by, hide_update_date 7/18/05 Change anchors to
 * buttons 5/17/06 check 'cancelOk' .. for Register page
 * 
 * 7/21/09 Put back in audit Fields.. need to have something there.
 * 10/26/10 - use getParentId for the code type
 * 
 * 
 */
public class FormAddWriter extends FormDataWriter {

	// private FormTemplateWriter templateWriter;

	// *****************
	// CONSTRUCTOR
	// *****************

	public FormAddWriter(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);
	}

	public void init(Plugin parmPlugin) {
		plugin = parmPlugin;
		this.formWriterType = "add";
		this.plugin.formWriterType = "add";

	}

	public void preDisplay() {
		plugin.preDisplay();	
		return;
	}

	public boolean isAdminFunction() {
		return plugin.getIsAdminFunction();
	}

	public void writeForm() throws ServicesException {

		out.println("<FORM Name=Form1 ACTION=Router?Target=" + sm.getTarget()
				+ "&From=List Method=Post >");

		out.println("<table  border=0 cellpadding=0>");
		out.println("<tr align=left><td>");

		try {
			debug("FormAddWriter : getting template :"
					+ plugin.getTemplateName());

			writeTemplate(plugin.getTemplateName(), "add");
		} catch (Exception e) {
			System.out.println("FormAddWriter:writeForm - except rd.include "
					+ e.toString());
		}
		out.println("</td></tr>");
		out.println("</table>");

		out.println("<input type=HIDDEN ID='Action' Name='Action' Value=Post>");
		out.println("<input type=HIDDEN Name=Relation Value=this>");
		out.println("<input type=HIDDEN Name=From Value=" + sm.Parm("From")
				+ ">");

		/*
		 * 12/18/06 dump out the list filters so they stick when user goes back
		 * to the list .. same code is in the FormShowWriter and FormEditWritter
		 */

		Enumeration<String> e =  sm.getParmeterNames();

		while (e.hasMoreElements()) {
			String filterName = (String) e.nextElement();
			if (filterName.startsWith("Filter")) {
				String filterValue = sm.Parm(filterName);
				out.println("<input Type=Hidden Name=" + filterName + " Value="
						+ filterValue + ">");
			}
		}

		out.println(getContextHidden());

		// the RowKey is used as a place to go back to if the user hits cancel
		// must have something here
		// it's usually on the url if they come from the 'new' option on the
		// page
		// ... but not if they come straight off the menu bar, they may not have
		// an existing row contect

		Integer rowKey = new Integer("0");

		try {
			if (sm.Parm("RowKey").length() > 0) {
				// Parm always returns a string, empty if no value found
				debug("FormAddWriter: RowKey is : " + sm.Parm("RowKey"));
				rowKey = new Integer(sm.Parm("RowKey"));
			} else {
				// oh no... probably coming from the menu bar, not the page
				// 'new' function
				// so try to get a context from the session
				// right now.. Project is the only page that allows 'new' from
				// the menu bar
				// TODO: there may be other tables that have a 'new' feature
				// from the menu bar, so it might not be a Project
				if (sm.getProjectId() != null) {
					debug("FormAddwriter: RowKey from getProjectId is "
							+ sm.getProjectId());
					rowKey = sm.getProjectId();
				} else
					rowKey = new Integer("1");

				// debug("getting rowkey from project... maybe it's null");
			}

		} catch (java.lang.NumberFormatException ex) {
			// double oh no... they probably never when to a 'project' before,
			// so never got a Project id
			debug("caught a NumberFormat error, defaulting RowKey 1 1"
					+ ex.toString());
			rowKey = new Integer("1"); // / must have something
		} catch (Exception je) {
			rowKey = new Integer("1");
			debug("general exception " + je.toString());
		}

		out.println("<input type=HIDDEN Name=RowKey Value='"
				+ rowKey.toString() + "'>");

		// could get here from eiher the list page or show page..

		if (plugin.getIsDetailForm()) {
			out.println("<input Type=Hidden Name=Filter"
					+ plugin.getParentTarget() + " Value="
					+ sm.Parm("Filter" + plugin.getParentTarget()) + ">");
		}

		// 8/25 add parm / list selector for the list

		// out.println("<input Type=Hidden Name=Filter" + sm.Parm("Target")
		// + " Value=" + plugin.getKey().toString() + ">");

		out.println("</form>");

	}

	/*
	 * This sets the project_id =x, application_id=x, etc hidden.
	 * 
	 */
	private String getContextHidden() {

		// don't put out the root key if a step-child. it doesn't have a
		// 'project_id', etc. column.
		if (plugin.getIsStepChild()) {
			return "";
		}

		if (plugin.getDataType().equalsIgnoreCase("Project")) {
			return "<input type=HIDDEN Name=project_id Value="
					+ sm.getProjectId().toString() + ">";
		}

		if (plugin.getDataType().equalsIgnoreCase("Application")) {
			return "<input type=HIDDEN Name=application_id Value="
					+ sm.getApplicationId().toString() + ">";
		}
		if (plugin.getDataType().equalsIgnoreCase("Division")) {
			return "<input type=HIDDEN Name=division_id Value="
					+ sm.getDivisionId().toString() + ">";
		}

		if (plugin.getDataType().equalsIgnoreCase("Code")) {
			return "<input type=HIDDEN Name=code_type_id Value="
					+ sm.getParentId().toString() + ">";
		}

		if (plugin.getDataType().equalsIgnoreCase("Form")) {
			return "<input type=HIDDEN Name=form_id Value="
					+ sm.getFormId().toString() + ">";
		}

		return "";

	}

	public String getButtons() {

		StringBuffer sb = new StringBuffer();
		if (plugin.getSaveOk()) {

			// some forms, like RFC, force the user into edit mode after a save
			if (!plugin.getForceEditOnSave()) {
				sb
						.append("<input type=button class='stdformtopbutton' onClick=\"var ok;ok=ValidateForm(); if (ok != true) { return false;} setAction('Post');document.forms[0].submit();\" value='Save'>&nbsp;&nbsp;");
			}

			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"var ok;ok=ValidateForm(); if (ok != true) { return false;} setAction('PostThenEdit');document.forms[0].submit();\" value='Save-Edit'>&nbsp;&nbsp;");

		}

		if (plugin.getCancelOk()) {

			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('CancelAdd');document.forms[0].submit();\" value='Cancel'>&nbsp;&nbsp;");
		}

		return sb.toString();

	}

	@SuppressWarnings("unchecked")
	public Hashtable getAuditFields(Hashtable ht) {
		// not sure why these were commented out!
		ht.put("added_by", new WebFieldDisplay("added_by", ""));
		ht.put("approved_by", new WebFieldDisplay("approved_by", ""));
		ht.put("submitted_by", new WebFieldDisplay("submitted_by", ""));
		ht.put("updated_by", new WebFieldDisplay("updated_by", ""));
		return ht;
	}

	// standard JavaScript to set hidden fields on Form1
	public String getJavaScript() throws services.ServicesException {

		StringBuffer st = new StringBuffer();

		st.append("\nfunction setAction(x){"
				+ "\ndocument.Form1.Action.value=x;}"
				+ "\nfunction setRelation(x){"
				+ "\ndocument.Form1.Relation.value=x;}");

		st.append("\n\nfunction tmInit() {");

		st.append("}");

		return st.toString();
	}

}
