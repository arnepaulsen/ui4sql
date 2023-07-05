/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * 
 * 4/20/09 Convert to Spring bean
 * 
 * 
 *
 */

public class ObjectivePlugin extends AbsProjectPlugin {

	private static BeanFieldSelect b01 = new BeanFieldSelect("stakeholder_resp_id", Integer.valueOf("0"), "userHT");

	private static BeanFieldSelect b02 = new BeanFieldSelect("verified_by_id", Integer.valueOf("0"), "userHT");

	private static BeanFieldSelect b03 = new BeanFieldSelect("measure_cd", "", "OBJECTIVE");

	private static BeanFieldSelect b04 = new BeanFieldSelect("status_cd", "O", "STATUS");

	private static BeanFieldString b06 = new BeanFieldString("reference_nm", 32, 32);

	private static BeanFieldString b07 = new BeanFieldString("title_nm", 64, 64);

	private static BeanFieldDate b08 = new BeanFieldDate("realize_dt");

	private static BeanFieldDate b09 = new BeanFieldDate("verified_dt");

	private static BeanFieldText b11 = new BeanFieldText("desc_blob", 3, 80);

	private static BeanFieldText b12 = new BeanFieldText("success_blob", 3, 80);

	private static BeanFieldText b13 = new BeanFieldText("result_blob", 3, 80);

	private static BeanFieldCheckbox b14 = new BeanFieldCheckbox("confirmed_flag", "N", "");

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ObjectivePlugin() throws services.ServicesException {

		super();
		this.setTableName("tobjective");
		this.setKeyName("objective_id");
		this.setTargetTitle("Objective");

		this.setListHeaders(new String[] {  "Reference", "Title", "Type", "Status" });

		this.setListViewName("vobjective_list");
		this.setSelectViewName("vobjective");

		this.setWebFieldBeans(new BeanWebField[] { b01, b02, b03, b04, b06, b07, b08, b09, b11, b12, b13, b14 });

	}
}
