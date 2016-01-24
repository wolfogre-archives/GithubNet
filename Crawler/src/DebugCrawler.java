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
		int time = 0;
		while(true){
			crawler.getUsers("wolfogre_not_exist", 1, Crawler.ConnectionType.FOLLOWER);
			System.out.println("   " + ++time);
		}

	}
}
