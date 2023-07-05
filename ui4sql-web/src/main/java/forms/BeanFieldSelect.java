/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import db.DbInterface;
import router.SessionMgr;
import java.util.Hashtable;
import java.util.Objects;

/*
 * 
 * 
 * changes : 
 * 
 * 11/3/09 - new Constructor string, string, string, boolean
 * 				set boolean true to use getCodesAlt
 *			see TestPlanPlugin  
 * 12/19 	Allow to set a custom filter for list/excel where clause
 */

public class BeanFieldSelect extends BeanWebField {

	private String codesetName = "";

	private boolean usingHT = false;
	private boolean usingCodeSet = false;
	private boolean useAltCodeSet = false; // to force getCodesAlt call
	private boolean usingArray = false;
	private boolean usingQuery = false;

	private boolean usingCustomSQL_filter = false;
	private String customSQL_filter;

	private String[][] choiceArray = null;
	private Hashtable choiceHashtable = null;
	private String choiceQuery = null;
	private String listDefault = null;
	private Integer listDefaultNumber = null;

	private boolean showPleaseSelect = true;
	private boolean multiSelect = false;
	private String promptText = "-Please Choose--";
	private boolean allowEdit = true; // some selectores need to block
	// selection
	// even in edit mode
	private String[] multiChoiceSelecteList = null;

	/*
	 * 2 most common constructors will be Form with codeset Form with Integer ht
	 */

	public BeanFieldSelect() {
		super();
	}

	public void setAltCodeSet(boolean b) {
		useAltCodeSet = b;
	}

	public BeanFieldSelect(String fieldName, String defaultValue, String codeSet) {

		super(fieldName);
		this.defaultStringValue = defaultValue;
		usingCodeSet = true;
		showPleaseSelect = true;
		codesetName = codeSet;

	}

	public BeanFieldSelect(String fieldName, String defaultValue, String codeSet, boolean useAltCodeSet) {

		super(fieldName);
		this.defaultStringValue = defaultValue;
		usingCodeSet = true;
		showPleaseSelect = true;
		codesetName = codeSet;
		this.useAltCodeSet = useAltCodeSet;

	}

	public BeanFieldSelect(String fieldName, String defaultValue, String codeSet_empty, String sql) {


		super(fieldName);
		this.defaultStringValue = defaultValue;
		usingCodeSet = true;
		showPleaseSelect = true;
		codesetName = "SQL";
		this.choiceQuery = sql;
		this.usingQuery = true;

	}

	public BeanFieldSelect(String fieldName, Integer defaultValue, String codeSet) {
		super(fieldName);


		
		if (Objects.isNull(defaultValue)) {
			debug("  parameter DefaulValue is null ");
			defaultIntegerValue = Integer.valueOf("1");

		}
	
	
		this.defaultIntegerValue= Integer.valueOf(0);
		
		try  {
			this.defaultIntegerValue = Integer.valueOf(defaultValue);			
		}
		catch (Exception e) {
			
			System.out.println ("Exception converting defaultValue to Integer : " + e.toString());
		}


		showPleaseSelect = true;
		codesetName = codeSet;
		this.isString = false;
		

	}

	/*
	 * MOST COMMON FILTER IS THIS ONE! these are common for list filters .. hint..
	 * the ones for list filters always start with an int for the list column#
	 */
	public BeanFieldSelect(int column, String filterName, String ColumnName, String defaultValue, String listDefault,
			String prompt, String codeSet) {
		super(filterName, ColumnName);

		this.defaultStringValue = defaultValue;
		codesetName = codeSet;
		showPleaseSelect = true;
		promptText = prompt;
		this.setListColumn(column);
		this.listDefault = listDefault;

	}

	/*
	 * list with sql! codeSet value should be "SQL" !!!
	 */
	public BeanFieldSelect(int column, String filterName, String ColumnName, String defaultValue, String listDefault,
			String prompt, String codeSet, String query) {
		super(filterName, ColumnName);
		this.defaultStringValue = defaultValue;
		codesetName = "SQL";
		this.usingQuery = true;
		this.choiceQuery = query;
		showPleaseSelect = true;
		promptText = prompt;
		this.setListColumn(column);
		this.listDefault = listDefault;

	}

