/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen
 * 
 * Change log..
 * 
 * 1/28/07 - just like WebFieldDate, only input box longer
 * 		- strip out 12:00 times
 *  
 * 
 */
public class WebFieldDateTime extends WebField {

	public WebFieldDateTime(String parmFieldId, String parmWebText) {

		super(parmFieldId, parmWebText);

	}

	public WebFieldDateTime(String parmFieldId) {

		super(parmFieldId);

	}

	/***************************************************************************
	 * 
	 * Legacy... change to getHTML
	 * 
	 **************************************************************************/

	public String getHTML(String parmMode) {

		String html = new String();

		if (parmMode.equalsIgnoreCase("show")) {
			html = (String) value;
			if (html.equalsIgnoreCase("01/01/1900 12:00")){
				html = "";
			}
			if (html.endsWith("12:00")) {
				html = html.substring(0, (html.length() - 6 ));
			}
			
		}

		if (parmMode.equalsIgnoreCase("edit")) {
			
			String theDate = htmlHelper.getHTML((String) value);
			
			if (theDate.equalsIgnoreCase("01/01/1900 12:00")){
				theDate = "";
			}
			if (theDate.endsWith("12:00")) {
				theDate = theDate.substring(0, (theDate.length() - 6 ));
			}
						
			html = "<input TYPE=TEXT NAME=" + webFieldId
					+ " size=16  maxFieldSize=16 VALUE=\""
					+ theDate + "\">";

		}

		if (parmMode.equalsIgnoreCase("add")) {
			html = "<INPUT TYPE=TEXT NAME=" + webFieldId
					+ " size=16 maxlength=16>";
		}

		return html;

	}

	/***************************************************************************
	 * 
	 * Legacy... change to getHTML
	 * 
	 **************************************************************************/

	public String getJS(String parmMode) {

		String html = new String();

		if (parmMode.equalsIgnoreCase("show")) {
			html = "\ndocument.all['" + webFieldId + "'].outerHTML = '<p>"
					+ (String) value + "</p>';";
		}

		if (parmMode.equalsIgnoreCase("edit")) {
			html = "\ndocument.all['" + webFieldId
					+ "'].outerHTML = '<input TYPE=TEXT NAME=" + webFieldId
					+ " size=16  maxFieldSize=16 VALUE=\""
					+ (String) htmlHelper.getHTML((String) value) + "\">';";

		}

		if (parmMode.equalsIgnoreCase("add")) {
			html = "\ndocument.all['" + webFieldId
					+ "'].outerHTML = '<INPUT TYPE=TEXT NAME=" + webFieldId
					+ " size=16 maxlength=16>';";
		}

		return html;

	}

}
