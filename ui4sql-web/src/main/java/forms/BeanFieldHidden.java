/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/*
 * Change log:
 * 
 * 8/2/10 - Add constructor for two strings: webField, databaseField
 * 
 */

import db.DbInterface;
import router.SessionMgr;

public class BeanFieldHidden extends BeanWebField {

	public BeanFieldHidden(String fieldName) {

		super(fieldName);

	}
	
	public BeanFieldHidden (String fieldName, String databaseName) {
		super(fieldName, databaseName);
	}
	

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;

		return new WebFieldHidden(this.webFieldName, (addMode ? "" : db
				.getText(databaseFieldName)));

	}
}
