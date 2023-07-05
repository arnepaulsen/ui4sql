/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import router.SessionMgr;

/**
 * @author PAULSEAR
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class EmptyForm extends FormWriter {

	public String getMenuName () {
		return "";
	}
	
	public EmptyForm(SessionMgr parmSm,	WebLineWriter parmOut) {
		super(parmSm, parmOut);
		this.formName = "LoginForm";
	}
	
	public boolean isAdminFunction() {
		return false;
	}

	public void writeMenu() {
		return;
	};

	public String getBrowserTitle() {
		return "PMO-Connect";
	}
	
	public String getSubTitle() {
		return "Welcome to an empty form!";
	}
	

	public void writeForm() {
		try {
			out.println("<form action=Router Method=GET>");
			out.println("<p>Pick your function... empty form</p>");
			out.println("<br><a href=Tasks>Tasks</a>");
			out.println("<br><a href=Projects>Projects</a>");
		} catch (Exception ioe) {
			// throw new services.ServicesException(ioe.toString());
		}

		return;

	}

	public void preDisplay() {
	}

	public String getPageTitle() {
		return new String("Empty");
	}

	public String getDisplayMessage() {
		return new String("Empty");
	}

	public String getJavaScript() {
		return new String("function tmInit() {}");
	}

}
