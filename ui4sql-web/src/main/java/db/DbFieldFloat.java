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
 * 
 * 9/15/06 remove unsued varable
 */
public class DbFieldFloat extends DbField {

	public String selectQuery;
	public String insertQuery;
	public String updateQuery;
	public String whereClause;


	//***************************
	//		CONSTRUCTORS
	//***************************

	public DbFieldFloat(String parmName) {
		super(parmName, parmName, new Float("0"));

	}

	// pass parmName as both the fieldId and the formFieldId
	public DbFieldFloat(String parmName, Float parmValue) {

		super(parmName, parmName, parmValue);
	}

	public DbFieldFloat(String parmName, String parmFormName, String parmValue) {

		super(parmName, parmFormName, new Float(parmValue));

	}
	
	public DbFieldFloat(String parmName, String parmFormName, Float parmValue) {

			super(parmName, parmFormName, parmValue);

		}
		

	//***********************
	//		PUBLIC
	//************************

	public void setValue(String value) {
		debug ("DbFieldFloat ... setting to " + value );
		try {
			
			fieldValue = new Float(value);
		} catch (NumberFormatException e) {
			System.out.println("DbFieldFloat... exception converting to number");
			fieldValue = new Float(0);
		}

	}

	public void setValue(Float value) {
		debug("DbFieldFloat.. seting Float " + value.toString());
		fieldValue = value;

	}

	public String getText() {

		//		System.out.println("DbFieldInteger:getHTML - starting");
		if (fieldValue != null) {

			return fieldValue.toString();
		} else {
			//			System.out.println("DbFieldInteger:getHTML - fieldValue is null");
			return new String("");
		}
	}

	public Float getFloat() {

		if (fieldValue != null) {
			return (Float) fieldValue;
		} else
			return new Float("0");
	}

	public String getSQL() {
		if (fieldValue != null) {
			return fieldValue.toString();
		} else {
			return new String("0");
		}
	}

}