/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import services.ServicesException;
import router.SessionMgr;

import java.util.Enumeration;
import java.util.Hashtable;

import plugins.Plugin;

/**
 * @author Arne Paulsen
 * 
 * Change Log:
 * 
 * 6/13 don't allow edit if record is in approved state 8/11 use
 * plugin.detailTarget for detail button text
 * 
 * 8/25 parent 'up' button go to show, not list form
 * 
 * 8/29 removed all the old result messages, like 'next record shown', 'delete
 * done', etc. .. we don't display the result messages anymore, Intuit QuickBase
 * doesn't, so we don't either .. go back to prior versoin to resurect.
 * 
 * 5/17/06 Add 'Print' button 8/21/06 no chnage,, just debug in getbuttons
 * 
 * 
 * 2/22/08 - Turn off print button - it's crashing anyway
 * 
 * ====================================== notes: If you are having problem here:
 * 
 * 1. make sure you don't reference the session object before the plugin has it
 * 
 * 2. for stepChildren-- make sure you have the project id or application id in
 * the moreSelectColumns !!!
 * 
 * Change log:
 * 
 * 2/2/11 - add button for Excel on detail page
 * 
 */

public class FormShowWriter extends FormDataWriter {

	// the dervied form must put out the actual table data
	// public void writeTableData(TableWriter plugin);

	// private FormTemplateWriter templateWriter;

	private String displayMessage;

