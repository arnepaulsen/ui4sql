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
 *   2/15 added mySql
 * */

/*******************************************************************************
 * Tollgate Manager
 * 
 * This data manager is unique in that the questions asked depend on the DMAIC
 * code value. The questions on the html page cannot be displayed until after
 * add mode is completed and a DMAIC code has been selected.
 * 
 * In display or update mode, the questions are obtained from table tfields
 * depending on the DMAIC code selected.
 * 
 * Change Log:
 * 
 * 
 ******************************************************************************/

public class TollgatePlugin extends AbsProjectPlugin {

	// *******************
	// *******************
	// CONSTRUCTORS *
	// *******************
	// *******************

	public String getCustomSubForm() {
		return db.getText("dmaic_cd");
	}

	public TollgatePlugin() throws services.ServicesException {
		super();

		this.setTableName("ttollgate");
		this.setKeyName("tollgate_id");
		this.setTargetTitle("PMR Tollgate Review");

		this.setListHeaders(new String[] { "Title", "Phase", "Version",
				"Review Date" });

		this.setMoreListColumns(new String[] { "title_nm", "code_desc",
				"version_nm", "version_date" });
		this
				.setMoreListJoins(new String[] { " join tcodes on ttollgate.dmaic_cd = tcodes.code_value and code_type_id  = 8 " });

		this.setMoreSelectColumns(new String[] { "code_desc" });
		this.setMoreSelectJoins(this.moreListJoins);

	}

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht;

