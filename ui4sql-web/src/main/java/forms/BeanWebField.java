/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;
import router.SessionMgr;
import db.DbInterface;


// TAG:UI4SQLV1 	overloaded setter methods for defaultValue creates gender confusion

public abstract class BeanWebField {
	
	
	public String webFieldName;
	public String databaseFieldName = null;
	public String defaultStringValue = null;
	public Integer defaultIntegerValue = null;
	public String defaultValue = null;
	
	public boolean isString = true;
	
	
	
	// stuff just for filterse
	private int listColumn = 99;	
	
	
		
	public abstract WebField getWebField(SessionMgr sm, DbInterface db, String mode);
	
	
	public String getListQueryFilter(SessionMgr sm ) {
		return "";
	}
	
	/*
	 * Constructors
	 */
	
	public BeanWebField() {
		
	}
	public BeanWebField(String fieldName) {
		this.webFieldName = fieldName;
		this.databaseFieldName = fieldName;
		this.isString = true;
	}
	
	public BeanWebField(String fieldName, String databaseName) {
		this.webFieldName = fieldName;
		this.databaseFieldName = databaseName;
		this.isString = true;
	}
	
	public BeanWebField(String fieldName, String databaseName, String defaultValue) {
		this.webFieldName = fieldName;
		this.databaseFieldName = databaseName;
		this.defaultStringValue = defaultValue;
		this.defaultIntegerValue = Integer.valueOf(0);
		this.isString = true;
		
	}
	
	
	// TAG:UI4SQLV1  overloaded setter methods for defaultValue creates gender confusion
	
	//public BeanWebField(String fieldName, String databaseName, Integer defaultValue) {
	//	this.webFieldName = fieldName;
	//	this.databaseFieldName = databaseName;
	//	this.defaultIntegerValue = defaultValue;
	//	this.isString = false;
		
//	}
		

	/*
	 * Sets
	 */
	
	public void setListColumn(int i) {
		listColumn = i;
	}

	public int getListColumn() {
		return listColumn ;
	}
	
	public void setName(String name) {
		this.webFieldName = name;
		this.databaseFieldName = name;
		
	}
	
	public void setDatabaseFieldName(String databaseFieldName) {
		this.databaseFieldName = databaseFieldName;
	}
	
	public void setDefaultValue(String defaultValue){
		this.defaultStringValue = defaultValue;
		this.defaultValue = defaultValue;
		this.isString = true;
	}
	
	
	// TAG:UI4SQLV1  overloaded setter methods for defaultValue creates gender confusion
	
	//public void setDefaultValue(Integer defaultValue){
	//	this.defaultIntegerValue = defaultValue;
	//	this.isString = false;
	//}

	
	/*
	 * gets
	 */
	
	public String getWebFieldName() {
		return webFieldName;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	
	
}
