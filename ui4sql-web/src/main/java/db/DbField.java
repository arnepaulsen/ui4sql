/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package db;

/**
 * @author PAULSEAR
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class DbField {

	public boolean debug = false;
	
	public String fieldName;
	public String formFieldName;

	//public String fieldType;
	public Object fieldValue;

	public abstract String getText();

	public abstract String getSQL();

	public abstract void setValue(String x);

	public void setValue(Integer i) {
		fieldValue = i;
	}

	//****************************************
	//*			CONSTRUCTORS
	//****************************************

	public DbField() {
	}

	// 1 parm - just the Name	
	public DbField(String parmName) {
		//debug("field : " + parmName);
		fieldName = parmName;
		formFieldName = parmName;

	}

	// 2 parms - Name + Type + Object value
	public DbField(String parmName, Object parmValue) {

		//debug("field : " + parmName);
		
		fieldName = parmName;
		formFieldName = parmName;
		fieldValue = parmValue;
	}

	//	3 parms... Name, FormName, Object value	
	//	Allow the DbField to hold the FormName
	//		... so you don't have to have equal database and form field names

	public DbField(String parmName, String parmFormName, Object parmValue) {

		//debug ("field : " + parmName);
		
		fieldName = parmName;
		formFieldName = parmFormName;
		fieldValue = parmValue;
	}

	//*********************************
	//			BEANS
	//*********************************

	public String getName() {
		return fieldName;
	}

	// override methods

	public Object getObject() {
		// debug ("dbfield getObject " + fieldValue.toString());
		return fieldValue;

		//if (fieldValue != null) {
		//	return fieldValue;
		//} else {
		//		return new String("");
		//	}
		
	}

	public void debug(String parmMsg) {
			if (debug == true)
				System.out.println("DbField: " + parmMsg);
		}
		
}
