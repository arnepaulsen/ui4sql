/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

import java.util.*;
import db.*;

/*
 * ProjectService Class
 * 
 * Purpose:  to create projects and return project info
 * 
 */
public class ProjectService {

	DbInterface db;

	boolean bKeyAutoIncrement = true;

	/*
	 * Constructor - assumes dbInterface already has Connection establised
	 */

	public ProjectService(DbInterface dbInterface) {

		db = dbInterface;

	}

	public Integer createProject(String projTitle, String projDescription,
			String typeCd, Integer applicationId, Integer divisionId,
			Integer rfcId, Integer processId, Integer projectManagerId, Integer userId) {

		Hashtable<String, DbField> ht = new Hashtable<String, DbField>();

		ht.put("title_nm", new DbFieldString("title_nm", projTitle));
		ht.put("desc_blob", new DbFieldString("desc_blob", projDescription));
		
		ht.put("type_cd", new DbFieldString("type_cd", typeCd));
		ht.put("status_cd", new DbFieldString("status_cd", "NEW"));

		
		ht.put("added_uid", new DbFieldInteger("added_uid", userId));
		ht.put("added_date", new DbFieldDate("added_date", new Date()));

		ht.put("rfc_no", new DbFieldInteger("rfc_no", rfcId));
		ht.put("process_id", new DbFieldInteger("process_id", processId));
		ht.put("division_id", new DbFieldInteger("division_id", divisionId));
		ht.put("primary_application_id", new DbFieldInteger(
				"primary_application_id", applicationId));
		ht.put("pm_uid", new DbFieldInteger("pm_uid", projectManagerId));

		
		try {
			return db
					.insertRow("tproject", "project_id", ht, bKeyAutoIncrement);
		}

		catch (ServicesException servicesException) {
			return new Integer("-1");
		}

	}

}
