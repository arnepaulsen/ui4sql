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

import java.sql.ResultSet;
import java.sql.SQLException;

import db.DbInterface;

public class RSSWriter {

	private String lf = "\n";

	private FileWriter fw;

	private String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private String rssTag = "<RSS xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";

	private String sSelect = "Select issue_id, title_nm, added_date, desc_blob from tissue ";

	public void writeRSS(DbInterface db, FileWriter parmFileWriter,
			Integer projectId) {

		fw = parmFileWriter;

		try {
			writeln(xmlTag);
			writeln(rssTag);
			writeIssues(db, projectId);
			writeln("</RSS>");
			fw.close();
		} catch (IOException io) {
			debug("RSS Writer: " + io.toString());
		}
	}

	

	private void writeIssues(DbInterface db, Integer projectId) {
		try {

			ResultSet rs = db.getRS(sSelect + " WHERE project_id = "
					+ projectId.toString());

			while (rs.next()) {
				writeln("<Items>");
				writeln("<Title>" + rs.getString("title_nm") + "</Title>");
				//writeln("<Link>http://localhost:8080/CMM/Router?Target=Issue&RowKey="
				//		+ (String) rs.getObject("issue_id").toString()
				//		+ ";</Link>");
				writeln("<Description>" + rs.getString("desc_blob")
						+ "</Description>");
				writeln("<PubDate>" + rs.getDate("added_date").toString()
						+ "</PubDate>");
				writeln("</Items>");
			}
		} catch (ServicesException se) {
			debug("ServiceException: " + se.toString());
		} catch (SQLException se) {
			debug("SQLException: " + se.toString());
		}
	}

	private void writeln(String s) {
		try {
			fw.write(s + lf);
		} catch (IOException e) {

		}
	}

	private void debug(String parmMsg) {
		if (true)
			System.out.println("RssWriter: " + parmMsg);
	}
}
