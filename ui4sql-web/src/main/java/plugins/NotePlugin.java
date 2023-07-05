/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * Note Plugin
 * 
 * Change log:
 * 
 * 2/15 added mySql 3/13 as 'target' to list query
 * 
 */
public class NotePlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * Constructors
	 * 
	 * @throws services.ServicesException
	 * 
	 * 
	 **************************************************************************/

	public NotePlugin() throws services.ServicesException {
		super();
		this.setTableName("tnote");
		this.setKeyName("note_id");
		this.setTargetTitle("Note");
		this.setListHeaders( new String[] { "Title", "Reference", "Type" });
		this.setMoreListColumns(new  String[] { "title_nm", "reference_nm",
				"code_desc" });
		this.setMoreListJoins(new  String[] { " left join tcodes on tnote.type_cd = tcodes.code_value and tcodes.code_type_id  = 43 " });
	}

	/***************************************************************************
	 * 
	 * Web Page Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable userHt = sm.getUserHT();

		WebFieldSelect wfType = new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("NOTETYPE"));

		WebFieldString wfTitle = new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 32, 32);

		WebFieldString wfRefr = new WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32);

		WebFieldString wfWho = new WebFieldString("participants_tx",
				addMode ? "" : db.getText("participants_tx"), 64, 255);

		WebFieldDate wfNoteDate = new WebFieldDate("note_date", (addMode ? ""
				: db.getText("note_date")));

		WebFieldText wfNotes = new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80);

		WebFieldSelect wfFollupId = new WebFieldSelect("followup_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("followup_uid"), userHt);

		WebFieldText wfFollowup = new WebFieldText("followup_blob",
				addMode ? "" : db.getText("followup_blob"), 3, 80);

		WebFieldText wfConclude = new WebFieldText("conclusion_blob",
				addMode ? "" : db.getText("conclusion_blob"), 3, 80);

		WebField[] wfs = { wfRefr, wfType, wfTitle, wfWho, wfConclude, wfNotes,
				wfFollupId, wfFollowup, wfNoteDate };

		return webFieldsToHT(wfs);

	}

}
