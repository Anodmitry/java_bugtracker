package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import entities.*;

public class SQLTools {
	public static Connection conn;
	public static Statement stat;
	public static ResultSet resset;
	
	//подключение к базе данных
	public static void connectToDB(String dbfilename) throws ClassNotFoundException, SQLException
	{
		conn = null;
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbfilename);
		stat = conn.createStatement();
		   
		System.err.println("Connected successfully to database!");
	}
	
	public static void closeDB() throws SQLException, ClassNotFoundException {
		if(conn != null) conn.close();
		if(stat != null) stat.close();
		if(resset != null) resset.close();
		
		System.err.println("Connection closed!");
	}
	
	public static void clearAllTables() throws SQLException {
		stat.execute("DELETE FROM Issues");
		stat.execute("DELETE FROM Projects");
		stat.execute("DELETE FROM Users");
		System.err.println("INFO: ALL TABLES ARE CLEAR");
	}
	
	public static void readQuery(String sql, boolean needshow) throws SQLException {
		resset = null;
		resset = stat.executeQuery(sql);
		if(needshow) {
			int columns_count = resset.getMetaData().getColumnCount();
			//System.out.println(columns_count);
			for (int i = 1; i <= columns_count; i++){
	            System.out.print(resset.getMetaData().getColumnLabel(i));
	            if(i<columns_count) System.out.print("|");
	        }
			System.out.println();
			while(resset.next()) {
				for (int i = 1; i <= columns_count; i++){
	                System.out.print(resset.getString(i));
	                if(i<columns_count) System.out.print("|");
	            }
	            System.out.println();		
			}
		}		
	}
	
	public static HashSet<Project> getProjects() throws SQLException{
		HashSet<Project> res = new HashSet<Project>();
		readQuery("SELECT name FROM Projects", false);
		while(resset.next()) {
			Project p = new Project(resset.getString(1));
			res.add(p);
		}
		return res;
	}
	
	public static HashSet<User> getUsers() throws SQLException{
		HashSet<User> res = new HashSet<User>();
		readQuery("SELECT name FROM Users", false);
		while(resset.next()) {
			User u = new User(resset.getString(1));
			res.add(u);
		}
		return res;
	}
	
	private static Project getObjProject(String id, HashSet<Project> all_projects_set) {
		for(Project tmp : all_projects_set) {
			if(tmp.getId().equals(id)) return tmp;//вернуть существующий Project
		}
		Project new_proj = new Project(id);
		all_projects_set.add(new_proj);
		return new_proj;//вернуть новый Project
	}
	
	//используется при считывании из бд для добавления либо нового User, либо существующего, без повторений
	private static User getObjUser(String id, HashSet<User> all_users_set) {
		for(User tmp : all_users_set) {
			if(tmp.getId().equals(id)) return tmp;//вернуть существующий User
		}
		User new_user = new User(id);
		all_users_set.add(new_user);//добавили к множеству всех пользователей нового
		return new_user;//вернуть новый User
	}
	
	public static HashSet<Issue> getIssues(HashSet<Project> hsp, HashSet<User> hsu) throws SQLException{
		HashSet<Issue> res = new HashSet<Issue>();
		
		SQLTools.readQuery("SELECT Projects.name AS project, Users.name AS user, Issues.discription, Issues.find_date FROM Issues INNER JOIN Projects, Users ON Projects.id = Issues.id_project AND Users.id = Issues.id_user", false);
		while(resset.next()) {
			Project p = getObjProject(resset.getString(1), hsp);
			User u = getObjUser(resset.getString(2), hsu);
			String disc = resset.getString(3);
			String id_issue = resset.getString(4);
			Issue is = new Issue(id_issue, p, u, id_issue, disc);
			
			p.addIssue(is);
			p.addUser(u);			
			u.addIssue(is);
			u.addProject(p);
			
			hsp.add(p);
			hsu.add(u);
			res.add(is);
		}
		return res;
	}
	
	public static HashSet<Issue> getSpecifiedIssues(Project p, User u) throws SQLException{
		HashSet<Issue> res = new HashSet<Issue>();
		
		SQLTools.readQuery("SELECT Issues.discription, Issues.find_date "
				+ "FROM Issues INNER JOIN Projects, Users ON Projects.id = Issues.id_project "
				+ "AND Users.id = Issues.id_user "
				+ "WHERE Projects.name = '" + p.getId() + "' AND Users.name = '" + u.getId() + "'", false);
		
		while(resset.next()) {
			String disc = resset.getString(1);
			String id_issue = resset.getString(2);
			Issue is = new Issue(id_issue, p, u, id_issue, disc);
			
			p.addIssue(is);
			p.addUser(u);			
			u.addIssue(is);
			u.addProject(p);
			
			res.add(is);
		}
		return res;
	}
	
	public static String getEntityIdFromDB(String table, String id) throws SQLException {
		readQuery("SELECT id FROM " + table + " WHERE name = '" + id + "'", false);
		String res = "";
		while(resset.next()) {
			res = resset.getString(1);
			//System.out.println(res);
		}
		return res;
	}
	
	public static void sendUser(User u) throws SQLException {
		String id_user = getEntityIdFromDB("Users", u.getId());
		if(id_user.isEmpty()) {//добавляем нового пользователя в бд
			stat.execute("INSERT INTO Users ('name') VALUES ('" + u.getId() + "')");
			id_user = getEntityIdFromDB("Users", u.getId());
		}
	}	
	
	public static void sendProject(Project p) throws SQLException {
		String id_project = getEntityIdFromDB("Projects", p.getId());
		if(id_project.isEmpty()) {//добавляем проект в бд, так как его нет в таблице проектов
			stat.execute("INSERT INTO Projects ('name') VALUES ('" + p.getId() + "')");
			id_project = getEntityIdFromDB("Projects", p.getId());
		}
	}
	
	public static void sendIssue(Issue i) throws SQLException {
		SQLTools.readQuery("SELECT * FROM Issues WHERE find_date = '" + i.getId() + "'", false);//проверка на наличие issue с таким же find_date
			//System.out.println(i.id);
			//System.out.println(!resset.next());
		if(!resset.next()) {//проверка, что не было ещё такой issue
			String id_project = getEntityIdFromDB("Projects", i.getProject().getId());
			if(id_project.isEmpty()) {//добавляем проект в бд, так как его не в таблице проектов
				stat.execute("INSERT INTO Projects ('name') VALUES ('" + i.getProject().getId() + "')");
				id_project = getEntityIdFromDB("Projects", i.getProject().getId());
			}
			
			String id_user = getEntityIdFromDB("Users", i.getUser().getId());
			if(id_user.isEmpty()) {//добавляем нового пользователя в бд
				stat.execute("INSERT INTO Users ('name') VALUES ('" + i.getUser().getId() + "')");
				id_user = getEntityIdFromDB("Users", i.getUser().getId());
			}				
			stat.execute("INSERT INTO Issues ('id_project', 'id_user', 'discription', 'find_date') VALUES ('"
					+ id_project + "', '" + id_user + "', '" + i.getDiscription() + "', '" + i.getId() + "')");
		}
	}
	
	public static void sendIssues(HashSet<Issue> iss) throws SQLException {
		for(Issue i : iss) {
			SQLTools.readQuery("SELECT * FROM Issues WHERE find_date = '" + i.getId() + "'", false);//проверка на наличие issue с таким же find_date
			//System.out.println(i.id);
			//System.out.println(!resset.next());
			if(!resset.next()) {//is empty, то есть не было ещё такой issue
				String id_project = getEntityIdFromDB("Projects", i.getProject().getId());
				if(id_project.isEmpty()) {//добавляем проект в бд, так как его не в таблице проектов
					stat.execute("INSERT INTO Projects ('name') VALUES ('" + i.getProject().getId() + "')");
					id_project = getEntityIdFromDB("Projects", i.getProject().getId());
				}
				
				String id_user = getEntityIdFromDB("Users", i.getUser().getId());
				if(id_user.isEmpty()) {//добавляем нового пользователя в бд
					stat.execute("INSERT INTO Users ('name') VALUES ('" + i.getUser().getId() + "')");
					id_user = getEntityIdFromDB("Users", i.getUser().getId());
				}				
				stat.execute("INSERT INTO Issues ('id_project', 'id_user', 'discription', 'find_date') VALUES ('"
						+ id_project + "', '" + id_user + "', '" + i.getDiscription() + "', '" + i.getId() + "')");
			}
		}
	}
}
