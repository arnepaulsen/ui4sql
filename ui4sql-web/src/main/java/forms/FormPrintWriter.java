/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import services.ServicesException;
import router.SessionMgr;
import java.util.Hashtable;

/**
 * @author Arne Paulsen
 * 
 * 5/17/06 New
 * 
 */

public class FormPrintWriter extends FormDataWriter {

	// the dervied form must put out the actual table data
	// public void writeTableData(TableWriter plugin);

	// private FormTemplateWriter templateWriter;

	private String displayMessage;

	public FormPrintWriter(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);

		this.formWriterType = "show";
		this.plugin.formWriterType = "show";

	}

	/*
	 * 
	 * set record position.
	 * 
	 */
	public void preDisplay() {

		Integer rc = new Integer("0");

		String action = "show";
		String relation = "this";

		/***********************************************************************
		 * 
		 * now try to get a record to print
		 * 
		 **********************************************************************/

		Integer rowId;

		// first get the rowId from the parm, keep it
		try {
			rowId = new Integer(sm.Parm("RowKey"));
		} catch (NumberFormatException e) {
			rowId = new Integer(0);
		}

		try {
			plugin.setRowRelation(rowId.toString(), "this");
		} catch (ServicesException e) {
			debug("writeForm:preDisplay - " + e.toString());
		}

	}

	public void writeForm() {

		try {
			out
					.println("<table border='1' cellspacing='1' class='tmtable'><tr align=left><td>");

			/*
			 * Write the template, substitute the tags with webFields values
			 */

			try {
				writeTemplate(plugin.getTemplateName(), "show");

			} catch (Exception e) {
				System.out
						.println("FormPrintWriter:writeForm - except rd.include "
								+ e.toString());
			}
			out.println("</td></tr>");

			debug("print...write template done");

			// out.println("<tr height=50><td valign=bottom>"
			// + this.displayMessage + "</td></tr>");

			// 8/25 add parm / list selector for the list

			out.println("</table>");

			debug("print.writeForm done");

			// if (displayMessage.length() < 1) {
			// displayMessage = new String("Display complete.");
			// }

		} catch (Exception se) {
			out.println("except in show form");
			out.println("\n" + se.toString());
			se.printStackTrace();
		}
	}

	public String getButtons() {

		return "";
	}

	public Hashtable getAuditFields(Hashtable ht) {

		String[][] reviewedChoices = { { "Y", "P", "R" },
				{ "Passed", "Pending", "Rejected" } };

		ht.put("added_by", new WebFieldDisplay("added_by", plugin
				.getText("added_by")
				+ "&nbsp;on&nbsp;" + plugin.getText("added_date")));

		if (plugin.getIsUpdated()) {
			ht.put("updated_by", new WebFieldDisplay("updated_by", plugin
					.getText("updated_by")
					+ "&nbsp;on&nbsp;" + plugin.getText("updated_date")));
		} else {
			ht.put("updated_by", new WebFieldDisplay("updated_by", ""));
		}

		if (!plugin.getShowAuditSubmitApprove())
			return ht;

		/*
		 * Submitted By
		 */

		if (plugin.getIsSubmitted()) {
			ht.put("submitted_tag", new WebFieldDisplay("submitted_tag",
					"Submitted By:"));
			ht.put("submitted_by", new WebFieldDisplay("submitted_by", plugin
					.getText("submitted_by")
					+ "&nbsp;on&nbsp;"
					+ plugin.getText("submitted_date")
					+ "<br>" + plugin.getText("submitted_tx")));
		} else {
			ht.put("submitted_tag", new WebFieldDisplay("submitted_tag", ""));
			ht.put("submitted_by", new WebFieldDisplay("submitted_by", ""));
		}

		/*
		 * Reviewed By
		 */

		debug("isPending " + plugin.isPendingReview());

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

		return "\n\nfunction tmInit() {}";

	}

}
