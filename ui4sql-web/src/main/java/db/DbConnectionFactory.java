/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package db;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import services.ServicesException;

/*
 * 	Return the SQL connection object depending on the vendor product.
  
 *
 * Change Log: 
 * 7/21 Add new methods to return connection from properties, or no parm at all
 * 8/1/06 Add SQLServer (Microsoft)
 * 9/15/06 Add logic to return db2 conn 
 */

public class DbConnectionFactory {

	private boolean debug = false;

	public Connection getConnection() throws services.ServicesException {
		// open a connection

		try {

			return getConnection(getProperties());

		} catch (Exception se) {
			System.out.println("Utility:getConnection: " + se.toString());
			throw new ServicesException(se.toString());
		}
	}

	public Connection getConnection(Properties prop)
			throws services.ServicesException {
		// open a connection

		debug("getConnection(prop) " + prop.getProperty("database_product"));
		try {

			Connection connection = getConnection(prop
					.getProperty("database_product"), prop
					.getProperty("database_url"), prop
					.getProperty("database_login"), prop
					.getProperty("database_pw"));

			return connection;

		} catch (Exception se) {
			System.out.println("Utility:getConnection: " + se.toString());
			throw new ServicesException(se.toString());
		}
	}

	public Connection getConnection(String product, String url, String login,
			String pw) throws services.ServicesException {

		debug(" dbfactory : product :  " + product);

		if (product.equalsIgnoreCase("ODBC")) {
			return getODBCConnection(url, login, pw);
		} else {
			if (product.equalsIgnoreCase("SQLServer")) {
				return getSQLServerConnection(url, login, pw);
			} else {
				if (product.equalsIgnoreCase("DB2")) {

					return getDB2Connection(url, login, pw);
				}
				else {

					return getMySQLConnection(url, login, pw);
				}
			}
		}
	}

	/*
	 * MySQL
	 * 
	 */
	private Connection getMySQLConnection(String url, String login, String pw)
			throws services.ServicesException {

		MySqlConn mysql = new MySqlConn();
		mysql.openConnection(url, login, pw);

		return mysql.getConnection();

	}

	/*
	 * ODBC
	 */
	private Connection getODBCConnection(String url, String login, String pw)
			throws services.ServicesException {

		ODBCConn odbc = new ODBCConn();
		odbc.openConnection(url, login, pw);

		return odbc.getConnection();
	}


	/*
	 * 
	 * SQL Server
	 */
	private Connection getSQLServerConnection(String url, String login,
			String pw) throws services.ServicesException {

		SQLServerConn sql  = new SQLServerConn();
		sql.openConnection(url, login, pw);

		return sql.getConnection();

	}

	/*
	 * 
	 * DB/2 ... in your dreams.
	 */
	private Connection getDB2Connection(String url, String login, String pw)
			throws services.ServicesException {

		DB2Conn db2 = new DB2Conn();
		db2.openConnection(url, login, pw);

		return db2.getConnection();

	}

	private Properties getProperties() {

		Properties prop = new Properties();

		try {
			InputStream is = getClass().getResourceAsStream(
					"/cmm/properties/cmm.properties");
			prop.load(is);

		} catch (Exception e) {
			debug("getProperties : exception loading properties");
			debug(e.toString());

		}

		return prop;
	}

	public void debug(String parmMsg) {
		if (debug)
			System.out.println("ConnectionFactory: " + parmMsg);
	}

}
