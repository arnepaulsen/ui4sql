/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

import java.util.Date;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.starbase.starteam.Server;
import com.starbase.starteam.Project;
import com.starbase.starteam.Folder;
import com.starbase.starteam.Folder;
import com.starbase.starteam.File;
import com.starbase.starteam.FileResult;
import com.starbase.starteam.Keyword;
import com.starbase.starteam.View;
import com.starbase.starteam.AddResult;
import com.starbase.starteam.CheckinOptions;
import com.starbase.starteam.CheckoutOptions;
import com.starbase.starteam.CheckinManager;
import com.starbase.starteam.CheckoutManager;
import com.starbase.starteam.Status;

public class StarTeam {
	// Sample code showing how to connect to a
	// StarTeam Server using the Java interfaces.

	// String folder = "Bridges";
	// String sub_folder = "PMOToolDocs"; // or "DCReport"
	// target folder is main paramater

	String strHostUrl = "starteam.arnepaulsenjr.com";
	int nPort = 49201;
	String strUserName = "D576781";
	String strPassword = "password";

	private Server server = null;

	String strProjectName = "HC_N00000_AC-NC"; // from Properties file

	private String getHostUrl() {
		return strHostUrl;
	}

	private int getPort() {
		return nPort;
	}

	private String getUser() {
		return strUserName;
	}

	private String getPassword() {
		return strPassword;
	}

	private static void debug(String s) {
		if (true)
			System.out.println(s);
	}

	public void setProject(String project) {
		strProjectName = project;
	}

	public void setHostUrl(String server) {
		this.strHostUrl = server;
	}

	public void setPort(int port) {
		this.nPort = port;
	}

	public void setUser(String user) {
		this.strUserName = user;
	}

	public void setPassword(String password) {
		this.strPassword = password;
	}

	/*
	 * Constructors
	 */

	public StarTeam() {

	}

	public StarTeam(String properyFileName) {
		loadProperties(properyFileName);

		this.server = getServer();
	}

	public StarTeam(String host, int port, String userid, String password,
			String strProject) throws services.ServicesException {

		setProject(strProject);
		setHostUrl(host);
		setPort(port);
		setUser(userid);
		setPassword(password);

		this.server = getServer();
	}

	// 
	public void addFiles(String folderName, String subFolderName,
			String description, String fileList) {

		View view = getView();

		if (view == null) {
			debug("addFiles:getView() error ");
		}
		int count = 0;

		CheckinOptions options = new CheckinOptions(view);
		options.setForceCheckin(true);
		options.setUpdateStatus(true);

		CheckinManager cm = new CheckinManager(view, options);

		try {
			Folder folder = findFolder(view, folderName, subFolderName);

			String[] files = fileList.split(",");

			for (short x = 0; x < files.length; x++) {
				count++;
				addFile(cm, options, folder, description, files[x]);
			}

		} catch (services.ServicesException e) {
			debug("addFiles:findFolder() ");
		}
		// loop adding each file

	}

	public boolean getFile(StringBuffer sb, String folderName, String subFolderName,
			String fileName, String targetPath) {

		debug("StarTeeam:getFile starting : folder: " + folderName + " for file: " + fileName);

		View view = getView();

		com.starbase.starteam.Folder folder = null;

		try {
			folder = findFolder(view, folderName, subFolderName);
		} catch (Exception e) {
			debug("findFolder : " + e.toString());
			sb.append("StarTeam: getFile error : " + e.toString());
			return false;
		}

		String fileType = server.getTypeNames().FILE;

		com.starbase.starteam.Item[] itemlist = folder.getItems(fileType);

		File file = null;

		try {
			for (int x = 0; x < itemlist.length; x++) {
				file = (File) itemlist[x];
				String name = file.getFullName();
				debug("file name : " + name);
				if (name.endsWith(fileName)) {
					debug("this file found, checking it out  : " + name);

					java.io.File out_1 = new java.io.File(targetPath  + fileName);

					java.io.OutputStream os = new java.io.FileOutputStream(
							out_1);

					int lockStatus = 0;
					boolean eol = false;
					file.checkoutToStream(os, lockStatus, eol);
					return true;
				}
			}
		} catch (Exception e) {
			sb.append("StarTeam: getFile error : " + e.toString());
			debug("Exceptin checing out : " + e.toString());
			return false;

		}
		return false;

		// co.checkoutTo(arg0, out_1);

	}

	private void addFile(CheckinManager cm, CheckinOptions options,
			Folder folder, String description, String filePath) {

		java.io.FileInputStream stream = null;

		String comment = ""; // not used yet.
		java.io.File myFile;

		// myFile = new java.io.File(
		// "C:\\StarTeam\\Kphc_Ac_Nc\\Bridges\\Reports\\DCReport\\PRODGGM_DcReport_06260803PM.xls");
		// "T:\\Bridges\\DcReport\\PRODGGM_DcReport_06260803AM.xls");

		myFile = new java.io.File(filePath);

		try {
			stream = new java.io.FileInputStream(myFile);

		} catch (Exception e) {
			debug("Error creating file input stream : " + e.toString());
		}

		com.starbase.starteam.File file = new File(folder);

		try {

			int lockStatus = 0;

			file.addFromStream(stream, myFile.getName(), description, comment,
					com.starbase.starteam.Status.CURRENT, true);

			// Keyword[] keywords = result.getKeywords();
			// for (int k = 0; k < keywords.length; k++) {
			// debug("result keyword " + keywords[0].toXMLString());
			// }

			// cm.checkin(file);
			file.checkinFrom(myFile, "", lockStatus, true, true, true);
			// cm.checkin(folder);

			// file.checkin("because", lockStatus, true, true, true);
		} catch (Exception e) {
			debug("StarTeam Add Exception " + e.toString());
		}

	}

