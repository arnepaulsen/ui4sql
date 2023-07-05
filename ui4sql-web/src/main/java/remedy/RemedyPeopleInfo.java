/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;

import java.util.Date;
import java.sql.*;

import db.DbInterface;

import batch.BatchSQL;

/*
 * Get Remedy User Info via SOAP call to ESS_People_Info / SearchByNUID
 * 
 * Receives : NUID
 * 
 * Returns: : XML of User
 * 
 * 
 * 
 */

// remedy user id  1B11Test 
public class RemedyPeopleInfo {

	private boolean debug = false;

	private boolean success = false;

	private String remedy_url = null;
	
	private static String webService = "ESS_People_Info";

	private static String soap_action_nuid = "urn:ESS_People_Info/SearchByNUID";

	private static String soap_action_full_name = "urn:ESS_People_Info/SearchByFullName";
	
	private static String soap_action_last_name = "urn:ESS_People_Info/SearchByLastName";
	
	private static String soap_xml_ns = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:s=\"urn:ESS_People_Info\">";

	// soap header
	private static String soap_header_1 = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>";

	private static String soap_header_2 = "</s:userName><s:password>";

	private static String soap_header_3 = "</s:password>"
			+ "<s:authentication/><s:locale/>" + "<s:timeZone/>"
			+ "</s:AuthenticationInfo></soap:Header>";

	// soap body for nuid
	private static String soap_body_nuid_beg = "<soap:Body><s:SearchByNUID><s:nuid>";

	private static String soap_body_nuid_end = "</s:nuid></s:SearchByNUID></soap:Body>";

	// soap body for full name
	private static String soap_body_name_beg = "<soap:Body><s:SearchByFullName><s:lastName>";

	private static String soap_body_name_middle = "</s:lastName><s:firstName>";

	private static String soap_body_name_end = "</s:firstName></s:SearchByFullName></soap:Body>";

	private static String soap_envelope_end = "</soap:Envelope>";

	
	private static String soap_body_last_name_beg = "<soap:Body><s:SearchByLastName><s:lastName>";

	private static String soap_body_last_name_end = "</s:lastName></s:SearchByLastName></soap:Body>";

	
	private String remedyUserid;

	private String remedyPassword;

	// HTTP REQUEST :

	public RemedyPeopleInfo(String remedyUserid, String remedyPassword, String url) {
		this.remedyUserid = remedyUserid;
		this.remedyPassword = remedyPassword;
		this.remedy_url = url;
	}
	
	public String SearchByNUID(String nuid) {


		HttpSoapConnection soap = new HttpSoapConnection(remedy_url + "&webService=" + webService,
				soap_action_nuid);
		
		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soap_body_nuid_beg
				+ nuid + soap_body_nuid_end + soap_envelope_end;

	
		String answer = soap.soapSendReceive(soap_req_str);

		success = soap.getSuccess();
	
		System.out.println("SearchByNUID xml : " + answer);

		return answer;

	}

	public boolean getSuccess() {
		return success;
	}

	

	public String SearchByLastName(String last_name) {

		HttpSoapConnection soap = new HttpSoapConnection(remedy_url + "&webService=" + webService,
				soap_action_full_name);

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soap_body_last_name_beg
				+ last_name + soap_body_last_name_end + soap_envelope_end;

		String answer = soap.soapSendReceive(soap_req_str);

		debug("response : " + answer);

		success = soap.getSuccess();

		return answer;

	}


	
	
	
	public String SearchByFullName(String first_name, String last_name) {

		HttpSoapConnection soap = new HttpSoapConnection(remedy_url + "&webService=" + webService,
				soap_action_full_name);

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soap_body_name_beg
				+ last_name + soap_body_name_middle + first_name
				+ soap_body_name_end + soap_envelope_end;

		String answer = soap.soapSendReceive(soap_req_str);

		debug("response : " + answer);

		success = soap.getSuccess();

		return answer;

	}

	public String getErrorText(String remedy_answer) {

		try {
			RemedyXMLParser parser = new RemedyXMLParser(remedy_answer,
					"soapenv:Fault");

			return parser.parseTextValue("faultstring");
		} catch (Exception e) {
			return "Unknown remedy exception";
		}

	}

	public void updateUser(DbInterface conn, Integer user_id,
			String first_name, String mi, String last_name, String phone,
			String tieline, String email, String floor) {

		String sql = "Update tuser set first_name = '" + first_name
				+ "', mi_nm = '" + mi + "', last_name = '" + last_name
				+ "', floor_tx ='" + floor + "', phone_tx = '" + phone
				+ "', tie_line_tx ='" + tieline + "', email_address = '"
				+ email + "' where user_id= " + user_id.toString();

		debug(" Updating tuser: " + sql);

		try {
			conn.runQuery(sql);
		} catch (services.ServicesException e) {

		}

	}

