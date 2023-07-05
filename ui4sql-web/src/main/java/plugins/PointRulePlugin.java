/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import router.SessionMgr;
import forms.*;

/**
 * Division Area Plugin make a change
 */
public class PointRulePlugin extends AbsDivisionPlugin {

	// *******************
	// CONSTRUCTORS *
	// *******************

	public PointRulePlugin() throws services.ServicesException {
		super();
		this.setTableName("tpoint_rule");
		this.setKeyName("point_rule_id");
		this.setTargetTitle("Function Point Algorithm");

		this.setShowAuditSubmitApprove(false);
		this.setIsAdminFunction (true);

		this.setListHeaders( new String[] { "Agorithm Name" });

		this.setMoreListColumns(new  String[] { "title_nm" });
		
		this.setUpdatesOk(sm.userIsAdministrator());
		this.setSubmitOk(false);
		this.setDeleteOk(false);
		this.setUpdatesLevel("administrator");
		
	}

	
	
	
	
	
	public void afterAdd(Integer rowKey) throws services.ServicesException {
		sm.removeCache("tpoint_rule");
	}



	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		
		
	
		
		/*
		 * Strings
		 */
		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64));

		/*
		 * Floats Modules
		 */
		
		ht.put("module_chg_simple_flt", new WebFieldString(" module_chg_simple_flt", (addMode ? "0"
				: db.getText("module_chg_simple_flt")), 4, 4));

		ht.put("module_new_simple_flt", new WebFieldString(" module_new_simple_flt", (addMode ? "0"
				: db.getText("module_new_simple_flt")), 4, 4));
		
		ht.put("module_chg_medium_flt", new WebFieldString(" module_chg_medium_flt", (addMode ? "0"
				: db.getText("module_chg_medium_flt")), 4, 4));

		ht.put("module_new_medium_flt", new WebFieldString(" module_new_medium_flt", (addMode ? "0"
				: db.getText("module_new_medium_flt")), 4, 4));
		
		ht.put("module_chg_complex_flt", new WebFieldString(" module_chg_complex_flt", (addMode ? "0"
				: db.getText("module_chg_complex_flt")), 4, 4));

		ht.put("module_new_complex_flt", new WebFieldString(" module_new_complex_flt", (addMode ? "0"
				: db.getText("module_new_complex_flt")), 4, 4));
		
		/*
		 * Floats Interfaces
		 */
		
		ht.put("interface_chg_simple_flt", new WebFieldString(" interface_chg_simple_flt", (addMode ? "0"
				: db.getText("interface_chg_simple_flt")), 4, 4));

		ht.put("interface_new_simple_flt", new WebFieldString(" interface_new_simple_flt", (addMode ? "0"
				: db.getText("interface_new_simple_flt")), 4, 4));
		
		ht.put("interface_chg_medium_flt", new WebFieldString(" interface_chg_medium_flt", (addMode ? "0"
				: db.getText("interface_chg_medium_flt")), 4, 4));

		ht.put("interface_new_medium_flt", new WebFieldString(" interface_new_medium_flt", (addMode ? "0"
				: db.getText("interface_new_medium_flt")), 4, 4));
		
		ht.put("interface_chg_complex_flt", new WebFieldString(" interface_chg_complex_flt", (addMode ? "0"
				: db.getText("interface_chg_complex_flt")), 4, 4));

		ht.put("interface_new_complex_flt", new WebFieldString(" interface_new_complex_flt", (addMode ? "0"
				: db.getText("interface_new_complex_flt")), 4, 4));
		
		/*
		 * Floats Data Structures
		 */
		
		ht.put("data_chg_simple_flt", new WebFieldString(" data_chg_simple_flt", (addMode ? "0"
				: db.getText("data_chg_simple_flt")), 4, 4));

		ht.put("data_new_simple_flt", new WebFieldString(" data_new_simple_flt", (addMode ? "0"
				: db.getText("data_new_simple_flt")), 4, 4));
		
		ht.put("data_chg_medium_flt", new WebFieldString(" data_chg_medium_flt", (addMode ? "0"
				: db.getText("data_chg_medium_flt")), 4, 4));

		ht.put("data_new_medium_flt", new WebFieldString(" data_new_medium_flt", (addMode ? "0"
				: db.getText("data_new_medium_flt")), 4, 4));
		
		ht.put("data_chg_complex_flt", new WebFieldString(" data_chg_complex_flt", (addMode ? "0"
				: db.getText("data_chg_complex_flt")), 4, 4));

		ht.put("data_new_complex_flt", new WebFieldString(" data_new_complex_flt", (addMode ? "0"
				: db.getText("data_new_complex_flt")), 4, 4));
		
		
		/*
		 * Floats for User Interface
		 */
		
		ht.put("gui_chg_simple_flt", new WebFieldString(" gui_chg_simple_flt", (addMode ? "0"
				: db.getText("gui_chg_simple_flt")), 4, 4));

		ht.put("gui_new_simple_flt", new WebFieldString(" gui_new_simple_flt", (addMode ? "0"
				: db.getText("gui_new_simple_flt")), 4, 4));
		
		ht.put("gui_chg_medium_flt", new WebFieldString(" gui_chg_medium_flt", (addMode ? "0"
				: db.getText("gui_chg_medium_flt")), 4, 4));

		ht.put("gui_new_medium_flt", new WebFieldString(" gui_new_medium_flt", (addMode ? "0"
				: db.getText("gui_new_medium_flt")), 4, 4));
		
		ht.put("gui_chg_complex_flt", new WebFieldString(" gui_chg_complex_flt", (addMode ? "0"
				: db.getText("gui_chg_complex_flt")), 4, 4));

		ht.put("gui_new_complex_flt", new WebFieldString(" gui_new_complex_flt", (addMode ? "0"
				: db.getText("gui_new_complex_flt")), 4, 4));
		
		/*
		 * Floats for Reports
		 */
		
		ht.put("report_chg_simple_flt", new WebFieldString(" report_chg_simple_flt", (addMode ? "0"
				: db.getText("report_chg_simple_flt")), 4, 4));

		ht.put("report_new_simple_flt", new WebFieldString(" report_new_simple_flt", (addMode ? "0"
				: db.getText("report_new_simple_flt")), 4, 4));
		
		ht.put("report_chg_medium_flt", new WebFieldString(" report_chg_medium_flt", (addMode ? "0"
				: db.getText("report_chg_medium_flt")), 4, 4));

		ht.put("report_new_medium_flt", new WebFieldString(" report_new_medium_flt", (addMode ? "0"
				: db.getText("report_new_medium_flt")), 4, 4));
		
		ht.put("report_chg_complex_flt", new WebFieldString(" report_chg_complex_flt", (addMode ? "0"
				: db.getText("report_chg_complex_flt")), 4, 4));

		ht.put("report_new_complex_flt", new WebFieldString(" report_new_complex_flt", (addMode ? "0"
				: db.getText("report_new_complex_flt")), 4, 4));
		
		return ht;

	}}
