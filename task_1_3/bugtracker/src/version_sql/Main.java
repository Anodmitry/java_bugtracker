package version_sql;

import tools.SQLTools;
import tools.XMLTools;

public class Main {
	public static void main(String[] args) {
		XMLTools.filename = "bugtracker_test.xml";		
		try {
			SQLTools.connectToDB("bugtracker_db.s3db");			
			SQLTools.clearAllTables();//чищу все таблицы в SQLite, чтобы честно загружать данные из XML-файла из файловой системы в SQLite - метод XMLToSQLite
			
			BugTracker bg = new BugTracker();
			bg.menu();
			SQLTools.closeDB();
		}
		catch(Throwable t) {//знаю, ловить Throwable плохо, но никак не ловилась почему-то ошибка синтаксиса XML
			t.printStackTrace();
		}
		
	}
}
