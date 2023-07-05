/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package remedy;


/*
 *
 *  Returns  the Remedy url and server strings depending on if PROD or QA
 * 
 */

// remedy user id  1B11Test
public class RemedyConnection {

	private String level = "QA";

	// post request header constants

	// QA Servers:

	private static String url_qa = "http://remedyweb.arnepaulsenjr:9085/arsys/services/ARService";

	private static String url_prod = "http://arnepaulsenjr.com:9086/arsys/services/ARService";

	private static String server_prod = "remedyprod.arnepaulsenjr.com";

	private static String server_qa = "remedyqa.arnepaulsenjr.com";

	
	public RemedyConnection(String level) {
		this.level = level;

	}

	public String GetRemedyURL() {
		if (level.equalsIgnoreCase("prod"))

			return url_prod + "?server=" + server_prod;
		else
			return url_qa + "?server=" + server_qa;
	}

	
	public String getDefaultUserid() {
		return "ui4sql";
	}
	
	public String getDefaultPassword() {
		return "password123";
	}

	
}
