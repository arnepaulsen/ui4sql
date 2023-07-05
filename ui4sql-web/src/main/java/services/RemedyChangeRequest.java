/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

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

import services.ServicesException;
import java.sql.*;
import java.util.Date;
import java.util.Properties;

/*******************************************************************************
 * 
 * @author PAULSEAR
 * 
 *         Class for managing an Remedy ChangeInfo XML into database - parse and
 *         post RFC change info from the 'ESS_Change_Query' web service - parse
 *         and post the elective Attributes from the 'ESS_Attribute_Info' wen
 *         service
 * 
 *         Constructor populates with XML from Remedy Web Service call
 * 
 *         Also has method to save info from XML into database.
 * 
 *         When saving go trfc or tsr, parse out the names and match to tcontact
 *         table for owner_uid, requester_uid and submitter_uid.
 * 
 *         accessor methods to return specific variables generic get function
 *         getValue("name") to get any variable - make sure its a valid name
 * 
 * 
 *         10/19/07 - Add tieLine, which they kludge in Remedy to hold the
 *         related SR/RFC #, sometimes
 * 
 *         3/12/08 - Main routine will automatically loop through all non-closed
 *         rfc's, fetch Remedy info, and post them to the database
 * 
 *         4/21/08 - add interface for implementationPlan, TestPlan, backoutPlan
 * 
 *         10/11/08 - add PlannedStart to database
 * 
 *         11/15/08 - add 'pendingCode
 * 
 *         12/1/08 - allow the Main to post to other target tables trfc is
 *         default tip_tracker is new target table
 * 
 *         1/25/10 - Add "verifier name" from elective attributes
 * 
 *         2/5/10 - add plannedDuration
 * 
 *         2/17/10 - add resolution code
 * 
 *         11/30/10 - temp changes to update Closed SR's
 * 
 *         1/20/11 Remove 500 limit
 * 
 *         2/2/11 Add "Customer Request Date" to the elective attributes
 * 
 * 
 * 
 * 
 */
public class RemedyChangeRequest {

	private boolean mySql = true;

	private boolean debug = true;

	String top1 = " TOP 1 ";
	String isNullChar = "IsNull_Char";
	String isNullInt = "IsNull_Int";

	private String getDate = "now()";

	public String[][] electiveAttributes;

	public String[][] comments;

	byte cr = 13;

	byte lf = 10;

	byte[] crlf = { cr, lf };

	byte[] lfcr = { lf, cr };

	String sBR = new String("<BR>");

	String sCR = new String(crlf, 0, 1);

	String sLF = new String(lfcr, 0, 1);

	private int changeCount = 0;

	private int attributeCount = 0;

	private int commentCount = 0;

	private String changeID;

	private String customerRequestDateTx;

	private String soxAttribute ;
	
	private String businessNeed; // this in the only field updated by the
	// ess_attribute_info service

	private String verifierName;

	private String implementationPlan;
	private String backoutPlan;
	private String testPlan;

	private String description;

	private String plannedDuration;

	private String category;

	private String createDate;

	private String plannedStartDate;

	private String plannedEndDate;

	private String actualEndDate;

	private String approvalStatus;

	private String emergency;

	private String employeeType;

	private String rfcEstEffort;
	
	/*
	 * april 2011 release ::
	 */
	private String projectNumber;

	private String fundingCategory;
	
	
	/*
	 * end april release
	 */
	private String group;

	private String impact;

	private String escalated;

	private String expedited;

	private String item;

	private String origSubmitter;

	private String outageRequired;

	private String owner;

	private String phoneNumber;

	private String region;

	private String releaseRelated;

	private String requestUrgency;

	private String requesterLoginName;

	private String requesterName;

	private String serviceArea;

	private String requestType;

	private String requestedCompletionDate;

	private String kPHCRelatedImpacted;

	private String resolutionCode;

	private String closureCode;

	private String pendingCode;

	private String hotlist;

	private String requesterDepartment;

	private String status;

	private String tieLine;

	private String submitterGroup;

	private String submitterLoginName;

	private String systemEnvironment;

	private String summary;

	private String type;

	private String lastModifiedBy;

	private String modifiedDate;

	private String workLog;

	// Construct RFC object from the Remedy XML string

