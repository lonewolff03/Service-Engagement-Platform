package com.anushka.serviceengagementplatform.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CustomerManagementService {
	private WebClient webClient;
	
	public CustomerManagementService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
	}
	
	ParameterizedTypeReference<Map<String, Object>> responseType =
            new ParameterizedTypeReference<Map<String, Object>>() {};
	
	public Map<String, Integer> getCustomerId(String userId) {
    	int customerId = -1;
    	int messageCode = 5;
    	Map<String, Integer> returnMap = new HashMap<>();
    	
    	try {
    		ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
    	            .uri("/api/customer/details?customerId={customerId}", userId)
    	            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    	            .retrieve()
    	            .toEntity(responseType)
    	            .block();
    		
    		Map<String, Object> resultMap = responseEntity.getBody();
    		customerId = (int) resultMap.get("customerId");
    		messageCode = (int) resultMap.get("messageCode");
    		
    		returnMap.put("serviceProviderId", customerId);
    		returnMap.put("userId", messageCode);
    		
    	} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            returnMap.put("messageCode", messageCode);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
    	
    	return returnMap;
    }
	
	public Map<String, Object> getCustomerDashboard(int customerId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> services;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/customer/dashboard?customerId={customerId}", customerId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		services = (List<Map<String, Object>>) resultMap.get("services");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("services", services);
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            returnMap.put("messageCode", messageCode);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return returnMap;
	}
	
	public int logout(int customerId, String authToken) {
	    int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.delete()
		    		.uri("/api/customer/logout?customerId={customerId}", customerId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");		
    		
		} catch (WebClientResponseException e){
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
	
	public Map<String, Object> getProfileDetails(int customerId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> profileDetails;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/customer/details?customerId={customerId}", customerId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		profileDetails = (Map<String, Object>) resultMap.get("details");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("details", profileDetails);
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            returnMap.put("messageCode", messageCode);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return returnMap;
	}
	
	public int updateProfileDetails(int customerId, String authToken, Map<String, Object> profileDetails) {
		int messageCode = 5;
		profileDetails.put("customerId", customerId);
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.put()
		    		.uri("/api/customer/details/update")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.bodyValue(profileDetails)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		
		} catch (WebClientResponseException e){
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
	
	public Map<String, Object> getPaymentMethods(int customerId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<Integer, String> paymentMethods;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/customer/payment-methods?customerId={customerId}", customerId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		paymentMethods = (Map<Integer, String>) resultMap.get("paymentMethods");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("paymentMethods", paymentMethods);
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            returnMap.put("messageCode", messageCode);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return returnMap;
	}
	
	public Map<String, Object> getSavedPaymentMethods(int customerId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<Integer, String> paymentMethods;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/customer/payment-methods/saved?customerId={customerId}", customerId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		paymentMethods = (Map<Integer, String>) resultMap.get("paymentMethods");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("paymentMethods", paymentMethods);
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            returnMap.put("messageCode", messageCode);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return returnMap;
	}
	
	public int addPaymentOption(int customerId, int paymentMethodId, String authToken) {
	    Map<String, Integer> requestBody = new HashMap<>();
		requestBody.put("customerId", customerId);
		requestBody.put("paymentMethodId", paymentMethodId);
		
		int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = ((RequestBodySpec) webClient.post()
		    		.uri("/api/customer/payment-methods/saved/add")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken))
		    		.bodyValue(requestBody)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");   		
    		
		} catch (WebClientResponseException e){
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
	
	public int removePaymentMethod(int customerId, int paymentMethodId, String authToken) {
	    int messageCode = 5;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.delete()
		    		.uri("/api/customer/payment-methods/saved/delete?customerId={customerId}&paymentMethodId={paymentMethodId}", customerId, paymentMethodId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		
		} catch (WebClientResponseException e){
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
	
	public Map<String, Object> getTransactionHistory(int customerId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> transactions;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/customer/transactions?customerId={customerId}", customerId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		transactions = (List<Map<String, Object>>) resultMap.get("transactions");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("transactions", transactions);
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            returnMap.put("messageCode", messageCode);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return returnMap;
	}
	
	public int changePassword(int customerId, String newPassword, String authToken) {
		int messageCode = 5;
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("customerId", customerId);
		requestBody.put("newPassword", newPassword);
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.put()
		    		.uri("/api/customer/password/update")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.bodyValue(requestBody)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		
		} catch (WebClientResponseException e){
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
	
	public Map<String, Object> getAvailableServices(int customerId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> services;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/customer/services/available?customerId={customerId}", customerId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		services = (List<Map<String, Object>>) resultMap.get("services");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("services", services);
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            returnMap.put("messageCode", messageCode);
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return returnMap;
	}
}
