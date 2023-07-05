/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;
import forms.*;
import router.SessionMgr;
import services.ServicesException;
import db.DbFieldInteger;
import db.DbFieldString;

/**
 * Usage Plugin
 * 
 * Keeps track of the relationships between modules and data structures in table
 * tusage.
 * 
 * The data structure represented could one of: 1. message 2. segment 3. table
 * 4. record 5. file
 * 
 * the type_cd tells which kind of data structure the module is related to, and
 * the structure_id is the row key of that structure.
 * 
 * The list page only shows one type at a time.
 * 
 * Before any add takes place, there must be a type_cd context.
 * 
 * This is similar to the 'step' data manager, in that these child segments are
 * associated with different parents. step has a 'parent-id', and parent
 * type_cd.
 * 
 * tusage always has the same parent, which is module id, but the type of the
 * associated structure can be changed on the list page. For example, you can
 * list all the module associations of type 'file', or 'segment', etc.
 * 
 * Keeping track of the association is a litte tricky. If the page post is
 * coming from the list page, then the 'filterType' will have the correct value.
 * if show or update, the value comes from the database, and is then saved to
 * the session via setStructureCd if the page post is coming from a show page,
 * then the 'new' page will have to get the assoicated usage type (file,
 * message) from the session manager.
 * 
 * the list queries are created depending on what type of 'assocation' is
 * reuested.
 * 
 * didn't want to show all types on the list page, because then the query
 * would have to join all the different types.... but is possible
 * 
 */
public class UsagePlugin extends AbsApplicationPlugin {

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

	public UsagePlugin() throws services.ServicesException {
		super();

		debug("Usage DM starting");

		this.setTableName("tusage");
		this.setKeyName("usage_id");
		this.setTargetTitle("Usage");

		this.setParentTarget ("Module");
		this.setIsStepChild(true);
		this.setIsDetailForm(true);
		this.setHasDetailForm (true);
		this.setDetailTarget ("Transform");
		this.setDetailTargetLabel ("Transforms");
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

		// 9/29 transform
		setStructureNames();

		this.setListHeaders( new String[] { "Module", "Kind", "Data Structure", "Usage" });

		this.moreListJoins = getListJoins(figureOutType().charAt(0));

		this.setMoreListColumns(new  String[] { "tmodule.title_nm as ModuleName",
				"kind.code_desc as module_type", "x.title_nm", " access.code_desc as usage_desc",
				"tproject.project_name" });

		// 9/29 transform....
		//if (false)
		//	this.setMoreSelectJoins (new String[] {
			//		" left join tmodule on tusage.module_id = tmodule.module_id ",
			//		" left join tapplications on tmodule.application_id = tapplications.application_id ",
			//		" left join tproject on tusage.project_id = tproject.project_id ",
			//		" left join tcodes kind on tusage.type_cd = kind.code_value and kind.code_type_id =  64 " });

		this.moreSelectJoins = getSelectJoins(figureOutType().charAt(0));

		// 9/29 transform
		this.setMoreSelectColumns (new String[] {
				"tmodule.title_nm as module_name", "kind.code_desc as kind",
				"x.title_nm as StructureName" });

	}

	/*
	 * Join to the structure depending on the Usage type
	 */

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

	private String[] getListJoins(char parmType) {
		debug("Usage DM getJoins ending");
		return new String[] {
				" left join tmodule on tusage.module_id = tmodule.module_id and tmodule.module_id = "
						+ (sm.Parm("FilterModule").length() == 0 ? "0" : sm
								.Parm("FilterModule")),
				" left join tapplications on tmodule.application_id = tapplications.application_id ",
				" left join tproject on tusage.project_id = tproject.project_id ",
				" left join " + structureTableName
						+ " x on tusage.structure_id = x." + structureKeyName
						+ " and tusage.type_cd = '" + parmType + "'",
				" left join tcodes access on tusage.usage_cd = access.code_value and access.code_type_id =  62 ",
				" left join tcodes kind on tusage.type_cd = kind.code_value and kind.code_type_id =  64 " };

	} // just like listjoin, but no qualify module id

	private String[] getSelectJoins(char parmType) {
		return new String[] {
				" left join tmodule on tusage.module_id = tmodule.module_id ",
				" left join tapplications on tmodule.application_id = tapplications.application_id ",
				" left join tproject on tusage.project_id = tproject.project_id ",
				" left join " + structureTableName
						+ " x on tusage.structure_id = x." + structureKeyName
						+ " and tusage.type_cd = '" + parmType + "'",
				" left join tcodes kind on tusage.type_cd = kind.code_value and kind.code_type_id =  64 " };

	}

	/*
	 * Parent Stuff
	 */

	
	/***************************************************************************
	 * 
	 * Filter Routnes... only filter is structure type, like message, table,
	 * segment, file, etc.
	 * 
	 **************************************************************************/

	
	public boolean listColumnHasSelector(int columnNumber) {
		// column 0 = ModuleName, column 1 =
		if (columnNumber < 2)

			return true;
		else
			return false;
	}

