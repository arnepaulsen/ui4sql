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
 * Exposure list for a contrl 
 * 
 * Proof-of-concept for Spring XML Beans :
 * this plugin isn't used because it gets build from the Spring xml 
 * 
 */

public class ExposurePlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	private static BeanFieldString b01 = new BeanFieldString("loss_amt", 4,
			4);

	
	private static BeanFieldSelect b02 = new BeanFieldSelect("assign_to_uid",
			new Integer("0"), "userHT");

	private static BeanFieldString b03 = new BeanFieldString("reference_nm",
			12, 12);

	private static BeanFieldString b04 = new BeanFieldString("title_nm", 64, 64);

	private static BeanFieldDisplay b05 = new BeanFieldDisplay("expectedlossamt");

	private static BeanFieldString b06 = new BeanFieldString("probability_pct",
			4, 4);

	private static BeanFieldString b07 = new BeanFieldString("exposure_amt", 4,
			4);

	private static BeanFieldSelect b08 = new BeanFieldSelect("state_cd", "",
			"STATUS");

	private static BeanFieldSelect b09 = new BeanFieldSelect("type_cd", "",
			"EXPOSURETYPES");

	private static BeanFieldText b10 = new BeanFieldText("consequence_blob", 3,
			80);

	private static BeanFieldText b11 = new BeanFieldText("mitigation_blob", 3,
			80);

	private static BeanFieldText b12 = new BeanFieldText("contingency_blob", 3,
			80);

	private static BeanFieldText b13 = new BeanFieldText("desc_blob", 3, 80);

	public ExposurePlugin() throws services.ServicesException {

		super();

		this.setTableName("texposure");
		this.setKeyName("exposure_id");
		this.setTargetTitle("Exposures");

		this.setHasDetailForm(true);
		this.setDetailTarget("Control");
		this.setDetailTargetLabel("Controls");

		this.setListViewName("vlistexposure");
		this.setSelectViewName("vexposure");

		this.setListHeaders(new String[] { "Reference", "Title", "Type", "Amount (000s)",
				"Chance (%)", "Expected Loss", "Status" });

		this.setWebFieldBeans(new BeanWebField[] { b01, b02, b03, b04, b05,
				b06, b07, b08, b09, b10, b11, b12, b13 });

	}

}
