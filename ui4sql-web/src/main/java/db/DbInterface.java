/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package db;

import java.sql.*;
import java.sql.Connection;
import services.ServicesException;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Hashtable;
import java.util.Enumeration;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;

// import org.apache.log4j.Logger;

/**
 * @author Arne Paulsen
 * 
 * 		4/15/21 - Huge fix to retrive the 'getColumnLabel' instead of getColumnName' for query ' select field AS label';
 * 			something must have changed in JDBC as the getColumnName returns the specific database column, not the  'field AS name'
 *  
 * 
 *         4/3/07 caution .. make Resultset public so can call directly
 * 
 * 
 *         3/14 change getList to put serial htKey instead of using key from
 *         rs[0] take out 'ORDER BY' on getLIst 3/21 add blob 3/22 return empty
 *         string if value is null, insteaof message 3/24 getLookupTable now
 *         returns 'ordered-by' as the hashkey, and the value/desc pair in an
 *         array[2]
 * 
 *         Change Log: 9/10/05 add getChar() 4/17/06 fix copy function, check
 *         for null exception 8/11/06 remove debug stmts from getLookupTable
 *         remove constructors with table names remove init methods add
 *         'dbProduct' to constructor
 * 
 *         8/21/06 - add functions to allow db portability: - getconcat (syntax
 *         depends on db product
 * 
 *         9/15/06 clean up unused, add <> to ht
 * 
 *         9/21/06 restore 'order by' parameter to getLookupTAble (s, s,s,s)
 *         10/30/06 : add sql Server/MySQL logic paths in the insert ... mySQL
 *         returns long, SQL Server returns int
 * 
 *         8/21/07 Implemenet SQL Server user-defined functions for concat, and
 *         remove .. the sql string fix procedures
 * 
 * 
 *         6/16/09 - Add type=3 = integer to jdbc type fields
 * 
 *         12/8/10 - Arne, make getNewRowKey a public method, and remove
 *         parameters that were used for SQL Server version.
 *         
 *         3/2/22 - Fix depreciated conversion of NewRowKey to integer
 * 
 * 
 */

public class DbInterface {

	// private static int errJDBCDuplicateRow = 2627;

	public ResultSet ourRS;

	public boolean debug = true;

	public String dbProduct = "";

	public final static int OK = 0;

	public DB2Conn dbConn;

	public Connection conn;

	public String errorMsg = " ";

	public String tableName;

	public String keyName;

	public String whereClause;

	public static SqlHelper sqlH = new SqlHelper();

	private boolean hasRow;

	public Hashtable<String, DbField> hashFields = new Hashtable<String, DbField>();

	public ResultSetMetaData rsmd;

	public String updateQuery;

	public String insertQuery;

	public String selectQuery;

	// db2
	// private static String constLimit = " FETCH FIRST 1 ROWS ONLY ";
	// mySql

	// ***************************************
	// CONSTRUCTORS - with Connection *
	// ***************************************

	public DbInterface() throws services.ServicesException {

	}

	public DbInterface(Connection parmConn, String pProduct) {

		dbProduct = pProduct;
		conn = parmConn;

	}

	public Connection getConnection() {
		return conn;
	}

	// *************************
	// PUBLIC METHODS
	// *************************

