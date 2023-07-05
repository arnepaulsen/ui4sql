/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package batch;

/**
 *
 *Program Name
 * BridgesSQL.java
 *
 *Last date of modification
 *18 Sep 2003
 *
 *Description:
 * generic JDBC connection to SQL Server 
 * 
 * Methods for :
 * - open connection to SQL Server
 * - run query
 * - getTableKey : to figure out the identity key of the last row inserted
 * 
 * 
 * 2/27/08 - fix order of userid and password in constructor
 *
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import services.ServicesException;

// import

// import java.sql.SQLException;
// import java.sql.ResultSet;


/**
 * @author PAULSEAR
 * 
 * 
 * change log:
 * 
 * 12/7/09 - force connection to MySQL driver
 * 
 */

public class BatchSQL {

	/*
	 * resources
	 */

	private boolean debug = false;

	private Connection connection;

	/*
	 * Connection Parameters - must pass in on constructor
	 */

	private String url = "bad-host";

	private String userid = "bad-to";

	private String password = "bad-from";

	private SQLException sqlexception;

	/*
	 * CONSTRUCTORS -
	 */

	
	public BatchSQL (Connection pConnection) {
		connection = pConnection;
		
		try {
			connection.clearWarnings();
		} catch (SQLException e) {

		}
		
	}
	public BatchSQL(String parmUrl, String parmUserid, String parmPassword) {
		super();
		this.url = parmUrl;
		this.password = parmPassword;
		this.userid = parmUserid;

		connection = openConnection(url, userid, password);
		
		try {
			debug("is closed : " + connection.isClosed());
			connection.clearWarnings();
		} catch (SQLException e) {

		}

	}
	
	/* 
	 * Open with a properties file
	 */
	
	public BatchSQL (String propFileName) {
		super();
		
		Properties props = getProperties(propFileName);
		
		this.url = (String) props.getProperty("DB-URL");
		this.userid = (String) props.getProperty("DB-USERID");
		this.password = (String) props
		.getProperty("DB-PASSWORD");
		
		connection = openConnection(url, userid, password);

		try {
			connection.clearWarnings();
		} catch (SQLException e) {

		}
	}
	

	/*
	 * load the database driver
	 */
	static {
		try {

			//Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver"); // .newInstance();
			
			Class.forName("org.mariadb.jdbc.Driver"); // .newInstance();
			
			// Driver d = (Driver) new com.mysql.jdbc.Driver();
			// System.out.println("good forname");
		} catch (Exception e) {
			System.out
					.println("Connection - bad forName, error opening connection.");
			System.out.println(e.toString());
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {

		}
	}
	


	public Connection openConnection(String parmUrl, String parmId,
			String parmPW) {
		// Set URL for the data source
		try {
			//Driver sql = new com.microsoft.jdbc.sqlserver.SQLServerDriver();
			Driver sql = new org.mariadb.jdbc.Driver();
			DriverManager.registerDriver(sql);
			connection = DriverManager.getConnection(parmUrl, parmId, parmPW);

		} catch (SQLException e) {
			System.out.println("SQLServerConn:OpenConnnection : "
					+ e.toString());
			System.exit(12);

		}
		return connection;

	}

	/*-----------------------------*
	 * load properties file
	 *-----------------------------*/
	
	

	private Properties getProperties(String propFileName) {

		Properties p = new Properties();

		// System.out.println("Properties filename: " + propFileName);
		try {
			// FileInputStream is = new FileInputStream(propFileName);

			FileInputStream is = new FileInputStream(propFileName);

			p.load(is);

		} catch (IOException e) {
			System.out.println("error loading properties file.");
			System.out.println(e.toString());
			System.exit(1);
		}

		return p;
	}
	
	public SQLException getException() {
		return sqlexception;
	}

	public int getTableKey(String table, String keyName) {
		int key = 0;

		String keyQuery = ("SELECT TOP 1 " + keyName + " from " + table
				+ " ORDER BY " + keyName + " DESC");
		Statement stmt;
		ResultSet rs;

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(keyQuery);
			if (rs.next()) {
				key = rs.getInt(1);
			}

		} catch (SQLException se) {
			key = -1;
			sqlexception = se;
			System.out.println("DbInterface:getRS: " + se.toString());
		}

		return key;
	}

	// *********************************************
	// general run query
	// *********************************************
	public int runQuery(String parmQuery) {

		Statement stmt;

		try {
			stmt = connection.createStatement();
			// System.out.println(".. running query : " + parmQuery);
			stmt.executeUpdate(parmQuery);
			// System.out.println("DbInterface:runQuery is done ... query was :
			// "
			// + parmQuery);
		} catch (SQLException se) {

			int e = se.getErrorCode();
			sqlexception = se;

			if (e == 2627)
				return e;

			System.out.println("Insert error : " + se.toString());

			// return true;
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	// return a rs from a query string
	public ResultSet getRS(String parmQuery) throws ServicesException {

		debug("BatchSQL:getRS - " + parmQuery);

		Statement stmt;
		ResultSet rs = null;

		String query = new String(parmQuery);

		try {
			if (connection == null) {
				debug("BatchSQL:getRs - conn is null");
			}
		} catch (Exception e) {
			return rs;
		}

		// debug("DbInterface:getRS(string): " + query);

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);

		} catch (SQLException se) {
			debug("BatchSQL:getRS - " + se.toString());

			throw new ServicesException("BatchSQL:getRS: " + se.toString());
		}
		// debug("DbInterface:getRS - ending ok");

		return rs;

	}

	private void debug(String s) {

		if (debug)
			System.out.println(s);
	}

}
