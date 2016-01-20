import java.sql.SQLException;
import java.util.Scanner;

/**
 * Created by Jason Song(wolfogre@outlook.com) on 01/20/2016.
 */
public class DebugCrawler {
	/**
	 * The program entry for debugging Crawler
	 */
	public static void main(String args[]){

		Crawler crawler = new Crawler();
		try {
			crawler.connectDatabase("com.microsoft.sqlserver.jdbc.SQLServerDriver",
									"jdbc:microsoft:sqlserver://115.28.191.67:1433/github-net",
									"sa",
									(new Scanner(System.in)).nextLine());
		} catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

	}
}
