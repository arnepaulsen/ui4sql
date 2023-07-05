/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import router.SessionMgr;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * @author Arne Paulsen (c) 2006
 * 
 *         7/7/05 write menuloader.js from lib jar file instead or
 *         RequestDispatch 11/9/05 add logging-in logic 5/17/06 add printPage
 *         12/7/06 add datepicker.js
 * 
 *         4/19/ block Home and Inbasket links on register form
 * 
 *         11/16 add 'metadata content=expires to prevent cache pages.
 * 
 *         3/29/09 - move styles into /styles directory
 * 
 *         7/26/09 - remove in-basket link.. not used
 * 
 */
public class WebPageWriter {

	boolean debug = true;

	boolean logging_in = false;

	String httpOffset; // set to "../" when running at ISP

	WebLineWriter out;

	HttpServletRequest req;

	HttpServletResponse resp;

	String title;

	static String productName = "UI4SQL - Sample Application";

	String bottomMsg = "";

	String formClassName = "";

	router.SessionMgr sm;

	private static String copyright = new String("<p class=copyright></p>");

	private static String sponsored = "<b>Proprietary & Confidential. For Internal Use Only.</b>";

	// "<p class=copyright>Copyright &copy; 2006 cmm-Frameworks, Inc.</p>");

	// **********************
	// Constructors
	// **********************

	public WebPageWriter(SessionMgr s, WebLineWriter pw, HttpServletRequest parmReq, HttpServletResponse parmResp) {

		sm = s;
		out = pw;
		req = parmReq;
		resp = parmResp;

		// httpOffset = (String) sm.getServletContext().getAttribute(
		// "prop_http_offset");

		httpOffset = (String) sm.getServletContext().getInitParameter("HTTP-Offset");

		// debug("Session Id: " + s.getId());
	}

	/*
	 * When not logged in, or no session available
	 * 
	 * Watch out... we have no session
	 */
	public WebPageWriter(WebLineWriter pw, HttpServletRequest parmReq, HttpServletResponse parmResp) {

		out = pw;
		req = parmReq;
		resp = parmResp;

		logging_in = true;

		httpOffset = ""; // could be wrong

	}

