package application;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({ "controllers", "configs" })
@SpringBootApplication
public class Application {
	public static final int TASK_NUM = 2;
	public static void main(String[] args) throws InterruptedException, SQLException {
		SpringApplication.run(Application.class, args);
		for (int i = 1; i <= TASK_NUM; i++) {
			new Thread(new TaskExecutor()).run();
		}
	}
}