	public BeanFieldSelect(int column, String filterName, String ColumnName, Integer defaultInteger,
			Integer listDefault, String prompt, String codeSet) {
		super(filterName, ColumnName);
		this.defaultIntegerValue = defaultInteger;
		codesetName = codeSet;
		showPleaseSelect = true;
		promptText = prompt;
		this.setListColumn(column);
		this.listDefaultNumber = listDefault;
		this.isString = false;

	}

	public BeanFieldSelect(int column, String filterName, String ColumnName, Integer defaultInteger,
			Integer listDefault, String prompt, Hashtable ht) {
		super(filterName, ColumnName);
	
		this.defaultIntegerValue = defaultInteger;
		this.choiceHashtable = ht;
		this.usingHT = true;
		showPleaseSelect = true;
		promptText = prompt;
		this.setListColumn(column);
		this.listDefaultNumber = listDefault;
		this.isString = false;

	}

	/*
	 * to use with SQL query
	 */
	public BeanFieldSelect(int column, String filterName, String ColumnName, Integer defaultInteger,
			Integer listDefault, String prompt, String codeSet, String query) {
		super(filterName, ColumnName);
	
		this.defaultIntegerValue = defaultInteger;
		codesetName = codeSet;
		showPleaseSelect = true;
		promptText = prompt;
		this.usingQuery = true;
		this.choiceQuery = query;
		this.setListColumn(column);
		this.listDefaultNumber = listDefault;
		this.isString = false;

	}

	/*
	 * Set the selector / hashtable
	 */

	public void setCustomFilter(String sql) {
		usingCustomSQL_filter = true;
		customSQL_filter = sql;
	}

	public void setQuery(String choiceQuery) {
		this.usingQuery = true;
		this.choiceQuery = choiceQuery;
	}

	public void setChoiceHashtable(Hashtable choiceHashtable) {
		this.usingHT = true;
		this.choiceHashtable = choiceHashtable;
	}

	public void setChoiceArray(String[][] choiceArray) {
		this.usingArray = true;
		this.choiceArray = choiceArray;
	}

	public void setCodeSet(String s) {
		this.usingCodeSet = true;
		this.codesetName = s;
	}

	public void setCodeSetName(String codeSet) {
		this.codesetName = codeSet;
	}

	public void setListDefault(String listDefault) {
		this.listDefault = listDefault;
	}

	public void setListDefaultNumber(Integer listDefault) {
		this.listDefaultNumber = listDefault;
	}

	public String getListDefault() {
		return this.listDefault;
	}

	public Integer getListDefaultNumber() {
		return this.listDefaultNumber;
	}

	/*
	 * 
	 */

