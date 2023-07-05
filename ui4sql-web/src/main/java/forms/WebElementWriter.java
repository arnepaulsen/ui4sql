/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

/*
 * 
 * 
 * 	wraps the html element in java script
 * 
 *   .. this is temporary until we get rid of outerHTML
 * 
 * 
 * 
 */

public class WebElementWriter {

	public WebElementWriter() {
		super();
	}

	public String write(String element, String html) {
		return new String("\ndocument.all['" + element + "'].outerHTML = '"
				+ html + "';");

	}
}
