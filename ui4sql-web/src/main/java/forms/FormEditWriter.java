/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import services.ServicesException;
import java.util.Enumeration;
import java.util.Hashtable;

import plugins.Plugin;

import router.SessionMgr;

/**
 * @author Arne Paulsen 3/15/05 remove
 *         <p>
 *         </p>
 *         on added/updated_by
 * 
 * retrofit frames.... changed hidden submitSave
 * 
 * Change Log : 3/15/07 - Fix bug. submit mode should call tmInit, not
 * 'pluginInit(), old name never fully replaced.
 */

public class FormEditWriter extends FormDataWriter {

	// private FormTemplateWriter templateWriter;

	public FormEditWriter(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);
	}

	
	public void init (Plugin parmPlugin) {
		plugin = parmPlugin;
		this.formWriterType = "edit";
		this.plugin.formWriterType = "edit";
	}

	
	public boolean isAdminFunction() {
		return plugin.getIsAdminFunction();
	}

	public void preDisplay() {

		/*
		 * do the database stuff first
		 */

		// doAction now done by Router
		
		// try {
		// Integer rc =
		// doAction(sm.Parm("Action"));
		// } catch (services.ServicesException se) {
		// debug("FormEditWriter .. error doAction :" + se.toString());
		// }
		
		try {
			if (sm.Parm("Action").equalsIgnoreCase("PostThenEdit")
					|| sm.Parm("Action").equalsIgnoreCase("Copy")) {
				plugin.setRow(plugin.rowId.toString());
			} else
				plugin.setRowRelation(sm.Parm("RowKey"), "this");
		} catch (ServicesException e) {
		}

		if (plugin.getHasRow()) {
			//debug(" FormEditWriter: preDisplay - tableMaager has no row.");
			plugin.preDisplay();

		}
	}

	public void writeForm() throws ServicesException {

		out.println("<FORM Name=Form1 ACTION=Router?Target=" + sm.getTarget()
				+ " Method=Post >");

		// the entire form in a table
		out.println("<table border=0 cellpadding=0><tr align=left><td>");

		// include the JSP in it's own table
		out.println("<tr><td align=left>");
		try {
			writeTemplate(plugin.getTemplateName(), "edit");
			// rd.include(req, resp);
		} catch (Exception e) {
			System.out.println("FormEditWriter:writeForm - except rd.include "
					+ e.toString());
		}
		out.println("</td></tr></table>"); // end of JSP table row

		out.println("<input type=HIDDEN Name=Relation Value=this>");
		out.println("<input type=HIDDEN ID=Action Name=Action Value=SubmitSave>");
		out.println("<input type=HIDDEN Name=RowKey Value='"
				+ plugin.getKey().toString() + "'>");

		/*
		 * 12/18/06 dump out the list filters so they stick when user goes back
		 * to the list .. same code is in the FormShowWriter.
		 */

		Enumeration e = sm.getParmeterNames();

		while (e.hasMoreElements()) {
			String filterName = (String) e.nextElement();
			if (filterName.startsWith("Filter")) {
				String filterValue = sm.Parm(filterName);
				out.println("<input Type=Hidden Name=" + filterName + " Value="
						+ filterValue + ">");
			}
		}

		out.println("</form>");

	}

	public String getButtons() {
		// Form submit buttons table row

		StringBuffer sb = new StringBuffer();

		if (sm.Parm("Action").equalsIgnoreCase("edit")
				|| sm.Parm("Action").equalsIgnoreCase("postthenedit")
				|| sm.Parm("Action").equalsIgnoreCase("copy")) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"var ok;ok=ValidateForm(); if (ok != true) { return false;} setAction('Save');document.forms[0].submit(); \" value='Save'>&nbsp;&nbsp;");
		}

		if (sm.Parm("Action").equalsIgnoreCase("submit")) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"var ok;ok=ValidateForm(); if (ok != true) { return false;} setAction('SubmitSave');document.forms[0].submit(); \" value='Save'>&nbsp;&nbsp;");

		}

		if (sm.Parm("Action").equalsIgnoreCase("review")) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"var ok;ok=ValidateForm(); if (ok != true) { return false;} setAction('ReviewSave');document.forms[0].submit(); \" value='Save'>&nbsp;&nbsp;");

		}

		if (sm.Parm("Action").equalsIgnoreCase("rescind")) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"var ok;ok=ValidateForm(); if (ok != true) { return false;} setAction('RescindSave');document.forms[0].submit(); \" value='Save'>&nbsp;&nbsp;");

		}

		// cancel
		sb
				.append("<input type=button class='stdformtopbutton' onClick=\"setAction('CancelEdit');document.forms[0].submit();\" value='Cancel'>&nbsp;&nbsp;");

		return sb.toString();

	}

	/*
	 * fill in the audit fields with the actual audit value, or put in a blank
	 */

	@SuppressWarnings("unchecked")
	public Hashtable<String, WebField> getAuditFields(Hashtable ht) {

		/*
		 * add should normally have something... .. unless added direct to db by
		 * jerk programmer
		 */

		String[][] reviewedChoices = { { "Y", "R" }, { "Pass", "Reject" } };

		ht.put("added_by", new WebFieldDisplay("added_by", plugin
				.getText("added_by")
				+ "&nbsp;on&nbsp;" + plugin.getText("added_date")));

		/*
		 * Updated...
		 */

		if (plugin.getIsUpdated()) {
			ht.put("updated_by", new WebFieldDisplay("updated_by", plugin
					.getText("updated_by")
					+ "&nbsp;on&nbsp;" + plugin.getText("updated_date")));
		} else {
			ht.put("updated_by", new WebFieldDisplay("updated_by", ""));
		}

		/*
		 * Audit Fields
		 */

		if (plugin.getShowAuditSubmitApprove()) {

			// submit
			if (sm.Parm("Action").equalsIgnoreCase("submit")) {

				ht.put("msg_tx", new WebFieldDisplay("msg_tx",
						"<FONT COLOR=RED>Please enter comments below.</FONT>"));
				ht.put("submitted_tag", new WebFieldDisplay("submitted_tag",
						"Submit Comments:"));
				ht.put("submitted_by", new WebFieldString("submitted_by", "",
						60, 255));
			} else {
				ht.put("submitted_tag", new WebFieldDisplay("submitted_tag",
						"Submitted By:"));
				ht.put("submitted_by", new WebFieldDisplay("submitted_by",
						plugin.getText("submitted_by") + "&nbsp;on&nbsp;"
								+ plugin.getText("submitted_date") + "<br>"
								+ plugin.getText("submitted_tx")));
			}

			// review
			if (sm.Parm("Action").equalsIgnoreCase("review")
					|| sm.Parm("Action").equalsIgnoreCase("rescind")) {
				ht.put("msg_tx", new WebFieldDisplay("msg_tx",
						"<FONT COLOR=RED>Please enter comments below.</FONT>"));
				ht.put("reviewed_tag", new WebFieldDisplay("reviewed_tag",
						"Review:"));
				ht.put("reviewed_by", new WebFieldString("reviewed_by", plugin
						.getText("reviewed_tx"), 60, 255));
				ht.put("reviewed_flag", new WebFieldRadio("reviewed_flag",
						plugin.getText("reviewed_flag"), reviewedChoices));

			} else {
				if (plugin.getIsReviewed()) {
					WebFieldRadio r = new WebFieldRadio("reviewed_flag", plugin
							.getText("reviewed_flag"), reviewedChoices);
					r.forceDisplay();
					ht.put("reviewed_flag", r);
					ht.put("reviewed_tag", new WebFieldDisplay("reviewed_tag",
							"Review Status:"));
					ht.put("reviewed_by", new WebFieldDisplay("reviewed_by",
							"<br>" + plugin.getText("reviewed_by")
									+ "&nbsp;on&nbsp;"
									+ plugin.getText("reviewed_date") + "<br>"
									+ plugin.getText("reviewed_tx")));
				} else {
					ht.put("reviewed_tag", new WebFieldDisplay("reviewed_tag",
							""));
					ht.put("reviewed_flag", new WebFieldDisplay(
							"reviewed_flag", ""));
					ht.put("reviewed_by",
							new WebFieldDisplay("reviewed_by", ""));
				}
			}

		}

		return ht;
	}

	// standard JavaScript to set hidden fields on Form1
	public String getJavaScript() throws services.ServicesException {

		StringBuffer st = new StringBuffer();

		st.append("\nfunction setAction(x){"
				+ "\ndocument.Form1.Action.value=x;}"
				+ "\nfunction setRelation(x){"
				+ "\ndocument.Form1.Relation.value=x;}");

		if (plugin.getShowAuditSubmitApprove()) {
			if (plugin.getIsSubmitted()
					|| sm.Parm("Action").equalsIgnoreCase("submit")) {
				st.append("\nvar showSubmit=true;");
			} else {
				st.append("\nvar showSubmit=false;");
			}

			if (plugin.getIsReviewed()
					|| sm.Parm("Action").equalsIgnoreCase("review")
					|| sm.Parm("Action").equalsIgnoreCase("rescind")) {
				st.append("\nvar showReview=true;");
			} else {
				st.append("\nvar showReview=false;");
			}

			if (sm.Parm("Action").equalsIgnoreCase("submit")
					|| sm.Parm("Action").equalsIgnoreCase("review")
					|| sm.Parm("Action").equalsIgnoreCase("rescind")) {
				st
						.append("\n\nfunction tmInit() {auditInit();toggle();toggle();}");
			} else {

				st.append("\n\nfunction tmInit() {auditInit();}");
			}

		} else {
			st.append("\n\nfunction tmInit() {}");
		}

		return st.toString();
	}

}
