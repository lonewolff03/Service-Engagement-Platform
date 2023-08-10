package com.anushka.serviceengagementplatform.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.anushka.serviceengagementplatform.model.Customer;
import com.anushka.serviceengagementplatform.model.ServiceProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpRequest;
import jakarta.servlet.http.HttpSession;
import reactor.core.publisher.Mono;

@Service
public class UserService {
	private WebClient webClient;
	
	ParameterizedTypeReference<Map<String, Object>> responseType =
            new ParameterizedTypeReference<Map<String, Object>>() {};
	
	public UserService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }
	
	public Map<String,String> getIndustryList() {
		String jsonString = webClient.get()
                .uri("/api/industries")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> resultMap = new HashMap<>();
        Map<String, String> resultList = new HashMap<>();
        
        try {
            resultMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
            resultList = (Map<String, String>) resultMap.get("industries");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		
        return resultList;
    }
	
	public Map<String,String> getCountryList() {
		String jsonString = webClient.get()
                .uri("/api/countries")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> resultMap = new HashMap<>();
        Map<String, String> resultList = new HashMap<>();
        
        try {
            resultMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
            resultList = (Map<String, String>) resultMap.get("countries");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		
        return resultList;
    }
	
	// Calling API to get list of states
	public Map<String, String> getStateList(String country) {
		String jsonString = webClient.get()
				.uri("/api/states?country=" + country)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, String> resultList = new HashMap<>();
        
        try {
            resultMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
            resultList = (Map<String, String>)resultMap.get("states");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		
        return resultList;
    }
		
	// Calling API to get list of cities
	public Map<String, String> getCityList(String state) {
		String jsonString = webClient.get()
				.uri("/api/cities?state=" + state)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
		
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, String> resultList = new HashMap<>();
        
        try {
            resultMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
            resultList = (Map<String, String>)resultMap.get("cities");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		
        return resultList;
    }
	
	public int generateOTP(String userId) {		
		Map<String, String> requestBody = new HashMap<>();
		
		requestBody.put("userId", userId);
		ResponseEntity<Map<String,Object>> responseEntity;
		int messageCode = 0;
		
		try {
			responseEntity = webClient.post()
		            .uri("/api/otp/generate")
		            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .bodyValue(requestBody)
		            .retrieve()
		            .toEntity(responseType)
		            .block();
			
			Map<String, Object> responseBody = responseEntity.getBody();
			messageCode = (int) responseBody.get("messageCode");
			
		} catch (WebClientResponseException e) {
			String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
		
		return messageCode;
	}
	
	public int verifyOTP(String userId, String otp) {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("userId", userId);
		requestBody.put("otp", otp);
		
		int messageCode = 5;
		
		ResponseEntity<Map<String,Object>> responseEntity;
		
		try {
			responseEntity = webClient.post()
		            .uri("/api/otp/verify")
		            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .bodyValue(requestBody)
		            .retrieve()
		            .toEntity(responseType)
		            .block();
			
			if (responseEntity != null && responseEntity.getBody() != null) {
		        Map<String, Object> responseBody = responseEntity.getBody();
		        messageCode = (int) responseBody.get("messageCode");
		    }
			
		} catch (WebClientResponseException e) {
			String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
		
		return messageCode;
	}
	
	// Calling the API to register the customer
	public int registerCustomer(Customer customer) {
		int messageCode = 5;
		
		ResponseEntity<Map<String,Object>> responseEntity; 
		
		try {
			responseEntity = webClient.post()
		            .uri("/api/customer/add")
		            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .bodyValue(customer)
		            .retrieve()
		            .toEntity(responseType)
		            .block();
			
			Map<String, Object> responseBody = responseEntity.getBody();
	        messageCode = (int) responseBody.get("messageCode");
	        
		} catch (WebClientResponseException e) {
			String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
		
		return messageCode;
	}
	
	// Calling the API to register the service provider
	public int registerServiceProvider(ServiceProvider serviceProvider) {
		int messageCode = 5;
		
		ResponseEntity<Map<String,Object>> responseEntity;
		
		try {
			responseEntity = webClient.post()
		            .uri("/api/service-provider/add")
		            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .bodyValue(serviceProvider)
		            .retrieve()
		            .toEntity(responseType)
		            .block();
			
			Map<String, Object> responseBody = responseEntity.getBody();
	        messageCode = (int) responseBody.get("messageCode");
	        
		} catch (WebClientResponseException e) {
			String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
		
		return messageCode;
	}
	
	public int setNewPwd(String userId, String newPwd) {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("userId", userId);
		requestBody.put("newPassword", newPwd);
		
		int messageCode = 5;
		
		if(userId.equals("") || newPwd.equals("")) {
			messageCode = 7;
			return messageCode;
		}	
		
		try {
			ResponseEntity<Map<String,Object>> responseEntity = webClient.put()
		            .uri("/api/password/update")
		            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .bodyValue(requestBody)
		            .retrieve()
		            .toEntity(responseType)
		            .block();
			
			Map<String, Object> responseBody = responseEntity.getBody();
			messageCode = (int) responseBody.get("messageCode");
		} catch (WebClientResponseException e) {
			String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
				
		return messageCode;
	}
	
	public int userLogin(String userId, String password, HttpSession session) {
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("userId", userId);
		requestBody.put("password", password);
		int messageCode = 5;
		
		if(userId.equals("") || password.equals("")) {
			messageCode = 7;
			return messageCode;
		}
		
		ResponseEntity<Map<String, Object>> responseEntity;
		
		try {
			responseEntity = webClient.post()
		            .uri("/api/login")
		            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		            .bodyValue(requestBody)
		            .retrieve()
		            .toEntity(responseType)
		            .block();
			
			Map<String, Object> responseBody = responseEntity.getBody();
			messageCode = (int) responseBody.get("messageCode");
			
			if(messageCode == 1) {
				session.setAttribute("authToken", (String)responseBody.get("authToken"));
				session.setAttribute("userRole", (String)responseBody.get("role"));
				session.setAttribute("userId", userId);
			}
			
		} catch (WebClientResponseException e) {
			String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
				String statusMessage = (String) errorResponse.get("message");
				System.out.println(statusMessage);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
		
		return messageCode;
	}
}
