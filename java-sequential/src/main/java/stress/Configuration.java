package stress;

import org.rapidoid.annotation.Run;
import org.rapidoid.jdbc.JDBC;
import org.rapidoid.jdbc.JdbcClient;
import org.rapidoid.log.Log;
import org.rapidoid.log.LogLevel;

@Run
public class Configuration {

	public static JdbcClient jdbcClient;

	public static void main(String[] args) {

		Log.setLogLevel(LogLevel.TRACE);

		jdbcClient = JDBC.driver("com.mysql.jdbc.Driver");
		//jdbcClient.url("jdbc:mysql://mysql:Welcome@localhost:3306/encrypted_tokens?logger=Slf4JLogger&profileSQL=true");
		jdbcClient.url("jdbc:mysql://mysql:Welcome@localhost:3306/encrypted_tokens");
		jdbcClient.init();

	}
}
