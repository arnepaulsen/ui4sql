/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

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

import services.ServicesException;

import java.sql.*;

/*******************************************************************************
 * 
 * @author PAULSEAR
 * 
 * Class for managing a Remedy problem report - maps to tincident table in
 *
 * Constructor populates with XML from Remedy Web Service call
 * 
 * Also has method to save info from XML into database.
 * 
 * When saving go tincident, parse out the names and match to tcontact table for
 * owner_uid, requester_uid and submitter_uid.
 * 
 * accessor methods to return specific variables generic get function
 * getValue("name") to get any variable - make sure its a valid name
 * 
 * 
 */
public class RemedyProblemReport {

	private int changeCount = 0;

	private String caseID;

	private String description;

	private String category;

	private String createDate;

	private String plannedStartDate;

	private String approvalStatus;

	private String employeeType;

	private String group;

	private String impact;

	private String escalated;

	private String expedited;

	private String item;

	private String origSubmitter;

	private String owner;

	private String phoneNumber;

	private String region;

	private String releaseRelated;

	private String requestUrgency;

	private String requesterName;

	private String serviceArea;

	private String requestType;

	private String requestedCompletionDate;

	private String kPHCRelatedImpacted;

	private String closureCode;

	private String hotlist;

	private String requesterDepartment;

	private String arrivelTime;

	private String status;

	private String submitterGroup;

	private String submitterLoginName;

	private String systemEnvironment;

	private String summary;

	private String type;

	private String lastModifiedBy;

	private String modifiedDate;

	private String workLog;

	// Construct problem object from the Remedy XML string

	public int getChangeCount() {
		return changeCount;
	}

	public RemedyProblemReport(String xml) {

		if (xml.length() < 1) {
			return;
		}

		Document dom = getDocument(xml);

		if (dom == null) {
			return;
		}
		Element el = getElementDocument(dom);

		if (el != null) {
			parseElement(el);
		}
	}

	private Document getDocument(String xmlString) {
		// get the factory

		Document dom = null;

		if (xmlString.length() < 1) {
			return dom;
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file

			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));

