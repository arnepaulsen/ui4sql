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
 * Help_Desk_Modify - Cancel or Resolve by Mofify 
 *  
 * Receives : incident parameters#
 * 
 * 
 * 
 */

// remedy user id  1B11Test 
public class RemedyHelpDeskModify {

	private Properties connection_properties;

	private String remedyUserid;

	private String remedyPassword;

	private boolean success = false;

	private String remedyURL = null;

	private static String webService = "ESS_HelpDesk_Modify";

	private static String soap_action_cancel = "urn:ESS_HelpDesk_Modify/CancelIncident";

	private static String soap_action_resolve = "urn:ESS_HelpDesk_Modify/ResolveIncident";

	//private static String soap_action_modify = "urn:ESS_HelpDesk_Modify/UpdateWorklog";

	private static String soap_xml_ns = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:s=\"urn:ESS_HelpDesk_Modify\">";

	// soap header
	private static String soap_header_1 = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>";

	private static String soap_header_2 = "</s:userName><s:password>";

	private static String soap_header_3 = "</s:password>"
			+ "<s:authentication/><s:locale/>" + "<s:timeZone/>"
			+ "</s:AuthenticationInfo></soap:Header>";

	private static String soap_envelope_end = "</soap:Envelope>";

	/*
	 * Constructor #1 with Remedy user-id and password
	 */
	public RemedyHelpDeskModify(String uid, String pw, String url) {

		this.remedyUserid = uid;
		this.remedyPassword = pw;
		this.remedyURL = url;
	}

