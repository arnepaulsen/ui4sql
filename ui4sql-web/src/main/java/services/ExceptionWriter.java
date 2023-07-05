/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

/*
 * Exception Writer
 * 
 * 
 * writes texception table for rule violations
 * 
 * gets the process rules for a project, and compares
 * to actual values... then creates exceptions for variations
 * 
 * called from ExceptionPlugin
 * 
 * inputs : ProjectId, DbInterface (with open connection)
 * 
 * Change:
 *   new call pattern for addRow - pass boolean bAutoIncrementKey
 * 
 */

import router.SessionMgr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;

import db.DbFieldDateTime;
import db.DbFieldInteger;
import db.DbInterface;
import db.DbFieldString;
import java.sql.ResultSetMetaData;

public class ExceptionWriter {

	/*
	 * rules defined in tprocess
	 */

	private int current_stage_id;

	private int maxPrograms;

	private int maxFte;

	private int maxAmount;

	private int maxStructures;

	private int maxInterfaces;

	private int maxPoints;

	private DbInterface db;

	private Integer projectId;

	private Integer auditKey;

	private SessionMgr sm;

	/*
	 * these are only used during life of audit methods, not save across calls
	 */
	int[] theAnswer = new int[2];

	public ExceptionWriter(DbInterface parmDb, SessionMgr parmSm,
			Integer parmProjectId, Integer parmAuditKey) {

		db = parmDb;
		sm = parmSm;
		projectId = parmProjectId;
		auditKey = parmAuditKey;
		getRules();

	}

	public Integer getStageId() {
		return new Integer(current_stage_id);
	}

	/*
	 * returns int[] as: 0 = max as per rule 1 = actual count
	 * 
	 */
	public boolean auditInterfaces() {

		String sQuery = "select project_id , count(*) as the_count from tinterface "
				+ " where project_id  = "
				+ projectId.toString()
				+ " group by project_id";

		return checkIt(sQuery, "I", "Interface Limit Exceeded", maxInterfaces);

	}

	public boolean auditFunctionPoints() {

		String sQuery = "select project_id , sum(total_points_no) as the_count from tpoint "
				+ " where project_id  = "
				+ projectId.toString()
				+ " group by project_id";

		return checkIt(sQuery, "F", "Function Points Limit Exceeded", maxPoints);

	}

	public boolean auditEffort() {

		String sQuery = "select project_id , est_days_no as the_count from tproject "
				+ " where project_id  = " + projectId.toString();

		return checkIt(sQuery, "E", "Effort Limit Exceeded", maxFte);

	}

	public boolean auditModules() {

		String sQuery = "select project_id , count(*) as the_count from tmodule "
				+ " where project_id  = "
				+ projectId.toString()
				+ " group by project_id";

		return checkIt(sQuery, "M", "Module Limit Exceeded", maxInterfaces);

	}
	
	public boolean auditStructures() {

		String sQuery = "select project_id , count(*) as the_count from ttable "
				+ " where project_id  = "
				+ projectId.toString()
				+ " group by project_id";

		return checkIt(sQuery, "S", "Data Structures Limit Exceeded", maxStructures);

	}
	

	/*
	 * audit list of required deliverable against what is actually present
	 */
	//	
	public boolean auditDeliverables() {
		boolean ok = true;

		/*
		 * get list of required deliverables for this stage
		 */

		String sQuery = ("select sd.stage_id, dlvtype.code_desc as DelvName, sd.type_cd, sd.required_flag  ,  if ( d.deliverable_status_cd = 'CMP', 'Y', 'N') as complete_cd"
				+ " from tproject "
				+ " join tstage on tproject.current_stage_id = tstage.stage_id  "
				+ " join tstage_deliverable sd on tstage.stage_id = sd.stage_id "
				+ " join tcodes dlvtype on sd.type_cd = dlvtype.code_value and dlvtype.code_type_id = 10 "
				+ " left join tdeliverable d on sd.type_cd = d.deliverable_cd and d.project_id = tproject.project_id"
				+ " where sd.required_flag = 'Y' "
				+ " and tproject.project_id = " + projectId.toString());

		try {
			ResultSet rs = db.getRS(sQuery);

			// check if complete, or deliverable join is not found
			while (rs.next() == true) {

				debug("testing deliverable null");

				if (rs.getString("complete_cd").equals("N")) {
					ok = false;
					writeException("D", rs.getString("DelvName"), 1, 0);

				}
			}

		} catch (ServicesException e) {
			// TODO Auto-generated catch block
			ok = false;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			ok = false;
		}

		return ok;

	}

	/*
	 * uses they query result to compare against a limit, and write to table
	 * texception if appropriate.
	 * 
	 * return true if pass, false if fail
	 */

	private boolean checkIt(String pQuery, String exceptionType,
			String errorTitle, int iLimit) {

		int iActualCount = 0;

		// count(*) is a jdbc bigint ... -5 in jdbc

		ResultSet rs;
		try {
			rs = db.getRS(pQuery);
			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next() == true) {
				int t = rsmd.getColumnType(1);
				t = rsmd.getColumnType(2);
				iActualCount = rs.getInt("the_count");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean ok = !(iActualCount > iLimit);

		if (!ok) {
			writeException(exceptionType, errorTitle, iLimit, iActualCount);
		}

		return ok;

	}

	private void writeException(String exceptionType, String title, int iLimit,
			int iActual) {

		Hashtable ht = new Hashtable();

		ht.put("project_id", new DbFieldInteger("project_id", projectId));
		ht.put("audit_id", new DbFieldInteger("audit_id", auditKey));
		ht.put("type_cd", new DbFieldString("type_cd", exceptionType));
		ht.put("status_cd", new DbFieldString("status_cd", "P"));
		ht.put("title_nm", new DbFieldString("title_nm", title));
		ht.put("rule_max_no", new DbFieldInteger("rule_max_no", new Integer(
				iLimit)));
		ht.put("actual_no", new DbFieldInteger("actual_no",
				new Integer(iActual)));
		ht.put("added_uid", new DbFieldInteger("added_uid", sm.getUserId()));
		ht.put("added_date", new DbFieldDateTime("added_date", new Date()));

		try {
			db.insertRow("texception", "exception_id", ht, true);
		} catch (ServicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getRules() {

		debug("get rules");

		String sQuery = "select current_stage_id, max_fte_no, max_dollar_k_amt, max_structure_no, max_function_point_no, max_program_no, max_interface_no  "
				+ " from tproject join tprocess on tproject.process_id = tprocess.process_id "
				+ " where tproject.project_id = " + projectId.toString();

		try {
			ResultSet rs = db.getRS(sQuery);

			debug("got rules");
			// just one record.
			while (rs.next() == true) {
				current_stage_id = rs.getInt("current_stage_id");
				maxPrograms = rs.getInt("max_program_no");
				maxPoints = rs.getInt("max_function_point_no");
				maxStructures = rs.getInt("max_structure_no");
				maxInterfaces = rs.getInt("max_interface_no");
				maxAmount = rs.getInt("max_dollar_k_amt");
				maxFte = rs.getInt("max_fte_no");

			}
		} catch (ServicesException se) {
			debug("ServiceException: " + se.toString());
		} catch (SQLException se) {
			debug("SQLException: " + se.toString());
		}

		debug("rules exiting");

	}

	private void debug(String parmMsg) {
		if (true)
			System.out.println("RssWriter: " + parmMsg);
	}
}
