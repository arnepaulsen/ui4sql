/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
/**
 *
 * 
 * 
 * @author PAULSEAR
 *
 * Change Log :
 * 
 * 	1/15/10 - get/set for user_suite_cd
 * 
 *  1/12/10 - Save contactHT to the servletContext..BIG  / buggy .., backed out
 *  
 *  3/6 add parmExists, getApplicationId, setApplicationId
 *  5/17/05 added getRequestParmNames (enumeration) for Plugin to fetch
 *  8/23/05 cacheUserHT now puts lastname,first_name
 *  5/6/06 correct spelling of 'successful'.
 *  5/17/06 test 'is null' on role_cd in select for setProjectId
 *  5/24/06 add 'setRequirementId' for testcase detail form
 *  6/26/06 add get and cache
 *  
 *  8/11/06 pass the dbProduct to the dbInterface constructor
 *  8/15/06 add getProduct() function to return the database type
 *  		- MySQL = ifnull, SQL Server = isnull
 *  
 *  9/7/06 - add ifnull logic in getApplicationId, watch for element name qualifer in setApplication
 *  9/9/06 - add get/set for Ra
 *  9/13/06 - application filter show all, regardless of permissions
 *   9/18/06 - add Executive and testor table
 *    10/11/06 - getHomePage(), abstract sm.Parm("Target") to sm.getTarget()
 *    10/12/06 ProjectFilter - show all projects for a division
 *    	invalidate session on logout
 *  12/12/06 add method to return enumeration of Request ParameterNames.. to put Filters on ShowPage
 *  1/25/07 add getUserType (from tuser.user_type_cd) and isLeader
 *  
 *  8/22/07 drop ApplicationStartListener, which was just moving vars from cts.getInitParameter
 *  
 *  2/6/08 - add get/set for ProjectPlanId
 *  
 *  2/25/08 - handle password reset form
 *  
 *  5/14/08 - handle MySQL and SQLServer 'concat' functions via getServerConcatFunction
 *  
 *  5/19 - add getDefaultHomePage(), instead of hard-code in tuser plugin
 *  
 *  9/9 - add  get/set OrderSetId/Name and get/set ServiceRequestId/Name
 *    
 *    9/27 - get/set pat_sfty_cd
 *    10/8 - get/set issue_id
 *    
 *   1/10/09 - add ParmArray to return multi-select and radio arrays
 *   
 *   3/14/09 - add userIsSox 
 *   
 *   8/17/09 - use vcodes for sm.getCodes !!!
 *   
 *   2/4/10 - add WEB-ROOT to web.xml file
 *   
 *   7/20/10 - Remove obsolete db-supports-subqueries
 *   
 *   9/27/10 - Add getSubCodes(CodeSet, SubCode)
 *   
 *   10/20/10 - Add method clearTable(String table) to null out a table
 *   
 *   10/25/10 - add get/setScratch to save off any string
 *   
 *   11/1/10 - add new methods to fetch SR_Tracker security Level
 *   
 *   11/10/10 - don't send password email to user with invalid email address
 *   
 *   2/4/11 - fix increment of user login count, needs to make sure key has some qualification
 *   
 *   2/9/11 - add getCodesActive to reutrn only active codes
 *   
 *       
 *   
 *   
 *    
 */

package router;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletContext;

// import org.apache.log4j.Logger;

import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;

import db.DbInterface;
import java.sql.ResultSet;
import db.DbConnectionFactory;
import plugins.UserPlugin;

import services.BridgesMail;

public class SessionMgr {

	private String concat = "concat"; // "concat"

	private boolean debug = true;

	public boolean userFound = false;

	private HttpServletRequest req;

	private HttpServletResponse resp;

	private ServletContext servletContext;

	private HttpSession session = null;

	private java.util.Properties requestProperties;

	private Connection connection;

	private DbInterface db;

	public String loginMessage = "";

	private String setrowcount = "SET ROWCOUNT 1 "; // " SET ROWCOUNT 1 "
	private String limit_one = " LIMIT 1 ";
	private String now = "now()";
	private String getdate = "getdate()"; // sqlserver = getDate() /mysql =

	// now();

	// *****************************
	// user variables
	// *****************************

	public Connection getConnection() {
		return connection;
	}


	public boolean isConnected() {

		try {

			if (connection == null) {
				return false;
			}

			return !connection.isClosed();
		} catch (Exception e) {
			return false;
		}
	}

	public String getWebRoot() {
		return "";
	}
	/*
	 * these two safety gets are so each fetch function doesn't have to repeat
	 * the saem try/catch logic
	 */

	private Hashtable setServletVar(String name, Hashtable ht) {
		if (ht == null) {
			servletContext.setAttribute("pmo.tables." + name.toLowerCase(),
					new Hashtable());
		} else
			servletContext.setAttribute("pmo.tables." + name.toLowerCase(), ht);

		return ht;
	}

	private Hashtable setSessionVar(String name, Hashtable ht) {
		if (ht == null) {
			session.setAttribute("pmo.tables." + name.toLowerCase(),
					new Hashtable());
		} else
			session.setAttribute("pmo.tables." + name.toLowerCase(), ht);

		return ht;
	}

	private void setSessionVar(String name, Integer value) {
		if (value == null) {
			session.setAttribute("pmo.int." + name.toLowerCase(), new Integer(
					"0"));
		} else
			session.setAttribute("pmo.int." + name.toLowerCase(), value);

	}

	private void setSessionVar(String name, String value) {
		if (value == null) {
			session.setAttribute("pmo.var." + name.toLowerCase(),
					new String(""));
		} else
			session.setAttribute("pmo.var." + name.toLowerCase(), value);
	}

	private Hashtable getServletHT(String name) {

		try {
			if (servletContext.getAttribute("pmo.tables." + name.toLowerCase()) != null) {
				return (Hashtable) servletContext.getAttribute("pmo.tables."
						+ name.toLowerCase());
			}
		} catch (Exception e) {
			debug("SM safeGetSessionInteger: " + name);
			return new Hashtable();
		}
		// debug("getSafeInt - not found .. returning default");
		return new Hashtable();
	}

	private Hashtable getSessionHT(String name) {

		try {
			if (session.getAttribute("pmo.tables." + name.toLowerCase()) != null) {
				return (Hashtable) session.getAttribute("pmo.tables."
						+ name.toLowerCase());
			}
		} catch (Exception e) {
			debug("SM safeGetSessionInteger: " + name);
			return new Hashtable();
		}
		return new Hashtable();
	}

	private Integer getSessionInteger(String name) {

		try {
			if (session.getAttribute("pmo.int." + name.toLowerCase()) != null) {
				return (Integer) session.getAttribute("pmo.int."
						+ name.toLowerCase());
			}
		} catch (Exception e) {
			debug("SM safeGetSessionInteger: " + name);
			return new Integer("0");
		}
		return new Integer("0");
	}

	private String getSessionString(String name) {
		try {
			if (session.getAttribute("pmo.var." + name.toLowerCase()) != null) {
				return (String) session.getAttribute("pmo.var."
						+ name.toLowerCase());
			}
		} catch (Exception e) {
			debug("SM safeGetSessionSring : " + name);
			return new String("");
		}
		return new String("");
	}

