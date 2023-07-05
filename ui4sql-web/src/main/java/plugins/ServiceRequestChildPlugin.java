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
 * 11/15/10 Paulsen - add logic for Add/Edit buttons.  Same as ServiceRequest.plugin
 * 
 */

public class ServiceRequestChildPlugin extends AbsDivisionPlugin {

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	boolean ac_ok ;
	boolean bp_ok ;
	
	public ServiceRequestChildPlugin() throws services.ServicesException {
		super();
		this.setTableName("tsr_child");
		this.setKeyName("sr_child_id");
		this.setTargetTitle("Service Request");
		this.setShowAuditSubmitApprove(false);

		this.setIsDetailForm(true);
		this.setIsStepChild(true);
		this.setParentTarget("ServiceRequest");
		this.setSelectViewName("vsr_child");
		this.setCopyOk(false);
		this.setEditOk(true);
		this.setDeleteOk(true);

		this.setListViewName("vsr_child_list");
		this.setListHeaders(new String[] { "BT", "Link To", "RFC#","BT Status","AC", "Rtn Maint", "CAB Date", "CAB Stat", "NCF Date", "NCF Status", "Remedy Begin", "Remedy End", "Summary"});

	}

	public void init(SessionMgr parmSm) {
		super.init(parmSm);

	
		// filterTeam.setPleaseSelect(false);

		bp_ok = sm.getSR_Tracker_Level().equalsIgnoreCase("BP")
				|| sm.getSR_Tracker_Level().equalsIgnoreCase("ED")
				|| sm.userIsLeader() || sm.userIsAdministrator();

		ac_ok = sm.getSR_Tracker_Level().equalsIgnoreCase("AC")
				|| sm.getSR_Tracker_Level().equalsIgnoreCase("ED")
				|| sm.userIsLeader() || sm.userIsAdministrator();

		if (!ac_ok) {
			this.setAddOk(false);
			this.setCopyOk(false);
		}

		// don't allow any edit if not an AC or BP or Leader or Administrator
		if ((!ac_ok) && (!bp_ok)) {
			this.setEditOk(false);
			this.setCopyOk(false);
		}

		if ((!sm.userIsLeader()) && (!sm.userIsAdministrator())) {
			this.setDeleteOk(false);
		}


	}
	
	public String getListTitle() {
		return this.getTargetTitle() + " - " + sm.getParentName();
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

		sb.append(" AND parent_sr_id = " + sm.getParentId().toString());

		return sb.toString();
	}

	public boolean beforeAdd(Hashtable ht) {

		ht.put("parent_sr_id", new DbFieldInteger("parent_sr_id", new Integer(
				sm.getParentId())));

		return true;
	}

	/*
	 * 
	 * Filter Control
	 */
	// called on the Show page, when returning to the list page
	public Integer getParentKey() {
		return db.getInteger("parent_sr_id");
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

		debug("1");
		ht.put("sr_no", new WebFieldDisplay("sr_no", addMode ? "" : db
				.getInteger("sr_no").toString()));

		ht.put("msg", new WebFieldDisplay("msg",
				addMode ? "Please select product and BT #, then Save-Edit."
						: ""));
		debug("3");
		ht.put("build_no", new WebFieldString("build_no", addMode ? "" : db
				.getText("build_no"), 6, 6));

		debug("4");
		
		ht.put("driver_cd", new WebFieldDisplay("driver_cd", addMode ? "" : db
				.getText("driver_cd")));
		ht.put("driver_no", new WebFieldDisplay("driver_no", addMode ? "" : db
				.getText("driver_no")));

		
		
		
		if (addMode) {
			
			ht.put("bt_link", new WebFieldDisplay("bt_link",  ""));
			
			ht.put("product_cd", new WebFieldSelect("product_cd", "", sm
					.getCodes("PRODUCTS"), true));
		} else {
			
			
			ht.put("product_cd", new WebFieldDisplay("product_cd", sm
					.getCodeDesc(sm.getCodes("PRODUCTS"), db
							.getText("product_cd"))));
			
			
			String buildLink = "";
			String buildId = db.getInteger("build_id").toString();

		
			if (buildId.equalsIgnoreCase("0")) {
				buildLink = "(Build Tracker entry not found.)";

			} else {
				buildLink = "<A href=Router?Target=Build&Action=Show&Relation=this&RowKey="
						+ buildId + ">Build Tracker</A>";
			}

			ht.put("bt_link", new WebFieldDisplay("bt_link", buildLink));
			

		}

		return ht;

	}

}
