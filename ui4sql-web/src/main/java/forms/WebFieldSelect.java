/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import services.ServicesException;

/**
 * @author Arne Paulsen
 * 
 * 6/22/05 add ability to receive String[][] instead of Hashtable ... used by
 * SecurityChecklist to create lists on the fly
 * 
 * todo: the array only supports strings... needs a litte work to support
 * integers...
 * 
 * 8/22 Hashtable has: 1. key, always a string, which is sort order ) , 2. value
 * is object array [2] that is sort order by 2. value is obj[2] , first is value
 * (either string or integer) , second is always string description ... forces
 * look-up to examine all entries, not just the key
 * 
 * 8/22 add public setOnChangeJavaScript 8/24 at work... put onChangeJavaScript
 * into the getHTML_array logic, plus fix </form>
 * 
 * 8/28 debug on / off 9/13/06 clean up unused variables,
 * @suppress stmt 3/13/08 add ID to field so can edit in html 1/9/08 allow
 *           'multiple' wow, allow to pass in list of selected
 * 
 * 
 * 5/29/09 - add constructor with integer, array and parmchoose
 * 
 * 9/21/10 - Add constructor to allow an array + the select text
 * 
 * 11/1/10 - New Constructor with Array and AllowEdit choice
 * 
 * 
 */

public class WebFieldSelect extends WebField {

	private Hashtable ht;

	String[][] choicesArray;

	String[] selectedList;

	String multiple = "";

	private String displayClass = "";

	// * options
	private boolean pleaseSelect = false;

	boolean multiSelect = false;

	boolean usingArray = false;

	boolean usingQuery = false;

	private boolean submitOnClick = false;

	boolean allowEdit = true; // false = display the desc in edit mode instead

	private String choiceQuery = null;

	String promptText = "-Please Choose--";

	/*
	 * 8/22 new for list filters:
	 */
	String onChangeJavaScript = " onchange=\"document.forms[0].submit(); \"";

	/*
	 * 
	 * BEAN SETTERS
	 */

	public void setPleaseSelect(boolean pleaseSelect) {
		this.pleaseSelect = pleaseSelect;
	}

	public void setSubmitOnClick(boolean submitOnClick) {
		this.submitOnClick = submitOnClick;
	}

	public void setSelectedList(String[] x) {
		selectedList = x;
	}

	public void setOnChangeJavaScript(String javaScript) {
		onChangeJavaScript = javaScript;
	}

	public void setDisplayClass(String sDisplayClass) {
		displayClass = sDisplayClass;
	}
	
	public void setAllowEdit (boolean b) {
		allowEdit=b;
	}

	public void setSelectPrompt(String promptText) {
		this.promptText = promptText;
	}

	public void allowMultiple() {
		multiSelect = true;
		multiple = " MULTIPLE SIZE=3 ";
	}

	public void setQuery(String choiceQuery) {
		this.usingQuery = true;
		this.choiceQuery = choiceQuery;
	}

	public void setListHashtable(Hashtable listHashtable) {
		this.ht = listHashtable;
	}

	public void setChoices(String[][] choices) {
		this.usingArray = true;
		this.choicesArray = choices;
	}

	/*
	 * special version for List Filter selectors, passing in the query to build
	 * the ht here!
	 */
	public WebFieldSelect(String parmFieldId, String parmValue,
			db.DbInterface db, String query, String selectPrompt) {
		super(parmFieldId, parmValue);

		setDisplayClass("listform");
		// setOnChangeJavaScript("
		// onchange=\"setAction('list');document.forms[0].submit(); \"");

		pleaseSelect = true;
		promptText = selectPrompt;

		try {
			ht = db.getLookupTable(query);
		} catch (ServicesException se) {

		}
	}

	// special version for List Selectors, but with Interger (usually for user
	// lists and parent table keys
	public WebFieldSelect(String parmFieldId, Integer parmValue,
			db.DbInterface db, String query, String selectPrompt) {
		super(parmFieldId, parmValue);

		setDisplayClass("listform");
		// setOnChangeJavaScript("
		// onchange=\"setAction('list');document.forms[0].submit(); \"");

		pleaseSelect = true;
		promptText = selectPrompt;

		try {
			ht = db.getLookupTable(query);
		} catch (ServicesException se) {

		}
	}

	public WebFieldSelect(String parmFieldId, Integer parmValue) {
		super(parmFieldId, parmValue);

	}

	public WebFieldSelect(String parmFieldId, String parmValue) {
		super(parmFieldId, parmValue);

	}

	// basic 1. web field id, 2. Integer value 3. ht ("PleaseSelect" option is
	// off)

