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
 * Phase :
 * 	PasswordStart  - the requeset
 * 	PasswordPost - interate until done
 * 	PasswordDone - final page
 * 
 *  
 */

public class PasswordForm extends FormWriter {

	
	private String loginMessage = "";
	
	public PasswordForm(SessionMgr parmSm,	WebLineWriter parmOut) {
		super(parmSm, parmOut);
		this.formName = "PasswordForm";
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
		return "PMO Connect";
	}
	
	public String getSubTitle() {
		return "Please login.";
	}
		
	public void writeForm() {
		
		String  phase =  sm.Parm("Action");
			
		
		try {
			out.println("<form name=Form1 action=Router Method=POST>");
			out.println("<br><br>Password Reset<br>");
		
		
			if (phase.equalsIgnoreCase("PasswordStart")) {
				writeStart();
			}
			else  {
				writePost();
			}							
		
			out.print("<tr><td colspan=2></td></tr><tr><td colspan=2> </td></trr>");
			
			out.println("</td></tr></table>");
			
			out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Target\" value=\"PasswordForm\">");
				
			out.println("</form>");
			out.println("<br>" + loginMessage);
		} catch (Exception ioe) {
		}

		return;

	}
	
	private void writeStart() {
		
		out.println("<table><tr><td></td><td>NUID:</td><td>" + sm.getHandle() + "</td></tr>");
			
		out.println(
			"<tr><td></td><td>Hint:</td><td>" + sm.getPasswordHint() + "</td></tr>");
		
		out.println("<tr><td></td><td>Answer</td><td><INPUT type=\"TEXT\" name=\"PasswordAnswer\" value=\"\"></td></tr>");
	
		out.println("<tr><td></td><td>New Password</td><td><INPUT type=\"TEXT\" name=\"NewPassword\" value=\"\"></td></tr>");
				
		out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Action\" value=\"PasswordReset\">");
		
		out.println("<tr><td></td><td><a href=# onClick='document.forms[0].submit();'>Reset it.</a></td></tr>");
		out.println("<tr></tr><tr><td></td></tr>");
		out.println("<tr><td>&nbsp;</td></tr>");
		
	}
	
	private void writePost() {
		
		String answer = sm.Parm("PasswordAnswer");
		
		if (answer.equalsIgnoreCase(sm.getPasswordAnswer())) {
			debug("reseting password");
			sm.resetPassword(sm.Parm("NewPassword"));
			
			out.println("<table><tr><td></td><td>Password changed.</td><td></td></tr>");
		}
				
		else {
			
			out.println("<table><tr><td></td><td>Incorrect answer. Try again, or contact the administrator.</td><td></td></tr>");
			
		}
		

		
	}
	
	
	
	
	
	public void preDisplay() {};

	public void setTableManager(Plugin tm) {
	}
	
	public String getDisplayMessage() {
		return new String("Enter user id and password.");
	}
	
	public String getPageTitle() {
		return new String("Grand Unified Spreadshet");
	}
	
	public String getJavaScript() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("function tmInit() {}");	
		
		sb.append("\nfunction doSend(){"
				+ "\ndocument.forms[0].submit();"				
				+ "}");

		
		
		return sb.toString();
	}
}
