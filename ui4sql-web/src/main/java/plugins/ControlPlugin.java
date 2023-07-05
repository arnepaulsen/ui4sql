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
 * Exposure Data Manager 3/23 new
 * 
 * Change Log:
 * 
 * 	6/14/10 Convert to standard view names 
 * 
 */

public class ControlPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private static String query = "select title_nm, exposure_id, title_nm from texposure where application_id = !APPLICATIONID!";

	private static BeanFieldSelect filterExposure = new BeanFieldSelect(2, "FilterExposure",
			"exposure_id", "", "", "User?", query);

	private static BeanFieldSelect b01 = new BeanFieldSelect("exposure_id",
			new Integer("0"), query);

	
	private static BeanFieldString b02 = new BeanFieldString("reference_nm", 8,
			8);

	private static BeanFieldString b03 = new BeanFieldString("title_nm", 64, 64);

	private static BeanFieldString b04 = new BeanFieldString("effective_pct",
			4, 4);

	private static BeanFieldDisplay b05 = new BeanFieldDisplay("msg",
			"Please  select a plan type, then Save-Edit.");

	private static BeanFieldDisplay b07 = new BeanFieldDisplay(
			"expectedlossamt");

	private static BeanFieldDisplay b08 = new BeanFieldDisplay(
			"probability_pct");

	private static BeanFieldDisplay b09 = new BeanFieldDisplay("effective_amt");

	private static BeanFieldDisplay b10 = new BeanFieldDisplay("exposure_amt");

	private static BeanFieldSelect b11 = new BeanFieldSelect("assign_to_uid",
			new Integer("0"), "userHT");

	private static BeanFieldString b12 = new BeanFieldString("reference_nm",
			12, 12);

	private static BeanFieldString b13 = new BeanFieldString("title_nm", 64, 64);

	private static BeanFieldSelect b14 = new BeanFieldSelect("state_cd", "",
			"STATUS");

	private static BeanFieldSelect b15 = new BeanFieldSelect("type_cd", "",
			"CONTROLTYPE");

	private static BeanFieldText b16 = new BeanFieldText("consequence_blob", 3,
			80);

	private static BeanFieldText b17 = new BeanFieldText("mitigation_blob", 3,
			80);

	private static BeanFieldText b18 = new BeanFieldText("contingency_blob", 3,
			80);

	private static BeanFieldText b19 = new BeanFieldText("effectiveness_blob",
			3, 80);

	private static BeanFieldText b20 = new BeanFieldText("desc_blob", 3, 80);

	public ControlPlugin() throws services.ServicesException {
		super();
		this.setTableName("tcontrol");
		this.setKeyName("control_id");
		this.setTargetTitle("Controls");

		this.setIsDetailForm(true);
		this.setParentTarget("Exposure");
		this.setListViewName("vcontrol_list");
		this.setSelectViewName("vcontrol");

		this.setListFilters(new BeanFieldSelect[] { filterExposure });

		this.setListHeaders(new String[] { "Ref#", "Control Title", "Exposure",
				"Exposure $", "Effective %", "Value $" });

		this.setWebFieldBeans(new BeanWebField[] { b01, b02, b03, b04, b05,
				b07, b08, b09, b10, b11, b12, b13, b14, b15, b16, b17, b18,
				b19, b20 });

		return;
	}

}