	public int getChangeCount() {
		return changeCount;
	}

	public int getAttributeCount() {
		return attributeCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public RemedyChangeRequest() {

	}

	public void initAttributeInfo(String xml, String tableName) {

		if (xml.length() < 1) {
			return;
		}

		Document dom = getDocument(xml);

		if (dom == null) {
			return;
		}

		Element el = getElementDocument(dom, "SearchAttributesByIDResponse");

		if (el != null) {

			parseElectiveAttributes(el, tableName);
		}
	}

	public void initCommentInfo(String xml) {

		if (xml.length() < 1) {
			return;
		}

		Document dom = getDocument(xml);

		if (dom == null) {
			return;
		}

		Element el = getElementDocument(dom, "SearchCommentByParentIdResponse");

		if (el != null) {

			parseComments(el);
		}
	}

	public RemedyChangeRequest(String xml) {

		if (mySql) {
			top1 = "";
			isNullInt = "IsNull_Int";
			isNullChar = "IsNull_Char";
		}

		if (xml.length() < 1) {
			return;
		}

		Document dom = getDocument(xml);

		if (dom == null) {
			return;
		}
		Element el = getElementDocument(dom, "SearchByChangeIDResponse");

		if (el != null) {
			parseRFC(el);
		}
	}

	private Document getDocument(String xmlString) {

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
			System.out.println("parseXMLFile.. ParserConfigurationException.");
			pce.printStackTrace();
		} catch (SAXException se) {
			System.out.println("parseXMLFile.. SaxException.");
			se.printStackTrace();
		} catch (IOException ioe) {
			System.out.println("parseXMLFile.. IOException.");
			ioe.printStackTrace();
		}

		return dom;
	}

	// use the rfc object to update the database

	public void attributesToDatabase(Connection conn, String tableName,
			String keyName, int keyValue) {
		try {

			StringBuffer sb = new StringBuffer();

			sb.append("UPDATE " + tableName + " set ");

			sb.append(" bsns_need_blob  = '" + businessNeed.replaceAll("'", "")
					+ "'");

			sb.append(", customer_rqst_date_tx = '" + customerRequestDateTx.replaceAll("'", "")
					+ "'");
			
			/* 
			 * April 2011 release
			 */
			if (tableName.equalsIgnoreCase("tremedy")) {
				sb.append(", sox_attribute_tx = '" + soxAttribute.replaceAll("'", "")
						+ "'");
			}
			
			sb.append(", remedy_verifier_nm = '"
					+ verifierName.replaceAll("'", "") + "'");

			sb.append(" WHERE " + keyName + " = " + keyValue);

			System.out.println("************* sql : " + sb.toString());

			runQuery(conn, sb.toString());

		} catch (services.ServicesException se) {
			System.out.println("services exception getting rfc : "
					+ se.toString());

		} catch (Exception e) {
			System.out.println("Error formatting sql string. ");
		}
	}

	public void commentsToDatabase(Connection conn, String tableName,
			String keyName, int keyValue) {
		try {

			if (comments == null)
				return;

			StringBuffer sb = new StringBuffer();

			sb.append("UPDATE " + tableName + " set remedy_comment_blob  =  '");

			for (int x = 0; x < comments[0].length; x++) {
				sb.append(comments[1][x].substring(0, 10) + " - "
						+ comments[0][x].replaceAll("'", "") + " - "
						+ comments[2][x].replaceAll("'", "") + "<BR><BR>");
			}

			sb.append("' WHERE " + keyName + " = " + keyValue);

			// System.out.println(" sql : " + sb.toString());

			runQuery(conn, sb.toString());

		} catch (services.ServicesException se) {
			System.out.println("services exception getting rfc : "
					+ se.toString());

		} catch (Exception e) {
			System.out.println("Error formatting sql string. ");
		}
	}

