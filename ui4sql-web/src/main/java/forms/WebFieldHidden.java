/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen
 * 
 * Used to populate a form 'hidden' input variable 
 * 
**/
public class WebFieldHidden extends WebField {

	public WebFieldHidden(String parmFieldId, String parmWebText) {

		super(parmFieldId, parmWebText);

	}

	public String getHTML(String parmMode) {

		return new String(
			"<INPUT TYPE=HIDDEN NAME="
				+ webFieldId
				+ "  value=\""
				+ (String) htmlHelper.getHTML((String) value)
				+ "\">");
	}
	
	public String getJS(String parmMode) {

		return new String(
			"\ndocument.all['"
				+ webFieldId
				+ "'].outerHTML = '<INPUT TYPE=HIDDEN NAME="
				+ webFieldId
				+ "  value=\""
				+ (String) htmlHelper.getHTML((String) value)
				+ "\">';");
	}
}