	public WebFieldSelect(String parmFieldId, Integer parmValue,
			Hashtable parmHashtable) {
		super(parmFieldId, parmValue);
		ht = parmHashtable;
	}

	// basic array 1. web field id 2. string value, 3. array of choices
	// 'please select' option is off by default
	// used by dm like Security that build choice arrays on-the-fly
	public WebFieldSelect(String parmFieldId, String parmValue,
			String[][] parmChoices) {

		super(parmFieldId, parmValue);
		choicesArray = parmChoices;
		usingArray = true;
	}

	public WebFieldSelect(String parmFieldId, String parmValue,
			String[][] parmChoices, boolean parmPleaseSelect) {

		super(parmFieldId, parmValue);
		choicesArray = parmChoices;
		usingArray = true;
		pleaseSelect = parmPleaseSelect;

	}

	// 9/21/10 - new constructor to allow passing an selector array, plus the select text
	
	public WebFieldSelect(String parmFieldId, String parmValue,
			String[][] parmChoices, boolean parmPleaseSelect, String selectText) {

		super(parmFieldId, parmValue);
		choicesArray = parmChoices;
		usingArray = true;
		pleaseSelect = parmPleaseSelect;
		promptText = selectText;
		
		

	}
	
	// need a hash table containing the lookup table arg / description fields
	// the arg is either a String or Integer
	// the descriptor is always a String

	// Basic : 1 WebFieldId 2, String value 3. Hashtable
	public WebFieldSelect(String parmFieldId, String parmValue,
			Hashtable parmHashtable) {

		super(parmFieldId, parmValue);
		ht = parmHashtable;
	}

	// 1. WebFieldId 2. string value 3. ht 4. boolean pleaseSelect option
	public WebFieldSelect(String parmFieldId, String parmValue,
			Hashtable parmHashtable, boolean parmPleaseSelect) {
		super(parmFieldId, parmValue);
		ht = parmHashtable;
		// debug("WF SELECT.. SETTING CHOOLE TO " + parmPleaseSelect);

		pleaseSelect = parmPleaseSelect;

	}

	/*
	 * 
	 * 1. WebFieldId 2. string value 3. ht 4. prompt string
	 */
	public WebFieldSelect(String parmFieldId, String parmValue,
			Hashtable parmHashtable, String parmSelectPrompt) {
		super(parmFieldId, parmValue);
		ht = parmHashtable;
		pleaseSelect = true;
		promptText = parmSelectPrompt;
	}

	// 1 WebFieldId 2, Integer value 3. Hashtable, 4. boolean for please select

	public WebFieldSelect(String parmFieldId, Integer parmValue,
			Hashtable parmHashtable, boolean parmPleaseSelect) {

		super(parmFieldId, parmValue);
		ht = parmHashtable;
		pleaseSelect = parmPleaseSelect;
	}

	// 1. WebField Id. 2. Integer, 3. ht 4. Prompt text

	public WebFieldSelect(String parmFieldId, Integer parmValue,
			Hashtable parmHashtable, String parmPromptText) {

		super(parmFieldId, parmValue);
		ht = parmHashtable;
		pleaseSelect = true;
		promptText = parmPromptText;
	}

	public WebFieldSelect(String parmFieldId, Integer parmValue,
			String[][] parmChoices, boolean parmChoose) {

		super(parmFieldId, parmValue);
		choicesArray = parmChoices;
		usingArray = true;
		pleaseSelect = parmChoose;
	}

	// 1. Web Field id, 2. Integer value, 3. boolean PleaseSelect 4. AllowEdit
	// toggle
	// (false = show-only in edit mode)
	// 1 WebFieldId 2, Integer value 3. Hashtable 4. Boolean Please Select 5,
	// Allow Edit
	// using Integer as compare value, and allowing true to add 'please select'
	public WebFieldSelect(String parmFieldId, Integer parmValue,
			Hashtable parmHashtable, boolean parmPleaseSelect,
			boolean parmAllowEdit) {

		super(parmFieldId, parmValue);
		ht = parmHashtable;
		pleaseSelect = parmPleaseSelect;
		allowEdit = parmAllowEdit;
	}

