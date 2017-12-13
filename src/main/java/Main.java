
import static spark.Spark.*;
import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import javax.servlet.MultipartConfigElement;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class Main {

	public static String doWork(Request request, Response response) throws ClassNotFoundException, URISyntaxException {
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

			statement.executeUpdate("insert into person values(1, 'PIN')");
			statement.executeUpdate("insert into person values(2, 'PON')");
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
		get("/hello", Main::doWork);

		
		get("/", (req, res) -> "<form action='/upload' method='post' enctype='multipart/form-data'>" // note the enctype
				+ "    <input type='file' name='uploaded_file' accept='.txt'>" // make sure to call getPart using the
																				// same "name" in the post
				+ "    <button>Upload file</button>" + "</form>");

		post("/upload", (req, res) -> {
			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
			String result = "";
			try (InputStream input = req.raw().getPart("uploaded_file").getInputStream()) { 
				// getPart needs to use same
				// "name" as input field in
			    // form

				InputStreamReader isr = new InputStreamReader(input);
				BufferedReader br = new BufferedReader(isr);
				String s;
				while ((s = br.readLine()) != null) {
					result = s;
					System.out.println(s);

					StringTokenizer tokenizer = new StringTokenizer(result, "/");
					while (tokenizer.hasMoreTokens()) {
						System.out.println(tokenizer.nextToken());
					}

				}
				input.close();
			}
			return result;
		});

	}

	static int getHerokuAssignedPort() {
		ProcessBuilder processBuilder = new ProcessBuilder();
		if (processBuilder.environment().get("PORT") != null) {
			return Integer.parseInt(processBuilder.environment().get("PORT"));
		}
		return 4567; // return default port if heroku-port isn't set (i.e. on localhost)
	}
}
