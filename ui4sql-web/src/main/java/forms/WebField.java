/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/**
 * @author Arne Paulsen
 * 
 * 8.28 .. just toggle debug switch
 * 
**/
public abstract class WebField {

	public static forms.HtmlHelper htmlHelper = new forms.HtmlHelper();
	public Object value;
	// the dbDatabase value, must be Object, not Java boolean, int, etc.
	public String webFieldId;
	public String fieldType;
	public boolean keyField = false; // if true, user input not allowed.
	public boolean debug = false;

	// 1 parm ... just the fieldName
	public WebField(String parmFieldId) {

		webFieldId = parmFieldId;
		value = null;
		//debug("WebField constructor for " + parmFieldId);
	}

	// 2 parms : the field Name and the object value
	public WebField(String parmFieldId, Object parmValue) {

		webFieldId = parmFieldId;
		value = parmValue;
		//debug("WebField constructor for " + parmFieldId);
	}

	public void setSelectedValue(Object value) {
		this.value = value;
	}
	
	// 3 parms.  . field name, value, and type
	public WebField(
		String parmFieldId,
		Object parmValue,
		String parmFieldType) {

		webFieldId = parmFieldId;
		value = parmValue;
		fieldType = parmFieldType;
		//debug("WebField constructor for " + parmFieldId);
	}

	public abstract String getHTML(String parmMode);

	public void debug(String parmMsg) {
		if (debug)
			System.out.println(parmMsg);
	}
}
