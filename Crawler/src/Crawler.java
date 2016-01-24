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
	name char(40) not null,
	done boolean not null,
	next_following_page int not null,
	next_follower_page int not null,
	update_time datetime not null,
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

insert into user(name, done, next_following_page, next_follower_page, update_time) values ('wolfogre', false, 1, 1, NOW());
 */

public class Crawler {

	private Connection connection;

	public void connectDatabase(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Class.forName(jdbcDriver).newInstance();
		connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
	}

	public void startCrawl() throws SQLException {
		Statement statementNotDone = connection.createStatement();
		Statement statementUpdate = connection.createStatement();

		ResultSet rsNotDone = statementNotDone.executeQuery("select * from user where done = false");

		while(rsNotDone.next()){
			if(crawlConnection(rsNotDone, ConnectionType.FOLLOWING) && crawlConnection(rsNotDone, ConnectionType.FOLLOWER)){
				statementUpdate.executeUpdate("update user set done = true where id = " + rsNotDone.getInt("id"));
				statementUpdate.executeUpdate("update user set update_time= NOW() where id = " + rsNotDone.getInt("id"));
			}

			if(!rsNotDone.next()){
				rsNotDone = statementNotDone.executeQuery("select * from user where done = false");
				continue;
			}
			rsNotDone.previous();
		}
	}

	enum ConnectionType{FOLLOWING, FOLLOWER};
	boolean crawlConnection(ResultSet rsNotDone, ConnectionType connectionType) throws SQLException {
		Statement statementQuery = connection.createStatement();
		Statement statementUpdate = connection.createStatement();
		int page = 1;
		switch(connectionType){
			case FOLLOWING:
				page = rsNotDone.getInt("next_following_page");
				break;
			case FOLLOWER:
				page = rsNotDone.getInt("next_follower_page");
				break;
		}
		int maxPage = page + 35;
		while(page < maxPage){
			String[] users = new String[0];
			switch(connectionType){
				case FOLLOWING:
					users = getFollowings(rsNotDone.getString("name"), page);
					break;
				case FOLLOWER:
					users = getFollowers(rsNotDone.getString("name"), page);
					break;
			}
			if (users.length == 0)
			{
				switch(connectionType){
					case FOLLOWING:
						statementUpdate.executeUpdate("update user set next_following_page = " + page + " where id = " + rsNotDone.getInt("id"));
						statementUpdate.executeUpdate("update user set update_time= NOW() where id = " + rsNotDone.getInt("id"));
						break;
					case FOLLOWER:
						statementUpdate.executeUpdate("update user set next_follower_page = " + page + " where id = " + rsNotDone.getInt("id"));
						statementUpdate.executeUpdate("update user set update_time= NOW() where id = " + rsNotDone.getInt("id"));
						break;
				}
				return true;
			}
			for (String user : users) {
				ResultSet rs = statementQuery.executeQuery("select * from user where name = '" + user + "'");
				if (!rs.next()) {
					statementUpdate.executeUpdate("insert into user(name, done, next_following_page, next_follower_page, update_time) values ('" + user + "', false, 1, 1, NOW())");
					rs = statementQuery.executeQuery("select * from user where name = '" + user + "'");
					rs.next();
					System.out.println("New user: ID " + rs.getInt("id") + ", name " + rs.getString("name"));
				}
				switch(connectionType){
					case FOLLOWING:
						try {
							statementUpdate.executeUpdate("insert into connection (followfrom, followto) values (" + rsNotDone.getInt("id") + "," + rs.getInt("id") + ")");
							System.out.println("New connection: " + rsNotDone.getInt("id") + " " + rsNotDone.getString("name") + " -> " + rs.getInt("id") + " " + rs.getString("name"));
						} catch (SQLException e) {
							System.out.println("Fail to insert into connetion (followfrom, followto) values (" + rsNotDone.getInt("id") + "," + rs.getInt("id") + ")");
							System.out.println(e.getMessage());
						}
						break;
					case FOLLOWER:
						//Do nothing
						break;
				}
			}
			++page;
		}
		switch(connectionType){
			case FOLLOWING:
				statementUpdate.executeUpdate("update user set next_following_page = " + page + " where id = " + rsNotDone.getInt("id"));
				statementUpdate.executeUpdate("update user set update_time= NOW() where id = " + rsNotDone.getInt("id"));
				break;
			case FOLLOWER:
				statementUpdate.executeUpdate("update user set next_follower_page = " + page + " where id = " + rsNotDone.getInt("id"));
				statementUpdate.executeUpdate("update user set update_time= NOW() where id = " + rsNotDone.getInt("id"));
				break;
		}
		return false;
	}

	String[] getFollowers(String username, int page){
		List followers = new LinkedList<>();
		while(true){
			if(page > 100)
				break;
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
					Thread.sleep(1000);
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
			if(page > 100)
				break;
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
					Thread.sleep(1000);
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
