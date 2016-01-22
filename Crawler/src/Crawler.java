import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jason Song(wolfogre@outlook.com) on 01/20/2016.
 */
public class Crawler {

	private Connection connection;
	private Queue userQueue;
	public Crawler(){
		userQueue = new LinkedBlockingQueue<>();
	}

	public void connectDatabase(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Class.forName(jdbcDriver).newInstance();
		connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
	}

	public void startCrawl(String firstUserName) throws SQLException {
		userQueue.add(firstUserName);
		Statement statement = connection.createStatement();
		while(!userQueue.isEmpty()){
			String username = (String)userQueue.poll();
			ResultSet rs = statement.executeQuery("select * from user where name = " + username );
			if(rs.next())
				continue;
			//TODO
		}
	}

	String[] getFollowers(String username){
		int sleepTime = 0;
		List followers = new LinkedList<>();
		int page = 1;
		while(true){
			try {
				Document document = Jsoup.connect("https://github.com/" + username + "/followers?page=" + page).get();
				Elements links = document.select("h3[class=follow-list-name]");
				if(links.isEmpty())
					break;
				for (Element link : links) {
					Document doc = Jsoup.parse(link.html());
					Element name = doc.select("a[href]").first();
					followers.add(name.attr("href"));
				}
			} catch (IOException e) {
				--page;
				System.out.println(e.getMessage());
				e.printStackTrace();
				try {
					sleepTime += 60000;
					Thread.sleep(60000);
					System.out.println("sleepTime  " + sleepTime);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			System.out.println(followers.size() + "  " + page);
			++page;
		}

		String[] result = new String[followers.size()];
		for(int i = 0; i < followers.size(); ++i){
			result[i] = ((String)followers.get(i)).replace("//", "");
		}
		System.out.println("sleepTime  " + sleepTime);
		return result;
	}
}
