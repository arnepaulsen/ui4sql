/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;

import java.util.Date;
import java.util.Properties;
import java.lang.StringBuffer;

import java.io.IOException;
import java.io.FileInputStream;

/*
 * Submit an Incident / HelpDesk problem to Remedy
 * 
 * 		Get back the Remedy Case ID if all goes well, or Remedy error text
 * 
 * Receives : incident parameters#
 * 
 * Returns: : the case id 
 * 
 */

// remedy user id  1B11Test 
public class RemedyHelpDeskSubmit {

	private Properties connection_properties;

	private String remedyUserid;

	private String remedyPassword;

	private boolean success = false;

	private String remedyURL;

	private static String webService = "ESS_HelpDesk_Submit";

	private static String soap_action = "urn:ESS_HelpDesk_Submit/SubmitIncident";

	private static String soap_xml_ns = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:s=\"urn:ESS_HelpDesk_Submit\">";

	// soap header
	private static String soap_header_1 = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>";

	private static String soap_header_2 = "</s:userName><s:password>";

	private static String soap_header_3 = "</s:password>"
			+ "<s:authentication/><s:locale/>" + "<s:timeZone/>"
			+ "</s:AuthenticationInfo></soap:Header>";

	// soap body... for practice. Normally pass in properties file

	private static String test_soap_body = "<soap:Body><s:SubmitIncident>"
			+ "<s:keyword>MAINHELPDESK</s:keyword>"
			+ "<s:category>Application Services</s:category>"
			+ "<s:type>KPHC Epic Environment</s:type>"
			+ "<s:item>Performance Management</s:item>"
			+ "<s:summary>The summary info.</s:summary>"
			+ "<s:description>Test new problem from Web service</s:description>"
			+ "<s:caseType>Incident</s:caseType>"
			+ "<s:class>Failure</s:class>"
			+ "<s:systemEnvironment>Production</s:systemEnvironment>"
			+ "<s:sourceCode>Automated</s:sourceCode>"
			+ "<s:group>KPHC ECP</s:group>"
			+ "<s:requestUrgency>Low</s:requestUrgency>"
			+ "<s:impact>Medium</s:impact>"
			+ "<s:priority>Medium</s:priority>"
			+ "<s:requesterLoginName>wspmoc</s:requesterLoginName>"
			+ "<s:requesterName>Web Services PMO Connect</s:requesterName>"
			+ "<s:phoneNumber>510-601-3039</s:phoneNumber>"
			+ "<s:floorNumber>11</s:floorNumber>"
			+ "<s:businessUnit>Administration</s:businessUnit>"
			+ "<s:requesterDepartment>Information Technology</s:requesterDepartment>"
			+ "<s:region>CN</s:region>"
			+ "<s:serviceArea>Regional Offices North - Emeryville</s:serviceArea>"
			+ "<s:facBldg>EMV - Health Connect - Emeryville</s:facBldg>"
			+ "<s:submitterGroup>KPHC ECP</s:submitterGroup>"
			+ "<s:serialNumber>HC999991</s:serialNumber>"
			+ "</s:SubmitIncident></soap:Body>";

	private static String soap_envelope_end = "</soap:Envelope>";

	/*
	 * Constructor #1 with Remedy user-id and password
	 */
	public RemedyHelpDeskSubmit(String uid, String pw, String url) {

		this.remedyUserid = uid;
		this.remedyPassword = pw;
		this.remedyURL = url;
	}

	/*
	 * Constructor #2 with String properties file name
	 */
	public RemedyHelpDeskSubmit(String propertiesFileName) {

		System.out.println("Properties filename: " + propertiesFileName);
		try {
			// FileInputStream is = new FileInputStream(propFileName);
			System.out
					.print("RemedyProblemSubmit: Reading connection properties...");

			FileInputStream is = new FileInputStream(propertiesFileName);

			connection_properties = new Properties();
			connection_properties.load(is);

			remedyUserid = connection_properties.getProperty("USERID");
			remedyPassword = connection_properties.getProperty("PASSWORD");
			remedyURL = connection_properties.getProperty("URL") + "?server="
					+ connection_properties.getProperty("SERVER");

			is.close();

		} catch (IOException e) {
			System.out.println("error loading properties file.");
			System.out.println(e.toString());
			System.exit(1);
		}
	}

	public boolean getSuccess() {
		return success;
	}

	/*
	 * submitProblem to Remedy using properties file.
	 * 
	 */

	public String submitTestMessage() {
		return sendRequest(test_soap_body);

	}

	public String submitProblem(String propsFileName) {

		String soap_body = formatSoapBody(propsFileName);

		System.out.println("Soap body : " + soap_body);

		return sendRequest(soap_body);

	}

