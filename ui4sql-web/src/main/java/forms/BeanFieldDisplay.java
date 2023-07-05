/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/*
 * Change log:
 * 
 * Add 3-string contstructor to allow passing in a constant value to display, instead of db source
 * 
 */
import db.DbInterface;
import router.SessionMgr;

public class BeanFieldDisplay extends BeanWebField {

	private String showValue = "";
	private boolean showThis = false;

	public BeanFieldDisplay() {
		super();
		debug("BeanFieldDisplay - Constructor 0  no args ");
	}

	public BeanFieldDisplay(String name) {

		super(name);
		debug("BeanFieldDisplay - Constructor 1 for : " + name);

	}

	public BeanFieldDisplay(String fieldName, String databaseColumn) {

		super(fieldName, databaseColumn);
		debug("BeanFieldDisplay - Constructor 2 for : " + fieldName + " for db column : " + databaseColumn);

	}

	public BeanFieldDisplay(String fieldName, String databaseColumn, String showValue) {

		super(fieldName, databaseColumn);

		System.out.println("BeanFieldDisplay - Constructor 3 for : " + fieldName);

		showThis = true;
		this.showValue = showValue;

	}

	public String getDisplayValue () {
		System.out.println("BeanFieldDisplay - getDisplayValue 4 for : " + showValue);
		return showValue;
}

	public void setDisplayValue(String showValue) {
		showThis = true;
		this.showValue = showValue;
		System.out.println("BeanFieldDisplay - setDisplayValue 4 for : " + showValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see forms.BeanWebField#getWebField(router.SessionMgr, db.DbInterface,
	 * java.lang.String)
	 * 
	 * Sets
	 */

	public void setDatabaseColumn(String databaseColumn) {
		this.databaseFieldName = databaseColumn;
	}

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		System.out.println("BeanFieldDisplay.getWebField: " + webFieldName + " " + showThis + " value : " + this.showValue);

		if (this.showThis) {

			System.out.println("returning THIS");
			return new WebFieldDisplay(this.webFieldName, this.showValue);
		}


		if (this.webFieldName.endsWith("pct") || this.webFieldName.endsWith("amt")) {
		
			return new WebFieldDisplay(this.webFieldName, (addMode ? "" : db.getInteger(databaseFieldName).toString()));

		}
		
		return new WebFieldDisplay(this.webFieldName, (addMode ? "" : db.getText(databaseFieldName)));

	}
	
	private void debug (String s) {
		//System.out.println (s);
	}
}
