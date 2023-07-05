/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;

/**
 * GUI Data Manager
 * 
 * Change Log:
 * 
 * 5/19/05 Take out getDbFields!!
 * 
 * 
 */
public class LessonPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public LessonPlugin() throws services.ServicesException {
		super();
		this.setTableName("tlesson");
		this.setKeyName("lesson_id");
		this.setTargetTitle("Lessons");

		this.setMoreListColumns(new  String[] { "reference_nm", "title_nm" , "s.code_desc as status_desc", "i.code_desc as impact_lvl"
				, "concat(u.last_name, ',', u.first_name)" });
		
		this.setMoreListJoins(new  String[] { " left join tcodes s on tlesson.status_cd = s.code_value and s.code_type_id  = 58 ",
				" left join tcodes i on tlesson.impact_cd = i.code_value and i.code_type_id  = 55 ",
				" left join tuser u on tlesson.responsible_uid = u.user_id "});
		
		this.setListHeaders( new String[] { "Reference", "Title" , "Status", "Impact", "Owner"});

	}

	/***************************************************************************
	 * 
	 * Web Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();
		
		ht.put("reference_nm", new 	WebFieldString("reference_nm",
				(addMode ? "" : db.getText("reference_nm")), 32, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? ""
				: db.getText("title_nm")), 64, 64));

		ht.put("impact_cd", new WebFieldSelect("impact_cd", addMode ? "" : db
				.getText("impact_cd"), sm.getCodes("HIGHMEDLOW")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("OPENCLOSE")));
		
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 3, 80));

		ht.put("follow_up_blob",  new WebFieldText("follow_up_blob", addMode ? ""
				: db.getText("follow_up_blob"), 3, 80));

		ht.put("impact_blob" , new WebFieldText("impact_blob",
				addMode ? "" : db.getText("impact_blob"), 3, 80));

		ht.put("solution_blob", new WebFieldText("solution_blob",
				addMode ? "" : db.getText("solution_blob"), 3, 80));

		ht.put("responsible_uid", new WebFieldSelect("responsible_uid",
				addMode ? new Integer("0") : (Integer) db
						.getObject("responsible_uid"), sm.getUserHT()));
		
		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 3, 80));

		return ht;
	}

}
