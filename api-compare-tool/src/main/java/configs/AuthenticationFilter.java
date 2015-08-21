package configs;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;

import com.google.gson.JsonObject;
import com.hulu.sso.HuluAuthException;
import com.hulu.sso.Sso;

@Configuration
public class AuthenticationFilter implements Filter {
	public static final String LOGIN_SERVER = "https://login.hulu.com";
	private static List<String> publicApi = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Initialize public api that needn't authentication.
		publicApi = new ArrayList<String>();
		publicApi.add("/api/tasks/get_creator");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		if (!publicApi.contains(httpRequest.getRequestURI())) {
			String ssoData = "";
			String ssoSig = "";
			// Get sso data and sso sig.
			Cookie[] cookies = httpRequest.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("hulu_sso_data")) {
						ssoData = cookie.getValue();
					}
					if (cookie.getName().equals("hulu_sso_sig")) {
						ssoSig = cookie.getValue();
					}
				}
			}
			JsonObject data = null;
			// Verify data.
			try {
				data = Sso.verifyCookie(ssoData, ssoSig, null, LOGIN_SERVER,
						null);
			} catch (HuluAuthException e) {
				httpResponse
						.sendRedirect(LOGIN_SERVER
								+ "/?redirect="
								+ URLEncoder.encode(
										"http://" + httpRequest.getServerName()
												+ ":"
												+ httpRequest.getServerPort()
												+ httpRequest.getRequestURI(),
										"UTF-8"));
				return;
			}
			// Record data.
			request.setAttribute(Config.USER_DATA, data);
			request.setAttribute(Config.USER_NAME, data.get("username")
					.getAsString());
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
