package entities;

import java.util.HashSet;

public class User {
	//ÏÎÌÅÍßÒÜ ÌÎÄÈÔÈÊÀÒÎĞÛ, íàêèäàòü set è get
	private String id;
	private HashSet<Project> projects;
	private HashSet<Issue> issues;
	public User(String name){
		try {
			this.setId(name);
			this.issues = new HashSet<Issue>();
			this.projects = new HashSet<Project>();
		} catch(Exception e) {
			throw e; 
		}
	}
	//addProject
	public void addProject(Project pro) {
		projects.add(pro);
	}
	//addIssue
	public void addIssue(Issue iss) {
		issues.add(iss);
	}
	
	@Override public String toString() {
		return "User: " + this.getId();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
