package utils;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;

public class Utils {
	/**
	 * Get user login name from http request.
	 * 
	 * @param request
	 * @return User's name.
	 */
	public static String getUserName(HttpServletRequest request) {
		return ((JsonObject) request.getAttribute("user_data")).get("username")
				.getAsString();
	}

	public static String jsonArrayToIntSet(String a) {
		if (a.equals("[]")) {
			return null;
		} else {
			return "(" + a.substring(1, a.length() - 1).replaceAll("\"", "")
					+ ")";
		}
	}
	public static int getRandomInt(int min, int max) {
		Random r = new Random();
		return r.nextInt(max - min + 1) + min;
	}
}
