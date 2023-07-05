/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen 3/7/05 remove
 *         <p>
 *         from display 6/13 display the value, even on add... constructors give
 *         a default value *
 * 
 * 8/16/08 edit box is dropping embedded quotes.. try single quotes around
 * string.
 * 
 * 12/1/08 HUGE.... put escape \ in front of single quotes
 */
public class WebFieldString extends WebField {

	public int displayWidth;

	public int maxFieldSize;

	/***************************************************************************
	 * 
	 * Constructors (2)
	 * 
	 */

	public WebFieldString(String parmFieldId, String parmWebText,
			int parmDisplayWidth, int parmMaxLength) {

		super(parmFieldId, parmWebText);

		displayWidth = parmDisplayWidth;
		maxFieldSize = parmMaxLength;

	}

	public WebFieldString(String parmFieldId) {

		super(parmFieldId);

	}

	/***************************************************************************
	 * Functions to return either 1. just the display value if in show mode 2.
	 * an html <input> element if edit or add mode
	 * 
	 */

	public String getHTML(String parmMode) {
		if (parmMode.equalsIgnoreCase("show")) {
			return (String) value;
		}

		/*
		 * 6/13 Even on an add, we should have a default value to display
		 */
		if ((parmMode.equalsIgnoreCase("edit"))
				|| (parmMode.equalsIgnoreCase("add"))) {
			return new String("<input  name=" + webFieldId + " id="
				+ webFieldId + " size=" + displayWidth + " maxlength="
				+ maxFieldSize + " type=text value='"
				+ (String) htmlHelper.getHTML((String) value) + "'>");

		}

		return new String("");

	}
	
	
	

}
