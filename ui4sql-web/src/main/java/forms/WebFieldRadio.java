/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen
 * 
 * 1/22/06 new radio element
 * 
 */

public class WebFieldRadio extends WebField {

	String[][] choices;

	String label;
	
	boolean bForceDisplay = false;

	/*
	 * 8/22 allow alternate display class
	 */

	// *********************************
	// CONSTRUCTORS
	// *********************************
	
	
	public WebFieldRadio(String parmFieldId, String parmValue,
			String[][] parmChoices) {

		super(parmFieldId, parmValue);
		choices = parmChoices;
		label = "";
	}

	public WebFieldRadio(String parmFieldId, String parmValue,
			String[][] parmChoices, String pLabel) {

		super(parmFieldId, parmValue);
		choices = parmChoices;
		label = pLabel;
	}
	
	public void forceDisplay() {
		bForceDisplay = true;
	}

	/*
	 * 
	 * return a dislay value for the selected option
	 * 
	 */

	public String getHTML(String parmMode) {

		if (parmMode.equalsIgnoreCase("show") || bForceDisplay) {
			return getDisplayValue();
		} else {
			return getRadios();
		}
	}

	// * find the value/desc pair in the array, and return, else return nothing.
	private String getDisplayValue() {
		for (int i = 0; i < choices[0].length; i++) {
			if (choices[0][i].equalsIgnoreCase((String) value)) {
				return label + choices[1][i];
			}
		}
		return label;
	}

	/*
	 * 
	 * return <input type=radio...> for each choice in array
	 * 
	 * 
	 */
	public String getRadios() {

		StringBuffer sb = new StringBuffer();

		sb.append(label);

		String selected = "";

		for (int i = 0; i < choices[0].length; i++) {

			sb.append(" ");
			sb.append("<INPUT NAME=" + webFieldId + " TYPE=RADIO ");

			if (choices[0][i].equalsIgnoreCase((String) value)) {
				selected = " CHECKED  ";
			} else {
				selected = "";
			}
			sb.append(" value=");
			sb.append(choices[0][i]);
			sb.append(selected);
			sb.append(">&nbsp;");
			sb.append(choices[1][i]);

		}
		sb.append("<br>");
		return sb.toString();
	}
}
