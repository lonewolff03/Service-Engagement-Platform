package com.anushka.serviceengagementplatform.controller.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.anushka.serviceengagementplatform.model.Customer;
import com.anushka.serviceengagementplatform.model.EnterNewPwd;
import com.anushka.serviceengagementplatform.model.ForgotPwd;
import com.anushka.serviceengagementplatform.model.OtpForm;
import com.anushka.serviceengagementplatform.model.ServiceProvider;
import com.anushka.serviceengagementplatform.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes({"userId"})
public class RegistrationController {

    private UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/signup1")
	public ModelAndView signup1(ModelMap model) {
		Customer customer = new Customer();
		Map<String, String> countriesMap = userService.getCountryList();
		model.addAttribute("countriesMap", countriesMap);
		model.addAttribute("customer", customer);
		return new ModelAndView("signup1", model);
	}
	
	@GetMapping("/signup2")
	public ModelAndView signup2(ModelMap model) {
		ServiceProvider serviceProvider = new ServiceProvider();
		Map<String, String> countriesMap = userService.getCountryList();
		Map<String, String> industryMap = userService.getIndustryList();
		
		model.addAttribute("industryMap", industryMap);
		model.addAttribute("countriesMap", countriesMap);
		model.addAttribute("serviceProvider", serviceProvider);
		return new ModelAndView("signup2", model);
	}
    
    @PostMapping("/signup1")
    public ModelAndView enterCustomerDetails(@ModelAttribute("customer") Customer customer, ModelMap model, HttpSession session) {
    	int messageCode = userService.registerCustomer(customer);
    	
    	if(messageCode == 1) {
			model.addAttribute("userId", customer.getUserId());
			
			int messageCode2 = userService.generateOTP(customer.getUserId());
			
			if(messageCode2 == 1) {
				session.setAttribute("otpPurpose", "register");
				return new ModelAndView("redirect:/otpform", model);
			}
			
			else {
				session.setAttribute("registerStatusMsg", "Error verifying your credentials");
				return new ModelAndView("redirect:/signup1", model);
			}
		}
		
    	else if(messageCode == 2) {    		
    		session.setAttribute("registerStatusMsg", "This username already exists");
    		
			return new ModelAndView("redirect:/signup1", model);
		}
		
		else {
			session.setAttribute("statusMsg", "Error registering your account");
			
            return new ModelAndView("redirect:/login", model);
		}
    }
    
    @PostMapping("/signup2")
    public ModelAndView enterServiceProviderDetails(@ModelAttribute("serviceProvider") ServiceProvider serviceProvider, ModelMap model, HttpSession session) {
		int messageCode = userService.registerServiceProvider(serviceProvider);
		
		if(messageCode == 1) {
			model.addAttribute("userId", serviceProvider.getUserId());
			
			int messageCode2 = userService.generateOTP(serviceProvider.getUserId());
			
			if(messageCode2 == 1) {
				session.setAttribute("otpPurpose", "register");
				return new ModelAndView("redirect:/otpform", model);
			}
			
			else {
				session.setAttribute("registerStatusMsg", "Error verifying your credentials");
				return new ModelAndView("redirect:/signup2", model);
			}
		}
		
    	else if(messageCode == 2) {    		
    		session.setAttribute("registerStatusMsg", "This username already exists");
    		
			return new ModelAndView("redirect:/signup2", model);
		}
		
		else {
			session.setAttribute("statusMsg", "Error registering your account");
			
            return new ModelAndView("redirect:/login", model);
		}
	}
    
    @PostMapping("/forgotpwd")
    public ModelAndView forgotPassword(@ModelAttribute("forgotPwd") ForgotPwd forgotPwd, ModelMap model, HttpSession session) {
    	
    	model.addAttribute("userId", forgotPwd.getUserId());
    	session.setAttribute("otpPurpose", "forgotPwd");
    	int messageCode = userService.generateOTP(forgotPwd.getUserId());
    	
    	if(messageCode == 1) {
    		return new ModelAndView("redirect:/otpform", model);
    	}
    	
    	else if(messageCode == 6) {
    		session.setAttribute("otpStatusMsg", "Please enter a valid userId");
    		return new ModelAndView("redirect:/forgotpwd", model);
    	}
    	
    	else {
    		session.setAttribute("otpStatusMsg", "Error sending OTP");
    		return new ModelAndView("redirect:/forgotpwd", model);
    	}
    }
    
    @GetMapping("/otpform")
	public ModelAndView otpForm(ModelMap model) {
		OtpForm otpForm = new OtpForm();
		model.addAttribute("otpForm", otpForm);
		
		// To keep this as a session attribute
		String userId = (String) model.getAttribute("userId");
		model.addAttribute("userId", userId);
		
		return new ModelAndView("otpform", model);
	}
    
    @PostMapping("/otpform")
    public ModelAndView registerCustomer(@ModelAttribute("otpForm") OtpForm otpForm, ModelMap model, HttpSession session) {
    	String userId = (String) model.getAttribute("userId");
    	
    	int messageCode = userService.verifyOTP(userId, otpForm.getOtp());
    	
    	String otpPurpose = (String) session.getAttribute("otpPurpose");
    	    	
    	if(messageCode == 1) {
    		if(otpPurpose.equals("forgotPwd")) {
    			return new ModelAndView("redirect:/enternewpwd");
    		}
    		
    		else {
    			session.setAttribute("statusMsg", "Registration successful");
        		return new ModelAndView("redirect:/userlogin");
    		}
    	}
    	
    	else if (messageCode == 3) {
    		session.setAttribute("otpFormStatusMsg", "Invalid OTP");
    		return new ModelAndView("redirect:/otpform", model);
    	}
    	
    	else {
    		session.setAttribute("otpFormStatusMsg", "Error verifying OTP");
    		return new ModelAndView("redirect:/otpform", model);
    	}
    }
    
    @GetMapping("/enternewpwd")
    public ModelAndView newPwdForm(ModelMap model, HttpSession session) {
    	EnterNewPwd enterNewPwd = new EnterNewPwd();
    	
    	model.addAttribute("enterNewPwd", enterNewPwd);
    	return new ModelAndView("enternewpwd", model);
    }
    
    @PostMapping("/enternewpwd")
    public ModelAndView setNewPwd(@ModelAttribute("enterNewPwd") EnterNewPwd enterNewPwd, ModelMap model, HttpSession session) {
    	String userId = enterNewPwd.getUserId();
    	String newPwd = enterNewPwd.getNewPwd();
    	
    	int messageCode = userService.setNewPwd(userId, newPwd);
    	
    	if(messageCode == 7) {
    		session.setAttribute("statusMsgNewPwd", "Please fill both fields");
    		return new ModelAndView("redirect:/enternewpwd");
    	}
    	
    	else if(messageCode == 1) {
    		session.setAttribute("statusMsg", "Password reset successful");
    		return new ModelAndView("redirect:/userlogin");
    	}
    	
    	else {
    		session.setAttribute("statusMsgNewPwd", "Failed to update password");
    		return new ModelAndView("redirect:/enternewpwd");
    	}
    }
}