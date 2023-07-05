/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/*
 * Get Remedy RFC/SR Info via SOAP call to ESS_Change_Query / SearchByChangeID
 * 
 * Receives : RFC or SR #
 * 
 * Returns: : XML of RFC / SR
 * 
 * 
 * 
 * Change Log:
 * 
 * 	8/20/08 	Remedy Category is now "End User Applications"
 * 11/10/09 - add more Bean "setter" functions for plugin "Test" 
 * 
 * 
 * 
 */

// remedy user id  1B11Test /

public class RemedyChangeSubmit {

	/*
	 * Remedy Values
	 */
	private String remedyCategory = "End User Applications";
	String remedyType;
	String remedyItem;

	private String backout_tx = "Backout plan goes here.";
	private String changeDescription = "Description goes here.";
	private String expeditedIndicator = "No";
	private String floorNumber = "16";
	private String hotlistIndicator = "No";
	private String impact_cd = "Low";
	private String implementationPlan = "Implementation plan goes here.";
	private String outageIndicator = "No";
	private String tieline = "8-409-9999";
	private String planned_end_date = "2011-12-31T14:15:12-07:00";
	private String planned_start_date = "2011-01-01T14:15:12-07:00";
	private String priority_cd = "Low";
	private String regulatory_cd = "No";
	private String release_related_cd = "Not Related"; // or "Related"
	private String urgency_cd = "Low";
	private String requested_completion_date = "2011-12-31T14:15:12-07:00";
	private String summary_tx = "Patient Validator Tracker";
	private String test_tx = "Test plan goes here.";
	private String phone_no = "510-601-9999";
	private String title_prefix = "Mr.";
	private String worklog_tx = "Worklog goes here.";
	private String target_date = "2011-02-01T14:15:12-07:00";

	/*
	 * Setter Functions
	 */

	public void setRemedyCategory(String remedyCategory) {
		this.remedyCategory = remedyCategory;

	}

	public void setRemedyType(String remedyType) {
		this.remedyType = remedyType;
	}

	public void setRemedyItem(String item) {
		this.remedyItem = item;

	}

	public void setChangeDescription(String desc) {
		this.changeDescription = desc;
	}

	public void setChangeSummary(String chg) {
		this.summary_tx = chg;
	}

	public void setExpeditedIndicator(String yesNo) {
		this.expeditedIndicator = yesNo;
	}

	public void setFloorNumber(String floor) {
		this.floorNumber = floor;
	}

	public void setHotlistIndicator(String yesNo) {
		this.hotlistIndicator = yesNo;
	}

	public void setOutageIndicator(String yesNo) {
		this.outageIndicator = yesNo;
	}

	public void setImplementationPlan(String plan) {
		this.implementationPlan = plan;
	}

	public void setPriority(String priority) {
		this.priority_cd = priority;
	}

	public void setPhoneNumber(String phone) {
		this.phone_no = phone;
	}

