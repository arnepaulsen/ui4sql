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
 * 3/13 as 'target' to list query 
 * */

/**
 * Project Table-of-Contents
 * 
 * Not used now... but can be revived.
 * 
 * An index into all artifacts on a ProjectId ... it's only used by the list
 * page, then it points into the target data manager so there are no WebFields
 * or dbFields * 3/7 add filtering for tuser_project
 */
public class ProjectTOCPlugin extends AbsProjectPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/
	public ProjectTOCPlugin() throws services.ServicesException {
		super();

		this.setTableName("ProjectTOC"); // dummy name
		this.setKeyName("there's not a key");
		this.setTargetTitle("Project Table of Contents");

		this.setListHeaders(new String[] { "Kind", "Reference Id", "Title" });

		this.setMoreListColumns(new String[] { "kind_id", "reference_id",
				"title" });
		
		this.setAddOk(false);
	}


	public String getListQuery() {

		return "select 'Project' as target, project_id as rowKey, 'Charter    ' as kind_id, chng_req_no as reference_id, project_name as title "
				+ " from tproject where project_id = "
				+ sm.getProjectId().toString()
				+ " union "
				+ " select 'Issue   ' as target, issue_id as rowKey, 'Issue    ' as kind_id,  reference_nm as reference_nm , title_nm as title "
				+ " from tissue where project_id = "
				+ sm.getProjectId().toString()
				+ " union "
				+ " select 'Impact' as target, impact_id as rowKey, 'Impact' as kind_id,  reference_nm , title_nm as title from timpact "
				+ " left join tproject on timpact.project_id = tproject.project_id  where 1=1 ";
	}

	public Hashtable getWebFields(String mode) {
		WebFieldString wf = new WebFieldString("nada");

		WebFieldString[] wfs = { wf };
		return webFieldsToHT(wfs);
	}

}