	public WebField getListSelector(int columnNumber) {

		switch (columnNumber) {
		case 0: {

			String sQuery = "Select title_nm, module_id, title_nm from tmodule where application_id = "
					+ sm.getApplicationId().toString();

			return new WebFieldSelect("FilterModule", sm.Parm("FilterModule")
					.length() == 0 ? new Integer("0") : new Integer(sm
					.Parm("FilterModule")), db, sQuery, "- Select Module -");
		}

		default: {
			// must be column 1 = Structure Type "FilterType"
			WebFieldSelect wf = new WebFieldSelect("FilterType",
					figureOutType(), sm.getCodes("STRUCTURES"), false);
				wf.setDisplayClass("listform");
			return wf;
		}

		}

	} /*
		 * filter the usage types (like message, segment, table, record, etc.)
		 */

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();

		sb.append(" AND tusage.type_cd = '" + figureOutType() + "'");

		return sb.toString();
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

		if (addMode) {
			ht = addNewItems(ht);
		} else {
			ht = addUpdateItems(ht);
		}

		ht = addNewAndUpdateItems(ht, addMode);

		return ht;

	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("module_id", new DbFieldInteger("module_id", new Integer(sm
				.Parm("FilterModule"))));
		

		// the type is only displayed on the screen, so we have to get it
		// from the list filter, or if coming from the 'new' button on the
		// display
		// page, then get it from the session where the prvious 'show' page save
		// it.

		ht.put("type_cd", new DbFieldString("type_cd", figureOutType()));
		
		return true;

	}

	private String figureOutType() {
		/*
		 * 1. if coming from the List page to Add page, then the type_cd is the
		 * "FilterType" 2. if coming from the show page, then the type_cd is in
		 * the sm.getStructureType() that was left there from the prior display
		 * page. 3. maybe nothing if
		 */

		debug("Usage DM figureOutType starting");

		String structureTypeCd = "F";

		if (sm.parmExists("FilterType")) {
			debug(".. FilterType parm exists.. value is "
					+ sm.Parm("FilterType"));
			structureTypeCd = sm.Parm("FilterType");
		} else {
			debug("testing null");
			structureTypeCd = sm.getStructureType().substring(0, 1);
		}

		debug("Usage DM figureOutType ending");
		debug("Usage DM figureOutType ending with type code : "
				+ structureTypeCd);
		return structureTypeCd;

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
		String moduleName = "";

		try {
			moduleName = db
					.getColumn("select title_nm from tmodule where module_id = "
							+ sm.Parm("FilterModule"));
		}

		catch (ServicesException e) {

		}

		ht.put("module_name", new WebFieldDisplay("module_name", moduleName));

		/*
		 * put out the literal of the type "Message", "Segment", "Table", F
		 */
		String structureTypeCd = figureOutType();
		sm.setStructureType(structureTypeCd);

		// todo: try to get this from the codes cached in the session manager
		try {
			ht
					.put(
							"type_cd",
							new WebFieldDisplay(
									"type_cd",
									db
											.getColumn("select code_desc from tcodes where code_type_id = 64 and code_value = '"
													+ structureTypeCd + "'")));
		} catch (ServicesException e) {

		}

		ht.put("structure_id", getUsageList(structureTypeCd.charAt(0)));

		return ht;
	}

	public Hashtable addUpdateItems(Hashtable ht)
			throws services.ServicesException {

		// 9/29 transform
		sm.setStructureType(db.getText("kind"));
		sm.setStructureId(db.getInteger("structure_id"), db
				.getText("StructureName"));
		sm.setParentId(db.getInteger("module_id"), db.getText("module_name"));

		ht.put("structure_id", getUsageList(db.getChar("type_cd")));

		// 9/29 for transform

		ht.put("type_cd", new WebFieldDisplay("type_cd", db.getText("kind")));

		ht.put("module_name", new WebFieldDisplay("module_name", db
				.getText("module_name")));

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
		 * codes
		 */

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "D" : db
				.getText("status_cd"), sm.getCodes("MODULESTATUS")));

		ht.put("usage_cd", new WebFieldSelect("usage_cd", addMode ? "R" : db
				.getText("usage_cd"), sm.getCodes("UPDATETYPE")));

		/*
		 * blobs
		 */

		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 5, 100));

		return ht;

	}

	/*
	 * so putting the list here because it's so big
	 * 
	 */

	private WebFieldSelect getUsageList(char structureType) {
		String query = new String("");

		switch (structureType) {

		case 'M': {

			query = "Select title_nm, message_id, title_nm from tmessage where application_id = "
					+ sm.getApplicationId().toString();
			break;
		}

		case 'T': {
			query = "Select title_nm, table_id, title_nm from ttable where application_id = "
					+ sm.getApplicationId().toString();
			;
			break;
		}

		case 'F': {
			query = "Select title_nm, file_id, title_nm from tfile where application_id = "
					+ sm.getApplicationId().toString();
			;
			break;
		}

		default: {
			query = "Select title_nm, segment_id, title_nm from tsegment  where application_id = "
					+ sm.getApplicationId().toString();
		}

		}

		Hashtable structures = new Hashtable();
		try {
			structures = db.getLookupTable(query);
		} catch (ServicesException s) {

		}
		return new WebFieldSelect("structure_id", db.hasRow() ? db
				.getInteger("structure_id") : new Integer("0"), structures,
				true);

	}
}
