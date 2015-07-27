package operators;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import configs.Config;

public class Database {
	public static JdbcTemplate getTemplate() {
		DriverManagerDataSource source = new DriverManagerDataSource();
		source.setDriverClassName(Config.DB_DRIVER);
		source.setUrl(Config.DB_ADDRESS);
		source.setUsername(Config.DB_USER);
		source.setPassword(Config.DB_PASSWD);
		return new JdbcTemplate(source);
	}
}
