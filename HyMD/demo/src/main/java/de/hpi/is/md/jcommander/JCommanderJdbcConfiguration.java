package de.hpi.is.md.jcommander;

import com.beust.jcommander.Parameter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.Data;

@Data
public class JCommanderJdbcConfiguration {

	@Parameter(names = {"--password",
		"-pw"}, description = "connection password", password = true, required = true)
	private String password;
	@Parameter(names = {"--user", "-u"}, required = true)
	private String user;
	@Parameter(names = {"--connection", "-c"}, required = true)
	private String url;
	@Parameter(names = {"--driver", "-d"}, required = true)
	private String driverName;

	public Connection createConnection() throws SQLException, ClassNotFoundException {
		Class.forName(driverName);
		return DriverManager.getConnection(url, user, password);
	}
}
