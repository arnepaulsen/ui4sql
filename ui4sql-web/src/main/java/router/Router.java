/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package router;

import java.io.IOException;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletConfig;

import plugins.Plugin;

//import org.apache.log4j.Logger;

import router.SessionMgr;
import forms.*;
import java.io.PrintWriter;

import forms.FormAddWriter;
import forms.FormEditWriter;
import forms.FormListWriter;
import forms.FormShowWriter;
import forms.FormWriter;
import forms.WebLineWriter;
import forms.WebPageWriter;
import forms.PopupWriter;

/**
 * Router - main servlet entry point for all Http requests - - gets SQL
 * connection, - creates SessionMgr object, passes it the connection object -
 * creates a WebPageWriter object (frame for the menu and target form) - creates
 * a FormDriver object based on the http request 'target' value - passes the
 * FormDriver to the WebPageWrite.writePage method - closses SQL connection,
 * always
 * 
 * 
 * Change Log :
 * 
 * 7/4/05 Move all connection open/close into Session Manager so it can use
 * properties for login, id, pw
 * 
 * 5/17/06 - add FormPrintWriter, and call webPageWriter.printPage() when
 * action=Print
 * 
 * 3/12/07 - handle 2 user sessions in same browser.
 * 
 * 8/24/08 - add switch to send redirect html for new sever
 * 
 * 8/20/08 - show list page if action = delete
 * 
 * 11/6/08 - HUGE CHANGE.. USE SYNCHRONIZED TO PREVENT USER PAGE CROSS-OVER -
 * use synchronized on all methods - make any new class variables local to the
 * methods
 * 
 * 11/17/08 caution .. adding expires header to html
 * 
 * 11/9/10 paulsen, fix bug when sending password and the user password is
 * blank, so it actually logs in the user
 * 
 * 
 */

public class Router extends HttpServlet implements jakarta.servlet.Servlet {

	private boolean debug = true;

	private boolean redirect = false; // true to send out a re-direct page to

	// a new server.

	private ServletConfig servletConfig = null;

	static final long serialVersionUID = 752647111412776147L;

	// ******************************
	// * CONSTRUCTORS
	// ******************************

	public synchronized void init(ServletConfig config) throws ServletException {
		this.servletConfig = config;

	}

	public synchronized void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		if (redirect)
			Redirect(req, resp);
		else
			RouteIt(req, resp);

