package entities;

import java.util.HashSet;

public class Project {
	private String id;//=name 
	private HashSet<User> users;
	private HashSet<Issue> issues;
	public Project(String name){
		this.setId(name);
		this.users = new HashSet<User>();
		this.issues = new HashSet<Issue>();
	}
	public void addIssue(Issue iss) {
		issues.add(iss);
	}	
	public void addUser(User us) {
		users.add(us);
	}
	@Override public String toString() {
		return "Project: " + this.getId();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
