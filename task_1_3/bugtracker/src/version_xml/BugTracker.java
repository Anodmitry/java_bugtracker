package version_xml;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import entities.*;
import tools.XMLTools;

import javax.xml.transform.TransformerException;

public class BugTracker {
	private User cur_user;
	private Project cur_project;
	private Issue cur_issue;//если она осталась null при сохранении, то ничего не сохранять, т.к. не было добавлено ни одной issue
	
	private HashSet<Project> all_projects;
	private HashSet<User> all_users;
	private HashSet<Issue> all_issues;
	
	public BugTracker() {
		this.cur_user = null;
		this.cur_project = null;
		this.cur_issue = null;
	}
		
	private void printIssues() {
		int i = 1;
		for(Issue iss : this.all_issues) {
			System.out.println(String.valueOf(i) + ". " + iss.toString());
			i++;
		}
	}
	
	private void printProjects() {
		int i = 1;
		for(Project p : this.all_projects) {
			System.out.println(String.valueOf(i) + ". " + p.toString());
			i++;
		}
	}
	
	private void printUsers() {
		int i = 1;
		for(User u : this.all_users) {
			System.out.println(String.valueOf(i) + ". " + u.toString());
			i++;
		}
	}
	
	//load data
	private void loadExistingData() {
//		if(this.all_issues != null) {
//			//System.err.println("\n\nERROR: ДАННЫЕ УЖЕ БЫЛИ ЗАГРУЖЕНЫ РАНЕЕ!");
//			return;
//		}
		if(this.all_projects == null) this.all_projects = new HashSet<Project>();
		if(this.all_users == null) this.all_users = new HashSet<User>();
		try {
			this.all_issues = XMLTools.getFromXML(this.all_projects, this.all_users);
		}
		catch(Throwable e) {
			System.err.println("\n\nERROR: XML LOADING ERROR! CHECK XML FILE AND ITS DATA");
			return;
		}
	}
	
	private HashSet<Issue> getSpecifiedIssues(Project p, User u){
		HashSet<Issue> res = new HashSet<Issue>();
		for(Issue is : this.all_issues) {
			if(is.getProject() == p && is.getUser() == u) res.add(is);
		}
		return res;
	}
	
