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
public class DB2Conn {

	/*
	 * Note : DB/2 connection url looks like : "jdbc:DB2:SSP";
	 * 
	 */

	public Connection connection = null;

	
	static {
		try {
			Class.forName("COM.ibm.db2.jdbc.app.DB2Driver"); // .newInstance();
		} catch (Exception e) {
			System.out.println(".........\n error open connection");
			System.out.println(e.toString());
		}
	}

	public void openConnection(String parmUrl, String parmId, String parmPW)
			throws ServicesException {
		// Set URL for the data source
		try {
			connection = DriverManager.getConnection(parmUrl, parmId, parmPW);
		} catch (SQLException e) {
			System.out.println("DB2:OpenConnnection : " + e.toString());
			throw new ServicesException(e.toString());
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

}
