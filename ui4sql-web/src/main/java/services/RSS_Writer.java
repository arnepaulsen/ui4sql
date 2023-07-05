/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

/*
 * RSS_Writer
 * writes out RSS for a project
 * so far, just writes out the issues table, more to come
 * 
 * 
 * inputs : FileWriter, ProjectId, DbInterface (with open connection)
 * 
 */
import java.io.FileWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import db.DbInterface;

public class RSS_Writer {

	private String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private String rssTag = "<rss xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";

	private String sSelect = "Select issue_id, title_nm, added_date, desc_blob from tissue ";

	public void writeRSS(Connection conn, DbInterface db, FileWriter fw,
			Integer projectId) {

		try {
			fw.write(xmlTag);
			fw.write(rssTag);
			writeIssues(db, fw, projectId);
			fw.write("</RSS>");
			fw.close();
		} catch (IOException io) {
			debug("RSS Writer: " + io.toString());
		}
	}

	private void writeHeader(FileWriter fw) {
		try {
			fw.write(" <Title>Techlaborater:  </Title>");
			fw
					.write(" <Title>You have information to view or work to do. </Title>");
			fw
					.write("<WebPageUrl>http://localhost:8080/CMM/login.html</WebPageUrl>");
		} catch (IOException e) {
		}
	}

	private void writeIssues(DbInterface db, FileWriter fw, Integer projectId) {
		try {
			ResultSet rs = db.getRS(sSelect + " WHERE project_id = "
					+ projectId.toString());

			while (!rs.next()) {
				fw.write("<Items>");
				fw.write("<Title>" + rs.getString("title_nm") + "</Title>");
				fw
						.write("<Link>http://localhost:8080/CMM/Router?Target=Issue&Action=Show&Relation=this&RowKey="
								+ (String) rs.getObject("issue_id").toString()
								+ "</Link>");
				fw.write("<Description>" + rs.getString("desc_blob")
						+ "</Description>");
				fw.write("<PubDate>" + rs.getDate("added_date").toString()
						+ "<PubDate>");
				fw.write("</Items>");
			}
		} catch (ServicesException se) {
			debug("ServiceException: " + se.toString());
		} catch (SQLException se) {
			debug("SQLException: " + se.toString());
		} catch (IOException se) {
			debug("IOException: " + se.toString());

		}
	}

	private void writeRS() {

	}

	private void debug(String parmMsg) {
		if (true)
			System.out.println("RssWriter: " + parmMsg);
	}
}