	private void genReport() throws IOException {
		System.out.println("\n\n---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tGENERATING REPORT FOR ISSUES");
		System.out.println("\nList of projects:");
		printProjects();
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE: ");
		Scanner in = new Scanner(System.in);
		int choice = in.nextInt();
		if(choice > this.all_projects.size() || choice < 1) {
			System.err.println("\n\nERROR: YOU ENTERED NUMBER NOT FROM THE LIST ABOVE!");
			return;
		}
		int i = 1;
		Project proj = null;
		for(Project p : this.all_projects) {
			if(i == choice) proj = p; 
			i++;
		}
		
		System.out.println("\nList of users:");
		printUsers();
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE: ");
		choice = in.nextInt();
		if(choice > this.all_users.size() || choice < 1) {
			System.err.println("\n\nERROR: YOU ENTERED NUMBER NOT FROM THE LIST ABOVE!");
			return;
		}
		i = 1;
		User us = null;
		for(User u : this.all_users) {
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
	
	private void menuShowInfo() throws IOException {
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
				printProjects();
				System.out.println("---------------------------------------------------------------------------------");
				System.out.println("Push ENTER to continue...");
				System.in.read();
				menuShowInfo();
				break;
			case 2:
				System.out.println("\n\nList of all available users:");
				printUsers();
				System.out.println("---------------------------------------------------------------------------------");
				System.out.println("Push ENTER to continue...");
				System.in.read();
				menuShowInfo();
				break;
			case 3:
				if(this.all_issues == null) {
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
				if(this.all_issues == null) {
					System.err.println("\n\nERROR: THERE IS NO INFORMATION ABOUT EXISTING ISSUES! LOAD DATA FROM THE MAIN MENU OR ADD ISSUES MANUALLY");
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
	
	private boolean chooseProject() {
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tCHOOSING PROJECT MENU");
		if(this.all_projects != null) {
			System.out.println("\nList of projects:");
			printProjects();
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE (to add new project enter 0, to return to the main menu enter any other number): ");
		}
		else {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("THERE IS NO INFORMATION ABOUT EXISTING PROJECTS IN THE SYSTEM (to add new project enter 0, to return to the main menu enter any other number): ");
		}
		Scanner in = new Scanner(System.in);
		int choice = in.nextInt();
		if(this.all_projects == null && choice != 0) return false;
		if(this.all_projects != null && ((choice > this.all_projects.size()) || choice < 0)) {
			return false;
		}
		else if(choice == 0) {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NAME OF NEW PROJECT: ");
			Scanner in1 = new Scanner(System.in);
			String name = in1.nextLine();
			//System.out.println(name);
			Project new_project = new Project(name);
			this.cur_project = new_project;
			if(this.all_projects == null) this.all_projects = new HashSet<Project>();
			this.all_projects.add(this.cur_project);
		}
		else {
			int i = 1;
			Project pr = null;
			for(Project p : this.all_projects) {
				if(i == choice) pr = p; 
				i++;
			}
			this.cur_project = pr;
		}
		return true;
	}
	
	private boolean logIn() {
		//в главном меню перед вызовом всегда подгружать данные
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("\t\tLOG IN MENU");
		if(this.all_users != null) {
			System.out.println("\nList of users:");
			printUsers();
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NUMBER FROM THE LIST ABOVE (to add new user enter 0, to return to the main menu enter any other number): ");
		}
		else {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("THERE IS NO INFORMATION ABOUT EXISTING USERS IN THE SYSTEM (to add new user enter 0, to return to the main menu enter any other number): ");
		}
		
		Scanner in = new Scanner(System.in);
		int choice = in.nextInt();
		if(this.all_users == null && choice != 0) return false;
		if(this.all_users != null && ((choice > this.all_users.size()) || choice < 0)) {
			return false;
		}
		else if(choice == 0) {
			System.out.println("---------------------------------------------------------------------------------");
			System.out.print("ENTER THE NAME OF NEW USER: ");
			Scanner in1 = new Scanner(System.in);
			String name = in1.nextLine();
			//System.out.println(name);
			User new_user = new User(name);
			this.cur_user = new_user;
			if(this.all_users == null) this.all_users = new HashSet<User>();
			this.all_users.add(this.cur_user);
			//не забыть добавить к новому проект, к проекту нового
		}
		else {
			int i = 1;
			User us = null;
			for(User u : this.all_users) {
				if(i == choice) us = u; 
				i++;
			}
			this.cur_user = us;
		}
		return true;
	}
	
	private boolean addIssue() {
		if(this.cur_user == null || this.cur_project == null) {
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
	    
	    Issue tmpiss = new Issue(dt, this.cur_project, this.cur_user, dt, discr);
	    if(this.all_issues == null) this.all_issues = new HashSet<Issue>();
	    this.all_issues.add(tmpiss);
	    this.cur_issue = tmpiss;
	    this.cur_project.addIssue(tmpiss);
	    this.cur_user.addIssue(tmpiss);		
		return true;
	}
	
	private void saveData() throws TransformerException, IOException {
		if(this.cur_issue == null) {
			System.err.println("\n\nINFO: NO ONE BUG HAS BEEN ADDED. SAVING IS NOT NEEDED");
			sleep();
			return;
		}
		XMLTools.saveToXML(this.all_issues);
		System.err.println("\n\nINFO: SAVED SUCCESSFULLY");
	}
	
	private void sleep() {
		try {
			Thread.sleep(100);
		}
		catch(Exception e) {}
	}
	//menu
	public void menu() throws IOException, TransformerException {
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		System.out.println("---------------------------------------------------------------------------------");
		System.out.print("\t\tMAIN MENU");
		if(this.cur_user != null && this.cur_project != null) {//вывод текущего пользователя и проекта
			sleep();
			System.err.print(" (Current: " + this.cur_user.toString() + " | " + this.cur_project.toString() + ")");			
		}
		Scanner in = new Scanner(System.in);
		System.out.println(
				"\n\n\n0.LEAVE SYSTEM WITH SAVING DATA TO FILESYSTEM\n"
				+ "1.LOAD DATA FROM FILESYSTEM\n"
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
				System.err.println("THE APPLICATION WAS STOPPED.");
				break;
			case 1:
				loadExistingData();
				System.err.println("\n\nINFO: ALL THE DATA LOADED SUCCESSFULLY");
				sleep();
				menu();
				break;
			case 2:
				System.out.println("\n");
				//loadExistingData();
				if(logIn() && cur_project == null) {
					System.out.println("\n");
					if(chooseProject()) {
						this.cur_user.addProject(this.cur_project);
						this.cur_project.addUser(this.cur_user);
						System.out.println("\n");
					}
					else {
						System.err.println("\n\nINFO: YOU NEED TO CHOOSE PROJECT");
						sleep();
					}
				}
				menu();
				break;
			case 3:
				System.out.println("\n");
				//loadExistingData();
				if(cur_user == null) {
					System.err.println("INFO: YOU NEED TO LOG IN");
					sleep();
					if(!logIn()) menu();
				}
				System.out.println("\n");
				if(chooseProject()) {
					this.cur_user.addProject(this.cur_project);
					this.cur_project.addUser(this.cur_user);
				}
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
