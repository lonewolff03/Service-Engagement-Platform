//package com.anushka.serviceengagementplatform.controller.ui;
//
//import org.springframework.boot.web.servlet.error.ErrorController;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.ModelMap;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.ModelAndView;
//
//import jakarta.servlet.http.HttpSession;
//
//@Controller
//public class ErrorPageController implements ErrorController{
//	
//	@PostMapping("/error")
//	public ModelAndView redirectFromError(ModelMap model, HttpSession session) {
//		String fromPage = (String) session.getAttribute("fromPage");
//		
//		if(fromPage.equals("custLogin")) {
//			session.setAttribute("statusMsg", "Error logging in");
//			return new ModelAndView("redirect:/custlogin", model);
//		}
//		return new ModelAndView("redirect:/errorpage", model);
//	}
//}
