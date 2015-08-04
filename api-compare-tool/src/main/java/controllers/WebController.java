package controllers;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Static apis.
 * 
 * @author ruochen.xu
 *
 */
@Controller
public class WebController implements ErrorController {
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