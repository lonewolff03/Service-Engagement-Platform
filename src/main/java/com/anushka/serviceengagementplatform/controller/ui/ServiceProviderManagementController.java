package com.anushka.serviceengagementplatform.controller.ui;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anushka.serviceengagementplatform.service.ServiceProviderManagementService;

@Controller
public class ServiceProviderManagementController {
	private ServiceProviderManagementService serviceProviderManagementService;

    @Autowired
    public ServiceProviderManagementController(ServiceProviderManagementService serviceProviderManagementService) {
        this.serviceProviderManagementService = serviceProviderManagementService;
    }
    
    @GetMapping("/external")
    public void callExternalAPI() {
    	Map<String, Integer> myMap = serviceProviderManagementService.getServiceProviderId("provider1");
    	System.out.println(myMap);
    }
}
