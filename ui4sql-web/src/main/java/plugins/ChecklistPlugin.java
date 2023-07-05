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

/**
 * Checklist Data Manager
 * 
 * 
 */

public class ChecklistPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public ChecklistPlugin() throws services.ServicesException {
		super();
		this.setTableName("tchecklist");

		this.setKeyName("checklist_id");
		this.setTargetTitle("Checklist");

		this.setListHeaders(new String[] { "Title", "Reference", "Version" });

		this.setMoreListColumns(new String[] { "title_nm", "reference_nm",
				"version_nm" });

	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		String role = addMode ? "" : sm.getProjectRole();

		WebField g1_st;
		WebField g1_rc;
		WebField g1_rmk;

		WebField g2_st;
		WebField g2_rc;
		WebField g2_rmk;

		WebField g3_st;
		WebField g3_rc;
		WebField g3_rmk;

		WebField g4_st;
		WebField g4_rc;
		WebField g4_rmk;

		WebField po1_st;
		WebField po1_rc;
		WebField po1_rmk;
		WebField po2_st;
		WebField po2_rc;
		WebField po2_rmk;
		WebField po3_st;
		WebField po3_rc;
		WebField po3_rmk;
		WebField po4_st;
		WebField po4_rc;
		WebField po4_rmk;
		WebField po5_st;
		WebField po5_rc;
		WebField po5_rmk;
		WebField po6_st;
		WebField po6_rc;
		WebField po6_rmk;
		WebField po7_st;
		WebField po7_rc;
		WebField po7_rmk;
		WebField po8_st;
		WebField po8_rc;
		WebField po8_rmk;
		WebField po9_st;
		WebField po9_rc;
		WebField po9_rmk;
		WebField po10_st;
		WebField po10_rc;
		WebField po10_rmk;
		WebField po11_st;
		WebField po11_rc;
		WebField po11_rmk;

		WebField sc1_st;
		WebField sc1_rc;
		WebField sc1_rmk;
		WebField sc2_st;
		WebField sc2_rc;
		WebField sc2_rmk;
		WebField sc3_st;
		WebField sc3_rc;
		WebField sc3_rmk;
		WebField sc4_st;
		WebField sc4_rc;
		WebField sc4_rmk;
		WebField sc5_st;
		WebField sc5_rc;
		WebField sc5_rmk;
		WebField sc6_st;
		WebField sc6_rc;
		WebField sc6_rmk;
		WebField sc7_st;
		WebField sc7_rc;
		WebField sc7_rmk;
		WebField sc8_st;
		WebField sc8_rc;
		WebField sc8_rmk;
		WebField sc9_st;
		WebField sc9_rc;
		WebField sc9_rmk;
		WebField sc10_st;
		WebField sc10_rc;
		WebField sc10_rmk;
		WebField sc11_st;
		WebField sc11_rc;
		WebField sc11_rmk;

		WebField i1_st;
		WebField i1_rc;
		WebField i1_rmk;
		WebField i2_st;
		WebField i2_rc;
		WebField i2_rmk;
		WebField i3_st;
		WebField i3_rc;
		WebField i3_rmk;

		WebField pp1_st;
		WebField pp1_rc;
		WebField pp1_rmk;
		WebField pp2_st;
		WebField pp2_rc;
		WebField pp2_rmk;
		WebField pp3_st;
		WebField pp3_rc;
		WebField pp3_rmk;
		WebField pp4_st;
		WebField pp4_rc;
		WebField pp4_rmk;

		// ********************************************************
		// if add mode... only allow projects that don't have a checklist
		// ********************************************************

		WebFieldString wfTitle = new WebFieldString("title_nm", addMode ? ""
				: db.getText("title_nm"), 64, 128);

		WebFieldString wfRefr = new WebFieldString("reference_nm", addMode ? ""
				: db.getText("reference_nm"), 32, 32);

