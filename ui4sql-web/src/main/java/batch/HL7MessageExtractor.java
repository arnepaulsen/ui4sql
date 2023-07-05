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
 * Args: 1. input hl7 file 2. input mrn file 3. output hl7 file
 * 
 */

public class HL7MessageExtractor {

	boolean debug = false;

	// constants
	public HL7MessageExtractor() {
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

	public int processMessages(String inFileName, String mrFileName,
			String outFileName) {

		int out_count = 0;

		String[] mrns = new String[10000];
		int mrn_count = 0;

		String record;

		try {
			// there are four segments in each record
			String msh_seg;
			String evn_seg;
			String pid_seg;
			String rol_seg;

			// load the mrn list

			debug("loading mrn file:");
			BufferedReader mr_file = new BufferedReader(new FileReader(
					mrFileName));

			while ((record = mr_file.readLine()) != null) {
				mrns[mrn_count] = record;
				mrn_count++;
			}
			mr_file.close();
			debug(" .. mrn# = " + mrn_count);

			// cycle the input file, write out each 4-segment message series
			// that matches an mrn in the array

			BufferedReader in = new BufferedReader(new FileReader(inFileName));

			FileWriter fw = new FileWriter(outFileName);

			String mrn_no;
			
			boolean endOfFile = false;
			

			in.readLine(); // burn the bhs header

			int loop = 0;
			
			while ((msh_seg = in.readLine()) != null && !endOfFile) {
				if (msh_seg.indexOf("BTS") == 0) {
					endOfFile = true;
				} else {
					loop++;
					evn_seg = in.readLine();
					pid_seg = in.readLine();
					rol_seg = in.readLine();
					if (msh_seg.indexOf("MSH") != 0
							|| pid_seg.indexOf("PID") != 0
							|| evn_seg.indexOf("EVN") != 0
							|| rol_seg.indexOf("ROL") != 0) {

						debug("OUT OF ORDER  " + evn_seg + "\n" + pid_seg);
						endOfFile = true;
					}
					boolean match = false;
					debug(" pid seg : " + pid_seg);
					// get mrn for this 4-seg series
					int mrnStart = pid_seg.indexOf("1100");
					if (mrnStart > -1) {
						try {
							mrn_no = pid_seg.substring(mrnStart, mrnStart + 12);
							debug("matching mrn " + mrn_no);
						} catch (Exception e) {
							debug("oops no.  mrn");
							mrn_no = "";
						}
						match = false;
						// search array for a match
						for (int x = 0; match == true || x < (mrn_count - 1); x++) {
							try {
								if (mrns[x].equalsIgnoreCase(mrn_no)) {
									match = true;
									debug("hit on mrn " + mrn_no);
									fw.write(msh_seg + "\n");
									debug("..writing evn");
									fw.write(evn_seg + "\n");
									debug("..writing pid");
									fw.write(pid_seg + "\n");
									debug("..writing rol");
									fw.write(rol_seg + "\n");
									debug("..done writing");
									out_count++;
								}
							} catch (Exception e) {
								debug (" loop error at : " + x);
								endOfFile = true;
								debug( "pid seg : " + pid_seg);
								
							}

						}
					} else {
						debug("mrn start < -1");
					}
				}
			}
			in.close();
			fw.close();
		} catch (IOException e) {
			debug("Excxeption at processMessages: " + e.toString());
		} catch (Exception je) {
			debug("java exceptoin : " + je.toString());
		}
		return out_count;

	}

	private void debug(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) {

		System.out.println("HL7 MRN Extract: Version 1.0 Starting at: "
				+ new Date().toString());

		System.out.println(" file 1 " + args[0]);
		System.out.println(" file 2 " + args[1]);
		System.out.println(" file 3 " + args[2]);

		// Connect to the database
		HL7MessageExtractor loader = new HL7MessageExtractor();

		// load messages into database
		int count = loader.processMessages(args[0], args[1], args[2]);

		System.out.println("HL7 MRN count: : " + count);

		System.out.println("HL7 MRN: ending at: " + new Date().toString());

	}
}
