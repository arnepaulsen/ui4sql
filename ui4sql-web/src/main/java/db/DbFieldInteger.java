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
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 * 
 *  * 9/15/06 remove unsued varable
 */
public class DbFieldInteger extends DbField {

	public String selectQuery;

	public String insertQuery;

	public String updateQuery;

	public String whereClause;


	// ***************************
	// CONSTRUCTORS
	// ***************************

	public DbFieldInteger(String parmName) {
		super(parmName, parmName, new Integer("0"));
		debug("field: " + parmName);
	}

	// pass parmName as both the fieldId and the formFieldId
	public DbFieldInteger(String parmName, Integer parmValue) {

		super(parmName, parmName, parmValue);
		//debug("field: " + parmName);
	}

	public DbFieldInteger(String parmName, String parmFormName, String parmValue) {

		super(parmName, parmFormName, new Integer(parmValue));
		//debug("field: " + parmName);
	}

	public DbFieldInteger(String parmName, String parmFormName,
			Integer parmValue) {

		super(parmName, parmFormName, parmValue);
		//debug("field: " + parmName);
	}

	// ***********************
	// PUBLIC
	// ************************

	public void setValue(String value) {
		//debug("DbFieldInteger ... " + fieldName + " setting to " );
		//debug ("  .. value:  "  + value);
		try {

			fieldValue = new Integer(value);
		} catch (NumberFormatException e) {
			System.out
					.println("DbFieldInteger... exception converting to number");
			fieldValue = new Integer(0);
		}

	}

	public void setValue(Integer value) {
		
	//	debug("DbFieldInteger ... " + fieldName + " setting to " );
		//	debug ("  .. value:  "  + value);
		
		fieldValue = value;

	}

	public String getText() {

		// System.out.println("DbFieldInteger:getHTML - starting");
		if (fieldValue != null) {
			return fieldValue.toString();
		} else {
			// System.out.println("DbFieldInteger:getHTML - fieldValue is
			// null");
			return new String("");
		}
	}

	public Integer getInteger() {

		if (fieldValue != null) {
			return (Integer) fieldValue;
		} else
			return new Integer("0");
	}

	public String getSQL() {
		if (fieldValue != null) {
			//debug ("dbField-Int: getSQL for :  " + fieldName + " value " +  fieldValue.toString() );
			return fieldValue.toString();
		} else {
			//debug("dbfield-int: getSQL for " + fieldName + " returning a 0 ");
			return new String("0");
		}
	}

}