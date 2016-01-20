import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Jason Song(wolfogre@outlook.com) on 01/20/2016.
 */
public class Crawler {
	public Crawler(){

	}

	public void connectDatabase(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		Driver driver = (Driver) (Class.forName(jdbcDriver).newInstance());
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		Connection connection = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
	}
}