		WebFieldString wfVersion = new WebFieldString("version_nm",
				addMode ? "" : db.getText("version_nm"), 4, 4);

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 60);

		WebFieldDate wfVerDate = new WebFieldDate("version_date", addMode ? ""
				: db.getText("version_date"));

		g1_st = getStatus("g1_st", addMode, role);
		g1_rc = getApprove("g1_rc", addMode, role);
		g1_rmk = getRemark("g1_rmk", addMode, role);
		g2_st = getStatus("g2_st", addMode, role);
		g2_rc = getApprove("g2_rc", addMode, role);
		g2_rmk = getRemark("g2_rmk", addMode, role);
		g3_st = getStatus("g3_st", addMode, role);
		g3_rc = getApprove("g3_rc", addMode, role);
		g3_rmk = getRemark("g3_rmk", addMode, role);
		g4_st = getStatus("g4_st", addMode, role);
		g4_rc = getApprove("g4_rc", addMode, role);
		g4_rmk = getRemark("g4_rmk", addMode, role);

		po1_st = getStatus("po1_st", addMode, role);
		po1_rc = getApprove("po1_rc", addMode, role);
		po1_rmk = getRemark("po1_rmk", addMode, role);
		po2_st = getStatus("po2_st", addMode, role);
		po2_rc = getApprove("po2_rc", addMode, role);
		po2_rmk = getRemark("po2_rmk", addMode, role);
		po3_st = getStatus("po3_st", addMode, role);
		po3_rc = getApprove("po3_rc", addMode, role);
		po3_rmk = getRemark("po3_rmk", addMode, role);
		po4_st = getStatus("po4_st", addMode, role);
		po4_rc = getApprove("po4_rc", addMode, role);
		po4_rmk = getRemark("po4_rmk", addMode, role);
		po5_st = getStatus("po5_st", addMode, role);
		po5_rc = getApprove("po5_rc", addMode, role);
		po5_rmk = getRemark("po5_rmk", addMode, role);
		po6_st = getStatus("po6_st", addMode, role);
		po6_rc = getApprove("po6_rc", addMode, role);
		po6_rmk = getRemark("po6_rmk", addMode, role);
		po7_st = getStatus("po7_st", addMode, role);
		po7_rc = getApprove("po7_rc", addMode, role);
		po7_rmk = getRemark("po7_rmk", addMode, role);
		po8_st = getStatus("po8_st", addMode, role);
		po8_rc = getApprove("po8_rc", addMode, role);
		po8_rmk = getRemark("po8_rmk", addMode, role);
		po9_st = getStatus("po9_st", addMode, role);
		po9_rc = getApprove("po9_rc", addMode, role);
		po9_rmk = getRemark("po9_rmk", addMode, role);
		po10_st = getStatus("po10_st", addMode, role);
		po10_rc = getApprove("po10_rc", addMode, role);
		po10_rmk = getRemark("po10_rmk", addMode, role);
		po11_st = getStatus("po11_st", addMode, role);
		po11_rc = getApprove("po11_rc", addMode, role);
		po11_rmk = getRemark("po11_rmk", addMode, role);

		sc1_st = getStatus("sc1_st", addMode, role);
		sc1_rc = getApprove("sc1_rc", addMode, role);
		sc1_rmk = getRemark("sc1_rmk", addMode, role);
		sc2_st = getStatus("sc2_st", addMode, role);
		sc2_rc = getApprove("sc2_rc", addMode, role);
		sc2_rmk = getRemark("sc2_rmk", addMode, role);
		sc3_st = getStatus("sc3_st", addMode, role);
		sc3_rc = getApprove("sc3_rc", addMode, role);
		sc3_rmk = getRemark("sc3_rmk", addMode, role);
		sc4_st = getStatus("sc4_st", addMode, role);
		sc4_rc = getApprove("sc4_rc", addMode, role);
		sc4_rmk = getRemark("sc4_rmk", addMode, role);
		sc5_st = getStatus("sc5_st", addMode, role);
		sc5_rc = getApprove("sc5_rc", addMode, role);
		sc5_rmk = getRemark("sc5_rmk", addMode, role);
		sc6_st = getStatus("sc6_st", addMode, role);
		sc6_rc = getApprove("sc6_rc", addMode, role);
		sc6_rmk = getRemark("sc6_rmk", addMode, role);
		sc7_st = getStatus("sc7_st", addMode, role);
		sc7_rc = getApprove("sc7_rc", addMode, role);
		sc7_rmk = getRemark("sc7_rmk", addMode, role);
		sc8_st = getStatus("sc8_st", addMode, role);
		sc8_rc = getApprove("sc8_rc", addMode, role);
		sc8_rmk = getRemark("sc8_rmk", addMode, role);
		sc9_st = getStatus("sc9_st", addMode, role);
		sc9_rc = getApprove("sc9_rc", addMode, role);
		sc9_rmk = getRemark("sc9_rmk", addMode, role);
		sc10_st = getStatus("sc10_st", addMode, role);
		sc10_rc = getApprove("sc10_rc", addMode, role);
		sc10_rmk = getRemark("sc10_rmk", addMode, role);
		sc11_st = getStatus("sc11_st", addMode, role);
		sc11_rc = getApprove("sc11_rc", addMode, role);
		sc11_rmk = getRemark("sc11_rmk", addMode, role);

		i1_st = getStatus("i1_st", addMode, role);
		i1_rc = getApprove("i1_rc", addMode, role);
		i1_rmk = getRemark("i1_rmk", addMode, role);
		i2_st = getStatus("i2_st", addMode, role);
		i2_rc = getApprove("i2_rc", addMode, role);
		i2_rmk = getRemark("i2_rmk", addMode, role);
		i3_st = getStatus("i3_st", addMode, role);
		i3_rc = getApprove("i3_rc", addMode, role);
		i3_rmk = getRemark("i3_rmk", addMode, role);

		pp1_st = getStatus("pp1_st", addMode, role);
		pp1_rc = getApprove("pp1_rc", addMode, role);
		pp1_rmk = getRemark("pp1_rmk", addMode, role);
		pp2_st = getStatus("pp2_st", addMode, role);
		pp2_rc = getApprove("pp2_rc", addMode, role);
		pp2_rmk = getRemark("pp2_rmk", addMode, role);
		pp3_st = getStatus("pp3_st", addMode, role);
		pp3_rc = getApprove("pp3_rc", addMode, role);
		pp3_rmk = getRemark("pp3_rmk", addMode, role);
		pp4_st = getStatus("pp4_st", addMode, role);
		pp4_rc = getApprove("pp4_rc", addMode, role);
		pp4_rmk = getRemark("pp4_rmk", addMode, role);

		WebField[] wfs = { wfTitle, wfRefr, wfVersion, wfVerDate, wfNotes,
				g1_st, g1_rc, g1_rmk, g2_st, g2_rc, g2_rmk, g3_st, g3_rc,
				g3_rmk, g4_st, g4_rc, g4_rmk, po1_st, po1_rc, po1_rmk, po2_st,
				po2_rc, po2_rmk, po3_st, po3_rc, po3_rmk, po4_st, po4_rc,
				po4_rmk, po5_st, po5_rc, po5_rmk, po6_st, po6_rc, po6_rmk,
				po7_st, po7_rc, po7_rmk, po8_st, po8_rc, po8_rmk, po9_st,
				po9_rc, po9_rmk, po10_st, po10_rc, po10_rmk, po11_st, po11_rc,
				po11_rmk, sc1_st, sc1_rc, sc1_rmk, sc2_st, sc2_rc, sc2_rmk,
				sc3_st, sc3_rc, sc3_rmk, sc4_st, sc4_rc, sc4_rmk, sc5_st,
				sc5_rc, sc5_rmk, sc6_st, sc6_rc, sc6_rmk, sc7_st, sc7_rc,
				sc7_rmk, sc8_st, sc8_rc, sc8_rmk, sc9_st, sc9_rc, sc9_rmk,
				sc10_st, sc10_rc, sc10_rmk, sc11_st, sc11_rc, sc11_rmk, i1_st,
				i1_rc, i1_rmk, i2_st, i2_rc, i2_rmk, i3_st, i3_rc, i3_rmk,
				pp1_st, pp1_rc, pp1_rmk, pp2_st, pp2_rc, pp2_rmk, pp3_st,
				pp3_rc, pp3_rmk, pp4_st, pp4_rc, pp4_rmk };
		return webFieldsToHT(wfs);

	} // return a WebFieldSelect if user is a Reviewer, else return a

	// WebFieldDisplay
	private WebField getStatus(String parmField, boolean addMode, String role) {

		Hashtable lookup = sm.getCodes("YESNO");

		// must be update mode and a 'REVIEWER' to get a select box
		if ((role.equalsIgnoreCase("REV")) && (addMode == false)) {
			return new WebFieldSelect(parmField, db.getText(parmField), lookup,
					false, true);
		} else {
			if (addMode == true) {
				return new WebFieldDisplay(parmField,
						"<INPUT TYPE=HIDDEN NAME=" + parmField + " VALUE=N>No");

			} else {
				String yesNo = new String(db.getText(parmField)
						.equalsIgnoreCase("Y") ? "Yes" : "No");

				return new WebFieldDisplay(parmField,
						"<INPUT TYPE=HIDDEN NAME=" + parmField + " VALUE="
								+ db.getText(parmField) + ">" + yesNo);
			}

		}
	}

	// return a WebFieldSelect if user is a Reviewer, else return a
	// WebFieldDisplay
	private WebField getApprove(String parmField, boolean addMode, String role) {
		Hashtable lookup = sm.getCodes("PROJREV");

		// must be update mode and a 'REVIEWER' to get a select box
		if ((role.equalsIgnoreCase("APR")) && (addMode == false)) {
			return new WebFieldSelect(parmField, db.getText(parmField), lookup,
					false, true);
		} else {
			if (addMode == true) {
				return new WebFieldDisplay(parmField,
						"<INPUT TYPE=HIDDEN NAME=" + parmField + " VALUE=N>No");

			} else {
				String yesNo = new String(db.getText(parmField)
						.equalsIgnoreCase("Y") ? "Yes" : "No");

				return new WebFieldDisplay(parmField,
						"<INPUT TYPE=HIDDEN NAME=" + parmField + " VALUE="
								+ db.getText(parmField) + ">" + yesNo);
			}

		}
	}

	// return a WebFieldSelect if user is a Reviewer, else return a
	// WebFieldDisplay
	private WebField getRemark(String parmField, boolean addMode, String role) {

		if (role.equalsIgnoreCase("APR")) {
			return new WebFieldString(parmField, addMode ? "" : db
					.getText(parmField), 64, 64);
		} else {
			return new WebFieldDisplay(parmField, addMode ? "" : db
					.getText(parmField));
		}

	}

}
