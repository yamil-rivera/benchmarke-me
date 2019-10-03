package stress;

import org.rapidoid.annotation.Run;
import org.rapidoid.config.Conf;
import org.rapidoid.jdbc.HikariFactory;
import org.rapidoid.jdbc.JDBC;
import org.rapidoid.jdbc.JdbcClient;
import org.rapidoid.log.Log;
import org.rapidoid.log.LogLevel;
import org.rapidoid.log.LogOptions;
import org.rapidoid.setup.On;
import org.slf4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Run
public class Configuration {

	public static JdbcClient jdbcClient;
	public static Executor ioExecutor;
	private static final short ioThreads = 8;
	private static final short maxConnectionPoolSize = 20;

	public static void main(String[] args) {

		Log.setLogLevel(LogLevel.TRACE);

		jdbcClient = JDBC.driver("org.sqlite.JDBC");
		//jdbcClient.url("jdbc:mysql://mysql:Welcome@localhost:3306/encrypted_tokens?logger=Slf4JLogger&profileSQL=true");
		jdbcClient.url("jdbc:sqlite:sample.db");
		Conf.HIKARI.set("maximumPoolSize", maxConnectionPoolSize);
		jdbcClient.dataSource(HikariFactory.createDataSourceFor(jdbcClient));
		jdbcClient.init();

		ioExecutor = Executors.newFixedThreadPool(ioThreads);

	}
}
