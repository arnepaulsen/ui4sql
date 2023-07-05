/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package batch;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Properties;
import java.util.Date;
import java.lang.StringBuffer;

/**
 * @author PAULSEAR
 * 
 * Load the csv file of message counts into table tmessage_stat
 * 
 * . the csv file contains an hly7 message count per:
 * 		- interfface
 * 		- instance
 * 		- message type
 * 		- day
 * 
 * Command line arguements: 1. properties file of the db connection, containing :
 * DB-URL = jdbc:microsoft:sqlserver://172.21.226.44:1433 DB-USERID =
 * the_database_userid DB-PASSWORD = the_userid_password
 * 
 * 
 * 2. CSV file of the record counts, each record has: date interface no
 * instance, message name, count queue name (not used)
 * 
 */

public class MessageCountLoad {

	boolean debug = false;

	// constants

	private BatchSQL conn;

	public MessageCountLoad(String propFileName) {
		super();
		init(propFileName);

	}

	/***************************************************************************
	 * 
	 * Process Queue
	 * 
	 **************************************************************************/

	private void init(String prop_file_name) {

		Properties props = getProperties(prop_file_name);

		info ("...opening connection.");
		
		conn = new BatchSQL((String) props.getProperty("DB-URL"),
				(String) props.getProperty("DB-USERID"), (String) props
						.getProperty("DB-PASSWORD"));
	
		
	}

	private void endit() {

		String sql_set_interface = "update tmessage_stat "
				+ "set interface_id = (select interface_id from tinterface  where  tmessage_stat.interface_no = tinterface.queue_nm  )"
				+ "where isnull(interface_id,0) = 0";

		String sql_set_message = " update tmessage_stat set message_id = "
				+ " (select message_id from tmessage "
				+ " where  tmessage_stat.message_tx = tmessage.reference_nm  ) "
				+ " where  isnull(message_id,0) = 0 ";

		info("...wrap up... seting interface id on all new rows. ");
		conn.runQuery(sql_set_interface);

		info("...wrap up... setting message id on all new rows.");
		conn.runQuery(sql_set_message);

		info("...closing connection.");

		conn.closeConnection();
	}

	/*-----------------------------*
	 * load properties file
	 *-----------------------------*/

	private Properties getProperties(String propFileName) {

		Properties p = new Properties();

		System.out.println("Properties filename: " + propFileName);
		try {
			// FileInputStream is = new FileInputStream(propFileName);
			System.out.print("Reading properties...");

			FileInputStream is = new FileInputStream(propFileName);

			p.load(is);

		} catch (IOException e) {
			System.out.println("error loading properties file.");
			System.out.println(e.toString());
			System.exit(1);
		}

		return p;
	}

	/*
	 * =================================================================
	 * 
	 * MAIN PROGRAM LOOP HERE
	 * 
	 * ==================================================================
	 */

	/*
	 * Put message to database, and create two output for header info and hl7
	 * lines
	 */

	private int processMessages(String fileName) {

		debug("Processing messages ");
		
		int count = 0;

		String last_rec = "";

		try {
			debug("Getting reader.");
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str;
			while ((str = in.readLine()) != null) {
				debug("record + " + count);
				
				if (!str.equalsIgnoreCase(last_rec)) { // watch out for
														// duplicates!
					count++;
					processRecord(str);
					debug(str);
				}
				last_rec = str;
			}
			in.close();
		} catch (IOException e) {
			debug("Excxeption at processMessages: " + e.toString());
		}
		return count;

	}

	private void processRecord(String record) {

		StringBuffer qry = new StringBuffer();

		String[] tokens = record.split(",");

		qry
				.append("insert into tmessage_stat (interface_no, instance_cd, activity_date, message_tx, count_no ) ");

		qry.append(" values ('" + tokens[1] + "','" + tokens[2].substring(4, 6)
				+ "'," + formatDate(tokens[0]) + "','"
				+ tokens[3].substring(0, 7) + "', " + tokens[4] + ")");

		conn.runQuery(qry.toString());

		debug(qry.toString());

	}

	private String formatDate(String s) {

		return new String("'" + s.substring(0, 4) + "/" + s.substring(4, 6)
				+ "/" + s.substring(6, 8));

	}

	private void debug(String msg) {
		if (debug)
			System.out.println(msg);
	}

	private void info(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) {

		System.out.println("MessageCountLoad: Version 1.1 Starting at: "
				+ new Date().toString());

		// Connect to the database
		MessageCountLoad loader = new MessageCountLoad(args[0]);

		// load messages into database
		int count = loader.processMessages(args[1]);
		//int count = 0;
		
		// close connection
		loader.endit();

		System.out.println("MessageCountLoad: count: : " + count);

		System.out.println("MessageCountLoad: ending at: "
				+ new Date().toString());

	}
}
