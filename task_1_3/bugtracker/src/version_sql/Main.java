package version_sql;

import tools.SQLTools;
import tools.XMLTools;

public class Main {
	public static void main(String[] args) {
		XMLTools.filename = "bugtracker_test.xml";		
		try {
			SQLTools.connectToDB("bugtracker_db.s3db");			
			SQLTools.clearAllTables();//���� ��� ������� � SQLite, ����� ������ ��������� ������ �� XML-����� �� �������� ������� � SQLite - ����� XMLToSQLite
			
			BugTracker bg = new BugTracker();
			bg.menu();
			SQLTools.closeDB();
		}
		catch(Throwable t) {//����, ������ Throwable �����, �� ����� �� �������� ������-�� ������ ���������� XML
			t.printStackTrace();
		}
		
	}
}
