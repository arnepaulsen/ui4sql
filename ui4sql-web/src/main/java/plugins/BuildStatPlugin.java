/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;
import java.util.Hashtable;

import services.ExcelWriter;
import forms.*;

/**
 * Build Status
 * 
 * 		this is a work-in-progress to manually track build statistics/metrics
 * 
 * Change Log:
 * 
 * 3/13 as 'target' to list query
 * 
 * 6/13/10 - change to standard view names
 */

public class BuildStatPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public BuildStatPlugin() throws services.ServicesException {
		super();
		this.setTableName("tbuild_stat");
		this.setKeyName("build_stat_id");
		this.setTargetTitle("Build Statistics");

		this.setSubmitOk(false);
		this.setCopyOk(false);
		this.setExcelOk(true);
		this.setShowAuditSubmitApprove(false);
		
		this.setListViewName("vbuildstat_list");
		this.setSelectViewName("vbuildstat");
		
		this.setListOrder("suite_cd");
		
		this.setListHeaders(new String[] { "Year", "Trigger", "Suite", "Jan",
				"Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
				"Nov", "Dec", "Total" });
		
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable();

		/*
		 * codes
		 */

		ht.put("suite_cd", new WebFieldSelect("suite_cd", addMode ? "" : db
				.getText("suite_cd"), sm.getCodes("SUITESRIP")));

		ht.put("trigger_cd", new WebFieldSelect("trigger_cd", addMode ? "" : db
				.getText("trigger_cd"), sm.getCodes("TRIGGER")));

		ht.put("year_no", new WebFieldString("year_no", addMode ? "" : db
				.getText("year_no"), 4, 4));

		
		/*
		 * numbers
		 */

	
		ht.put("hd_jan_no", new WebFieldString("hd_jan_no", addMode ? ""
				: db.getText("hd_jan_no"), 3, 3));
		

		ht.put("hd_feb_no", new WebFieldString("hd_feb_no", addMode ? ""
				: db.getText("hd_feb_no"), 3, 3));
		
		ht.put("hd_mar_no", new WebFieldString("hd_mar_no", addMode ? ""
				: db.getText("hd_mar_no"), 3, 3));
		
		ht.put("hd_apr_no", new WebFieldString("hd_apr_no", addMode ? ""
				: db.getText("hd_apr_no"), 3, 3));
		ht.put("hd_may_no", new WebFieldString("hd_may_no", addMode ? ""
				: db.getText("hd_may_no"), 3, 3));
		
		ht.put("hd_jun_no", new WebFieldString("hd_jun_no", addMode ? ""
				: db.getText("hd_jun_no"), 3, 3));
		
		ht.put("hd_jul_no", new WebFieldString("hd_jul_no", addMode ? ""
				: db.getText("hd_jul_no"), 3, 3));
		
		ht.put("hd_aug_no", new WebFieldString("hd_aug_no", addMode ? ""
				: db.getText("hd_aug_no"), 3, 3));
		
		ht.put("hd_sep_no", new WebFieldString("hd_sep_no", addMode ? ""
				: db.getText("hd_sep_no"), 3, 3));
		
		ht.put("hd_oct_no", new WebFieldString("hd_oct_no", addMode ? ""
				: db.getText("hd_oct_no"), 3, 3));
		
		ht.put("hd_nov_no", new WebFieldString("hd_nov_no", addMode ? ""
				: db.getText("hd_nov_no"), 3, 3));
		
		ht.put("hd_dec_no", new WebFieldString("hd_dec_no", addMode ? ""
				: db.getText("hd_dec_no"), 3, 3));
		
	
	//	ht.put("yeartotal", new WebFieldDisplay("yeartotal", addMode ? ""
		//		: db.getText("total_no")));

		
		/*
		 * blobs
		 */
	//	ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
		//		.getText("notes_blob"), 3, 60));

		return ht;

	}
	
	
	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "BuildStat.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName() + "_Build_Statistic_";
		int columns = 16;
		short startRow = 5;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);

	}
	
	
	public ResultSet getExcelResultSet() {

		StringBuffer query = new StringBuffer();

		query.append("SELECT * FROM vbuildstat_excel WHERE 1=1 ");

		query.append(" order by suite_cd");
		ResultSet rs = null;

		try {
			rs = db.getRS(query.toString());
		} catch (services.ServicesException e) {
			debug("Excel RS Fetch Error : " + e.toString());
		}
		return rs;

	}
	
	
	
	
}
