/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
/*
 * file:       MpxjQuery.java
 * author:     Jon Iles
 * copyright:  (c) Packwood Software Limited 2002-2003
 * date:       13/02/2003
 */

/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package mpxj;

import batch.BatchSQL;

import java.sql.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectHeader;
import net.sf.mpxj.Relation;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.mpx.MPXReader;
import net.sf.mpxj.mspdi.MSPDIReader;

/**
 * Load an mpp project file into the sql database.
 * 
 * args:	connection properties
 * 			file to load/parse the file
 * 
 * 		then.. 
 * 			use loadTasks(project_id) to save to database
 * 
 *  	target table is tproject_task
 */
public class ProjectTaskLoad {
	/**
	 * Main method.
	 * 
	 * @param args
	 *            array of command line arguments
	 */

	private Connection connection;

	ProjectFile mpx = null;

	public static void main(String[] args) {
		try {
			if (args.length != 2) {
				System.out.println("arg count" + args.length);

				System.out
						.println("Usage: ProjectTaskLoad <connectionProperties file, ProjectPlan file_name>");
			} else {

				ProjectTaskLoad projectTaskLoad = new ProjectTaskLoad(args[0]);
				projectTaskLoad.loadFile(args[1]);
			}
		}

		catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
	}

	public ProjectTaskLoad(String propFileName) {
		super();

		BatchSQL batchSQL = new BatchSQL(propFileName);

		connection = batchSQL.getConnection();

	}

	public ProjectTaskLoad(Connection c) {
		super();

		connection = c;
	}

	/**
	 * This method performs a set of queries to retrieve information from the an
	 * MPP or an MPX file.
	 * 
	 * @param filename
	 *            name of the MPX file
	 * @throws Exception
	 *             on file read error
	 */
	public void loadFile(String filename) throws Exception {

		try {
			mpx = new MPXReader().read(filename);
		}

		catch (Exception ex) {
			mpx = null;
		}

		if (mpx == null) {
			try {
				mpx = new MPPReader().read(filename);
			}

			catch (Exception ex) {
				mpx = null;
			}
		}

		if (mpx == null) {
			try {
				mpx = new MSPDIReader().read(filename);
			}

			catch (Exception ex) {
				mpx = null;
			}
		}

		if (mpx == null) {
			throw new Exception("Failed to read file");
		}

	}

	public void runQueries(ProjectFile mpx) {

		listProjectHeader(mpx);

		listResources(mpx);

		listTasks(mpx);

		listAssignments(mpx);

		listAssignmentsByTask(mpx);

		listAssignmentsByResource(mpx);

		listHierarchy(mpx);

		listTaskNotes(mpx);

		listResourceNotes(mpx);

		listPredecessors(mpx);

		listSlack(mpx);

		listCalendars(mpx);
	}

	/**
	 * Reads basic summary details from the project header.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listProjectHeader(ProjectFile file) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		ProjectHeader header = file.getProjectHeader();
		Date startDate = header.getStartDate();
		Date finishDate = header.getFinishDate();
		String formattedStartDate = startDate == null ? "(none)" : df
				.format(startDate);
		String formattedFinishDate = finishDate == null ? "(none)" : df
				.format(finishDate);

		System.out.println("Project Header: StartDate=" + formattedStartDate
				+ " FinishDate=" + formattedFinishDate);
		System.out.println();
	}

	/**
	 * This method lists all resources defined in the file.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listResources(ProjectFile file) {
		for (Resource resource : file.getAllResources()) {
			System.out.println("Resource: " + resource.getName()
					+ " (Unique ID=" + resource.getUniqueID() + ")");
		}
		System.out.println();
	}

	/**
	 * This method lists all tasks defined in the file.
	 * 
	 * @param file
	 *            MPX file
	 */
	private void listTasks(ProjectFile file) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		String startDate;
		String finishDate;
		String duration;
		Date date;
		Duration dur;