	public void setAllowEdit(boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	public void setPleaseSelect(boolean pleaseSelect) {
		this.showPleaseSelect = pleaseSelect;
	}

	public void setIsMultiSelect(boolean isMultiSelect) {
		multiSelect = isMultiSelect;
	}

	public void setMultiChoiceSelectedList(String[] multiChoiceSelectedList) {
		this.multiChoiceSelecteList = multiChoiceSelectedList;
	}

	/*
	 * if key is integer, then the codeset will not be from tcodes, .. it has to be
	 * either name of session hashtable, or a query
	 */

	public String getListQueryFilter(SessionMgr sm) {

		if (isString) {
			// return filter on web page if present and not = 0 / 'please
			// select' entry
			// debug("... doing a string filter.. parm is : "
			// + sm.Parm(webFieldName));
			if (sm.Parm(webFieldName).length() > 0) {
				if (!sm.Parm(webFieldName).equalsIgnoreCase("0")) {

					// allow custom filter on list/excel query
					if (usingCustomSQL_filter) {
						return " AND " + customSQL_filter;
					}
					return " AND " + databaseFieldName + " = '" + sm.Parm(webFieldName) + "'";
				} else {
					return "";
				}
			} else {
				if (this.listDefault != null && this.listDefault.length() > 0) {

					if (usingCustomSQL_filter) {
						return " AND " + customSQL_filter;
					}

					return " AND " + databaseFieldName + " = " + "'" + listDefault + "'";
				}
			}
		} else {
			// return filter on web page value if present and not = 0 / please
			// select

			if (sm.Parm(webFieldName).equalsIgnoreCase("0"))
				return "";

			if (sm.Parm(webFieldName).length() > 0) {
				return " AND " + databaseFieldName + " = " + sm.Parm(webFieldName);

			} else {
				if (this.listDefaultNumber > 0) {

					if (this.listDefaultNumber == 99) {
						return " AND " + databaseFieldName + " = " + sm.getUserId().toString();
					} else {
						return " AND " + databaseFieldName + " = " + this.listDefaultNumber.toString();
					}

				}
			}

		}

		return "";

	}

	public WebField getWebField(SessionMgr sm, DbInterface db, String mode) {

		
		
		System.out.println("BeanFieldSelect.getWebField starting for " +  webFieldName + "  ... for mode: " + mode );
		
		if (Objects.isNull(defaultIntegerValue)) {
			System.out.println (" ... default integer value is null");
			defaultIntegerValue = Integer.valueOf("1");
		}
		
		

		boolean addMode = mode.equalsIgnoreCase("add") ? true : false;
		boolean listMode = mode.equalsIgnoreCase("list") ? true : false;


		WebFieldSelect wf;

		if (isString) {
			System.out.println(" .. its a string");

			String value = "";

			if (listMode) {
				if (sm.Parm(webFieldName).length() == 0)
					value = this.listDefault;

				else
					value = sm.Parm(webFieldName);

			} else {
				value = (addMode ? defaultStringValue
						: (db.getText(this.databaseFieldName) == null) ? "" : db.getText(this.databaseFieldName));

			}
			wf = new WebFieldSelect(webFieldName, value);

		} else {
			//System.out.println(" Not a string... select : def value " + defaultIntegerValue.toString());

			if (listMode) {

			
				wf = new WebFieldSelect(webFieldName,
						(sm.Parm(webFieldName).length() == 0)
								? (this.listDefaultNumber == 99 ? sm.getUserId() : this.listDefaultNumber)
								: new Integer(sm.Parm(webFieldName)));
				//System.out.println("wf ok");

			} else {

				//System.out.println(".. not list mode");

				if (addMode) {
					// special trigger of default 99 mean put userId (ME) as default
					// if add mode , use either the session getUserId , otherwise just use 0 for
					// normal select (please select)
					if (this.defaultIntegerValue == 99) {
						debug ("BeanSelect.getWebField ... setting selected value logged in user ");
						wf = new WebFieldSelect(webFieldName, sm.getUserId());
					} else {
						// otherwise just use zero for 'select something'
						debug ("BeanSelect.getWebField ... creating the WebField with 2 constructor   1 name and 2 zero as default ");
						wf = new WebFieldSelect(webFieldName, Integer.valueOf(0));
					}
				}

				else {
				
					// must be edit mode, so put in the database value saved from before
					wf = new WebFieldSelect(webFieldName,  db.getInteger(this.databaseFieldName));
				}


			}

		}

		//debug(" Bean Select .. code set: " + codesetName);

		wf.setPleaseSelect(this.showPleaseSelect);
		wf.setSelectPrompt(promptText);
		wf.setAllowEdit(allowEdit);

		//if (usingHT )..addMode. old code
		if (usingHT) {
			debug("..using ht");
			wf.setListHashtable(choiceHashtable);
		} else {
			if (usingArray) {
				debug("using array");
				wf.setChoices(choiceArray);
			} else {
				if (usingQuery) {
					// debug("using query");
					Hashtable ht = new Hashtable();
					try {
						ht = db.getLookupTable(smartQuery(sm, choiceQuery));
					} catch (Exception e) {
						debug("BeanFieldSelect.. sql error " + e.toString());
					}
					wf.setListHashtable(ht);

				} else {
					// debug("codesetname : " + codesetName);

					if (codesetName.equalsIgnoreCase("userht"))
						wf.setListHashtable(sm.getUserHT());
					else {
						if (codesetName.equalsIgnoreCase("contactHT")) {
							wf.setListHashtable(sm.getContactHT());
						} else {
							if (useAltCodeSet)
								wf.setListHashtable(sm.getCodesAlt(codesetName));
							else
								wf.setListHashtable(sm.getCodes(codesetName));
						}
					}
				}
			}
		}

		return wf;

	}

	/*
	 * replace query symbolics with run-time values
	 */
	private String smartQuery(SessionMgr sm, String s) {

		String x = s.replaceAll("%PROJECTID%", sm.getProjectId().toString());
		// debug("smart query : " + x);
		return x;

	}

	private void debug(String s) {
		//System.out.println(s);
	}
}