	public void rfcToDatabase(Connection conn, String tableName,
			String keyName, int keyValue) {

		// now put the info to the database..... geez

		debug("RemedyChangeRequest: rfcToDatabase .. starting.");

		debug(" table: " + tableName);

		String sImpact = defaultCode(getImpact()).substring(0, 1);
		String sUrgency = defaultCode(getRequestUrgency()).substring(0, 1);
		String sExpedited = defaultCode(getExpedited()).substring(0, 1);
		String sClosure = defaultCode(getClosureCode()).substring(0, 1);
		String sResolution = defaultCode(getResolutionCode()).substring(0, 1);
		String sEscalated = defaultCode(getEscalated()).substring(0, 1);
		String sOutage = defaultCode(getOutageRequired()).substring(0, 1);
		String sEmergency = defaultCode(getEmergency()).substring(0, 1);
		String sStatus = defaultCode(getStatus()).substring(0, 3);

		String createdDate = "";
		if (getCreatedDate().length() > 18) {
			createdDate = getCreatedDate().substring(0, 19);
		}

		String plannedEndDate = "";
		if (getPlannedEndDate().length() > 18) {
			plannedEndDate = getPlannedEndDate().substring(0, 19);
		}

		String actualEndDate = "";
		if (getActualEndDate().length() > 18) {
			actualEndDate = getActualEndDate().substring(0, 19);
		}

		String plannedStartDate = "";
		if (getPlannedStartDate().length() > 18) {
			plannedStartDate = getPlannedStartDate().substring(0, 19);
		}

		String requestedCompletionDate = "";
		if (getRequestedCompletionDate().length() > 18) {
			requestedCompletionDate = getRequestedCompletionDate().substring(0,
					19);
		}

		int owner_id = checkAddContact(conn, last_name(getOwner()),
				first_name(getOwner()));

		int submitter_id = checkAddContact(conn, last_name(getOrigSubmitter()),
				first_name(getOrigSubmitter()));

		int requester_id = checkAddContact(conn, last_name(getRequesterName()),
				first_name(getRequesterName()));

		// map the owner_name
		String owner_query = "(select " + top1 + isNullInt
				+ "(contact_id,0) from tcontact where first_name ='"
				+ first_name(getOwner()) + "' and last_name = '"
				+ last_name(getOwner()) + "') ";

		// map the owner_name
		String submitter_query = "(select " + top1 + isNullInt
				+ "(contact_id,0) from tcontact where first_name ='"
				+ first_name(getOrigSubmitter()) + "' and last_name = '"
				+ last_name(getOrigSubmitter()) + "' ) ";

		// map the owner_name
		String requester_query = "(select " + top1 + isNullInt
				+ "(contact_id,0) from tcontact where first_name ='"
				+ first_name(getRequesterName()) + "' and last_name = '"
				+ last_name(getRequesterName()) + "') ";

		owner_query = "" + owner_id;
		submitter_query = "" + submitter_id;
		requester_query = "" + requester_id;

		// map the approval status to the codes table
		String approve_cd_query = "(select "
				+ top1
				+ isNullChar
				+ "(code_value, '') from tcodes where code_type_id = 124 and code_desc  = '"
				+ defaultCode(getApprovalStatus()) + "')";

		try {

			debug("RemedyChangRequest: rfcToDatabase");

			StringBuffer sb = new StringBuffer();
			
			sb.append("UPDATE " + tableName + " set ");
			
			/*
			 * start of table-specific updates
			 */
		
			sb.append(" remedy_grp_tx = '" + getGroup() + "'");
			
			if (tableName.equalsIgnoreCase("trfc")) {
				sb.append(", project_abcid_tx = '" + projectNumber + "'");
				sb.append(", funding_cat_tx = '" + fundingCategory + "'" );
			}
	
			sb.append(", remedy_grp_cd = (select code_value from tcodes where tcodes.code_desc = '"
					+ getGroup() + "' and tcodes.code_type_id = 131)");

			sb.append(", remedy_cat_tx = '" + getCategory() + "'");

			sb.append(", remedy_planned_duration_tx = '" + getPlannedDuration()
					+ "'");

			sb.append(", remedy_asof_date = " + getDate);

			sb.append(", remedy_type_tx = '" + getType() + "'");

			sb.append(", remedy_item_tx = '" + getItem() + "'");

			sb.append(", urgency_cd = '" + sUrgency + "'");

			sb.append(", impact_cd = '" + sImpact + "'");

			sb.append(", remedy_pending_cd ='" + getPendingCode() + "'");

			sb.append(", expedited_cd = '" + sExpedited + "'");

			sb.append(", escalated_cd = '" + sEscalated + "'");

			sb.append(", status_cd = '" + sStatus + "'");

			sb.append(", resolution_cd = '" + sResolution + "'");

			sb.append(", closure_cd = '" + sClosure + "'");

			sb.append(", remedy_approve_cd = " + approve_cd_query);

			sb.append(", outage_cd = '" + sOutage + "'");

			sb.append(", emergency_cd = '" + sEmergency + "'");

			sb.append(", remedy_end_dt = " + dateToSQL(plannedEndDate));

			sb.append(", remedy_start_dt = " + dateToSQL(plannedStartDate));

			sb.append(", remedy_act_end_dt = " + dateToSQL(actualEndDate));

			sb.append(", remedy_requested_completion_dt = "
					+ dateToSQL(requestedCompletionDate));

			sb.append(", remedy_create_dt = '" + createdDate + "'");

			sb.append(", owner_uid = " + owner_query);

			sb.append(", submitter_uid = " + submitter_query);

			sb.append(" ,requester_uid = " + requester_query);

			sb.append(", remedy_effort_tx = ' " + rfcEstEffort + "'");
			
			sb.append(", description_blob = '"
					+ replace_LF_BR(trimField(getDescription(), 2000)) + "'");

			sb.append(", worklog_blob = '"
					+ replace_LF_BR(trimField(getWorkLog(), 2000)) + "'");

			sb.append(", title_nm = '" + trimField(getSummary(), 128) + "'");

			// only save the tieLine (holding related_item) if it starts with
			// RFC/SR

			if (getTieLine().startsWith("RFC") || getTieLine().startsWith("SR")) {
				sb.append(", remedy_related_item_tx = '" + getTieLine() + "'");
			}

			sb.append(" WHERE " + keyName + " = " + keyValue);

			debug(" update rfc : " + sb.toString());

			runQuery(conn, sb.toString());

		} catch (services.ServicesException se) {
			System.out.println("services exception getting rfc : "
					+ se.toString());

		} catch (Exception e) {
			System.out.println("Error formatting sql string. ");
		}

		debug("rfcToDatabase exiting paragraph.");

	}

