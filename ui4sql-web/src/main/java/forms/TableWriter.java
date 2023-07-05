/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import java.io.PrintWriter;

/**
 * @author PAULSEAR
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TableWriter {

	PrintWriter out;

	public TableWriter(PrintWriter out) {
		this.out = out;
		out.println("<table align=left>");
	}


	public void writeRow(String td1) {
		out.println("<tr><td COLSPAN=2>" + td1 + "</td></tr>");
	}

	public void writeRow(String td1, String td2) {
		out.println("<tr><td width=30%>" + td1 + "</td><td width=70%>" + td2 + "</td></tr>");
	}

	public void writeRow(String td1, String td2, String td3) {
		out.println(
			"<tr><td>"
				+ td1
				+ "</td><td>"
				+ td2
				+ "</td><td>"
				+ td3
				+ "</td></tr>");
	}

	public void writeRow(String td1, String td2, String td3, String td4) {
		out.println(
			"<tr><td>"
				+ td1
				+ "</td><td>"
				+ td2
				+ "</td><td>"
				+ td3
				+ "</td></tr>");
	}

	public void tableEnd() {
		out.println("</table>");

	}
}
