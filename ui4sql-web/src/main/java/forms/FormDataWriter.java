/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen, all rights reserved.
 *
 * This is a special type of FormWriter for List,Edit, Show and Add
 *  
 * These four FormWriters have a Plugin that gets data from
 * the database and maps it into a template, generating html 
 * objects that depend on the mode (show, add, edit) 
 * 
 * The ListFormWriter doesn't use the template, it just lists 
 * specific columns for a set of rows. 
 * 
 * Change Log : 
 * 7/9/ Brand New
 * 10/27 move plugin.init into the try-catch block
 * 10/12/06 get target from SM
 * 8/14/07 - drop applicationListener, use servletContext.getInitParameter("")
 * 5/1/08 increase kbd replace from 6 to 8 
 * 6/5/23 get templates from application path "/templates/,  not url
 * 
 */

import java.io.BufferedReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import router.*;
import plugins.Plugin;

public abstract class FormDataWriter extends FormWriter {

	// private String template_path = new String("d:/templates/");

	String mode = ""; // one of shoe, edit, add

	public String formName = "";

	public Plugin plugin;

	public String formWriterType = ""; // like list, show, add, Edit

	// *****************************
	// CONSTRUCTORS
	// *****************************

	public FormDataWriter(SessionMgr parmSm, WebLineWriter parmOut) {
		super(parmSm, parmOut);

		// buildPlugin(); // build the Plugin
	}

	public void init(Plugin parmPlugin) {
		plugin = parmPlugin;
	}

	public boolean isAdminFunction() {
		return plugin.getIsAdminFunction();
	}

	// 9/9/07 Changed from 'return target'
	public String getBrowserTitle() {
		return sm.getTarget();
	}

	public String getSubTitle() {
		return plugin.getSubTitle();
	}
	
	// *****************************
	// ABSTRACT METHODS
	// *****************************

	abstract public void writeForm() throws services.ServicesException;

	public String getJavaScript() throws services.ServicesException {
		return new String("function tmInit() {}");
	}

	/*jwt download
	 * Let the add/show/edit form manager add more fields to the mapping ... so each
	 * dataManager doesn't have to do these
	 */
	public Hashtable getAuditFields(Hashtable ht) {
		return ht;
	}

	abstract public String getButtons();

	public String getMenuName() {
		return plugin.getMenuName(sm.Parm("Action"));

	}

	// *****************************

	// PUBLIC GETS
	// *****************************

	public void writeMenu() {

		// out
		// .println("<table cellspacing=0 cellpadding=0 border=0
		// style='margin-bottom:10'></table>");

	}

	// 9/10/07 do we need this ?

	// public void setTarget(String parmTarget) {
	// target = parmTarget;
	// }

	// *****************************
	// UTILITY
	// *****************************
	public void debug(String debugMsg) {

		//if (sm.getLastName().equalsIgnoreCase("paulsen")) {
		sm.debug(debugMsg);
		//}
	}

	public Integer doAction(String parmAction) throws services.ServicesException {

		return plugin.doAction(parmAction);

	}

