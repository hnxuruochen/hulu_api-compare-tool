package controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Static apis.
 * @author ruochen.xu
 *
 */
@Controller
public class WebController {
	@RequestMapping("/")
	public String index() {
		return "layout.html";
	}
}