	private String getSessionString(String name, String defaultVar) {
		try {
			if (session.getAttribute("pmo.var." + name.toLowerCase()) != null) {
				return (String) session.getAttribute("pmo.var."
						+ name.toLowerCase());
			}
		} catch (Exception e) {
			debug("SM safeGetSessionSring : " + name);
			return new String(defaultVar);
		}
		return new String(defaultVar);
	}

	/* Restore existing session */
	public SessionMgr(HttpServletRequest parmReq, HttpServletResponse parmResp,
			ServletContext pServletContext) {

		/* get session, create new one if not exists */

		req = parmReq;
		resp = parmResp;
		servletContext = pServletContext;

		try {

			session = req.getSession(true);

			if (session.isNew()) {
				if (debug)
					System.out
							.println("SessionMgr: Starting new session, loading properties from lib.");
				// getSystemProperties(); ... moved to the ServletContext
				setSessionVar("userLoggedIn", new String("N"));
			}

			/*
			 * get Database connection using connection properties alredy saved
			 * into session todo: is it safe to save the db password in the
			 * session , where else to put them?
			 */
			connection = openConnection();

			// db = new DbInterface(connection, (String) servletContext
			// .getAttribute("prop_database_product"));

			db = new DbInterface(connection,
					(String) servletContext.getInitParameter("DB-Product"));

			/*
			 * always save HTML FORM variables to requestProperties
			 */

			requestProperties = unloadrequestProperties(parmReq);

		} catch (Exception e) {

		}

		return;
	}

	public String getTemplatesPath() {
		return (String) getServletContext().getInitParameter("Templates-path");
	}


	// get a Hashtable of the projects the current user is permitted to
	public Hashtable cacheAncillaryList() {

		// admins see all applications for a division
		try {
			String query = " select application_name as odor, tapplications.application_id, application_name "

					+ " from tapplications  "
					+ " where division_id <>  "
					+ getUserDivision();

			Hashtable ht = db.getLookupTable(query);

			setServletVar("AncillaryList", ht);
			return ht;
		} catch (Exception e) {
			return new Hashtable();
		}
	}

	// get a Hashtable of the projects the current user is permitted to
	public void cacheApplicationFilter() throws services.ServicesException {

		Integer userid = (Integer) getSessionInteger("userId");

		String query;

		if (userIsAdministrator()) {

			// admins see all applications for a division
			query = " select application_name as odor, tapplications.application_id, application_name "
					+ " from tapplications  "
					+ " where division_id =  "
					+ getUserDivision();

		} else {
			// other users only see permitted applications
			query = " select application_name as odor, tapplications.application_id, application_name "
					+ " from tapplications  "
					+ " join tuser_application on tapplications.application_id = tuser_application.application_id "
					+ " where tuser_application.user_id = "
					+ userid.toString()
					+ " and tapplications.division_id =  " + getUserDivision();
		}
		Hashtable ht = db.getLookupTable(query);

		setSessionVar("ApplicationFilter", ht);
		return;
	}

	public Hashtable cacheCodes(String codeType) {
		// get the codes and save in session.

		String query = " select * from vcodes " + " where code_type = '"
				+ codeType + "'  ORDER BY odor ";

		Hashtable ht = new Hashtable();

		try {
			ht = db.getLookupTable(query);
			servletContext.setAttribute("pmo.codes." + codeType.toLowerCase(),
					ht);

		} catch (services.ServicesException e) {
			debug("Error caching table to servletContext" + e.toString());
		}

		return ht;
	}

	/*
	 * Same as CacheCodes, except uses the alternate 'code_desc2' of the table
	 * values
	 */
	public Hashtable cacheCodesAlt(String codeType) {
		// get the codes and save in session.

		String query = " select order_by as odor, code_value, code_desc2 from tcodes  "
				+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
				+ " where tcodes.code_type_id = tcode_types.code_type_id  and  tcode_types.code_type = '"
				+ codeType + "'  ORDER BY order_by ";

		Hashtable ht = new Hashtable();

		try {
			ht = db.getLookupTable(query);
			servletContext.setAttribute("pmo.codes." + codeType.toLowerCase()
					+ ".alt", ht);
		} catch (services.ServicesException e) {
			debug("Error cache table to servletContext" + e.toString());
		}

		return ht;
	}
	
	/*
	 * Same as CacheCodes, except uses the alternate 'code_desc2' of the table
	 * values
	 */
	public Hashtable cacheCodesActive(String codeType) {
		// get the codes and save in session.

		String query = " select order_by as odor, code_value, code_desc from tcodes  "
				+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
				+ " where tcodes.code_type_id = tcode_types.code_type_id  and  tcode_types.code_type = '"
				+ codeType + "' AND tcodes.active_flag = 'Y' ORDER BY order_by ";

		Hashtable ht = new Hashtable();

		try {
			ht = db.getLookupTable(query);
			servletContext.setAttribute("pmo.codes." + codeType.toLowerCase()
					+ ".active", ht);
		} catch (services.ServicesException e) {
			debug("Error cache table to servletContext" + e.toString());
		}

		return ht;
	}
	

	/*
	 * 9/27/10 New method Same as CacheCodes, except uses the 2nd parameter to
	 * limit code set by the tcodes.Desc2 field
	 */
	public Hashtable cacheSubCodes(String codeType, String subCode) {
		// get the codes and save in session.

		String query = " select order_by as odor, code_value, code_desc from tcodes  "
				+ " join tcode_types on tcodes.code_type_id = tcode_types.code_type_id "
				+ " where tcodes.code_type_id = tcode_types.code_type_id  and  tcode_types.code_type = '"
				+ codeType
				+ "'  AND tcodes.code_desc2 = '"
				+ subCode
				+ "' ORDER BY order_by ";

		Hashtable ht = new Hashtable();

		try {
			ht = db.getLookupTable(query);
			servletContext.setAttribute("pmo.codes." + codeType.toLowerCase()
					+ subCode.toLowerCase(), ht);
		} catch (services.ServicesException e) {
			debug("Error cache table to servletContext" + e.toString());
		}
		return ht;
	}

	// get a Hashtable of the projects the current user is permitted to
	public Hashtable cacheCodeTypeFilter() {

		try {
			String query = " select title_nm as odor, tcode_types.code_type_id, title_nm "
					+ " from tcode_types  ";

			Hashtable ht = db.getLookupTable(query);

			setServletVar("code_types", ht);

			return ht;
		} catch (Exception e) {
			return new Hashtable();
		}
	}

	/*
	 * Get User Record and save to session. User handle and pw must be in the
	 * HTTP request
	 */

	// get a Hashtable of the projects the current user is permitted to
	public void cacheCodeTypeNames() throws services.ServicesException {

		String query = " select title_nm as odor, code_type, title_nm "
				+ " from tcode_types  ";

		Hashtable ht = db.getLookupTable(query);

		servletContext.setAttribute("pmo.tables.code_type_names", ht);

		return;

	}

	// todo.. set up system-wide default home page.
	public String getDefaultHomePage() {
		return "Rfc";
	}

	public void setSystemMessage(String message) {
		servletContext.setAttribute("pmo.system.message", message);
	}