	public void closeConnection() throws services.ServicesException {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new services.ServicesException(e.toString());
		}
	}

	// *************************************
	// PUBLIC GET METHODS
	// *************************************

	private boolean isSQLServer() {

		// debug("isSqlServer : dbProduct : " + dbProduct);

		if (dbProduct.equalsIgnoreCase("SQLServer"))
			return true;
		else
			return false;
	}

	public boolean hasRow() {
		return hasRow;
	}

	// *************************************
	// PUBLIC SET METHODS
	// *************************************

	public Integer setRow(String parmTableName, String parmKeyName,
			String parmSelectQuery) throws services.ServicesException {

		tableName = parmTableName;
		keyName = parmKeyName;
		selectQuery = parmSelectQuery;

		// debug("setRow() : query : " + selectQuery);

		// caution.
		// ResultSet myRS = getRS(selectQuery);
		ourRS = getRS(selectQuery);

		return setFields(ourRS);
	}

	public Date getDateX(String column) {
		try {
			return ourRS.getDate(column);
		} catch (SQLException e) {
			return null;
		}
	}

	// *********************************************
	// general run query
	// *********************************************
	public int runQuery(String parmQuery) throws services.ServicesException {

		Statement stmt;
		try {
			// debug("..creating statement");
			stmt = conn.createStatement();
			//debug("dbInterface:runQuery: " + parmQuery);
			stmt.executeUpdate(parmQuery);
			// debug("DbInterface:runQuery is done ... query was : " +
			// parmQuery);
		} catch (SQLException se) {
			errorMsg = se.toString();
			int e = se.getErrorCode();

			if (e == 2627)
				return e;

			debug("SQLException in runQuery: " + e);
			throw new ServicesException(se.toString());

			// return true;
		} catch (Exception e) {
			debug("Java Exception in runQuery: " + e);
			return -1;
		}
		return 0;
	}

	/*
	 * copy row to history
	 * 
	 * copy the original key to the column name "setColumnName" set the log flag
	 * to yes then create new row
	 */

	public Integer logRow(String parmTableName, String parmKeyName,
			String parmSourceKey, String setColumnName,
			boolean bKeyAutoIncrement) throws services.ServicesException {

		tableName = new String(parmTableName);
		keyName = new String(parmKeyName);

		/*
		 * get existing row info
		 */
		String query = ("SELECT * FROM " + parmTableName + " WHERE "
				+ parmKeyName + " = " + parmSourceKey);

		// call setRow to get the existing row, which saves all the data in ht
		// hashFields!
		setRow(parmTableName, parmKeyName, query);

		DbFieldInteger theField = (DbFieldInteger) hashFields
				.remove(parmKeyName);

		Integer rowKey = theField.getInteger();

		Object o = hashFields.remove(setColumnName);
		hashFields
				.put(setColumnName, new DbFieldInteger(setColumnName, rowKey));

		DbFieldString flag = (DbFieldString) hashFields.get("log_flag");
		flag.setValue("Y");

		// now put back in the added_uid and added_date with current info

		String insertQuery = buildAddQuery(hashFields, bKeyAutoIncrement);

		runQuery(insertQuery);

		return getNewRowKey();
	}/*
	 * /* Copy a row
	 */

	public Integer copyRow(String parmTableName, String parmKeyName,
			Integer parmCopyKey, Integer userId, boolean bKeyAutoIncrement)
			throws services.ServicesException {

		tableName = new String(parmTableName);
		keyName = new String(parmKeyName);

		/*
		 * get existing row info
		 */
		String query = ("SELECT * FROM " + parmTableName + " WHERE "
				+ parmKeyName + " = " + parmCopyKey.toString());

		// call setRow to get the existing row, which saves all the data in ht
		// hashFields!
		setRow(parmTableName, parmKeyName, query);

		// * now do some surgery to manipulate the audit fields (approved_by,
		// approved_date... etc)

		Enumeration<String> en = hashFields.keys();

		// take out the audit fields.... they don't carry over on a copy
		// also.. take out date fields that are invalid.. they blow up the
		// subsequent insert
		while (en.hasMoreElements()) {
			String fieldName = en.nextElement();
			// debug("copy checking.." + fieldName);

			try {
				if (fieldName.equalsIgnoreCase("approved_uid")
						|| fieldName.equalsIgnoreCase("added_uid")
						|| fieldName.equalsIgnoreCase("submitted_uid")
						|| fieldName.equalsIgnoreCase("updated_uid")
						|| fieldName.equalsIgnoreCase("added_date")
						|| fieldName.equalsIgnoreCase("approved_date")
						|| fieldName.equalsIgnoreCase("updated_date")
						|| fieldName.equalsIgnoreCase("submitted_date")) {
					// debug("copy is removing " + fieldName);
					Object o = hashFields.remove(fieldName);
					if (o == null) {
						debug("didn't find the entry");
					}

				}
			} catch (Exception e) {
				debug("caught remove excetion");
				e.printStackTrace();

			}

		}

		// now put back in the added_uid and added_date with current info

		hashFields.put("added_uid", new DbFieldInteger("added_uid", userId));

		hashFields.put("added_date", new DbFieldDateTime("added_date",
				new Date()));

		// * use the hashfields to build the insert query
		String insertQuery = buildAddQuery(hashFields, bKeyAutoIncrement);

		runQuery(insertQuery);

		return getNewRowKey();
	} /*
	 * 
	 * Insert new row, based on Hashtable of DbFields
	 */

	public Integer insertRow(String parmTableName, String parmKeyName,
			Hashtable<String, DbField> parmHtFields, boolean bKeyAutoIncrement)
			throws services.ServicesException {

		tableName = parmTableName;
		keyName = parmKeyName;

		// debug("dbInterface - insertRow - starting");

		// debug("building add query " );
		insertQuery = buildAddQuery(parmHtFields, bKeyAutoIncrement);

		// debug("insert query " + insertQuery);
		int rc = runQuery(insertQuery);

		// debug(" insert rc = " + rc);

		
		// fixed depreciated Java constructor New Integer(string)  
		if (rc == 2627) {
			// debug("duplicate row ");

			return Integer.parseInt("-1");
			
			//return ri;
		}

		// debug("getting row id");

		if (bKeyAutoIncrement) {
			return getNewRowKey();
		}

		/*
		 * user-supplied key,,, so just give it back
		 */

		// debug (" user-supplied key ");
		DbField dbField = parmHtFields.get(parmKeyName);
		return (Integer) dbField.fieldValue;

	}

	/*
	 * fetch back the new key
	 * 
	 * SQLServer - use @@IDENTIDY
	 */
	public Integer getNewRowKey() throws services.ServicesException {

		String selectQuery;

		selectQuery = new String("SELECT last_insert_id()");

		ResultSet rs = getRS(selectQuery);

		/*
		 * Now get the key back as a long
		 */
		//Integer newRowKey = new Integer("0");]
		int i = 0;
		
		Integer newRowKey  = Integer.valueOf(i);
		
		
		

		try {
			if (rs.next() == true) {
				//newRowKey = (Integer) rs.getObject(1);
				
				i = rs.getInt(1);
				
				newRowKey =  Integer.valueOf(i);

			}
			rs.close();
		} catch (SQLException e) {
			debug("exception after insert , trying to get row key "
					+ e.toString());
		}

		return newRowKey;
	}

	// builds ' ( field1, ... fieldn) values (value1, ... valueN)"
	private String buildAddQuery(Hashtable<String, DbField> parmHT, boolean bKeyAutoIncrement) {
		// debug("DbIntef:buildAddquery columns ... starting");
		// debug("DbIntef:buildAddquery columns" + tableName);

		Enumeration<String> en = parmHT.keys();
		DbField dbField;

		StringBuffer queryBuffer = new StringBuffer("INSERT INTO " + tableName
				+ " (");
		String comma = " ";

		while (en.hasMoreElements()) {
			String fieldName = en.nextElement();

			// debug("field: " + fieldName);

			dbField = parmHT.get(fieldName);

			if (!bKeyAutoIncrement
					|| !keyName.equalsIgnoreCase(dbField.getName())) {
				queryBuffer.append(comma + dbField.getName());
				comma = ", ";
			}

		}

		// debug("DbIntef:buildAddquery values" );

		comma = " ";
		queryBuffer.append(" ) VALUES (");

		// / TODO: Can the first enumeration be re-used?

		Enumeration<String> en2 = parmHT.keys();

		while (en2.hasMoreElements()) {
			String fieldName = en2.nextElement();

			// debug(" adding values " + fieldName);

			try {
				dbField = parmHT.get(fieldName);

				if (!bKeyAutoIncrement
						|| !keyName.equalsIgnoreCase(dbField.getName())) {
					// debug(" adding " + fieldName);
					queryBuffer.append(comma + (String) dbField.getSQL() + " ");
					comma = ", ";
				}
			} catch (Exception e) {

				queryBuffer.append(comma + "''");
				comma = ", ";

			}

		}

		queryBuffer.append(") ");
		return queryBuffer.toString();
	}

	// FIXED Parameterize Hashtable with ?? but no idea if correct ??
	public int updateRow(String parmTableName, String parmKeyName,
			String parmKey, Hashtable<?, ?> htDbFields)
			throws services.ServicesException {

		tableName = parmTableName;
		keyName = parmKeyName;
		updateQuery = buildUpdateQuery(parmKey, htDbFields);

		debug("Query : " + updateQuery);
		return runQuery(updateQuery);
	}

	// builds ' ( field1, ... fieldn) values (value1, ... valueN)"
	private String buildUpdateQuery(String parmKey, Hashtable<?, ?> parmHT) {

		//System.out.println("building update query !!!");
		
		Enumeration<?> en = parmHT.keys();
		DbField dbField;

		StringBuffer sb = new StringBuffer("UPDATE " + tableName + " SET ");
		String comma = " ";

		// * loop through the HT of dbFields, append the update SQL to the query
		// string
		while (en.hasMoreElements()) {
			String fieldName = (String) en.nextElement();
			dbField = (DbField) parmHT.get(fieldName);
			if (!keyName.equalsIgnoreCase(dbField.getName())) {
				sb.append(comma + dbField.getName());
				sb.append(" = " + (String) dbField.getSQL());
				comma = ", ";
			}
		}

		sb.append(" WHERE " + keyName + " = " + parmKey);
		return sb.toString();
	}

	/*
	 * THE OLD WAY
	 */

	// *************************************
	// UPDATE ROW
	// *************************************
	public int updateRow(String parmTableName, String parmKeyName,
			String parmKey, DbField[] parmUpdateFields)
			throws services.ServicesException {

		tableName = parmTableName;
		keyName = parmKeyName;
		updateQuery = buildUpdateQuery(parmKey, parmUpdateFields);

		// debug("update query " + updateQuery);
		return runQuery(updateQuery);
	}

	// builds ' ( field1, ... fieldn) values (value1, ... valueN)"
	private String buildUpdateQuery(String parmKey, DbField[] parmFields) {

		StringBuffer queryBuffer = new StringBuffer("UPDATE " + tableName
				+ " SET ");
		String comma = " ";
		for (int x = 0; x < parmFields.length; x++) {
			if (!keyName.equalsIgnoreCase(parmFields[x].getName())) {
				queryBuffer.append(comma + parmFields[x].getName());
				queryBuffer.append(" = " + (String) parmFields[x].getSQL());
				comma = ", ";
			}
		}

		queryBuffer.append(" WHERE " + keyName + " = " + parmKey);
		return queryBuffer.toString();
	}

	// ********************************************
	// GET a lookup table that has a String arg
	// *******************************************
	public Hashtable<Object, Object> getLookupTable(String parmTableName, String valueColumn,
			String descColumn) throws services.ServicesException {

		// System.out.println(" getting select list... arne here : ");

		String query = new String("SELECT  " + valueColumn + " as odor,  "
				+ valueColumn + ", " + descColumn + " FROM " + parmTableName
				+ " ORDER BY 1 ");

		// System.out.println(" getting select list... arne here : " + query);

		return getLookupTable(query);

	}

	// ********************************************
	// GET a lookup table that has a String arg - with an ORDER-BY parameter

	// careful.. the order-by has to be in the select statement
	// *******************************************
	public Hashtable<Object, Object> getLookupTable(String parmTableName, String valueColumn,
			String descColumn, String parmOrderBy)
			throws services.ServicesException {
		String query = new String("SELECT  " + valueColumn + "," + valueColumn
				+ ", " + descColumn + " FROM " + parmTableName + " ORDER BY  "
				+ parmOrderBy);

		// debug(" getting select list... : " + query);

		return getLookupTable(query);

	}

	// first column must be the 'ordered_by" column, then col 2/3 must be
	// value/desc pair
	public Hashtable<Object, Object>  getLookupTable(String query)
			throws services.ServicesException {

		Hashtable<Object, Object> ht = new Hashtable<Object, Object>();

		// debug("lookup query " + query);

		ResultSet rs = getRS(query);

		try {
			while (rs.next() == true) {
				Object[] obj = new Object[2];

				obj[0] = rs.getObject(2); // lookup arg
				obj[1] = (String) rs.getObject(3); // lookup result
				// debug("getLookupTable ... desc = " + obj[1]);
				// debug("... from the rs: " + (String) rs.getObject(3));
				//Object temp = rs.getObject(1);   3/3/22 COMMENT - NOT USED
				ht.put(rs.getObject(1), obj); // column 1 is the key!!!
			}
			rs.close(); // / TODO: WATCH OUT FOR RS.CLOSE
		} catch (SQLException e) {
			System.out.println("DbInterface:getStringLookupTable: "
					+ e.toString());
			throw new services.ServicesException(e.toString());

		} catch (Exception e) {
			System.out.println("DbInterface:getStringLookupTable: "
					+ e.toString());
			throw new services.ServicesException(e.toString());
		}

		return ht;
	}

	/*
	 * This hashtable can any number of fields in the ht array.
	 * 
	 * the fieldCount is the number of fields to store, which should always be
	 * one more than the number of fields in the query/
	 * 
	 * we could elegant and use getMetadata on the RS and figure this out
	 * here... but this will do for now.
	 * 
	 * the first rs column is the ht key, so the ht array will have rs columns -
	 * 1
	 */

	public Hashtable<Object, Object[]> getLookupTable(int fieldCount, String query)
			throws services.ServicesException {

		Hashtable<Object, Object[]> ht = new Hashtable<Object, Object[]>();

		ResultSet rs = getRS(query);

		try {
			while (rs.next() == true) {
				Object[] obj = new Object[fieldCount];

				for (int x = 0; x < fieldCount; x++) {
					// obj[] is 0-based, rs rows is 1-based, and we start 2nd
					// column
					obj[x] = rs.getObject(x + 2);
				}
				ht.put(rs.getObject(1), obj); // rs column 1 is the key!!!
			}
			rs.close(); // / TODO: WATCH OUT FOR RS.CLOSE
		} catch (SQLException e) {
			System.out.println("DbInterface:getStringLookupTable: "
					+ e.toString());
			throw new services.ServicesException(e.toString());

		} catch (Exception e) {
			System.out.println("DbInterface:getStringLookupTable: "
					+ e.toString());
			throw new services.ServicesException(e.toString());
		}
		return ht;
	}

	// *************************************
	// GET LIST - HASTTABLE OF DBFIELDS
	// *************************************

	public Hashtable<Integer, DbField[]> getList(String parmTableName, String parmKeyName,
			String parmQuery) throws services.ServicesException {

		// debug("getList(table, key, query) query : " + parmQuery);

		tableName = parmTableName;
		keyName = parmKeyName;
		return getList(parmQuery);
	}

	public Hashtable<Integer, DbField[]> getList(String parmListQuery) // ,
			throws services.ServicesException {

		Hashtable<Integer, DbField[]> ht = new Hashtable<Integer, DbField[]>();

		ResultSet rs = getRS(parmListQuery);

		int htKey = 0;
		
		try {
			// debug(" getList... starting ht build for # columns "
			// + rsmd.getColumnCount());

			while (rs.next() == true) {

				// debug("metadata count" + rsmd.getColumnCount());
				DbField[] dbFields = new DbField[rsmd.getColumnCount()];
				// debug(" done building the empty dbField[] ht");
				// create a DbField for each column in the row
				for (int x = 0; x < rsmd.getColumnCount(); x++) {
					//dbFields[x] = getDbField(rs, rsmd.getColumnName(x + 1));
					dbFields[x] = getDbField(rs, rsmd.getColumnLabel(x + 1));
					//debug (" medata field : " + rsmd.getColumnLabel(x + 1));
				}
				htKey++;
			
				// fix Integer constructor depreciation
				ht.put(Integer.valueOf(htKey), dbFields);
				
				
				// return Integer.parseInt("-1");
			}
		} catch (SQLException e) {
			debug("DbInterface:getList: " + e.toString());
			throw new services.ServicesException(e.toString());

		} catch (Exception e) {
			debug("DbInterface:getList: " + e.toString());
			throw new services.ServicesException(e.toString());
		}

		// debug("getList... leaving ok");
		return ht;
	}

	private DbField setField(ResultSet parmRs, String columnName)
			throws services.ServicesException {
		// debug("DbInterface:setField, column " + columnName);
		DbField field = getDbField(parmRs, columnName);
		hashFields.put(columnName, field);
		return field;
	}

	// return a DbField for the specified column in the current ResultSet row
	private DbField getDbField(ResultSet parmRs, String parmColumn) {
		// debug("DbInterface:setDhField - column " + parmColumn);

		int t = 0;

		try {

			int i = parmRs.findColumn(parmColumn);

			if (rsmd == null) { // just get it once
				// debug("...getting new metadata for rs");
				rsmd = parmRs.getMetaData();
			}

			t = rsmd.getColumnType(i);

			// debug("column: " + parmColumn + " type :" + t);

			// check for blob

			if (t == -4) {
				// debug("getting a blob... column type is " + t);

				Blob blob = parmRs.getBlob(i);
				byte[] b = blob.getBytes(1, (int) blob.length());
				String s = new String(b);
				// debug("the blob string is " + s);
				return new DbFieldString(parmColumn, s);
			}

			if (t == 12 || t == 1) {
				// debug("getting a string... column type is " + t
				// + " value is : " + parmRs.getString(i));

				return new DbFieldString(parmColumn, parmRs.getString(i));
			}

			if (t == 91) {
				// debug("getting a date... column type is " + t);
				try {
					return new DbFieldDate(parmColumn, parmRs.getDate(i));
				} catch (SQLException e) {
					return new DbFieldDate(parmColumn);
				} catch (Exception e) {
					return new DbFieldDate(parmColumn);
				}
			}

			// if date not set, then sql exception, keep as a null DbFieldDate,
			// not an empty string
			if (t == 93) {
				try {
					return new DbFieldDateTime(parmColumn,
							parmRs.getTimestamp(i));
				} catch (SQLException e) {
					return new DbFieldDateTime(parmColumn);
				} catch (Exception e) {
					return new DbFieldDateTime(parmColumn);
				}
			}

			if (t == 4 || t == -6 || t == 5 || t == -5 || t == 8 || t == 3) {
				try {
					// debug("integer");
					int z = parmRs.getInt(i);
					Integer xx = new Integer(z);
					return new DbFieldInteger(parmColumn, xx);
				} catch (Exception e) {
					debug("exception converting integer");
					return new DbFieldInteger(parmColumn, new Integer("0"));
				}

			}

			if (t == 7) {
				// debug("float");
				return new DbFieldFloat(parmColumn, new Float(
						parmRs.getFloat(i)));
			}

			// varchar -3, text = -1
			if (t == -3 || t == -1) {
				// debug("fetching a blob to a string !");

				byte[] ba = parmRs.getBytes(i);

				String s = new String(ba);

				// debug("returnnig a dbFieldstring");

				return new DbFieldString(parmColumn, s);
			}

			// debug(" ALERT getDbField - rs class " + o.getClass().getName());
			// debug("getDbField - type is " + o.getClass().getName());
		} catch (SQLException e) {
			// debug("getDbField - sql exception getting string : " +
			// e.toString());
		} catch (Exception e) {
			// debug("getDbField - Exception getting string" + e.toString());
			// debug(" error : " + e.toString());
		}

		// field is probably null
		// debug("getDbField - oops for " + parmColumn + " is: " + t);

		return new DbFieldString(parmColumn, "");
	}

	public int deleteRow(String parmTableName, String parmKeyName,
			String parmRowKey) throws services.ServicesException {

		tableName = parmTableName;
		keyName = parmKeyName;
		return deleteRow(parmRowKey);

	}

	public int deleteRow(String parmRowKey) throws services.ServicesException {

		String query = new String("DELETE FROM " + tableName + " WHERE "
				+ keyName + " = " + parmRowKey);
		return runQuery(query);
	}

	private Integer setFields(ResultSet myRS) throws services.ServicesException {

		// debug("DbInterface;setFields - starting ");

		Integer rc;
		// get each DbField from the hashFields, and set the value from the RS
		try {
			// debug("setFields ... getting MetaData !!!!!!!");
			int columnCount = rsmd.getColumnCount();
			// debug(".. columnCount = " + columnCount);

			if (myRS.next() == true) {
				// for (int x = 0; x < selectColumns.length; x++) {
				for (int x = 1; x < columnCount + 1; x++) {
					String columnName = new String(rsmd.getColumnLabel(x));
					//debug("calling setField for " + columnName);
					// setField(myRS, selectColumns[x]);
					setField(myRS, columnName);
					// debug("setField done ");
				}
				// debug("setFields all done" + keyName);
				rc = stringToInteger(getText(keyName));
				hasRow = true;
			} else {
				rc = new Integer("0");
				hasRow = false;
			}
		} catch (SQLException se) {
			throw new services.ServicesException("DbInterface:setFields - "
					+ se.toString());
		}
		rc = new Integer("0");
		// debug/("DbInterface;setFields - done ");
		return rc;
	}

	public char getChar(String parmFieldName) {
		// debug(":getText for " + parmFieldName);
		try {
			DbField f = hashFields.get(parmFieldName);
			return f.getText().charAt(0);
		} catch (Exception e) {
			return ' ';
		}
	}

	public String getText(String parmFieldName) {
		// debug(":getText for " + parmFieldName);
		try {
			DbField f = hashFields.get(parmFieldName);
			return f.getText();
		} catch (Exception e) {
			return new String("");
		}
	}

	public Date getDate(String parmFieldName) {
		try {
			DbField f = hashFields.get(parmFieldName);
			Date d = (Date) f.getObject();
			return d;
		} catch (Exception e) {
			return new Date();
		}
	}

	public Integer getInteger(String parmFieldName) {
		DbField f = hashFields.get(parmFieldName);
		return (Integer) f.getObject();
	}

	public Float getFloat(String parmFieldName) {
		DbField f = hashFields.get(parmFieldName);
		return (Float) f.getObject();
	}

	public Object getObject(String parmFieldName) {
		DbField f = hashFields.get(parmFieldName);
		return f.getObject();
	}

	public Integer getKey() {
		DbField f = hashFields.get(keyName);
		return new Integer(f.getText());
	}

	// change this to "get the object"
	// public String getValue(String parmFieldName) {
	// DbField f = (DbField) hashFields.get(parmFieldName);
	// return f.getText();
	// }

	public String getWhereClause() {
		return whereClause;
	}

	public boolean rowExists(String query) {
		boolean answer = false;
		try {
			ResultSet rs = getRS(query);
			if (rs.next() == true) {
				answer = true;
			}
			rs.close();
		} catch (services.ServicesException se) {
		} catch (java.sql.SQLException sql) {
		}

		return answer;
	}

	// return a rs from a query string
	public ResultSet getRS(String parmQuery) throws ServicesException {

		Statement stmt;
		ResultSet rs = null;

		String query = new String(parmQuery);

		try {
			if (conn == null) {
				debug("DbInterface:getRs - conn is null");
			}
		} catch (Exception e) {
			return rs;
		}

		debug("DbInterface:getRS(string): " + query);

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			rsmd = rs.getMetaData();
		} catch (SQLException se) {
			debug("dbInterface:getRS - " + se.toString());
			errorMsg = se.toString();
			throw new ServicesException("DbInterface:getRS: " + se.toString());
		}
		// debug("DbInterface:getRS - ending ok");

		return rs;

	}

	/*
	 * 
	 * Just returns one string value as per a query -
	 * 
	 * Warning --- don't overuse ....
	 */
	public String getColumn(String parmQuery) throws ServicesException {

		Statement stmt;
		ResultSet rs;
		String s = new String("");
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(parmQuery);
			if (rs.next()) {
				s = rs.getString(1);
			}
			rs.close();
			return s;

		} catch (SQLException se) {
			debug("dbInterface:getRS - " + se.toString());
			errorMsg = se.toString();
			throw new ServicesException("DbInterface:getRS: " + se.toString());
		}
	}

	public Integer getRSInt(String parmQuery) throws ServicesException {

		Statement stmt;
		ResultSet rs;
		Integer i = new Integer("-1");
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(parmQuery);
			if (rs.next()) {
				i = new Integer(rs.getInt(1)); // java 1.4
				// java 1.5 i = rs.getInt(1);
			}
			rs.close();
			return i;

		} catch (SQLException se) {
			debug("dbInterface:getRS - " + se.toString());
			errorMsg = se.toString();
			throw new ServicesException("DbInterface:getRS: " + se.toString());
		}
	}

	/*
	 * just return 1 integer from a query...
	 */
	public Integer getColumnInt(String parmQuery) throws ServicesException {

		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(parmQuery);
			if (rs.next()) {
				i = rs.getInt(1);
			}
			rs.close();
			return new Integer(i);

		} catch (SQLException se) {
			debug("dbInterface:getRS - " + se.toString());
			errorMsg = se.toString();
			throw new ServicesException("DbInterface:getRS: " + se.toString());
		}

	}

	// ********************
	// utility functions
	// ********************
	public Integer stringToInteger(String s) {
		Integer p;
		try {
			p = new Integer(s);
		} catch (NumberFormatException e) {
			p = new Integer("1");
		}
		return p;
	} // build a select query as : 'SELECT field_1, field_2 FROM table_name

	// converts date from web input format MM/dd/yyyy to mySql format yyyy/MM/dd
	public String flipToYYYYMMDD(String parmDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

		// SimpleDateFormat indDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date date = new Date();
		try {
			date = dateFormat.parse(parmDate);
		} catch (java.text.ParseException pe) {
		}
		return dateFormat.format(date);
	}

	public String flipToYYYYMMDD_HHMM(String parmDate) {
		// SimpleDateFormat indDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		Date date = new Date();
		try {
			date = dateFormat.parse(parmDate);
		} catch (java.text.ParseException pe) {
		}
		return dateFormat.format(date);
	}

	public void debug(String debugMsg) {
		// Logger.getLogger("ui4sql").debug("DbInterface: " + debugMsg);


		System.out.println(debugMsg);
	}

}
