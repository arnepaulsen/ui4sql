/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package db;

import java.sql.*;
import java.sql.Connection;
import services.ServicesException;


/**
 * @author PAULSEAR
 * 
 * 	jdbc-odbc bridge connection
 */
public class ODBCConn {

	public Connection connection = null;

	private String exceptionMsg = null;

	public ODBCConn() throws ServicesException {

	}

	static {
		try {
			//System.out.println("setting forName");
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); // .newInstance();
			// Driver d = (Driver) new com.mysql.jdbc.Driver();
			//System.out.println("good forname");
		} catch (Exception e) {
			System.out.println("MySqlConn - bad forName, error opening connection.");
			System.out.println(e.toString());
		}
	}

	public void openConnection(String parmUrl, String parmId, String parmPW)
			throws ServicesException {
		// Set URL for the data source
		try {
			connection = DriverManager.getConnection("jdbc:odbc:CMM", parmId, parmPW);
			
			
		} catch (SQLException e) {
			System.out.println("ODBCSqlConn:OpenConnnection : " + e.toString());
			exceptionMsg = e.toString();
			throw new ServicesException(e.toString());
		}
	}

	public Connection getConnection() {
		return connection;
	}

	

}
