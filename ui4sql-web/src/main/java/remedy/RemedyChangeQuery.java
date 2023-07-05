/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;

import java.util.Date;
import services.RemedyChangeRequest;

/*
 * Get Remedy RFC/SR Info via SOAP call to ESS_Change_Query / SearchByChangeID
 * 
 * Receives : RFC or SR #
 * 
 * Returns: : XML of RFC / SR
 * 
 * 
 * 
 */

// remedy user id  1B11Test 
public class RemedyChangeQuery {

	private boolean isConnected = false;

	private boolean success = false;

	private String remedyURL = null;

	private static String webService = "ESS_Change_Query";

	private static String soap_action = "urn:ESS_Change_Query/SearchByChangeID";

	private static String soap_xml_ns = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:s=\"urn:ESS_Change_Query\">";

	// soap header
	private static String soap_header_1 = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>";

	private static String soap_header_2 = "</s:userName><s:password>";

	private static String soap_header_3 = "</s:password>"
			+ "<s:authentication/><s:locale/>" + "<s:timeZone/>"
			+ "</s:AuthenticationInfo></soap:Header>";

	// soap body
	private static String soap_body_beg = "<soap:Body><s:SearchByChangeID><s:changeID>";

	private static String soap_body_end = "</s:changeID></s:SearchByChangeID></soap:Body>";

	private static String soap_envelope_end = "</soap:Envelope>";

	private String remedyUserid;

	private String remedyPassword;

	// HTTP REQUEST :

	public RemedyChangeQuery(String remedyUserid, String remedyPassword, String url) {
		this.remedyUserid = remedyUserid;
		this.remedyPassword = remedyPassword;
		this.remedyURL = url;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean getSuccess() {
		System.out.println("RemedyChangeQuery - get success " + success);

		return success;
	}

	public String GetChangeInfo(String changeID) {

		String answer = "";
		String server_url = remedyURL + "&webService="
				+ webService;

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soap_body_beg
				+ changeID + soap_body_end + soap_envelope_end;

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

		System.out
				.println("Remedy SOAP Interface - SearchByChangeID Version 0.1 : "
						+ new Date().toString());

		
		RemedyConnection remedy = new RemedyConnection("PROD");
				
		RemedyChangeQuery req = new RemedyChangeQuery(remedy.getDefaultUserid(), remedy.getDefaultPassword(), remedy.GetRemedyURL());

		String xml = req.GetChangeInfo("CHG000001892736"); // real qa rfc :
		// CHG000000616252

		System.out.println("\nData received ==> \n\n" + xml);

		// parse the xml and make elements via get functions
		RemedyChangeRequest rfco = new RemedyChangeRequest(xml);

		rfco.printRFC();

		System.out.println("\nRemedy Web Client complete "
				+ new Date().toString());

	}

}
