//Top Nav bar script v2.1- http://www.dynamicdrive.com/dynamicindex1/sm/index.htm

function showToolbar()
{
// AddItem(id, text, hint, location, alternativeLocation);
// AddSubItem(idParent, text, hint, location, linktarget);

	menu = new Menu();
	menu.addItem("system", "System", "System",  null, null);
	menu.addItem("users", "Users and Roles", "Users and Roles",  null, null);
	menu.addItem("org", "Organizations", "Organizations",  null, null);
	menu.addItem("forms", "Forms and Codes", "Forms and Codes",  null, null);
	menu.addItem("utilities", "Utilities", "Utilities",  null, null);

		
	menu.addSubItem("system", "Options", "Options",  "Router?Target=Options&Action=Show&Relation=this&RowKey=1", "");

	menu.addSubItem("users", "Users", "Users",  "Router?Target=User&Action=list", "");
	menu.addSubItem("users", "Contacts", "Contacts",  "Router?Target=Contact&Action=list", "");
	menu.addSubItem("users", "Roles", "Roles",  "Router?Target=ApplicationUser&Action=list", "");
	
	menu.addSubItem("org", "Programs", "Programs",  "Router?Target=Division&Action=list", "");
	menu.addSubItem("org", "Departments", "Departments",  "Router?Target=Department&Action=list", "");
	
	menu.addSubItem("forms", "Forms", "System Forms",  "Router?Target=Form&Action=list", "");
	menu.addSubItem("forms", "Codes", "System Internal Codes",  "Router?Target=CodeType&Action=list", "");
	
	menu.addSubItem("utilities", "Logs", "User Access Logs",  "Router?Target=Log&Action=list", "");

	
	menu.showMenu();
}