	/*
	 * 11/1/10 New Integer Constructor with Array and AllowEdit choice
	 */
	// 1. Web Field id, 2. Integer value, 3. boolean PleaseSelect 4. AllowEdit
	// toggle
	// (false = show-only in edit mode)
	// 1 WebFieldId 2, Integer value 3. Hashtable 4. Boolean Please Select 5,
	// Allow Edit
	// using Integer as compare value, and allowing true to add 'please select'
	public WebFieldSelect(String parmFieldId, Integer parmValue,
			String[][] choices, boolean parmPleaseSelect,
			boolean parmAllowEdit) {

		super(parmFieldId, parmValue);
		usingArray = true;
		this.choicesArray = choices;
		pleaseSelect = parmPleaseSelect;
		allowEdit = parmAllowEdit;
	}
	
	// 1 WebFieldId 2, String value 3. Hashtable 4. Boolean Please Select 5,
	// boolean AllowEdit
	public WebFieldSelect(String parmFieldId, String parmValue,
			Hashtable parmHashtable, boolean parmPleaseSelect,
			boolean parmAllowEdit) {

		super(parmFieldId, parmValue);
		ht = parmHashtable;
		pleaseSelect = parmPleaseSelect;
		allowEdit = parmAllowEdit;
	}

	/*
	 * 11/1/10 String Constructor with Array and AllowEdit
	 */
	public WebFieldSelect(String parmFieldId, String parmValue,
			String[][] choices, boolean parmPleaseSelect,
			boolean parmAllowEdit) {

		super(parmFieldId, parmValue);
		usingArray = true;
		this.choicesArray = choices;
		pleaseSelect = parmPleaseSelect;
		allowEdit = parmAllowEdit;
	}
	
	/*
	 * 
	 * return a dislay value for the selected option
	 * 
	 * ... no html <select> retuned here!!! .. will use ht or string[] depending
	 * on how it was constructed.
	 */

	public String getHTML(String parmMode) {

		//debug("WebFieldSelect:getHTML. " + webFieldId +  " using array : " + usingArray);

		// find the right value
		if ((parmMode.equalsIgnoreCase("show"))
				|| (parmMode.equalsIgnoreCase("edit") && (allowEdit == false))) {

			if (usingArray)
				return getDisplayHTML_Array();
			else
				return getDisplayHTML_HT();
		}

		// put out HTML <select></select> and <options> array
		// .. add and edit are the same , except edit emits a 'SELECTED'
		// attribute
		// if the table argeuement column matches the field value
		else {
			// debug("WebFieldSelect:getJs - doing edit mode");
			if (usingArray) {
				return getSelectHTML_Array();
			} else

				return getHTML();

		}

	}

	// * find the value/desc pair in the array, and return, else return nothing.
	private String getDisplayHTML_Array() {
		for (int i = 0; i < choicesArray[0].length; i++) {
			if (choicesArray[0][i].equalsIgnoreCase((String) value)) {
				return choicesArray[1][i];
			}
		}
		return "";
	}

	/*
	 * This will never be called for a multi-select, they are only on the list
	 * page, for now!
	 */
	private String getDisplayHTML_HT() {

		// debug("getDisplayHTML ht ");

		// todo: put in exit once the value is found
		String valueType = value.getClass().getName();

		boolean firstTime = true;

		// the single source value we are trying to match
		boolean valueIsString = false;
		boolean valueIsNumber = false;

		// the set of choices we are looping through
		boolean selectorIsInteger = false;
		boolean selectorIsLong = false;

		String printValue = "";
		String objectStringValue = "";
		Integer objectIntegerValue;
		Integer hashIntegerValue;
		Object[] obj;

		// debug("getDisplayHTML ht ");

		// search the ht for the value
		Enumeration en = ht.keys();
		firstTime = true;
		while (en.hasMoreElements() && printValue.length() == 0) {
			// debug("getting obj");

			obj = (Object[]) ht.get(en.nextElement());
			if (obj != null) {

				if (firstTime) {

					firstTime = false;

					if (valueType.equalsIgnoreCase("java.lang.string")) {
						valueIsString = true;
						valueIsNumber = false;

					}
				}

				// debug("select type : " + valueType);

				if (valueIsString) {
					String hashValue = (String) obj[0];
					objectStringValue = (String) value;

					if (hashValue.equalsIgnoreCase(objectStringValue)) {
						// debug("they are equal");
						return printValue = (String) obj[1];

					}
				} else {

					// debug(" value class : " + value.getClass());

					objectIntegerValue = (Integer) value;
					
					// one has to be true if not first time
					if (!(selectorIsInteger || selectorIsLong)) {

						if (obj[0].getClass().getName().equalsIgnoreCase(
								"java.lang.Long")) {
							selectorIsLong = true;

						} else {
							selectorIsInteger = true;
						}

					}
					if (selectorIsLong) {
						Long l = (Long) obj[0];

						if (l.intValue() == objectIntegerValue.intValue()) {
							return printValue = (String) obj[1];
						}
					} else {
						if (objectIntegerValue.equals(obj[0])) {
							return printValue = (String) obj[1];
						}
					}
				}
			} else {
				debug("got null from the ht!!!!");
			}
		}

		return new String("");

	}