	public void updateContact(BatchSQL conn, Integer contact_id, String nuid) {

		String sql = "Update tcontact set nuid_nm = '" + nuid
				+ "' where contact_id = " + contact_id.toString();

		debug(" Updating : " + sql);
		conn.runQuery(sql);

	}

	public void updateContacts() {

		// HttpSoapConnection soap = getSearchByFullNameConnection();

		debug("list starting");

		String db_url = "jdbc:microsoft:sqlserver://172.21.226.44:1433";
		String db_userid = "apaulsen";
		String db_password = "password";

		String first_name;
		String last_name;
		Integer contact_id = new Integer(0);

		int max = 100;
		int count = 0;

		BatchSQL conn = new BatchSQL(db_url, db_userid, db_password);

		try {
			ResultSet rs = conn
					.getRS("select contact_id, first_name, last_name, nuid_nm from tcontact where isnull(nuid_nm,'') = ''");

			while (rs.next() && count < max) {

				count++;
				first_name = rs.getString("first_name");
				last_name = rs.getString("last_name");


				contact_id = rs.getInt("contact_id");  // java 1.5 

				//contact_id = new Integer(rs.getInt("contact_id"));  // java 1.4 

				String xml = SearchByFullName(first_name, last_name);

				debug("Remedy response : " + xml);

				RemedyXMLParser parser = new RemedyXMLParser(xml,
						"getListValues");
				debug(last_name + "  has  " + parser.getCount());

				String remedy_nuid = parser.parseTextValue("loginName");

				if (parser.getCount() == 1) {
					updateContact(conn, contact_id, remedy_nuid);
				}

			}

			// soap.disconnect();

		} catch (services.ServicesException e) {
			debug(" exc 1 " + e.toString());

		} catch (SQLException sqle) {
			debug(" exc2 " + sqle.toString());
		}
	}

	private void debug(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	public static void main(String[] args) {

		System.out.println("Remedy SOAP Client - SearchByNUID Version 1.1 : "
				+ new Date().toString());

		RemedyConnection remedy = new RemedyConnection("PROD");
		
		System.out.println(" remedy user id :" + remedy.getDefaultUserid());
		
		
		RemedyPeopleInfo req = new RemedyPeopleInfo(remedy.getDefaultUserid(), remedy.getDefaultPassword(), remedy.GetRemedyURL());

		String xml = "";

		//req.updateContacts();

		//System.exit(0);
		
		// xml = req.SearchByNUID("U922638");
		//xml = req.SearchByNUID("D576781");
		//xml = req.SearchByFullName("Arne", "Paulsen");
		

		//System.exit(0);

		// System.out.println("People Info :" + xml); // xml response:
		// SearchByNUIDResponse

		//xml = req.SearchByFullName("Arne", "Paulsen");
		
		xml = req.SearchByLastName("Motta");
		
		System.out.println("xml: " + xml);
			

		if (req.getSuccess() == true) {

			RemedyXMLParser parser = new RemedyXMLParser(xml, "getListValues");

			int x = parser.getCount();

			System.out.println("Count : " + x);

			System.out.println("Full Name: "
					+ parser.parseTextValue("fullName"));
			System.out.println("Last Name: "
					+ parser.parseTextValue("lastName"));
			System.out
					.println(" NUID  : " + parser.parseTextValue("loginName"));

			if (x > 1) {
				parser.setElementNo(1); // zero
				System.out.println("Full Name: "
						+ parser.parseTextValue("fullName"));
				System.out.println("Last Name: "
						+ parser.parseTextValue("lastName"));
				System.out.println(" NUID  : "
						+ parser.parseTextValue("loginName"));
			}

			if (x > 2) {
				parser.setElementNo(2); // zero
				System.out.println("Full Name: "
						+ parser.parseTextValue("fullName"));
				System.out.println("Last Name: "
						+ parser.parseTextValue("lastName"));
				System.out.println(" NUID  : "
						+ parser.parseTextValue("loginName"));
			}

		} else {
			System.out.println("bad response");
			System.out.println("\n\n" + xml);
		}

		System.out.println("\nData received ==> \n\n" + xml);

		System.out.println("Remedy SOAP Client - done: "
				+ new Date().toString());

	}

}
