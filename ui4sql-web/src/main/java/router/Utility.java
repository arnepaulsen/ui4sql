/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
/*
 * 
 *	Utility Database Servlet to get a record set and list it 
 *
 *	Limits : only works with rs columns of type string for now.
 * 
 */

package router;

import java.io.IOException;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import db.DbConnectionFactory;
import db.DbInterface;

import java.io.InputStream;
import java.io.PrintWriter;

/**
 * @version 1.0
 * @author
 * 
 * change"
 * 
 * 8/10/06 - assume dbProduct is MySql..
 * 				need to get it from the props file
 */
public class Utility extends HttpServlet implements Servlet {

	static final long serialVersionUID = 45673414325476147L;
	
	private Connection connection;

	private PrintWriter out;

	private DbInterface db = null;

	Properties prop = new Properties();

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
		out = resp.getWriter();
		
		String product = "MySQL";
		//String url  = "jdbc:mysql://localhost/comtechm_pmo";
		//String id = "pmo";
		//String pw = "pmo";
		
		String url  = "jdbc:mysql://localhost/comtechm_pmo";
		String id = "comtechm";
		String pw = "pmo";
		
		
		DbConnectionFactory factory ;
		
		
		// prop = getSystemProperties();

		/*
		 * get DB connection
		 */
	

		try {
			
			factory = new DbConnectionFactory();
			
			connection = factory.getConnection(product, url, id, pw);
			
			db = new DbInterface(connection, "MySQL");
			
		} catch (Exception se) {
			debug("openConnection Failed: " + se.toString());
			
			out.println("<HTML><BODY>");
			out.println(se.toString());
			out.println("</BODY></HTML>");
			return;
			
		}


	
		out.println("<HTML><BODY>");
		out.println(getHTML(req));
		out.println("</BODY></HTML>");

	}

	private String getHTML(HttpServletRequest req) {

		StringBuffer sb = new StringBuffer();
		ResultSetMetaData rsmd;
		
		String query = req.getParameter("query");
		
		//String query1 = "select user_id, division_id, handle, first_name, last_name from tuser";
		

		sb.append("<Table border=1>");

		try {
			ResultSet rs = db.getRS(query);
			rsmd = rs.getMetaData();

			int columns = rsmd.getColumnCount();

			//debug(sb, " column count " + columns);

			/*
			 * print the column names
			 */
			sb.append("<TR>");
			for (int c = 1; c < columns + 1; c++) {
				sb.append("<TD>");
				sb.append(rsmd.getColumnName(c));
				sb.append("</TD>");
			}
			sb.append("</TR>");

			while (rs.next() == true) {
				sb.append("<TR>");
				for (int i = 1; i < columns + 1; i++) {
					debug(" gettign column : " + i);
					debug(" value...");
					debug(" value is : " + (String) rs.getString(i));
					sb.append("<TD>");
					sb.append(rs.getString(i));
					sb.append("</TD>");

				}
				sb.append("</TR>");
			}

		}

		catch (services.ServicesException se) {
			sb.append(" Service Exception : " + se.toString());
			//return sb.toString();
			//return ("Utility, ServicesException : bad query");
		}

		catch (SQLException sql) {
			sb.append(" SQL exception : " + sql.toString());
			//return sb.toString();
			//return ("got sql exception... bad query");
		}

		sb.append("</TABLE>");
		return sb.toString();
	}

	

	// ************************
	// always close connection
	// ************************
	private void closeConnection() {
		try {
			connection.close();
		} catch (SQLException se) {

			System.out.println("DbInterface:openConnection: " + se.toString());
		}
	}

	public Properties getSystemProperties() {

		Properties prop = new Properties();

		try {
			InputStream is = getClass().getResourceAsStream(
					"/cmm/properties/cmm.properties");
			prop.load(is);
		} catch (Exception e) {
			System.out
					.println("Session Manager : exception loading properties");
			debug("Session Manager : exception loading properties");
			debug(e.toString());

		}
		return prop;
	}
	

	public void debug (StringBuffer sb, String msg) {
		sb.append(msg);
	}

	public void debug(String parmMsg) {
		if (true)
			System.out.println("Utility: " + parmMsg);
	}

}
