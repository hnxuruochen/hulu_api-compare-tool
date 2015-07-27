package configs;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

public class WebAppInitializer implements WebApplicationInitializer {

	public static final String LOGIN_SERVER = "https://login.hulu.com";

	@Override
	public void onStartup(ServletContext servletContext)
			throws ServletException {
		// Configure filter.
		FilterRegistration.Dynamic authFilter = servletContext.addFilter(
				"authFilter", new AuthenticationFilter());
		authFilter.addMappingForUrlPatterns(null, false, "/");
		authFilter.addMappingForUrlPatterns(null, false, "/layout.html");
	}

}
