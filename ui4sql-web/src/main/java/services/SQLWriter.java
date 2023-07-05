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

public class SQLWriter {

	private String lf = "\n";

	private FileWriter fw;

	private String sSelect = "Select table_nm from tform  where table_nm = 'texpectation'  ";

	public void writeSQL(DbInterface db, FileWriter parmFileWriter,
			Integer projectId) {

		fw = parmFileWriter;

		try {

			writeSQLChanges(db);

			fw.close();
		} catch (IOException io) {
			debug("RSS Writer: " + io.toString());
		}
	}

	private void writeSQLChanges(DbInterface db) {
		try {

			ResultSet rs = db.getRS(sSelect);

			while (rs.next()) {

				//writeln("alter table "
				//		+ rs.getString("table_nm")
					//	+ " ADD COLUMN `submitted_tx` VARCHAR(255) AFTER `approved_flag`;\n ");
				
				//writeln("update cmm." + rs.getString("table_nm")
					//	+ " set added_date = '2006/01/01 08:00'; \n ");

				if (true) {
					writeln("alter table " + rs.getString("table_nm")
							+ " drop column added_date; \n ");

					writeln("alter table " + rs.getString("table_nm")
							+ " drop column updated_date; \n ");
					writeln("alter table " + rs.getString("table_nm")
							+ " drop column submitted_date; \n ");
					writeln("alter table " + rs.getString("table_nm")
							+ " drop column approved_date ; \n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " CHANGE COLUMN `updated_uid` `updated_uid` INTEGER ; \n ");
					writeln("alter table "
							+ rs.getString("table_nm")
							+ " CHANGE COLUMN `added_uid` `added_uid` INTEGER; \n ");
					writeln("alter table "
							+ rs.getString("table_nm")
							+ " CHANGE COLUMN `submitted_uid` `submitted_uid` INTEGER; \n ");
					writeln("alter table "
							+ rs.getString("table_nm")
							+ " CHANGE COLUMN `approved_uid` `approved_uid` INTEGER; \n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `reviewed_uid` INTEGER AFTER submitted_uid ;\n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `added_date` DATETIME DEFAULT '0000-00-00 00:00:00' AFTER `approved_uid`;\n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `updated_date` DATETIME DEFAULT '0000-00-00 00:00:00' AFTER `added_date`;\n ");
					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `submitted_date` DATETIME DEFAULT '0000-00-00 00:00:00' AFTER `updated_date`;\n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `reviewed_date` DATETIME DEFAULT '0000-00-00 00:00:00' AFTER `submitted_date`;\n ");
					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `approved_date` DATETIME DEFAULT '0000-00-00 00:00:00' AFTER `reviewed_date`;\n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `reviewed_flag` CHAR DEFAULT 'N' AFTER `approved_date`;\n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `approved_flag` CHAR DEFAULT 'N' AFTER `reviewed_flag`;\n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `reviewed_tx` VARCHAR(255) AFTER `approved_flag`;\n ");

					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `approved_tx` VARCHAR(255) AFTER `reviewed_tx`;\n ");
					
					writeln("alter table "
							+ rs.getString("table_nm")
							+ " ADD COLUMN `submitted_tx` VARCHAR(255) AFTER `approved_flag`;\n ");
					
				}

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