	/*
	 * Constructor #2 with String properties file name
	 */
	public RemedyHelpDeskModify(String propertiesFileName) {

		System.out.println("Properties filename: " + propertiesFileName);
		try {
			// FileInputStream is = new FileInputStream(propFileName);
			System.out
					.print("Remedy HelpDesk Modify: Reading connection properties...");

			FileInputStream is = new FileInputStream(propertiesFileName);

			connection_properties = new Properties();
			connection_properties.load(is);

			remedyUserid = connection_properties.getProperty("USERID");
			remedyPassword = connection_properties.getProperty("PASSWORD");
			remedyURL = connection_properties.getProperty("URL") + "?server=" + connection_properties.getProperty("SERVER");

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

	public String cancelIncident(String propsFileName) {

		String soap_body = formatSoapCancelBody(propsFileName);

		System.out.println("Soap body : " + soap_body);

		return sendRequest("Cancel", soap_body);

	}

	public String resolveIncident(String propsFileName) {

		String soap_body = formatSoapResolveBody(propsFileName);

		System.out.println("Soap body : " + soap_body);

		return sendRequest("Resolve", soap_body);

	}
	
	
	public String updateWorklog(String caseId, String worklog) {

		String soap_body = formatSoapWorklog(caseId, worklog);

		//System.out.println("Soap body : " + soap_body);

		return sendRequest("UpdateWorklog", soap_body);

	}

	
	
	private String formatSoapWorklog (String caseId, String worklog) {
		
		StringBuffer sb = new StringBuffer();

		sb.append("<soap:Body><s:UpdateWorklog>");
		sb.append("<s:workLog>" + worklog + "</s:workLog>");
		sb.append("<s:caseID>" + caseId + "</s:caseID>");
		sb.append("</s:UpdateWorklog></soap:Body>");
		
		return sb.toString();
	}

	private String formatSoapCancelBody(String propFileName) {

		StringBuffer sb = new StringBuffer();

		Properties props = getProperties(propFileName);

		sb.append("<soap:Body><s:CancelIncident>");
		sb.append("<s:caseID>" + props.getProperty("CASE_ID") + "</s:caseID>");
		sb.append("<s:status>" + props.getProperty("STATUS") + "</s:status>");
		sb
				.append("<s:workLog>" + props.getProperty("WORKLOG")
						+ "</s:workLog>");

		sb.append("<s:resolutionCode>" + props.getProperty("RESOLUTION_CODE")
				+ "</s:resolutionCode>");

		// sb.append("<s:attributeName>cancelReason</s:attributeName>");
		// sb.append("<s:attributeValue>" + props.getProperty("CANCEL_REASON")
		// + "</s:attributeValue>");

		sb.append("<s:attributeName>Cause Code 1</s:attributeName>");
		sb.append("<s:attributeValue>" + props.getProperty("CAUSE_CODE_1")
				+ "</s:attributeValue>");

		sb.append("<s:webServiceAction>"
				+ props.getProperty("WEB_SERVICE_ACTION")
				+ "</s:webServiceAction>");
		sb.append("</s:CancelIncident></soap:Body>");

		return sb.toString();

	}

	private String formatSoapResolveBody(String propFileName) {

		StringBuffer sb = new StringBuffer();

		Properties props = getProperties(propFileName);

		sb.append("<soap:Body><s:ResolveIncident>");

		sb.append("<s:status>" + props.getProperty("STATUS") + "</s:status>");
		sb
				.append("<s:workLog>" + props.getProperty("WORKLOG")
						+ "</s:workLog>");

		sb.append("<s:resolutionCode>" + props.getProperty("RESOLUTION_CODE")
				+ "</s:resolutionCode>");

		sb.append("<s:causeCode1>" + props.getProperty("CAUSE_CODE_1")
				+ "</s:causeCode1>");

		sb.append("<s:causeCode2>" + props.getProperty("CAUSE_CODE_2")
				+ "</s:causeCode2>");

		sb.append("<s:causeDescription>"
				+ props.getProperty("CAUSE_DESCRIPTION")
				+ "</s:causeDescription>");

		sb.append("<s:resolutionAction>"
				+ props.getProperty("RESOLUTION_ACTION")
				+ "</s:resolutionAction>");

		sb.append("<s:resolutionDescription>"
				+ props.getProperty("RESOLUTION_DESCRIPTION")
				+ "</s:resolutionDescription>");

		sb.append("<s:solutionSummary>" + props.getProperty("SOLUTION_SUMMARY")
				+ "</s:solutionSummary>");

		sb.append("<s:solutionDescription>"
				+ props.getProperty("SOLUTION_DESCRIPTION")
				+ "</s:solutionDescription>");

		sb.append("<s:caseID>" + props.getProperty("CASE_ID") + "</s:caseID>");

		sb.append("</s:ResolveIncident></soap:Body>");

		return sb.toString();

	}

	private Properties getProperties(String prop_file_name) {
		Properties props = new Properties();

		System.out.println("Remedy incident properteis filename: "
				+ prop_file_name);
		try {
			// FileInputStream is = new FileInputStream(propFileName);
			System.out
					.print("Remedy HelpDesk Modify: Reading incident properties...");

			FileInputStream is = new FileInputStream(prop_file_name);

			props = new Properties();
			props.load(is);
			is.close();

		} catch (IOException e) {
			System.out.println("error loading properties file.");
			System.out.println(e.toString());
			System.exit(1);
		}

		return props;
	}

	public String sendRequest(String modify_function, String soap_body) {

		HttpSoapConnection soap = null;

		String answer = "";
		String server_url = remedyURL + "&webService="
				+ webService;

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soap_body
				+ soap_envelope_end;

		System.out.println("HTTPSoapConnection...formatted XML to send: "
				+ soap_req_str);

		System.out.println("HTTPSoapConnection....connecting to Remedy.");

		if (modify_function.equalsIgnoreCase("resolve")) {
			soap = new HttpSoapConnection(server_url, soap_action_resolve);
		} else {
			soap = new HttpSoapConnection(server_url, soap_action_cancel);
		}

		System.out.println("HTTPSoapConnection...connected, sending request.");

		answer = soap.soapSendReceive(soap_req_str);
		success = soap.getSuccess();
		return answer;

	}

	public static void main(String[] args) {

		System.out.println("Remedy HelpDesk_Modify - Version 1.0 Starting @ "
				+ new Date().toString());
		String xml_response;
		String remedy_response_element_name = "";

		if (args.length < 3) {

			System.out
					.println("Usage : RemedyHelpDeskModify modify_function, login_prop_file, function_properties_file"
							+ "\n    \"function\"  should be \"cancel\" or \"resolve\".");
			System.exit(99);
		}

		System.out.println("... requested function is : " + args[0]);

		RemedyHelpDeskModify incident = new RemedyHelpDeskModify(args[1]);

		if (args[0].equalsIgnoreCase("cancel")) {
			xml_response = incident.cancelIncident(args[2]);
			remedy_response_element_name = "CancelIncidentResponse";
		} else {
			xml_response = incident.resolveIncident(args[2]);
			remedy_response_element_name = "ResolveIncidentResponse";

		}

		if (incident.getSuccess() == true) {

			RemedyXMLParser parser = new RemedyXMLParser(xml_response,
					remedy_response_element_name);
			String new_status = parser.parseTextValue("status");
			String case_id = parser.parseTextValue("caseID");
			System.out.println("Case # " + case_id + " new status : "
					+ new_status);
		} else {
			System.out.println("\n\n" + xml_response);
		}

		System.out.println("\nRemedy HelpDesk_Modify ending @ "
				+ new Date().toString());

	}

}
