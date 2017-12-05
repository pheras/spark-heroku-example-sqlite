
import static spark.Spark.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.net.URISyntaxException;
import java.net.URI;

public class Main {
	


	public static String doWork() throws ClassNotFoundException, URISyntaxException {
		String result = new String();
		
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
			
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists person");
			statement.executeUpdate("create table person (id integer, name string)");

			statement.executeUpdate("insert into person values(1, 'PING')");
			statement.executeUpdate("insert into person values(2, 'PONG')");
			ResultSet rs = statement.executeQuery("select * from person");
			while (rs.next()) {
				// read the result set
				result += "name = " + rs.getString("name") + "\n";
				System.out.println("name = " + rs.getString("name"));
				
				result += "id = " + rs.getInt("id") + "\n";
				System.out.println("id = " + rs.getInt("id"));
			}
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}

		return result;
	}

	public static void main(String[] args) throws ClassNotFoundException {
        port(getHerokuAssignedPort());

		// spark server
		get("/hello", (req, res) -> doWork());

	}
	static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }

}


