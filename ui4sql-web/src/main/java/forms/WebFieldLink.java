/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import java.net.URLEncoder;
import java.text.*;
import java.util.*;

/**
 * @author Arne Paulsen 3/7/05 remove
 *         <p>
 *         from display 6/13 display the value, even on add... constructors give
 *         a default value *
 */
public class WebFieldLink extends WebField {

	public int displayWidth;

	public int maxFieldSize;

	/***************************************************************************
	 * 
	 * Constructors (2)
	 * 
	 */

	public WebFieldLink(String parmFieldId, String parmWebText,
			int parmDisplayWidth, int parmMaxLength) {

		super(parmFieldId, parmWebText);

		displayWidth = parmDisplayWidth;
		maxFieldSize = parmMaxLength;

	}

	public WebFieldLink(String parmFieldId) {

		super(parmFieldId);

	}

	private static final char c[] = { ' ','<', '>', '&', '\"' };
	private static final String expansion[] = {"%20%", "&lt;", "&gt;", "&amp;",
			"&quot;" };

	public static String HTMLEncode(String s) {
		StringBuffer st = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			boolean copy = true;
			char ch = s.charAt(i);
			for (int j = 0; j < c.length; j++) {
				if (c[j] == ch) {
					st.append(expansion[j]);
					copy = false;
					break;
				}
			}
			if (copy)
				st.append(ch);
		}
		return st.toString();
	}

	/***************************************************************************
	 * Functions to return either 1. just the display value if in show mode 2.
	 * an html <input> element if edit or add mode
	 * 
	 */

	public String getHTML(String parmMode) {
		if (parmMode.equalsIgnoreCase("show")) {

			String encoded = "";
			String unencoded = (String) value;

			try {
				//encoded = HTMLEncode(unencoded);
				
				encoded = java.net.URLEncoder.encode(unencoded, "UTF-8");
				debug("bad encode");
				
			} catch (Exception e) {

			}

			return new String("<a href='"
					+ unencoded + "' target='_blank'>"
					+ (String) htmlHelper.getHTML((String) value) + "</a>");

		}

		/*
		 * 6/13 Even on an add, we should have a default value to display
		 */
		if ((parmMode.equalsIgnoreCase("edit"))
				|| (parmMode.equalsIgnoreCase("add"))) {
			return new String("<input name=" + webFieldId + " id=" + webFieldId
					+ " size=" + displayWidth + " maxlength=" + maxFieldSize
					+ " type=text value=\""
					+ (String) htmlHelper.getHTML((String) value) + "\">");

		}

		return new String("");

	}
}
