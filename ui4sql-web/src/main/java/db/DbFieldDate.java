/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package db;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;

/**
 * @author PAULSEAR
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 * 
 * 8/25/06 - SQLSERVER - use empty string for formatted date
 * 4/3/907 - Add method to return the Date value
 */

public class DbFieldDate extends DbField {

	private boolean mySQL = true;
	// db2
	// private static SimpleDateFormat sdf_in = new
	// SimpleDateFormat("MM-dd-yyyy");
	private static SimpleDateFormat sdf_in = new SimpleDateFormat("yyyy/MM/dd");


	private static SimpleDateFormat sdf_out = new SimpleDateFormat("MM/dd/yyyy");

	// **************************
	// CONSTRUCTORS
	// **************************

	// just the field name, default date to now
	public DbFieldDate(String parmName) {
		// super(parmName, new Date());
		// WARNING... CHANGED:
		super(parmName);
	}

	// field name and Date
	public DbFieldDate(String parmName, Date parmValue) {

		super(parmName, parmValue);
		//debug("DbFieldDate : constructor with Date as second parm" + parmName
			//	+ " date : " + parmValue.toString());
	}

	// field name and Date
	public DbFieldDate(String parmName, String parmValue) {

		super(parmName, new Date());
		setValue(parmValue);
		//debug("DbFieldDate : constructor with String as second parm" + parmName
			//	+ " value " + parmValue);
	}

	// field name and Date
	public DbFieldDate(String parmName, String formFieldName, String parmValue) {

		super(parmName, formFieldName, new Date());
		setValue(parmValue);
		//debug("DbFieldDate : constructor 3 parms with String as 3rd "
			//	+ parmName + " :" + parmValue);

	}

	// *****************************
	// PUBLIC METHODS
	// *****************************

	public boolean hasValidDate() {

		if (fieldValue == null)
			return false;
		return true;
	}
	
	public void setMySQL(boolean b) {
		mySQL = b;
	}

	
	public String getText() {

		// System.out.println("DbFieldInteger:getHTML - starting");
		if (fieldValue != null) {
			// System.out.println("clas of field " +
			// fieldValue.getClass().getClass());
			// return fieldValue.toString();

			String s = sdf_out.format(fieldValue);
			//debug("DbFieldDate. getText . for " + fieldName + " returning: "
				//	+ s);
			return s;

		} else {
			// System.out.println("DbFieldInteger:getHTML - fieldValue is
			// null");
			return new String("");
		}
	}

	public String getSQL() {

		String formattedDate;

		//debug("DbFieldDate.getSQL for " + fieldName);

		if (fieldValue == null) {
			//debug(" the field value is null");
			if (mySQL)
				formattedDate = "0000-00-00"; // MYSQL
			else
				formattedDate = ""; // SQLSERVER

		} else {
			//debug("DbFieldDate.getSql value: " + fieldValue.toString());
			try {
				formattedDate = sdf_in.format(fieldValue);

			} catch (NullPointerException e) {
				System.out.println("DbFieldDate;getSQL - Null");
				if (mySQL)
					formattedDate = "null"; // mysql
				else
					formattedDate = ""; // sql server
			} catch (Exception e) {
				System.out.println("DbFieldDate;getSQL - Null");
				if (mySQL)
					formattedDate = "null"; // mysql
				else
					formattedDate = ""; // sql server
			}
		}
		//debug("DbFieldDate:getSQL - returning : " + formattedDate);
		return "'" + formattedDate + "'";

	}

	public void setValue(String dateString) {
		//debug("dbFieldDate... setting value" + dateString);

		ParsePosition pp = new ParsePosition(0);

		try {
			fieldValue = sdf_out.parse(dateString, pp);

			//Date d = (Date) fieldValue;

			//debug("setValue .. parsed to " + d.toString());

		} catch (NullPointerException e) {
			//debug("dbFieldDate ... exception setting value to " + dateString);
			// ISSUE:
			// fieldValue = sdf_in.parse("00-00-0000", pp);
			fieldValue = sdf_in.parse("", pp);

		}

	}

}