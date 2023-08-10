package com.anushka.serviceengagementplatform.controller.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.anushka.serviceengagementplatform.model.Customer;
import com.anushka.serviceengagementplatform.model.ForgotPwd;
import com.anushka.serviceengagementplatform.model.OtpForm;
import com.anushka.serviceengagementplatform.model.ServiceProvider;

import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes({"statusMsg"})
public class HomeController {
	
	@GetMapping("/")
	public ModelAndView home(ModelMap model) {
		return new ModelAndView("home", model);
	}
	
	@GetMapping("/aboutus")
	public ModelAndView aboutus(ModelMap model) {
		return new ModelAndView("aboutus", model);
	}
	
	@GetMapping("/forgotpwd")
	public ModelAndView resetpwd(ModelMap model) {
		ForgotPwd forgotPwd = new ForgotPwd();
		model.addAttribute("forgotPwd", forgotPwd);
		return new ModelAndView("forgotpwd", model);
	}
}