			dom = db.parse(inStream);

		} catch (ParserConfigurationException pce) {
			debug("parseXMLFile.. ParserConfigurationException.");
			pce.printStackTrace();
		} catch (SAXException se) {
			debug("parseXMLFile.. SaxException.");
			se.printStackTrace();
		} catch (IOException ioe) {
			debug("parseXMLFile.. IOException.");
			ioe.printStackTrace();
		}

		return dom;
	}

	private Element getElementDocument(Document dom) {
		// get the root elememt

		Element el = null;

		Element docEle = dom.getDocumentElement();

		// get a nodelist of <employee> elements
		NodeList nl = docEle.getElementsByTagName("SearchByCaseIDResponse");

		changeCount = nl.getLength();

		if (nl != null && nl.getLength() > 0) {

			el = (Element) nl.item(0);
		}

		return el;

	}

	private void parseElement(Element e) {

		// set defaults;
		approvalStatus = saveXML(e, "approvalStatus");
		category = saveXML(e, "category");
		caseID = saveXML(e, "caseID");
		createDate = saveXML(e, "createDate");
		description = saveXML(e, "description");
		escalated = saveXML(e, "escalated");
		expedited = saveXML(e, "expedited");
		employeeType = saveXML(e, "employeeType");
		arrivelTime = saveXML(e, "arrivelTime");
		closureCode = saveXML(e, "closureCode");
		group = saveXML(e, "group");
		hotlist = saveXML(e, "hotlist");
		impact = saveXML(e, "impact");
		item = saveXML(e, "item");
		plannedStartDate = saveXML(e, "plannedStartDate");
		kPHCRelatedImpacted = saveXML(e, "kPHCRelatedImpacted");
		origSubmitter = saveXML(e, "origSubmitter");
		owner = saveXML(e, "owner");
		phoneNumber = saveXML(e, "phoneNumber");
		region = saveXML(e, "region");
		releaseRelated = saveXML(e, "releaseRelated");
		requestUrgency = saveXML(e, "requestUrgency");
		requesterName = saveXML(e, "requesterName");
		serviceArea = saveXML(e, "serviceArea");
		requestType = saveXML(e, "requestType");
		requestedCompletionDate = saveXML(e, "requestedCompletionDate");
		requesterDepartment = saveXML(e, "requesterDepartment");
		status = saveXML(e, "status");
		submitterGroup = saveXML(e, "submitterGroup");
		submitterLoginName = saveXML(e, "submitterLoginName");
		systemEnvironment = saveXML(e, "systemEnvironment");
		summary = saveXML(e, "summary");
		type = saveXML(e, "type");
		lastModifiedBy = saveXML(e, "lastModifiedBy");
		modifiedDate = saveXML(e, "modifiedDate");
		workLog = saveXML(e, "workLog");

	}

	private String saveXML(Element e, String tag_name) {

		try {
			return getTextValue(e, tag_name);

		} catch (Exception ex) {
			// debug("exception in saveXML: tagname :" + tag_name + " "
			// + ex.toString());
		}
		return "";
	}

	public void printProblem() {
		debug("caseID: " + getcaseID());
		debug("Description: " + getDescription());
		debug("Category: " + getCategory());
		debug("Status: " + getApprovalStatus());
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public String getCreateDate() {
		return createDate;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public String getArrivelTime() {
		return arrivelTime;
	}

	public String getcaseID() {
		return caseID;
	}

	public String getClosureCode() {
		return closureCode;
	}

	public String getEmployeeType() {
		return employeeType;
	}

	public String getGroup() {
		return group;
	}

	public String getImpact() {
		return impact;
	}

	public String getEscalated() {
		return escalated;
	}

	public String getExpedited() {
		return expedited;
	}

	public String getItem() {
		return item;
	}

	public String getOrigSubmitter() {
		return origSubmitter;
	}

	public String getPlannedStartDate() {
		return plannedStartDate;
	}

	public String getOwner() {
		return owner;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getRegion() {
		return region;
	}

	public String getReleaseRelated() {
		return releaseRelated;
	}

	public String getRequestUrgency() {
		return requestUrgency;
	}

	public String getRequesterName() {
		return requesterName;
	}

	public String getServiceArea() {
		return serviceArea;
	}

	public String getRequestType() {
		return requestType;
	}

	public String getRequestedCompletionDate() {
		return requestedCompletionDate;
	}

	public String getKPHCRelatedImpacted() {
		return kPHCRelatedImpacted;
	}

	public String getHotlist() {
		return hotlist;
	}

	public String getRequesterDepartment() {
		return requesterDepartment;
	}

	public String getStatus() {
		return status;
	}

	public String getSubmitterGroup() {
		return submitterGroup;
	}

	public String getSubmitterLoginName() {
		return submitterLoginName;
	}

	public String getSystemEnvironment() {
		return systemEnvironment;
	}

	public String getSummary() {
		return summary;
	}

	public String getType() {
		return type;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public String getModifiedDate() {
		return modifiedDate;
	}

	public String getWorkLog() {
		return workLog;
	}

	/**
	 * Calls getTextValue and returns a int value
	 * 
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private int getIntValue(Element ele, String tagName) {
		// in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele, tagName));
	}

	private String getTextValue(Element ele, String tagName) {
		String textVal = "";
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	// use the incident/problem object to update the database

	public void saveToDatabase(Connection conn, String tableName,
			String keyName, int keyValue) {

		// now put the info to the database..... geez

		String sImpact = defaultCode(getImpact()).substring(0, 1);
		String sUrgency = defaultCode(getRequestUrgency()).substring(0, 1);
		String sExpedited = defaultCode(getExpedited()).substring(0, 1);
		String sClosure = defaultCode(getClosureCode()).substring(0, 1);
		String sEscalated = defaultCode(getEscalated()).substring(0, 1);
		String sStatus = defaultCode(getStatus()).substring(0, 3);

		String startDate = "";
		if (getArrivelTime().length() > 18) {
			startDate = getArrivelTime().substring(0, 19);
		}

		// map the owner_name
		String owner_query = "(select top 1 isnull(contact_id,0) from tcontact where first_name ='"
				+ first_name(getOwner())
				+ "' and last_name = '"
				+ last_name(getOwner()) + "') ";

		// map the owner_name
		String submitter_query = "(select top 1 isnull(contact_id,0) from tcontact where first_name ='"
				+ first_name(getOrigSubmitter())
				+ "' and last_name = '"
				+ last_name(getOrigSubmitter()) + "') ";

		// map the owner_name
		String requester_query = "(select top 1 isnull(contact_id,0) from tcontact where first_name ='"
				+ first_name(getRequesterName())
				+ "' and last_name = '"
				+ last_name(getRequesterName()) + "') ";

		// map the approval status to the codes table
		String approve_cd_query = "(select top 1 isnull(code_value, '') from tcodes where code_type_id = 124 and code_desc  = '"
				+ defaultCode(getApprovalStatus()) + "')";

		try {

			String sql = "update " + tableName
					+ " set  remedy_asof_date = getDate(), remedy_grp_tx = '"
					+ trimField(getGroup(), 128) + "', remedy_cat_tx = '"
					+ trimField(getCategory(), 128) + "' , remedy_type_tx = '"
					+ trimField(getType(), 128) + "', remedy_item_tx = '"
					+ trimField(getItem(), 128) + "', urgency_cd = '"
					+ sUrgency + "', impact_cd = '" + sImpact
					+ "', expedited_cd = '" + sExpedited
					+ "', escalated_cd = '" + sEscalated + "', status_cd = '"
					+ sStatus + "', closure_cd = '" + sClosure
					+ "', remedy_approve_cd = " + approve_cd_query
					+ ", start_date = '" + startDate + "', owner_uid = "
					+ owner_query + ", submitter_uid = " + submitter_query
					+ ", requester_uid = " + requester_query
					+ ", worklog_blob = '" + trimField(getWorkLog(), 1000)
					+ "'" + ", description_blob = '"
					+ trimField(getDescription(), 1000) + "', title_nm = '"
					+ trimField(getSummary(), 128) + "' where " + keyName
					+ " = " + keyValue;

			debug("remedy sql: " + sql);

			runQuery(conn, sql);

		} catch (services.ServicesException se) {
			debug("services exception getting incident : " + se.toString());

		} catch (Exception e) {
			debug("Error formatting sql string. ");
		}

	}

	// parse the first name out of a person name string
	private String first_name(String name) {

		String s = new String("");

		String[] result = name.split("\\s");

		if (result.length > 0)
			s = result[0];

		return s;

	}

	// parse the first name out of a person name string
	private String last_name(String name) {

		String s = new String("");

		String[] result = name.split("\\s");

		if (result.length > 1)
			s = result[result.length - 1];

		return s;

	}

	// set code to min length of 3 (status code is 3 chars)
	private String defaultCode(String s) {
		if (s.length() < 1)
			return "   ";
		else
			return s;
	}

	private String trimField(String s, int max) {
		String answer;
		if (s.length() > max)
			answer = s.substring(0, max);
		else
			answer = s;
		return answer.replaceAll("'", "").trim();

	}

	// *********************************************
	// general run query
	// *********************************************
	public int runQuery(Connection conn, String parmQuery)
			throws services.ServicesException {

		//

		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(parmQuery);
		} catch (SQLException se) {
			int e = se.getErrorCode();

			if (e == 2627)
				return e;

			debug("jdbc on update : " + e);
			throw new ServicesException(se.toString());

			// return true;
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	private void debug(String s) {
		System.out.println(s);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