	public String getSystemMessage() {

		try {

			if (servletContext.getAttribute("pmo.system.message") != null) {
				return (String) servletContext
						.getAttribute("pmo.system.message");
			}

			return (String) servletContext.getInitParameter("System-Message");

		} catch (Exception e) {
			debug("WEB-XML System-Message not found.");

			return "";
		}

	}

	public Hashtable cacheContactHT() {

		if (servletContext.getAttribute("pmo.tables.contacts") != null) {
			return (Hashtable) servletContext
					.getAttribute("pmo.tables.contacts");
		}

		Hashtable ht = new Hashtable();
		try {
			ht = db.getLookupTable("select "
					+ concat
					+ "(last_name, ',',first_name) as odor, cast(contact_id "
					+ " as signed "
					+ ") as contact_id, "
					+ concat
					+ "(last_name, ',',first_name) as contact_name from tcontact "
					+ " where contact_type_cd != 'VND' and last_name != '' and substring(last_name,1,1) != '('");

			// ms sql server should be "as integer" instead of "as signed"

			servletContext.setAttribute("pmo.tables.contacts", ht);
			return ht;

		} catch (services.ServicesException e) {
			debug("SessionMgr:cacheContactHT: " + e.toString());
		}
		return new Hashtable();
	}

	public Hashtable cacheVendorHT() {
		Hashtable ht = new Hashtable();

		try {
			ht = db.getLookupTable("select "
					+ concat
					+ "(last_name, ',',first_name) as odor, cast(contact_id "
					+ " as signed "
					+ " ) as contact_id, "
					+ concat
					+ "(last_name, ',',first_name) as contact_name from tcontact where contact_type_cd = 'VND' ");

			setServletVar("vendors", ht);
		} catch (services.ServicesException e) {
		}
		return ht;
	}

