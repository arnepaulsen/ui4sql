/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen
 * 
 * 3/26 allow the display yes/no values to be passed in as a string array
 * 
 */
public class WebFieldCheckbox extends WebField {

	private String displayText;

	private boolean onOff = false;
	
	private String[] displayYesNo  = new String[] {"Yes", "No"};

	// to init the super 'value' field via a boolean parm

	// can use to init value via Integer rather than boolean
	public WebFieldCheckbox(String parmFieldId, String valueOnOff,
			String parmDisplayText, String [] parmYesNo) {

		super(parmFieldId, valueOnOff);
		
			
		displayText = parmDisplayText;

		displayYesNo[0] = parmYesNo[0];
	
		displayYesNo[1] = parmYesNo[1];
		
		if (valueOnOff.equalsIgnoreCase("Y")) {
			onOff = true;
		} else {

			onOff = false;
		}
				
	}

		

	// to init the super 'value' field via a boolean parm
	public WebFieldCheckbox(String parmFieldId, boolean valueOnOff,
			String parmDisplayText) {

		// set the super 'value' field to Integer("1') if true, Integer("0)' if
		// false
		super(parmFieldId, valueOnOff ? "Y" : "N");

		displayText = parmDisplayText;
		
		displayYesNo[0] = "Yes";
		displayYesNo[1] = "No";
		
		onOff = valueOnOff;
	}

	// can use to init value via Integer rather than boolean
	public WebFieldCheckbox(String parmFieldId, String valueOnOff,
			String parmDisplayText) {

		super(parmFieldId, valueOnOff);
		displayText = parmDisplayText;

		displayYesNo[0] = "Yes";
		displayYesNo[1] = "No";
		
		
		if (valueOnOff.equalsIgnoreCase("Y")) {
			onOff = true;
		} else {

			onOff = false;
		}
	}

	public WebFieldCheckbox(String parmFieldId) {

		super(parmFieldId, new Integer("0"));

	}

	/*
	 * 
	 * getHTML() to support Netscape/Mozilla
	 * 
	 */

	// return HTML checkbox string, the boolean onOff is already set by the
	// constructor
	public String getHTML(String parmMode) {

		
		String checked = new String(onOff ? " checked " : " ");
		
		String not = new String(onOff ? displayYesNo[0] : displayYesNo[1]);

		if (parmMode.equalsIgnoreCase("show")) {
			return new String(not);
		}

		if (parmMode.equalsIgnoreCase("edit")) {

			return new String("<input type=checkbox name=" + webFieldId
					+ checked + " value=Y" + ">" + displayText);

		}

		if (parmMode.equalsIgnoreCase("add")) {
			return new String("<input type=checkbox name=" + webFieldId
					+ " value=Y" + ">" + displayText);

		}

		return new String("");

	}

	/*
	 * 
	 * Legacy.... convert to getHTML
	 * 
	 */

	// return HTML checkbox string, the boolean onOff is already set by the
	// constructor
	public String getJS(String parmMode) {

		String checked = new String(onOff ? " checked " : " ");
		String not = new String(onOff ? "Yes" : " No");

		if (parmMode.equalsIgnoreCase("show")) {
			return new String("\ndocument.all['" + webFieldId
					+ "'].outerHTML = '<p>" + not + "</p>';");
		}

		if (parmMode.equalsIgnoreCase("edit")) {

			return new String("\ndocument.all['" + webFieldId
					+ "'].outerHTML = '<input type=checkbox name=" + webFieldId
					+ checked + " value=Y" + ">" + displayText + "';");

		}

		if (parmMode.equalsIgnoreCase("add")) {
			return new String("\ndocument.all['" + webFieldId
					+ "'].outerHTML = '<input type=checkbox name=" + webFieldId
					+ " value=Y" + ">" + displayText + "';");

		}

		return new String("");

	}

}