	// this routine assumes the folder is 3-levels down.
	// go-live will probably be at 2nd level

	private Folder findFolder(View view, String folderName, String subFolderName)
			throws services.ServicesException {

		Folder[] root_folders = view.getRootFolder().getSubFolders();

		for (int f = 0; f < root_folders.length; f++) {
			if (root_folders[f].getName().equalsIgnoreCase(folderName)) {
				Folder folder = root_folders[f];
				Folder[] sub_folders = folder.getSubFolders();

				for (int f2 = 0; f2 < sub_folders.length; f2++) {
					if (sub_folders[f2].getName().equalsIgnoreCase(
							subFolderName)) {
						return sub_folders[f2];
					}
				}
			}
		}
		throw new services.ServicesException(
				"StarTeam:getFolder - folder not found");

	}

	/*
	 * get the Star Team server object using host, port, user, password
	 */

	private Server getServer() {

		// Create a new StarTeam Server object.

		Server server = null;

		server = new Server(strHostUrl, nPort);

		System.out.println(".. connecting.");

		server.connect();

		boolean isConnected = server.isConnected();

		server.logOn(strUserName, strPassword);

		System.out.println(". is connected ? " + isConnected);

		return server;
	}

	private View getView() {

		debug("getView startrig");

		View view = null;
		Project[] projects = server.getProjects();

		if (projects == null) {
			debug("no projects!");
			System.exit(99);
		}
		debug(" projects # " + projects.length);

		for (int i = 0; i < projects.length; i++) {
			debug(" project name : " + projects[i].getName());

			if (projects[i].getName().equalsIgnoreCase(strProjectName)) {
				View[] views = projects[i].getViews();
				view = views[0];
				debug(" Good work.. project : " + projects[i].getName()
						+ " view :   " + view.getName() + " found.");
				return view;
			}

		}
		debug("Project not found");

		return view;
	}

	private void disconnect() {
		server.disconnect();
	}

	public void loadProperties(String propFileName) {

		Properties p = getProperties(propFileName);

		setHostUrl(p.getProperty("HOST"));

		setUser(p.getProperty("USERID"));

		setPassword(p.getProperty("PASSWORD"));

		setProject(p.getProperty("PROJECT"));

		// port
		try {
			setPort(Integer.parseInt((String) p.getProperty("PORT")));
			System.out.println(" StarTeam port: " + nPort);
		} catch (NumberFormatException e) {
			System.out.println("Error formatting port number");
			e.printStackTrace();
		}

		// debug("host: " + strHostUrl);
		// debug("user: " + strUserName);
		// debug("password: " + strPassword);
		// debug("project: " + strProjectName);
		// debug("port: " + nPort);

	}

	private Properties getProperties(String propFileName) {

		Properties p = new Properties();

		System.out.println("Properties filename: " + propFileName);
		try {
			// FileInputStream is = new FileInputStream(propFileName);
			System.out.print("...reading properties...");

			FileInputStream is = new FileInputStream(propFileName);

			p.load(is);

		} catch (IOException e) {
			System.out.println("error loading properties file.");
			System.out.println(e.toString());
			System.exit(1);
		}

		return p;
	}

	public static void main(String[] args) {

		// Arguement list :
		// 1. properties file name path
		// 2. Star Team target folder
		// 3. Star Team sub-filder
		// 4. Comment to add to file
		// 5. String of comma-delimited files
		//  

		System.out.println("Star Team CheckinManager starting "
				+ new Date().toString());
		System.out.println(" Properties file : " + args[0]);

		System.out.println(" folder : " + args[1]);
		System.out.println(" subfolder : " + args[2]);

		System.out.println(" commen4t : " + args[3]);
		System.out.println(" file list : " + args[4]);

		StarTeam st = new StarTeam(args[0]); // construct with properties
		// file

		if (args.length < 5) {
			System.out
					.println("Usage: StarTeam propertyFile, targetFolder, description/comment, commaSeparatedFileList");
			System.exit(99);
		}

		System.out.println(" Folder " + args[1] + " subfolder : " + args[2]);

		// st.addFiles(args[1], args[2], args[3], args[4]);

		StringBuffer sb = new StringBuffer();
		
		st.getFile(sb, args[1], args[2], "SBAR_1.XLS", "c:/temp/");

		// folder, (0)
		// subfolder (1)
		// comment, (2)
		// fileList (3)

		st.disconnect();

		System.out.println("Star Team CheckinManager ending "
				+ new Date().toString());

	}

}
