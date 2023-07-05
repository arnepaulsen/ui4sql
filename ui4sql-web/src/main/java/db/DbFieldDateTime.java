/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package db;

import java.util.Date;
import java.text.SimpleDateFormat;
// import java.text.DateFormat;
import java.text.ParsePosition;

/**
 * @author PAULSEAR
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 * 
 * 
 * 8/24/06 SQLSERVER use empty string for formatted empty date
 * 
 * 3/26/07 getText returns "" if date = 01/01/1900 00:00 
 * 
 */
public class DbFieldDateTime extends DbField {

	boolean mySQL = true;

	// db2
	// private static SimpleDateFormat sdf_in = new
	// SimpleDateFormat("MM-dd-yyyy");
	private static SimpleDateFormat sdf_in = new SimpleDateFormat(
			"yyyy/MM/dd hh:mm");

	// mysql

	private static SimpleDateFormat sdf_out = new SimpleDateFormat(
			"MM/dd/yyyy hh:mm");

	// just the field name, default date to now
	public DbFieldDateTime(String parmName) {
		// CHANGE:
		// super(parmName, new Date());
		super(parmName);
	}

	// **************************
	// BEAN Get/Set
	// **************************

	
	public void setMySQL(boolean b) {
		mySQL = b;
	}
	
	// **************************
	// CONSTRUCTORS
	// **************************

	// field name and Date
	public DbFieldDateTime(String parmName, Date parmValue) {

		super(parmName, parmValue);
		//String s = sdf_out.format(parmValue);

		//debug("DbFieldDateTime : constructor with Date as second parm : "
		//		+ parmName + " date-time: " + parmValue.toString());
		//debug(" .. formated : " + s);
	}

	// field name and Date
	public DbFieldDateTime(String parmName, String parmValue) {

		super(parmName, new Date());
		setValue(parmValue);

	}

	// field name and Date
	public DbFieldDateTime(String parmName, String formFieldName,
			String parmValue) {

		super(parmName, formFieldName, new Date());
		setValue(parmValue);
		//debug("DbFieldDateTime : constructor 3 parms with String as 3rd "
			//	+ parmName + " :" + parmValue);

	}

	public String getSQL() {

		// DateFormat df = DateFormat.getDateInstance();

		String formattedDate;

		//debug("DbFieldDateTime.getSQL for " + fieldName);

		if (fieldValue == null) {
			//debug(" the field value is null");
			if (mySQL)
				formattedDate = "0000/00/00"; // mysql
			else
				formattedDate = ""; // SQL SERVER

		} else {
			//debug("DbFieldDateTime.getSql value: " + fieldValue.toString());
			try {
				formattedDate = sdf_in.format(fieldValue);

			} catch (NullPointerException e) {
				System.out.println("DbFieldDateTime;getSQL - Null");
				if (mySQL)
					formattedDate = "0000/00/00"; // mysql
				else
					formattedDate = ""; // SQL SERVER
			} catch (Exception e) {
				System.out.println("DbFieldDateTime;getSQL - Null");
				if (mySQL)
					formattedDate = "0000/00/00"; // mysql
				else
					formattedDate = ""; // SQL SERVER
			}
		}
		//debug("DbFieldDateTime:getSQL - returning : " + formattedDate);
		return "'" + formattedDate + "'";

	}

	// *****************************
	// PUBLIC METHODS
	// *****************************

	public String getText() {

		if (fieldValue != null) {
			// System.out.println("clas of field " +
			// fieldValue.getClass().getClass());
			// return fieldValue.toString();

			String s = sdf_out.format(fieldValue);
			//debug("DbFieldDateTime. getText . for " + fieldName
				//	+ " returning: " + s);
			
			if (s.equalsIgnoreCase("01/01/1900 12:00")) return "";
			
			return s;

		} else {
			// System.out.println("DbFieldInteger:getHTML - fieldValue is
			// null");
			return new String("");
		}
	}

	public boolean hasValidDate() {

		if (fieldValue == null)
			return false;
		return true;
	}

	// this probably won't get used, because these are just used by
	// the audit fields, added_date, updated_date, approved_date, submitted_date
	// ... and those fields are set by direct sql calls with the Plugin.sql
	public void setValue(String dateString) {
		//debug("DbFieldDateTime... setting value" + dateString);

		ParsePosition pp = new ParsePosition(0);

		try {
			fieldValue = sdf_out.parse(dateString, pp);

			//Date d = (Date) fieldValue;

			//debug("setValue .. parsed to " + d.toString());

		} catch (NullPointerException e) {
			System.out
					.println("DbFieldDateTime ... exception setting value to "
							+ dateString);
			fieldValue = sdf_in.parse("01-01-2000", pp);
		}

	}

}