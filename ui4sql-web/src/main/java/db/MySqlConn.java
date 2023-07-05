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
 * 
 * 2/25/22 - com.mysql.jdbc.Driver. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'.
 */
public class MySqlConn {

	public Connection connection = null;

	private String exceptionMsg = null;

	public MySqlConn() throws ServicesException {

	}

	static {
		try {
			//System.out.println("setting forName");

			// 6/28/07 CHANGE FOR NEW HOST..
			
			Class.forName("org.mariadb.jdbc.Driver"); // .newInstance();
			
			// Class.forName("com.mysql.cj.jdbc.Driver"); // .newInstance();
			//Class.forName("org.gjt.mm.mysql.Driver").newInstance();
			 

		} catch (Exception e) {
			System.out.println("MySqlConn - bad forName, error opening connection.");
			System.out.println(e.toString());
		}
	}

	public void openConnection(String parmUrl, String parmId, String parmPW)
			throws ServicesException {
		// Set URL for the data source
		try {
			Driver mysql = new org.mariadb.jdbc.Driver();
			DriverManager.registerDriver(mysql);
			connection = DriverManager.getConnection(parmUrl, parmId, parmPW);
		} catch (SQLException e) {
			System.out.println("MySqlConn:OpenConnnection : " + e.toString());
			exceptionMsg = e.toString();
			throw new ServicesException(e.toString());
		}
	}

	public Connection getConnection() {
		return connection;
	}

	

}
