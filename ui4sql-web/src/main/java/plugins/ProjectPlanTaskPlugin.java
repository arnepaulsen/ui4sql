/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import services.ExcelWriter;

import java.sql.ResultSet;
import java.util.Hashtable;

import org.apache.poi.hssf.util.HSSFColor;

import db.DbFieldInteger;
import db.DbFieldString;

import router.SessionMgr;
import forms.*;

/**
 * Exposure Data Manager 3/23 new
 * 
 */

public class ProjectPlanTaskPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ProjectPlanTaskPlugin() throws services.ServicesException {
		super();
		this.setTableName("tproject_plan_task");
		this.setKeyName("task_id");
		this.setTargetTitle("Tasks");
		this.setEditOk(true);

		this.setIsDetailForm(true);
		this.setIsStepChild (true);
		this.setParentTarget ("ProjectPlan");
		

		this.setListHeaders( new String[] { "Outline", "Level", "Title", "%Done",
				"Start", "End", "Owner" });

	
	}

	

	public void init(SessionMgr parmSm) {

		super.init(parmSm);
		
		this.setMoreListColumns(new  String[] {
				"outline_nm",
				"level_no",
				"tproject_plan_task.title_nm",
				"complete_pct_no",
				dbprefix + "FormatDateTime(tproject_plan_task.start_date, 'MM/DD/YY') as end_dt",
				dbprefix + "FormatDateTime(tproject_plan_task.end_date, 'MM/DD/YY') as start_dt",
				"tproject_plan_task.owner_nm" });

		this.setMoreListJoins(new  String[] {
				" left join tcodes on tproject_plan_task.status_cd = tcodes.code_value and tcodes.code_type_id  = 5 ",
				" left join tproject_plan p on tproject_plan_task.project_plan_id = p.project_plan_id" });

		this.setMoreSelectJoins (new String[] {
				" left join tcodes on tproject_plan_task.status_cd = tcodes.code_value and tcodes.code_type_id  = 5 ",
				" left join tproject_plan p on tproject_plan_task.project_plan_id = p.project_plan_id",
				" join tproject on p.project_id = tproject.project_id " });

		this.setMoreSelectColumns (new String[] { "tproject.project_name",
				"tproject.project_id", "p.title_nm as proj_plan" });

	
	
	}
	
	/*
	 * 
	 * Set Parent Key on Add
	 * 
	 */


	public boolean beforeAdd(Hashtable ht) {
		ht.put("project_plan_id", new DbFieldInteger("project_plan_id",
				new Integer(sm.getParentId())));
		return true;

	}

	public boolean getListColumnCenterOn(int c) {
		if (c == 3) {
			return true;
		}

		return false;
	}

	public boolean listColumnHasSelector(int columnNumber) {
		// the deliverable id in column 1
		if (columnNumber == 0)
			// true causes getListSelector to be called for this column.
			return false;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		String query = new String(
				"select title_nm, exposure_id, title_nm from texposure where application_id = "
						+ sm.getApplicationId().toString());
		Hashtable exposures = sm.getTable("texposure", query);

		// * filter on exposure id
		WebFieldSelect wf = new WebFieldSelect("FilterExposure", (sm.Parm(
				"FilterExposure").length() == 0 ? new Integer("0")
				: new Integer(sm.Parm("FilterExposure"))), exposures,
				"-All Exposures-");
		wf.setDisplayClass("listform");
		return wf;

	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		sb.append(" AND tproject_plan_task.project_plan_id = "
				+ sm.getParentId());

		if ((sm.Parm("FilterExposure").length() > 0)
				&& (!sm.Parm("FilterExposure").equalsIgnoreCase("0"))) {
			sb.append(" AND e.exposure_id= " + sm.Parm("FilterExposure"));
		}

		return sb.toString();

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		ht.put("planName", new WebFieldDisplay("planName", sm
				.getParentName()));

		ht.put("title_nm", new WebFieldString("title_nm", addMode ? "" : db
				.getText("title_nm"), 64, 64));

		ht.put("seq_no", new WebFieldString("seq_no", addMode ? "" : db
				.getText("seq_no"), 6, 6));

		/*
		 * Ids
		 */

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));

		/*
		 * Strings
		 */

		ht.put("reference_nm", new WebFieldString("reference_nm", addMode ? ""
				: db.getText("reference_nm"), 12, 12));

		ht.put("title_nm", new WebFieldString("title_nm", addMode ? "" : db
				.getText("title_nm"), 64, 64));

		ht.put("owner_nm", new WebFieldString("owner_nm", addMode ? "" : db
				.getText("owner_nm"), 64, 64));

		ht.put("outline_nm", new WebFieldString("outline_nm", addMode ? "" : db
				.getText("outline_nm"), 16, 16));

		/*
		 * Numbers
		 */

		ht.put("complete_pct_no", new WebFieldString("complete_pct_no",
				addMode ? "" : db.getText("complete_pct_no"), 4, 4));

		/*
		 * Dates
		 */

		ht.put("start_date", new WebFieldDateTime("start_date", (addMode ? ""
				: db.getText("start_date"))));

		ht.put("end_date", new WebFieldDateTime("end_date", (addMode ? "" : db
				.getText("end_date"))));

		/*
		 * Codes
		 */

		ht.put("level_no", new WebFieldSelect("level_no", addMode ? "1" : db
				.getText("level_no"), sm.getCodes("RANK10")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("STATUS")));

		/*
		 * Blobs
		 */

		debug("Task : blobs");

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 5, 100));

		return ht;

	}

	/*
	 * EXCEL
	 */

	// create Excel from ResultSet and save to the path in the web.xml config
	// file
	public String makeExcelFile() {

		ExcelWriter excel = new ExcelWriter();

		String templateName = "orderset.xls";
		String templatePath = sm.getWebRoot() + "excel/" + templateName;
		String filePrefix = sm.getLastName().replace(" ", "") + "_PLAN";
		int columns = 6;
		short startRow = 1;

		return excel.appendWorkbook(sm.getExcelPath(), templatePath,
				filePrefix, getExcelResultSet(), startRow, columns);


	}

	public ResultSet getExcelResultSet() {

		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM vexcelprojecttask WHERE 1=1 ");

		// filter review date
		
		if (sm.Parm("FilterReview").length() > 0) {
			if (!sm.Parm("FilterReview").equalsIgnoreCase("0")) {
				sb.append(" AND review_date = '" + sm.Parm("FilterReview")
						+ "'");
			}
		}
		sb.append(" ORDER BY Outline");

		ResultSet rs = null;

		try {
			rs = db.getRS(sb.toString());
		} catch (services.ServicesException e) {
		}
		return rs;

	}

}
