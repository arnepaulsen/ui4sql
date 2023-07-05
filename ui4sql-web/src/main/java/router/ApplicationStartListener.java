/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package router;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContext;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/*
 * change log:
 * 
 * 6/30/07 remove logger output for now
 * 
 * 7/20/10 - remove obsolete db-supports-subqueries for pre-5.0 MySQL
 * 
 * this class is not avaialbe in Java Servlet 1.2 (requires 1.3 i believe),
 * Websphere 5 does not support 
 * so. load what's needed in the user context
 */

public class ApplicationStartListener implements ServletContextListener {

	ServletContext ctx = null;

	public ApplicationStartListener() {

	}

	public void contextDestroyed(ServletContextEvent evt) {
		// TODO Auto-generated method stub

	}

	public void contextInitialized(ServletContextEvent evt) {

		System.out.println("UI4SQL App STart Listner : starting");

		ctx = evt.getServletContext();

		String debugLevel = ctx.getInitParameter("Log-Level");
		ctx.setAttribute("Log-Level", debugLevel);

		// getSystemProperties(ctx);
		// getWebXMLParameters();

		Logger mainLogger = Logger.getLogger("ui4sql");
		PatternLayout pl = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss}; %m%n");
		// "%d{yyyy-MM-dd HH:mm:ss};%t;%p; %m%n"

		try {
			File f = new File(ctx.getRealPath("/logs"));
			if (!f.exists())
				f.mkdir();

			mainLogger.setLevel(Level.toLevel(debugLevel));
			mainLogger.setLevel(Level.DEBUG);

			String theLogFile = ctx.getRealPath("/logs/ui4sql.log");
			FileAppender appender = new FileAppender(pl, theLogFile);

			mainLogger.setAdditivity(false);
			mainLogger.addAppender(appender);
		}

		catch (Exception e) {
			System.out.println("StartListener : Error " + e.toString());
		}
	}

	/*
	 * Fetch configuration from web.xml context-parama
	 */
	public void getWebXMLParameters() {

		/*
		 * Path to templates
		 */

		// debug("starting listner - get parameters");

		ctx.setAttribute("prop_templates_from_lib", ctx
				.getInitParameter("Templates-from-lib"));

		ctx.setAttribute("prop_templates_path", ctx
				.getInitParameter("Templates-path"));

		// debug ("template path is - " + ctx
		// .getInitParameter("Templates-path"));

		/*
		 * Path to root of web
		 */
		ctx.setAttribute("prop_http_offset", "");

		try {
			if (ctx.getInitParameter("HTTP-Offset") != null) {
				ctx.setAttribute("prop_http_offset", ctx
						.getInitParameter("HTTP-Offset"));
			}
		} catch (Exception e) {

		}

		/*
		 * Database connection, capabilities
		 */
		ctx.setAttribute("prop_database_login", ctx
				.getInitParameter("DB-Userid"));
		ctx.setAttribute("prop_database_pw", ctx
				.getInitParameter("DB-Password"));
		ctx.setAttribute("prop_database_url", ctx.getInitParameter("DB-URL"));
		ctx.setAttribute("prop_database_product", ctx
				.getInitParameter("DB-Product"));
	}

	/*
	 * load system properties
	 */
	public void getSystemProperties(ServletContext ctx) {

		Properties prop = new Properties();

		try {
			InputStream is = getClass().getResourceAsStream(
					"/cmm/properties/cmm.properties");

			prop.load(is);

			ctx.setAttribute("prop_company", prop.getProperty("company"));

			ctx.setAttribute("prop_templates_from_lib", prop
					.getProperty("templates_from_lib"));
			ctx.setAttribute("prop_templates_path", prop
					.getProperty("templates_path"));
			ctx.setAttribute("prop_http_offset", prop
					.getProperty("http_offset"));

			ctx.setAttribute("prop_database_login", prop
					.getProperty("database_login"));
			ctx.setAttribute("prop_database_pw", prop
					.getProperty("database_pw"));
			ctx.setAttribute("prop_database_url", prop
					.getProperty("database_url"));
			ctx.setAttribute("prop_database_product", prop
					.getProperty("database_product"));

		} catch (Exception e) {
			debug("Application Listener: Exception loading properties");
			debug(e.toString());

		}

	}

	private void debug(String debugMsg) {
		System.out.println(debugMsg);
		Logger.getLogger("ui4sql").debug(debugMsg);
	}

}
