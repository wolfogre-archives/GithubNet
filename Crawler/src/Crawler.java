import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
		List followers = new LinkedList<>();
		while(true){
			try {
				Document document = Jsoup.connect("https://github.com/wolfogre/followers").get();
				String html = document.html();
				followers.add(html);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}

		String[] result = new String[followers.size()];
		for(int i = 0; i < followers.size(); ++i){
			result[i] = (String)followers.get(i);
		}
		return result;
	}
}
