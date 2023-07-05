//Top Nav bar script v2.1- http://www.dynamicdrive.com/dynamicindex1/sm/index.htm

function showToolbar()
{
// AddItem(id, text, hint, location, alternativeLocation);
// AddSubItem(idParent, text, hint, location, linktarget);

	menu = new Menu();
	menu.addItem("gov", "Governance", "Governance",  null, null);
	menu.addItem("intake", "Intake", "Intake",  null, null);
	menu.addItem("analysis", "Analysis", "Analysis",  null, null);
	menu.addItem("define", "Definition", "Definition",  null, null);
	menu.addItem("planning", "Planning", "Planning",  null, null);
	menu.addItem("tracking", "Tracking", "Tracking",  null, null);
	menu.addItem("business", "Business", "Business",  null, null);
	menu.addItem("system", "System", "Application Components",  null, null);
	menu.addItem("testing", "Testing", "Testing",  null, null);
	menu.addItem("review", "Review", "Review",  null, null);
	menu.addItem("events", "Live", "Live",  null, null);
		
	menu.addSubItem("gov", "Customers", "Customers",  "Router?Target=Customer&Action=List", "");
	menu.addSubItem("gov", "Committments", "Committments",  "Router?Target=Commitment&Action=List", "");
	menu.addSubItem("gov", "Contacts", "Contacts",  "Router?Target=HCContact&Action=List", "");
	menu.addSubItem("gov", "Goals", "Goals",  "Router?Target=Goal&Action=List", "");
	menu.addSubItem("gov", "Policies", "Policies",  "Router?Target=Policy&Action=List", "");
	menu.addSubItem("gov", "Processes", "Processes",  "Router?Target=Process&Action=List", "");
	menu.addSubItem("gov", "Products", "Products",  "Router?Target=Product&Action=List", "");
	menu.addSubItem("gov", "Standards", "WadDaYaThink",  "Router?Target=Standard&Action=List", "");
	menu.addSubItem("gov", "Waivers", "Waivers",  "Router?Target=Waiver&Action=List", "");
			

	menu.addSubItem("intake", "Business Case", "Business Qualifying Form",  "Router?Target=BsnsQualForm&Action=List", "");
	menu.addSubItem("intake", "Change Request", "Change Request",  "Router?Target=EpiccrInit&Action=List", "");
	menu.addSubItem("intake", "Triage", "SBAR",  "Router?Target=SBAR&Action=List", "");
	menu.addSubItem("intake", "Scope Change", "Scope Change",  "Router?Target=Scope&Action=List", "");
	menu.addSubItem("intake", "Service Request", "SR",  "Router?Target=ServiceRequest&Action=List", "");
	menu.addSubItem("intake", "Work Effort", "Work Effort",  "Router?Target=Work&Action=List", "");
	menu.addSubItem("intake", "Vendor CR", "Vendor Change Request",  "Router?Target=Epiccr&Action=List", "");
	
	menu.addSubItem("analysis", "Criteria", "Acceptance Criteria",  "Router?Target=Criteria&Action=List", "");
	menu.addSubItem("analysis", "Business Plan", "Business Plan",  "Router?Target=BusinessPlan&Action=List", "");
	menu.addSubItem("analysis", "Customer Voice", "VOC",  "Router?Target=VOC&Action=List", "");
	menu.addSubItem("analysis", "Deliverables", "List Deliverables",  "Router?Target=Deliverable&Action=List", "");
	menu.addSubItem("analysis", "Level of Effort", "Work Sizing",  "Router?Target=Sizing&Action=List", "");
	menu.addSubItem("analysis", "Objectives", "Expected Outcomes",  "Router?Target=Objective&Action=List", "");
	menu.addSubItem("analysis", "Projects", "Projects",  "Router?Target=Project&Action=List", "");
	menu.addSubItem("analysis", "Responsibility", "Responsibility Matrix",  "Router?Target=Responsibility&Action=List", "");

	menu.addSubItem("define", "Action Items", "Action Items",  "Router?Target=Action&Action=List", "");
	menu.addSubItem("define", "Assumptions", "Define Assumptions",  "Router?Target=Assumption&Action=List", "");
	menu.addSubItem("define", "Constraints", "Define Constraints",  "Router?Target=Constraint&Action=List", "");
	menu.addSubItem("define", "Objectives", "Objectives","Router?Target=Objective&Action=List", "");
	menu.addSubItem("define", "Impacts", "Define Impacts",  "Router?Target=Impact&Action=List", "");
	menu.addSubItem("define", "Issues", "Define Issues",  "Router?Target=Issue&Action=List", "");
	menu.addSubItem("define", "Milestones", "Define Milestones",  "Router?Target=Milestone&Action=List", "");
	menu.addSubItem("define", "Next Steps", "Define Next Steps",  "Router?Target=Task&Action=List", "");
	menu.addSubItem("define", "Requirements", "Requirements",  "Router?Target=Requirement&Action=List", "");
	menu.addSubItem("define", "People", "People Resources",  "Router?Target=ProjectUser&Action=List", "");	
	menu.addSubItem("define", "Risks", "Define Risks",  "Router?Target=Risk&Action=List", "");
	menu.addSubItem("define", "Service Levels", "Define Servce Levels",  "Router?Target=ServiceLevel&Action=List", "");
	
	menu.addSubItem("planning", "Communication Plan", "Plan of Type Communication",  "Router?Target=Plan&Action=List&Kind=Communication", "");
	menu.addSubItem("planning", "Risk Plan", "Plan to Manage Risks",  "Router?Target=Plan&Action=List&Kind=Risk", "");
	menu.addSubItem("planning", "Training Plan", "Plan of Type Training",  "Router?Target=Plan&Action=List&Kind=Training", "");
	menu.addSubItem("planning", "Education Plan", "Plan of Type Education",  "Router?Target=Plan&Action=List&Kind=Education", "");
	menu.addSubItem("planning", "Disaster Plan", "Disaster Plan",  "Router?Target=DisasterPlan&Action=List", "");

	menu.addSubItem("tracking", "Ad-Hoc", "One-Off Request",  "Router?Target=Adhoc&Action=List", "");
	menu.addSubItem("tracking", "Build", "Environment Builds",  "Router?Target=Build&Action=List", "");
	menu.addSubItem("tracking", "Releases", "Releases",  "Router?Target=Release&Action=List", "");
	menu.addSubItem("tracking", "Release Notes", "Release Notes",  "Router?Target=ReleaseNote&Action=List", "");
	menu.addSubItem("tracking", "Single Work Queue", "","Router?Target=SWQ&Action=List", "");
	
	menu.addSubItem("business", "Algorithms", "Algorithms","Router?Target=Algorithm&Action=List", "");
	menu.addSubItem("business", "Alternatives", "Alternatives","Router?Target=Alternative&Action=List", "");
	menu.addSubItem("business", "Business Rules", "Business Rules","Router?Target=Rule&Action=List", "");
	menu.addSubItem("business", "Exposures", "Exposures","Router?Target=Exposure&Action=List", "");
	menu.addSubItem("business", "Functions", "Work Functions","Router?Target=Function&Action=List", "");
	menu.addSubItem("business", "Gaps", "Functional Gaps","Router?Target=Gap&Action=List", "");
	menu.addSubItem("business", "Observations", "Observations","Router?Target=Obversation&Action=List", "");
	menu.addSubItem("business", "Terms", "Definitions","Router?Target=Term&Action=List", "");
	menu.addSubItem("business", "Use Cases", "Use Cases","Router?Target=UseCase&Action=List", "");
	menu.addSubItem("business", "Work Flows", "Work Flows","Router?Target=Workflow&Action=List", "");
	
	menu.addSubItem("system", "Applications", "Applications","Router?Target=Application&Action=List", "");
	menu.addSubItem("system", "Architecture", "Architecture","Router?Target=Architecture&Action=List", "");
	menu.addSubItem("system", "User Interface", "User Interface","Router?Target=Gui&Action=List", "");
	menu.addSubItem("system", "Databases", "Databases","Router?Target=Database&Action=List", "");	
	menu.addSubItem("system", "Messages", "HL7 Messages","Router?Target=Message&Action=List", "");
	menu.addSubItem("system", "Files", "Files","Router?Target=File&Action=List", "");
	menu.addSubItem("system", "Reports", "Reports","Router?Target=Report&Action=List", "");
	
	menu.addSubItem("testing", "Test Cases", "Test Cases ","Router?Target=Testcase&Action=List", "");
	menu.addSubItem("testing", "Test Cycles", "Test Cycles","Router?Target=Cycle&Action=List", "");
	menu.addSubItem("testing", "Defects", "Defects","Router?Target=Defect&Action=List", "");
	menu.addSubItem("testing", "Environments", "Environments","Router?Target=Environment&Action=List", "");
	menu.addSubItem("testing", "Patients", "Patients",  "Router?Target=Patient&Action=List", "");
	menu.addSubItem("testing", "Express Test", "Express Test Request","Router?Target=X&Action=List", "");
	menu.addSubItem("testing", "Request", "Test Request","Router?Target=Test&Action=List", "");
	menu.addSubItem("testing", "Test Plan", "Test Plan","Router?Target=TestPlan&Action=List", "");
	
	menu.addSubItem("review", "Closure Report", "Closure Report","Router?Target=Closure&Action=List", "");
	menu.addSubItem("review", "Phase Review", "Phase Reviews","Router?Target=Review&Action=List", "");
	menu.addSubItem("review", "Lessons Learned", "Lessons Learned","Router?Target=Lesson&Action=List", "");
	menu.addSubItem("review", "Security Checklist", "Security Checklist","Router?Target=SecurityCklist&Action=List", "");
	menu.addSubItem("review", "Sign-Offs", "Approvals","Router?Target=SignOff&Action=List", "");
	menu.addSubItem("review", "Service Requests", "Service Requests","Router?Target=ServiceRequest&Action=List&FilterMode=Review", "");

	menu.addSubItem("events", "Alerts", "Alerts","Router?Target=Alert&Action=List", "");
	menu.addSubItem("events", "Event", "Live Event",  "Router?Target=Event&Action=List", "");
	menu.addSubItem("events", "Outages", "System Outage",  "Router?Target=Outage&Action=List", "");
	menu.addSubItem("events", "Procedures", "Procedures",  "Router?Target=Procedure&Action=List", "");
	menu.addSubItem("events", "Errors", "Message Errors",  "Router?Target=Ehs&Action=List", "");

	
	menu.showMenu();
}