package application;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * App starter.
 * 
 * @author ruochen.xu
 */
@ComponentScan({ "controllers", "configs" })
@SpringBootApplication
public class Application {
	public static void main(String[] args) throws InterruptedException,
			SQLException {
		/*
		 * If http client print too much log info, please set root logger level
		 * by hand.
		 * 
		 * ch.qos.logback.classic.Logger logger =
		 * (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
		 * .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		 * logger.setLevel(ch.qos.logback.classic.Level.INFO);
		 */
		SpringApplication.run(Application.class, args);
		// Start task manager.
		new TaskManager().work();
		
	}
}