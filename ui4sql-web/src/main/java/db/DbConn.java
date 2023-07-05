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
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 * 
 * Change log:
 * 
 * 9/15/06 remove unused db2 constants
 */
public class DbConn {

	// db/2 connection variables
	private String db2_id = "paulsear";
	private String db2_pw = "kp2arne";
	String url = "jdbc:DB2:SSP";

	public Connection sqlConn = null;
	private String exceptionMsg = null;
	private int errorCode = 0;

	private final static int DB_CONNECT_ERROR = 900;

	static {
		try {
			Class.forName("COM.ibm.db2.jdbc.app.DB2Driver"); // .newInstance();
		} catch (Exception e) {
			System.out.println(".........\n error open connection");
			System.out.println(e.toString());
		}
	}

	public DbConn() throws ServicesException {

		// Set URL for the data source
		try {
			sqlConn = DriverManager.getConnection(url, db2_id, db2_pw);
		} catch (SQLException e) {
			exceptionMsg = e.toString();
			errorCode = DB_CONNECT_ERROR;
			throw new ServicesException(e.toString());
		}
	}

	public void Open() throws ServicesException {
	}


	public void Close() throws ServicesException {
	}


	public Connection getConnection() {
		return sqlConn;
	}
	public String getExceptionMsg() {
		return exceptionMsg;
	}
	public int getErrorCd() {
		return errorCode;
	}

}
