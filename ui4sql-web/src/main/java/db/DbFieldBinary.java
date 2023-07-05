/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package db;


/**
 * @author PAULSEAR
 * 
 *  5/1/05 return an empty string if null
 */
public class DbFieldBinary extends DbField {

	private static db.SqlHelper sql = new SqlHelper();

	// ****************************
	// CONSTRUCTORS
	// ****************************

	// 1 parm
	public DbFieldBinary(String parmName) {
		super(parmName, new String(""));
	}

	// 2 parms
	public DbFieldBinary(String parmName, String parmValue) {
		super(parmName, parmValue);
	}

	// 3 parms
	public DbFieldBinary(String parmName, String parmFormName, String parmValue) {
		super(parmName, parmFormName, parmValue);
	}

	// ****************************
	// PUBLIC GET
	// ****************************

	public String getText() {

		try {
			if (fieldValue != null) {
				return (String) fieldValue;
			} else {
				return new String("");
			}
		} catch (Exception e) {
			return new String("");
		}
	}

	public String getSQL() {
		return "CAST ( " + "'" + sql.doubleQ((String) fieldValue) + "' AS VARBINARY(8000))  ";
	}

	public void setValue(String parmString) {
		fieldValue = parmString;
	}

}