	/*
	 * arrays are only strings for now.. no need to worry about integers in
	 * arrays
	 */
	private String getSelectHTML_Array() {

		//debug("html select for : " + webFieldId);

		StringBuffer sb = new StringBuffer();

		sb.append("<SELECT " + multiple);

		if (displayClass.length() > 0) {
			sb.append(" CLASS=" + displayClass + " ");
		}

		if (submitOnClick == true) {
			sb.append(onChangeJavaScript);
		}

		sb.append(" ID=" + webFieldId + " NAME=" + webFieldId + "> ");

		if (pleaseSelect == true) {
			sb.append("<OPTION VALUE=0>");
			sb.append(promptText);
		}

		String selected = "";

		for (int i = 0; i < choicesArray[0].length; i++) {
			//debug(".looping, " + (String) value + "  array " + choicesArray[0][i] + " parm value  " + (String) value) ;

			
			// turn on selected if either the current item equals either the
			// initial value or the list value
			if (choicesArray[0][i].equalsIgnoreCase((String) value)) {
				selected = " SELECTED";
			} else {
				selected = "";
			}

			if (multiSelect) {
				for (int x = 0; x < selectedList.length; x++) {
					if (choicesArray[0][i].equalsIgnoreCase(selectedList[x])) {
						selected = " SELECTED";
					}
				}
			}

			sb.append("<option value=");
			sb.append(choicesArray[0][i]);
			sb.append(selected);
			sb.append(">");
			sb.append(choicesArray[1][i]);
		}

		sb.append("</SELECT>");
		return sb.toString();

	}

	public String getHTML() {

		//debug("gethtml for : " + webFieldId);

		StringBuffer sb = new StringBuffer();

		SortedSet set = Collections.synchronizedSortedSet(new TreeSet(ht
				.keySet()));

		Iterator it = set.iterator();

		String selected = "";

		sb.append("<SELECT " + multiple);

		/*
		 * 8/22 use public variable
		 */
		if (displayClass.length() > 0) {
			sb.append(" CLASS=" + displayClass + " ");
		}

		if (submitOnClick == true) {
			sb.append(onChangeJavaScript);
		}

		sb.append(" ID=" + webFieldId + " NAME=" + webFieldId + "> ");

		if ((pleaseSelect == true) || (pleaseSelect == true)) {
			sb.append("<OPTION VALUE=0>");
			sb.append(promptText);
		}

		Object[] obj;

		// this.//debug = true;

		while (it.hasNext()) {
			// LOGIC FOR AN INTEGER ARGUEMENT
			Object hashKey = (Object) it.next();
			obj = (Object[]) ht.get(hashKey);
			//debug("it.hasNext loop in WebFieldSelect");

			if (value.getClass().getName()
					.equalsIgnoreCase("java.lang.Integer")) {
				//debug("value is integer");
				//debug(" integer obj : " + obj[0].toString() + " .. match to : " + value);
				// to 
				// old compare of obj[0].equals(value)  doesn't seem to work on MySQL
				if (obj[0].toString().equalsIgnoreCase(value.toString())) {
					//debug("matched it!!!");
					selected = " SELECTED";
				} else {
					selected = "";
				}

				sb.append("<option value=");
				sb.append(obj[0].toString());
				sb.append(selected);
				sb.append(">");
				sb.append((String) obj[1]);
				// LOGIC FOR AN STRING ARGUEMENT
			} else {
				//debug("select value is : " + (String) value);
				//debug(" the match is --" + (String) obj[0]);
				
				if (obj[0].equals((String) value)) {
					selected = " SELECTED";
				} else {
					selected = "";
				}

				// debug(" the obj 0 : " + obj[0]);
				

				if (selectedList != null) {
					if (multiSelect && true) {
						// debug(" selected list len : " + selectedList.length);
						for (int z = 0; z < selectedList.length; z++) {
							if (obj[0].equals(selectedList[z])) {
								selected = " SELECTED";
							}
						}
					}
				}

				sb.append("<option value=");
				sb.append(obj[0]);
				sb.append(selected);
				sb.append(">");
				sb.append((String) obj[1]);
			}

		}
		sb.append("</SELECT>");
		// debug("done... returning select wf ok");
		// debug(".. the html.." + sb.toString());

		// debug(" html : " + sb.toString());
		return sb.toString();

	}

}
