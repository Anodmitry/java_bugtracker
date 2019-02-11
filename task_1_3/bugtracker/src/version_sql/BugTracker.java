package version_sql;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import entities.*;

import javax.xml.transform.TransformerException;

import tools.SQLTools;
import tools.XMLTools;

public class BugTracker {
	
	private String cur_user;
	private String cur_project;
	
	private boolean issue_was_added;
	
	public BugTracker() throws ClassNotFoundException, SQLException {
		this.cur_user = "";
		this.cur_project = "";
		this.issue_was_added = false;
	}
		
	private void printIssues() throws SQLException {
		int i = 1;
		HashSet<Issue> all_issues = SQLTools.getIssues(new HashSet<Project>(), new HashSet<User>());
		for(Issue iss : all_issues) {
			System.out.println(String.valueOf(i) + ". " + iss.toString());
			i++;
		}
	}
	
	private void printProjects(HashSet<Project> all_projects) throws SQLException {
		int i = 1;
		for(Project p : all_projects) {
			System.out.println(String.valueOf(i) + ". " + p.toString());
			i++;
		}
	}
	
	private void printUsers(HashSet<User> all_users) throws SQLException {
		int i = 1;
		for(User u : all_users) {
			System.out.println(String.valueOf(i) + ". " + u.toString());
			i++;
		}
	}
	
	public void XMLToSQLite() {
		try {
			SQLTools.sendIssues(XMLTools.getFromXML(new HashSet<Project>(), new HashSet<User>()));//загрузка данных из XML в SQLite
			System.err.println("\n\nINFO: ALL THE DATA LOADED SUCCESSFULLY");
		}
		catch(Throwable e) {
			System.err.println("\n\nERROR: DATA LOADING ERROR!");
			return;
		}
	}
	
	private HashSet<Issue> getSpecifiedIssues(Project p, User u) throws SQLException{
		return SQLTools.getSpecifiedIssues(p, u);
	}
	
	private void genReport() throws IOException, SQLException {
		System.out.println("\n\n---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tGENERATING REPORT FOR ISSUES");
		System.out.println("\nList of projects:");
		HashSet<Project> all_projects = SQLTools.getProjects();
		printProjects(all_projects);
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE: ");
		Scanner in = new Scanner(System.in);
		int choice = in.nextInt();
		if(choice > all_projects.size() || choice < 1) {
			System.err.println("\n\nERROR: YOU ENTERED NUMBER NOT FROM THE LIST ABOVE!");
			return;
		}
		int i = 1;
		Project proj = null;
		for(Project p : all_projects) {
			if(i == choice) proj = p; 
			i++;
		}
		
		System.out.println("\nList of users:");
		HashSet<User> all_users = SQLTools.getUsers();
		printUsers(all_users);
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE: ");
		choice = in.nextInt();
		if(choice > all_users.size() || choice < 1) {
			System.err.println("\n\nERROR: YOU ENTERED NUMBER NOT FROM THE LIST ABOVE!");
			return;
		}
		i = 1;
		User us = null;
		for(User u : all_users) {
			if(i == choice) us = u; 
			i++;
		}
		
		HashSet<Issue> iss_got = getSpecifiedIssues(proj, us);
		if(iss_got.isEmpty()) {System.err.println("\n\nINFO: <" + us.toString() + "> IN <" + proj.toString() + "> HASN'T FOUND ANY ISSUE YET"); return;}
		System.out.println("\n\n---------------------------------------------------------------------------------");		
		String report = "List of issues found by <" + us.toString() + "> in <" + proj.toString() + ">\n";
		i = 1;
		for(Issue is : iss_got) {
			report += String.valueOf(i) + "." + is.toString() + "\n";
			i++;
		}
		System.out.println(report);
		try(FileWriter writer = new FileWriter("report.txt", false))
        {
            writer.write(report);
            writer.flush();
        }
        catch(IOException ex){             
            throw ex;
        }
		System.err.println("\nINFO: THE REPORT ALSO SAVED TO FILESYSTEM IN THE ROOT OF THIS JAVA PROJECT. NAME OF FILE IS " + "report.txt");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("Push ENTER to continue...");
		System.in.read();
	}
	
