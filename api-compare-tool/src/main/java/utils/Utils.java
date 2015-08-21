package utils;

import javax.servlet.http.HttpServletRequest;

import configs.Config;

public class Utils {
	/**
	 * Get user login name from http request.
	 * 
	 * @param request
	 * @return User's name.
	 */
	public static String getUserName(HttpServletRequest request) {
		return request.getAttribute(Config.USER_NAME).toString();
	}

	public static String jsonArrayToIntSet(String a) {
		if (a.equals("[]")) {
			return null;
		} else {
			return "(" + a.substring(1, a.length() - 1).replaceAll("\"", "")
					+ ")";
		}
	}
}
