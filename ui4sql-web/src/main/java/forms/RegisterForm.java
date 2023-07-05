/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import router.SessionMgr;
import plugins.Plugin;

/**
 * @author PAULSEAR
 *
 * 11/30/05 - Use Method=POST
 * 02/2/06 - Change target to 'Frame' 
 * 5/10/06 - Change target to Project&Action=List
 * 5/12/06 - fix 'myInit()... should be 'tmInit();
 * 
 * todo:
 * 	add user preference to select Home or Project/Dashboard
 * 
 */
public class RegisterForm extends FormWriter {

	
	private String loginMessage = "";
	
	public RegisterForm(SessionMgr parmSm,	WebLineWriter parmOut) {
		super(parmSm, parmOut);
		this.formName = "LoginForm";
	}
	
			
	public void setLoginMessage(String msg) {
		loginMessage = msg;
	}
	
	// Need a menu for lowly login page.
	public String getMenuName () {
		return "empty";
	}
	public boolean isAdminFunction() {
		return false;
	}
	
	public void writeMenu() {
		return;
	};

	public String getBrowserTitle() {
		return "Empty Form";
	}
	
	public String getSubTitle() {
		return "Please login.";
	}
		
	public void writeForm() {
		try {
			out.println("<form action=Router Method=POST>");
			out.println("<br><br><br>");
			out.println("<table><tr><td>User id:</td><td><input type=TEXT NAME=formHandle size=20 value=demo></td></tr>");
			out.println(
				"<tr><td>Password:</td><td><input type=PASSWORD NAME=formPassword size=20 value=demo></td></tr>");
			out.println("<tr><td><a href=# onClick='document.forms[0].submit();'>Login</a></td></tr>");
			out.println("</td></tr></table>");
			
			out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Target\" value=\"Home\">");
			out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Action\" value=\"Register\">");
		
			
			//out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Target\" value=\"Project\">");
			//out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Action\" value=\"List\">");
			//out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Start\" value=\"Yes\">");
			
			out.println("</form>");
			out.println("<br><br>" + loginMessage);
		} catch (Exception ioe) {
		}

		return;

	}
	
	
	public void preDisplay() {};

	public void setTableManager(Plugin tm) {
	}
	
	public String getDisplayMessage() {
		return new String("Enter user id and password.");
	}
	
	public String getPageTitle() {
		return new String("Grand Unified Spreadsheet");
	}
	
	public String getJavaScript() {
		return new String("function tmInit() {}");	
	}
}
