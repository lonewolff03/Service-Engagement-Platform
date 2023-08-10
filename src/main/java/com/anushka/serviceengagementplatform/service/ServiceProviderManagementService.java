package com.anushka.serviceengagementplatform.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ServiceProviderManagementService {
	private WebClient webClient;
	
	public ServiceProviderManagementService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
	}
	
	ParameterizedTypeReference<Map<String, Object>> responseType =
            new ParameterizedTypeReference<Map<String, Object>>() {};
            
    public Map<String, Integer> getServiceProviderId(String userId) {
    	int serviceProviderId = -1;
    	int messageCode = 5;
    	Map<String, Integer> returnMap = new HashMap<>();
    	
    	try {
    		ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
    	            .uri("/api/service-provider/service-provider-id?userId={userId}", userId)
    	            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    	            .retrieve()
    	            .toEntity(responseType)
    	            .block();
    		
    		Map<String, Object> resultMap = responseEntity.getBody();
    		serviceProviderId = (int) resultMap.get("serviceProviderId");
    		messageCode = (int) resultMap.get("messageCode");
    		
    		returnMap.put("serviceProviderId", serviceProviderId);
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
	
	public Map<String, Object> getDashboard(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> services;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/dashboard?serviceProviderId={serviceProviderId}", serviceProviderId)
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
	
	public int addServiceProviderService(Integer serviceId, Integer serviceProviderId, String authToken) {
	    Map<String, Integer> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("serviceId", serviceId);
		int messageCode = 5;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = ((RequestBodySpec) webClient.post()
		    		.uri("/api/service-provider/service/add")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken))
		            .bodyValue(requestBody)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		return messageCode;
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            return messageCode;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            return 5;
	        }
    	}
	    
	}
	
	public Map<String, Object> fetchServicesByProvider(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/service/possible?serviceProviderId={serviceProviderId}", serviceProviderId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		List<Map<String, Object>> services = (List<Map<String, Object>>) resultMap.get("services");
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
	
	public int logout(int serviceProviderId, String authToken) {
	    int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.delete()
		    		.uri("/api/service-provider/logout?serviceProviderId={serviceProviderId}", serviceProviderId)
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
	
	public int addServiceProviderServiceDescription(int serviceProviderId, int serviceId, String authToken) {
	    Map<String, Integer> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("serviceId", serviceId);
		
		int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = ((RequestBodySpec) webClient.post()
		    		.uri("/api/service-provider/service/add-description")
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
	
	public int addServiceProviderServicePlan(int serviceProviderId, int serviceId, String authToken) {
		Map<String, Integer> requestBody = new HashMap<>();
		//Need to make updates to the requestBody
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("serviceId", serviceId);
		int messageCode = 5;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = ((RequestBodySpec) webClient.post()
		    		.uri("/api/service-provider/service/plan/add")
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
	
	public int deleteServiceProviderServicePlan(int serviceProviderId, int serviceId, int planId, String authToken) {
		int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.delete()
		    		.uri("/api/service-provider/service/plan/delete?serviceProviderId={serviceProviderId}&serviceID={serviceId}&planId={planId}", serviceProviderId, serviceId, planId)
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
	
	public int deleteServiceProviderService(int serviceProviderId, int serviceId, String authToken) {
		int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.delete()
		    		.uri("/api/service-provider/service/delete?serviceProviderId={serviceProviderId}&serviceId={serviceId}", serviceProviderId, serviceId)
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
	
	public Map<String, Object> getAllAppointments(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> appointments;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/appointments?serviceProviderId={serviceProviderId}", serviceProviderId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		appointments = (List<Map<String, Object>>) resultMap.get("appointments");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("appointments", appointments);
    		
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
	
	public Map<String, Object> getPastAppointments(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> appointments;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/appointments/past?serviceProviderId={serviceProviderId}", serviceProviderId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		appointments = (List<Map<String, Object>>) resultMap.get("appointments");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("appointments", appointments);
    		
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
	
	public Map<String, Object> getUpcomingAppointments(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> appointments;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/appointments/future?serviceProviderId={serviceProviderId}", serviceProviderId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		appointments = (List<Map<String, Object>>) resultMap.get("appointments");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("appointments", appointments);
    		
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
	
	public int acceptAppointment(int serviceProviderId, int appointmentId, String authToken) {
		int messageCode = 5;
		Map<String, Integer> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("appointmentId", appointmentId);
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.put()
		    		.uri("/api/service-provider/appointments/accept")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.bodyValue(requestBody)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		return messageCode;
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            return messageCode;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return messageCode;
	}
	
	public int declineAppointment(int serviceProviderId, int appointmentId, String authToken) {
		int messageCode = 5;
		Map<String, Integer> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("appointmentId", appointmentId);
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.put()
		    		.uri("/api/service-provider/appointments/decline")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.bodyValue(requestBody)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		return messageCode;
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            return messageCode;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return messageCode;
	}
	
	public int rescheduleAppointment(int serviceProviderId, int appointmentId, String newDateTime, String authToken) {
		int messageCode = 5;
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("appointmentId", appointmentId);
		requestBody.put("newDateTime", newDateTime);
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.put()
		    		.uri("/api/service-provider/appointments/reschedule")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.bodyValue(requestBody)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		return messageCode;
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            return messageCode;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return messageCode;
	}
	
	public Map<String, Object> getProfileDetails(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> profileDetails;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/details?serviceProviderId={serviceProviderId}", serviceProviderId)
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
	
	public int updateProfileDetails(int serviceProviderId, String authToken, Map<String, Object> profileDetails) {
		int messageCode = 5;
		profileDetails.put("serviceProviderId", serviceProviderId);
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.put()
		    		.uri("/api/service-provider/details/update")
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
	
	public int changePassword(int serviceProviderId, String newPassword, String authToken) {
		int messageCode = 5;
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("newPassword", newPassword);
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.put()
		    		.uri("/api/service-provider/password/update")
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.bodyValue(requestBody)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		return messageCode;
    		
		} catch (WebClientResponseException e){
    		String errorResponseJsonString = e.getResponseBodyAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			
			Map<String, Object> errorResponse;
			try {
	            errorResponse = objectMapper.readValue(errorResponseJsonString, new TypeReference<Map<String, Object>>() {});
	            messageCode = (int) errorResponse.get("messageCode");
	            return messageCode;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
    	}
		
		return messageCode;
	}
	
	public Map<String, Object> getReviews(int serviceProviderId, int serviceId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> reviews;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/reviews?serviceProviderId={serviceProviderId}&serviceId={serviceId}", serviceProviderId, serviceId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		reviews = (Map<String, Object>) resultMap.get("reviews");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("details", reviews);
    		
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
	
	public Map<String, Object> getAllServiceProviderChats(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> reviews;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/chats?serviceProviderId={serviceProviderId}", serviceProviderId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		List<Map<String, Object>> chats = (List<Map<String, Object>>) resultMap.get("chats");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("chats", chats);
    		
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
	
	public Map<String, Object> getRecentServiceProviderChats(int serviceProviderId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> reviews;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/chats/recent?serviceProviderId={serviceProviderId}", serviceProviderId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		List<Map<String, Object>> chats = (List<Map<String, Object>>) resultMap.get("chats");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("chats", chats);
    		
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
	
	public Map<String, Object> getChatMessages(int serviceProviderId, int chatId, String authToken) {
		int messageCode = 5;
		Map<String, Object> returnMap = new HashMap<>();
		Map<String, Object> reviews;
		
		try {
			ResponseEntity<Map<String, Object>> responseEntity = webClient.get()
		    		.uri("/api/service-provider/messages?serviceProviderId={serviceProviderId}&chatId={chatId}", serviceProviderId)
		    		.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		    		.header("AuthToken", authToken)
		    		.retrieve()
		    		.toEntity(responseType)
		    		.block();

			Map<String, Object> resultMap = responseEntity.getBody();
    		messageCode = (int) resultMap.get("messageCode");
    		List<Map<String, Object>> chatMessages = (List<Map<String, Object>>) resultMap.get("messages");
    		returnMap.put("messageCode", messageCode);
    		returnMap.put("messages", chatMessages);
    		
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
	
	public int sendMessage(int serviceProviderId, int chatId, String authToken) {
	    Map<String, Integer> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("chatId", chatId);
		
		int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = ((RequestBodySpec) webClient.post()
		    		.uri("/api/service-provider/messages/send")
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
	
	public int startChat(int serviceProviderId, int customerId, String authToken) {
	    Map<String, Integer> requestBody = new HashMap<>();
		requestBody.put("serviceProviderId", serviceProviderId);
		requestBody.put("customerId", customerId);
		
		int messageCode = 5;
		try {
			ResponseEntity<Map<String, Object>> responseEntity = ((RequestBodySpec) webClient.post()
		    		.uri("/api/service-provider/chats/start")
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
}
