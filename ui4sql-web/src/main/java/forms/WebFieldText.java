/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * 
 * Write an HTML TextArea control 3/7/05 remove
 * <p>
 * </p>
 * for font display o9/26/06 use new method getTextBox for edit mode 9/10/07
 * allow add to show initial value 9/24/07 remove legacy getJS... long gone.
 */
public class WebFieldText extends WebField {


	public int displayRows;

	public int displayCols;

	public WebFieldText(String parmFieldId, String parmWebText, int parmRows,
			int parmCols) {

		super(parmFieldId, parmWebText);

		displayRows = parmRows;
		displayCols = parmCols;

	}

	public WebFieldText(String parmFieldId) {

		super(parmFieldId);

	}

	public String getHTML(String parmMode) {
		if (parmMode.equalsIgnoreCase("show")) {
		
			return new String((String) htmlHelper.replace_LF_BR((String) value) + "");

		}

		if (parmMode.equalsIgnoreCase("edit")
				|| parmMode.equalsIgnoreCase("add")) {

			return new String("<TEXTAREA  NAME=" + webFieldId + " rows="
					+ displayRows + " cols=" + displayCols + ">"
					+ (String)  value
					+ "</TEXTAREA>");

			
		}

		return new String("");

	}

	

}
