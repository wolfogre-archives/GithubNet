import java.sql.*;

/**
 * Created by Jason Song(wolfogre@outlook.com) on 01/20/2016.
 */
public class Crawler {
	public Crawler(){

	}

	public void connectDatabase(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Class.forName(jdbcDriver).newInstance();
		Connection connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("select * from user");
		while(rs.next()){
			System.out.println(rs.getInt(1)+"\t"+rs.getString(2));
		}
	}
}
