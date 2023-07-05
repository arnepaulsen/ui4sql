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

public class X12Reporter {

	String fileName;

	StringBuffer report;

	boolean firstMessage = true;

	int nm1_ct = 0; // turn on to fetch NM1 that follows REF*JD

	StringBuffer thisOne = new StringBuffer();

	public X12Reporter(String[] args) {

		fileName = args[0];

		report = new StringBuffer();

	}

	public void run() {


		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str;
			while ((str = in.readLine()) != null) {
				// debug("record + " + count);
				processRecord(str);
				// debug(str);
			}
			in.close();

			debug(report.toString());

		} catch (IOException e) {
			debug("Excxeption at processMessages: " + e.toString());
		}

	}

	private void processRecord(String s) {

		if (s.startsWith("BHT")) {

			int i = s.indexOf("200");
			thisOne.append(s.substring(i + 4, i + 6) + "/");

			thisOne.append(s.substring(i + 6, i + 8) + "/2009,");
			nm1_ct = 0;

		}
		if (s.startsWith("REF*JD")) {
			if (s.indexOf("KPNCALREALTIME") > 2)

				thisOne.append(s.substring(22, 29) + ","); // nuid

			if (s.length() > 29) {

				thisOne.append(s.substring(30).replace("~", "")
						.replace(",", "").replace("-", " ")
						+ ",");
			}
		}

		if (s.startsWith("NM1")) {
			nm1_ct++;

			if (nm1_ct == 3) {
				nm1_ct = 0;

				thisOne.append(s.substring(9).replace("~", ""));

				thisOne.append("\n");
				writeIt();

			}

		}

	}

	private void writeIt() {

		String thisMessage = thisOne.toString();
		thisOne = new StringBuffer();

		if (thisMessage.startsWith("ON: 03")) {
			return;
		}

		report.append(thisMessage);

	}

	private void debug(String s) {
		System.out.println(s);
	}

	public static void main(String[] args) {

		X12Reporter r = new X12Reporter(args);

		r.run();

	}

}
