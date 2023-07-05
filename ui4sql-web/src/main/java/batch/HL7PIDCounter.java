/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package batch;

import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Date;

/**
 * @author PAULSEAR
 * 
 * Read hl7 file and dump out MRN #s from PID-3 segment.
 * 
 * 
 * 
 */

public class HL7PIDCounter {

	boolean debug = false;

	// constants

	public HL7PIDCounter() {

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

	public int processMessages(String inFileName, String outFileName) {

		int count = 0;

		try {
			BufferedReader in = new BufferedReader(new FileReader(inFileName));

			FileWriter fw = new FileWriter(outFileName);

			String record;
			String mrn;

			while ((record = in.readLine()) != null) {
				debug("record :  pid index " + record.indexOf("PID") + record);
				if (record.indexOf("PID") == 0) {
					count++;
					debug(" ... index of 1100 =" + record.indexOf("1100"));
					int mrnStart = record.indexOf("1100");

					mrn = record.substring(mrnStart, mrnStart + 12);
					fw.write(mrn + "\n");
				}

			}
			in.close();
			fw.close();
		} catch (IOException e) {
			debug("Excxeption at processMessages: " + e.toString());
		}
		return count;

	}

	private void debug(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) {

		System.out.println("HL7 MRN Extract: Version 1.0 Starting at: "
				+ new Date().toString());

		// Connect to the database
		HL7PIDCounter loader = new HL7PIDCounter();

		// load messages into database
		int count = loader.processMessages(args[0], args[1]);

		System.out.println("HL7 MRN count: : " + count);

		System.out.println("HL7 MRN: ending at: " + new Date().toString());

	}
}
