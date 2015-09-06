package operators;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import configs.Config;

/**
 * @author ruochen.xu
 */
public class Database {
	public static DataSource getDataSrouce() {
		DriverManagerDataSource source = new DriverManagerDataSource();
		source.setDriverClassName(Config.DB_DRIVER);
		source.setUrl(Config.DB_ADDRESS);
		source.setUsername(Config.DB_USER);
		source.setPassword(Config.DB_PASSWD);
		return source;
	}

	public static JdbcTemplate getTemplate() {
		return new JdbcTemplate(getDataSrouce());
	}

	public static Connection getConneciont() throws SQLException {
		return getDataSrouce().getConnection();
	}
}
