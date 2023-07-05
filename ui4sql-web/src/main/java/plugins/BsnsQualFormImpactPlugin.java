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
 * 2/15 added mySql 3/10 use tcodes
 */

public class BsnsQualFormImpactPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public BsnsQualFormImpactPlugin() throws services.ServicesException {

		super();
		this.setTableName("tbqf");
		this.setKeyName("bqf_id");
		this.setTargetTitle("BQF - Impact Analysis");

		this.setAddOk(false);

		this.setSubmitOk(false);
		this.setCopyOk(false);
		this.setNextOk(false);
		this.setDeleteOk(false);
		this.setListOk(false);
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

		Hashtable htProducts = sm.getTable("tproduct",
				"select title_nm, product_id, title_nm from tproduct where division_id = "
						+ sm.getDivisionId().toString()
						+ " order by group_cd, title_nm");

		/*
		 * link to Impact BQF Form
		 */
		String href = new String("");

		if (parmMode.equalsIgnoreCase("show")) {

			href = "<A href=Router?Target=BsnsQualForm&Action=Show&Relation=this&RowKey="
					+ db.getText("bqf_id") + ">CB Impact BQF</A>";
		}

		ht.put("bqflink", new WebFieldDisplay("bqflink", href));

		/*
		 * Codes
		 */

		String[][] types = { { "CODE", "CONF", "DATA", "FIX", "DEC" },
				{ "Code", "Configuration", "Data", "Fix", "Decision" } };

		String[][] sources = { { "B", "P" }, { "Build", "Production" } };

		String[][] cb_decions = { { "A", "D", "M" },
				{ "Approved", "Denied", "More Analysis Needed" } };

		String[][] dispositions = { { "A", "D" }, { "Accept", "Demy" } };

		String[][] agrees = { { "A", "D", "E", "NA" },
				{ "Agree", "Disagree", "Neutral", "Not Impacted" } };

