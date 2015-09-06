package configs;

/**
 * @author ruochen.xu
 */
public class Config {
	// Database.
	public static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	public static final String DB_ADDRESS = "jdbc:mysql://10.128.10.174:3306/api_compare_tool";
	public static final String DB_USER = "search";
	public static final String DB_PASSWD = "search";

	// Task.
	public static final String HDFS_PATH = "http://10.16.60.114:50070/webhdfs/v1/user/search/:id.txt?op=OPEN&user.name=search";
	public static final int TASK_EXECUTOR_NUM = 2;
	public static final int TASK_SLEEP_TIME = 10 * 1000;
	public static final int REQUESTS_LIMIT = 10;

	// Cookies.
	public static final String USER_DATA = "user_data";
	public static final String USER_NAME = "user_name";
}
