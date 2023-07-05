/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
/*
 * Created on Feb 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package forms;

/**
 * @author PAULSEAR
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 * 
 * Change log:
 * 
 * 9/27/06 - increase the 'run-away loop stop' from 10 to 50 add 'getTextBox'
 * method to reverse <br>
 * back to crlf
 * 
 * KEYWORDS: java 1.5 replaceAll
 * 
 */
public class HtmlHelper {

	public static String newline = System.getProperty("line.separator");

	byte cr = 13;

	byte lf = 10;

	byte[] crlf = { cr, lf };

	byte[] lfcr = { lf, cr };

	byte[] crlflf = { cr, lf, lf };

	String sBR = new String("<BR>");

	String sLF = new String(lfcr, 0, 1);

	String quote = new String("&#39;"); // /"&rsquo;"

	// private String crlf = new String(x'0a0d');

	// get HTML friendly string by replacing quotes with &quote;
	public String getHTML(String p) {

		return p.replaceAll("'", quote).replaceAll(sLF, "<br>");

	}

	public String replace_LF_BR(String s) {
		return s.replaceAll(sLF, sBR);
	}

}
