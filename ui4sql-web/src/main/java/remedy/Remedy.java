/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;

import java.net.*;
import java.io.*;

/*
 * Not used.. first attempt to connect to web services via sockets
 * 
 * 	but then realized that HTTPUrlConnection class is easier to use
 */

public class Remedy {

	String poststr = "HTTP/1.1";

	String hostName = "remedy.server";

	String remedyUser = "xxx";
	String remedyPw = "pw";
	int portNo = 80;

	String requesttop = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
			+ "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ; +"
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
			+ "xmlns:s=\"urn:ESS_People_Info\">";

	String requestheader = "<soap:Header><s:AuthenticationInfo>"
			+ "<s:userName>" + remedyUser + "</s:userName>"
			+ "<s:password>" + remedyPw + "</s:password><s:authentication/>"
			+ "<s:locale/><s:timeZone/></s:AuthenticationInfo></soap:Header>";

	String soapBody = "<soap:Body><s:SearchByChangeID>"
			+ "<s:changeID>CHG000001663054</s:changeID>"
			+ "</s:SearchByChangeID>" + "</soap:Body>";

	String requestbottom = "</soap:Envelope>";

	String hoststr = "Host: http://" + hostName + ":" + portNo + "\n";

	String contenttypestr = "Content-Type: text/xml; charset=utf-8";

	String soapactionstr = "urn:ESS_People_Info/SearchByChangeID";

	String connectionstr = "Connection: close";

	public boolean getRFC(int rfc_no) {

		return true;

	}

	public String getChangeInfo(String requesNo) {

		String xmldata = requesttop + requestheader + soapBody + requestbottom;

		StringBuffer sb = new StringBuffer();
		
		InetAddress addr = null;
		Socket sock = null;

		BufferedWriter wr ;

		BufferedReader rd  ;
		
		String line ;
		 
		addr = getInetAddress(hostName);

		sock = getSocket(addr, portNo);

		try {
			wr = new BufferedWriter(new OutputStreamWriter(sock
					.getOutputStream(), "UTF-8"));

			wr.write(poststr);
			wr.write(hoststr);
			wr.write("Content-Length: " + xmldata.length() + "\n");
			wr.write(contenttypestr);
			wr.write(soapactionstr);
			wr.write(connectionstr);
			wr.write("\n");
			wr.write(xmldata);
		    wr.flush();
		    wr.close();

		    
		} catch (IOException e) {
			System.out.println("Error writting to socket." + e.toString());
		}
		
		
		try {
			rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					
			while((line = rd.readLine()) != null) {
			     System.out.println(line);
			     sb.append(line);
			    }			
		}
		catch (IOException e){

			System.out.println("Error reading from socket. " + e.toString());
		}

		return sb.toString();
	}

	private Socket getSocket(InetAddress addrin, int portin) {
		try {
			Socket sockin = new Socket(addrin, portin);
			return sockin;
		} catch (Exception e07) {
			return null;
		}
	}

	private InetAddress getInetAddress(String hostnamein) {
		try {
			InetAddress addrin = InetAddress.getByName(hostnamein);
			return addrin;
		} catch (Exception e06) {
			return null;
		}

	}
	
	
	
}