	/*
	 * 
	 * Really its not write page... it's just writting the form in the middle
	 */
	public void writePage(FormWriter fw) {

		// debug("WebPageWriter:writePage - Jan 12, 09 ver 2");

		// debug(" fw name : " + fw.getClass().getName());

		formClassName = fw.getClass().getName();
		logging_in = logging_in || formClassName.equalsIgnoreCase("forms.LoginForm");

		try {

			fw.preDisplay();

			out.println("<HTML>");
			out.println("<HEAD>");
			out.println("<META HTTP-EQUIV=\"EXPIRES\" CONTENT=\"0\">");

			out.println("<TITLE>" + fw.getBrowserTitle() + "</TITLE>");

			if (!formClassName.equalsIgnoreCase("forms.LoginForm")) {
				out.println(
						"<script src=\"" + httpOffset + "javascript/validate.js\"  type=\"text/javascript\"></script>");

				out.println("<script src=\"" + httpOffset + "javascript/ajax.js\"  type=\"text/javascript\"></script>");

				// out.println("<script src=\"" + httpOffset
				// + "javascript/datetimepicker.js\" type=\"text/javascript\"></script>");

				// TAG VER 1.0

				out.println("<link rel=\"stylesheet\" href=\"" + httpOffset + "styles/menustyle.css\">");

				out.println("<script src=\"" + httpOffset + "javascript/menuoptions.js\"></script>");

			}

			out.print("<link rel=\"stylesheet\" href=\"" + httpOffset + "styles/");

			if (logging_in)
				out.print("Style.css");
			else
				out.print(sm.getStyle());

			out.println("\">");

			/*
			 * dump JavaScript from the Plugin.. stuff like date and integer edits.
			 * 
			 */

			// TAG:UI4SQL2.0 REMOVE JAVASCRIPT START AND END TAGS <!-- ... --> FOR HTML5
			out.println("<script >\n");
			out.println(fw.getJavaScript());
			out.println("\n\n</script>");

			out.println("</HEAD>");
			out.println("<BODY topmargin=0 marginheight=0 leftmargin=0 marginwidth=0 onLoad=tmInit()> ");

			/*
			 * The whole page
			 */

			out.println("<TABLE  width='100%' height='100%' BORDER='0' CELLSPACING='0' CELLPADDING='0' >");

			/*
			 * Line #1 - Title
			 */

			// bgcolor=#2d5b8d
			// <img src="/docushare/images/KP_header_logo.gif" border="0"
			// width="184" height="23" alt="Home" title="Home"
			// class="midalign"></img>
			out.println("<TR  HEIGHT=26>" + "<td><TABLE WIDTH=100% BORDER='0' CELLSPACING='0' CELLPADDING='0'><TR>"
					+ "<TD bgcolor=white ALIGN=LEFT VALIGN=MIDDLE WIDTH=75><img src='" + httpOffset
					+ "images/logo.png' border='0' width='184' height='23' alt='Home' title='Home'></TD>"
					+ "<TD ALIGN=LEFT VALIGN=MIDDLE WIDTH=125><font size=2 color=red>&nbsp;</TD>"
					+ "<TD VALIGN=CENTER ALIGN=center><font size=4 color=#484848>" + productName + "</font></td>"
					+ "<TD ALIGN=RIGHT WIDTH=200><font size=1 color=WHITE>");

			if (!logging_in) {
				out.println("<A class=blackButton HREF=Router?Target=Login&Action=Logout>Logout</a>");
			}

			out.println("&nbsp;&nbsp;</font></TD>" + "</TR></TABLE></td>" + "</TR>");

			/*
			 * Line #2 - Toolbar
			 */

			out.println("<TR  bgcolor=#ededed HEIGHT=27><TD align=center width='100%' ALIGN=left>&nbsp;</td></tr>");

			/*
			 * Line #3 - Sub-Title (mostly from the dataManaagers)
			 */

			out.println("<TR bgcolor=#F0F0F0 HEIGHT=25>"
					+ "<TD><table width=100% BORDER='0' CELLSPACING='0' CELLPADDING='0'><tr>"
					+ "<td ALIGN=LEFT VALIGN=CENTER>" + "<span class=regtext>&nbsp;&nbsp;" + fw.getSubTitle()
					+ "</span></td>" + "<TD align=right>");

			// print Home link if not already there.
			if (!logging_in) {
				if (!sm.getTarget().equalsIgnoreCase("home") && !sm.getTarget().equalsIgnoreCase("register")
						&& !sm.Parm("Action").equalsIgnoreCase("sendpassword")) {

					out.println("<A class=blackButton HREF=Router?Target=guess&Action=list>Home</a>&nbsp;&nbsp;");

					// out
					// .println("<A class=blackButton
					// HREF=Router?Target=Home>Inbasket</a>&nbsp;&nbsp;");

				}
			}

			/*
			 * Show Link to either User Preferences or Password Change Page
			 */

			if (!fw.isAdminFunction() && !logging_in) {

				out.println("<A class=blackButton HREF=Router?Target=Project&Action=List>Dashboard</a>&nbsp;&nbsp;");

				if (sm.getTarget().equalsIgnoreCase("preference")) {
					out.println("<A class=blackButton HREF=Router?Target=Password&Action=Edit&Relation=this&RowKey="
							+ sm.getUserId().toString() + ">Password</a>&nbsp;&nbsp;");
				} else {
					out.println("<A class=blackButton HREF=Router?Target=Preference&Action=Show&Relation=this&RowKey="
							+ sm.getUserId().toString() + ">Preferences</a>&nbsp;&nbsp;");
				}
			}

			// print the 'Admin' link if user is administrator, and not already
			// on the admin pages.

			if (!logging_in && !fw.isAdminFunction() && sm.userIsAdministrator()) {
				out.println(
						"<A class=blackButton HREF=Router?Target=Options&Action=Show&Relation=this&RowKey=1>System Admin.</a>"
								+ "&nbsp;&nbsp;");

			}
			out.println("</TD></tr></table></td></tr>");

			/*
			 * Line #4 - Image divider
			 */

			out.println("<tr valign=top bgcolor=#ffffff><td><img src='" + httpOffset
					+ "images/BarShadow.gif' height=5 width='100%'></td></tr>");

			/*
			 * Line #4 - Spacer
			 */

			out.println("<TR  bgcolor=#ffffff HEIGHT=10><TD width='100%' ALIGN=left>&nbsp;</td></tr>");

			/*
			 * Line #5 - Content -
			 */

			out.println("<tr  VALIGN=TOP><TD>");

			out.println("<table border=0 cellpadding=0 cellspacing=0><tr bgcolor=#FFFFFF>");

			// left side
			out.println("<td width=20 valign=top>"); // width='20%' 3/13/08
			// narrow the left side
			// on list

			fw.writeMenu();
			out.println("</td>");

			// right side
			out.println("<td bgcolor=#FFFFFF align=left>");

			fw.writeForm();
			out.println("</td></tr></table>");

			out.println("</TD></TR>");
			out.println("<TR  class='copyright' align=bottom HEIGHT='30px'><TD align=center bgcolor=#ededed >"
					+ copyright + (logging_in ? sponsored : "") + "</TD></TR>");
			out.print("</TABLE>");

			// keep toolbar javascript inside the body, don't put in the header.
			// As per DMB tech support.

			if (!formClassName.equalsIgnoreCase("forms.LoginForm_abc")) {
				// TAG VER 1.0
				out.println("<script src=\"" + httpOffset + "javascript/menu.js\"></script>");

				if (fw.isAdminFunction())
					out.println("<script src=\"" + httpOffset + "javascript/menucontext2.js\"></script>");
				else
					out.println("<script src=\"" + httpOffset + "javascript/menucontext.js\"></script>");

				out.println("<script src=\"" + httpOffset + "javascript/menuupdate.js\"></script>");
				out.println("<script >showToolbar();</script>");

			}

			out.println("</BODY></HTML>");

		} catch (Exception ioe) {
			System.out.println("WebPageWriter:writePage fw : " + ioe.toString());
		}

	}

