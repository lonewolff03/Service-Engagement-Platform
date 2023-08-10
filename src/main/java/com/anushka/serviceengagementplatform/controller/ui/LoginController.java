package com.anushka.serviceengagementplatform.controller.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.anushka.serviceengagementplatform.model.UserLogin;
import com.anushka.serviceengagementplatform.service.UserService;

import jakarta.servlet.http.HttpSession;


@Controller
public class LoginController {
	private UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/userlogin")
    public ModelAndView loginPage(ModelMap model, HttpSession session) {
    	UserLogin userLogin = new UserLogin();
    	model.addAttribute("userLogin", userLogin);
    	
    	return new ModelAndView("userlogin", model);
    }
    
    @PostMapping("/userlogin")
    public ModelAndView userLogin(@ModelAttribute("userLogin") UserLogin userLogin, ModelMap model, HttpSession session) {
    	int messageCode = 0;
    	String userId = userLogin.getUserId();
    	String password = userLogin.getPassword();
    	
    	messageCode = userService.userLogin(userId, password, session);
    	
    	if(messageCode == 7){
    		session.setAttribute("statusMsg", "Please fill both fields");
    		return new ModelAndView("redirect:/userlogin", model);
    	}
    	
    	else if(messageCode == 3) {
    		session.setAttribute("statusMsg", "Wrong Password");
    		return new ModelAndView("redirect:/userlogin", model);
    	}
    	
    	else if(messageCode == 4){
    		session.setAttribute("statusMsg", "User is already logged in");
    		return new ModelAndView("redirect:/userlogin", model);
    	}
    	
    	else if (messageCode == 1) {
    		return new ModelAndView("redirect:/dashboard", model);
    	}
    	
    	else {
    		session.setAttribute("statusMsg", "Error logging in");
    		return new ModelAndView("redirect:/userlogin", model);
    	}
    }    
}
