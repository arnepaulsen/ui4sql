/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */


package forms;

/**
 * @author Arne Paulsen, all rights reserved.
 *
 * Ca 
 * Change Log : 
 * 7/5/05  writeMenu() - to dump out stuff on the left side menu
 * 			.. especially FormListWriter
 * 7/5/05  get template location (WEB-INF/lib or disk) from properties
 * 				use template_path if reading from disk
 */


import java.util.*;
import router.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


public abstract class FormWriter {

	public String formName = "";
	
	private String subTitle;

	public SessionMgr sm;

	public WebLineWriter out;

	public HttpServletRequest req;

	public HttpServletResponse resp;

	// *****************************
	// CONSTRUCTORS
	// *****************************
	public FormWriter(router.SessionMgr parmSm, WebLineWriter parmOut) {
		sm = parmSm;
		out = parmOut;

		req = parmSm.getRequest();
		resp = parmSm.getResponse();


	}

	// *****************************
	// ABSTRACT METHODS
	// *****************************

	//* the main portion of the pate
	abstract public void writeForm() throws services.ServicesException;

	//* the left side, usually a menu, but could just be info, or blank
	abstract public void writeMenu() throws services.ServicesException;

	abstract public String getBrowserTitle();

	abstract public String getMenuName();

	public abstract boolean isAdminFunction();

	/*
	 * Let the add/show/edit form manager add more fields to the mapping ... so
	 * each dataManager doesn't have to do these
	 */
	public Hashtable getAuditFields(Hashtable ht) {
		return ht;
	}

	// * kind of abstract... normally override by subclasses
	abstract public void preDisplay();

	public void preDisplay(String parmRowKey, String parmAction,
			String parmRelation) {
	}

	/*
	 * 	Public Gets
	 */

	public String getSubTitle() {
		return subTitle;
		
	}
	
	public void setSubTitle(String s) {
		subTitle = s;
	}
	
	public String getJavaScript() throws services.ServicesException {
		return new String("function tmInit() {}");
	}

	// *****************************
	// UTILITY
	// *****************************
	public void debug(String parmMsg) {
		Logger.getLogger("ui4sql").debug(parmMsg);
		//sm.debug("FormWriter: " + parmMsg);
		System.out.println(parmMsg);
	}

}