	private String formatSoapBody(String propFileName) {
		StringBuffer sb = new StringBuffer();

		Properties props = new Properties();

		System.out.println("Remedy incident properteis filename: "
				+ propFileName);
		try {
			System.out.print("... reading incident properties.");

			FileInputStream is = new FileInputStream(propFileName);
			props = new Properties();
			props.load(is);
			is.close();

		} catch (IOException e) {
			System.out.println("...error loading properties file.");
			System.out.println(e.toString());
			System.exit(99);
		}

		sb.append("<soap:Body><s:SubmitIncident>");
		sb
				.append("<s:keyword>" + props.getProperty("KEYWORD")
						+ "</s:keyword>");
		sb.append("<s:category>" + props.getProperty("CATEGORY")
				+ "</s:category>");
		sb.append("<s:type>" + props.getProperty("TYPE") + "</s:type>");
		sb.append("<s:item>" + props.getProperty("ITEM") + "</s:item>");
		sb
				.append("<s:summary>" + props.getProperty("SUMMARY")
						+ "</s:summary>");
		sb.append("<s:description>" + props.getProperty("DESCRIPTION")
				+ "</s:description>");
		sb.append("<s:caseType>" + props.getProperty("CASE_TYPE")
				+ "</s:caseType>");
		sb.append("<s:class>" + props.getProperty("CLASS") + "</s:class>");
		sb.append("<s:systemEnvironment>" + props.getProperty("ENVIRONMENT")
				+ "</s:systemEnvironment>");
		sb.append("<s:sourceCode>" + props.getProperty("SOURCE_CODE")
				+ "</s:sourceCode>");
		sb.append("<s:group>" + props.getProperty("GROUP") + "</s:group>");
		sb.append("<s:requestUrgency>" + props.getProperty("URGENCY")
				+ "</s:requestUrgency>");
		sb.append("<s:impact>" + props.getProperty("IMPACT") + "</s:impact>");
		sb.append("<s:priority>" + props.getProperty("PRIORITY")
				+ "</s:priority>");
		sb.append("<s:requesterLoginName>"
				+ props.getProperty("REQUESTER_LOGIN")
				+ "</s:requesterLoginName>");
		sb.append("<s:requesterName>" + props.getProperty("REQUESTER_NAME")
				+ "</s:requesterName>");
		sb.append("<s:phoneNumber>" + props.getProperty("PHONE")
				+ "</s:phoneNumber>");
		sb.append("<s:floorNumber>" + props.getProperty("FLOOR")
				+ "</s:floorNumber>");
		sb.append("<s:businessUnit>" + props.getProperty("BUSINESS_UNIT")
				+ "</s:businessUnit>");
		sb.append("<s:requesterDepartment>"
				+ props.getProperty("REQUESTER_DEPT")
				+ "</s:requesterDepartment>");
		sb.append("<s:region>" + props.getProperty("REGION") + "</s:region>");
		sb.append("<s:serviceArea>" + props.getProperty("SERVICE_AREA")
				+ "</s:serviceArea>");
		sb.append("<s:facBldg>" + props.getProperty("FAC_BLDG")
				+ "</s:facBldg>");
		sb.append("<s:submitterGroup>" + props.getProperty("SUBMITTER_GROUP")
				+ "</s:submitterGroup>");
		sb.append("<s:serialNumber>" + props.getProperty("SERIAL_NUMBER")
				+ "</s:serialNumber>");
		sb.append("</s:SubmitIncident></soap:Body>");

		return sb.toString();

	}

	public String sendRequest(String soap_body) {

		success = false;
		String answer = "";
		String server_url = remedyURL + "&webService=" + webService;

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soap_body
				+ soap_envelope_end;

		HttpSoapConnection soap = new HttpSoapConnection(server_url,
				soap_action);

		try {
			answer = soap.soapSendReceive(soap_req_str);
			success = soap.getSuccess();
		} catch (Exception e) {
			System.out.println("Soap Exception : " + e.toString());
		}

		if (answer == null) {
			System.out.println("Remedy returned no answer");
			answer = "";
		}

		// System.out.println("remedy returned : " + answer);
		return answer;

	}

	public static void main(String[] args) {

		System.out.println("Remedy HelpDesk_Submit - Version 1.0 Starting @ "
				+ new Date().toString());

		String loginPropFile = "";
		String submitPropFile = "";

		if (args.length < 2) {
			System.out
					.println("... usage is RemedyHelpDeskSubmit login_properties, submit_properties");
			System.exit(99);
		}

		loginPropFile = new String(args[0]);
		submitPropFile = new String(args[1]);

		RemedyHelpDeskSubmit problem = new RemedyHelpDeskSubmit(loginPropFile);

		String xml_response = problem.submitProblem(submitPropFile);

		if (problem.getSuccess() == true) {

			RemedyXMLParser parser = new RemedyXMLParser(xml_response,
					"SubmitIncidentResponse");
			String case_id = parser.parseTextValue("Case_ID");
			System.out.println("New case created # " + case_id);
		} else {
			System.out.println("\nBad Reqeust : ==> \n\n" + xml_response);
		}

		System.out.println("\nRemedy HelpDesk_Submit ending @ "
				+ new Date().toString());
	}
}