		return;
	}

	public synchronized void doPost(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);

		return;
	}

	private synchronized void Redirect(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		PrintWriter out;

		String l1 = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
		String l2 = "<HTML><HEAD>";
		String l3 = "<META http-equiv=\"Refresh\" content=\"15; URL=http://arnepaulsenjr.com:8088/ui4sql/Router\">";
		String l4 = "<META name=\"GENERATOR\" content=\"IBM WebSphere Studio\"></HEAD><BODY>";
		String l5 = "Production has moved to  <a href=\"https://ui4sql.net:8082/ui4sql/Router\">https://ui4sql.net:8082/ui4sql/Router</a>";
		String l6 = "<br><br> Please update your link.<br><br> Thank you.</BODY></HTML>";

		resp.setContentType("text/html");

		resp.setHeader("Pragma", "No-cache");
		resp.setDateHeader("Expires", 0);
		resp.setHeader("Cache-Control", "no-cache");

		out = resp.getWriter();

		out.write(l1);
		out.write(l2);
		out.write(l3);
		out.write(l4);
		out.write(l5);
		out.write(l6);

	}

	// ******************************
	// * MAIN ROUTINE
	// ******************************

	private synchronized void RouteIt(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		ServletContext servletContext = servletConfig.getServletContext();

		WebPageWriter webPageWriter;

		SessionMgr sm;

		PrintWriter out;

		WebLineWriter webLineWriter;

		/*
		 * Get the Session Mgr
		 */

		try {
			sm = new SessionMgr(req, resp, servletContext);
		} catch (Exception e) {
			System.out.println("Router... error getting SessionMgr");
			e.printStackTrace();
			return;
		}

		resp.setContentType("text/html");
		out = resp.getWriter();

		webLineWriter = new WebLineWriter(out);
		webPageWriter = new WebPageWriter(sm, webLineWriter, req, resp);

		/*
		 * Validate the user
		 */

		/*
		 * Note the login screen will have 'action' of login, and a 'target' of
		 * 'HomeForm' .. so log the login action occurs here, then if
		 * successful, the 'HomeForm' target .. page is displayed.
		 */
		if (sm.Parm("Action").equalsIgnoreCase("login")
				|| sm.Parm("Action").equalsIgnoreCase("PasswordStart")
				|| sm.Parm("Action").equalsIgnoreCase("SendPassword")
				|| sm.Parm("Signin").equalsIgnoreCase("yes")) {
			sm.login();
		}

		if (sm.userFound && sm.Parm("Action").equalsIgnoreCase("SendPassword")) {
			sm.sendPassword();
		}

		if (sm.Parm("Action").equalsIgnoreCase("logout")) {
			sm.logout();
		}

		/*
		 * Check for Password Reset request
		 */

		if (sm.Parm("Target").equalsIgnoreCase("PasswordForm")) {
			PasswordForm passwordForm = (PasswordForm) getPasswordForm(sm,
					webLineWriter);

			webPageWriter.writePage(passwordForm);
			webLineWriter.unLoad();

			closeConnection(sm);
			return;
		}

		if (sm.Parm("Target").equalsIgnoreCase("popup")) {

			forms.PopupWriter popup = new forms.PopupWriter(sm, webLineWriter);
			popup.writeWindow();
			webLineWriter.unLoad();
			closeConnection(sm);
			return;

		}

		/*
		 * Send the login form of the user is not logged in.
		 * 
		 * 11/9/10 paulsen. force user to the login form if requesting password.
		 * (The user might actually be logged in if real password is blank.!)
		 */

		if (!sm.userIsLoggedIn()
				|| (sm.Parm("Action").length() == 0 && sm.Parm("Target")
						.length() == 0)
				|| sm.Parm("Action").equalsIgnoreCase("SendPassword")) {

			LoginForm loginForm = (LoginForm) getLoginForm(sm, webLineWriter);

			loginForm.setLoginMessage(sm.loginMessage);

			webPageWriter.writePage(loginForm);
			webLineWriter.unLoad();

			closeConnection(sm);
			return;
		}

		/*
		 * User is logged in.
		 * 
		 * Either home page, or a data display page.
		 */

		if (sm.Parm("Target").equalsIgnoreCase("home")) {

			FormWriter homeForm = new HomeForm(sm, webLineWriter);
			webPageWriter.writePage(homeForm);
			webLineWriter.unLoad();
		} else {
			try {
				if (sm.Parm("Action").equalsIgnoreCase("print")) {
					webPageWriter.printPage(getDataWriter(sm, webLineWriter));
				} else {
					webPageWriter.writePage(getDataWriter(sm, webLineWriter));

				}
				webLineWriter.unLoad();
			} catch (Exception e) {
				out.println("<html><body>Router: error creating FormDriver "
						+ sm.Parm("Target") + "/n exception: " + e.toString()
						+ "</body></html>");
				System.out.println("ex: " + e.toString());
			}
		}

		/*
		 * All done
		 */

		closeConnection(sm);

		return;
	}

	public FormWriter getLoginForm(SessionMgr sm, WebLineWriter webLineWriter) {
		LoginForm loginForm = new LoginForm(sm, webLineWriter);
		return loginForm;
	}

	public FormWriter getPasswordForm(SessionMgr sm, WebLineWriter webLineWriter) {
		PasswordForm passwordForm = new PasswordForm(sm, webLineWriter);
		return passwordForm;
	}

	// default getWriter assumes there is a form for List, Edit, Show, Add
	// if not, or special processing, then FormDriver must override.

	public FormWriter getDataWriter(SessionMgr sm, WebLineWriter webLineWriter) {

		// System.out.println("router: getDataWriter") ;

		Plugin plugin = buildPlugin(sm);

		try {
			plugin.doAction(sm.Parm("Action"));
		} catch (services.ServicesException e) {
			System.out.println("doAction exception");

		}

		// ********************************
		// first get the table Manager
		// ********************************
		// debug("getWriter: building plugin - Action = " + sm.Parm("Action")
		// + " FROM = " + sm.Parm("From"));

		FormWriter formWriter;

		if (sm.Parm("Action").equalsIgnoreCase("List")
				|| sm.Parm("Action").equalsIgnoreCase("excel")
				|| sm.Parm("Action").equalsIgnoreCase("delete")
				|| plugin.getDataFormName().equalsIgnoreCase("list")
				|| (sm.Parm("Action").equalsIgnoreCase("canceladd") && sm.Parm(
						"From").equalsIgnoreCase("list"))) {
			FormListWriter formListWriter = new FormListWriter(sm,
					webLineWriter);
			formListWriter.init(plugin);
			formWriter = (FormWriter) formListWriter;

		} else if (sm.Parm("Action").equalsIgnoreCase("Add")
				|| plugin.getDataFormName().equalsIgnoreCase("Add")) {
			FormAddWriter formAddWriter = new FormAddWriter(sm, webLineWriter);
			formAddWriter.init(plugin);
			formWriter = (FormWriter) formAddWriter;

			// retrofit for frames
		} else if ((sm.Parm("Action").equalsIgnoreCase("Edit"))
				|| (sm.Parm("Action").equalsIgnoreCase("Copy"))
				|| (sm.Parm("Action").equalsIgnoreCase("Submit"))
				|| (sm.Parm("Action").equalsIgnoreCase("Review"))
				|| (sm.Parm("Action").equalsIgnoreCase("Approve"))
				|| (sm.Parm("Action").equalsIgnoreCase("Rescind"))
				|| (sm.Parm("Action").equalsIgnoreCase("Retract"))
				|| (sm.Parm("Action").equalsIgnoreCase("PostThenEdit"))) {
			FormEditWriter formEditWriter = new FormEditWriter(sm,
					webLineWriter);
			formEditWriter.init(plugin);
			formWriter = (FormWriter) formEditWriter;

		} else {

			if (sm.Parm("Action").equalsIgnoreCase("Print")) {
				FormPrintWriter formPrintWriter = new FormPrintWriter(sm,
						webLineWriter);
				formPrintWriter.init(plugin);
				formWriter = (FormWriter) formPrintWriter;
			}

			else {
				FormShowWriter formShowWriter = new FormShowWriter(sm,
						webLineWriter);
				formShowWriter.init(plugin);
				formWriter = (FormWriter) formShowWriter;
			}
		}

		return formWriter;

	}

	public synchronized Plugin buildPlugin(SessionMgr sm) {

		Plugin plugin = null;

		// System.out.println("router: build plugin:");

		/*
		 * 1st try to find a traditional plugin, .. if not found, then look for
		 * a Spring bean
		 */

		if (sm.getTarget().equalsIgnoreCase("Exposure")
				|| sm.getTarget().equalsIgnoreCase("Patient")) {
			debug("getting xml bean..");
			return getPluginFromBean(sm, sm.getTarget());
		}
		try {

			plugin = (Plugin) Class.forName(
					"plugins." + sm.getTarget() + "Plugin").newInstance();

			plugin.init(sm); // pass the Session to the TableManager

		} catch (InstantiationException e) {
			System.out.println("instanciaion except");
		} catch (IllegalAccessException e) {
			System.out.println("illegal access");
		} catch (ClassNotFoundException e) {

			System.out.println("plugin not found, looking for a bean!!!");

			return getPluginFromBean(sm, sm.getTarget());

		}
		return plugin;

	}

	private Plugin getPluginFromBean(SessionMgr sm, String name) {

		System.out.println("router:getPluginFromBean calling bean factory!!!");

		PMOBeanFactory bf = new PMOBeanFactory();
		Plugin plugin = bf.getPlugin(sm.getTarget());
		plugin.init(sm);
		return plugin;

	}

	// ************************
	// always close connection
	// ************************
	private void closeConnection(SessionMgr sm) {
		try {
			if (sm.isConnected())
				sm.closeConnection();
		} catch (Exception e) {
			System.out.println("Error closing connection");
		}
	}

	public void debug(String debugMsg) {
		//Logger.getLogger("ui4sql").debug(debugMsg);
		if (true) {
			System.out.println("Router: " + debugMsg);
		}
	}
}
