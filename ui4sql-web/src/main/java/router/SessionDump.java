/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
/*
 * 
 *	Utility Database Servlet to get a record set and list it 
 *
 *
 *	Limits : only works with rs columns of type string for now.
 * 
 *  Change Log:
 *  
 *  	10/15/08 WebField instead of WebFieldSelect : getHTML 
 *  
 *  	TAG UI4SQL 2.0 fix enumaration declaration
 */


package router;

import forms.WebFieldSelect;

import java.io.IOException;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletConfig;

import java.util.*;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * @version 1.0
 * @author
 */
public class SessionDump extends HttpServlet implements Servlet {

	static final long serialVersionUID = 14314132452776147L;

	private PrintWriter out;

	private StringBuffer integerAttributes;

	private StringBuffer stringAttributes;

	private StringBuffer hashAttributes;

	private HttpSession session = null;

	private ServletConfig conf = null;

	//TAG UI4SQL 2.0 fix raw type
	private Enumeration <String> en = null;

	public void init(ServletConfig pConfig) throws ServletException {
		this.conf = pConfig;

	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		RouteIt(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		RouteIt(req, resp);
	}

	private void RouteIt(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/html");

		session = req.getSession(true);

		out = resp.getWriter();

		/*
		 * Start HTML
		 */
		out.println("<html>");
		out.println(" <head>");
		out.println("  <style type=\"text/css\">");
		out.println("   <!--");
		out.println("    a { text-decoration: none } ");
		out
				.println("	   body { font-family: verdana, helvetica, sans serif; font-size: 10pt; }");
		out.println("   -->");
		out.println("  </style>");
		out.println(" </head>");
		out.println(" <body>");
		out.println("  <center>");
		out.println("   <h3>InfoDumperServlet</h3>");
		out.println("  </center>");

		/*
		 * 
		 */
		out.println("<br>Session Id : " + session.getId() + "<br>");

		StringBuffer sb0 = new StringBuffer();
		//TAG UI4SQL 2.0
		Enumeration  <String> e = conf.getServletContext().getInitParameterNames();
		startTable(sb0, "ServletContext: init parameters");
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			printRow(sb0, name, conf.getServletContext().getInitParameter(name));
		}
		endTable(sb0);
		out.println(sb0.toString());

		/*
		 * Servlet
		 */  
		StringBuffer sb1 = new StringBuffer();
		e = conf.getServletContext().getAttributeNames();
		startTable(sb1, "ServletContext: attribute objects");
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			Object o = conf.getServletContext().getAttribute(name);

			if (false || o.getClass().getName().equalsIgnoreCase("java.lang.String")) {
				printRow(sb1, name, (String) o);
			} else {
				printRow(sb1, name, o.getClass().getName());
			}

		}
		endTable(sb1);
		out.println(sb1.toString());

		StringBuffer sb2 = new StringBuffer();
		e = conf.getInitParameterNames();
		startTable(sb2, "ServletConfig: init parameters");
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			printRow(sb2, name, conf.getInitParameter(name));
		}
		endTable(sb2);
		out.println(sb2.toString());

		/*
		 * System Properties
		 * 
		 * TAG: UI4SQL2.0 .. Need to fix return type of getProperties().propertyNames()
		 */
		//StringBuffer sb3 = new StringBuffer();
		//e = System.getProperties().propertyNames();
		//startTable(sb3, "System properties");
		//while (e.hasMoreElements()) {
		//	String name = (String) e.nextElement();
		//	printRow(sb3, name, System.getProperty(name));
		//}
		//endTable(sb3);
		//out.println(sb3.toString());

		/*
		 * Request Headers
		 */

		StringBuffer sb4 = new StringBuffer();
		e = req.getHeaderNames();
		startTable(sb4, "Request: headers");
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			printRow(sb4, name, req.getHeader(name));
		}
		endTable(sb4);
		out.println(sb4.toString());

		/*
		 * Session Attributes
		 */
		integerAttributes = new StringBuffer();
		stringAttributes = new StringBuffer();
		hashAttributes = new StringBuffer();
		loadSessionBuffers(req);

		out.println("<br><br>" + stringAttributes.toString());
		out.println("<br><br>" + integerAttributes.toString());
		out.println("<br><br>" + hashAttributes.toString());

		/*
		 * 
		 */

		out.println("</BODY></HTML>");

	}

	private void loadSessionBuffers(HttpServletRequest req) {

		Object o;
		String className;



		en = session.getAttributeNames();

		/*
		 * dump out each sesson attribute while (en.hasMoreElements()) {
		 */

		startTable(stringAttributes, "Session Strings");
		startTable(integerAttributes, "Session Integers");
		startTable(hashAttributes, "Session Hashtables");

		while (en.hasMoreElements()) {
			
			
			debug(" session attribute..");
			
			String objectName = (String) en.nextElement();

			o = (Object) session.getAttribute(objectName);

			className = o.getClass().getName();

			debug("object name: " + objectName);
			

			if (className.equalsIgnoreCase("java.lang.String")) {
				printRow(stringAttributes, objectName.toString(), o.toString());

			}

			if (className.equalsIgnoreCase("java.lang.Integer")) {
				printRow(integerAttributes, objectName.toString(), o.toString());

			}

			if (className.equalsIgnoreCase("java.util.Hashtable")) {

				// need info for Integer HashTable. 
				
				WebFieldSelect wf = new WebFieldSelect(objectName.toString(), "",
						(Hashtable) o);
				;

				printRow(hashAttributes, objectName.toString(), wf.getHTML("show"));

				printRow(hashAttributes, objectName.toString(), "");

				
			}

		}

		endTable(stringAttributes);
		endTable(integerAttributes);
		endTable(hashAttributes);

	}

	private void startTable(StringBuffer sb, String title) {
		sb.append("  <h5>" + title + "</h5>");
		sb.append("  <table cellpadding=\"2\" cellspacing=\"2\">");
		sb.append("   <tr bgcolor=\"lightGrey\">");
		sb.append("    <td align=\"center\">Name</td>");
		sb.append("    <td align=\"center\">Value</td>");
		sb.append("   </tr>");
	}

	private void printRow(StringBuffer sb, String n, String v) {
		sb.append("   <tr bgcolor=\"white\">");
		sb.append("    <td align=\"left\">" + n + "</td>");
		sb.append("    <td align=\"left\">" + v + "</td>");
		sb.append("   </tr>");
	}

	private void endTable(StringBuffer sb) {
		sb.append("  </table>");
		sb.append("  <br/>");
		sb.append("  <br/>");
	}
	
	private void debug(String s) {
		System.out.println(s);
	}

}
