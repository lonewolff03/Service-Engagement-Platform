package com.anushka.serviceengagementplatform.controller.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.anushka.serviceengagementplatform.model.NewServiceProviderPassword;
import com.anushka.serviceengagementplatform.model.NewServiceProviderService;
import com.anushka.serviceengagementplatform.model.ServiceProviderDetails;
import com.anushka.serviceengagementplatform.service.ServiceProviderManagementService;
import com.anushka.serviceengagementplatform.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {
	private ServiceProviderManagementService serviceProviderManagementService;
	private UserService userService;

    @Autowired
    public DashboardController(ServiceProviderManagementService serviceProviderManagementService, UserService userService) {
        this.serviceProviderManagementService = serviceProviderManagementService;
        this.userService = userService;
    }
	
	@GetMapping("/dashboard")
	public ModelAndView showDashboard(ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		
		Map<String, Object> availableServicesMap = serviceProviderManagementService.fetchServicesByProvider(serviceProviderId, authToken);
		
		Map<String, Object> dashboardMap = serviceProviderManagementService.getDashboard(serviceProviderId, authToken);
		
		List<Map<String, Object>> services = (List<Map<String, Object>>) dashboardMap.get("services");
		int messageCode = (int) dashboardMap.get("messageCode");
		
		List<Map<String, Object>> availableServices = (List<Map<String, Object>>) ((serviceProviderManagementService.fetchServicesByProvider(serviceProviderId, authToken)).get("services"));
		
		if(messageCode == 4) {
			session.setAttribute("statusMsg", "Please Login");
			return new ModelAndView("redirect:/userlogin", model);
		}
		
		else if(messageCode == 1) {
			NewServiceProviderService newServiceProviderService = new NewServiceProviderService();
			
			session.setAttribute("dashboardServices", services);
			model.addAttribute("newServiceProviderService", newServiceProviderService);
			model.addAttribute("availableServices", availableServices);
			return new ModelAndView("service-provider/dashboard", model);
		}
		
		else {
			session.setAttribute("statusMsg", "Internal Server Error");
			return new ModelAndView("redirect:/userlogin", model);
		}
	}
	
	@PostMapping("/add-new-sp-service")
	public ModelAndView addServiceProviderService(@ModelAttribute("newServiceProviderService") NewServiceProviderService newServiceProviderService, ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		
		int serviceId = newServiceProviderService.getServiceId();
		
		int messageCode = serviceProviderManagementService.addServiceProviderService(serviceId, serviceProviderId, authToken);
		
		if(messageCode == 4) {
			session.setAttribute("statusMsg", "Please Login");
			return new ModelAndView("redirect:/userlogin", model);
		}
		
		if(messageCode == 1) {
			return new ModelAndView("redirect:/dashboard", model);
		}
		
		else {
			return new ModelAndView("redirect:/dashboard", model);
		}
	}
	
	@PostMapping("/delete-sp-service")
	public ModelAndView deleteServiceProviderService(@ModelAttribute("newServiceProviderService") NewServiceProviderService newServiceProviderService, ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		 
		int serviceId = newServiceProviderService.getServiceId();
		
		int messageCode = serviceProviderManagementService.deleteServiceProviderService(serviceProviderId, serviceId, authToken);
		
		if(messageCode == 4) {
			session.setAttribute("statusMsg", "Please Login");
			return new ModelAndView("redirect:/userlogin", model);
		}
		
		if(messageCode == 1) {
			return new ModelAndView("redirect:/dashboard", model);
		}
		
		else {
			return new ModelAndView("redirect:/dashboard", model);
		}
	}
	
	@GetMapping("/logout")
	public ModelAndView userLogout(ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		
		int messageCode = serviceProviderManagementService.logout(serviceProviderId, authToken);
		
		if(messageCode == 1) {
			session.removeAttribute("authToken");
			session.removeAttribute("userId");
			session.removeAttribute("role");
			return new ModelAndView("redirect:/userlogin", model);
		}
		
		else {
			session.setAttribute("statusMsg", "Please Login");
			return new ModelAndView("redirect:/userlogin", model);
		}
	}
	
	@GetMapping("/chats")
	public ModelAndView showChatPage(ModelMap model) {
		return new ModelAndView("service-provider/chats", model);
	}
	
	@GetMapping("/profile-settings")
	public ModelAndView showProfileSettings(ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		
		ServiceProviderDetails serviceProviderDetails = new ServiceProviderDetails();
		
		NewServiceProviderPassword newServiceProviderPassword = new NewServiceProviderPassword();
		
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		
		Map<String, Object> detailsMap = serviceProviderManagementService.getProfileDetails(serviceProviderId, authToken);
		Map<String, Object> profileDetails = (Map<String, Object>) detailsMap.get("details");
		
		Map<String, String> industryList = userService.getIndustryList();
		String industry = industryList.get(Integer.toString((int)profileDetails.get("industryId")));
		
		Map<String, String> countryList = userService.getCountryList();
		String country = countryList.get(Integer.toString((int)profileDetails.get("countryCode")));
		
		Map<String, String> stateList = userService.getStateList(country);
		String state = stateList.get(Integer.toString((int)profileDetails.get("stateCode")));
		
		Map<String, String> cityList = userService.getCityList(state);
		String city = cityList.get(Integer.toString((int)profileDetails.get("cityCode")));
		
		profileDetails.put("industry", industry);
		profileDetails.put("country", country);
		profileDetails.put("state", state);
		profileDetails.put("city", city);
		
		model.addAttribute("profileDetails", profileDetails);
		model.addAttribute("industryMap", industryList);
		model.addAttribute("countriesMap", countryList);
		
		serviceProviderDetails.setCompanyName((String) profileDetails.get("companyName"));
		serviceProviderDetails.setAddress((String) profileDetails.get("address"));	
		serviceProviderDetails.setIndustryId((int) profileDetails.get("industryId"));
		serviceProviderDetails.setCorporateIdentificationNumber((String) profileDetails.get("corporateIdentificationNumber"));
		serviceProviderDetails.setBusinessType((String) profileDetails.get("businessType"));
		serviceProviderDetails.setCountryCode((int) profileDetails.get("countryCode"));
		serviceProviderDetails.setStateCode((int) profileDetails.get("stateCode"));
		serviceProviderDetails.setCityCode((int) profileDetails.get("cityCode"));
		serviceProviderDetails.setPinCode(Integer.valueOf((String) profileDetails.get("pinCode")));
		serviceProviderDetails.setEmailAddress((String) profileDetails.get("emailAddress"));
		
		model.addAttribute("serviceProviderDetails", serviceProviderDetails);
		model.addAttribute("newServiceProviderPassword", newServiceProviderPassword);
		
		return new ModelAndView("service-provider/profile-settings", model);
	}
	
	@PostMapping("/update-sp-details")
	public ModelAndView changeProfileDetails(@ModelAttribute("serviceProviderDetails") ServiceProviderDetails serviceProviderDetails, ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		
		Map<String, Object> profileDetails = new HashMap<String, Object>();
		profileDetails.put("companyName", serviceProviderDetails.getCompanyName());
		profileDetails.put("industryId", serviceProviderDetails.getIndustryId());
		profileDetails.put("corporateIdentificationNumber", serviceProviderDetails.getCorporateIdentificationNumber());
		profileDetails.put("businessType", serviceProviderDetails.getBusinessType());
		profileDetails.put("address", serviceProviderDetails.getAddress());
		profileDetails.put("countryCode", serviceProviderDetails.getCountryCode());
		profileDetails.put("stateCode", serviceProviderDetails.getStateCode());
		profileDetails.put("cityCode", serviceProviderDetails.getCityCode());
		profileDetails.put("pinCode", Integer.toString(serviceProviderDetails.getPinCode()));
		profileDetails.put("emailAddress", serviceProviderDetails.getEmailAddress());
		
		int messageCode = serviceProviderManagementService.updateProfileDetails(serviceProviderId, authToken, profileDetails);
		
		if(messageCode == 4) {
			session.setAttribute("statusMsg", "Please Login");
			return new ModelAndView("redirect:/userlogin", model);
		}
		
		if(messageCode == 1) {
			return new ModelAndView("redirect:/profile-settings", model);
		}
		
		else {
			return new ModelAndView("redirect:/profile-settings", model);
		}
	}
	
	@PostMapping("/update-sp-password")
	public ModelAndView changePassword(@ModelAttribute("newServiceProviderPassword") NewServiceProviderPassword newServiceProviderPassword, ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		
		String newPassword = newServiceProviderPassword.getNewPassword();
		
		int messageCode = serviceProviderManagementService.changePassword(serviceProviderId, newPassword, authToken);
		
		if(messageCode == 4) {
			session.setAttribute("statusMsg", "Please Login");
			return new ModelAndView("redirect:/userlogin", model);
		}
		
		if(messageCode == 1) {
			return new ModelAndView("redirect:/profile-settings", model);
		}
		
		else {
			return new ModelAndView("redirect:/profile-settings", model);
		}
	}
	
	@GetMapping("/contact")
	public ModelAndView showContactPage(ModelMap model) {
		return new ModelAndView("service-provider/contact", model);
	}
	
	@GetMapping("/appointments")
	public ModelAndView showAppointments(ModelMap model, HttpSession session) {
		String authToken = (String) session.getAttribute("authToken");
		String userId = (String) session.getAttribute("userId");
		
		Map<String, Integer> resultMap = serviceProviderManagementService.getServiceProviderId(userId);
		
		int serviceProviderId = resultMap.get("serviceProviderId");
		if(Objects.isNull(authToken)) {
			authToken = "None";
		}
		
		Map<String, Object> appointmentsMap = serviceProviderManagementService.getAllAppointments(serviceProviderId, authToken);
		List<Map<String, Object>> appointments = (List<Map<String, Object>>) appointmentsMap.get("appointments");
		int messageCode1 = (int) appointmentsMap.get("messageCode");
		
		Map<String, Object> pastAppointmentsMap = serviceProviderManagementService.getPastAppointments(serviceProviderId, authToken);
		List<Map<String, Object>> pastAppointments = (List<Map<String, Object>>) pastAppointmentsMap.get("appointments");
		int messageCode2 = (int) pastAppointmentsMap.get("messageCode");
		
		Map<String, Object> upcomingAppointmentsMap = serviceProviderManagementService.getPastAppointments(serviceProviderId, authToken);
		List<Map<String, Object>> upcomingAppointments = (List<Map<String, Object>>) upcomingAppointmentsMap.get("appointments");
		int messageCode3 = (int) upcomingAppointmentsMap.get("messageCode");
		
		if(messageCode1 == 4) {
			session.setAttribute("statusMsg", "Please Login");
			return new ModelAndView("redirect:/userlogin", model);
		}
		
		else if(messageCode1 == 1) {
			NewServiceProviderService newServiceProviderService = new NewServiceProviderService();
			
			session.setAttribute("appointments", appointments);
			session.setAttribute("pastAppointments", pastAppointments);
			session.setAttribute("upcomingAppointments", upcomingAppointments);
			
			return new ModelAndView("service-provider/appointments", model);
		}
		
		else {
			session.setAttribute("statusMsg", "Internal Server Error");
			return new ModelAndView("redirect:/userlogin", model);
		}
	}
}
