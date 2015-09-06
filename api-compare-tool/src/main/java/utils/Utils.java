package utils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import configs.Config;

/**
 * @author ruochen.xu
 */
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

	/**
	 * Convert a json array to int set.
	 * 
	 * @param arr
	 * @return Set string, null for empty array.
	 */
	public static String jsonArrayToIntSet(String arr) {
		if (arr.equals("[]")) {
			return null;
		} else {
			return "("
					+ arr.substring(1, arr.length() - 1).replaceAll("\"", "")
					+ ")";
		}
	}

	/**
	 * Get string body from http response.
	 * 
	 * @param response
	 * @return
	 */
	public static String getHttpResponseBody(HttpResponse response) {
		String body = "";
		try {
			body = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return body;
	}
}
