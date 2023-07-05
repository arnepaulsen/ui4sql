/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package batch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sf.mpxj.Duration;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.Priority;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectHeader;
import net.sf.mpxj.Relation;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.ResourceField;
import net.sf.mpxj.Table;
import net.sf.mpxj.Task;
import net.sf.mpxj.TaskField;
import net.sf.mpxj.TimeUnit;
import net.sf.mpxj.View;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.mpx.MPXReader;
import net.sf.mpxj.mpx.MPXWriter;
import net.sf.mpxj.mspdi.MSPDIReader;
import net.sf.mpxj.mspdi.MSPDIWriter;
import net.sf.mpxj.utility.NumberUtility;

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
 * Command line arguements: 
 * 	1. properties file of the db connection, containing : 
 *  	DB-URL = jdbc:microsoft:sqlserver://172.21.226.44:1433
 *  	DB-USERID = the_database_userid
 *  	DB-PASSWORD = the_userid_password
 *  
 *  
 *  2. Microsoft MPP file to load tasks
 *  
 */

public class ProjectLoad {

	boolean debug = false;

	// constants

	private BatchSQL conn;

	public ProjectLoad(String propFileName) {
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

		conn = new BatchSQL((String) props.getProperty("DB-URL"),
				(String) props.getProperty("DB-USERID"), (String) props
						.getProperty("DB-PASSWORD"));

	}

	private void endit() {
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
			System.out.print("BridgesQR: Reading properties...");

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

	
	public int loadFile (String fileName ) {
		return 0;
	}
	
	private String formatDate(String s) {

		return new String("'" + s.substring(0, 4) + "/" + s.substring(4, 6)
				+ "/" + s.substring(6, 8));

	}

	
	public static void main(String[] args) {

		System.out.println("MessageCountLoad: Version 1.0 Starting at: "
				+ new Date().toString());

		// Connect to the database
		ProjectLoad loader = new ProjectLoad(args[0]);

		// load messages into database
		int count = loader.loadFile(args[1]);

		// close connection
		loader.endit();

		System.out.println("MessageCountLoad: count: : " + count);

		System.out.println("MessageCountLoad: ending at: "
				+ new Date().toString());

	}
}