	private void menuShowInfo() throws IOException, SQLException {
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tSHOWING DATA MENU");
		Scanner in = new Scanner(System.in);
		System.out.println(
				"\n\n0.RETURN TO THE MAIN MENU\n"
				+ "1.SHOW ALL PROJECTS\n"
				+ "2.SHOW ALL USERS\n"
				+ "3.SHOW ALL ISSUES\n"
				+ "4.GENERATE REPORT WITH ALL ISSUES FROM SPECIFIED PROJECTS FOUND BY SPECIFIED USER"
				);
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("ENTER THE NUMBER OF APPROPRIATE OPTION: ");
		int choice = in.nextInt();
		switch (choice) {
			case 0:
				break;
			case 1:
				System.out.println("\n\nList of all available projects:");
				printProjects(SQLTools.getProjects());
				System.out.println("---------------------------------------------------------------------------------");
				System.out.println("Push ENTER to continue...");
				System.in.read();
				menuShowInfo();
				break;
			case 2:
				System.out.println("\n\nList of all available users:");
				printUsers(SQLTools.getUsers());
				System.out.println("---------------------------------------------------------------------------------");
				System.out.println("Push ENTER to continue...");
				System.in.read();
				menuShowInfo();
				break;
			case 3:
				if(SQLTools.getIssues(new HashSet<Project>(), new HashSet<User>()).isEmpty()) {
					System.err.println("\n\nERROR: THERE IS NO INFORMATION ABOUT EXISTING ISSUES! LOAD DATA FROM THE MAIN MENU OR ADD ISSUES MANUALLY");
					return;
				}
				System.out.println("\n\nList of all available issues:");
				printIssues();
				System.out.println("---------------------------------------------------------------------------------");
				System.out.println("Push ENTER to continue...");
				System.in.read();
				menuShowInfo();
				break;
			case 4:
				if(SQLTools.getIssues(new HashSet<Project>(), new HashSet<User>()).isEmpty()) {
					System.err.println("\n\nERROR: THERE IS NO INFORMATION ABOUT EXISTING ISSUES! PLEASE ADD ISSUES FIRSTLY");
					return;
				}
				genReport();
				menuShowInfo();
				break;
			default:
				System.err.println("\n\nERROR: YOU ENTERED INCORRECT NUMBER! REPEAT PLEASE");
				menuShowInfo();
		}
	}
	
	private boolean chooseProject() throws SQLException {
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tCHOOSING PROJECT MENU");
		HashSet<Project> all_projects = SQLTools.getProjects();
		if(!all_projects.isEmpty()) {
			System.out.println("\nList of projects:");
			printProjects(all_projects);
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE (to add new project enter 0, to return to the main menu enter any other number): ");
		}
		else {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("THERE IS NO INFORMATION ABOUT EXISTING PROJECTS IN THE SYSTEM (to add new project enter 0, to return to the main menu enter any other number): ");
		}
		Scanner in = new Scanner(System.in);
		int choice = in.nextInt();
		if(all_projects.isEmpty() && choice != 0) return false;
		if(!all_projects.isEmpty() && ((choice > all_projects.size()) || choice < 0)) {
			return false;
		}
		else if(choice == 0) {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NAME OF NEW PROJECT: ");
			Scanner in1 = new Scanner(System.in);
			String name = in1.nextLine();
			SQLTools.sendProject(new Project(name));
			this.cur_project = name;
		}
		else {
			int i = 1;
			Project pr = null;
			for(Project p : all_projects) {
				if(i == choice) pr = p; 
				i++;
			}
			this.cur_project = pr.getId();
		}
		return true;
	}
	
	private boolean logIn() throws SQLException {
		//в главном меню перед вызовом всегда подгружать данные
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tLOG IN MENU");
		HashSet<User> all_users = SQLTools.getUsers();
		if(!all_users.isEmpty()) {
			System.out.println("\nList of users:");
			printUsers(all_users);
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE (to add new user enter 0, to return to the main menu enter any other number): ");
		}
		else {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("THERE IS NO INFORMATION ABOUT EXISTING USERS IN THE SYSTEM (to add new user enter 0, to return to the main menu enter any other number): ");
		}
		
		Scanner in = new Scanner(System.in);
		int choice = in.nextInt();
		if(all_users.isEmpty() && choice != 0) return false;
		if(!all_users.isEmpty() && ((choice > all_users.size()) || choice < 0)) {
			return false;
		}
		else if(choice == 0) {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NAME OF NEW USER: ");
			Scanner in1 = new Scanner(System.in);
			String name = in1.nextLine();
			SQLTools.sendUser(new User(name));
			this.cur_user = name;
		}
		else {
			int i = 1;
			User us = null;
			for(User u : all_users) {
				if(i == choice) us = u; 
				i++;
			}
			this.cur_user = us.getId();
		}
		return true;
	}
	
