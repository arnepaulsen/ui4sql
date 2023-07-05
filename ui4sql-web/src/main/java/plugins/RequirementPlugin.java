/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import services.ServicesException;
import forms.*;

/**
 * 
 * 2/15 added mySql 3/10 use tcodes 3/13 as 'target' to list query 4/10 set
 * projectid on add, rearrange
 * 
 * 5/28/06 make 'Testcase' a child form
 */

public class RequirementPlugin extends AbsApplicationPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/
	public RequirementPlugin() throws services.ServicesException {
		super();
		this.setTableName("trequirement");
		this.setKeyName("requirement_id");
		this.setTargetTitle("Requirements");

		this.setHasDetailForm(true); // detail is the Codes form
		this.setDetailTarget("Testcase");
		this.setDetailTargetLabel("Test&nbsp;Cases");

		this.setListSelectorColumnFlags(new boolean[] { false, false, false,
				false, false, true});
		
		this.setListHeaders(new String[] { "Reference", "Title", "Type",
				"Status", "Priority", "SubProject" });

		this.setMoreListColumns(new String[] { "reference_nm", "title_nm",
				"t.code_desc as theType ", "stat.code_desc as theStat",
				"p.code_desc as priority", "s.code_desc as SubProj" });
		this
				.setMoreListJoins(new String[] {
						" left join tproject on trequirement.project_id = tproject.project_id ",
						" left join tcodes stat on trequirement.status_cd = stat.code_value and stat.code_type_id = 5 ",
						" left join tcodes  t on trequirement.type_cd = t.code_value and t.code_type_id = 15  ",
						" left join tcodes  p on trequirement.priority_cd = p.code_value and p.code_type_id = 7",
						" left join tcodes s on trequirement.sub_cd = s.code_value and tproject.chng_req_nm = s.code_desc2  and s.code_type_id = 157" });

	}

	public String getListAnd() {

		StringBuffer sb = new StringBuffer();
		
		sb.append(" and trequirement.project_id = " + sm.getProjectId().toString());

		if (sm.Parm("FilterSubProject").length() > 0 && !sm.Parm("FilterSubProject").equalsIgnoreCase("0")) {
			sb.append(" and sub_cd = '" + sm.Parm("FilterSubProject") + "'");
		}
		
		return sb.toString();
		
		
	}
	
	public WebField getListSelector(int columnNumber) {

		
		// Just get contracts that actually have an adHoc assigned
		if (columnNumber > 1) {
			
			String qry = "select c.order_by as odor, code_value, code_desc "
				+ " from tproject p join tcodes c on p.chng_req_nm = c.code_desc2 "
				+ " where p.project_id = "
				+ sm.getProjectId().toString()
				+ " and c.code_type_id = 157  ";
			
		
			Hashtable subs = new Hashtable();

			try {
				subs = db.getLookupTable(qry);
			} catch (ServicesException e) {
			}
			return getListSelector("FilterSubProject", "", "Sub-Project ? ",
					subs);
		}

		

		// will never get here
		Hashtable ht = new Hashtable();
		return getListSelector("dummy", new Integer(""), "badd..", ht);

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

		/*
		 * Set up for detail form 'testcase'
		 */
		if (parmMode.equalsIgnoreCase("show")) {

			sm.setRequirementId(db.getInteger("requirement_id"), db
					.getText("title_nm"));
		}

		// the "REQUIREMENT' table has sub-project codes, and linked via the
		// requriment table column 'chng_req_nm' to the codes table column
		String codes = "select c.order_by as odor, code_value, code_desc "
				+ " from tproject p join tcodes c on p.chng_req_nm = c.code_desc2 "
				+ " where p.project_id = "
				+ sm.getProjectId().toString()
				+ " and c.code_type_id = 157  ";

		Hashtable subProjects = db.getLookupTable(codes);

		Hashtable htProcesses = db
				.getLookupTable("select title_nm as odor, workflow_id, title_nm from tworkflow");

		/*
		 * id's
		 */
		ht.put("project_id", new WebFieldSelect("project_id",
				addMode ? new Integer("0") : db.getInteger("project_id"), sm
						.getProjectFilter()));

		ht.put("owner_uid", new WebFieldSelect("owner_uid",
				addMode ? new Integer("0") : db.getInteger("owner_uid"), sm
						.getUserHT(), true));

		ht.put("workflow_id", new WebFieldSelect("workflow_id",
				addMode ? new Integer("0") : db.getInteger("workflow_id"),
				htProcesses));

		ht.put("sub_cd", new WebFieldSelect("sub_cd", addMode ? "" : db
				.getText("sub_cd"), subProjects));

		/*
		 * codes
		 */
		ht.put("type_cd", new WebFieldSelect("type_cd", addMode ? "" : db
				.getText("type_cd"), sm.getCodes("REQUIREMENT")));

		ht.put("status_cd", new WebFieldSelect("status_cd", addMode ? "" : db
				.getText("status_cd"), sm.getCodes("STATUS")));
		
		ht.put("priority_cd", new WebFieldSelect("priority_cd", addMode ? "New"
				: db.getText("priority_cd"), sm.getCodes("PRIORITY")));

		/*
		 * Numbers (put as strings)
		 */
		ht.put("benefit_flt", new WebFieldString("benefit_flt", (addMode ? ""
				: db.getText("benefit_flt")), 6, 6));

		/*
		 * strings
		 */
		ht.put("reference_nm", new WebFieldString("reference_nm", (addMode ? ""
				: db.getText("reference_nm")), 16, 32));

		ht.put("title_nm", new WebFieldString("title_nm", (addMode ? "" : db
				.getText("title_nm")), 64, 128));

		/*
		 * blobs
		 */
		ht.put("desc_blob", new WebFieldText("desc_blob", addMode ? "" : db
				.getText("desc_blob"), 4, 80));

		ht.put("notes_blob", new WebFieldText("notes_blob", addMode ? "" : db
				.getText("notes_blob"), 4, 80));

		ht.put("problem_blob", new WebFieldText("problem_blob", addMode ? ""
				: db.getText("problem_blob"), 4, 80));

		ht.put("alternatives_blob", new WebFieldText("alternatives_blob",
				addMode ? "" : db.getText("alternatives_blob"), 4, 80));

		return ht;

	}
}
