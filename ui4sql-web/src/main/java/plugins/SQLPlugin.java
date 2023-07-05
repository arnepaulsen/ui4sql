/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import java.io.*;
import services.SQLWriter;

/**
 * Division Area Plugin make a change
 */
public class SQLPlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	
	private String fileName;
	
	public SQLPlugin() throws services.ServicesException {
		super();
		this.setTableName("tsql");
		this.setKeyName("sql_id");
		this.setTargetTitle("Create SQL Changes");
		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction (true);

		this.setListHeaders( new String[] { "Project", "Status" });

		this.setMoreListColumns(new  String[] { "project_name", "code_desc" });
		this.setMoreListJoins(new  String[] {
				" left join tproject on tsql.project_id = tproject.project_id",
				" left join tcodes on tsql.status_cd = tcodes.code_value and tcodes.code_type_id  = 45 " });

		this.setUpdatesLevel("Administrator");
		
	}

	

	/*
	 * Write the RSS file after the update is complete
	 */

	public void beforeUpdate(Hashtable ht) {
		debug("afterUpdate... now sqlPlugin's turn");

		SQLWriter sql = new SQLWriter();

		fileName = sm.getServletContext().getRealPath(
		"/")
		+ "rss"
		+ File.separator
		+ "SQL.txt";
		
		debug("have sqlWriter... now getting file writer 1");
		try {
				
			debug ("RSS File Name : " + fileName);
			
			FileWriter fw = new FileWriter(fileName);

			debug("got file writer.. calling writeRSS");
			
			sql.writeSQL(sm.getDbInterface(), fw, new Integer("1"));

		}

		catch (IOException e) {
			debug("IOException getting file writer." + e.toString());
		}
		
	
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		WebFieldSelect wfProject = new WebFieldSelect("project_id",
				addMode ? new Integer("0") : (Integer) db
						.getObject("project_id"), sm.getProjectFilter());

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64);

		WebFieldSelect wfStatus = new WebFieldSelect("status_cd",
				addMode ? "New" : db.getText("status_cd"), sm
						.getCodes("ISSUESTAT"));
		
		WebFieldDisplay wfMessage = new WebFieldDisplay("message", "RSS File : " + fileName);

		WebField[] wfs = { wfProject, wfStatus, wfTitle, wfMessage };
		return webFieldsToHT(wfs);

	}

}