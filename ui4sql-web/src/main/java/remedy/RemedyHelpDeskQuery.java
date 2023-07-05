/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;

import java.util.Date;

/*
 * Get Remedy RFC/SR Info via SOAP call to ESS_HelpDesk_Query / SearchByCaseID
 * 
 * Receives : RFC or SR #
 * 
 * Returns: : XML of RFC / SR
 * 
 * 
 * 
 */

// remedy user id  1B11Test
public class RemedyHelpDeskQuery {

	private boolean isConnected = false;

	private String remedyURL = null;
	
	private boolean success = false;
	
	private String answer = "";

	private static String webService = "ESS_HelpDesk_Query";

	private static String soap_action = "urn:ESS_HelpDesk_Query/SearchByCaseID";

	private static String soap_xml_ns = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:s=\"urn:ESS_HelpDesk_Query\">";

	// soap header
	private static String soap_header_1 = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>";

	private static String soap_header_2 = "</s:userName><s:password>";

	private static String soap_header_3 = "</s:password>"
			+ "<s:authentication/><s:locale/>" + "<s:timeZone/>"
			+ "</s:AuthenticationInfo></soap:Header>";

	// soap body
	private static String soap_body_beg = "<soap:Body><s:SearchByCaseID><s:caseID>";

	private static String soap_body_end = "</s:caseID></s:SearchByCaseID></soap:Body>";

	private static String soap_envelope_end = "</soap:Envelope>";

	private String remedyUserid;

	private String remedyPassword;

	// HTTP REQUEST :

	public RemedyHelpDeskQuery(String remedyUserid, String remedyPassword, String url) {
		this.remedyUserid = remedyUserid;
		this.remedyPassword = remedyPassword;
		this.remedyURL = url;
		
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean getSuccess() {
		return success;
	}
	
	public String getCaseInfo(String caseID) {

	
		String server_url = remedyURL + "&webService=" + webService;

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soap_body_beg
				+ caseID + soap_body_end + soap_envelope_end;

		// System.out.println("connecteing to remedy web");
		HttpSoapConnection soap = new HttpSoapConnection(server_url,
				soap_action);

		// System.out.println("sending PR request to remedy ");

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

	public String getErrorText() {

		try {
			RemedyXMLParser parser = new RemedyXMLParser(answer,
					"soapenv:Fault");

			return parser.parseTextValue("faultstring");
		} catch (Exception e) {
			return "Unknown remedy exception";
		}

	}
	
	public static void main(String[] args) {

		System.out
				.println("Remedy SOAP Interface - SearchByChangeID Version 0.1 : "
						+ new Date().toString());

		RemedyConnection remedy = new RemedyConnection("PROD");
		
		RemedyHelpDeskQuery problem = new RemedyHelpDeskQuery(remedy.getDefaultUserid(),
				remedy.getDefaultPassword(), remedy.GetRemedyURL());

		// 1297193

		String xml = problem.getCaseInfo("HD0000001349946"); // real qa rfc :
		// CHG000000616252

		System.out.println("\nData received ==> \n\n" + xml);

		System.out.println("\nRemedy Web Client complete "
				+ new Date().toString());

	}

}
