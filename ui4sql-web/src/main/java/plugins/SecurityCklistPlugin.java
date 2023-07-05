/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * 
 *   2/15 added mySql
 * */

/*******************************************************************************
 * Tollgate Manager
 * 
 * This data manager is unique in that the questions asked depend on the DMAIC
 * code value. The questions on the html page cannot be displayed until after
 * add mode is completed and a DMAIC code has been selected.
 * 
 * In display or update mode, the questions are obtained from table tfields
 * depending on the DMAIC code selected.
 * 
 * Change Log:
 * 
 * 
 ******************************************************************************/

public class SecurityCklistPlugin extends AbsProjectPlugin {

	// *******************
	// *******************
	// CONSTRUCTORS *
	// *******************
	// *******************

	public String getCustomSubForm() {
		return db.getText("section_cd");
	}

	public SecurityCklistPlugin() throws services.ServicesException {
		super();

		this.setTableName("tsecurity_cklist");
		this.setKeyName("security_cklist_id");
		this.setTargetTitle("Security Checklist");

		this.setListHeaders( new String[] { "Section", "Phase", "Application" });

		this.setMoreListColumns(new  String[] {
				"section.code_desc as section_desc",
				"phase.code_desc as phase_desc", "application_name" });
		this.setMoreListJoins(new  String[] {
				" left join tcodes section on tsecurity_cklist.section_cd = section.code_value and section.code_type_id  = 48 ",
				" left join tcodes phase on tsecurity_cklist.phase_cd = phase.code_value and phase.code_type_id  = 41 ",
				" left join tapplications on tsecurity_cklist.application_id = tapplications.application_id " });

		this.setMoreSelectJoins (this.moreListJoins);
		this.setMoreSelectColumns ( this.moreListColumns);

	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht;

		if (addMode) {
			ht = showNewPage();
		} else {
			ht = showUpdatePage();
		}
		return ht;

	}

	/*
	 * 
	 * New Tollgate pages just get the project and dmiac id,
	 * 
	 * because the questions to display depend on the phase.
	 */
	private Hashtable showNewPage() {

		Hashtable ht = new Hashtable();

		ht.put("application_id", new WebFieldSelect("application_id", "0", sm
				.getApplicationFilter()));

		ht.put("section_cd", new WebFieldSelect("section_cd", "S01", sm
				.getCodes("SECURITYCKLIST")));

		ht.put("phase_cd", new WebFieldSelect("phase_cd", "", sm
				.getCodes("LIFECYCLE")));

		ht
				.put(
						"msg",
						new WebFieldDisplay("msg",
								"Please first select a checklist section, then select Save-Edit."));

		return ht;

	}

