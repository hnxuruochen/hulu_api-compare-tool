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
	@RequestMapping("/")
	public String index() {
		return "layout.html";
	}

	@RequestMapping("/error")
	public String error() {
		return "redirect:/#/404";
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
}