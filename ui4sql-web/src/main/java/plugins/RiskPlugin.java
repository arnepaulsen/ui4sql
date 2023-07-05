/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Risk Data Manager 3/23 new
 * 
 */

public class RiskPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public RiskPlugin() throws services.ServicesException {
		super();
		this.setTableName("trisk");
		this.setKeyName("risk_id");
		this.setTargetTitle("Risks");

		this.setListHeaders(new String[] { "Title", "Reference",
				"Amount (000s)", "Chance (%)", "Loss * Chance", "Status" });
		
		this.setMoreListColumns(new String[] { "title_nm", "reference_nm",
				"risk_amt", "probability_pct",
				"round(risk_amt * probability_pct / 100) as theLoss",
				"code_desc" });
		this
				.setMoreListJoins(new String[] { " left join tcodes on trisk.state_cd = tcodes.code_value and tcodes.code_type_id  = 5 " });

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
		 * Computed amounts
		 */

		if (parmMode.equalsIgnoreCase("show")) {
			int pct = db.getInteger("probability_pct").intValue();
			int amt = db.getInteger("risk_amt").intValue();
			int loss = pct * amt / 100;

			ht.put("expected_loss", new WebFieldDisplay("expected_loss",
					new Integer(loss).toString()));
		} else {
			ht.put("expected_loss", new WebFieldDisplay("expected_loss", ""));
		}

		/*
		 * Ids
		 */

		ht.put("assign_to_uid", new WebFieldSelect("assign_to_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("assign_to_uid"), sm.getUserHT(), true));

		/*
		 * Strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 12, 12));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 64));

		/*
		 * Numbers
		 */
		ht.put("probability_pct", new WebFieldString("probability_pct",
				addMode ? "0" : db.getText("probability_pct"), 4, 4));

		ht.put("impact_rank", new WebFieldString("impact_rank", addMode ? "0"
				: db.getText("impact_rank"), 4, 4));

		ht.put("risk_amt", new WebFieldString("risk_amt", addMode ? "0" : db
				.getText("risk_amt"), 4, 4));

		ht.put("loss_amt", new WebFieldString("loss_amt", addMode ? "0" : db
				.getText("loss_amt"), 4, 4));

		/*
		 * Codes
		 */
		ht.put("state_cd", new WebFieldSelect("state_cd", addMode ? "New" : db
				.getText("state_cd"), sm.getCodes("STATUS")));

		ht.put("risk_type_cd", new WebFieldSelect("risk_type_cd",
				addMode ? "New" : db.getText("risk_type_cd"), sm
						.getCodes("RISKTYPE")));

		/*
		 * Blobs
		 */

		ht.put("consequence_desc", new WebFieldText("consequence_desc",
				addMode ? "" : db.getText("consequence_desc"), 5, 100));

		ht.put("mitigation_desc", new WebFieldText("mitigation_desc",
				addMode ? "" : db.getText("mitigation_desc"), 5, 100));

		ht.put("contingency_desc", new WebFieldText("contingency_desc",
				addMode ? "" : db.getText("contingency_desc"), 5, 100));

		ht.put("triggers_desc", new WebFieldText("triggers_desc", addMode ? ""
				: db.getText("triggers_desc"), 5, 100));

		return ht;

	}

}
