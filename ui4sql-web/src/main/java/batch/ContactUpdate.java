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
import remedy.RemedyPeopleInfo;
import remedy.RemedyXMLParser;

import services.ServicesException;
import java.sql.*;
import java.util.Date;
import java.util.Properties;

/*******************************************************************************
 * 
 * @author PAULSEAR
 * 
 * Update department and facility fields on tContact table
 * 
 * get ResultSet of all tContact persons loop though each tContct - call Remedy
 * WebService to retrieve list of persons matching last name - if one or more
 * match last name loop through remedy list and match back to tContact first
 * name if match found, then update dept and facility
 * 
 */
public class ContactUpdate {

	/**
	 * @param args
	 * 
	 * Main - Loop through all trfc open records, and post remedy XML into to
	 * the database.
	 * 
	 */
	public ContactUpdate(String[] args) {
		super();

	}

	String remedyURL = "";
	String remedyUserid = "";
	String remedyPassword = "";
	int counter = 0;
	int changeCount = 0;

	BatchSQL batchSQL;
	Connection connection;
	Properties properties;

	public int run(String[] args) {

		debug("UpdateContact Department/Facility info");

		String file = (String) args[0];

		loadProperties(file);

		RemedyPeopleInfo person = new remedy.RemedyPeopleInfo(remedyUserid,
				remedyPassword, remedyURL);

		batchSQL = new BatchSQL(args[0]);

		connection = batchSQL.getConnection();

		String query = "SET ROWCOUNT 900 SELECT last_name, first_name from tcontact where len(last_name) > 1 and isnull(nuid_nm,'arne') = 'arne' order by last_name";

		String last_name;
		String first_name;

		try {
			ResultSet rs = batchSQL.getRS(query);

			while (rs.next() && counter < 900) {
				debug("next contact");

				counter++;

				last_name = rs.getString("last_name");
				first_name = rs.getString("first_name");

				debug("looking up : " + last_name + "," + first_name);

				String xml = person.SearchByLastName(last_name);

				// debug("xml: " + xml);

				matchIt(xml, last_name, first_name);
			}

			// System.out.println("Done processing open RFCs.");

		} catch (services.ServicesException e) {

		} catch (SQLException sql) {

		}

		return counter;
	}

	private boolean loadProperties(String file) {

		/*
		 * get Remedy connection properties
		 */
		try {
			// FileInputStream is = new FileInputStream(propFileName);
			// System.out
			// .print("PMO Tool - Update RFCs from Remedy. Starting.");

			FileInputStream is = new FileInputStream(file);

			properties = new Properties();
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

		return true;
	}

	/*
	 * The SearchByLastName will return 0..n occurence. ... so have to loop
	 * through each one and match on the first name for a hit
	 * 
	 * 
	 */
	private void matchIt(String xml, String last_name, String first_name) {

		if (xml.length() < 1) {
			return;
		}

		RemedyXMLParser parser = new RemedyXMLParser(xml, "getListValues");

		int count = parser.getCount();
		debug(" .. found matches : " + count);

		boolean stillLooking = true;

		for (int x = 0; x <= count && stillLooking; x++) {
			parser.setElementNo(x);

			String remedy_fullName = parser.parseTextValue("fullName");

			if (remedy_fullName.contains(first_name)) {
				debug("found first name match : " + remedy_fullName + " on "
						+ first_name);
				String dept = parser.parseTextValue("requesterDepartment");
				String facility = parser.parseTextValue("facBldg");
				String phone = parser.parseTextValue("phone");
				String nuid = parser.parseTextValue("loginName");
				String email = parser.parseTextValue("email");
				updateDatabase(last_name, first_name, facility, dept, nuid,
						phone, email);
				stillLooking = false;
			}
		}
	}

	private void updateDatabase(String last_name, String first_name,
			String facility, String dept, String nuid, String phone,
			String email) {

		String sql = " SET ROWCOUNT 5 UPDATE tContact set facility_nm = '"
				+ facility + "', dept_nm = '" + dept + "', phone_nm = '"
				+ phone + "', email_address= '" + email + "', nuid_nm = '"
				+ nuid + "' WHERE last_name = '" + last_name
				+ "' AND first_name = '" + first_name + "'";

		debug("SQL: " + sql);

		try {
			batchSQL.runQuery(sql);
		} catch (Exception e) {

			debug("Error " + e.toString());
		}
	}

	private void debug(String s) {

		System.out.println(s);
	}

	public static void main(String[] args) {

		System.out.println("ContactUpdate Vers 1.0 : Starting at "
				+ new Date().toString());

		ContactUpdate contactUpdate = new ContactUpdate(args);

		int count = contactUpdate.run(args);

		System.out.println("ContactUpdate: Processed  " + count + " at:"
				+ new Date().toString());

	}

}
