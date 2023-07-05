/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



/*
 * 
 * 
 * 	All http output is redirected through here
 * 
 * 	... so I can see every line
 * 
 * 
 * 
 */

public class WebLineWriter   {

	PrintWriter out;
	HttpServletRequest req;

	HttpServletResponse resp;
	
	private StringBuffer sb = null;
	
	
	public WebLineWriter (PrintWriter out) {
		sb = new StringBuffer();
		this.out = out;
	}
		
	public void println(String s) {
		sb.append(s + "\n");
		//out.println(s);
	}
	
	public void print(String s) {
		//out.print(s);
		sb.append(s);
	}
	
	public void unLoad () {
		//System.out.println("WeblineWriter:unloading : " + sb.toString());
		
		out.println(sb.toString());
	}
	
	
	
}
