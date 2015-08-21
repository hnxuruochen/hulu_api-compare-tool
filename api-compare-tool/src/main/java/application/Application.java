package application;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import configs.Config;

@ComponentScan({ "controllers", "configs" })
@SpringBootApplication
public class Application {
	public static void main(String[] args) throws InterruptedException, SQLException {
		SpringApplication.run(Application.class, args);
		// Start task executor.
		for (int i = 1; i <= Config.TASK_EXECUTOR_NUM; i++) {
			new Thread(new TaskExecutor()).run();
		}
	}
}