	public void writeTemplate(String templateName, String mode) throws services.ServicesException {

		/*
		 * big call... the dataManager puts all the webfields into a ht
		 */

		/*
		 * Having problems.. first check the plug to make sure the getWebFields doesn't
		 * have a bug... like referencing the db.getText in add mode (there's no db
		 * object in add mode)
		 */

		debug ("formDataWriter.writeTemplate..starting writeTemplate - mode is : " + mode);

		plugin.mode = mode;

		// TAG UI4SQL.2.0 paramaterize HT
		Hashtable <String, WebField> webFields  = plugin.getWebFieldHashtable(mode);

		debug ("FormDataWriter .. calling getWebFieldHashTable");

		// TAG: UI4SQLV1
		webFields = getAuditFields(webFields);

		/*
		 * Add the button anchors to the webFields, then they get put in the form
		 */

		//debug("getting buttons");

		String buttons = getButtons();

		if (buttons.length() > 1) {
			webFields.put("buttons", new WebFieldDisplay("buttons", buttons));
		}

		//debug ("  the buttons .. " + buttons);

		try {

			/*
			 * If local develop system, read templates from disk, at isp, read from jar file
			 * in /lib/ directory.
			 */

			BufferedReader br;

			String path = "http://45.56.91.201:8082/templates/" + templateName;
			
			debug ("template path : " + path);
			
			URL url = new URL(path);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			InputStreamReader isr = new InputStreamReader(conn.getInputStream());

			debug("getting buffered reader: " + path);
			

			//FileInputStream dataFile = new FileInputStream(sm
				//	.getServletContext().getInitParameter("Templates-path")
				//+ templateName);
			
			//FileInputStream dataFile = new FileInputStream("/templates/"	+ templateName);
						
			//InputStreamReader isr = new InputStreamReader(dataFile);
			 //debug("getting buffered reader ");
			
			br = new BufferedReader(isr);


			while (br.ready()) {
				String line = br.readLine();

				// debug(".. " + line);

				String s = insertHTML(line, webFields, mode);
				out.println(s);
			}
			br.close();

		} catch (Exception ioe) {
			debug("FormDataWritter:write template error :: " + ioe.toString());
		}
		

		debug ("formDataWriter.writeTetmplate - done");

	}

	private String insertHTML(String line, Hashtable<String, WebField> webfields, String mode) {

		String newLine = new String(line);
		int max = 0;

		/*
		 * if the WebField is not found.. the TAG never get
		 */
		while ((newLine.indexOf("<KBD") > -1) && (max < 8)) {
			// debug(".. newline : " + newLine);
			newLine = replaceTag(newLine, webfields, mode);
			max++;
		}
		return newLine;

	}

	/*
	 * Look at an html line and replace <KBD ID='something'></KBD> 1. parse out the
	 * TAG id name 2. look in the ht for a WebField of that name 3. call the wf to
	 * get the hmtl (like a select box, input, or display value 4. replace the whole
	 * tag string with the html
	 */
	private String replaceTag(String line, Hashtable<String, WebField> webfields, String mode) {

		// * typical template line looks like :
		// *
		// * <tr><td>Some Title</td><td><KBD ID='fiend_name'></KBD></TD></TR>

		int firstChar = line.indexOf("<KBD") + 9; // the field name is 9
		// characters the start of the <KBD tag
		int lastChar = line.indexOf("'", firstChar + 2); // find the closing
		// '
		// quote after the field name
		if (lastChar == -1) {
			lastChar = line.indexOf("\"", firstChar + 2); // find the closing
			// "
		}
		// get out if can't parse out
		if (lastChar < firstChar) {
			return line;
		}
		String webId = line.substring(firstChar, lastChar); // now we have the
		// web tag name =
		// "field_name"

		//debug(" insrthtml .. found first cwhathar : " + firstChar + " last " + lastChar + " field : " + webId);

		WebField wf = webfields.get(webId); // field_name should be
		//debug(" wf type " + wf.fieldType);

		// in here somewhere.

		// some templates, like Security Checklist, have a bunch of lines that
		// get populated depending
		// on a sub data-type. the checklist has about 16 lines, but will have
		// less custom questions

		// note - were working backwards from the template into the ht.... maybe
		// we should work from
		// the ht into the tempplate and replace only whats in ht.

		if (wf == null) {
			//debug("relace tag failed...wf is null for : " + webId);
			return line; // carefull - the <KBD ID='field_name'></KBD>
			// sequence won't get parsed out
		} else {
			//debug("calling wf.getHTML");
			//debug("calling wf.getHTML for " + webId);
			;
			String html = wf.getHTML(mode);
			//debug("done getHTML");

			//debug(" js : " + html);
			return replaceMe(line, "<KBD ID='" + webId + "'></KBD>", html);
		}
	}

	/*
	 * TODO: Use Java 1.5 when available
	 */

	public String replaceMe(String original, String oldValue, String newValue) {

		int startPos;

		startPos = original.indexOf(oldValue); // find the start

		if (startPos == -1)
			return original;

		return new String(
				original.substring(0, startPos) + newValue + original.substring(startPos + oldValue.length()));

	}


}
