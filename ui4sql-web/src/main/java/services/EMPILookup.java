/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

import java.util.Date;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

/*
 * EMPILookup
 * 
 * 	Web Service call to EMPI return Server 1...6 based on MRN  
 * 
 * 	Retrieves the N.Calif Epic instance for an mrn via call to EMPI web service
 * 
 * 	Calls EMPI using either getHome or getDestination
 * 
 * 		getDestination will return a default 'CA' if mrn not found or bad
 * 		getHome returns a null of the mrn is not on the empi database
 * 
 *  Uses https for psup and production servers
 * 
 */

// remedy user id  1B11Test 

public class EMPILookup {

	private static boolean debug = false;

	private boolean success = false;

	private String searchType = "getDestination"; // or getHome

	private String url = null;

	private String env = "";

	private String neighborhood;

	private String wits_server = "http://arnepaulsenjr.com/interconnect-witsem2/httplistener.ashx";

	private String renc_server = "http://arnepaulsenjr.com/httplistener.ashx";

	private String psup_server = "arnepaulsenjr.com/interconnect/httplistener.ashx";

	private String prod_server = "arnepaulsenjr.com/interconnect/httplistener.ashx";

	private static String soap_action = "urn:GetDestination";

	// XML Strings

	private static String getDest_xml = "<?xml version=\"1.0\"?>";

	private static String getDest_env_beg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";

	private static String getDest_body_beg = "<soapenv:Body>";

	private static String getDest_getDest_beg = "<NS1:GetDestination xmlns:NS1=\"urn:epicsystems-com:EMPI.2005-01.Services\">";

	private static String getDest_patient_id_beg = "<NS1:PatientID>";

	private static String getDest_patient_id_end = "</NS1:PatientID>";

	private static String getDest_id_type = "<NS1:IDType>NCALMRN</NS1:IDType>";

	private static String getDest_neighborhood_beg = "<NS1:Neighborhood>urn:kp:";

	// middle like: witsncal

	private static String getDest_neighborhood_end = "</NS1:Neighborhood>";

	private static String getDest_getDest_end = "</NS1:GetDestination>";

	private static String getDest_body_end = "</soapenv:Body>";

	private static String getDest_env_end = "</soapenv:Envelope>";

	// getHome XML

	private static String getHome_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private static String getHome_env_beg = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";

	private static String getHome_body_beg = "<soapenv:Body>";

	private static String getHome_beg = "<GetHome xmlns=\"urn:epicsystems-com:EMPI.2008-03.Services\">";

	private static String getHome_pat_id_beg = "<PatientID>";

	private static String getHome_pat_id_end = "</PatientID>";

	private static String getHome_id_type = "<IDType>NCALMRN</IDType>";

	private static String getHome_historical = "<SearchHistoricalIDs>true</SearchHistoricalIDs>";

	private static String getHome_getHome_end = "</GetHome>";

	private static String getHome_body_end = "</soapenv:Body>";

	private static String getHome_env_end = "</soapenv:Envelope>";

	// HTTP REQUEST :

	public EMPILookup(String env, String searchType) {

		this.env = env.toLowerCase();
		this.searchType = searchType;

		neighborhood = env.toLowerCase() + "ncal"; // neighborhood should be
		// like 'WITSNCAL'

		if (env.equalsIgnoreCase("PROD")) {
			this.url = prod_server;
			//this.httpConnectionType = "HTTP";
			return;
		}

		if (env.equalsIgnoreCase("PSUP")) {
			this.url = psup_server;
			// this.httpConnectionType = "HTTPS";
			return;
		}

		if (env.equalsIgnoreCase("RENC")) {
			this.url = renc_server;
			return;
		}

		if (env.equalsIgnoreCase("WITS")) {
			this.url = wits_server;
			return;
		}

		// default to wits
		debug("Constructor: Caution - defaulting to WITS.");
		this.url = wits_server;

	}

	public int doListFile(String inFileName, String outFileName) {

		int counter = 0;

		boolean reprocessing = false;

		boolean first_record = true;

		try {
			BufferedReader inputFile = new BufferedReader(new FileReader(
					inFileName));

			FileWriter writer = new FileWriter(outFileName);

			String record = null;
			String mrn_no = null;
			String instance = null;

			while ((record = inputFile.readLine()) != null) {

				// check the first record to see if it already has ":", meaning
				// it was already done once
				if (first_record) {
					first_record = false;
					reprocessing = record.indexOf(":") > 0;
					debug("First record... reprocessing = " + reprocessing);
				}

				counter++;
				mrn_no = record.substring(0, 12);
				System.out.print(".");

				if (reprocessing) {
					debug("reprocessing record ");
					if (record.toLowerCase().indexOf(":u") > 0) {
						// redo the unknown server record
						instance = getInstance(mrn_no);
						String newRecord = record.replaceFirst(":U", ":"
								+ instance);
						writer.write(newRecord + "\n");
						debug(newRecord);
					} else {
						writer.write(record + "\n");
					}

				} else {
					// first time... to just append ":n" to the end of record
					instance = getInstance(mrn_no);
					writer.write(record + ":" + instance + "\n");
					debug(record + ":" + instance);
				}
			}
	
			writer.close();
			inputFile.close();

		} catch (IOException ioe) {
			System.out.println("GetDestination : IOException : "
					+ ioe.toString());
		}

		return counter;
	}