	/*
	 * Put out page for printing.
	 * 
	 * .... debug warning - this is not the writePage method
	 * 
	 */
	public void printPage(FormWriter fw) {
		// debug("WebPageWriter:printPage - Vers Jan 04, 07");

		// debug(" fw name : " + fw.getClass().getName());
		formClassName = fw.getClass().getName();

		try {

			fw.preDisplay();

			out.println("<HTML>");
			out.println("<HEAD>");

			out.println("<TITLE>" + fw.getBrowserTitle() + "</TITLE>");

			out.print("<link rel=\"stylesheet\" href=\"" + httpOffset + "Print.css" + "\">");

			/*
			 * dump out the JavaScript from the Plugin.. stuff like date and integer edits.
			 */

			out.println("<script >\n<!--");
			out.println(fw.getJavaScript());
			out.println("\n//-->\n</script>");

			out.println("</HEAD>");
			out.println("<BODY topmargin=0 marginheight=0 leftmargin=0 marginwidth=0 onLoad=tmInit()> ");

			/*
			 * The whole page
			 */

			out.println("<TABLE  class='tmtable' width='100%' height='100%'  >");

			/*
			 * Line #3 - two-column table to hold left and right sides
			 */

			out.println("<tr  VALIGN=TOP><TD>");

			out.println("<TABLE class='tmtable'><tr bgcolor=#FFFFFF>");

			// left side

			out.println("<td width=60 valign=top>"); // width='20%'
			fw.writeMenu();
			out.println("</td>");

			// right side
			out.println("<td bgcolor=#FFFFFF align=left>");

			fw.writeForm();
			out.println("</td></tr></table>");

			out.println("</TD></TR>");
			out.println("<TR  class='copyright' align=bottom HEIGHT='30px'><TD align=center bgcolor=#ededed >"
					+ copyright + "</TD></TR>");
			out.print("</TABLE>");

			out.println("</BODY></HTML>");

		} catch (Exception ioe) {

			System.out.println("WebPageWriter:printPage fw : " + ioe.toString());
		}

	}

	/*
	 * Write the Beginning HTML
	 * 
	 */

	/*
	 * Write a file from the Lib directory to the web. This is used instead of
	 * RequestDispather because RD processes the file... that is, translates it.
	 * 
	 * RD will look at the file and try to instantiate objects if it sees stuff like
	 * src="", which is in the menuLoader.js files.
	 * 
	 * So use this read/write instead to bypass RequestDispatch processing
	 * 
	 * the resource should be a path in a jar file in the lib directory, parallel to
	 * the web-inf classes.
	 */
	public void writeResource(String resource, WebLineWriter pw) {

		try {
			// debug("the resource is : " + resource);

			BufferedReader br;
			// debug("get InputStream - using getresource as stream");
			InputStream is = getClass().getResourceAsStream(resource);
			// debug("get InputStreamReader using is");
			InputStreamReader propReader = new InputStreamReader(is);

			// debug("getBuffered REader");
			br = new BufferedReader(propReader);

			// debug("reading loop");
			while (br.ready()) {
				pw.println(br.readLine());
			}

		}

		catch (Exception ioe) {

			pw.println("WebPageWritter : Error writting source file :  " + ioe.toString());
		}

	}

	public void debug(String debugMsg) {
		// System.out.println (debugMsg);

		if (this.debug)
			sm.debug(debugMsg);
		Logger.getLogger("ui4sql").debug(debugMsg);
	}

}