	private int checkAddContact(Connection conn, String lastName,
			String firstName) {

		int contact_id = queryContact(conn, lastName, firstName);

		if (contact_id > 0)
			return contact_id;

		contact_id = addContact(conn, lastName, firstName);

		return contact_id;

	}

	/*
	 * return SQL string for a Remedy date
	 * 
	 * Note... if blank - return word "null" else return value with single
	 * quotes ''
	 */
	private String dateToSQL(String s) {

		if (s.length() < 1) {
			return "null";
		}

		return "'" + s + "'";

	}

	private int queryContact(Connection conn, String lastName, String firstName) {

		// HACK - forcing contact_id to the proxy login!

		if (lastName.equalsIgnoreCase("wspmoc")) {
			return 721;
		}

		int key = 0;
		String sql = "SELECT contact_id from tcontact where last_name = '"
				+ lastName.replaceAll("'", "") + "' and first_name = '"
				+ firstName.replaceAll("'", "") + "'";
		// System.out.println(sql);
		Statement stmt;
		ResultSet rs;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				// System.out.println("got contact # " + rs.getInt(1));
				key = rs.getInt(1);
			}

		} catch (SQLException se) {
			System.out.println("RemedyChangeRequest:getRS: " + se.toString());
		}

		return key;

	}

	// Add a contact, and re-query to get the id.
	private int addContact(Connection conn, String lastName, String firstName) {

		int contact_id = 0;

		String sql = "INSERT INTO tcontact (last_name, first_name, division_id, contact_type_cd, active_flag, added_uid, added_date	 ) VALUES ('"
				+ new String(lastName.replaceAll("'", ""))
				+ "','"
				+ new String(firstName.replaceAll("'", ""))
				+ "', 1, 'GEN', 'Y', 1, " + getDate + ")";
		// System.out.println(sql);

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
			return queryContact(conn, lastName, firstName);
		} catch (SQLException e) {
			debug("tcontact INSERT error  " + e.toString());
		}
		return contact_id;
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
		if (s == null) {
			return "";
		}
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

		// debug("running query ..");

		debug(" sql : " + parmQuery);

		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(parmQuery);
		} catch (SQLException se) {
			int e = se.getErrorCode();

			if (e == 2627)
				return e;

			// debug("jdbc on update : " + e);
			throw new ServicesException(se.toString());

			// return true;
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	private Element getElementDocument(Document dom, String tagname) {
		// get the root elememt

		Element el = null;
		Element docEle = dom.getDocumentElement();
		NodeList nl = docEle.getElementsByTagName(tagname);
		changeCount = nl.getLength();

		if (nl != null && nl.getLength() > 0) {
			el = (Element) nl.item(0);
		}
		return el;
	}

	/*
	 * the attributes are arrays, like: <getListValues>
	 * <parentID>CHG000001892736</parentID> <attributeName>HealthConnect
	 * Impact</attributeName> <attributeValue>No impact to
	 * user.</attributeValue> </getListValues>
	 */
	private void parseElectiveAttributes(Element e, String tableName ) {

		customerRequestDateTx = "";
		businessNeed = "";
		verifierName = "";

		attributeCount = 0;

		NodeList nl = e.getElementsByTagName("getListValues");

		if (nl != null && nl.getLength() > 0) {

			electiveAttributes = new String[2][nl.getLength()];

			for (int x = 0; x < nl.getLength(); x++) {

				Element child = (Element) nl.item(x);

				String attrName = getTextValue(child, "attributeName");
				String attrValue = getTextValue(child, "attributeValue");

				electiveAttributes[0][x] = new String(attrName);
				electiveAttributes[1][x] = new String(attrValue);

				// debug("attr name:" + electiveAttributes[0][x] +
				// " value: "
				// + electiveAttributes[1][x]);

				if (attrName.equalsIgnoreCase("Business Need Justification")) {
					businessNeed = attrValue;
					attributeCount++;
				}
				
				if (attrName.equalsIgnoreCase("KPHC NCAL SOX") && tableName.equalsIgnoreCase("tremedy")) {
					soxAttribute = attrValue;
					attributeCount++;
				}

				if (attrName.equalsIgnoreCase("Customer Request Date")) {
					customerRequestDateTx = attrValue;
					attributeCount++;
				}

				/*
				 * if (attrName.equalsIgnoreCase("Verifier")) { verifierName =
				 * attrValue; System.out.println("************ Verifier " +
				 * verifierName ); attributeCount++; }
				 */

				if (attrName.startsWith("Verifier")
						|| attrName.startsWith("verifier")) {
					verifierName = attrValue;
					System.out.println("************ Verifier " + verifierName);
					attributeCount++;
				}

				// Release Specialist
				if (attrName.equalsIgnoreCase("Release Specialist")) {
					verifierName = attrValue;
					System.out.println("************ Release Specialist "
							+ verifierName);
					attributeCount++;
				}

			}
		}
	}

	private void parseComments(Element e) {

		commentCount = 0;
		comments = null;

		NodeList nl = e.getElementsByTagName("getListValues");

		if (nl != null && nl.getLength() > 0) {

			comments = new String[3][nl.getLength()];

			for (int x = 0; x < nl.getLength(); x++) {

				Element child = (Element) nl.item(x);

				String comment = getTextValue(child, "Comment");
				String submitter = getTextValue(child, "Submitter_Name");
				String date = getTextValue(child, "Create_Date");

				comments[0][x] = new String(submitter);
				comments[1][x] = new String(date);
				comments[2][x] = new String(comment);

				commentCount++;
			}
		}
		return;
	}

	private void parseRFC(Element e) {

		// set defaults;

		plannedDuration = saveXML(e, "plannedDuration");
		implementationPlan = saveXML(e, "implementationPlan");
		testPlan = saveXML(e, "testPlan");
		backoutPlan = saveXML(e, "backoutPlan");
		approvalStatus = saveXML(e, "approvalStatus");
		category = saveXML(e, "category");
		changeID = saveXML(e, "changeID");
		createDate = saveXML(e, "createDate");
		description = saveXML(e, "description");
		emergency = saveXML(e, "emergency");
		rfcEstEffort = saveXML(e, "rfcEstEffort");
		projectNumber = saveXML(e, "projectNumber");
		fundingCategory = saveXML(e, "fundingCategory");
		escalated = saveXML(e, "escalated");
		expedited = saveXML(e, "expedited");
		employeeType = saveXML(e, "employeeType");
		tieLine = saveXML(e, "tieLine");
		closureCode = saveXML(e, "closureCode");
		resolutionCode = saveXML(e, "resolutionCode");
		pendingCode = saveXML(e, "pendingCode");
		group = saveXML(e, "group");
		hotlist = saveXML(e, "hotlist");
		impact = saveXML(e, "impact");
		item = saveXML(e, "item");
		plannedStartDate = saveXML(e, "plannedStartDate");
		plannedEndDate = saveXML(e, "plannedEndDate");
		actualEndDate = saveXML(e, "actualEndDate");
		requestedCompletionDate = saveXML(e, "requestedCompletionDate");
		kPHCRelatedImpacted = saveXML(e, "kPHCRelatedImpacted");
		origSubmitter = saveXML(e, "origSubmitter");
		outageRequired = saveXML(e, "outageRequired");
		owner = saveXML(e, "owner");
		phoneNumber = saveXML(e, "phoneNumber");
		region = saveXML(e, "region");
		releaseRelated = saveXML(e, "releaseRelated");
		requestUrgency = saveXML(e, "requestUrgency");
		requesterName = saveXML(e, "requesterName");
		requesterLoginName = saveXML(e, "requesterLoginName");
		serviceArea = saveXML(e, "serviceArea");
		requestType = saveXML(e, "requestType");
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

		}
		return "";
	}

	public void printRFC() {
		System.out.println("ChangeID: " + getChangeID());
		System.out.println("Description: " + getDescription());
		System.out.println("Category: " + getCategory());
		System.out.println("Status: " + getApprovalStatus());
	}

	public String getDescription() {
		return description;
	}

	public String getImplementationPlan() {
		return implementationPlan;
	}

	public String getBackoutPlan() {
		return backoutPlan;
	}

	public String getTestPlan() {
		return testPlan;
	}

	public String getCategory() {
		return category;
	}

	public String getTieLine() {
		return tieLine;
	}

	public String getRfcEstEffort() {
		return rfcEstEffort;
	}

	public String getCreateDate() {
		return createDate;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public String getChangeID() {
		return changeID;
	}

	public String getPlannedDuration() {
		return plannedDuration;
	}

	public int stringToInt(String s) {
		try {
			if (s == null) {
				return 0;
			}
			return Integer.parseInt(s.substring(0, s.indexOf("."))) + 1;

		} catch (Exception e) {
			debug("except parsing int " + e.toString());
			return 0;
		}
	}

	public String getResolutionCode() {
		return resolutionCode;
	}

	public String getClosureCode() {
		return closureCode;
	}

	public String getPendingCode() {
		return pendingCode;
	}

	public String getEmergency() {
		return emergency;
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

	public String getOutageRequired() {
		return outageRequired;
	}

	public String getCreatedDate() {
		return createDate;
	}

	public String getPlannedStartDate() {
		return plannedStartDate;
	}

	public String getActualEndDate() {
		return actualEndDate;
	}

	public String getPlannedEndDate() {
		return plannedEndDate;
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

	public String getRequesterLoginName() {
		return requesterLoginName;
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

	private void debug(String s) {
		if (debug)
			System.out.println(s);
	}

	private String replace_LF_BR(String s) {

		// java 1.4

		// HtmlHelper helper = new HtmlHelper();

		// String x = helper.fix(s, sLF, sLF + sLF);

		// java 1.5
		String x = s.replaceAll(sLF, sLF + sLF);

		return s;

	}

	/**
	 * @param args
	 * 
	 *            Main - Loop through all trfc open records, and post remedy XML
	 *            into to the database.
	 * 
	 */
	public static void main(String[] args) {

		System.out.println("RemedyChangeRequest Vers 2.1 : Starting at "
				+ new Date().toString());

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
				+ " where status_cd <> 'Clo'  ";

		// + " where status_cd = 'P' and remedy_id > 405" ;

		try {
			ResultSet rs = batchSQL.getRS(query);

			int z = 0;

			// 1/20/11 Remove 500 limit

			while (rs.next()) {

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

		System.out.println("RemedyChangeRequest: Processed " + counter
				+ ". Ending at " + new Date().toString());

	}

}