		if (addMode) {
			ht = showNewPage();
		} else {
			this.setAddCustomFields(true);
			ht = showUpdatePage();
		}
		return ht;

	}

	/*
	 * 
	 * New Tollgate pages just get the project and dmiac id,
	 * 
	 * because the questions to display depend on the phase.
	 */
	private Hashtable showNewPage() {

		Hashtable ht = new Hashtable();

		ht.put("dmaic_cd", new WebFieldSelect("dmaic_cd", "D", sm
				.getCodes("DMAIC")));

		ht.put("msg", new WebFieldDisplay("msg",
				"First select a DMAIC type, then select Save-Edit."));

		return ht;

	}

	/*
	 * 
	 * This is always Update or Display, no need to worry about add ( no value
	 * in the dbInterface rs)
	 */
	private Hashtable showUpdatePage() throws services.ServicesException {

		/* save off the dmaic_cd when processing updates */

		// WHY-WHY sm.setFilterCode("dmaic_cd", db.getText("dmaic_cd"));
		Hashtable ht = new Hashtable();

		/*
		 * Web Fields for all dmaic phases - hardcoded on template
		 * 
		 */
		ht.put("dmaic_cd", new WebFieldDisplay("dmaic_cd", db
				.getText("code_desc")));

		ht.put("title_nm", new WebFieldString("title_nm", db
				.getText("title_nm"), 64, 128));

		ht.put("reference_nm", new WebFieldString("reference_nm", db
				.getText("reference_nm"), 32, 32));

		ht.put("version_nm", new WebFieldString("version_nm", db
				.getText("version_nm"), 4, 4));

		ht.put("version_date", new WebFieldDate("version_date", db
				.getText("version_date")));

		ht.put("version_tx", new WebFieldText("version_tx", db
				.getText("version_tx"), 3, 60));

		/*
		 * Status of all General Questions
		 * 
		 */
		ht.put("s1_st", new WebFieldSelect("s1_st", db.getText("s1_st"), sm
				.getCodes("YESNO")));
		ht.put("s2_st", new WebFieldSelect("s2_st", db.getText("s2_st"), sm
				.getCodes("YESNO")));
		ht.put("s3_st", new WebFieldSelect("s3_st", db.getText("s3_st"), sm
				.getCodes("YESNO")));
		ht.put("s4_st", new WebFieldSelect("s4_st", db.getText("s4_st"), sm
				.getCodes("YESNO")));
		ht.put("s5_st", new WebFieldSelect("s5_st", db.getText("s5_st"), sm
				.getCodes("YESNO")));
		ht.put("s6_st", new WebFieldSelect("s6_st", db.getText("s6_st"), sm
				.getCodes("YESNO")));
		ht.put("s7_st", new WebFieldSelect("s7_st", db.getText("s7_st"), sm
				.getCodes("YESNO")));
		ht.put("s8_st", new WebFieldSelect("s8_st", db.getText("s8_st"), sm
				.getCodes("YESNO")));
		ht.put("s9_st", new WebFieldSelect("s9_st", db.getText("s9_st"), sm
				.getCodes("YESNO")));
		ht.put("s10_st", new WebFieldSelect("s10_st", db.getText("s10_st"), sm
				.getCodes("YESNO")));
		ht.put("s11_st", new WebFieldSelect("s11_st", db.getText("s11_st"), sm
				.getCodes("YESNO")));
		ht.put("s12_st", new WebFieldSelect("s12_st", db.getText("s12_st"), sm
				.getCodes("YESNO")));
		ht.put("s13_st", new WebFieldSelect("s13_st", db.getText("s13_st"), sm
				.getCodes("YESNO")));
		ht.put("s14_st", new WebFieldSelect("s14_st", db.getText("s14_st"), sm
				.getCodes("YESNO")));
		ht.put("s15_st", new WebFieldSelect("s15_st", db.getText("s15_st"), sm
				.getCodes("YESNO")));
		ht.put("s16_st", new WebFieldSelect("s16_st", db.getText("s16_st"), sm
				.getCodes("YESNO")));
		ht.put("s17_st", new WebFieldSelect("s17_st", db.getText("s17_st"), sm
				.getCodes("YESNO")));
		ht.put("s18_st", new WebFieldSelect("s18_st", db.getText("s18_st"), sm
				.getCodes("YESNO")));
		ht.put("s19_st", new WebFieldSelect("s19_st", db.getText("s19_st"), sm
				.getCodes("YESNO")));
		ht.put("s20_st", new WebFieldSelect("s20_st", db.getText("s20_st"), sm
				.getCodes("YESNO")));
		ht.put("s21_st", new WebFieldSelect("s21_st", db.getText("s21_st"), sm
				.getCodes("YESNO")));
		ht.put("s22_st", new WebFieldSelect("s22_st", db.getText("s22_st"), sm
				.getCodes("YESNO")));

		/*
		 * A remark to all general questions (they are all blobs)
		 * 
		 */
		ht.put("s1_rmk", new WebFieldString("s1_rmk", db.getText("s1_rmk"), 64,
				64));
		ht.put("s2_rmk", new WebFieldString("s2_rmk", db.getText("s2_rmk"), 64,
				64));
		ht.put("s3_rmk", new WebFieldString("s3_rmk", db.getText("s3_rmk"), 64,
				64));
		ht.put("s4_rmk", new WebFieldString("s4_rmk", db.getText("s4_rmk"), 64,
				64));
		ht.put("s5_rmk", new WebFieldString("s5_rmk", db.getText("s5_rmk"), 64,
				64));
		ht.put("s6_rmk", new WebFieldString("s6_rmk", db.getText("s6_rmk"), 64,
				64));
		ht.put("s7_rmk", new WebFieldString("s7_rmk", db.getText("s7_rmk"), 64,
				64));
		ht.put("s8_rmk", new WebFieldString("s8_rmk", db.getText("s8_rmk"), 64,
				64));
		ht.put("s9_rmk", new WebFieldString("s9_rmk", db.getText("s9_rmk"), 64,
				64));
		ht.put("s10_rmk", new WebFieldString("s10_rmk", db.getText("s10_rmk"),
				64, 64));
		ht.put("s11_rmk", new WebFieldString("s11_rmk", db.getText("s11_rmk"),
				64, 64));
		ht.put("s12_rmk", new WebFieldString("s12_rmk", db.getText("s12_rmk"),
				64, 64));
		ht.put("s13_rmk", new WebFieldString("s13_rmk", db.getText("s13_rmk"),
				64, 64));
		ht.put("s14_rmk", new WebFieldString("s14_rmk", db.getText("s14_rmk"),
				64, 64));
		ht.put("s15_rmk", new WebFieldString("s15_rmk", db.getText("s15_rmk"),
				64, 64));
		ht.put("s16_rmk", new WebFieldString("s16_rmk", db.getText("s16_rmk"),
				64, 64));
		ht.put("s17_rmk", new WebFieldString("s17_rmk", db.getText("s17_rmk"),
				64, 64));
		ht.put("s18_rmk", new WebFieldString("s18_rmk", db.getText("s18_rmk"),
				64, 64));
		ht.put("s19_rmk", new WebFieldString("s19_rmk", db.getText("s19_rmk"),
				64, 64));
		ht.put("s20_rmk", new WebFieldString("s20_rmk", db.getText("s20_rmk"),
				64, 64));
		ht.put("s21_rmk", new WebFieldString("s21_rmk", db.getText("s21_rmk"),
				64, 64));
		ht.put("s22_rmk", new WebFieldString("s22_rmk", db.getText("s22_rmk"),
				64, 64));

		return ht;

	}

}
