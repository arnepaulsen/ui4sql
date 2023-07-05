/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import router.SessionMgr;
import forms.*;

import java.util.Hashtable;
import java.util.Date;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import services.ExcelWriter;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * 
 * Just display a list of log entries
 * 
 * 
 * 
 */
public class LogPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructors
	 * 
	 **************************************************************************/

	private static String accessQuery = "SELECT DISTINCT concat(last_name, ', ', first_name) as odor, tuser.user_id, concat(last_name , ',', first_name) from tlog "
			+ " LEFT JOIN tuser on tlog.access_uid = tuser.user_id where user_id is not null ; ";

	// 99 = code word for userId

	private BeanFieldSelect filterAccess = new BeanFieldSelect(0,
			"FilterUser", "access_uid", 0, 0, "User ?", "SQL",
			accessQuery);
	

	public LogPlugin() throws services.ServicesException {
		super();
		this.setTableName("tlog");
		this.setKeyName("log_id");
		this.setTargetTitle("Access Log");
		this.setListOrder("user_name");
		this.setListViewName("vlog_list");
		this.setShowAuditSubmitApprove(false);
		this.setEditOk(false);
		this.setExcelOk(false);
		this.setListOnly(true);
		this.setIsAdminFunction(true);

		this.setListHeaders(new String[] { "User", "Form", "Access", "Date", "ID", "Topic" });
		
		this.setListFilters(new BeanWebField[] { filterAccess });
		
		

	}


}