	private boolean addIssue() throws SQLException {
		if(this.cur_user.isEmpty() || this.cur_project.isEmpty()) {
			System.err.println("\n\nERROR: YOU PROBABLY DIDN'T LOG IN OR DIDN'T CHOOSE PROJECT");
			sleep();
			return false;
		}
		System.out.println("\n\n---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tADDING ISSUE");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("ENTER DISCRIPTION OF ISSUE (to return to the main menu enter 0): ");
		Scanner in = new Scanner(System.in);
		String discr = in.nextLine();
		
		System.out.println("\n");
		if(discr.equals("0")) {
			return false;
		}
		
		Date dateNow = new Date();
	    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss a");
	    String dt = formatForDateNow.format(dateNow);
	    
	    SQLTools.sendIssue(new Issue(dt, new Project(this.cur_project), new User(this.cur_user), dt, discr));
	    this.issue_was_added = true;//for not to save when no issues were added	
		return true;
	}
	
	private void saveData() throws TransformerException, IOException, SQLException {
		if(!this.issue_was_added) {
			System.err.println("\n\nINFO: NO ONE BUG HAS BEEN ADDED. SAVING IS NOT NEEDED");
			sleep();
			return;
		}
		XMLTools.saveToXML(SQLTools.getIssues(new HashSet<Project>(), new HashSet<User>()));//get data from database and save them to filesystem
		System.err.println("\n\nINFO: SAVED SUCCESSFULLY");
	}
	
	private void sleep() {
		try {
			Thread.sleep(100);
		}
		catch(Exception e) {}
	}
	
	//menu
	public void menu() throws IOException, TransformerException, SQLException, ClassNotFoundException {
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("\t\tMAIN MENU");
		if(!this.cur_user.isEmpty() && !this.cur_project.isEmpty()) {//вывод текущего пользователя и проекта
			sleep();
			System.err.print(" (Current: user - " + this.cur_user + " | project - " + this.cur_project + ")");			
		}
		Scanner in = new Scanner(System.in);
		System.out.println(
				"\n\n\n0.LEAVE SYSTEM WITH SAVING DATA TO FILESYSTEM\n"
				+ "1.LOAD DATA FROM FILESYSTEM TO SQL DATABASE\n"
				+ "2.LOG IN TO THE SYSTEM\n"
				+ "3.CHOOSE PROJECT\n"
				+ "4.GO TO SHOWING INFORMATION AND GENERATING REPORT MENU\n"
				+ "5.ADD AN ISSUE TO THE SYSTEM\n"
				+ "6.SAVE DATA TO FILESYSTEM WITHOUT EXIT"
				);
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("ENTER THE NUMBER OF APPROPRIATE OPTION: ");
		int choice = in.nextInt();
		switch (choice) {
			case 0:
				saveData();
				break;
			case 1:
				XMLToSQLite();
				sleep();
				menu();
				break;
			case 2:
				System.out.println("\n");
				if(logIn() && cur_project.isEmpty()) {
					System.out.println("\n\n");
					if(!chooseProject()) {
						System.err.println("\n\nINFO: YOU NEED TO CHOOSE PROJECT");
						sleep();
					}
				}
				menu();
				break;
			case 3:
				System.out.println("\n");
				if(cur_user.isEmpty()) {
					System.err.println("INFO: YOU NEED TO LOG IN");
					sleep();
					if(!logIn()) menu();
				}
				System.out.println("\n");
				chooseProject();
				System.out.println("\n");
				menu();
				break;
			case 4:
				try {
					System.out.println("\n");
					menuShowInfo();
				} catch(IOException e) {throw e;}
				System.out.println("\n");
				menu();
				break;
			case 5:
				addIssue();
				menu();
				break;
			case 6:
				saveData();
				sleep();
				menu();
				break;
			default:
				System.err.println("\n\nERROR: YOU ENTERED INCORRECT NUMBER! REPEAT PLEASE");
				menu();
		}
	}
}
