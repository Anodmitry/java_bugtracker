package tools;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import entities.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

public class XMLTools {
	public static String filename;// = "bugtracker_test.xml";
	private static HashSet<Project> all_projects_set;
	private static HashSet<User> all_users_set;
	
	private static DocumentBuilder builder;
	
	private static void initXMLWriter() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try { builder = factory.newDocumentBuilder(); }
		catch (ParserConfigurationException e) { e.printStackTrace(); }
	}
	
	public static void saveToXML(HashSet<Issue> issues_set) throws TransformerException, IOException {
		initXMLWriter();
		
        Document doc=builder.newDocument();
        Element root=doc.createElement("Root");
        Element issues = doc.createElement("Issues");
        
        for(Issue tis : issues_set) {
        	Element issue = doc.createElement("Issue");
        	issue.setAttribute("id", tis.getId());
        	
        	Element project = doc.createElement("Project");
        	project.setAttribute("id", tis.getProject().getId());
        	issue.appendChild(project);
        	
        	Element user = doc.createElement("User");
        	user.setAttribute("id", tis.getUser().getId());
        	issue.appendChild(user);
        	
        	Element discr = doc.createElement("Discription");
        	discr.setTextContent(tis.getDiscription());
        	issue.appendChild(discr);
        	
        	Element date = doc.createElement("Date");
        	date.setTextContent(tis.getDate_of_birth());
        	issue.appendChild(date);
        	
        	issues.appendChild(issue);
        }
        root.appendChild(issues);
        doc.appendChild(root);
 
        Transformer t=TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.transform(new DOMSource(doc), new StreamResult(new FileOutputStream("bugtracker_test.xml")));
 
    }

	private static String getEntityId(String entity, Node node) {
		String res = "error";
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element iss = (Element) node;
            Element sub = (Element) iss.getElementsByTagName(entity).item(0);
    		res = sub.getAttribute("id");            
        }
        return res;
    }
	
	private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }
	
	private static Project getObjProject(String id) {
		for(Project tmp : all_projects_set) {
			if(tmp.getId().equals(id)) return tmp;//вернуть существующий Project
		}
		Project new_proj = new Project(id);
		all_projects_set.add(new_proj);
		return new_proj;//вернуть новый Project
	}
	
	//используется при считывании xml для добавления либо нового User, либо существующего
	private static User getObjUser(String id) {
		for(User tmp : all_users_set) {
			if(tmp.getId().equals(id)) return tmp;//вернуть существующий User
		}
		User new_user = new User(id);
		all_users_set.add(new_user);//добавили к множеству всех пользователей нового
		return new_user;//вернуть новый User
	}
	
	private static Issue getIssue(Node node) {
		try {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
		        Element iss = (Element) node;
		        Project tmpp = getObjProject(getEntityId("Project", iss));//возвращает существующий проект с таким id, если его нет, создаёт новый
		        User tmpu = getObjUser(getEntityId("User", iss));
		        Issue obj_iss = new Issue(iss.getAttribute("id"), tmpp, tmpu, getTagValue("Date", iss), getTagValue("Discription", iss));
		        tmpp.addIssue(obj_iss);
		        tmpp.addUser(tmpu);
		        tmpu.addIssue(obj_iss);
		        tmpu.addProject(tmpp);
		        return obj_iss;
		    }
		    return null;
		}
		catch(Throwable t) {
			throw t;
		}
	}
	
	public static HashSet<Issue> getFromXML(HashSet<Project> all_projects, HashSet<User> all_users) throws Throwable {
		String filepath = filename;
		try {
        	File xmlFile = new File(filepath);
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder builder;
			//Парсить xml и сохранять в arraylist
        	builder = factory.newDocumentBuilder();	
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            //System.out.println("Корневой элемент: " + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("Issue");           
            HashSet<Issue> issues_set = new HashSet<Issue>();
            all_projects_set = all_projects;
            all_users_set = all_users;
            for (int i = 0; i < nodeList.getLength(); i++) {
            	issues_set.add(getIssue(nodeList.item(i)));
            }
            return issues_set;
		}
		catch(Throwable e) {
			throw e;
		}
	}
}
