/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import db.DbInterface;
import router.SessionMgr;

public class BeanFieldText extends BeanWebField {

	private int columns;
	private int rows;

	public BeanFieldText(String name, int rows, int columns) {

		super(name);
		this.columns = columns;
		this.rows = rows;
		this.defaultStringValue = "";
	}
	
	public BeanFieldText(String name, int rows, int columns, String defaultValue) {

		super(name);
		this.columns = columns;
		this.rows = rows;
		this.defaultStringValue = defaultValue;
	}
	
	public BeanFieldText(String name, Integer rows, Integer columns, String defaultValue) {

		super(name);
		this.columns = Integer.valueOf(columns);
		this.rows = Integer.valueOf (rows);
		this.defaultStringValue = defaultValue;
	}
	
	
	
	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	public void setRows(int rows) {
		this.rows = rows;
	}

	public BeanFieldText() {
		columns =80;
		rows = 3;
	}
	
	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		//System.out.println ( " name: "+ this.getWebFieldName());
		
		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		return new WebFieldText(this.webFieldName, (addMode ? this.defaultStringValue : db
				.getText(databaseFieldName)), rows, columns);

	}
}