		String[][] impacts = {
				{ "COMP", "Leg", "IC", "PHY", "CLIN", "ANC", "OTH" },
				{ "Compliance", "Legal", "Information Counsil",
						"National Physicians", "Clinical Content",
						"Ancilliaries", "Other" } };

		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), types, true));

		ht.put("impact_cd", new WebFieldSelect("impact_cd", addMode ? "" : db
				.getText("impact_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("loe_cd", new WebFieldSelect("loe_cd", addMode ? "" : db
				.getText("loe_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("source_cd", new WebFieldSelect("source_cd", addMode ? "" : db
				.getText("source_cd"), sources, true));

		ht.put("recommend_cd", new WebFieldSelect("recommend_cd", addMode ? ""
				: db.getText("recommend_cd"), dispositions));

		ht.put("cb_decision_cd", new WebFieldSelect("cb_decision_cd",
				addMode ? "" : db.getText("cb_decision_cd"), cb_decions));

		ht.put("prod_1_impact_cd", new WebFieldSelect("prod_1_impact_cd",
				addMode ? "" : db.getText("prod_1_impact_cd"), sm
						.getCodes("EPICPRODS"), true, true));

		ht.put("prod_2_impact_cd", new WebFieldSelect("prod_2_impact_cd",
				addMode ? "" : db.getText("prod_2_impact_cd"), sm
						.getCodes("EPICPRODS"), " "));

		ht.put("prod_3_impact_cd", new WebFieldSelect("prod_3_impact_cd",
				addMode ? "" : db.getText("prod_3_impact_cd"), sm
						.getCodes("EPICPRODS"), " "));

		ht.put("prod_4_impact_cd", new WebFieldSelect("prod_4_impact_cd",
				addMode ? "" : db.getText("prod_4_impact_cd"), sm
						.getCodes("EPICPRODS"), " "));

		ht.put("colorado_resp_cd", new WebFieldSelect("colorado_resp_cd",
				addMode ? "" : db.getText("colorado_resp_cd"), agrees));

		ht.put("hawaii_resp_cd", new WebFieldSelect("hawaii_resp_cd",
				addMode ? "" : db.getText("hawaii_resp_cd"), agrees));

		ht.put("georgia_resp_cd", new WebFieldSelect("georgia_resp_cd",
				addMode ? "" : db.getText("georgia_resp_cd"), agrees));

		ht.put("ma_resp_cd", new WebFieldSelect("ma_resp_cd", addMode ? "" : db
				.getText("ma_resp_cd"), agrees));

		ht.put("ncal_resp_cd", new WebFieldSelect("ncal_resp_cd", addMode ? ""
				: db.getText("ncal_resp_cd"), agrees));

		ht.put("nw_resp_cd", new WebFieldSelect("nw_resp_cd", addMode ? "" : db
				.getText("nw_resp_cd"), agrees));

		ht.put("ohio_resp_cd", new WebFieldSelect("ohio_resp_cd", addMode ? ""
				: db.getText("ohio_resp_cd"), agrees));

		ht.put("scal_resp_cd", new WebFieldSelect("scal_resp_cd", addMode ? ""
				: db.getText("scal_resp_cd"), agrees));

		/*
		 * sme input
		 */

		ht.put("sme_1_impact_cd", new WebFieldSelect("sme_1_impact_cd",
				addMode ? "Y" : db.getText("sme_1_impact_cd"), sm
						.getCodes("HIGHMEDLOW")));
		ht.put("sme_2_impact_cd", new WebFieldSelect("sme_2_impact_cd",
				addMode ? "Y" : db.getText("sme_2_impact_cd"), sm
						.getCodes("HIGHMEDLOW")));
		ht.put("sme_3_impact_cd", new WebFieldSelect("sme_3_impact_cd",
				addMode ? "Y" : db.getText("sme_3_impact_cd"), sm
						.getCodes("HIGHMEDLOW")));
		ht.put("sme_4_impact_cd", new WebFieldSelect("sme_4_impact_cd",
				addMode ? "Y" : db.getText("sme_4_impact_cd"), sm
						.getCodes("HIGHMEDLOW")));

		ht.put("sme_1_type_cd", new WebFieldSelect("sme_1_type_cd",
				addMode ? "Y" : db.getText("sme_1_type_cd"), impacts));
		ht.put("sme_2_type_cd", new WebFieldSelect("sme_2_type_cd",
				addMode ? "Y" : db.getText("sme_2_type_cd"), impacts));
		ht.put("sme_3_type_cd", new WebFieldSelect("sme_3_type_cd",
				addMode ? "Y" : db.getText("sme_3_type_cd"), impacts));
		ht.put("sme_4_type_cd", new WebFieldSelect("sme_4_type_cd",
				addMode ? "Y" : db.getText("sme_4_type_cd"), impacts));

		/*
		 * Display only
		 */

		ht.put("rfc_no", new WebFieldDisplay("rfc_no", (addMode ? "" : db
				.getText("rfc_no"))));

		ht.put("regions_tx", new WebFieldDisplay("regions_tx", (addMode ? ""
				: db.getText("regions_tx"))));

		ht.put("title_nm", new WebFieldDisplay("title_nm", (addMode ? "" : db
				.getText("title_nm"))));

		ht.put("natl_ac_nm", new WebFieldDisplay("natl_ac_nm", (addMode ? ""
				: db.getText("natl_ac_nm"))));

		/*
		 * Strings
		 */

		ht.put("recommend_tx", new WebFieldString("recommend_tx", (addMode ? ""
				: db.getText("recommend_tx")), 64, 64));

		ht.put("cb_decision_tx", new WebFieldString("cb_decision_tx",
				(addMode ? "" : db.getText("cb_decision_tx")), 64, 64));

		ht.put("cb_decision_tx", new WebFieldString("cb_decision_tx",
				(addMode ? "" : db.getText("cb_decision_tx")), 64, 64));

		ht.put("colorado_assesor_nm", new WebFieldString("colorado_assesor_nm",
				(addMode ? "" : db.getText("colorado_assesor_nm")), 32, 32));
		ht.put("hawaii_assesor_nm", new WebFieldString("hawaii_assesor_nm",
				(addMode ? "" : db.getText("hawaii_assesor_nm")), 32, 32));
		ht.put("georgia_assesor_nm", new WebFieldString("georgia_assesor_nm",
				(addMode ? "" : db.getText("georgia_assesor_nm")), 32, 32));
		ht.put("nw_assesor_nm", new WebFieldString("nw_assesor_nm",
				(addMode ? "" : db.getText("nw_assesor_nm")), 32, 32));
		ht.put("ncal_assesor_nm", new WebFieldString("ncal_assesor_nm",
				(addMode ? "" : db.getText("ncal_assesor_nm")), 32, 32));
		ht.put("ohio_assesor_nm", new WebFieldString("ohio_assesor_nm",
				(addMode ? "" : db.getText("ohio_assesor_nm")), 32, 32));
		ht.put("scal_assesor_nm", new WebFieldString("scal_assesor_nm",
				(addMode ? "" : db.getText("scal_assesor_nm")), 32, 32));
		ht.put("ma_assesor_nm", new WebFieldString("ma_assesor_nm",
				(addMode ? "" : db.getText("ma_assesor_nm")), 32, 32));

		ht.put("ma_resp_tx", new WebFieldString("ma_resp_tx", (addMode ? ""
				: db.getText("ma_resp_tx")), 128, 128));
		ht.put("colorado_resp_tx", new WebFieldString("colorado_resp_tx",
				(addMode ? "" : db.getText("colorado_resp_tx")), 128, 128));
		ht.put("georgia_resp_tx", new WebFieldString("georgia_resp_tx",
				(addMode ? "" : db.getText("georgia_resp_tx")), 128, 128));
		ht.put("hawaii_resp_tx", new WebFieldString("hawaii_resp_tx",
				(addMode ? "" : db.getText("hawaii_resp_tx")), 128, 128));
		ht.put("nw_resp_tx", new WebFieldString("nw_resp_tx", (addMode ? ""
				: db.getText("nw_resp_tx")), 128, 128));
		ht.put("ohio_resp_tx", new WebFieldString("ohio_resp_tx", (addMode ? ""
				: db.getText("ohio_resp_tx")), 128, 128));
		ht.put("ncal_resp_tx", new WebFieldString("ncal_resp_tx", (addMode ? ""
				: db.getText("ncal_resp_tx")), 128, 128));
		ht.put("scal_resp_tx", new WebFieldString("scal_resp_tx", (addMode ? ""
				: db.getText("scal_resp_tx")), 128, 128));

		ht.put("sme_1_nm", new WebFieldString("sme_1_nm", (addMode ? "" : db
				.getText("sme_1_nm")), 32, 32));
		ht.put("sme_2_nm", new WebFieldString("sme_2_nm", (addMode ? "" : db
				.getText("sme_2_nm")), 32, 32));
		ht.put("sme_3_nm", new WebFieldString("sme_3_nm", (addMode ? "" : db
				.getText("sme_3_nm")), 32, 32));
		ht.put("sme_4_nm", new WebFieldString("sme_4_nm", (addMode ? "" : db
				.getText("sme_4_nm")), 32, 32));

		ht.put("sme_1_impact_tx", new WebFieldString("sme_1_impact_tx",
				(addMode ? "" : db.getText("sme_1_impact_tx")), 64, 128));
		ht.put("sme_2_impact_tx", new WebFieldString("sme_2_impact_tx",
				(addMode ? "" : db.getText("sme_2_impact_tx")), 64, 128));
		ht.put("sme_3_impact_tx", new WebFieldString("sme_3_impact_tx",
				(addMode ? "" : db.getText("sme_3_impact_tx")), 64, 128));
		ht.put("sme_4_impact_tx", new WebFieldString("sme_4_impact_tx",
				(addMode ? "" : db.getText("sme_4_impact_tx")), 64, 128));

		/*
		 * Dates
		 */

		ht.put("decision_dt", new WebFieldDate("decision_dt", addMode ? "" : db
				.getText("decision_dt")));

		/*
		 * Blobs
		 */

		ht.put("rejection_blob", new WebFieldText("rejection_blob",
				addMode ? "" : db.getText("rejection_blob"), 5, 100));

		ht.put("prod_1_impact_blob", new WebFieldText("prod_1_impact_blob",
				addMode ? "" : db.getText("prod_1_impact_blob"), 5, 100));

		ht.put("prod_2_impact_blob", new WebFieldText("prod_2_impact_blob",
				addMode ? "" : db.getText("prod_2_impact_blob"), 5, 100));

		ht.put("prod_3_impact_blob", new WebFieldText("prod_3_impact_blob",
				addMode ? "" : db.getText("prod_3_impact_blob"), 5, 100));

		ht.put("prod_4_impact_blob", new WebFieldText("prod_4_impact_blob",
				addMode ? "" : db.getText("prod_4_impact_blob"), 5, 100));

		/*
		 * Return
		 */
		return ht;

	}
}
