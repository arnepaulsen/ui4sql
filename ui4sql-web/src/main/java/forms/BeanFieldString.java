/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import db.DbInterface;
import router.SessionMgr;

/*
 * 1/14/10 add list filter capability and constructor
 */
public class BeanFieldString extends BeanWebField {

	private int displayWidth;
	private int maxDisplayWidth;

	public BeanFieldString() {
		super();
	}

	public void setWidth(int width) {
		this.displayWidth = width;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxDisplayWidth = maxWidth;
	}

	public BeanFieldString(String name, String defaultValue, int width,
			int maxWidth) {

		super(name);
		this.displayWidth = width;
		this.maxDisplayWidth = maxWidth;
		this.defaultStringValue = defaultValue;
	}
	
	
	// new one to take width and maxWidth as Intergers 
	public BeanFieldString(String name, String defaultValue, Integer width,
			Integer maxWidth) {

		super(name);
		this.displayWidth = Integer.valueOf(width);
		this.maxDisplayWidth = Integer.valueOf(maxWidth);
		this.defaultStringValue = defaultValue;
	}

	
	
	public BeanFieldString(String name, int width, int maxWidth) {

		super(name);
		this.displayWidth = width;
		this.maxDisplayWidth = maxWidth;
		this.defaultStringValue = "";

	}

	/*
	 * when used as filters, constructor as column # as first parm
	 */
	public BeanFieldString(int listColumn, String name, int width, int maxWidth) {
		super(name);
		this.displayWidth = width;
		this.maxDisplayWidth = maxWidth;
		this.defaultStringValue = "";
		this.setListColumn(listColumn);
	}

	public BeanFieldString(int listColumn, String name, Integer width, Integer maxWidth) {
		super(name);
		this.displayWidth = Integer.valueOf(width);
		this.maxDisplayWidth = Integer.valueOf(maxWidth);
		this.defaultStringValue = "";
		this.setListColumn(listColumn);
	}
		
		
	public BeanFieldString(int listColumn, String name, String defaultValue,
			int width, int maxWidth) {
		super(name);
		this.displayWidth = width;
		this.maxDisplayWidth = maxWidth;
		this.defaultStringValue = defaultValue;
		this.setListColumn(listColumn);
	}

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		// System.out.println("BeanFieldString - getWebString " );
		//System.out.println(" getWebField String for name: "
			//	+ this.getWebFieldName());

		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		if (this.webFieldName.endsWith("pct")
				|| this.webFieldName.endsWith("amt")) {

			return new WebFieldString(this.webFieldName,
					(addMode ? defaultStringValue : db.getInteger(
							databaseFieldName).toString()), displayWidth,
					maxDisplayWidth);

		}

//		System.out.println("beanFieldString getWebField" + this.webFieldName
//				+ " ..default string");

		WebFieldString wf;

		String default_or_parm = sm.Parm(webFieldName).length() == 0 ? defaultStringValue
				: sm.Parm(webFieldName);

//		System.out.println(" BeanFieldString: listColumn "
//				+ this.getListColumn());
		try {
			wf = new WebFieldString(this.webFieldName, ((addMode || this
					.getListColumn() != 99) ? default_or_parm : db
					.getText(databaseFieldName)), displayWidth, maxDisplayWidth);

			return wf;
		} catch (Exception e) {
			System.out.println(" width " + displayWidth);

			System.out.println(" error in getWebField .. for "
					+ this.webFieldName);
		}

		return null;

	}

	public String getListQueryFilter(SessionMgr sm) {
		
		
		// don't qualifiy list if empty box, or it still contains the default / display value
		if (sm.Parm(webFieldName).length() < 1
				|| sm.Parm(webFieldName).equalsIgnoreCase(
						this.defaultStringValue))
			
			return "";


		return new String(" AND " + webFieldName + " LIKE '%"
				+ sm.Parm(webFieldName) + "%' ");

	}
}