	private boolean servletHtExists(String table) {
		try {
			if (servletContext.getAttribute("pmo.tables." + table) != null)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	public Hashtable getLeaderHT() {

		if (servletHtExists("leaders"))
			return getServletHT("leaders");
		else
			return cacheLeaderHT();
	}

	private Hashtable cacheLeaderHT() {

		try {

			Hashtable ht = db
					.getLookupTable("select "
							+ concat
							+ "(last_name, ',',first_name) as odor, cast(user_id "
							+ " as signed "
							+ " ) as user_id, "
							+ concat
							+ "(last_name, ',',first_name) as user_name from tuser where leader_flag = 'Y' ");

			setServletVar("leaders", ht);
			return ht;
		} catch (services.ServicesException e) {
			return new Hashtable();
		}
	}

	/*
	 * Cache important stuff
	 */

	// get a Hashtable of the projects the current user is permitted to
	public Hashtable cacheDivisionFilter() {

		try {
			String query = " select div_name as odor, tdivision.division_id, div_name "
					+ " from tdivision  ";

			Hashtable ht = db.getLookupTable(query);

			setServletVar("DivisionFilter", ht);
			return ht;
		} catch (Exception e) {
			return new Hashtable();
		}
	}

	// *****************
	// GET BEANS
	// *****************

	// used by managers that have Steps and Elements as children

	public void cacheFormFilter() throws services.ServicesException {

		if (servletContext.getAttribute("pmo.tables.form_filter") != null) {
			return;
		}

		String formQuery = " select tform.table_nm as odor, form_id, form_nm, data_type_cd, table_nm, key_nm, active_flag from tform ";

		Hashtable formFilter = db.getLookupTable(6, formQuery);

		servletContext.setAttribute("pmo.tables.form_filter", formFilter);
		return;

	}

	// get a Hashtable of the projects the current user is permitted to
	public void cacheProjectFilter() throws services.ServicesException {

		Integer userid = (Integer) getSessionInteger("userId");

		String projectQuery;

		if (userIsAdministrator()) {
			// admins can see all projects, regardless of permissions
			projectQuery = " select tproject.project_id as odor, tproject.project_id, project_name "
					+ " from tproject  "
					+ " where tproject.division_id = "
					+ getDivisionId().toString();

		} else {
			// other users must have permissions defined in table tproject_user
			// projectQuery = " select tproject.project_id as odor,
			// tproject.project_id, project_name "
			// + " from tproject "
			// + " join tuser_project on tproject.project_id =
			// tuser_project.project_id "
			// + " where tuser_project.user_id = "
			// + userid.toString()
			// + " and tproject.division_id = "
			// + getDivisionId().toString();

			projectQuery = " select tproject.project_id as odor, tproject.project_id, project_name "
					+ " from tproject  "
					+ " where tproject.division_id = "
					+ getDivisionId().toString();
		}

		Hashtable projectFilter = db.getLookupTable(projectQuery);

		setSessionVar("ProjectFilter", projectFilter);
		return;

	}

	public int getServerPort() {

		try {
			return req.getServerPort();
		} catch (Exception e) {
			debug("sessionMgr:getPort() error fetching port");
			return 0;
		}
	}

	public String getHost() {

		String host = "";
		try {
			host = req.getServerName();
			if (host == null)
				return "";
			return host;
		} catch (Exception e) {
			debug("sessionMgr:getPort() error fetching port");
			return "";
		}
	}

	public String getTomcatName() {
		try {
			String tomcat = "";
			tomcat = (String) servletContext.getInitParameter("tomcat-name");
			if (tomcat == null) {
				return "pmo";
			}
			return tomcat;
		} catch (Exception e) {
			debug("sm:getTomcatName()-error getting tomcat-name param ");
			debug(e.toString());
			return "pmo";
		}
	}

	public String sendPassword() {

		String mailHost = "mailhub.arnepaulsenjr.com"; // mail.smtp.host";

		String message = "Your PMO Tool password for NUID '" + getHandle()
				+ "' is : " + getPassword();

		String[] addressList = new String[] { getEmailAddress() };

		// 11/9/10 a.paulsen - don't try to send mail to an invalid address

		if (addressList[0].indexOf("@") > 0) {

			BridgesMail mailer = new BridgesMail();

			mailer.sendMessage(mailHost, addressList, "arne@arnepaulsenjr.com",
					"PMO Tool Password", message);

			loginMessage = "Password sent to " + getEmailAddress();

		} else {

			loginMessage = "No valid email address for user '" + getHandle()
					+ "'.";
		}

		return "";

	}

	private Hashtable queryLookupTable(String query) {
		Hashtable ht = new Hashtable();

		String rationalized = query.replaceFirst("!APPLICATIONID!",
				getApplicationId().toString());

		try {
			ht = db.getLookupTable(rationalized);
		} catch (services.ServicesException e) {
			debug("sessionManager:queryLookup : " + query + e.toString());
		}

		return ht;

	}

	/*
	 * build the sevlet hashtables if not already done.
	 */
	public void cacheServletFilters() {

		String cached = new String("");
		try {
			if (servletContext.getAttribute("pmo.cache") != null) {
				cached = (String) servletContext.getAttribute("pmo.cache");

				if (cached.equalsIgnoreCase("Y")) {
					return;
				}
			}
		} catch (Exception e) {
			debug("error fetching servlet cache flag - " + e.toString());

		}

		servletContext.setAttribute("pmo.cache", new String("Y"));

		try {
			cacheCodeTypeNames();
			cacheUserHT();
			cacheContactHT();
			cacheFormFilter();

		} catch (Exception e) {
			debug("cacheServletFilters: " + e.toString());
		}

	}

	public void cacheUserFilters() {

		try {
			setProjectId((Integer) getSessionInteger("ProjectId"));
			cacheProjectFilter();
			cacheApplicationFilter();
			cacheDivisionFilter();

		} catch (Exception e) {
			e.printStackTrace();
			loginMessage = "Error caching user tables. ";
			debug("SessionMgr:Constructor - error pushing attributes : "
					+ e.toString());
		}

	}

	public Hashtable cacheUserHT() {

		if (servletContext.getAttribute("pmo.tables.users") != null) {

			return (Hashtable) servletContext.getAttribute("pmo.tables.users");
		}

		Hashtable ht = new Hashtable();
		try {
			ht = db.getLookupTable("select " + concat
					+ "(last_name, ',',first_name) as odor, user_id, " + concat
					+ "(last_name, ',',first_name) as user_name from tuser");

			servletContext.setAttribute("pmo.tables.users", ht);
		} catch (services.ServicesException e) {
		}
		return ht;
	}

	public Hashtable cacheDomainContacts() {

		if (servletContext.getAttribute("pmo.tables.domain_Contacts") != null) {
			return (Hashtable) servletContext
					.getAttribute("pmo.tables.domain_contacts");
		}

		Hashtable ht = new Hashtable();
		try {
			ht = db.getLookupTable("select "
					+ concat
					+ "(last_nm, ',',first_nm) as odor, hc_contact_id, "
					+ concat
					+ "(last_nm, ',',first_nm) as contact_name from thc_contact");

			servletContext.setAttribute("pmo.tables.domain_contacts", ht);
		} catch (services.ServicesException e) {
			debug("cacheDomainContacts : " + e.toString());
		}
		return ht;
	}

	public Hashtable cacheTestorHT() {

		Hashtable ht = new Hashtable();
		try {
			ht = db.getLookupTable("select "
					+ concat
					+ "(last_name, ',',first_name) as odor, user_id, "
					+ concat
					+ "(last_name, ',',first_name) as user_name from tuser where testor_flag = 'Y' ");

			servletContext.setAttribute("pmo.tables.testors", ht);
		} catch (services.ServicesException e) {
			return new Hashtable();
		}
		return ht;
	}

	public void closeConnection() {

		try {
			connection.close();
		} catch (SQLException se) {
		}

	}

	public Enumeration getParmeterNames() {
		return req.getParameterNames();
	}

	public String getStarTeamUserid() {
		return (String) servletContext.getInitParameter("STARTEAM-USERID");
	}

	public String getStarTeamPassword() {
		return (String) servletContext.getInitParameter("STARTEAM-PASSWORD");
	}

	public String getRemedyUserid() {
		return (String) servletContext.getInitParameter("REMEDY-USERID");
	}

	public String getRemedyPassword() {
		return (String) servletContext.getInitParameter("REMEDY-PASSWORD");
	}


	public String getRemedyURL() {

		
		return (String) servletContext.getInitParameter("REMEDY-URL");

	}

	public String getExcelPath() {

		String path = (String) servletContext.getInitParameter("EXCEL-PATH");

		return path;
	}

	public void debug(String debugMsg) {
		// Logger.getLogger("ui4sql").debug("SessionMgr: " + debugMsg);

		if (debug)
			System.out.println(debugMsg);
	}

	public Hashtable getAncillaryList() {

		if (servletHtExists("AncillaryList"))
			return getServletHT("AncillaryList");
		else
			return cacheAncillaryList();
	}

	public Hashtable getApplicationFilter() {
		return (Hashtable) getSessionHT("ApplicationFilter");
	}

	public Integer getApplicationId() {

		return getSessionInteger("ApplicationId");
	}

	public String getApplicationName() {
		return getSessionString("ApplicationName");
	}

	public String getApplicationRole() {
		return getSessionString("ApplicationRole");
	}

	public String getApplicationRoleName() {
		return getSessionString("ApplicationRoleName");
	}

	public String getAttribute(String attribute) {
		return getSessionString(attribute);
	}

	/*
	 * Returns the descriptor for a given code in a Hashtable .. couldn't quite
	 * figure out how to fetch it with the ht.get(theCode); result should be an
	 * Object[2], with the second value containing the desriptor
	 */
	public String getCodeDesc(Hashtable ht, String code) {

		debug("sm.getCodeDesc for : " + code);

		try {
			Object[] obj;
			Enumeration en = ht.keys();
			while (en.hasMoreElements()) {
				obj = (Object[]) ht.get(en.nextElement());

				String hashValue = (String) obj[0];

				if (hashValue.equalsIgnoreCase(code)) {
					// System.out.println(" anme" + (String) obj[1]);
					debug(" ..found it. " + (String) obj[1]);
					return (String) obj[1];
				}
			}
			debug(".. not found");
			return "";
		} catch (Exception e) {
			System.out.println("SessionManager:getCodeDesc: " + e.toString());
		}
		return "";
	}

	public Hashtable getActiveCodes(String codeType) {
		String qual_name = "pmo.codes." + codeType.toLowerCase() + ".active";

		try {
			if (servletContext.getAttribute(qual_name) != null) {
				return (Hashtable) servletContext.getAttribute(qual_name);
			} else {
				return cacheCodesActive(codeType);
			}
		} catch (Exception e) {
			debug("Exception session:getCodes() " + codeType + "  .."
					+ e.toString());
			return new Hashtable();
		}

	}

	public Hashtable getCodes(String codeType) {

		String qual_name = "pmo.codes." + codeType.toLowerCase();

		try {
			if (servletContext.getAttribute(qual_name) != null) {
				return (Hashtable) servletContext.getAttribute(qual_name);
			} else {
				return cacheCodes(codeType);
			}
		} catch (Exception e) {
			debug("Exception session:getCodes() " + codeType + "  .."
					+ e.toString());
			return new Hashtable();
		}
	}

	public Hashtable getCodesAlt(String codeType) {

		if (servletContext.getAttribute("pmo.codes." + codeType.toLowerCase()
				+ ".alt") != null) {
			return (Hashtable) servletContext.getAttribute("pmo.codes."
					+ codeType.toLowerCase() + ".alt");
		} else {
			return cacheCodesAlt(codeType);
		}
	}

	/*
	 * this bean returns only the codes that are of type 2nd parameter
	 */
	public Hashtable getSubCodes(String codeType, String subCode) {

		if (servletContext.getAttribute("pmo.codes." + subCode.toLowerCase()
				+ codeType.toLowerCase()) != null) {
			return (Hashtable) servletContext.getAttribute("pmo.codes."
					+ codeType.toLowerCase() + subCode.toLowerCase());
		} else {
			return cacheSubCodes(codeType, subCode);
		}
	}

	public Hashtable getCodeTypeFilter() {
		if (servletHtExists("code_types"))
			return getServletHT("code_types");
		else
			return cacheCodeTypeFilter();

	}

	// save anything off plugin desires - may be against purist principles!
	public void setScratch(String s) {

		session.setAttribute("pmo.user.scratch", s);
	}

	public String getScratch() {
		if (session.getAttribute("pmo.user.scratch") != null)
			return (String) session.getAttribute("pmo.user.scratch");

		else
			return "";
	}

	public Hashtable getContactHT() {

		try {
			if (servletContext.getAttribute("pmo.tables.contacts") != null) {
				return (Hashtable) servletContext
						.getAttribute("pmo.tables.contacts");
			}
		} catch (Exception e) {
			debug(" getContactHT - servletContextError " + e.toString());
		}

		return cacheContactHT();
	}

	public Hashtable getVendorHT() {

		if (servletHtExists("vendors"))
			return getServletHT("vendors");
		else
			return cacheVendorHT();
	}

	public String getDatabaseProduct() {
		// return (String) servletContext.getAttribute("prop_database_product");

		return (String) servletContext.getInitParameter("DB-Product");

	}

	public DbInterface getDbInterface() {
		return db;
	}

	public Hashtable getDivisionFilter() {
		if (servletHtExists("DivisionFilter"))
			return getServletHT("DivisionFilter");
		else
			return cacheDivisionFilter();
	}

	public Integer getDivisionId() {
		Integer div = getSessionInteger("divisionid");
		if (div.toString().equalsIgnoreCase("0"))
			return new Integer("1");
		else
			return div;
	}

	public String getDivisionName() {
		return getSessionString("DivisionName");
	}

	public String getFirstName() {
		return getSessionString("first_name");
	}

	public String getHomePage() {
		return getSessionString("home_page");
	}

	// new dynamic home page from user table
	public String getTarget() {
		if (Parm("Target").equalsIgnoreCase("guess")) {
			return getHomePage();
		} else
			return Parm("Target");
	}

	public Hashtable getFormFilter() {
		return (Hashtable) servletContext
				.getAttribute("pmo.tables.form_filter");
	}

	public Integer getFormId() {
		return getSessionInteger("FormId");
	}

	public String getId() {
		return session.getId();
	}

	public String getLastName() {
		return getSessionString("last_name");
	}

	public String getUserSuite() {

		return getSessionString("user_suite_cd", "HC");
	}

	public void setUserSuite(String suite_cd, String suite_name) {
		try {
			setSessionVar("user_suite_cd", suite_cd);
			setSessionVar("user_suite_nm", suite_name);
		} catch (Exception e) {

		}
	}

	public String getUserSuiteName() {

		return getSessionString("user_suite_nm", "HC/Default");

	}

	public String getUserType() {
		return getSessionString("user_type");
	}

	public String getPassword() {
		return getSessionString("password");
	}

	public boolean resetPassword(String newPassword) {

		String sql = isSQLServer() ? setrowcount : ""
				+ "UPDATE tuser set password_nm = '" + newPassword
				+ "' WHERE tuser.user_id = " + getUserId() + limit_one;

		try {
			db.runQuery(sql);
		} catch (services.ServicesException s) {

		}
		return true;

	}

	/***************************************************************************
	 * 
	 * public gets for all the filters
	 */

	public Hashtable getProjectFilter() {
		return getSessionHT("ProjectFilter");
	}

	public Integer getProjectId() {
		if (!getSessionInteger("ProjectId").toString().equals("0")) {
			return getSessionInteger("ProjectId");
		}

		return getSessionInteger("user_default_project_id");

	}

	public String getProjectMenu() {
		return "large";
	}

	public String getProjectName() {
		return getSessionString("ProjectName");
	}

	// ************************************
	// GET FORM ELEMENTS... 'PARMS'
	// ************************************

	public String getProjectRole() {
		return getSessionString("ProjectRole");
	}

	public String getProjectRoleName() {
		return getSessionString("ProjectRoleName");
	}

	public jakarta.servlet.http.HttpServletRequest getRequest() {
		return req;
	}

	// ************************************
	// TUSER related things
	// ************************************

	public String getRequestParm(String parm) {
		try {
			if (requestProperties.containsKey((Object) parm)) {
				// System.out.println("router:getParm:" + parm + " value: " +
				// requestProperties.getProperty(parm));
				return requestProperties.getProperty(parm);
			} else {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String[] getRequestParmArray(String parm) {

		String[] x;
		try {
			x = req.getParameterValues(parm);
			if (x == null) {
				return new String[] { "" };
			}
			if (x.length > 0) {
				return x;
			}
			return new String[] { "" };
		} catch (Exception e) {
			// e.printStackTrace();
			return new String[] { "" };
		}
	}

	public Enumeration getRequestParmNames() {
		return req.getParameterNames();
	}

	public Properties getRequestProperties() {
		return requestProperties;
	}

	public Integer getParentId() {
		return getSessionInteger("ParentId");
	}

	public String getParentName() {
		return getSessionString("ParentName");
	}

	public String getParentTable() {
		return getSessionString("ParentTable");
	}

	public String getParentKeyName() {
		return getSessionString("ParentKeyName");
	}

	public jakarta.servlet.http.HttpServletResponse getResponse() {
		return resp;
	}

	public jakarta.servlet.ServletContext getServletContext() {
		return servletContext;
	}

	public Integer getStageId() {
		return getSessionInteger("StageId");
	}

	public String getStageName() {
		return getSessionString("StageName");
	}

	public String getStepKind() {
		return getSessionString("StepKind");
	}

	public Integer getServiceRequestId() {
		return getSessionInteger("ServiceRequestId");
	}

	public String getServiceRequestName() {
		return getSessionString("ServiceRequestName");
	}

	public Integer getStructureId() {
		return getSessionInteger("StructureId");
	}

	public String getStructureName() {
		return getSessionString("StructureName");
	}

	public String getStructureType() {

		return getSessionString("StructureType", "F");

	}

	// *****************
	// SET BEANS
	// *****************

	// public void setFilterId(String filterName, Integer parmId) {
	// setSessionVar(filterName, parmId);
	// }

	// public void setFilterCode(String filterName, String parmCode) {
	// setSessionVar(filterName, parmCode);
	// }//

	/*
	 * setRfcBlock is used to block the 'new' button on rfc entry
	 */
	public void setRfcBlock(boolean blockOn) {
		servletContext.setAttribute("flags.rfc.block", blockOn ? "Y" : "N");
	}

	public void setRipBlock(boolean blockOn) {
		servletContext.setAttribute("flags.rip.block", blockOn ? "Y" : "N");
	}

	public void setAmbBlock(boolean blockOn) {
		servletContext.setAttribute("flags.amb.block", blockOn ? "Y" : "N");
	}

	public boolean getAmbBlock() {
		return getBlockValue("flags.amb.block");
	}

	public boolean getRfcBlock() {
		return getBlockValue("flags.rfc.block");
	}

	public boolean getRipBlock() {
		return getBlockValue("flags.rip.block");
	}

	private boolean getBlockValue(String suite) {
		try {

			// this.setAddOk(!sm.getRfcBlock() || sm.userIsChgApprover());

			// `SimpleDateFormat hhmm = new SimpleDateFormat("hh/mm");
			// Date start = new hhmm.par

			// D/ate today = new Date();

			String blocked = (String) servletContext.getAttribute(suite);
			if (blocked.equalsIgnoreCase("Y"))
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	public String getStyle() {
		return getSessionString("user_style_nm") + ".css";
	}

	/*
	 * getCodeDescription
	 * 
	 * this is a reverse look-up from a code back to the description
	 * 
	 * it has to iterate through the table, because the HT key is the sort
	 * order, and is meaningless
	 * 
	 * the stored value is an 2-dim array, first position has the code, second
	 * has the description
	 */

	public String getCodeDescription(Hashtable ht, String code) {

		if (ht.isEmpty()) {
			return code;
		}

		try {
			SortedSet set = Collections.synchronizedSortedSet(new TreeSet(ht
					.keySet()));
			Iterator it = set.iterator();

			Object[] obj;

			while (it.hasNext()) {
				// LOGIC FOR AN INTEGER ARGUEMENT
				Object hashKey = (Object) it.next();
				obj = (Object[]) ht.get(hashKey);

				if (obj[0].equals((String) code)) {
					String x = (String) obj[1];
					return x;
				}
			}
			return code;

		} catch (Exception e) {
			debug("Exception on reverse lookup : " + e.toString());
			return code;
		}

	}

	/*
	 * fetchTable = getTable, but forces re-query
	 */
	public Hashtable getServletTable(String tableName, String query) {

		if (servletContext.getAttribute("pmo.tables." + tableName) != null)
			return getServletHT(tableName);
		else
			return setServletVar(tableName, queryLookupTable(query));

	}

	public Hashtable refreshTable(String tableName, String query) {
		return setSessionVar(tableName, queryLookupTable(query));

	}

	public Hashtable getTable(String tableName, String query) {

		if (session.getAttribute("pmo.tables." + tableName) != null)
			return getSessionHT(tableName);
		else
			return setSessionVar(tableName, queryLookupTable(query));

	}

	public Hashtable getDomainContacts() {

		if (servletContext.getAttribute("pmo.tables.domain_contacts") != null) {
			return (Hashtable) servletContext
					.getAttribute("pmo.tables.domain_contacts");
		} else {
			return cacheDomainContacts();
		}
	}

	public String getUsageName() {
		return getSessionString("UsageName");
	}

	public Integer getUserDivision() {
		return getSessionInteger("user_division_id");
	}

	public Hashtable getHT(String hashtable) {
		return getSessionHT(hashtable);
	}

	public void clearTable(String table) {

		servletContext.setAttribute("pmo.tables." + table, null);

	}

	public Hashtable getUserHT() {

		if (servletContext.getAttribute("pmo.tables.users") != null) {

			return (Hashtable) servletContext.getAttribute("pmo.tables.users");
		} else {
			return cacheUserHT();

		}
	}

	public Hashtable getTestorHT() {

		if (servletContext.getAttribute("pmo.tables.testors") != null) {
			servletContext.getAttribute("pmo.tables.testors");
		}
		return cacheTestorHT();
	}

	public Integer getUserId() {
		return getSessionInteger("userId");
	}

	public boolean isGuest() {
		String firstName = getSessionString("first_name");
		return firstName.equalsIgnoreCase("guest");
	}

	public boolean isSQLServer() {

		// String product = (String) servletContext
		// .getAttribute("prop_database_product");

		String product = (String) servletContext.getInitParameter("DB-Product");

		return product.equalsIgnoreCase("SQLServer");

	}

	public boolean login() {

		String requestHandle = "";
		String requestPassword = "no password";

		if (requestProperties.containsKey("formHandle")) {
			requestHandle = (String) requestProperties
					.getProperty("formHandle");
		}
		if (requestProperties.containsKey("formPassword")) {
			requestPassword = (String) requestProperties
					.getProperty("formPassword");
		}

		return login(requestHandle, requestPassword);

	}

	public boolean login(String userid, String password) {

		String requestHandle = "";
		String requestPassword = "no password";
		String databasePassword = "get from db";
		String formTarget = "";

		requestHandle = userid;
		requestPassword = password;

		setSessionVar("userLoggedIn", new String("N"));

		/*
		 * find the user handle and password in the HTML request parameters
		 * (FORM variables or URL parameters)
		 */

		if (requestProperties.containsKey("Target")) {
			formTarget = (String) requestProperties.getProperty("Target");
		}

		try {

			UserPlugin userTbl = new UserPlugin(db, requestHandle, this);

			/*
			 * pass sm to the Plugin
			 */
			userTbl.sm = this;

			if (userTbl.getHasRow()) {

				userFound = true;

				databasePassword = userTbl.getText("password_nm");

				setSessionVar("userId", (Integer) userTbl.getKey());

				setSessionVar("password", databasePassword);

				setSessionVar("first_name", userTbl.getText("first_name"));

				setSessionVar("home_page", userTbl.getText("home_page_nm"));

				setSessionVar("user_type", userTbl.getText("type_cd"));

				setSessionVar("last_name", userTbl.getText("last_name"));

				try {
					setSessionVar("user_suite_cd",
							userTbl.getText("user_suite_cd"));
					setSessionVar("user_suite_nm", userTbl.getText("suite_nm"));
				} catch (Exception e) {
					setSessionVar("user_suite_cd", "HC");
					setSessionVar("user_suite_nm", "Default/HC");
				}

				setSessionVar("default_prod_cd",
						userTbl.getText("default_prod_cd"));

				setSessionVar("handle", userTbl.getText("handle"));

				setSessionVar("ip_issue_triage_cd",
						userTbl.getText("ip_issue_triage_cd"));

				setSessionVar("pat_sfty_cd", userTbl.getText("pat_sfty_cd"));

				setSessionVar("sr_tracker_level_cd",
						userTbl.getText("sr_tracker_level_cd"));

				setSessionVar("active_flag", userTbl.getText("active_flag"));

				setSessionVar("testor_flag", userTbl.getText("testor_flag"));

				setSessionVar("leader_flag", userTbl.getText("leader_flag"));

				setSessionVar("sox_role_flag", userTbl.getText("sox_role_flag"));

				setSessionVar("ip_leader_flag",
						userTbl.getText("ip_leader_flag"));

				setSessionVar("reviewer_flag", userTbl.getText("reviewer_flag"));

				setSessionVar("administrator_flag",
						userTbl.getText("administrator_flag"));

				setSessionVar("root_flag", userTbl.getText("root_flag"));

				setSessionVar("grantable_flag",
						userTbl.getText("grantable_flag"));

				setSessionVar("executive_flag",
						userTbl.getText("executive_flag"));

				setSessionVar("chg_aprv_flag", userTbl.getText("chg_aprv_flag"));

				setSessionVar("user_default_project_id",
						(Integer) userTbl.getObject("default_project_id"));

				setSessionVar("user_style_nm", userTbl.getText("style_nm"));

				setSessionVar("user_division_id",
						(Integer) userTbl.getObject("division_id"));

				setSessionVar("divisionid",
						(Integer) userTbl.getObject("division_id"));

				setSessionVar("ProjectId",
						(Integer) userTbl.getObject("default_project_id"));

				setSessionVar("email_address", userTbl.getText("email_address"));

				setSessionVar("DivisionName", userTbl.getText("div_name"));

				setSessionVar("PasswordHint",
						userTbl.getText("password_hint_tx"));

				setSessionVar("PasswordAnswer",
						userTbl.getText("password_answer_tx"));

			}

			else {
				if (requestHandle.length() == 0) {
					loginMessage = "";
				} else {
					loginMessage = "User '" + requestHandle + "' not found.";
				}

				return false;
			}

		} catch (services.ServicesException e) {
			debug("Exception ; " + requestHandle);
			loginMessage = "Exception fetching user record from database for "
					+ requestHandle;
			return false;
		}

		/*
		 * Check for Password Reset request
		 */

		if (formTarget.equalsIgnoreCase("PasswordForm")) {
			loginMessage = "Enter answer to secret question.";
			setSessionVar("userLoggedIn", "N");
			return false;
		}

		if (databasePassword.equalsIgnoreCase(requestPassword)) {
			loginMessage = "Login ok.";
			setSessionVar("userLoggedIn", "Y");
			cacheUserFilters();
			cacheServletFilters();
			updateUserLogin(requestHandle);
			return true;
		} else {
			loginMessage = "Incorrect password.";
			return false;
		}
	}

	// update the user last_login_date and login_count_no
	private void updateUserLogin(String handle) {

		String sql = isSQLServer() ? setrowcount
				: ""
						+ " UPDATE tuser set logged_in_flag = 'Y', login_count_no = login_count_no + 1, "
						+ " login_date = " + (isSQLServer() ? getdate : now)
						+ " where user_id > 0 AND handle = '" + handle + "'"
						+ (isSQLServer() ? "" : limit_one);
		try {
			db.runQuery(sql);
		} catch (services.ServicesException e) {
			debug("Error updating login user date and count.");
		}
	}

	// update the user last_login_date and login_count_no
	private void updateUserLogout(String handle) {

		String sql = isSQLServer() ? setrowcount : ""
				+ " UPDATE tuser set logged_in_flag = 'N' "
				+ " where user_id > 0 AND  handle = '" + handle + "'"
				+ (isSQLServer() ? "" : limit_one);
		try {
			db.runQuery(sql);
		} catch (services.ServicesException e) {
			debug("Error updating login user date and count.");
		}
	}

	public void logout() {
		updateUserLogout(getHandle());
		setSessionVar("userLoggedIn", new String("N"));
		session.invalidate();
		loginMessage = "Logout successful.";
	}

	public String lookUpApplicationName(Integer applicationId) {
		String theName;

		try {
			theName = db
					.getColumn(" Select application_name from tapplications where application_id = "
							+ applicationId.toString());
			return theName;
		} catch (services.ServicesException se) {
			return "Application not found";
		}
	}

	public String lookUpDivisionName(Integer divisionId) {
		String theName;

		try {
			theName = db
					.getColumn(" Select div_name from tdivision where division_id = "
							+ divisionId.toString());
			return theName;
		} catch (services.ServicesException se) {
			return "Unknown Program";
		}
	}

	/*
	 * Use the DbConnectionFactory to return an SQL connection object ...
	 * depending on the properties for db product (DB2, MySQL, Oracle, SQL
	 * Server, etc)
	 */

	private Connection openConnection() throws services.ServicesException {

		DbConnectionFactory factory = new DbConnectionFactory();

		// return factory.getConnection((String) servletContext
		// .getAttribute("prop_database_product"), (String) servletContext
		// .getAttribute("prop_database_url"), (String) servletContext
		// .getAttribute("prop_database_login"), (String) servletContext
		// .getAttribute("prop_database_pw"));

		return factory.getConnection(
				(String) servletContext.getInitParameter("DB-Product"),
				(String) servletContext.getInitParameter("DB-URL"),
				(String) servletContext.getInitParameter("DB-Userid"),
				(String) servletContext.getInitParameter("DB-Password"));

	}

	// alias for getRequestParm
	public String Parm(String p) {
		return getRequestParm(p);
	}

	public String[] ParmArray(String p) {
		return getRequestParmArray(p);
	}

	public boolean parmExists(String parmId) {
		try {
			if (requestProperties.containsKey((Object) parmId)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Used by the CodesPlugin after a code is added or updated. this allows the
	 * new or changed code to immediately appear on the form pages
	 */
	public void removeCache(String codeType) {
		try {
			servletContext.removeAttribute(codeType);
		} catch (Exception e) {

		}
	}

	public void removeServletVar(String s) {
		try {
			servletContext.removeAttribute(s);
		} catch (Exception e) {

		}
	}

	public void removeServletCodes(String table) {

		try {
			servletContext.removeAttribute("pmo.codes." + table.toLowerCase());

		} catch (Exception e) {
			debug("sm:removeServletCodes: " + e.toString());

		}
		try {
			if (servletContext.getAttribute("pmo.codes." + table.toLowerCase()) == null) {
			} else {
			}
		} catch (Exception e) {
			debug(" ex removing table " + e.toString());
		}
	}

	public void removeServletTable(String table) {
		try {
			servletContext.removeAttribute("pmo.tables." + table);
		} catch (Exception e) {

		}
	}

	public void setApplicationId(Integer application_id) {

		String ifnull = " ifnull";

		if (isSQLServer())
			ifnull = " isnull";

		String query = null;
		try {

			// do left join on tuser_project because they may not have a role if
			// they are administrator.
			// Also, watch out for Administrator deleting the user authority
			// which user is working.

			if (userIsAdministrator() && false) {
				query = new String(
						" Select application_name , 'ADM' as role_cd, 'Administrator' as code_desc "
								+ " from tapplications "
								+ " where tapplications.application_id = "
								+ application_id.toString());
			} else {
				query = new String(
						" Select tapplications.application_id as application_id, application_name , "
								+ ifnull
								+ "(tuser_application.role_cd, 'BRW') as role_cd, "
								+ ifnull
								+ "(tcodes.code_desc, 'Browse') as code_desc  "
								+ " from tapplications "
								+ " left join tuser_application on tapplications.application_id = tuser_application.application_id and tuser_application.user_id = "
								+ getUserId().toString()
								+ " left join tcodes on tuser_application.role_cd = tcodes.code_value and tcodes.code_type_id = 14 "
								+ " where tapplications.application_id = "
								+ application_id.toString());

			}

			ResultSet rs = db.getRS(query);

			if (rs.next()) {
				setApplicationId(new Integer(rs.getInt("application_id")),
						rs.getString("application_name"),
						rs.getString("role_cd"), rs.getString("code_desc"));

			}
		} catch (services.ServicesException se) {
			debug("services sql except setting application id " + se.toString());
		} catch (SQLException se) {
			debug("SQL Exception setting application id :  " + se.toString());
		}

	}

	public void setApplicationId(Integer parmId, String appName,
			String role_cd, String role_name) {

		setSessionVar("ApplicationId", parmId);
		setSessionVar("ApplicationName", appName);
		setSessionVar("ApplicationRole", role_cd);
		setSessionVar("ApplicationRoleName", role_name);

	}

	public void setApplicationName(String appName) {
		setSessionVar("ApplicationName", appName);
	}

	// 10/25/10 Paulsen ; use generic parent get/set
	// public void setCodeTypeId(Integer parmId, String codeTypeName) {
	// setSessionVar("CodeTypeId", parmId);
	// setSessionVar("CodeTypeName", codeTypeName);
	// }

	// public void setCodeTypeName(String codeName) {
	// setSessionVar("CodeTypeName", codeName);
	// }

	public void setFormId(Integer formId, String formName) {
		setSessionVar("FormId", formId);
		setSessionVar("FormName", formName);
	}

	public void setDivisionId(Integer parmId, String divName) {
		setSessionVar("divisionid", parmId);
		setSessionVar("DivisionName", divName);
	}

	public void setDivisionName(String divName) {
		setSessionVar("DivisionName", divName);
	}

	public void setRfcNo(String parmId, String rfcName) {
		setSessionVar("RfcNo", parmId);
		setSessionVar("RfcName", rfcName);
	}

	public String getRfcNo() {
		return getSessionString("RfcNo", "0");
	}

	public String getFormName() {
		return getSessionString("FormName");
	}

	public void setFormName(String formName) {
		setSessionVar("FormName", formName);
	}

	// *****************
	// PUBLIC METHODS
	// *****************

	public void setProjectId(Integer project_id) {

		String query = null;
		try {

			// do left join on tuser_project because they may not have a role if
			// they are administrator.
			// Also, watch out for Administrator deleting the user authority
			// which user is working.

			String ifnull = " ifnull";

			if (isSQLServer())
				ifnull = " isnull";

			if (userIsAdministrator() && false) {
				query = new String(
						" Select project_name , menu_cd , primary_application_id, 'ADM' as role_cd, 'Administrator' as code_desc "
								+ " from tproject "
								+ " join tprocess on tproject.process_id = tprocess.process_id "
								+ " where tproject.project_id = "
								+ project_id.toString());
			} else {
				query = new String(
						" Select project_name , menu_cd , primary_application_id, "
								+ ifnull
								+ "(tuser_project.role_cd, 'BRW') as role_cd, "
								+ ifnull
								+ "(tcodes.code_desc,'Browse') as code_desc "
								+ " from tproject "
								+ " join tprocess on tproject.process_id = tprocess.process_id "
								+ " left join tuser_project on tproject.project_id = tuser_project.project_id and tuser_project.user_id = "
								+ getUserId().toString()
								+ " left join tcodes on tuser_project.role_cd = tcodes.code_value and tcodes.code_type_id = 14 "
								+ " where tproject.project_id = "
								+ project_id.toString());

			}

			ResultSet rs = db.getRS(query);

			if (rs.next()) {
				setProjectId(project_id, rs.getString("project_name"),
						rs.getString("menu_cd"), rs.getString("role_cd"),
						rs.getString("code_desc"));
				setApplicationId(new Integer(
						rs.getInt("primary_application_id")));

			}
		} catch (services.ServicesException se) {
			debug("sql except reading project name and menu");
		} catch (SQLException se) {
			debug("sql except reading project name and menu");
		}

	}

	public void setProjectId(Integer parmId, String projName, String menuName,
			String role_cd, String role_name) {
		setSessionVar("ProjectId", parmId);
		setSessionVar("ProjectName", projName);
		setSessionVar("ProjectMenu", menuName);
		setSessionVar("ProjectRole", role_cd);
		setSessionVar("ProjectRoleName", role_name);
	}

	public void setProjectName(String projName) {
		setSessionVar("ProjectName", projName);
	}

	public void setServiceRequestId(Integer theId, String theName) {
		setSessionVar("ServiceRequestId", theId);
		setSessionVar("ServiceRequestName", theName);
	}

	public void setParentId(Integer theId, String theName) {
		setSessionVar("ParentId", theId);
		setSessionVar("ParentName", theName);
	}

	// this is used to pass the rowId from the Attachment detail page
	// to the StarTeamUpload servlet, which updates the tAttachment with the
	// file name
	// after it uploads it to StarTeam

	public Integer getAttachmentId() {
		return getSessionInteger("AttachmentId");
	}

	public void setAttachmentId(Integer i) {
		setSessionVar("AttachmentId", i);
	}

	public void setParentTable(String table) {
		setSessionVar("ParentTable", table);
	}

	public void setParentKeyName(String keyname) {
		setSessionVar("ParentKeyName", keyname);
	}

	public void setRequirementId(Integer theId, String theName) {
		setSessionVar("RequirementId", theId);
		setSessionVar("RequirementName", theName);
	}

	public void setStageId(Integer stageId, String stageName) {
		setSessionVar("StageId", stageId);
		setSessionVar("StageName", stageName);
	}

	public void setStepKind(String stepKind) {
		setSessionVar("StepKind", stepKind);
	}

	public void setStructureId(Integer structureId, String structureName) {
		setSessionVar("StructureId", structureId);
		setSessionVar("StructureName", structureName);
	}

	public void setStructureType(String elementKind) {
		setSessionVar("StructureType", elementKind);
	}

	public void setStyle(String style_nm) {
		setSessionVar("user_style_nm", style_nm);
	}

	// * create a properties object containing the request parameters
	private Properties unloadrequestProperties(HttpServletRequest req) {

		Enumeration en = req.getParameterNames();
		java.lang.String paramName = new String("");
		java.util.Properties requestProp = new java.util.Properties();

		while (en.hasMoreElements()) {
			paramName = (String) en.nextElement();
			// debug("..." + paramName + " -" + req.getParameter(paramName));
			requestProp.put(paramName, req.getParameter(paramName));
		}
		return requestProp;
	}

	public boolean userIsActive() {
		return getSessionString("active_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	// get enterprise user id
	public String getHandle() {

		return getSessionString("handle");

	}

	public String getPasswordHint() {

		return getSessionString("PasswordHint");

	}

	public String getIssueTriageLevel() {

		return getSessionString("ip_issue_triage_cd");

	}

	public String getPatientSafetyLevel() {

		return getSessionString("pat_sfty_cd");

	}

	// SR TRacker Release 2
	public String getSR_Tracker_Level() {

		return getSessionString("sr_tracker_level_cd");

	}

	public String getPasswordAnswer() {
		return getSessionString("PasswordAnswer");
	}

	public String getEmailAddress() {
		return getSessionString("email_address", "No email address");
	}

	public boolean userIsSox() {
		return getSessionString("sox_role_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean userIsLeader() {
		return getSessionString("leader_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean userIsIP_Leader() {
		return getSessionString("ip_leader_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean userIsReviewer__Not_Used_Anymore() {
		return getSessionString("reviewer_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean userIsAdministrator() {
		return getSessionString("administrator_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean userIsRoot() {
		return getSessionString("root_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean userCanGrant() {
		return getSessionString("grantable_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean isTableLocked() {
		return getSessionString("table_locked").equalsIgnoreCase("Y") ? true
				: false;
	}

	// used by the tcode list/edit page to know of the tcode_types is locked
	public void setTableLocked(String s) {
		setSessionVar("table_locked", s);
	}

	public boolean userIsTestor() {
		return getSessionString("testor_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public boolean userIsExecutive() {
		String flag = getSessionString("executive_flag");
		return flag.equalsIgnoreCase("Y") ? true : false;
	}

	public boolean userIsChgApprover() {
		return getSessionString("chg_aprv_flag").equalsIgnoreCase("Y") ? true
				: false;
	}

	public void setUserProductCode(String product) {
		setSessionVar("default_prod_cd", product);
	}

	public String getUserProduct() {

		return getSessionString("default_prod_cd", "ADT");
	}

	public boolean userIsLoggedIn() {

		return getSessionString("userLoggedIn").equalsIgnoreCase("Y");
	}

}
