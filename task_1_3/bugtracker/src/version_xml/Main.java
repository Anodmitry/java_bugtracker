package version_xml;

import tools.XMLTools;

public class Main {
	public static void main(String[] args) {
		XMLTools.filename = "bugtracker_test.xml";
		BugTracker bg = new BugTracker();
		try {
			bg.menu();
		}
		catch(Throwable t) {
			t.printStackTrace();
		}		
	}
}