	public String getInstance(String mrn) {

		String response = "";

		String soap_message = "";

		if (searchType.toLowerCase().indexOf("dest") > -1) {
			// debug("building getDest message : searchType : " + searchType);
			soap_message = buildGetDestination(mrn);
		} else {
			// debug("building getHOme message : searchType : " + searchType);
			soap_message = buildGetHome(mrn);
		}

		// debug("SOAP Message : " + soap_message);

		try {

			HttpSoapConnection soap = new HttpSoapConnection(url, soap_action);
			response = soap.soapSendReceive(soap_message);
			// debug("EMPI response :" + response);
			success = soap.getSuccess();

		} catch (Exception e) {
			System.out.println("Soap Exception : " + e.toString());

		}

		if (!success || response == null) {
			System.out.println("getDestination returned no answer");
			return "No response from getDestination.";
		}

		String answer = "";
		if (searchType.toLowerCase().indexOf("dest") > -1) {
			answer = parseXML(response, "GetDestinationResponse");
		} else {
			answer = parseXML(response, "GetHomeResponse");
		}

		return parseServer(answer);

	}

	/*
	 * XML Parser to find the value withing 'GetDestinationResult' tag
	 * 
	 * the return value will usually be like : urn:kp:witsca2 .. so the real
	 * answer is the 'ca' portion
	 */

	private String parseXML(String xml, String tag) {

		String theServer;

		XMLParser parser = new XMLParser(xml, tag);

		int count = parser.getCount();

		if (count > 0) {
			if (searchType.toLowerCase().indexOf("dest") > -1) {
				theServer = parser.parseTextValue("GetDestinationResult");
			} else {
				theServer = parser.parseTextValue("GetHomeResult");
			}

		} else {
			theServer = "U";
		}

		return theServer;
	}

	/*
	 * parseServer
	 * 
	 * locate the actual server id (ca, eb, gg, etc.) from value like
	 * 'urn:kp:witsca2'
	 * 
	 * so find the class variable env (like wits), then find next two characters
	 * 
	 */
	private String parseServer(String s) {

		String server_id = ""; // will be values 1-6, or 'u' for unknown

		try {
			server_id = s.substring((s.indexOf(env) + 4), (s.indexOf(env) + 6));

		} catch (Exception e) {
			// debug("parseServer: Error parsing instance from : " + s);
			return "U";
		}

		if (server_id.equals("ca")) {
			return "1";
		}

		if (server_id.equals("sb")) {
			return "2";
		}

		if (server_id.equals("gg")) {
			return "3";
		}

		if (server_id.equals("nb")) {
			return "4";
		}

		if (server_id.equals("eb")) {
			return "5";
		}

		if (server_id.equals("cc")) {
			return "6";
		}

		return "U";

	}

	private String buildGetDestination(String mrn) {

		StringBuffer sb = new StringBuffer();

		sb.append(getDest_xml);

		sb.append(getDest_env_beg);

		sb.append(getDest_body_beg);

		sb.append(getDest_getDest_beg);

		sb.append(getDest_patient_id_beg + mrn + getDest_patient_id_end);

		sb.append(getDest_id_type);

		sb.append(getDest_neighborhood_beg + neighborhood
				+ getDest_neighborhood_end);

		sb.append(getDest_getDest_end);

		sb.append(getDest_body_end);

		sb.append(getDest_env_end);

		return sb.toString();
	}

	private String buildGetHome(String mrn) {

		StringBuffer sb = new StringBuffer();

		sb.append(getHome_xml);

		sb.append(getHome_env_beg);

		sb.append(getHome_body_beg);

		sb.append(getHome_beg);

		sb.append(getHome_pat_id_beg + mrn + getHome_pat_id_end);

		sb.append(getHome_id_type);

		sb.append(getHome_historical);

		sb.append(getHome_getHome_end);

		sb.append(getHome_body_end);

		sb.append(getHome_env_end);

		return sb.toString();
	}

	static public void debug(String message) {
		if (debug) {
			System.out.println("HttpSoapConnection: " + message);
		}

	}

	public static void main(String[] args) {

		System.out.println("EMPI Lookup : Version 3.0 Starting at : "
				+ new Date().toString());

		/*
		 * 
		 * args:
		 * 
		 * 1. PSUP|PROD|WITS
		 * 
		 * 2. getHome or getDestination
		 * 
		 * 3. either a. input file name or b. mrn starting with 1100
		 * 
		 * 4. if 3= input file name, then 4 = output file name, else nothing
		 * (mrn will write to system.out
		 * 
		 */
		if (args.length < 3) {
			System.out
					.println("EMPI Lookup \nUsage : EMPILookup env(psup|wits|prod) mrn infile outfile");
			System.exit(99);
		}

		String env = args[0];

		String searchType = args[1];

		EMPILookup empi = new EMPILookup(env, searchType);

		/*
		 * 3 args = mrn search
		 */
		if (args.length == 3) {

			String mrn = args[2];

			// fix mrn
			if (mrn.length() == 8) {
				mrn = "1100" + mrn;
			}
			if (mrn.length() == 7) {
				mrn = "11000" + mrn;
			}

			if (mrn.length() < 12) {
				System.out.println(".. Error : unknown MRN format '" + args[2]
						+ "'  .exiting.");

			} else {
				System.out.println("Callng EMPI " + searchType
						+ " sevice for MRN " + mrn);
				System.out.println(".. MRN is at : " + empi.getInstance(mrn));
			}
		} else {

			int fileCount = 0;

			System.out.println("  input file : " + args[2]);

			System.out.println("  outputfile : " + args[3]);

			System.out.print("  starting ");
			fileCount = empi.doListFile(args[2], args[3]);

			System.out.println("\n  total lookups : " + fileCount);
		}

		System.out.println("EMPILookup : ending at : " + new Date().toString());

	}
}
