/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.util.Hashtable;

import db.DbFieldInteger;
import forms.*;
import router.SessionMgr;
import services.ServicesException;

/**
 * 
 * Order Set / SR Linkage
 * 
 * Used to maintain the many-to-many relationship between Order Sets and Service
 * Requests
 * 
 * Change log:
 * 
 * 
 */

public class OrderSetLinkPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	public OrderSetLinkPlugin() throws services.ServicesException {
		super();
		this.setTableName("korder_set_link");
		this.setKeyName("link_id");
		this.setTargetTitle("Order Set / SR Assocations");
		this.setShowAuditSubmitApprove(false);

		this.setIsDetailForm (true);
		this.setIsStepChild (true);
		this.setParentTarget("IPTrackerOS");
		
		this.setCopyOk(false);
		this.setNextOk(false);

		this.setListHeaders(new String[] { "Order Set", "Version",
				"Service Request", "Change Title" });

		this.setMoreListJoins (new String[] {
				" left join korder_set orderset on korder_set_link.order_set_id = orderset.order_set_id ",
				" left join tip_tracker tracker on korder_set_link.request_id = tracker.tracker_id",
				" join tdivision on tracker.division_id = tdivision.division_id " });

		this.setMoreListColumns (new String[] { "orderset.title_nm as set_title",
				"version_tx", "tracker.sr_no as srno",
				"tracker.title_nm as chgtitle" });
		
		
		this.setMoreSelectColumns ( new String[] { "tracker.sr_no",
				"tracker.title_nm requestTitle", "orderset.title_nm setTitle",
				"tdivision.division_id",
				this.dbprefix + "isNull_char(orderset.order_set_id,0) as linkid" });

		

		this.setMoreSelectJoins (this.moreListJoins);

	
	}


	
	public String getListTitle() {
		return this.getTargetTitle()  + " -  " + sm.getServiceRequestName();
	}

	

	

	/*
	 * 
	 * List Control
	 * 
	 * @see plugins.Plugin#listColumnHasSelector(int)
	 */
	public boolean listColumnHasSelector(int columnNumber) {

		return false;

	}

	public String getListAnd() {
		/*
		 * watch out for "o" open values vs. zero (0) for 'all' value
		 */

		StringBuffer sb = new StringBuffer();

		sb.append(" AND korder_set_link.request_id = "
				+ sm.getServiceRequestId().toString());

		return sb.toString();
	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("request_id", new DbFieldInteger("request_id", new Integer(sm
				.getServiceRequestId())));

		return true;
	}

	/*
	 * 
	 * Filter Control
	 */
	// called on the Show page, when returning to the list page
	public Integer getParentKey() {
		return db.getInteger("link_id");
	}

	/***************************************************************************
	 * 
	 * HTML Field Mapping
	 * 
	 **************************************************************************/

	public Hashtable getWebFields(String parmMode)
			throws services.ServicesException {

		boolean addMode = parmMode.equalsIgnoreCase("add") ? true : false;

		Hashtable<String, WebField> ht = new Hashtable<String, WebField>();

		if (addMode) {
			getAddFields(ht);
			return ht;
		}

		getUpdateFields(ht, parmMode);

		return ht;

	}

	private void getAddFields(Hashtable<String, WebField> ht) {

		String query = "select title_nm, order_set_id, title_nm from korder_set ORDER BY title_nm";

		Hashtable orders = new Hashtable();
		try {
			orders = db.getLookupTable(query);
		} catch (ServicesException sql) {
			debug(sql.toString());
		}

		ht.put("order_set_id", new WebFieldSelect("order_set_id", new Integer(
				"0"), orders));
		ht.put("link", new WebFieldDisplay("link", ""));

		ht.put("msg", new WebFieldDisplay("msg",
				"Please  select Order Set. Then Save-Edit."));

		ht.put("version_tx", new WebFieldString("version_tx", "", 3, 3));

		ht.put("srtitle", new WebFieldDisplay("srtitle", sm
				.getServiceRequestName()));

		ht.put("notes_tx", new WebFieldText("notes_tx", "", 5, 100));

		return;

	}

	private void getUpdateFields(Hashtable<String, WebField> ht, String parmMode) {

		/*
		 * Display-only fields from tRa
		 */

		ht.put("srno", new WebFieldDisplay("srno", db.getText("sr_no")));

		ht.put("srtitle", new WebFieldDisplay("srtitle", db
				.getText("requestTitle")));

		ht.put("order_set_id", new WebFieldDisplay("order_set_id", db
				.getText("setTitle")));

		ht.put("version_tx", new WebFieldString("version_tx", db
				.getText("version_tx"), 8, 8));

		String link = new String("");

		if (parmMode.equalsIgnoreCase("show")) {
			if (db.getText("linkid").equalsIgnoreCase("0")) {
				link = "";
			} else {
				link = "<A href=Router?Target=OrderSet&Action=Show&Relation=this&RowKey="
						+ db.getText("linkid") + ">Jump to Order Set</A>";
			}
		}

		ht.put("link", new WebFieldDisplay("link", link));

		/*
		 * Blobs
		 */

		ht.put("notes_tx", new WebFieldText("notes_tx", db.getText("notes_tx"),
				5, 100));

		return;

	}

}
