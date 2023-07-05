/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package batch;

import forms.HtmlHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import batch.BatchSQL;
import services.RemedyChangeRequest;

import services.ServicesException;
import java.sql.*;
import java.util.Date;
import java.util.Properties;

/*******************************************************************************
 * 
 * @author PAULSEAR
 * 
 * Use RemedyChangeRequest service to download set of RFC's
 * 
 * 11/18/10 Include "Resloved" RFC/SR's in the selection.  
 * 
 * 
 * 
 */
public class RemedyRFCDownload {

	/**
	 * @param args
	 * 
	 * Main - Loop through all trfc open records, and post remedy XML into to
	 * the database.
	 * 
	 */
	public RemedyRFCDownload(String[] args) {
		super();

	}

	public void run(String[] args) {

		String remedyURL = "";
		String remedyUserid = "";
		String remedyPassword = "";
		int counter = 0;

		// default target table to the original trfc
		String tableName = "trfc";
		String tableKey = "rfc_id";
		String remedyKey = "rfc_no";

		if (args.length > 3) {
			tableName = args[1];
			tableKey = args[2];
			remedyKey = args[3];

		}

		// System.out.println(" Target table : " + tableName + " , key : " +
		// tableKey);

		/*
		 * get Remedy connection properties
		 */
		try {
			// FileInputStream is = new FileInputStream(propFileName);
			// System.out
			// .print("PMO Tool - Update RFCs from Remedy. Starting.");

			FileInputStream is = new FileInputStream(args[0]);

			Properties properties = new Properties();
			properties.load(is);

			remedyUserid = properties.getProperty("REMEDY-USERID");
			remedyPassword = properties.getProperty("REMEDY-PASSWORD");
			remedyURL = properties.getProperty("REMEDY-URL");

			is.close();

		} catch (IOException e) {
			System.out.println("...Rerror loading properties file.");
			System.out.println(e.toString());
			System.exit(1);
		}

		remedy.RemedyChangeQuery remedyRFC = new remedy.RemedyChangeQuery(
				remedyUserid, remedyPassword, remedyURL);

		remedy.RemedyAttributeInfo remedyAttributes = new remedy.RemedyAttributeInfo(
				remedyUserid, remedyPassword, remedyURL);

		remedy.RemedyCommentQuery remedyComments = new remedy.RemedyCommentQuery(
				remedyUserid, remedyPassword, remedyURL);

		BatchSQL batchSQL = new BatchSQL(args[0]);

		Connection connection = batchSQL.getConnection();

		String query = "SELECT " + tableKey + "," + remedyKey
				+ ", status_cd from " + tableName
				+ " where status_cd <> 'Clo' ";

		// System.out.println ("SQL : " + query);

		try {
			ResultSet rs = batchSQL.getRS(query);

			int z = 0;

			while (rs.next() && counter < 500) {

				int rfc_id = rs.getInt(1);
				int rfcNo = rs.getInt(2);

				z++;

				System.out
						.println("RemedyChangeRequest: Updating PMO database for RFC "
								+ rfcNo);

				counter++;

				// convert the rfc# into 'CHG000000000000'
				String formattedRfcNo = "" + rfcNo;

				while (formattedRfcNo.length() < 12)
					formattedRfcNo = "0" + formattedRfcNo;

				formattedRfcNo = "CHG" + formattedRfcNo;

				String xml = remedyRFC.GetChangeInfo(formattedRfcNo); // get

				String xml2 = remedyAttributes.GetAttributeInfo(formattedRfcNo);

				RemedyChangeRequest rfc = new RemedyChangeRequest(xml);

				if (rfc.getChangeCount() > 0) {
					rfc.rfcToDatabase(connection, tableName, tableKey, rfc_id);
				} else {
					System.out.println("...not found in Remedy");
				}

				rfc.initAttributeInfo(xml2, tableName);

				if (rfc.getAttributeCount() > 0) {
					// System.out.println("..updating elective attributes");
					rfc.attributesToDatabase(connection, tableName, tableKey,
							rfc_id);
				}

				String xml3 = remedyComments.GetCommentInfo(formattedRfcNo);

				rfc.initCommentInfo(xml3);

				rfc.commentsToDatabase(connection, tableName, tableKey, rfc_id);

			}

			// System.out.println("Done processing open RFCs.");

		} catch (services.ServicesException e) {

		} catch (SQLException sql) {

		}

		return;
	}

	public static void main(String[] args) {

		System.out.println("RemedyChangeRequest Vers 2.0 : Starting at "
				+ new Date().toString());

		RemedyRFCDownload down = new RemedyRFCDownload(args);

		down.run(args);

		System.out.println("RemedyChangeRequest: " + new Date().toString());

	}

}