		for (Task task : file.getAllTasks()) {
			date = task.getStart();
			if (date != null) {
				startDate = df.format(date);
			} else {
				startDate = "(no date supplied)";
			}

			date = task.getFinish();
			if (date != null) {
				finishDate = df.format(date);
			} else {
				finishDate = "(no date supplied)";
			}

			dur = task.getDuration();
			if (dur != null) {
				duration = dur.toString();
			} else {
				duration = "(no duration supplied)";
			}

			System.out.println("Task: " + task.getName() + " ID="
					+ task.getID() + " Unique ID=" + task.getUniqueID()
					+ " (Start Date=" + startDate + " Finish Date="
					+ finishDate + " Duration=" + duration + " Outline Level="
					+ task.getOutlineLevel() + " Outline Number="
					+ task.getOutlineNumber() + ")");

		}
		System.out.println();
	}

	public void uploadTasks (Integer projectPlanKey) {
		loadTasks (projectPlanKey, mpx);
		
	}
	
	private void loadTasks(Integer projectPlanKey, ProjectFile file) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		String startDate;
		String finishDate;
		
		Date date;
		Duration dur;

		String deleteOld = "DELETE FROM tproject_plan_task WHERE project_plan_id = "
				+ projectPlanKey.toString();

		runQuery(deleteOld);
		
		for (Task task : file.getAllTasks()) {
			date = task.getStart();
			if (date != null) {
				startDate = df.format(date);
			} else {
				startDate = "(no date supplied)";
			}

			date = task.getFinish();
			if (date != null) {
				finishDate = df.format(date);
			} else {
				finishDate = "(no date supplied)";
			}

			Number n = task.getPercentageComplete();

			StringBuffer qry = new StringBuffer();

			qry
					.append("INSERT INTO tproject_plan_task (project_plan_id, title_nm, start_date, end_date, outline_nm, level_no, owner_nm) ");

			qry.append(" VALUES (");
			qry.append(projectPlanKey.toString());
			qry.append(",'" + task.getName() + "'");
			qry.append(",'" + startDate + "'");
			qry.append(",'" + finishDate + "'");
			qry.append(",'" + task.getOutlineNumber() + "'");
			qry.append("," + task.getOutlineLevel());
			qry.append(",'" + getAssignmentsByTask(task) + "')");

			System.out.println("task: " + qry.toString());

			runQuery(qry.toString());

		}
		System.out.println();
	}

	// *********************************************
	// general run query
	// *********************************************
	public int runQuery(String parmQuery) {

		Statement stmt;

		try {
			stmt = connection.createStatement();
			// System.out.println(".. running query : " + parmQuery);
			stmt.executeUpdate(parmQuery);
			// System.out.println("DbInterface:runQuery is done ... query was :
			// "
			// + parmQuery);
		} catch (SQLException se) {

			int e = se.getErrorCode();

			if (e == 2627)
				return e;

			System.out.println("Insert error : " + se.toString());

			// return true;
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	public String getAssignmentsByTask(Task task) {

		StringBuffer sb = new StringBuffer();

		for (ResourceAssignment assignment : task.getResourceAssignments()) {
			Resource resource = assignment.getResource();

			if (resource != null) {
				sb.append(resource.getName());
			}

		}
		return sb.toString();
	}

	/**
	 * This method lists all tasks defined in the file in a hierarchical format,
	 * reflecting the parent-child relationships between them.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listHierarchy(ProjectFile file) {
		for (Task task : file.getChildTasks()) {
			System.out.println("Task: " + task.getName());
			listHierarchy(task, " ");
		}

		System.out.println();
	}

	/**
	 * Helper method called recursively to list child tasks.
	 * 
	 * @param task
	 *            task whose children are to be displayed
	 * @param indent
	 *            whitespace used to indent hierarchy levels
	 */
	private static void listHierarchy(Task task, String indent) {
		for (Task child : task.getChildTasks()) {
			System.out.println(indent + "Task: " + child.getName());
			listHierarchy(child, indent + " ");
		}
	}

	/**
	 * This method lists all resource assignments defined in the file.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listAssignments(ProjectFile file) {
		Task task;
		Resource resource;
		String taskName;
		String resourceName;

		for (ResourceAssignment assignment : file.getAllResourceAssignments()) {
			task = assignment.getTask();
			if (task == null) {
				taskName = "(null task)";
			} else {
				taskName = task.getName();
			}

			resource = assignment.getResource();
			if (resource == null) {
				resourceName = "(null resource)";
			} else {
				resourceName = resource.getName();
			}

			System.out.println("Assignment: Task=" + taskName + " Resource="
					+ resourceName);
		}

		System.out.println();
	}

	/**
	 * This method displays the resource assignments for each task. This time
	 * rather than just iterating through the list of all assignments in the
	 * file, we extract the assignments on a task-by-task basis.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listAssignmentsByTask(ProjectFile file) {
		for (Task task : file.getAllTasks()) {
			System.out.println("Assignments for task " + task.getName() + ":");

			for (ResourceAssignment assignment : task.getResourceAssignments()) {
				Resource resource = assignment.getResource();
				String resourceName;

				if (resource == null) {
					resourceName = "(null resource)";
				} else {
					resourceName = resource.getName();
				}

				System.out.println("   " + resourceName);
			}
		}

		System.out.println();
	}

	/**
	 * This method displays the resource assignments for each resource. This
	 * time rather than just iterating through the list of all assignments in
	 * the file, we extract the assignments on a resource-by-resource basis.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listAssignmentsByResource(ProjectFile file) {
		for (Resource resource : file.getAllResources()) {
			System.out.println("Assignments for resource " + resource.getName()
					+ ":");

			for (ResourceAssignment assignment : resource.getTaskAssignments()) {
				Task task = assignment.getTask();
				System.out.println("   " + task.getName());
			}
		}

		System.out.println();
	}

	/**
	 * This method lists any notes attached to tasks.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listTaskNotes(ProjectFile file) {
		for (Task task : file.getAllTasks()) {
			String notes = task.getNotes();

			if (notes != null && notes.length() != 0) {
				System.out
						.println("Notes for " + task.getName() + ": " + notes);
			}
		}

		System.out.println();
	}

	/**
	 * This method lists any notes attached to resources.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listResourceNotes(ProjectFile file) {
		for (Resource resource : file.getAllResources()) {
			String notes = resource.getNotes();

			if (notes != null && notes.length() != 0) {
				System.out.println("Notes for " + resource.getName() + ": "
						+ notes);
			}
		}

		System.out.println();
	}

	/**
	 * This method lists the predecessors for each task which has predecessors.
	 * 
	 * @param file
	 *            MPX file
	 */
	private static void listPredecessors(ProjectFile file) {
		for (Task task : file.getAllTasks()) {
			List<Relation> predecessors = task.getPredecessors();
			if (predecessors != null && predecessors.isEmpty() == false) {
				System.out.println(task.getName() + " predecessors:");
				for (Relation relation : predecessors) {
					System.out.println("   Task: "
							+ file
									.getTaskByUniqueID(
											relation.getTaskUniqueID())
									.getName());
					System.out.println("   Type: " + relation.getType());
					System.out.println("   Lag: " + relation.getDuration());
				}
			}
		}
	}

	/**
	 * List the slack values for each task.
	 * 
	 * @param file
	 *            ProjectFile instance
	 */
	private static void listSlack(ProjectFile file) {
		for (Task task : file.getAllTasks()) {
			System.out.println(task.getName() + " Total Slack="
					+ task.getTotalSlack() + " Start Slack="
					+ task.getStartSlack() + " Finish Slack="
					+ task.getFinishSlack());
		}
	}

	/**
	 * List details of all calendars in the file.
	 * 
	 * @param file
	 *            ProjectFile instance
	 */
	private static void listCalendars(ProjectFile file) {
		for (ProjectCalendar cal : file.getBaseCalendars()) {
			System.out.println(cal.toString());
		}

		for (ProjectCalendar cal : file.getResourceCalendars()) {
			System.out.println(cal.toString());
		}
	}
}
