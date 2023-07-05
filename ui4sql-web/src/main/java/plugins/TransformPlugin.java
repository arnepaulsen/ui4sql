/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import router.SessionMgr;
import db.DbFieldInteger;

/**
 * Transform Plugin - child of Usage
 * 
 * Documents what modules to do data structures.
 * 
 * Normally just done for module outputs.
 * 
 * 
 */
public class TransformPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 * ... reminder.. there is no sm in the constructor, use init() to refer sm.
	 * 
	 **************************************************************************/

	char defaultType = 'F'; // default to file!

	String structureTableName;

	String structureKeyName;

	public TransformPlugin() throws services.ServicesException {
		super();

		this.setTableName("ttransform");
		this.setKeyName("transform_id");

		this.setParentTarget ("Usage");
		this.setIsStepChild(true);
		this.setIsDetailForm(true);
		this.setContextSwitchOk (false);

	}

	/*
	 * remember, don't try to reference sm in the constructor, because the data
	 * manager doesn't have a reference to it until the init() method is run.
	 * 
	 * figureOutType() uses sm.
	 * 
	 */
	public void init(SessionMgr parmSm) {

		sm = parmSm;
		db = sm.getDbInterface(); // has an open connection
		setStructureNames();

		this.setListHeaders( new String[] { "Module", "Element", "Kind" });

		this.setTargetTitle(sm.getParentName() + " : " + sm.getStructureName()
				+ " - Data Transformations");

		this.setMoreListJoins(new  String[] {
				" left join telement on ttransform.element_id = telement.element_id ",
				" left join tusage on ttransform.usage_id = tusage.usage_id and tusage.usage_id =  "
						+ sm.Parm("FilterUsage"),
				" left join " + structureTableName
						+ " on tusage.structure_id = " + structureTableName
						+ "." + structureKeyName +

						" left join tmodule on tusage.module_id = tmodule.module_id ",
				" left join tapplications on tmodule.application_id = tapplications.application_id ",
				" left join tproject on tusage.project_id = tproject.project_id ",
				" left join tcodes kind on tusage.type_cd = kind.code_value and kind.code_type_id =  64 " });

		this.setMoreListColumns(new  String[] { "tmodule.title_nm as ModuleName",
				"telement.title_nm as ElementName",
				"kind.code_desc as module_type" });

		this.setMoreSelectJoins (new String[] {
				" left join telement on ttransform.element_id = telement.element_id ",
				" left join tusage on ttransform.usage_id = tusage.usage_id   ",
				" left join " + structureTableName
						+ " on tusage.structure_id = " + structureTableName
						+ "." + structureKeyName +

						" left join tmodule on tusage.module_id = tmodule.module_id ",
				" left join tapplications on tmodule.application_id = tapplications.application_id ",
				" left join tproject on tusage.project_id = tproject.project_id ",
				" left join tcodes kind on tusage.type_cd = kind.code_value and kind.code_type_id =  64 " });

		this.setMoreSelectColumns (new String[] {
				"tmodule.title_nm as module_name", "kind.code_desc as kind",
				"telement.title_nm as ElementName",
				structureTableName + ".title_nm as StructureName" });

	}
	
	public boolean afterGet() {
		
		this.setAddOk(myAddOk());
		return true;
	}

	private void setStructureNames() {

		char parmType = sm.getStructureType().charAt(0);

		switch (parmType) {
		case 'S': {
			structureTableName = "tsegment";
			structureKeyName = "segment_id";
			break;
		}
		case 'T': {
			structureTableName = "ttable";
			structureKeyName = "table_id";
			break;
		}

		case 'F': {
			structureTableName = "tfile";
			structureKeyName = "file_id";
			break;
		}

		// M Message is the default
		default: {
			structureTableName = "tmessage";
			structureKeyName = "message_id";
		}

		}

	}

	

	/***************************************************************************
	 * 
	 * Filter Routnes... only filter is structure type, like message, table,
	 * segment, file, etc.
	 * 
	 **************************************************************************/

	public boolean myAddOk() {

		if (this.formWriterType.equalsIgnoreCase("list")) {

			// only allow add if there is a module selected
			return sm.Parm("FilterUsage").length() > 0
					&& !sm.Parm("FilterUsage").equalsIgnoreCase("0");
		} else
			return true;
	}

	/*
	 * filter the usage types (like message, segment, table, record, etc.)
	 */

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable ht = new Hashtable();

		if (addMode) {
			ht = addNewItems(ht);
		} else {
			ht = addUpdateItems(ht);
		}

		ht = addNewAndUpdateItems(ht, addMode);

		return ht;

	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("usage_id", new DbFieldInteger("usage_id", new Integer(sm
				.Parm("FilterUsage"))));

		// type_cd is from parent tusage.
		return true;
	}

	public Hashtable addNewItems(Hashtable ht)
			throws services.ServicesException {

		/*
		 * we could be coming here from either the list page or the show page
		 * 'new' buttons, so have to look around for the type (file, segment,
		 * message, table) code
		 */

		// look up the module name... we only have the id, and want to display
		// it
		ht.put("module_name", new WebFieldDisplay("module_name", sm
				.getParentName()));

		ht.put("structure_id", new WebFieldDisplay("structure_name", sm
				.getStructureName()));

		ht
				.put("type_cd", new WebFieldDisplay("type_cd", sm
						.getStructureType()));

		String query = " select title_nm, element_id, title_nm from telement "
				+ " where telement.parent_kind_cd ='"
				+ sm.getStructureType().charAt(0) + "'"
				+ " and telement.parent_id = " + sm.getStructureId().toString();

		Hashtable elements = db.getLookupTable(query);

		ht.put("element_id", new WebFieldSelect("element_id", new Integer("0"),
				elements));

		return ht;
	}

	public Hashtable addUpdateItems(Hashtable ht)
			throws services.ServicesException {

		sm.setStructureType(db.getText("kind"));

		ht.put("module_name", new WebFieldDisplay("module_name", db
				.getText("module_name")));

		ht.put("type_cd", new WebFieldDisplay("type_cd", db.getText("kind")));

		ht.put("structure_id", new WebFieldDisplay("structure_id", db
				.getText("StructureName")));

		ht.put("element_id", new WebFieldDisplay("element_id", db
				.getText("ElementName")));

		/*
		 * get Structure name... can't do in select because we don't know what
		 * to join to until the record is returned with the type_cd
		 */

		return ht;

	}

	private Hashtable addNewAndUpdateItems(Hashtable ht, boolean addMode) {

		/*
		 * integers
		 */

		ht.put("project_id", new WebFieldSelect("project_id",
				addMode ? new Integer("0") : db.getInteger("project_id"), sm
						.getProjectFilter(), true));

		/*
		 * Codes
		 */
		ht.put("method_cd", new WebFieldSelect("method_cd", db
				.getText("method_cd"), sm.getCodes("TRANSFORMTYPE")));

		/*
		 * blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		return ht;

	}
}
