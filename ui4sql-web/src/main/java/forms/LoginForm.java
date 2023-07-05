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
 * 11/30/05 - Use Method=POST 02/2/06 - Change target to 'Frame' 5/10/06 -
 * Change target to Project&Action=List 5/12/06 - fix 'myInit()... should be
 * 'tmInit(); 5/15/06 - add 'register' link for new users 10/12/06 - get target
 * page from user record 'home_page' 9/24/07 - set handle maxlength=7 (can never
 * be longer than an nuid)
 * 
 * 
 * todo: add user preference to select Home or Project/Dashboard
 * 
 * 2/14 - Add system message 
 * 
 * 
 */
public class LoginForm extends FormWriter {

	private String loginMessage = "";

	public LoginForm(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);
		this.formName = "LoginForm";
	}

	public void setLoginMessage(String msg) {
		loginMessage = msg;
	}

	// Need a menu for lowly login page.
	public String getMenuName() {
		return "empty";
	}

	public boolean isAdminFunction() {
		return false;
	}

	public void writeMenu() {
		return;
	};

	public String getBrowserTitle() {
		return "PMO 2 Arne";
	}

	public String getSubTitle() {
		return "Please login.";
	}

	public void writeForm() {
		try {

			String systemMessage = sm.getSystemMessage();

			out.println("<form name=Form1 action=Router Method=POST>");

			out.println("<br><br><br>");
			out.println("<table>");


			out.println("<tr><td></td><td></td><td>" + systemMessage
					+ "</td></tr>");
			
			out.println("<tr><td></td><td></td><td>&nbsp;</td></tr>");
			out.println("<tr><td></td><td></td><td>&nbsp;</td></tr>");
			out.println("<tr><td></td><td></td><td>&nbsp;</td></tr>");

			out
					.println("<tr><td></td><td>User:</td><td><input type=TEXT NAME=formHandle size=7 maxlength=7 value=''></td></tr>");

			out
					.println("<tr><td></td><td>Password:</td><td><input type=PASSWORD onKeyPress='checkEnter(event)' NAME=formPassword size=12 value=''>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=# onClick='document.forms[0].submit();'>Login</a> </td></tr>");

			out.println("<tr><td>&nbsp;</td></tr>");
			out.println("<tr></tr><tr><td></td></tr>");
			out.println("<tr><td>&nbsp;</td></tr>");
		//	out.println("<tr><td></td><td><a name=loginanchor href=# onClick='doRegister();'>Create login id.</a></td></tr>");

			if (false) {
				out
						.println("<tr><td></td><td><a name=resetanchor href=# onClick='doPassword();'>Reset password.</a></td></tr>");
			}

			if (true) {
				out
						.println("<tr><td></td><td><a name=emailpassword href=# onClick='doEmail();'>Send password.</a></td></tr>");
			}

			out
					.print("<tr><td colspan=2></td></tr><tr><td colspan=2> </td></trr>");

			out.println("</td></tr></table>");

			out
					.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Target\" value=\"guess\">");
			out
					.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Action\" value=\"List\">");
			out
					.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\" name=\"Signin\" value=\"Yes\">");

			// out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\"
			// name=\"Target\" value=\"Project\">");
			// out.println("<br>&nbsp;&nbsp;&nbsp;&nbsp;<INPUT type=\"hidden\"
			// name=\"Action\" value=\"List\">");

			out.println("</form>");
			out.println("<br>" + loginMessage);
		} catch (Exception ioe) {
		}

		return;

	}

	public void preDisplay() {
	};

	public void setTableManager(Plugin tm) {
	}

	public String getDisplayMessage() {
		return new String("Enter user id and password.");
	}

	public String getPageTitle() {
		return new String("Grand Unified Spreadsheet");
	}

	public String getJavaScript() {

		StringBuffer sb = new StringBuffer();

		sb.append("function tmInit() {}");

		sb.append("\nfunction doRegister(){"
				+ "\ndocument.Form1.Target.value='Register';"
				+ "\ndocument.Form1.Action.value='add';"
				+ "\ndocument.Form1.formPassword.value='register';"
				+ "\ndocument.Form1.formHandle.value='registe';"
				+ "\ndocument.forms[0].submit();return;} "

				+ "\nfunction doPassword() {"
				+ "\ndocument.Form1.Target.value='PasswordForm';"
				+ "\ndocument.Form1.Action.value='PasswordStart';"
				+ "\ndocument.Form1.formPassword.value='';"
				+ "\ndocument.forms[0].submit();return;} "

				+ "\nfunction doEmail() {"
				+ "\ndocument.Form1.Target.value='LoginForm';"
				+ "\ndocument.Form1.Action.value='SendPassword';"
				+ "\ndocument.Form1.formPassword.value='';"
				+ "\ndocument.forms[0].submit();return;} "

				+ "\nfunction checkEnter(e){" + "\nvar characterCode "
				+ "\nif (e && e.which) { " + "\n	e=e "
				+ "\n	characterCode = e.which }" + "\nelse {" + "\n	e=event "
				+ "\n	characterCode=e.keyCode } "
				+ "\nif (characterCode == 13) {"
				+ "\n	document.forms[0].submit() " + "\n	return false }"
				+ "\nelse {" + "\n	return true }" + "\n}");

		// + "\n//-->\n</script>");

		return sb.toString();
	}
}
