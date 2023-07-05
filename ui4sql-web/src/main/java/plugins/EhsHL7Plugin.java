/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import java.util.StringTokenizer;
import forms.*;
import router.SessionMgr;

/*******************************************************************************
 * Stage Plugin
 * 
 * This is a child of EHS.
 * 
 * 
 * 
 * 
 * Change Log:
 * 
 * 9/8 added
 * 
 * 
 ******************************************************************************/
public class EhsHL7Plugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public EhsHL7Plugin() throws services.ServicesException {
		super();

		this.setTableName("tehs_hl7");
		this.setKeyName("ehs_hl7_id");
		this.setTargetTitle("HL7");

		this.setIsStepChild (true);
		this.setIsDetailForm (true);
		this.setParentTarget ("Ehs");

	}

	public void init(SessionMgr parmSm) {

		/*
		 * we have to wait for the FormData to run init to push the sm into the
		 * dm object
		 * 
		 */

		this.sm = parmSm;
		this.db = sm.getDbInterface(); // has an open connection

		/*
		 * 
		 */

		this.setListOrder ("seq_no; ");

		this.setListHeaders( new String[] { "Seq", "Type" });

		this.setMoreListColumns(new  String[] { "seq_no", "type_cd" });

		this.setMoreSelectColumns (new String[] { "d.division_id" });

		this.setMoreListJoins(new  String[] {
				" join tehs on tehs_hl7.ehs_id = tehs.ehs_id and tehs.ehs_id = "
						+ sm.getParentId().toString(),
				" join tdivision d on tehs.division_id  = d.division_id" });

		this.setMoreSelectJoins (new String[] {
				" join tehs on tehs_hl7.ehs_id = tehs.ehs_id and tehs.ehs_id = "
						+ sm.getParentId().toString(),
				" join tdivision d on tehs.division_id  = d.division_id" });

	}

	

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/
	/*
	 * data manager calls this just before passing the ht to the db for insert
	 * ... we can insert the 'parent_kind_cd' now,
	 */

	public Hashtable<String, WebField> getWebFields(String parmMode)
			throws services.ServicesException {

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();
		/*
		 * strings
		 * 
		 */

		ht
				.put("type_cd", new WebFieldDisplay("type_cd", db
						.getText("type_cd")));

		ht.put("seq_no", new WebFieldDisplay("seq_no", db.getText("seq_no")));

		ht.put("hl7_tx", new WebFieldDisplay("hl7_tx", db.getText("hl7_tx")));

		String separator = "|";

		StringTokenizer st = new StringTokenizer(db.getText("hl7_tx"),
				separator, true);

		int token_no = 0;
		
		
		st.nextToken();// first one doesnt count

		for (int x = 0; x < 60;) {

			if (st.hasMoreTokens()) {
				String token = (String) st.nextToken();
				if (token.equals(separator)) {
					x++;
				} else {
					token_no++;
					ht.put("fld_" + x + "_tx", new WebFieldDisplay(
							"fld_" + x + "_tx", token));
				}
			} else {
				x++;
				ht.put("fld_" + x + "_tx", new WebFieldDisplay("fld_" + x
						+ "_tx", ""));
			}
		}
		
		String s = "" + token_no;
		
		ht.put("ct_tx", new WebFieldDisplay("ct_tx", s));

		return ht;

	}

}