	public FormShowWriter(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);
	}

	public void init(Plugin parmPlugin) {
		plugin = parmPlugin;
		this.formWriterType = "show";
		this.plugin.formWriterType = "show";
		
	}

	/*
	 * 
	 * this gets called to set correct record position.
	 * 
	 * method 'writeForm' gets called after this
	 * 
	 * updates/adds/deletes etc. have already been done by the data manager
	 * 
	 */
	public void preDisplay() {

		//debug("FormShowWriter:preDisplay...starting");

		String action = sm.Parm("Action");
		String relation = sm.Parm("Relation");
		
		
		/*
		 * get the Excel File
		 */
		
		
		if (action.equalsIgnoreCase("exceldetail")) {
					
			plugin.mode = "show";
			plugin.makeExcelFile();
			
		}

		/***********************************************************************
		 * call Plugin to save, copy, delete, insert
		 * 
		 **********************************************************************/

		// debug("... calling doAction : " + sm.Parm("Action"));
		// doAction now done by Router !
		// try {
		// rc = doAction(sm.Parm("Action"));
		// debug (" .. rc = " + rc);
		//	
		// } catch (services.ServicesException se) {
		// debug("FormShowWriter .. error doAction :" + se.toString());
		// }
		/***********************************************************************
		 * 
		 * now try to get a new record to show
		 * 
		 **********************************************************************/

		//debug("FormShow:Predisplay:setrow");
		try {
			plugin.setRowRelation(plugin.rowId.toString(), relation);
		} catch (ServicesException e) {
			debug("writeForm:preDisplay - " + e.toString());
		}

		// debug("show: 3");
		try {

			this.displayMessage = "";

			// recover from invalid scrolls (only next scrolling is now used!!)

			if ((relation.equalsIgnoreCase("prev"))
					&& (plugin.getHasRow() == false)) {

				plugin.setRow(plugin.rowId.toString());
				// this.displayMessage = "Already at begining.";
			}

			if ((relation.equalsIgnoreCase("next"))
					&& (plugin.getHasRow() == false)) {
				plugin.setRow(plugin.rowId.toString());
				// this.displayMessage = "Already at end.";
			}

			/*
			 * used in ProjectPlan to upload a file
			 * 
			 * can also be used to supply other actions
			 */

			//debug("formShowWriter:predisplay - calling plugin predisplay");

			plugin.preDisplay();

			if (action.equalsIgnoreCase("custom")) {
				plugin.customAction();
			}

		} catch (services.ServicesException e) {
			debug("FormShowWriter:preDisplay - " + e.toString());

		}

		/*
		 * fix the sm project id if they display a project other than the
		 * current context .. but absProject will do this!!! if plugin.hasrow ==
		 * true
		 */

		// debug("checking if projectid is blank");
		// debug("fpw:3");
		if (plugin.getDataType().equalsIgnoreCase("Project")) {
			// debug("comparing sm project to db project");
			if (!sm.getProjectId().equals(plugin.getObject(("project_id")))) {
				// debug("FormShowWriter - proj id is different than the
				// rs...");
				sm.setProjectId((Integer) plugin.getObject("project_id"));
			}
		}

		// debug("fpw:5");

	}

	public void writeForm() {


		debug ("formShowWriter.writeForm " );
		
		
		try {

			// debug ("FormShow: write form");
			// if (this.target == null) {
			// debug("FormShowWriter target is null");
			// }

			out.println("<FORM Name=Form1 ACTION=Router  Method=Post>");

			out.println("<table border=0 cellpadding=0><tr align=left><td>");

			/*
			 * Write the template, substitute the tags with webFields values
			 */


			debug ("formShowWriter.writeTemplate. Template name" );
			
			debug  ( " ..... template" + plugin.getTemplateName());
			
			try {


				debug ("formShowWriter.writeTemplate. calling WriteTemplate" );

				
				writeTemplate(plugin.getTemplateName(), "show");
				// rd.include(req, resp);
			} catch (Exception e) {
				System.out
						.println("FormShowWriter:writeTemplate - except rd.include "
								+ e.toString());
			}
			out.println("</td></tr>");

			out.println("<tr height=50><td valign=bottom>"
					+ this.displayMessage + "</td></tr>");

			out.println("<input type=hidden name=RowKey value="
					+ plugin.getKey() + ">");
			out.println("<input Type=Hidden ID=Action Name=Action Value=show>");
			out.println("<input Type=Hidden Name=Relation Value=next>");
			out.println("<input Type=Hidden Name=From Value=Show>");

			/*
			 * 12/18/06 dump out the list filters so they stick when user goes
			 * back to the list
			 */

			Enumeration e = sm.getParmeterNames();

			while (e.hasMoreElements()) {
				String filterName = (String) e.nextElement();
				if (filterName.startsWith("Filter")) {
					String filterValue = sm.Parm(filterName);
					out.println("<input Type=Hidden Name=" + filterName
							+ " Value=" + filterValue + ">");
				}
			}

			if (plugin.getIsDetailForm()) {
				out.println("<input Type=Hidden Name=Filter"
						+ plugin.getParentTarget() + " Value="
						+ plugin.getParentKey().toString() + ">");
			}

			// 8/25 add parm / list selector for the list

			out.println("<input Type=Hidden Name=Filter" + sm.getTarget()
					+ " Value=" + plugin.getKey().toString() + ">");

			out.println("<input Type=Hidden Name=Target Value="
					+ sm.getTarget() + ">");
			out.println("</table>");

			out.println("</form>");

			if (displayMessage.length() < 1) {
				displayMessage = new String("Display complete.");
			}

		} catch (Exception se) {
			out.println("except in show form");
			out.println("\n" + se.toString());
			se.printStackTrace();
		}
	}

	public String getButtons() {
		// menu bar
		StringBuffer sb = new StringBuffer();

		// sb.append(""
		// + "<a class='smallButton' href=#
		// onClick=\"setAction('show');setRelation('this');setAction('refresh');document.Form1.submit();\">Refresh</A>&nbsp;&nbsp;");

		// check to see if this datamanager wants VCR buttons (first, prev,
		// next, last)
		if (plugin.getShowVCR() && false) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick='\"setAction('show');setRelation('first');document.forms[0].submit();\" value='First>"
							+ "<input type=button class='stdformtopbutton' onClick=\"setAction('show');setRelation('prev');document.forms[0].submit();\" value='Prevnbsp;"
							+ "<a class='smallButton' href=# onClick=\"setAction('show');setRelation('next');document.forms[0].submit();\">Next</A>&nbsp;&nbsp;"
							+ "<a class='smallButton' href=# onClick=\"setAction('show');setRelation('last');document.forms[0].submit();\">Last</A>&nbsp;&nbsp;");
		}
		/*
		 * Just show the next button, don't really need the others!
		 */

		if (plugin.getNextOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('show');setRelation('next');document.forms[0].submit();\" value=Next>&nbsp;&nbsp;");
		}

		if (plugin.getAddOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('add');document.forms[0].submit();\" value='New'>&nbsp;&nbsp;");
		}

		if (plugin.getEditOk() && !plugin.isPassReview()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('edit');document.forms[0].submit();\" value=Edit>&nbsp;&nbsp;");
		}

		if (plugin.getCopyOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('copy'); copyOk = confirm('Copy?'); if (copyOk == false)  { return false;};document.forms[0].submit();\" value=Copy>&nbsp;&nbsp;");
		}

		if (plugin.getExcelDetailOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setRelation('this');setAction('exceldetail');document.forms[0].submit();\" value=Excel>&nbsp;&nbsp;");
		}
		
		if (plugin.getSubmitOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setRelation('this');setAction('submit');document.forms[0].submit();\" value=Submit>&nbsp;&nbsp;");
		}

		if (plugin.isFailReview()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setRelation('this');setAction('submit');document.forms[0].submit();\" value=Re-Submit>&nbsp;&nbsp;");
		}

		if (plugin.getReviewOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setRelation('this');setAction('review');document.forms[0].submit();\" value=Review>&nbsp;&nbsp;");
		}

		//		
		if (plugin.isPassReview()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setRelation('this');setAction('rescind');document.forms[0].submit();\" value='Rescind'>&nbsp;&nbsp;");
		}

		if (plugin.getDeleteOk() == true) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('delete'); deleteOk = confirm('Delete?'); if (deleteOk == false)  { return false;};document.forms[0].submit();\" value=Delete>&nbsp;&nbsp;");
		}

		if (plugin.getRemedyOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('remedy');setRelation('this');document.forms[0].submit();\" value='Remedy'>&nbsp;&nbsp;");
		}

		if (plugin.getHasDetailForm() == true) {
			// append an 's' to make detail list plural (like Issues) ony if
			// it's not already plural.

			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('list');setTarget('"
							+ plugin.getDetailTarget()
							+ "');document.forms[0].submit();\" value="
							+ plugin.getDetailTargetLabel() + ">&nbsp;&nbsp;");
		}

		if (plugin.getListOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setAction('list');document.forms[0].submit();\" value=List>&nbsp;&nbsp;");
		}

		if (false && plugin.getPrintOk()) {
			sb
					.append("<input type=button class='stdformtopbutton' onClick=\"setRelation('this');setAction('print');document.forms[0].submit();return;printWindow();return false;\" value=Print>&nbsp;&nbsp;");
		}

		String buttons = sb.toString();

		return buttons;
	}

	public Hashtable getAuditFields(Hashtable ht) {
		
		
		//debug("FormShowWriter.getAuditFields... added_by");

		String[][] reviewedChoices = { { "Y", "P", "R" },
				{ "Passed", "Pending", "Rejected" } };

		ht.put("added_by", new WebFieldDisplay("added_by", plugin
				.getText("added_by")
				+ "&nbsp;on&nbsp;" + plugin.getText("added_date")));

		debug("FormShowWriter.getAuditFields... updated_by");
		
		if (plugin.getIsUpdated()) {
			//debug("FormShowWriter.getAuditFields... its updated");
			ht.put("updated_by", new WebFieldDisplay("updated_by", plugin
					.getText("updated_by")
					+ "&nbsp;on&nbsp;" + plugin.getText("updated_date")));
		} else {
			//debug("FormShowWriter.getAuditFields... not updated");
			ht.put("updated_by", new WebFieldDisplay("updated_by", ""));
		}

		if (!plugin.getShowAuditSubmitApprove()) {
			return ht;
		}

		/*
		 * Submitted By
		 */
		
		//debug("FormShowWriter: getAuditFields");

		if (plugin.getIsSubmitted()) {
			//debug("FormShowWriter: isSubmittted = true");
			
			ht.put("submitted_tag", new WebFieldDisplay("submitted_tag",
					"Submitted By:"));

			ht.put("submitted_by", new WebFieldDisplay("submitted_by", plugin
					.getText("submitted_by")
					+ "&nbsp;on&nbsp;"
					+ plugin.getText("submitted_date")
					+ "<br>" + plugin.getText("submitted_tx")));
		} else {
			//debug("FormShowWriter: isSubmittted = false");
			ht.put("submitted_tag", new WebFieldDisplay("submitted_tag", ""));
			ht.put("submitted_by", new WebFieldDisplay("submitted_by", ""));
		}

		/*
		 * Reviewed By
		 */

		if (plugin.getIsReviewed() || plugin.isPendingReview()) {

			ht.put("reviewed_tag", new WebFieldDisplay("reviewed_tag",
					"Review Status:"));

			ht.put("reviewed_flag", new WebFieldRadio("reviewed_flag", plugin
					.getText("reviewed_flag"), reviewedChoices));

			if (plugin.isPendingReview()) {

			} else {
				ht.put("reviewed_by", new WebFieldDisplay("reviewed_by", "<br>"
						+ plugin.getText("reviewed_by") + "&nbsp;on&nbsp;"
						+ plugin.getText("reviewed_date") + "<br>"
						+ plugin.getText("reviewed_tx")));
			}

		} else {
			ht.put("reviewed_tag", new WebFieldDisplay("reviewed_tag", ""));
			ht.put("reviewed_flag", new WebFieldDisplay("reviewed_flag", ""));
			ht.put("reviewed_by", new WebFieldDisplay("reviewed_by", ""));
		}

		return ht;
	}

	// standard JavaScript to set hidden fields on Form1
	public String getJavaScript() throws services.ServicesException {

		StringBuffer st = new StringBuffer();

		if (plugin.getIsSubmitted()) {
			st.append("\nvar showSubmit=true;");
		} else {
			st.append("\nvar showSubmit=false;");
		}

		if (plugin.getIsReviewed()) {
			st.append("\nvar showReview=true;");
		} else {
			st.append("\nvar showReview=false;");
		}

		st.append("\nvar deleteOk;" + "\nfunction setAction(x){"
				+ "\ndocument.Form1.Action.value=x;}"
				+ "\nfunction setTarget(x){"
				+ "\ndocument.Form1.Target.value=x;}"
				+ "\nfunction setRowKey(x){"
				+ "\ndocument.Form1.RowKey.value=x;}"
				+ "\nfunction setRelation(x){"
				+ "\ndocument.Form1.Relation.value=x;}");

		st
				.append("\n\nfunction printWindow() {var remote = open(\"http://localhost:8080/cmm/Router?Target=Project&Action=Print&RowKey=1&Relation=this\",\"printit\",\"menubar=1,toolbar=0\");\nif (remote.opener == null)\n remote.opener = window;\n remote.opener.name = 'winx';\n }");

		if (plugin.getShowAuditSubmitApprove())
			st.append("\n\nfunction tmInit() {auditInit();toggle();}");
		else
			st.append("\n\nfunction tmInit() {}");

		return st.toString();
	}

}
