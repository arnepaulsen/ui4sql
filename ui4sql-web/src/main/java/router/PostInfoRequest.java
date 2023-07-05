/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package router;

/**
 * @version 	1.0
 * @author
 * 
 * 1. Receive Post from url html/Contact.html
 * 2. insert contract info to table 'twebinforequests
 * 3. return simple thank you page with like back to ccm home  * and return a thank you page. 
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.*;

import db.DbConnectionFactory;
import services.ServicesException;

/**
 * @version 1.0
 * @author
 * 
 * This servlet receives a request-for-information (if anyone really ever
 * inquires) from the marketing web site. It's a straight form with email, name,
 * etc.
 * 
 * It doesn't run under the CMM application (no toolbar, formWritter etc. )
 * 
 * The servlet just posts the info to the database, and responds with 'thank
 * you' and a link back to the main web site.
 * 
 * 7/21 re-did the connection to just call dbConnectionFactory with no
 * parameters.
 * 
 */
public class PostInfoRequest extends HttpServlet {

	private PrintWriter out;

	Properties prop = new Properties();

	private Connection connection;
	
	static final long serialVersionUID = 143124534412776147L;
	

	//private java.util.Properties requestParms;

	/**
	 * @see jakarta.servlet.http.HttpServlet#void
	 *      (jakarta.servlet.http.HttpServletRequest,
	 *      jakarta.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		RouteIt(req, resp);
	}

	/**
	 * @see jakarta.servlet.http.HttpServlet#void
	 *      (jakarta.servlet.http.HttpServletRequest,
	 *      jakarta.servlet.http.HttpServletResponse)
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		RouteIt(req, resp);
	}

	// ******************************
	// * MAIN ROUTINE
	// ******************************

	private void RouteIt(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html");
		out = resp.getWriter();

		prop = getSystemProperties();

		String product = "MySQL";
		String url  = "jdbc:mysql://localhost/comtechm_pmo";
		String id = "comtechm";
		String pw = "pmo";

		
		/*
		 * get DB connection
		 */
		DbConnectionFactory factory = new DbConnectionFactory();

		try {
			
			connection = factory.getConnection(product, url, id, pw);
	
			
			//connection = factory.getConnection(prop);
		} catch (Exception se) {
			String props = prop.toString();
			System.out.println("Connection for properties : " + props);
			System.out.println("openConnection Failed: " + se.toString());
		}

		/*
		 * post request information to database
		 */
		
		postIt(req);
		
		/*
		 * close connection 
		 */
		
		closeConnection();

		/*
		 * return HTML response
		 */
		String offset = (String) prop.getProperty("http_offset");

		out.println("Thank you. <a href='" + offset
				+ "html/index.html'>Back to Home page.</a> ");

		return;
	}

	public void postIt(HttpServletRequest req) {
		/*
		 * build query from input FORM
		 */

		String email = new String(req.getParameter("email"));
		String lastName = new String(req.getParameter("name"));
		String info = new String(req.getParameter("other"));
		String phone = new String(req.getParameter("phone"));

		String insertQry = new String(
				"insert into twebinforequests (email, lastname, phone, info) values ('"
						+ email + "','" + lastName + "','" + phone + "','"
						+ info + "')");

		try {
			runQuery(insertQry);
			// debug("run query goes here");

		} catch (Exception e) {
			debug("<br> query : " + insertQry + "<br>");
			debug("error running insert " + e.toString());
		}

	}

	public boolean runQuery(String parmQuery) throws services.ServicesException {

		Statement stmt;
		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(parmQuery);
			// debug("DbInterface:runQuery : " + parmQuery);
		} catch (SQLException se) {

			throw new ServicesException(se.toString());
		}

		return true;
	}

	// ************************
	// NOT USED
	// ************************
	//private void showStatus() {
	//	try {
	//		out.println("is closed: " + connection.isClosed());
	//	} catch (SQLException se) {
//
//			System.out.println("DbInterface:openConnection: " + se.toString());
	//	}
	//}

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

	// ************************
	// always close connection
	// ************************
	private void closeConnection() {
		try {
			connection.close();
		} catch (SQLException se) {
			out.println("close error" + se.toString());
			System.out.println("PostInfoRequest:closeConnection: "
					+ se.toString());
		}
	}

	public void debug(String parmMsg) {
		System.out.println("PostInfoRequest: " + parmMsg);
	}

}

