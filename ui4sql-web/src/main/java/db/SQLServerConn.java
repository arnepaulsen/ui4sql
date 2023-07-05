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
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class SQLServerConn {

	public Connection connection = null;

	public SQLServerConn() throws ServicesException {

	}

	static {
		try {
			//System.out.println("setting forName");
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver"); // .newInstance();
			// Driver d = (Driver) new com.mysql.jdbc.Driver();
			//System.out.println("good forname");
		} catch (Exception e) {
			System.out.println("SqlServerConn - bad forName, error opening connection.");
			System.out.println(e.toString());
		}
	}

	public void openConnection(String parmUrl, String parmId, String parmPW)
			throws ServicesException {
		// Set URL for the data source
		
		// useJvmCharsetConverters=true
		
		try {
			Driver sql = new com.microsoft.jdbc.sqlserver.SQLServerDriver();
			DriverManager.registerDriver(sql);
			connection = DriverManager.getConnection(parmUrl + ";" + "useJvmCharsetConverters=true", parmId, parmPW);
		} catch (SQLException e) {
			System.out.println("SQLServerConn:OpenConnnection : " + e.toString());
			throw new ServicesException(e.toString());
		}
	}

	public Connection getConnection() {
		return connection;
	}

	

}
