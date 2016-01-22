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
			System.out.print("Password for database:");
			crawler.connectDatabase("com.mysql.jdbc.Driver", "jdbc:mysql://120.27.99.15:3306/github_net", "wolf", new Scanner(System.in).nextLine());
		} catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
			return;
		}

		try {
			crawler.startCrawl();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
