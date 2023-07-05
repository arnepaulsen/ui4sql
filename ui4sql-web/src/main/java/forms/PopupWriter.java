/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen, all rights reserved.
 * 
 *
 * Change Log : 
 * 
 * Description: 
 * 	Show a pop-up window of the Remedy Elective Attributes for an SR/RFC  !!!
 * 
 * 	The window just dumps each attribute to a table row.. nothing pretty now.
 * 
 * change:
 * 	10/22/08 watch out for null elective attributes
 *  7/2/09 - remove debug statgement for "C"
 *  
 */

import remedy.RemedyChangeQuery;
import router.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import services.RemedyChangeRequest;

public class PopupWriter {

	public String formName = "";

	public SessionMgr sm;

	public WebLineWriter out;

	public HttpServletRequest req;

	public HttpServletResponse resp;

	// *****************************
	// CONSTRUCTORS
	// *****************************
	public PopupWriter(router.SessionMgr parmSm, WebLineWriter parmOut) {
		sm = parmSm;
		out = parmOut;

		req = parmSm.getRequest();
		resp = parmSm.getResponse();

	}

	public void writeWindow() {
		out.println("<html><body>");
		out.println("<table>");

		/*
		 * Now get and post the Elective Attributes (business need
		 * justification)
		 * 
		 */

		String rfc_no = "0";
		String remedy_result = new String("");

		try {
			rfc_no = sm.getRfcNo();
		} catch (Exception e) {
			debug("Popup: RFC not found in session mgr. ");
		}
		
	
		out.println("<tr><td>RFC#</td><td>" + rfc_no + "</td></tr>");
		out.println("<tr><td><b>Basic Info:</b></td><td></td></tr>");

		String formatted_chg_id = formatChangeId(rfc_no);

		// connect to remedy with id/pw
		RemedyChangeQuery remedy = new RemedyChangeQuery(sm.getRemedyUserid(),
				sm.getRemedyPassword(), sm.getRemedyURL());

		// get rfc info in xml form
		String xml = remedy.GetChangeInfo(formatted_chg_id); // get rfc to

		RemedyChangeRequest rfc = new RemedyChangeRequest(xml);

		if (rfc.getChangeCount() > 0) {

			out.println("<tr><td>Planned Start</td><td>"
					+ rfc.getPlannedStartDate().replaceAll("T", " ")
					+ "</td></tr>");
			out.println("<tr><td>Planned End</td><td>"
					+ rfc.getPlannedEndDate().replaceAll("T", " ")
					+ "</td></tr>");

			out.println("<tr><td>Build Plan</td><td>"
					+ rfc.getImplementationPlan() + "</td></tr>");
			out.println("<tr><td>Test Plan</td><td>" + rfc.getTestPlan()
					+ "</td></tr>");
			out.println("<tr><td>Backout Plan</td><td>" + rfc.getBackoutPlan()
					+ "</td></tr>");

		}

		remedy.RemedyAttributeInfo remedyAttributes = new remedy.RemedyAttributeInfo(
				sm.getRemedyUserid(), sm.getRemedyPassword(), sm.getRemedyURL());

		String xml2 = remedyAttributes.GetAttributeInfo(formatted_chg_id);

		/*
		 * April Release, add Sox info to tRemedy table only
		 */
		rfc.initAttributeInfo(xml2, "trfc");

		String[][] attrs = rfc.electiveAttributes;

		if (rfc.electiveAttributes != null) {

			out.println("<tr><td><b>Addtional Info:</b></td><td></td></tr>");

			for (int x = 0; x < attrs[0].length; x++) {

				out.println("<tr><td>" + attrs[0][x] + "</td><td>"
						+ attrs[1][x] + "</td></tr>");

			}
		}

		out.println("</table>");

		out.println("</body></html>");

	}

	private String formatChangeId(String rfcNo) {

		String chg_id = rfcNo;

		while (chg_id.length() < 12)
			chg_id = "0" + chg_id;

		chg_id = "CHG" + chg_id;

		return chg_id;

	}

	// *****************************
	// UTILITY
	// *****************************
	public void debug(String parmMsg) {
		System.out.println(parmMsg);

	}

}
