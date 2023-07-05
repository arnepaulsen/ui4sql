/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;

import java.util.Date;

/*
 * SOAP API to Remedy/ESS to Modify an RFC or SR
 * 
 * Operation :
 * 
 * 	UpdateWorkLog
 * 
 * Other operations are not implemented:
 * 
 * 	CancelSRRFC
 * 	UpdateSRSTatus
 *  ResolveSR
 * 	
 * 
 * 
 */

public class RemedyChangeModify {

	private boolean isConnected = false;

	// QA Servers:

	private String remedyURL = null;

	private static String webService = "ESS_Change_Modify";

	private static String soap_action_UpdateWorkLog = "urn:ESS_Change_Modify/UpdateWorkLog";

	private static String soap_action_ResolveSR = "urn:ESS_Change_Modify/ResolveSR";

	private static String soap_xml_ns = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:s=\"urn:ESS_Change_Modify\">";

	// soap header
	private static String soap_header_1 = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>";

	private static String soap_header_2 = "</s:userName><s:password>";

	private static String soap_header_3 = "</s:password>"
			+ "<s:authentication/><s:locale/>" + "<s:timeZone/>"
			+ "</s:AuthenticationInfo></soap:Header>";

	private static String soap_envelope_end = "</soap:Envelope>";

	private String remedyUserid;

	private String remedyPassword;

	// HTTP REQUEST :

	public RemedyChangeModify(String remedyUserid, String remedyPassword,
			String url) {
		this.remedyUserid = remedyUserid;
		this.remedyPassword = remedyPassword;
		this.remedyURL = url;

	}

	public boolean isConnected() {
		return isConnected;
	}

	public String updateWorkLog(String changeID, String workLog) {

		return sendRemedyRequest(formatSoapBodyWorkLog(changeID, workLog),
				soap_action_UpdateWorkLog);

	}

	public String resolveSR(String changeID) {
		return sendRemedyRequest(formatSoapBodyResolveSR(changeID),
				soap_action_ResolveSR);

	}

	private String sendRemedyRequest(String soapRequest, String soapAction) {

		String answer = "";
		String server_url = remedyURL + "&webService=" + webService;

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soapRequest
				+ soap_envelope_end;

		HttpSoapConnection soap = new HttpSoapConnection(server_url,
				soap_action_UpdateWorkLog);

		try {
			answer = soap.soapSendReceive(soap_req_str);
		} catch (Exception e) {
			System.out.println("Soap Exception : " + e.toString());
		}
		return answer;
	}

	private String formatSoapBodyWorkLog(String changeId, String workLog) {
		StringBuffer sb = new StringBuffer();
		sb.append("<soap:Body><s:UpdateWorkLog>");
		sb.append("<s:changeID>" + changeId + "</s:changeID>");
		sb.append("<s:workLog>" + workLog + "</s:workLog>");
		sb.append("</s:UpdateWorkLog></soap:Body>");
		return sb.toString();
	}

	private String formatSoapBodyResolveSR(String changeId) {
		StringBuffer sb = new StringBuffer();

		sb.append("<soap:Body><s:ResolveSR>");
		sb.append("<s:changeID>" + changeId + "</s:changeID>");
		sb.append("<s:status>Resolved</s:status>");
		sb.append("<s:resolutionCode>Successful</s:resolutionCode>");
		sb
				.append("<s:actualStartDate>2007-08-02T11:15:12-07:00</s:actualStartDate>");
		sb
				.append("<s:actualEndDate>2007-08-02T12:15:12-07:00</s:actualEndDate>");
		sb.append("<s:workLog>Finished okay</s:workLog>");
		sb.append("</s:ResolveSR></soap:Body>");
		return sb.toString();
	}

	public static void main(String[] args) {

		System.out
				.println("Remedy SOAP Interface - SearchByChangeID Version 0.1 : "
						+ new Date().toString());

		RemedyConnection remedy = new RemedyConnection("PROD");

		RemedyChangeModify req = new RemedyChangeModify(remedy
				.getDefaultUserid(), remedy.getDefaultPassword(), remedy
				.GetRemedyURL());

		String xml = req.resolveSR("CHG000000616252");
		System.out.println("\nData received ==> \n\n" + xml);

		System.out.println("\nRemedy Web Client complete "
				+ new Date().toString());

	}

}
