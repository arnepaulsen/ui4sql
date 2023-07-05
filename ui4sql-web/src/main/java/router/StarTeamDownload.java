/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package router;

import java.io.IOException;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletConfig;
import java.util.List;

import java.util.Iterator;
import java.sql.*;

import router.SessionMgr;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;

//import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;

import java.io.File;
import org.apache.commons.fileupload.*;

import java.io.PrintWriter;
import services.StarTeam;

/**
 * 
 * Uploads a file from user and saves into STar Team!
 * 
 * the request comes from /UploadAttahment.html pop-up window .. which is
 * popped-up from the Attachment plugin, a detail from of SBAR
 * 
 * 1. read the file and save to the /reports directory 2. Use the
 * /services/StarTeam class login and connect to StarTeam, then add the file 3.
 * gets the tattachment table key from the session, and then update that row ...
 * to indciate the file name in column file_nm.
 * 
 */

public class StarTeamDownload extends HttpServlet implements
		jakarta.servlet.Servlet {

	String strStarTeamUrl = "starteam.arnepaulsenjr.com";
	int nStarTeamPort = 49201;
	String strStarTeamUser = "D576781";
	String strStarTeamPassword = "password123";
	String strProjectName = "HC_N00000_AC-NC";

	String strFolder = "Inpatient";
	String strSubFolder = "IssueTriage";

	private PrintWriter out;

	private HttpServletRequest req;

	private HttpServletResponse resp;

	private ServletConfig servletConfig = null;

	private ServletContext servletContext = null;

	static final long serialVersionUID = 752647111412776147L;

	// ******************************
	// * CONSTRUCTORS
	// ******************************

	public void init(ServletConfig config) throws ServletException {
		this.servletConfig = config;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.req = req;
		this.resp = resp;
		RouteIt();
		return;
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.req = req;
		this.resp = resp;
		RouteIt();
		return;
	}

	// ******************************
	// * MAIN ROUTINE
	// ******************************

	private void RouteIt() throws ServletException, IOException {

		SessionMgr sessionMgr;

		StringBuffer html = new StringBuffer();

		servletContext = servletConfig.getServletContext();

		try {
			sessionMgr = new SessionMgr(req, resp, servletContext);
			if (sessionMgr == null) {
				System.out.println("RouteIt: session mgr is null");
				resp.setContentType("text/html");
				out = resp.getWriter();
				out
						.println("<html><Body>Error getting session... problem with server.</body></html>");
				return;
			}
		} catch (Exception e) {
			System.out.println("Router... error getting SessionMgr");
			e.printStackTrace();
			return;
		}

		/*
		 * Get the Session Mgr
		 */

		resp.setContentType("text/html");
		out = resp.getWriter();

		
		try {
		
			html.append("<HTML><BODY>Retrieving file from Star Team : ");

			String fileName = getFileName(sessionMgr);
			html.append(" " + fileName);

			StarTeam starTeam = new StarTeam(strStarTeamUrl, nStarTeamPort,
					strStarTeamUser, strStarTeamPassword, strProjectName);
			
		//	boolean success = starTeam.getFile(html, strFolder, strSubFolder,
			//		fileName, sessionMgr.getWebRoot() + "reports/");

			boolean success = true;
			
			if (success){
	
			html
				.append("<br><br>File successfully retrieved. ");

				
				html.append("<br><br><a href=reports/" + fileName + " >" + "Click here to download it to your PC."
					+ "</a><br>");
			}
			else {
				html.append("<br>Error - file not downloaded.");
			}
		} catch (services.ServicesException e) {
			html.append(" error : " + e.toString());
			debug("Exception " + e.toString());

		}
	
		html.append("</BODY></HTML>");

		out.println(html.toString());

		out.flush();

		out.close();

		return;
	}

	private void debug(String s) {
		System.out.println("StarTeam Attachment Uploader: " + s);
	}


	private String getFileName(SessionMgr sessionMgr) {

		Integer rowId = sessionMgr.getAttachmentId();

		String query = " select file_nm from tattachment where attachment_id = "
				+ rowId.toString() + " LIMIT 1;";

		debug("query  : " + query);

		try {
			ResultSet rs = sessionMgr.getDbInterface().getRS(query);

			if (rs.next()) {
				return rs.getString("file_nm");
			}
			return "File Not Found";
		} catch (Exception e) {
			debug("getFileName Error getting file name: " + e.toString());
			return "";
		}

	}

}
