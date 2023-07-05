/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.io.DataOutputStream;

/*
 * Send a file to a socket
 * 
 */

public class SocketClient {

	private boolean success = false;

	private boolean debug = true;

	private Socket connection = null;

	private String server = null;
	private int port = 0;
	private String file = "";

	String record;

	DataOutputStream output;

	BufferedReader response = null;

	BufferedReader inFile = null;

	public SocketClient(String server, String port, String file) {

		this.server = server;
		this.port = Integer.parseInt(port);
		this.file = file;

	}

	public void sendFile() {
		try {
			connection = new Socket(server, port);

		} catch (IOException e) {

		}

		try {
			output = new DataOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			System.out.println(e);
			System.exit(99);
		}

		/*
		 * response data from connection
		 */
		try {
			response = new BufferedReader(new InputStreamReader(connection
					.getInputStream()));
		} catch (IOException e) {
			debug("bad open socket inputStream");
			System.out.println(e);
			System.exit(99);
		}

		/*
		 * open file to send
		 */
		try {
			inFile = new BufferedReader(new FileReader(file));

		} catch (IOException e) {
			debug("Bad open on file: " + file);

		}

		/*
		 * push out the file
		 */
		try {
			while ((record = inFile.readLine()) != null) {
				output.writeBytes(record);
				debug("record: " + record);
			}
		} catch (IOException e) {

		}

		String responseLine;

		try {
			while ((responseLine = response.readLine()) != null) {
				debug("response: "  + responseLine);
			}

			inFile.close();
			response.close();
			output.close();
		} catch (IOException e) {

		}
		
		debug("Done");
		

	}


	private void debug(String message) {
		if (debug == true) {
			System.out.println("HttpSoapConnection: " + message);
		}

	}
	
	public static void main(String[] args) {

		System.out.println("SocketClient : Version 1.0 Starting at: "
				+ new Date().toString());

		// Connect to the database
		SocketClient s = new SocketClient (args[0], args[1], args[2]);
		
		s.sendFile();
		
		System.out.println("SocketClient MRN: ending at: " + new Date().toString());

	}
	



}