	public void setStartDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		try {
			Date d = sdf.parse(date);
			setStartDate(d);
		} catch (Exception e) {
		}
		return;
	}

	public void setEndDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		try {
			Date d = sdf.parse(date);
			setEndDate(d);
		} catch (Exception e) {
		}
		return;
	}

	
	public void setStartDate(Date date) {
		// Remedy date has format : 2010-01-01T14:15:12-07:00
		// -07:00 = Pacific Daylight Time!

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String remedyDate = sdf.format(date);

		planned_start_date = remedyDate.toString() + "T01:01:01-07:00";

	}

	public void setEndDate(Date date) {
		// Remedy date has format : 2010-01-01T14:15:12-07:00
		// -07:00 = Pacific Daylight Time!

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String remedyDate = sdf.format(date);

		planned_end_date = remedyDate.toString() + "T01:01:01-07:00";

	}

	/*
	 * 
	 */
	private String remedy_answer;

	private String remedyURL = null;

	private static String webService = "ESS_Change_Submit";

	private static String soap_action = "urn:ESS_Change_Submit/SubmitSRRFC";

	private static String soap_xml_ns = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:s=\"urn:ESS_Change_Submit\">";

	// soap header
	private static String soap_header_1 = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>";

	private static String soap_header_2 = "</s:userName><s:password>";

	private static String soap_header_3 = "</s:password>"
			+ "<s:authentication/><s:locale/>" + "<s:timeZone/>"
			+ "</s:AuthenticationInfo></soap:Header>";

	private static String example_soap_body = "<soap:Body><s:SubmitSRRFC>"
			+ "<s:attachedToRel>No</s:attachedToRel>"
			+ "<s:backoutPlan>My backout plan.</s:backoutPlan>"
			+ "<s:businessUnit>Administration</s:businessUnit>"
			+ "<s:category>Application Services</s:category>"
			+ "<s:dataCenterImpacted>Corona</s:dataCenterImpacted>"
			+ "<s:description>the sr desription</s:description>"
			+ "<s:expedited>No</s:expedited>"
			+ "<s:facBldg>EMV - Health Connect - Emeryville</s:facBldg>"
			+ "<s:floorNumber>11</s:floorNumber>"
			+ "<s:fundingCategory>Core-MRO</s:fundingCategory>"
			+ "<s:hotlist>No</s:hotlist>"
			+ "<s:impact>Low</s:impact>"
			+ "<s:implementationPlan>My implementation plan</s:implementationPlan>"
			+ "<s:item>Database Service</s:item>"
			+ "<s:kPHCRelatedImpacted>Yes</s:kPHCRelatedImpacted>"
			+ "<s:outageRequired>No</s:outageRequired>"
			+ "<s:phoneNumber>501-601-3039</s:phoneNumber>"
			+ "<s:plannedEndDate>2007-09-04T14:15:12-07:00</s:plannedEndDate>"
			+ "<s:plannedStartDate>2007-09-01T14:15:12-07:00</s:plannedStartDate>"
			+ "<s:priority>Low</s:priority>"
			+ "<s:region>CN</s:region>"
			+ "<s:regulatory>No</s:regulatory>"
			+ "<s:releaseRelated>Not Related</s:releaseRelated>"
			+ "<s:requestType>SR</s:requestType>"
			+ "<s:requestUrgency>Low</s:requestUrgency>"
			+ "<s:requestedCompletionDate>2007-09-01T14:15:12-07:00</s:requestedCompletionDate>"
			+ "<s:requesterDepartment>Health Connect</s:requesterDepartment>"
			+ "<s:requesterLoginName>wspmoc</s:requesterLoginName>"
			+ "<s:requesterName>Web Services PMO Connect</s:requesterName>"
			+ "<s:scheduledGoLive>2007-09-01T14:15:12-07:00</s:scheduledGoLive>"
			+ "<s:serviceArea>Regional Offices North - Emeryville</s:serviceArea>"
			+ "<s:sourceCode>Automated</s:sourceCode>"
			+ "<s:submitterGroup>KPHC ECP</s:submitterGroup>"
			+ "<s:summary>My summary info.</s:summary>"
			+ "<s:systemEnvironment>Production</s:systemEnvironment>"
			+ "<s:testPlan>My summary info.</s:testPlan>"
			+ "<s:tieLine>8-409-3039</s:tieLine>"
			+ "<s:type>KPHC Bridges</s:type>"
			+ "<s:workLog>My work log.</s:workLog>"
			+ "<s:keyword>MAINCHANGE</s:keyword>"
			+ "</s:SubmitSRRFC></soap:Body>";

	// Optional fields :
	// + "<s:titlePrefix>Mr.</s:titlePrefix>"
	// + "<s:businessProjMgr>Arne S. Paulsen</s:businessProjMgr>"
	// + "<s:projectNumber>777</s:projectNumber>"
	// + "<s:office>Emeryville</s:office>"
	// + "<s:systemTestEnd>2007-09-01T14:15:12-07:00</s:systemTestEnd>"
	// + "<s:systemTestStart>2007-09-01T14:15:12-07:00</s:systemTestStart>"
	// + "<s:pcParentID>CHG000000678878</s:pcParentID>"
	// + "<s:goNoGo>2007-09-02T14:15:12-07:00</s:goNoGo>"
	// + "<s:prodShakeDown>2007-09-01T14:15:12-07:00</s:prodShakeDown>"
	// + "<s:employeeType>IT Staff</s:employeeType>"
	// + "<s:developmentEnd>2007-09-02T12:15:12-07:00</s:developmentEnd>"
	// + "<s:developmentStart>2007-09-02T14:15:12-07:00</s:developmentStart>"
	// + "<s:group>CTO DBS DBA SFA</s:group>"
	// + "<s:vendorNumber>2</s:vendorNumber>"
	// + "<s:pcField1>Create PC Record</s:pcField1>"
	// + "<s:uatEnd>2007-09-01T14:15:12-07:00</s:uatEnd>"
	// + "<s:uatStart>2007-09-01T14:15:12-07:00</s:uatStart>"

	// Fields for type RFC only:
	// +
	// "<s:releaseIdentification>2007-09-01T14:15:12-07:00</s:releaseIdentification>"
	// + "<s:releaseNumber>1</s:releaseNumber>"
	// + "<s:releaseReview>2007-09-01T14:15:12-07:00</s:releaseReview>"
	// + "<s:releaseType>Planned</s:releaseType>"

	private static String soap_envelope_end = "</soap:Envelope>";

	private String remedyUserid;

	private String remedyPassword;

	// HTTP REQUEST :

	public RemedyChangeSubmit(String remedyUserid, String remedyPassword,
			String url) {
		this.remedyUserid = remedyUserid;
		this.remedyPassword = remedyPassword;
		this.remedyURL = url;
	}

	public String getReply() {
		return remedy_answer;
	}

	public String formatSoapBody(String changeType, String requesterNUID,
			String requesterName) {

		StringBuffer sb = new StringBuffer();

		sb.append("<soap:Body><s:SubmitSRRFC>");

		sb.append("<s:attachedToRel>No</s:attachedToRel>");

		sb.append("<s:backoutPlan>" + backout_tx + "</s:backoutPlan>");

		sb.append("<s:businessUnit>Administration</s:businessUnit>");

		sb.append("<s:category>" + remedyCategory + "</s:category>");

		sb.append("<s:dataCenterImpacted>Corona</s:dataCenterImpacted>");

		sb.append("<s:description>" + changeDescription + "</s:description>");

		sb.append("<s:expedited>" + expeditedIndicator + "</s:expedited>");

		sb.append("<s:facBldg>EMV - Emeryville</s:facBldg>");

		sb.append("<s:floorNumber>" + floorNumber + "</s:floorNumber>");

		sb.append("<s:fundingCategory>Core-MRO</s:fundingCategory>");

		sb.append("<s:hotlist>" + hotlistIndicator + "</s:hotlist>");

		sb.append("<s:impact>" + impact_cd + "</s:impact>");

		sb.append("<s:implementationPlan>" + implementationPlan
				+ "</s:implementationPlan>");

		sb.append("<s:item>" + remedyItem + "</s:item>");

		sb.append("<s:kPHCRelatedImpacted>Yes</s:kPHCRelatedImpacted>");

		sb.append("<s:outageRequired>" + outageIndicator
				+ "</s:outageRequired>");

		sb.append("<s:phoneNumber>" + phone_no + "</s:phoneNumber>");

		sb.append("<s:plannedEndDate>" + planned_end_date
				+ "</s:plannedEndDate>");

		sb.append("<s:plannedStartDate>" + planned_start_date
				+ "</s:plannedStartDate>");

		sb.append("<s:priority>" + priority_cd + "</s:priority>");

		sb.append("<s:region>CN</s:region>");

		sb.append("<s:regulatory>" + regulatory_cd + "</s:regulatory>");

		sb.append("<s:releaseRelated>" + release_related_cd
				+ "</s:releaseRelated>");

		sb.append("<s:requestType>" + changeType + "</s:requestType>");

		sb.append("<s:requestUrgency>" + urgency_cd + "</s:requestUrgency>");

		sb.append("<s:requestedCompletionDate>" + target_date
				+ "</s:requestedCompletionDate>");
		sb
				.append("<s:requesterDepartment>Health Connect</s:requesterDepartment>");

		sb.append("<s:requesterLoginName>" + requesterNUID
				+ "</s:requesterLoginName>");

		sb.append("<s:requesterName>" + requesterName + "</s:requesterName>");

		sb
				.append("<s:serviceArea>Regional Offices North - Emeryville</s:serviceArea>");

		sb.append("<s:sourceCode>Automated</s:sourceCode>");

		sb.append("<s:submitterGroup>KPHC ECP</s:submitterGroup>");

		sb.append("<s:summary>" + summary_tx + "</s:summary>");

		sb.append("<s:systemEnvironment>Production</s:systemEnvironment>");

		sb.append("<s:testPlan>" + test_tx + "</s:testPlan>");

		sb.append("<s:tieLine>" + tieline + "</s:tieLine>");

		sb.append("<s:titlePrefix>" + title_prefix + "</s:titlePrefix>");

		sb.append("<s:type>" + remedyType + "</s:type>");

		sb.append("<s:workLog>" + worklog_tx + "</s:workLog>");

		sb.append("<s:keyword>MAINCHANGE</s:keyword>");

		sb.append("</s:SubmitSRRFC></soap:Body>");

		System.out.println("SOAP BODY >" + sb.toString());
		return sb.toString();

	}

	public String formatSoapBody(Properties prop, String changeType,
			String requesterNUID, String requesterName) {

		StringBuffer sb = new StringBuffer();

		sb.append("<soap:Body><s:SubmitSRRFC>");

		sb.append("<s:attachedToRel>No</s:attachedToRel>");

		sb.append("<s:backoutPlan>" + getParm(prop, "backout_blob")
				+ "</s:backoutPlan>");

		sb.append("<s:businessUnit>Administration</s:businessUnit>");

		sb.append("<s:category>End User Applications</s:category>");

		sb.append("<s:dataCenterImpacted>Corona</s:dataCenterImpacted>");

		sb.append("<s:description>" + getParm(prop, "description_blob")
				+ "</s:description>");

		sb.append("<s:expedited>" + getYesNo(getParm(prop, "expedited_cd"))
				+ "</s:expedited>");

		sb.append("<s:facBldg>EMV - Health Connect - Emeryville</s:facBldg>");
		sb.append("<s:floorNumber>" + getParm(prop, "floor")
				+ "</s:floorNumber>");

		sb.append("<s:fundingCategory>Core-MRO</s:fundingCategory>");

		sb.append("<s:hotlist>" + getYesNo(getParm(prop, "hotlist_cd"))
				+ "</s:hotlist>");

		sb.append("<s:impact>" + getHighMediumLow(getParm(prop, "impact_cd"))
				+ "</s:impact>");

		sb.append("<s:implementationPlan>"
				+ getParm(prop, "implementation_blob")
				+ "</s:implementationPlan>");

		sb.append("<s:item>" + remedyItem + "</s:item>");

		sb.append("<s:kPHCRelatedImpacted>Yes</s:kPHCRelatedImpacted>");

		sb.append("<s:outageRequired>" + getYesNo(getParm(prop, "outage_cd"))
				+ "</s:outageRequired>");

		sb.append("<s:phoneNumber>" + getParm(prop, "tieline")
				+ "</s:phoneNumber>");

		sb.append("<s:plannedEndDate>"
				+ remedyDateTime(getParm(prop, "remedy_end_dt"))
				+ "</s:plannedEndDate>");

		sb.append("<s:plannedStartDate>"
				+ remedyDateTime(getParm(prop, "remedy_start_dt"))
				+ "</s:plannedStartDate>");

		sb.append("<s:priority>"
				+ getHighMediumLow(getParm(prop, "priority_cd"))
				+ "</s:priority>");

		sb.append("<s:region>CN</s:region>");

		sb.append("<s:regulatory>" + getYesNo(getParm(prop, "regulatory_cd"))
				+ "</s:regulatory>");

		sb.append("<s:releaseRelated>"
				+ (getParm(prop, "release_related_cd") == "Y" ? "Related"
						: "Not Related") + "</s:releaseRelated>");

		sb.append("<s:requestType>" + changeType + "</s:requestType>");

		sb.append("<s:requestUrgency>"
				+ getHighMediumLow(getParm(prop, "urgency_cd"))
				+ "</s:requestUrgency>");

		sb.append("<s:requestedCompletionDate>"
				+ remedyDateTime(getParm(prop, "target_date"))
				+ "</s:requestedCompletionDate>");
		sb
				.append("<s:requesterDepartment>Health Connect</s:requesterDepartment>");

		sb.append("<s:requesterLoginName>" + requesterNUID
				+ "</s:requesterLoginName>");

		sb.append("<s:requesterName>" + requesterName + "</s:requesterName>");

		sb
				.append("<s:serviceArea>Regional Offices North - Emeryville</s:serviceArea>");

		sb.append("<s:sourceCode>Automated</s:sourceCode>");

		sb.append("<s:submitterGroup>KPHC ECP</s:submitterGroup>");

		sb.append("<s:summary>" + getParm(prop, "title_nm") + "</s:summary>");

		sb.append("<s:systemEnvironment>Production</s:systemEnvironment>");

		sb
				.append("<s:testPlan>" + getParm(prop, "test_blob")
						+ "</s:testPlan>");

		sb.append("<s:tieLine>" + getParm(prop, "tieline") + "</s:tieLine>");

		sb.append("<s:titlePrefix>" + getParm(prop, "prefix")
				+ "</s:titlePrefix>");

		sb.append("<s:type>" + remedyType + "</s:type>");

		sb.append("<s:workLog>" + getParm(prop, "worklog_blob")
				+ "</s:workLog>");

		sb.append("<s:keyword>MAINCHANGE</s:keyword>");

		sb.append("</s:SubmitSRRFC></soap:Body>");

		System.out.println("SOAP BODY >" + sb.toString());

		return sb.toString();

	}

	public String getParm(Properties prop, String parm) {
		try {
			if (prop.containsKey((Object) parm)) {
				return prop.getProperty(parm);
			} else {
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	/*
	 * Prefered method, with the properties already set.
	 */
	public String submitChange(String changeType, String nuid, String userName) {

		return sendRequest(formatSoapBody(changeType, nuid, userName));
	}

	/*
	 * Legacy method, using properties from the HTTP request properties
	 * 
	 */

	public String submitChange(Properties prop, String changeType, String nuid,
			String userName, String remedyType, String remedyItem) {

		return sendRequest(formatSoapBody(prop, changeType, nuid, userName));
	}

	public String submitChange() {

		return sendRequest(example_soap_body);
	}

	private String getCaseId(String xml) {

		RemedyXMLParser parser = new RemedyXMLParser(xml, "SubmitSRRFCResponse");

		return parser.parseTextValue("changeID");
	}

	public String getErrorText() {

		try {
			RemedyXMLParser parser = new RemedyXMLParser(remedy_answer,
					"soapenv:Fault");

			return parser.parseTextValue("faultstring");
		} catch (Exception e) {
			return "Unknown remedy exception";
		}

	}

	// soapenv:Fault

	private String sendRequest(String soapBody) {

		boolean success = false;

		String server_url = remedyURL + "&webService=" + webService;

		String soap_header = soap_header_1 + remedyUserid + soap_header_2
				+ remedyPassword + soap_header_3;

		String soap_req_str = soap_xml_ns + soap_header + soapBody
				+ soap_envelope_end;

		HttpSoapConnection soap = new HttpSoapConnection(server_url,
				soap_action);

		try {
			remedy_answer = soap.soapSendReceive(soap_req_str);
			System.out.println("REmedy reply to CreateChnage " + remedy_answer);

			success = soap.getSuccess();
		} catch (Exception e) {
			System.out.println("Soap Exception : " + e.toString());
		}

		if (success) {
			return getCaseId(remedy_answer);
		}

		return "0"; // invalid requeset
	}

	/*
	 * internal string functions
	 * 
	 */
	private String remedyDateTime(String s) {

		// 2007-09-04T14:15:12-07:00

		SimpleDateFormat sdf_from_browser = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat sdf_to_query = new SimpleDateFormat(
				"yyyy-MM-dd'T16:00:00-07:00'");

		try {
			Date d = sdf_from_browser.parse(s);
			return sdf_to_query.format(d);

		} catch (ParseException e1) {
			return "";
		}
	}

	private String getHighMediumLow(String s) {
		if (s.equalsIgnoreCase("L")) {
			return "Low";
		}
		if (s.equalsIgnoreCase("M")) {
			return "Medium";
		}
		return "High";
	}

	private String getYesNo(String s) {
		if (s.equalsIgnoreCase("N")) {
			return "No";
		}
		return "Yes";
	}

	public static void main(String[] args) {

		System.out.println("Remedy Change_Submit Version 0.1 : "
				+ new Date().toString());

		RemedyConnection remedy = new RemedyConnection("PROD");
		RemedyChangeSubmit req = new RemedyChangeSubmit(remedy
				.getDefaultUserid(), remedy.getDefaultPassword(), remedy
				.GetRemedyURL());

		String case_id = req.submitChange(); // real qa rfc :

		if (!case_id.equalsIgnoreCase("0")) {
			System.out.println("New case created # " + case_id);
		} else {
			System.out.println("\nError ==> " + req.getErrorText());
		}

		System.out.println("\nRemedy Change_Submit complete "
				+ new Date().toString());

	}

}
