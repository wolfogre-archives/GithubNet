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

/*
use github_net;
drop table connection;
drop table user;
create table user(
	id int auto_increment,
	name char(39) not null,
	done boolean not null,
	primary key (id),
	constraint AK_NoRepeatName unique (name)
);
create index idx on user(name);

create table connection(
	followfrom int not null,
	followto int not null,
	primary key (followfrom, followto),
	foreign key (followfrom) references user(id),
	foreign key (followto) references user(id),
	constraint CN_NoFollowSelf check (followfrom <> followto)
);

insert into user(name, done) values ('wolfogre', false);
 */

public class Crawler {

	private Connection connection;

	public void connectDatabase(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Class.forName(jdbcDriver).newInstance();
		connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
	}

	public void startCrawl() throws SQLException {
		Statement statementNotDone = connection.createStatement();
		Statement statementQuery = connection.createStatement();
		Statement statementUpdate = connection.createStatement();
		ResultSet rsNotDone = statementNotDone.executeQuery("select * from user where done = false");

		while(rsNotDone.next()){
			String username = rsNotDone.getString("name");
			int userId = rsNotDone.getInt("id");
			int followingPage = 1;
			while(true) {
				String[] followings = getFollowings(username, followingPage++);
				if (followings.length == 0)
					break;
				else {
					for (String following : followings) {
						ResultSet rs = statementQuery.executeQuery("select * from user where name = '" + following + "'");
						if (!rs.next()) {
							statementUpdate.executeUpdate("insert into user(name, done) values ('" + following + "', false)");
							rs = statementQuery.executeQuery("select * from user where name = '" + following + "'");
							rs.next();
							System.out.println("New user: ID " + rs.getInt("id") + ", name " + following);
						}
						try {
							statementUpdate.executeUpdate("insert into connection (followfrom, followto) values (" + userId + "," + rs.getInt("id") + ")");
							System.out.println("New connection: " + userId + " " + username + " -> " + rs.getInt("id") + " " + following);
						} catch (SQLException e) {
							System.out.println("Fail to insert into connetion (followfrom, followto) values (" + userId + "," + rs.getInt("id") + ")");
							System.out.println(e.getMessage());
						}
					}
				}
			}
			int followerPage = 1;
			while(true){
				String[] followers = getFollowers(username, followerPage++);
				if(followers.length == 0)
					break;
				else{
					for(String follower : followers){
						ResultSet rs = statementQuery.executeQuery("select * from user where name = '" + follower + "'");
						if(!rs.next())
						{
							statementUpdate.executeUpdate("insert into user(name, done) values ('" + follower +"', false)");
							rs = statementQuery.executeQuery("select * from user where name = '" + follower + "'");
							rs.next();
							System.out.println("New user: ID " + rs.getInt("id") + ", name " + rs.getString("name"));
						}
					}
				}
			}

			statementUpdate.executeUpdate("update user set done = true where id = " + userId);

			if(!rsNotDone.next()){
				rsNotDone = statementNotDone.executeQuery("select * from user where done = false");
				continue;
			}
			rsNotDone.previous();

		}
	}

	String[] getFollowers(String username, int page){
		List followers = new LinkedList<>();
		while(true){
			try {
				System.out.println("Connect to " + "https://github.com/" + username + "/followers?page=" + page);
				Document document = Jsoup.connect("https://github.com/" + username + "/followers?page=" + page).get();
				Elements links = document.select("h3[class=follow-list-name]");
				for (Element link : links) {
					Document doc = Jsoup.parse(link.html());
					Element name = doc.select("a[href]").first();
					followers.add(name.attr("href"));
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					System.out.println("Waiting...");
					Thread.sleep(60000 * 2);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			break;
		}
		String[] result = new String[followers.size()];
		for(int i = 0; i < followers.size(); ++i){
			result[i] = ((String)followers.get(i)).replace("/", "");
		}
		return result;
	}

	String[] getFollowings(String username, int page){
		List followings = new LinkedList<>();
		while(true){
			try {
				System.out.println("Connect to " + "https://github.com/" + username + "/following?page=" + page);
				Document document = Jsoup.connect("https://github.com/" + username + "/following?page=" + page).get();
				Elements links = document.select("h3[class=follow-list-name]");
				for (Element link : links) {
					Document doc = Jsoup.parse(link.html());
					Element name = doc.select("a[href]").first();
					followings.add(name.attr("href"));
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					System.out.println("Waiting...");
					Thread.sleep(60000 * 2);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
			break;
		}
		String[] result = new String[followings.size()];
		for(int i = 0; i < followings.size(); ++i){
			result[i] = ((String)followings.get(i)).replace("/", "");
		}
		return result;
	}
}
