/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen 3/7/05 took out
 *         <p>
 *         </p>
 *         for font fix
 */
public class WebFieldDisplay extends WebField {

	
	public WebFieldDisplay(String parmFieldId, String parmWebText) {

		super(parmFieldId, parmWebText);

	}

	public WebFieldDisplay(String parmFieldId) {

		super(parmFieldId);

	}

	public String getHTML(String parmMode) {
		//System.out.println("WebFieldDisplay:getHTML - " + this.webFieldId
			//	+ " = " + value);

		if (value == null) {
			return new String("<p>&nbsp;</p>");
		} else {
			return htmlHelper.replace_LF_BR((String) value);
		}

	} /*
		 * Legacy... changing to getHTML w/o javaScript
		 * 
		 */

	

}
