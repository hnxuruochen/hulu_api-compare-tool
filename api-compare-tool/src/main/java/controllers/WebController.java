package controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import configs.Config;

/**
 * Static apis.
 * 
 * @author ruochen.xu
 *
 */
@Controller
public class WebController implements ErrorController {
	/**
	 * Get user's login info.
	 * 
	 * @return Json string of user info.
	 */
	@RequestMapping("/api/user/info")
	@ResponseBody
	public String getUserInfo(HttpServletRequest request) {
		return request.getAttribute(Config.USER_DATA).toString();
	}

	/**
	 * Return layout template for all unknown urls and frontend will route.
	 * 
	 * @return layout template.
	 */
	@RequestMapping("/error")
	public String error() {
		// If frontend's html5mode is disabled, just add the # tag and redirect.
		// return "redirect:/#" + request.getRequestURI();
		return "layout.html";
	}

	/**
	 * Useless, but must override it for changing error page.
	 */
	@Override
	public String getErrorPath() {
		return "/error";
	}
}