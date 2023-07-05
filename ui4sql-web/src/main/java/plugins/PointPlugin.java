/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import db.DbFieldInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Function Points
 * 
 * Table : tpoint
 * 
 * Allows function point analysis for a project/application combination.
 * 
 * Unique attributes of this plugin is that it applies a hard-coded points
 * algorithm to the entered values.
 * 
 * Totals can be entered for : - modules - interfaces - data structures
 * 
 * Each type has six two levels : - new, and individual counts for simple,
 * medium, complex - changed, and individual counts for simple, medium, complex
 * 
 * the point values are re-computed each time this page is displayed
 * 
 * a total point value is computed on add/update, and saved to the database.
 * Yes.. this is redundant data, but is helpful when computing function-points
 * exception in module services.ExceptionWriter
 * 
 * 
 * ToDo... allow external configuration of algorithm.
 * 
 * 
 * 
 * 
 * Change log:
 * 
 * 
 * 
 */
public class PointPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	/*
	 * Module factors
	 */

	public PointPlugin() throws services.ServicesException {
		super();
		this.setTableName("tpoint");
		this.setKeyName("point_id");
		this.setTargetTitle("Function Point Analysis");

		this.setListHeaders(new String[] { "Title", "Version", "Application",
				"Calcuation Rule", "Total" });
		this.setMoreListColumns(new String[] {
				"tpoint.title_nm as point_title", "tpoint.version_nm",
				"application_name", "tpoint_rule.title_nm as rule_name",
				"total_points_no" });
		this
				.setMoreListJoins(new String[] {
						" left join tpoint_rule on tpoint.point_rule_id = tpoint_rule.point_rule_id ",
						" left join tapplications on tpoint.application_id = tapplications.application_id " });

		this.setMoreSelectColumns(new String[] { " tpoint_rule.*" });

		this
				.setMoreSelectJoins(new String[] { " left join tpoint_rule on tpoint.point_rule_id = tpoint_rule.point_rule_id " });
	}

	/*
	 * These routines are used to get the total-points, and save it to
	 * 'total_points_no'
	 * 
	 */
	public boolean beforeAdd(Hashtable ht) {
		putPointTotal(ht);
		return true;
	}

	public void beforeUpdate(Hashtable ht) {
		putPointTotal(ht);
	}

	private void putPointTotal(Hashtable ht) {

		/*
		 * the true parameter causes the calcuators to get the values from the
		 * web page, not the db
		 */

		Integer rule_id;

		debug("PUT POINT.. action is " + sm.Parm("Action"));
		debug("web rule id " + sm.Parm("point_rule_id"));

		if (sm.Parm("Action").equalsIgnoreCase("show")) {
			rule_id = db.getInteger("point_rule_id");
		} else {
			rule_id = new Integer(sm.Parm("point_rule_id"));
		}

		try {
			ResultSet rs = db
					.getRS(" select * from tpoint_rule where point_rule_id = "
							+ rule_id.toString());

			while (rs.next() == true) {

				int moduleChangePoints = computeModuleChangePoints(true, rs);
				int moduleNewPoints = computeModuleNewPoints(true, rs);
				int moduleTotalPoints = moduleChangePoints + moduleNewPoints;

				int dataChangePoints = computeDataChangePoints(true, rs);
				int dataNewPoints = computeDataNewPoints(true, rs);
				int dataTotalPoints = dataChangePoints + dataNewPoints;

				int interfaceChangePoints = computeInterfaceChangePoints(true,
						rs);
				int interfaceNewPoints = computeInterfaceNewPoints(true, rs);
				int interfaceTotalPoints = interfaceChangePoints
						+ interfaceNewPoints;

				int guiChangePoints = computeGuiChangePoints(true, rs);
				int guiNewPoints = computeGuiNewPoints(true, rs);
				int guiTotalPoints = guiChangePoints + guiNewPoints;

				int reportChangePoints = computeReportChangePoints(true, rs);
				int reportNewPoints = computeReportNewPoints(true, rs);
				int reportTotalPoints = reportChangePoints + reportNewPoints;

				/*
				 * Grand total
				 */

				int totalPoints = moduleTotalPoints + dataTotalPoints
						+ interfaceTotalPoints + guiTotalPoints
						+ reportTotalPoints;

				ht.put("total_points_no", new DbFieldInteger("total_points_no",
						new Integer(totalPoints)));
			}

		}

		catch (SQLException s) {

		}

		catch (services.ServicesException s) {

		}

	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		Hashtable userHt = sm.getUserHT();

		if (parmMode.equalsIgnoreCase("show")) {
			displayTotals(ht);
		}

		/*
		 * Id's
		 */

		Hashtable rules = sm
				.getTable(
						"tpoint_rule",
						"select title_nm, point_rule_id, title_nm from tpoint_rule where division_id = "
								+ sm.getDivisionId().toString());

		ht.put("point_rule_id", new WebFieldSelect("point_rule_id",
				addMode ? new Integer("0") : db.getInteger("point_rule_id"),
				rules, "-Please pick a rule-"));

		ht.put("application_id", new WebFieldSelect("application_id",
				addMode ? sm.getProjectId() : (Integer) db
						.getObject("application_id"),
				sm.getApplicationFilter(), true));

		/*
		 * Codes
		 */

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("LIFECYCLE")));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		ht.put("version_nm", new WebFieldString("version_nm", (addMode ? ""
				: db.getText("version_nm")), 8, 8));

		/*
		 * Blobs
		 */

		ht.put("notes_blob", new WebFieldText("notes_blob", (addMode ? "" : db
				.getText("notes_blob")), 5, 80));

		/*
		 * Module counts
		 * 
		 */

		ht.put("module_chg_simple_qty", new WebFieldString(
				"module_chg_simple_qty", (addMode ? "" : db
						.getText("module_chg_simple_qty")), 3, 3));

		ht.put("module_chg_medium_qty", new WebFieldString(
				"module_chg_medium_qty", (addMode ? "" : db
						.getText("module_chg_medium_qty")), 3, 3));

		ht.put("module_chg_complex_qty", new WebFieldString(
				"module_chg_complex_qty", (addMode ? "" : db
						.getText("module_chg_complex_qty")), 3, 3));

		ht.put("module_new_simple_qty", new WebFieldString(
				"module_new_simple_qty", (addMode ? "" : db
						.getText("module_new_simple_qty")), 3, 3));

		ht.put("module_new_medium_qty", new WebFieldString(
				"module_new_medium_qty", (addMode ? "" : db
						.getText("module_new_medium_qty")), 3, 3));

		ht.put("module_new_complex_qty", new WebFieldString(
				"module_new_complex_qty", (addMode ? "" : db
						.getText("module_new_complex_qty")), 3, 3));

		/*
		 * data counts
		 */
		ht.put("data_chg_simple_qty", new WebFieldString("data_chg_simple_qty",
				(addMode ? "" : db.getText("data_chg_simple_qty")), 3, 3));

		ht.put("data_chg_medium_qty", new WebFieldString("data_chg_medium_qty",
				(addMode ? "" : db.getText("data_chg_medium_qty")), 3, 3));

		ht.put("data_chg_complex_qty", new WebFieldString(
				"data_chg_complex_qty", (addMode ? "" : db
						.getText("data_chg_complex_qty")), 3, 3));

		ht.put("data_new_simple_qty", new WebFieldString("data_new_simple_qty",
				(addMode ? "" : db.getText("data_new_simple_qty")), 3, 3));

		ht.put("data_new_medium_qty", new WebFieldString("data_new_medium_qty",
				(addMode ? "" : db.getText("data_new_medium_qty")), 3, 3));

		ht.put("data_new_complex_qty", new WebFieldString(
				"data_new_complex_qty", (addMode ? "" : db
						.getText("data_new_complex_qty")), 3, 3));

		/*
		 * interface counts
		 */
		ht.put("interface_chg_simple_qty", new WebFieldString(
				"interface_chg_simple_qty", (addMode ? "" : db
						.getText("interface_chg_simple_qty")), 3, 3));

		ht.put("interface_chg_medium_qty", new WebFieldString(
				"interface_chg_medium_qty", (addMode ? "" : db
						.getText("interface_chg_medium_qty")), 3, 3));

		ht.put("interface_chg_complex_qty", new WebFieldString(
				"interface_chg_complex_qty", (addMode ? "" : db
						.getText("interface_chg_complex_qty")), 3, 3));

		ht.put("interface_new_simple_qty", new WebFieldString(
				"interface_new_simple_qty", (addMode ? "" : db
						.getText("interface_new_simple_qty")), 3, 3));

		ht.put("interface_new_medium_qty", new WebFieldString(
				"interface_new_medium_qty", (addMode ? "" : db
						.getText("interface_new_medium_qty")), 3, 3));

		ht.put("interface_new_complex_qty", new WebFieldString(
				"interface_new_complex_qty", (addMode ? "" : db
						.getText("interface_new_complex_qty")), 3, 3));

		/*
		 * gui counts
		 */
		ht.put("gui_chg_simple_qty", new WebFieldString("gui_chg_simple_qty",
				(addMode ? "" : db.getText("gui_chg_simple_qty")), 3, 3));

		ht.put("gui_chg_medium_qty", new WebFieldString("gui_chg_medium_qty",
				(addMode ? "" : db.getText("gui_chg_medium_qty")), 3, 3));

		ht.put("gui_chg_complex_qty", new WebFieldString("gui_chg_complex_qty",
				(addMode ? "" : db.getText("gui_chg_complex_qty")), 3, 3));

		ht.put("gui_new_simple_qty", new WebFieldString("gui_new_simple_qty",
				(addMode ? "" : db.getText("gui_new_simple_qty")), 3, 3));

		ht.put("gui_new_medium_qty", new WebFieldString("gui_new_medium_qty",
				(addMode ? "" : db.getText("gui_new_medium_qty")), 3, 3));

		ht.put("gui_new_complex_qty", new WebFieldString("gui_new_complex_qty",
				(addMode ? "" : db.getText("gui_new_complex_qty")), 3, 3));

		/*
		 * report counts
		 */
		ht.put("report_chg_simple_qty", new WebFieldString(
				"report_chg_simple_qty", (addMode ? "" : db
						.getText("report_chg_simple_qty")), 3, 3));

		ht.put("report_chg_medium_qty", new WebFieldString(
				"report_chg_medium_qty", (addMode ? "" : db
						.getText("report_chg_medium_qty")), 3, 3));

		ht.put("report_chg_complex_qty", new WebFieldString(
				"report_chg_complex_qty", (addMode ? "" : db
						.getText("report_chg_complex_qty")), 3, 3));

		ht.put("report_new_simple_qty", new WebFieldString(
				"report_new_simple_qty", (addMode ? "" : db
						.getText("report_new_simple_qty")), 3, 3));

		ht.put("report_new_medium_qty", new WebFieldString(
				"report_new_medium_qty", (addMode ? "" : db
						.getText("report_new_medium_qty")), 3, 3));

		ht.put("report_new_complex_qty", new WebFieldString(
				"report_new_complex_qty", (addMode ? "" : db
						.getText("report_new_complex_qty")), 3, 3));

		/*
		 * Return
		 */

		return ht;

	}

	/*
	 * compute points for each type, and put out WebDisplayFields with results
	 * 
	 */
	private void displayTotals(Hashtable ht) {

		/*
		 * compute module points
		 */

		try {
			ResultSet rs = db
					.getRS(" select * from tpoint_rule where point_rule_id = "
							+ db.getInteger("point_rule_id").toString());

			while (rs.next() == true) {

				/*
				 * modules
				 */
				int moduleChangePoints = computeModuleChangePoints(false, rs);
				int moduleNewPoints = computeModuleNewPoints(false, rs);
				int moduleTotalPoints = moduleChangePoints + moduleNewPoints;

				/*
				 * Data structures (tables, files, etc.)
				 */
				int dataChangePoints = computeDataChangePoints(false, rs);
				int dataNewPoints = computeDataNewPoints(false, rs);
				int dataTotalPoints = dataChangePoints + dataNewPoints;

				/*
				 * Interfaces
				 */
				int interfaceChangePoints = computeInterfaceChangePoints(false,
						rs);
				int interfaceNewPoints = computeInterfaceNewPoints(false, rs);
				int interfaceTotalPoints = interfaceChangePoints
						+ interfaceNewPoints;

				/*
				 * Use Interfaces
				 */
				int guiChangePoints = computeGuiChangePoints(false, rs);
				int guiNewPoints = computeGuiNewPoints(false, rs);
				int guiTotalPoints = guiChangePoints + guiNewPoints;

				/*
				 * Reports
				 */
				int reportChangePoints = computeReportChangePoints(false, rs);
				int reportNewPoints = computeReportNewPoints(false, rs);
				int reportTotalPoints = reportChangePoints + reportNewPoints;

				int totalPoints = moduleTotalPoints + dataTotalPoints
						+ interfaceTotalPoints + guiTotalPoints
						+ reportTotalPoints;

				/*
				 * Grand Total
				 */
				ht.put("total_points_no", new DbFieldInteger("total_points_no",
						new Integer(totalPoints)));

				/*
				 * put module totals
				 */

				ht.put("module_chg_pts", new WebFieldDisplay("module_chg_pts",
						new Integer(moduleChangePoints).toString()));

				ht.put("module_new_pts", new WebFieldDisplay("module_new_pts",
						new Integer(moduleNewPoints).toString()));

				ht.put("module_tot_pts", new WebFieldDisplay("module_tot_pts",
						new Integer(moduleTotalPoints).toString()));

				/*
				 * put data totals
				 */

				ht.put("data_chg_pts", new WebFieldDisplay("data_chg_pts",
						new Integer(dataChangePoints).toString()));

				ht.put("data_new_pts", new WebFieldDisplay("data_new_pts",
						new Integer(dataNewPoints).toString()));

				ht.put("data_tot_pts", new WebFieldDisplay("data_tot_pts",
						new Integer(dataTotalPoints).toString()));

				/*
				 * put interface totals
				 */

				ht.put("interface_chg_pts", new WebFieldDisplay(
						"interface_chg_pts", new Integer(interfaceChangePoints)
								.toString()));

				ht.put("interface_new_pts", new WebFieldDisplay(
						"interface_new_pts", new Integer(interfaceNewPoints)
								.toString()));

				ht.put("interface_tot_pts", new WebFieldDisplay(
						"interface_tot_pts", new Integer(interfaceTotalPoints)
								.toString()));

				/*
				 * put user interface totals
				 */

				ht.put("gui_chg_pts", new WebFieldDisplay("gui_chg_pts",
						new Integer(guiChangePoints).toString()));

				ht.put("gui_new_pts", new WebFieldDisplay("gui_new_pts",
						new Integer(guiNewPoints).toString()));

				ht.put("gui_tot_pts", new WebFieldDisplay("gui_tot_pts",
						new Integer(guiTotalPoints).toString()));

				/*
				 * put reports totals
				 */

				ht.put("report_chg_pts", new WebFieldDisplay("report_chg_pts",
						new Integer(reportChangePoints).toString()));

				ht.put("report_new_pts", new WebFieldDisplay("report_new_pts",
						new Integer(reportNewPoints).toString()));

				ht.put("report_tot_pts", new WebFieldDisplay("report_tot_pts",
						new Integer(reportTotalPoints).toString()));

				/*
				 * Grand total
				 */

				debug("total points ...  : " + totalPoints);

				ht.put("tot_pts", new WebFieldDisplay("tot_pts", new Integer(
						totalPoints).toString()));
			}
		} catch (SQLException s) {
		}

		catch (services.ServicesException s) {

		}

	}

	/*
	 * get the values from web or database
	 */
	private int computeModuleChangePoints(boolean fromWeb, ResultSet rs) {
		debug("computing 1");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("module_chg_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("module_chg_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("module_chg_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("module_chg_simple_qty").intValue();
			i2 = db.getInteger("module_chg_medium_qty").intValue();
			i3 = db.getInteger("module_chg_complex_qty").intValue();
		}

		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		debug("getting floats 1a");

		try {
			f1 = f1 * rs.getFloat("module_chg_simple_flt");
			f2 = f2 * rs.getFloat("module_chg_medium_flt");
			f3 = f3 * rs.getFloat("module_chg_complex_flt");
			f1 = f1 + f2 + f3;

		} catch (SQLException s) {

		}
		Float f = new Float(f1);
		return f.intValue();

	}

	private int computeModuleNewPoints(boolean fromWeb, ResultSet rs) {

		debug("computing 2");
		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("module_new_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("module_new_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("module_new_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("module_new_simple_qty").intValue();
			i2 = db.getInteger("module_new_medium_qty").intValue();
			i3 = db.getInteger("module_new_complex_qty").intValue();

		}
		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {
			f1 = f1 * rs.getFloat("module_new_simple_flt");
			f2 = f2 * rs.getFloat("module_new_medium_flt");
			f3 = f3 * rs.getFloat("module_new_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException s) {

		}

		Float f = new Float(f1);
		return f.intValue();

	}

	private int computeDataChangePoints(boolean fromWeb, ResultSet rs) {

		debug("computing 3");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("data_chg_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("data_chg_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("data_chg_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("data_chg_simple_qty").intValue();
			i2 = db.getInteger("data_chg_medium_qty").intValue();
			i3 = db.getInteger("data_chg_complex_qty").intValue();

		}

		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {
			f1 = f1 * rs.getFloat("data_chg_simple_flt");
			f2 = f2 * rs.getFloat("data_chg_medium_flt");
			f3 = f3 * rs.getFloat("data_chg_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException e) {

		}

		Float f = new Float(f1);
		return f.intValue();

	}

	private int computeDataNewPoints(boolean fromWeb, ResultSet rs) {

		debug("computing 4");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("data_new_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("data_new_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("data_new_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("data_new_simple_qty").intValue();
			i2 = db.getInteger("data_new_medium_qty").intValue();
			i3 = db.getInteger("data_new_complex_qty").intValue();

		}

		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {
			f1 = f1 * rs.getFloat("data_new_simple_flt");
			f2 = f2 * rs.getFloat("data_new_medium_flt");
			f3 = f3 * rs.getFloat("data_new_complex_flt");
			f1 = f1 + f2 + f3;

		} catch (SQLException e) {

		}
		Float f = new Float(f1);
		return f.intValue();

	}

	private int computeInterfaceChangePoints(boolean fromWeb, ResultSet rs) {

		debug("computing 5");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("interface_chg_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("interface_chg_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("interface_chg_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("interface_chg_simple_qty").intValue();
			i2 = db.getInteger("interface_chg_medium_qty").intValue();
			i3 = db.getInteger("interface_chg_complex_qty").intValue();

		}
		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {

			f1 = f1 * rs.getFloat("interface_chg_simple_flt");
			f2 = f2 * rs.getFloat("interface_chg_medium_flt");
			f3 = f3 * rs.getFloat("interface_chg_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException e) {

		}
		Float f = new Float(f1);
		return f.intValue();
	}

	private int computeInterfaceNewPoints(boolean fromWeb, ResultSet rs) {

		debug("computing 6");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("interface_new_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("interface_new_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("interface_new_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("interface_new_simple_qty").intValue();
			i2 = db.getInteger("interface_new_medium_qty").intValue();
			i3 = db.getInteger("interface_new_complex_qty").intValue();

		}

		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {

			f1 = f1 * rs.getFloat("interface_new_simple_flt");
			f2 = f2 * rs.getFloat("interface_new_medium_flt");
			f3 = f3 * rs.getFloat("interface_new_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException e) {

		}
		debug("done computig new Interface points");

		Float f = new Float(f1);
		return f.intValue();

	}

	private int computeReportChangePoints(boolean fromWeb, ResultSet rs) {

		debug("computing 7");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("report_chg_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("report_chg_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("report_chg_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("report_chg_simple_qty").intValue();
			i2 = db.getInteger("report_chg_medium_qty").intValue();
			i3 = db.getInteger("report_chg_complex_qty").intValue();

		}
		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {

			f1 = f1 * rs.getFloat("report_chg_simple_flt");
			f2 = f2 * rs.getFloat("report_chg_medium_flt");
			f3 = f3 * rs.getFloat("report_chg_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException e) {

		}
		Float f = new Float(f1);
		return f.intValue();
	}

	private int computeReportNewPoints(boolean fromWeb, ResultSet rs) {

		debug("computing 8");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("report_new_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("report_new_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("report_new_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("report_new_simple_qty").intValue();
			i2 = db.getInteger("report_new_medium_qty").intValue();
			i3 = db.getInteger("report_new_complex_qty").intValue();

		}

		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {

			f1 = f1 * rs.getFloat("report_new_simple_flt");
			f2 = f2 * rs.getFloat("report_new_medium_flt");
			f3 = f3 * rs.getFloat("report_new_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException e) {

		}
		debug("done computig new Interface points");

		Float f = new Float(f1);
		return f.intValue();

	}

	private int computeGuiChangePoints(boolean fromWeb, ResultSet rs) {

		debug("computing 7");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("gui_chg_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("gui_chg_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("gui_chg_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("gui_chg_simple_qty").intValue();
			i2 = db.getInteger("gui_chg_medium_qty").intValue();
			i3 = db.getInteger("gui_chg_complex_qty").intValue();

		}
		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {

			f1 = f1 * rs.getFloat("gui_chg_simple_flt");
			f2 = f2 * rs.getFloat("gui_chg_medium_flt");
			f3 = f3 * rs.getFloat("gui_chg_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException e) {

		}
		Float f = new Float(f1);
		return f.intValue();
	}

	private int computeGuiNewPoints(boolean fromWeb, ResultSet rs) {

		debug("computing 8");

		int i1 = 0;
		int i2 = 0;
		int i3 = 0;

		if (fromWeb) {
			i1 = new Integer(sm.Parm("gui_new_simple_qty")).intValue();
			i2 = new Integer(sm.Parm("gui_new_medium_qty")).intValue();
			i3 = new Integer(sm.Parm("gui_new_complex_qty")).intValue();

		} else {
			i1 = db.getInteger("gui_new_simple_qty").intValue();
			i2 = db.getInteger("gui_new_medium_qty").intValue();
			i3 = db.getInteger("gui_new_complex_qty").intValue();

		}

		float f1 = i1;
		float f2 = i2;
		float f3 = i3;

		try {

			f1 = f1 * rs.getFloat("gui_new_simple_flt");
			f2 = f2 * rs.getFloat("gui_new_medium_flt");
			f3 = f3 * rs.getFloat("gui_new_complex_flt");
			f1 = f1 + f2 + f3;
		} catch (SQLException e) {

		}
		debug("done computig new Interface points");

		Float f = new Float(f1);
		return f.intValue();

	}

}
