/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package plugins;

import java.sql.ResultSet;

import router.SessionMgr;
import services.ExcelWriter;
import org.apache.poi.hssf.util.HSSFColor;

/*******************************************************************************
 * Change Approval Board - Review
 * 
 * Keywords - sql server only
 * 
 * Change Log:
 * 
 * 5/10/08 Wow... a 4th genaration subclass
 * 
 * done so we ALWAYS know where we are NCF Cab vs RIP Cab.. ... FilterMode parm
 * is not realiable enough
 * 
 * 
 */

public class RfcNcfCabPlugin extends RfcPlugin {

	/***************************************************************************
	 * 
	 * List and database overrides
	 * 
	 **************************************************************************/

	public RfcNcfCabPlugin() throws services.ServicesException {
		super();
		this.ncf_cab = true;
	}

	

	

}
