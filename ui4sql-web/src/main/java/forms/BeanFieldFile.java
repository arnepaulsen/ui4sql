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
public class BeanFieldFile extends BeanWebField {

	private int displayWidth;
	private int maxDisplayWidth;

	public BeanFieldFile() {
		super();
	}

	public void setWidth(int width) {
		this.displayWidth = width;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxDisplayWidth = maxWidth;
	}

	public BeanFieldFile(String name, String defaultValue, int width,
			int maxWidth) {

		super(name);
		this.displayWidth = width;
		this.maxDisplayWidth = maxWidth;
		this.defaultStringValue = defaultValue;
	}

	public BeanFieldFile(String name, int width, int maxWidth) {

		super(name);
		this.displayWidth = width;
		this.maxDisplayWidth = maxWidth;
		this.defaultStringValue = "";

	}

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {
		
		System.out.println("WebField: getWebField - WebFieldName" + sm.Parm(webFieldName));

		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		WebFieldFile wf;

		String default_or_parm = sm.Parm(webFieldName).length() == 0 ? defaultStringValue
				: sm.Parm(webFieldName);

		System.out.println(" BeanFieldString: listColumn "
				+ this.getListColumn());
		try {
			wf = new WebFieldFile(this.webFieldName, ((addMode || this
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