	/*
	 * 
	 * This is always Update or Display, no need to worry about add ( no value
	 * in the dbInterface rs)
	 */
	private Hashtable showUpdatePage() throws services.ServicesException {

		/* save off the dmaic_cd when processing updates  WHY-WHY*/

		//sm.setFilterCode("section_cd", db.getText("section_cd"));

		Hashtable ht = new Hashtable();

		/*
		 * Web Fields for all dmaic phases - hardcoded on template
		 * 
		 */
		ht.put("project_id", new WebFieldDisplay("project_id", db
				.getText("project_name")));

		ht.put("section_cd", new WebFieldDisplay("section_cd", db
				.getText("section_desc")));

		ht.put("phase_cd", new WebFieldDisplay("phase_cd", db
				.getText("phase_desc")));

		ht.put("application_id", new WebFieldDisplay("application_id", db
				.getText("application_name")));

		ht.put("version_nm", new WebFieldString("version_nm", db
				.getText("version_nm"), 4, 4));
		
		ht.put("status_cd", new WebFieldSelect("status_cd",
				db.getText("status_cd"), sm
						.getCodes("ISSUESTAT")));
		

		// ht.put("version_date", new WebFieldDate("version_date", db
		// .getText("version_date")));

		/*
		 * Add in the section-specific questions
		 * 
		 */
		if (db.getText("section_cd").equalsIgnoreCase("S02")) {
			add_Section_02(ht);
		}

		if (db.getText("section_cd").equalsIgnoreCase("S03")) {
			add_Section_03(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S04")) {
			add_Section_04(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S05")) {
			add_Section_05(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S06")) {
			add_Section_06(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S07")) {
			add_Section_07(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S08")) {
			add_Section_08(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S09")) {
			add_Section_09(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S10")) {
			add_Section_10(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S11")) {
			add_Section_11(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S12")) {
			add_Section_12(ht);
		}
		if (db.getText("section_cd").equalsIgnoreCase("S13")) {
			add_Section_13(ht);
		}
		
		return ht;

	}

	private void add_Section_02(Hashtable ht) {

		String[][] q01 = {
				{ "I", "C", "E", "O" },
				{ "In-House", "Commercial off-the-shelf (COTS)", "External",
						"Other (please specifiy)" } };

		String[][] q02 = { { "N", "L", "E" },
				{ "New", "Legacy", "Undergoing major enhancements." } };

		String[][] q03 = { { "I", "E" },
				{ "Within Network", "External Network" } };

		String[][] q04 = { { "T", "S" },
				{ "Temporary Solution", "Permanent Solution" } };

		String[][] q05 = { { "B", "I" },
				{ "Business Supported", "IT Supported" } };

		String[][] q06 = {
				{ "N", "D", "T", "B" },
				{ "Not Outsourced", "Development and/or Maintenance",
						"Testing", "Both" } };

		String[][] q07 = { { "I", "O" }, { "Inside U.S.", "Outside U.S." } };

		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"2.1 The application developed by: "));
		ht.put("q01_cd",
				new WebFieldSelect("q01_cd", db.getText("q01_cd"), q01));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

		ht.put("q02_q",
				new WebFieldDisplay("q02_q", "2.2 The application is: "));
		ht.put("q02_cd",
				new WebFieldSelect("q02_cd", db.getText("q02_cd"), q02));
		ht.put("q02_rmk", new WebFieldText("q02_rmk", db.getText("q02_rmk"), 3,
				40));

		ht.put("q03_q", new WebFieldDisplay("q03_q",
				"2.3 Where does the application run: "));
		ht.put("q03_cd",
				new WebFieldSelect("q03_cd", db.getText("q03_cd"), q03));
		ht.put("q03_rmk", new WebFieldText("q03_rmk", db.getText("q03_rmk"), 3,
				40));

		ht.put("q04_q", new WebFieldDisplay("q04_q", "2.4 Solution Type: "));
		ht.put("q04_cd",
				new WebFieldSelect("q04_cd", db.getText("q04_cd"), q04));
		ht.put("q04_rmk", new WebFieldText("q04_rmk", db.getText("q04_rmk"), 3,
				40));

		ht.put("q05_q", new WebFieldDisplay("q05_q",
				"2.5 Support orgagnization: "));
		ht.put("q05_cd",
				new WebFieldSelect("q05_cd", db.getText("q05_cd"), q05));
		ht.put("q05_rmk", new WebFieldText("q05_rmk", db.getText("q05_rmk"), 3,
				40));

		ht.put("q06_q", new WebFieldDisplay("q06_q", "2.6 Outsourcing: "));
		ht.put("q06_cd",
				new WebFieldSelect("q06_cd", db.getText("q06_cd"), q06));
		ht.put("q06_rmk", new WebFieldText("q06_rmk", db.getText("q06_rmk"), 3,
				40));

		ht.put("q07_q", new WebFieldDisplay("q07_q",
				"2.7 If outsourced, vendor is:"));
		ht.put("q07_cd",
				new WebFieldSelect("q07_cd", db.getText("q07_cd"), q07));
		ht.put("q07_rmk", new WebFieldText("q07_rmk", db.getText("q07_rmk"), 3,
				40));

		ht
				.put(
						"q08_q",
						new WebFieldDisplay(
								"q08_q",
								"2.8 Are there any waivers assocated with this application? If so, list the waiver control number."));
		ht.put("q08_cd", new WebFieldSelect("q08_cd", db.getText("q08_cd"), sm
				.getCodes("YESNO")));
		ht.put("q08_rmk", new WebFieldText("q08_rmk", db.getText("q08_rmk"), 3,
				40));

	}

	private void add_Section_03(Hashtable ht) {

		String[][] q02 = { { "N", "L", "E" },
				{ "New", "Legacy", "Undergoing major enhancements." } };

		String[][] q03 = { { "R", "C", "I", "P" },
				{ "Restricted", "Confidential", "Internal Use", "Public" } };

		String[][] q04 = {
				{ "0", "1", "2" },
				{ "Not performed", "Within Last 12 Months",
						"More than 12 months" } };

		String[][] q05 = {
				{ "N", "B", "I", "O" },
				{ "Not performed", "Business Management",
						"Technology Management", "Other" } };

		ht
				.put(
						"q01_q",
						new WebFieldDisplay(
								"q01_q",
								"3.1 Does this application generate, process, or store electronic sensitive information? "));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

		ht
				.put(
						"q02_q",
						new WebFieldDisplay("q02_q",
								"3.1.1 Will protected information be exchanged with vendors? "));
		ht.put("q02_cd", new WebFieldSelect("q02_cd", db.getText("q02_cd"), sm
				.getCodes("YESNO")));
		ht.put("q02_rmk", new WebFieldText("q02_rmk", db.getText("q02_rmk"), 3,
				40));

		ht.put("q03_q", new WebFieldDisplay("q03_q",
				"3.2 Highest data classification: "));
		ht.put("q03_cd",
				new WebFieldSelect("q03_cd", db.getText("q03_cd"), q03));
		ht.put("q03_rmk", new WebFieldText("q03_rmk", db.getText("q03_rmk"), 3,
				40));

		ht.put("q04_q", new WebFieldDisplay("q04_q",
				"3.3 Latest risk accessment, give details:"));
		ht.put("q04_cd",
				new WebFieldSelect("q04_cd", db.getText("q04_cd"), q04));
		ht.put("q04_rmk", new WebFieldText("q04_rmk", db.getText("q04_rmk"), 3,
				40));

		ht.put("q05_q", new WebFieldDisplay("q05_q",
				"3.4 Who approved the risk plan?"));
		ht.put("q05_cd",
				new WebFieldSelect("q05_cd", db.getText("q05_cd"), q05));
		ht.put("q05_rmk", new WebFieldText("q05_rmk", db.getText("q05_rmk"), 3,
				40));

	}

	private void add_Section_04(Hashtable ht) {
		String[][] q01 = {
				{ "1", "2", "3", "4", "5", "6", "7" },
				{ "None", "Enterprise LDAP", "E-Business LDAP", "Oblix CoreID",
						"Active Directory", "RACF", "Other" } };

		String[][] q02 = { { "N", "E", "P", "A" },
				{ "None", "Element", "Page", "Application" } };

		String[][] q03 = {
				{ "N", "L", "S", "E" },
				{ "Not performed", "Local accounts and administration",
						"Account synchronization to other database",
						"Externally controlled" } };

		String[][] q04 = {
				{ "1", "2", "3" },
				{ "Each instance has its own administration",
						"Single database is distributed to each instance",
						"Instances share a single database" } };

		String[][] q05 = {
				{ "1", "2", "3" },
				{ "Rights are assigned to individual accounts",
						"Combination of individual accounts and groups/roles",
						"All rights are assigned via a role/groups schema" } };

		String[][] q06 = {
				{ "LDAP", "SPML", "XML", "SCR", "BATCH", "OTHER", "NONE" },
				{ "LDAP Pull", "SPML", "XML", "Scriptable", "Batch Import",
						"Other (explain)", "None" } };

		String[][] q07 = {
				{ "NONE", "DB", "LOG", "XML", "FILE", "OTHER" },
				{ "None", "Store events to a database", "System Log", "XML",
						"Flat file", "Other (explain)" } };

		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"4.1 Security system used? "));
		ht.put("q01_cd",
				new WebFieldSelect("q01_cd", db.getText("q01_cd"), q01));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

		ht.put("q02_q", new WebFieldDisplay("q02_q",
				"4.2 If If web application, what level of Oblix is used? "));
		ht.put("q02_cd",
				new WebFieldSelect("q02_cd", db.getText("q02_cd"), q02));
		ht.put("q02_rmk", new WebFieldText("q02_rmk", db.getText("q02_rmk"), 3,
				40));

		ht.put("q03_q", new WebFieldDisplay("q03_q",
				"4.3 Account information strategy: "));
		ht.put("q03_cd",
				new WebFieldSelect("q03_cd", db.getText("q03_cd"), q03));
		ht.put("q03_rmk", new WebFieldText("q03_rmk", db.getText("q03_rmk"), 3,
				40));

		ht
				.put(
						"q04_q",
						new WebFieldDisplay(
								"q04_q",
								"4.4 If application runs on multiple servers, which best describes your account administration function?"));
		ht.put("q04_cd",
				new WebFieldSelect("q04_cd", db.getText("q04_cd"), q04));
		ht.put("q04_rmk", new WebFieldText("q04_rmk", db.getText("q04_rmk"), 3,
				40));

		ht.put("q05_q", new WebFieldDisplay("q05_q",
				"4.5 Assignment of rights is?"));
		ht.put("q05_cd",
				new WebFieldSelect("q05_cd", db.getText("q05_cd"), q05));
		ht.put("q05_rmk", new WebFieldText("q05_rmk", db.getText("q05_rmk"), 3,
				40));

		ht.put("q06_q", new WebFieldDisplay("q06_q",
				"4.6 API For Security Maintenance?"));
		ht.put("q06_cd",
				new WebFieldSelect("q06_cd", db.getText("q06_cd"), q06));
		ht.put("q06_rmk", new WebFieldText("q06_rmk", db.getText("q06_rmk"), 3,
				40));

		ht.put("q07_q", new WebFieldDisplay("q07_q",
				"4.7 API For Security Maintenance?"));
		ht.put("q07_cd",
				new WebFieldSelect("q07_cd", db.getText("q07_cd"), q07));
		ht.put("q07_rmk", new WebFieldText("q07_rmk", db.getText("q07_rmk"), 3,
				40));

	}

	private void add_Section_05(Hashtable ht) {
		String[][] q01 = {
				{ "1", "2", "3", "4", "5" },
				{ "Does not use Unix or Windows", "Unix compliant",
						"Windows compliant", "Unix - Does not compley",
						"Windows - does not comply" } };

		String[][] q02 = {
				{ "NA", "Y", "N" },
				{ "Not Unix application", "Unix - configured for SSH",
						"Unix - not configured for SSH" } };

		String[][] q03 = {
				{ "NA", "Y", "N" },
				{ "System does not use.", "System complies.",
						"System does not comply." } };

		/*
		 * Section 5 - Question 1
		 */

		ht
				.put(
						"q01_q",
						new WebFieldDisplay(
								"q01_q",
								"5.1 If your system uses Windows or UNIX operating systems, does it comply with the Operating Systems Configurations for UNIX and Windows Systems SOP? "));
		ht.put("q01_cd",
				new WebFieldSelect("q01_cd", db.getText("q01_cd"), q01));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 2
		 */

		ht
				.put(
						"q02_q",
						new WebFieldDisplay(
								"q02_q",
								"5.2 If your system runs on UNIX operating systems, is SSH configured according to the OpenSSH Security Configuration SOP "));
		ht.put("q02_cd",
				new WebFieldSelect("q02_cd", db.getText("q02_cd"), q02));
		ht.put("q02_rmk", new WebFieldText("q02_rmk", db.getText("q02_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 3
		 */

		ht
				.put(
						"q03_q",
						new WebFieldDisplay(
								"q03_q",
								"5.3 If your system uses Citrix, does it comply with the Citrix Metaframe Security Specifications SOP?"));
		ht.put("q03_cd",
				new WebFieldSelect("q03_cd", db.getText("q03_cd"), q03));
		ht.put("q03_rmk", new WebFieldText("q03_rmk", db.getText("q03_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 4
		 */
		ht
				.put(
						"q04_q",
						new WebFieldDisplay(
								"q04_q",
								"5.4 If your system uses terminal services, does it comply with the Terminal Services Security SOP?"));
		ht.put("q04_cd",
				new WebFieldSelect("q04_cd", db.getText("q04_cd"), q03));
		ht.put("q04_rmk", new WebFieldText("q04_rmk", db.getText("q04_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 5
		 */

		ht
				.put(
						"q05_q",
						new WebFieldDisplay(
								"q05_q",
								"5.5 If your system requires connectivity to any outside partner systems or data, does it comply with the External Connections SOP?"));
		ht.put("q05_cd",
				new WebFieldSelect("q05_cd", db.getText("q05_cd"), q03));
		ht.put("q05_rmk", new WebFieldText("q05_rmk", db.getText("q05_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 6
		 */

		ht
				.put(
						"q06_q",
						new WebFieldDisplay(
								"q06_q",
								"5.6 If your system is running on Wireless networks, does it comply with the Wireless LAN Security Specifications SOP?"));
		ht.put("q06_cd",
				new WebFieldSelect("q06_cd", db.getText("q06_cd"), q03));
		ht.put("q06_rmk", new WebFieldText("q06_rmk", db.getText("q06_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 7
		 */

		ht
				.put(
						"q07_q",
						new WebFieldDisplay(
								"q07_q",
								"5.7 If your system uses remote control functions, does it comply with the Remote Control Security Requirements SOP?"));
		ht.put("q07_cd",
				new WebFieldSelect("q07_cd", db.getText("q07_cd"), q03));
		ht.put("q07_rmk", new WebFieldText("q07_rmk", db.getText("q07_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 8
		 */

		ht
				.put(
						"q08_q",
						new WebFieldDisplay(
								"q08_q",
								"5.8 If your system uses IBM HTTP Server, does it comply with the IBM HTTP Server Security Specifications SOP?"));
		ht.put("q08_cd",
				new WebFieldSelect("q08_cd", db.getText("q08_cd"), q03));
		ht.put("q08_rmk", new WebFieldText("q08_rmk", db.getText("q08_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 9
		 */

		ht
				.put(
						"q09_q",
						new WebFieldDisplay(
								"q09_q",
								"5.9 If your system uses Microsoft IIS and is only accessible from the Intranet, "
										+ " does it comply with the Internal Use of MS IIS Servers SOP?"));
		ht.put("q09_cd",
				new WebFieldSelect("q09_cd", db.getText("q09_cd"), q03));
		ht.put("q09_rmk", new WebFieldText("q09_rmk", db.getText("q09_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 10
		 */
		ht
				.put(
						"q10_q",
						new WebFieldDisplay(
								"q10_q",
								"5.10 If your system uses Microsoft IIS and is accessible from the Internet, "
										+ " does it comply with the Public Use of MS IIS Servers SOP?"));
		ht.put("q10_cd",
				new WebFieldSelect("q10_cd", db.getText("q10_cd"), q03));
		ht.put("q10_rmk", new WebFieldText("q10_rmk", db.getText("q10_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 11
		 */

		ht
				.put(
						"q11_q",
						new WebFieldDisplay(
								"q11_q",
								"5.11 If your system uses Microsoft Message Queuing (MSMQ), "
										+ " does it comply with the MSMQ Security Configuration?"));
		ht.put("q11_cd",
				new WebFieldSelect("q11_cd", db.getText("q11_cd"), q03));
		ht.put("q11_rmk", new WebFieldText("q11_rmk", db.getText("q11_rmk"), 3,
				40));

		/*
		 * Section 5 - Question 12
		 */

		ht
				.put(
						"q12_q",
						new WebFieldDisplay(
								"q12_q",
								"5.12 If your system is integrated with Oblix, "
										+ " does it comply with the Oblix Integration with Web-Based Applications SOP?"));
		ht.put("q12_cd",
				new WebFieldSelect("q12_cd", db.getText("q12_cd"), q03));
		ht.put("q12_rmk", new WebFieldText("q12_rmk", db.getText("q12_rmk"), 3,
				40));

	}

	private void add_Section_06(Hashtable ht) {

		String[][] q01 = { { "N", "L", "E" },
				{ "New", "Legacy", "Undergoing major enhancements." } };

		String[][] q03 = {
				{ "NA", "Y", "N" },
				{ "Not ASP", "ASP has undergone the waiver process.",
						"ASP has not undergone the waiver process." } };

		String[][] q05 = {
				{ "1", "2", "3" },
				{ "Not documented.", "Documented > 12 months ago.",
						"Documented < 12 months ago. " } };

		/*
		 * Section 6- Question 1
		 */

		ht
				.put(
						"q01_q",
						new WebFieldDisplay(
								"q01_q",
								"6.1 If application is in production, is it traked "
										+ " in the Corporate application inventory? If yes, give identification id."));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

		/*
		 * Section 6- Question 2
		 */
		ht.put("q02_q", new WebFieldDisplay("q02_q",
				"6.2 Does this system/application" + "/project <br> "
						+ "involve an external Application "
						+ "Service Provider (ASP)? If go, "
						+ "give vendor details."));
		ht.put("q02_cd", new WebFieldSelect("q02_cd", db.getText("q02_cd"), sm
				.getCodes("YESNO")));
		ht.put("q02_rmk", new WebFieldText("q02_rmk", db.getText("q02_rmk"), 3,
				40));

		/*
		 * Section 6- Question 3
		 */
		ht.put("q03_q", new WebFieldDisplay("q03_q",
				"6.3 If system/application/project "
						+ " involves an external Application "
						+ "Service Provider (ASP), then has this "
						+ "ASP undergone the waiver submission process? "
						+ "<br>If yes, supply waiver details."));
		ht.put("q03_cd",
				new WebFieldSelect("q03_cd", db.getText("q03_cd"), q03));
		ht.put("q03_rmk", new WebFieldText("q03_rmk", db.getText("q03_rmk"), 3,
				40));

		/*
		 * Section 6- Question 4
		 */

		ht.put("q04_q", new WebFieldDisplay("q04_q",
				"6.4 Does the application use the standard corporate process framework?"
						+ "<br>If not, explain."));
		ht.put("q04_cd", new WebFieldSelect("q04_cd", db.getText("q04_cd"), sm
				.getCodes("YESNO")));
		ht.put("q04_rmk", new WebFieldText("q04_rmk", db.getText("q04_rmk"), 3,
				40));

		/*
		 * Section 6- Question 5
		 */

		ht.put("q05_q", new WebFieldDisplay("q05_q",
				"6.5 Are security administration procedures and processes "
						+ "<br> for this system/application documented?"));
		ht.put("q05_cd",
				new WebFieldSelect("q05_cd", db.getText("q05_cd"), q05));
		ht.put("q05_rmk", new WebFieldText("q05_rmk", db.getText("q05_rmk"), 3,
				40));

	}

	private void add_Section_07(Hashtable ht) {
		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"7.1 To do - plug in your own security questions !!"));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

	}

	private void add_Section_08(Hashtable ht) {
		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"8.1 To do - plug in your own security questions !!"));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

	}

	private void add_Section_09(Hashtable ht) {
		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"9.1 To do - plug in your own security questions !!"));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

	}

	private void add_Section_10(Hashtable ht) {
		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"10.1 To do - plug in your own security questions !!"));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

	}

	private void add_Section_11(Hashtable ht) {
		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"11.1 To do - plug in your own security questions !!"));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

	}

	private void add_Section_12(Hashtable ht) {
		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"12.1 To do - plug in your own security questions !!"));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

	}

	private void add_Section_13(Hashtable ht) {
		ht.put("q01_q", new WebFieldDisplay("q01_q",
				"13.1 To do - plug in your own security questions !!"));
		ht.put("q01_cd", new WebFieldSelect("q01_cd", db.getText("q01_cd"), sm
				.getCodes("YESNO")));
		ht.put("q01_rmk", new WebFieldText("q01_rmk", db.getText("q01_rmk"), 3,
				40));

	}

}
