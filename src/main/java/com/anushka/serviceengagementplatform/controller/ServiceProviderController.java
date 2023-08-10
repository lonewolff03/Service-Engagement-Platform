package com.anushka.serviceengagementplatform.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/service-provider")
public class ServiceProviderController {

    private final HikariDataSource dataSource;

    public ServiceProviderController(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/service-provider-id")
    public ResponseEntity<Map<String, Object>> getServiceProviderIdByUserId(@RequestParam("userId") String userId) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the user exists in the Users table
            if (!userExists(connection, userId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "User not found");
            }

            // Check if the user is a service provider
            if (!isServiceProvider(connection, userId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 4, "User is not a service provider");
            }

            // Get the service provider ID for the user
            int serviceProviderId = getServiceProviderId(connection, userId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("serviceProviderId", serviceProviderId);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/dashboard")
    private ResponseEntity<Map<String, Object>> getServiceProviderDashboard(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the service provider's services
            List<Map<String, Object>> services = getServiceProviderServices(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("services", services);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/service/add")
    private ResponseEntity<Map<String, Object>> addServiceProviderService(
            @RequestBody Map<String, Integer> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if(requestBody == null)
                throw new HttpMessageNotReadableException("Request body is missing");
            // Extract the service provider ID and service ID from the request body
            int serviceProviderId = requestBody.get("serviceProviderId");
            int serviceId = requestBody.get("serviceId");
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service provider and service ID pair already exists in ServiceProviderServices
            if (serviceProviderServiceExists(connection, serviceProviderId, serviceId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Service provider and service ID pair already exists");
            }

            // Insert the service provider and service ID pair into ServiceProviderServices
            insertServiceProviderService(connection, serviceProviderId, serviceId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.CREATED, 1, "Service added successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/service/possible")
    private ResponseEntity<Map<String, Object>> getServicesByProvider(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Service Provider not found");
            }

            // Retrieve the industry ID for the service provider
            int industryId = getProviderIndustryId(connection, serviceProviderId);
            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the services for the given industry
            List<Map<String, Object>> services = getServicesByIndustryId(connection, industryId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("services", services);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @DeleteMapping("/logout")
    private ResponseEntity<Map<String, Object>> logoutServiceProvider(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Service Provider not found");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Delete the authentication token for the user
            deleteAuthToken(connection, userId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Logged out successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/service/add-description")
    private ResponseEntity<Map<String, Object>> addServiceProviderServiceDetails(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the service provider ID, service ID, and service description from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int serviceId = (int) requestBody.get("serviceId");
            String serviceDescription = (String) requestBody.get("serviceDescription");

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service provider and service ID pair exists in ServiceProviderServices
            if (!serviceProviderServiceExists(connection, serviceProviderId, serviceId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Service provider service not found");
            }

            // Insert the service provider, service ID, and service description into ServiceProviderServices
            insertServiceProviderServiceDescription(connection, serviceProviderId, serviceId, serviceDescription);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.CREATED, 1, "Service details added successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/service/plan/add")
    private ResponseEntity<Map<String, Object>> addServiceProviderServicePlan(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the service provider ID, service ID, plan description, price, and currency from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int serviceId = (int) requestBody.get("serviceId");
            String planDescription = (String) requestBody.get("planDescription");
            double price = (double) requestBody.get("price");
            String currency = (String) requestBody.get("currency");

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Insert the plan details into ServiceProviderServiceDetails
            insertServiceProviderServicePlan(connection, serviceProviderId, serviceId, planDescription, price, currency);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.CREATED, 1, "Plan added successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @DeleteMapping("/service/plan/delete")
    private ResponseEntity<Map<String, Object>> deleteServiceProviderServicePlan(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestParam("serviceId") int serviceId,
            @RequestParam("planId") int planId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service provider, service ID, and plan ID combination exists in ServiceProviderServiceDetails
            if (!servicePlanExists(connection, serviceProviderId, serviceId, planId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Plan does not exist for the service provider and service");
            }

            // Delete the service provider, service ID, and plan ID combination from ServiceProviderServiceDetails
            deletePlan(connection, serviceProviderId, serviceId, planId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Plan deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @DeleteMapping("/service/delete")
    private ResponseEntity<Map<String, Object>> deleteServiceProviderService(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestParam("serviceId") int serviceId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service provider and service ID pair exists in ServiceProviderServices
            if (!serviceProviderServiceExists(connection, serviceProviderId, serviceId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Service provider service not found");
            }

            // Delete the service provider and service ID pair from ServiceProviderServices
            deleteService(connection, serviceProviderId, serviceId);

            // Delete the service provider and service ID pair from ServiceProviderServiceDetails
            deleteServiceDetails(connection, serviceProviderId, serviceId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Service deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/appointments")
    private ResponseEntity<Map<String, Object>> getAppointmentsByServiceProviderId(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the appointments for the given service provider
            List<Map<String, Object>> appointments = getAppointments(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("appointments", appointments);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/appointments/past")
    public ResponseEntity<Map<String, Object>> getPastAppointmentsByServiceProviderId(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the past appointments for the service provider
            List<Map<String, Object>> pastAppointments = getPastAppointments(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("appointments", pastAppointments);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/appointments/future")
    public ResponseEntity<Map<String, Object>> getFutureAppointmentsByServiceProviderId(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the future appointments for the service provider
            List<Map<String, Object>> futureAppointments = getFutureAppointments(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("appointments", futureAppointments);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/appointments/accept")
    public ResponseEntity<Map<String, Object>> acceptAppointment(
            @RequestBody Map<String, Integer> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the service provider ID and appointment ID from the request body
            int serviceProviderId = requestBody.get("serviceProviderId");
            int appointmentId = requestBody.get("appointmentId");

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the appointment exists in the Appointments table
            if (!appointmentExists(connection, appointmentId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Appointment not found");
            }

            // Update the status of the appointment to "Accepted"
            updateAppointmentStatus(connection, appointmentId, "Accepted");

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Appointment accepted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/appointments/decline")
    public ResponseEntity<Map<String, Object>> declineAppointment(
            @RequestBody Map<String, Integer> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the service provider ID and appointment ID from the request body
            int serviceProviderId = requestBody.get("serviceProviderId");
            int appointmentId = requestBody.get("appointmentId");

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the appointment exists in the Appointments table
            if (!appointmentExists(connection, appointmentId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Appointment not found");
            }

            // Update the status of the appointment to "Declined"
            updateAppointmentStatus(connection, appointmentId, "Declined");

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Appointment declined successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/appointments/reschedule")
    public ResponseEntity<Map<String, Object>> rescheduleAppointment(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the service provider ID, appointment ID, and new date-time from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int appointmentId = (int) requestBody.get("appointmentId");
            LocalDateTime newDateTime = LocalDateTime.parse((String) requestBody.get("newDateTime"));

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the appointment exists in the Appointments table
            if (!appointmentExists(connection, appointmentId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Appointment not found");
            }

            // Update the Date_Time for the appointment in the Appointments table
            updateAppointmentDateTime(connection, appointmentId, newDateTime);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Appointment rescheduled successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getServiceProviderDetails(@RequestParam("serviceProviderId") int serviceProviderId,
                                                                         @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the service provider details
            Map<String, Object> serviceProviderDetails = fetchServiceProviderDetails(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("details", serviceProviderDetails);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/details/update")
    public ResponseEntity<Map<String, Object>> updateServiceProviderDetails(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the service provider ID from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Service Provider not found");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Update the service provider details
            updateDetails(connection, serviceProviderId, requestBody);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Service provider details updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/availability/update")
    public ResponseEntity<Map<String, Object>> updateServiceProviderAvailability(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            String availability = (String) requestBody.get("availability");

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Update the availability for the service provider in the ServiceProviders table
            updateAvailability(connection, serviceProviderId, availability);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Service provider availability updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/notification-preference/update")
    public ResponseEntity<Map<String, Object>> updateNotificationPreference(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            String notificationPreference = (String) requestBody.get("notificationPreference");

            // Validate the auth token
            String userId = getServiceProviderUserId(connection, serviceProviderId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Update the notification preferences for the service provider in the ServiceProviders table
            updateNotifPref(connection, serviceProviderId, notificationPreference);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Notification preferences updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/password/update")
    public ResponseEntity<Map<String, Object>> changeServiceProviderPassword(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            String newPassword = (String) requestBody.get("newPassword");

            // Validate the auth token
            String userId = getServiceProviderUserId(connection, serviceProviderId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Update the password for the service provider in the ServiceProviders table
            changePassword(connection, serviceProviderId, newPassword);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Password updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/reviews")
    private ResponseEntity<Map<String, Object>> getServiceReviews(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestParam("serviceId") int serviceId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the reviews for the given service
            List<Map<String, Object>> reviews = getReviews(connection, serviceProviderId, serviceId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("reviews", reviews);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("reviews/respond")
    public ResponseEntity<Map<String, Object>> respondToReview(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int reviewId = (int) requestBody.get("reviewId");
            String response = (String) requestBody.get("response");

            // Validate the auth token
            String userId = getServiceProviderUserId(connection, serviceProviderId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the review ID corresponds to the same service provider ID
            if (!reviewBelongsToServiceProvider(connection, reviewId, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Update the Service Provider's response in the Reviews table
            sendResponse(connection, reviewId, response);

            // Create the success response
            Map<String, Object> apiResponse = createSuccessResponse(HttpStatus.OK, 1, "Response sent successfully");
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/chats")
    private ResponseEntity<Map<String, Object>> getServiceProviderChats(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the chats for the given service provider
            List<Map<String, Object>> chats = getChats(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("chats", chats);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/chats/recent")
    private ResponseEntity<Map<String, Object>> getChatsLastThreeMonths(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the chats for the given service provider with last message timestamp from the last 3 months
            List<Map<String, Object>> chats = getRecentChats(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("chats", chats);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/messages")
    private ResponseEntity<Map<String, Object>> getChatMessages(
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestParam("chatId") int chatId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the chat ID corresponds to the service provider ID
            if (!chatBelongsToServiceProvider(connection, chatId, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the chat messages for the given chat ID
            List<Map<String, Object>> chatMessages = getMessages(connection, chatId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("messages", chatMessages);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/messages/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int chatId = (int) requestBody.get("chatId");
            String message = (String) requestBody.get("message");

            // Validate the auth token
            String userId = getServiceProviderUserId(connection, serviceProviderId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the chat ID and service provider ID are valid
            if (!chatBelongsToServiceProvider(connection, chatId, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Add the message to the Messages table
            int messageId = addMessage(connection, chatId, userId, message);

            // Update the Last_Message_Timestamp in the Chats table
            updateLastMessageTimestamp(connection, chatId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Message sent successfully");
            response.put("messageId", messageId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/chats/start")
    public ResponseEntity<Map<String, Object>> startChat(
            @RequestBody Map<String, Integer> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the service provider ID and customer ID from the request body
            int serviceProviderId = requestBody.get("serviceProviderId");
            int customerId = requestBody.get("customerId");

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_ID for the service provider
            String serviceProviderUserId = getServiceProviderUserId(connection, serviceProviderId);

            // Validate the auth token for the service provider
            if (!validateAuthToken(connection, serviceProviderUserId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Start a new chat and retrieve the generated chat ID
            int chatId = startNewChat(connection, serviceProviderId, customerId);

            // Create the success response with the generated chat ID
            Map<String, Object> response = createSuccessResponse(HttpStatus.CREATED, 1, "Chat started successfully");
            response.put("chatId", chatId);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/messages/reply")
    public ResponseEntity<Map<String, Object>> sendReplyMessage(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int chatId = (int) requestBody.get("chatId");
            int messageId = (int) requestBody.get("messageId");
            String message = (String) requestBody.get("message");

            // Validate the auth token
            String userId = getServiceProviderUserId(connection, serviceProviderId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the chat ID corresponds to the service provider ID
            if (!chatBelongsToServiceProvider(connection, chatId, serviceProviderId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the message ID corresponds to the chat ID
            if (!messageExistsInChat(connection, messageId, chatId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Invalid message ID");
            }

            // Send the reply message
            int replyMessageId = sendReply(connection, chatId, userId, messageId, message);

            // Update the Last_Message_Timestamp value in the Chats table
            updateLastMessageTimestamp(connection, chatId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Reply sent successfully");
            response.put("replyMessageId", replyMessageId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> postFeedback(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            String feedback = (String) requestBody.get("feedback");

            // Validate the auth token and get the User_Id for the service provider
            String userId = getServiceProviderUserId(connection, serviceProviderId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Insert the feedback into the Feedback table
            insertFeedback(connection, userId, feedback);

            // Create the success response
            Map<String, Object> apiResponse = createSuccessResponse(HttpStatus.OK, 1, "Feedback posted successfully");
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    private boolean isServiceProvider(Connection connection, String userId) throws SQLException {
        String sql = "SELECT Role FROM UserRoles WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String role = resultSet.getString("Role");
                    return "ServiceProvider".equals(role);
                }
                return false; // User not found
            }
        }
    }

    private int getServiceProviderId(Connection connection, String userId) throws SQLException {
        String sql = "SELECT Service_Provider_Id FROM ServiceProviders WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Service_Provider_Id");
                }
                return -1; // Service provider ID not found
            }
        }
    }

    private boolean userExists(Connection connection, String userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM UserRoles WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
                return false; // User not found
            }
        }
    }

    private boolean serviceProviderExists(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT Service_Provider_Id FROM ServiceProviders WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private String getServiceProviderUserId(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT User_Id FROM ServiceProviders WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("User_Id");
            }
            return null;
        }
    }

    private boolean validateAuthToken(Connection connection, String userId, String authToken) throws SQLException {
        String sql = "SELECT User_Id FROM AuthenticationTokens WHERE User_Id = ? AND Auth_Token = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, authToken);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private List<Map<String, Object>> getServiceProviderServices(Connection connection, int serviceProviderId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT sps.Service_Id, s.Service_Name " +
                        "FROM ServiceProviderServices sps " +
                        "JOIN Services s ON sps.Service_Id = s.Service_Id " +
                        "WHERE sps.Service_Provider_Id = ?");

        // Set the service provider ID parameter
        statement.setInt(1, serviceProviderId);

        // Execute the query and retrieve the services
        ResultSet resultSet = statement.executeQuery();
        List<Map<String, Object>> services = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> service = new HashMap<>();
            service.put("serviceId", resultSet.getInt("Service_Id"));
            service.put("serviceName", resultSet.getString("Service_Name"));
            services.add(service);
        }

        return services;
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, int messageCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("messageCode", messageCode);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }

    private Map<String, Object> createSuccessResponse(HttpStatus status, int messageCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("messageCode", messageCode);
        response.put("message", message);
        return response;
    }

    private boolean serviceProviderServiceExists(Connection connection, int serviceProviderId, int serviceId) throws SQLException {
        String sql = "SELECT * FROM ServiceProviderServices WHERE Service_Provider_Id = ? AND Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private void insertServiceProviderService(Connection connection, int serviceProviderId, int serviceId) throws SQLException {
        String sql = "INSERT INTO ServiceProviderServices (Service_Provider_Id, Service_Id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            statement.executeUpdate();
        }
    }

    private int getProviderIndustryId(Connection connection, int providerId) throws SQLException {
        String sql = "SELECT Industry_Id FROM ServiceProviders WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, providerId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("Industry_Id");
            }
            return -1; // or throw an exception if industry ID is not found
        }
    }

    private List<Map<String, Object>> getServicesByIndustryId(Connection connection, int industryId) throws SQLException {
        String sql = "SELECT Service_Id, Service_Name FROM Services WHERE Industry_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, industryId);
            ResultSet resultSet = statement.executeQuery();

            List<Map<String, Object>> services = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> service = new HashMap<>();
                service.put("serviceId", resultSet.getInt("Service_Id"));
                service.put("serviceName", resultSet.getString("Service_Name"));
                services.add(service);
            }

            return services;
        }
    }

    private void deleteAuthToken(Connection connection, String userId) throws SQLException {
        String sql = "DELETE FROM AuthenticationTokens WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.executeUpdate();
        }
    }

    private void insertServiceProviderServiceDescription(Connection connection, int serviceProviderId, int serviceId, String serviceDescription) throws SQLException {
        String query = "UPDATE ServiceProviderServices SET Description = ? WHERE Service_Provider_Id = ? AND Service_Id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, serviceDescription);
            statement.setInt(2, serviceProviderId);
            statement.setInt(3, serviceId);
            statement.executeUpdate();
        }
    }

    private void insertServiceProviderServicePlan(Connection connection, int serviceProviderId, int serviceId, String planDescription, double price, String currency) throws SQLException {
        String query = "INSERT INTO ServiceProviderServiceDetails (Service_Provider_Id, Service_Id, Plan_Description, Price, Currency) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            statement.setString(3, planDescription);
            statement.setDouble(4, price);
            statement.setString(5, currency);
            statement.executeUpdate();
        }
    }

    private boolean servicePlanExists(Connection connection, int serviceProviderId, int serviceId, int planId) throws SQLException {
        String query = "SELECT COUNT(*) FROM ServiceProviderServiceDetails WHERE Service_Provider_Id = ? AND Service_Id = ? AND Plan_Id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            statement.setInt(3, planId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }

        return false;
    }

    private void deletePlan(Connection connection, int serviceProviderId, int serviceId, int planId) throws SQLException {
        String query = "DELETE FROM ServiceProviderServiceDetails WHERE Service_Provider_Id = ? AND Service_Id = ? AND Plan_Id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            statement.setInt(3, planId);
            statement.executeUpdate();
        }
    }

    private void deleteService(Connection connection, int serviceProviderId, int serviceId) throws SQLException {
        String sql = "DELETE FROM ServiceProviderServices WHERE Service_Provider_Id = ? AND Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> getAppointments(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT Appointment_Id, Customer_Id, Service_Id, Date_Time, Status FROM Appointments WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> appointments = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentId", resultSet.getInt("Appointment_Id"));
                    appointment.put("customerId", resultSet.getInt("Customer_Id"));
                    appointment.put("serviceId", resultSet.getInt("Service_Id"));
                    appointment.put("dateTime", resultSet.getTimestamp("Date_Time"));
                    appointment.put("status", resultSet.getString("Status"));
                    appointments.add(appointment);
                }
                return appointments;
            }
        }
    }

    private List<Map<String, Object>> getPastAppointments(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE Service_Provider_Id = ? AND Date_Time < CURRENT_TIMESTAMP";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> pastAppointments = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentId", resultSet.getInt("Appointment_Id"));
                    appointment.put("customerId", resultSet.getInt("Customer_Id"));
                    appointment.put("serviceId", resultSet.getInt("Service_Id"));
                    appointment.put("dateTime", resultSet.getTimestamp("Date_Time"));
                    appointment.put("status", resultSet.getString("Status"));
                    pastAppointments.add(appointment);
                }
                return pastAppointments;
            }
        }
    }

    private List<Map<String, Object>> getFutureAppointments(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE Service_Provider_Id = ? AND Date_Time >= CURRENT_TIMESTAMP";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> futureAppointments = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentId", resultSet.getInt("Appointment_Id"));
                    appointment.put("customerId", resultSet.getInt("Customer_Id"));
                    appointment.put("serviceId", resultSet.getInt("Service_Id"));
                    appointment.put("dateTime", resultSet.getTimestamp("Date_Time"));
                    appointment.put("status", resultSet.getString("Status"));
                    futureAppointments.add(appointment);
                }
                return futureAppointments;
            }
        }
    }

    private void deleteServiceDetails(Connection connection, int serviceProviderId, int serviceId) throws SQLException {
        String sql = "DELETE FROM ServiceProviderServiceDetails WHERE Service_Provider_Id = ? AND Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            statement.executeUpdate();
        }
    }

    private boolean appointmentExists(Connection connection, int appointmentId) throws SQLException {
        String sql = "SELECT Appointment_Id FROM Appointments WHERE Appointment_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private void updateAppointmentStatus(Connection connection, int appointmentId, String status) throws SQLException {
        String sql = "UPDATE Appointments SET Status = ? WHERE Appointment_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, appointmentId);
            statement.executeUpdate();
        }
    }

    private void updateAppointmentDateTime(Connection connection, int appointmentId, LocalDateTime newDateTime) throws SQLException {
        String sql = "UPDATE Appointments SET Date_Time = ? WHERE Appointment_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(newDateTime));
            statement.setInt(2, appointmentId);
            statement.executeUpdate();
        }
    }

    private void updateDetails(Connection connection, int serviceProviderId, Map<String, Object> requestBody) throws SQLException {
        String sql = "UPDATE ServiceProviders SET Company_Name = ?, Industry_Id = ?, Corporate_Identification_Number = ?, Business_Type = ?, Address = ?, Country_Code = ?, State_Code = ?, City_Code = ?, Pin_Code = ?, Email_Address = ? WHERE Service_Provider_Id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Set the parameter values
            statement.setString(1, (String) requestBody.get("companyName"));
            statement.setInt(2, (int) requestBody.get("industryId"));
            statement.setString(3, (String) requestBody.get("corporateIdentificationNumber"));
            statement.setString(4, (String) requestBody.get("businessType"));
            statement.setString(5, (String) requestBody.get("address"));
            statement.setInt(6, (Integer) requestBody.get("countryCode"));
            statement.setInt(7, (Integer) requestBody.get("stateCode"));
            statement.setInt(8, (Integer) requestBody.get("cityCode"));
            statement.setString(9, (String) requestBody.get("pinCode"));
            statement.setString(10, (String) requestBody.get("emailAddress"));
            statement.setInt(11, serviceProviderId);

            // Execute the update statement
            statement.executeUpdate();
        }
    }

    private void updateAvailability(Connection connection, int serviceProviderId, String availability) throws SQLException {
        String sql = "UPDATE ServiceProviders SET Availability = ? WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, availability);
            statement.setInt(2, serviceProviderId);

            statement.executeUpdate();
        }
    }

    private void updateNotifPref(Connection connection, int serviceProviderId, String notificationPreference) throws SQLException {
        String sql = "UPDATE ServiceProviders SET Email_Notifications = ? WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, "yes".equalsIgnoreCase(notificationPreference));
            statement.setInt(2, serviceProviderId);

            statement.executeUpdate();
        }
    }

    private void changePassword(Connection connection, int serviceProviderId, String newPassword) throws SQLException {
        String sql = "UPDATE ServiceProviders SET Password = ? WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setInt(2, serviceProviderId);

            statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> getReviews(Connection connection, int serviceProviderId, int serviceId) throws SQLException {
        String sql = "SELECT Review_Id, Customer_Id, Review, Rating, Response FROM Reviews WHERE Service_Provider_Id = ? AND Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> reviews = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> review = new HashMap<>();
                    review.put("reviewId", resultSet.getInt("Review_Id"));
                    review.put("customerId", resultSet.getInt("Customer_Id"));
                    review.put("review", resultSet.getString("Review"));
                    review.put("rating", resultSet.getFloat("Rating"));
                    review.put("response", resultSet.getString("Response"));
                    reviews.add(review);
                }
                return reviews;
            }
        }
    }

    private boolean reviewBelongsToServiceProvider(Connection connection, int reviewId, int serviceProviderId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Reviews WHERE Review_Id = ? AND Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reviewId);
            statement.setInt(2, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("count") > 0;
            }
        }
    }

    private void sendResponse(Connection connection, int reviewId, String response) throws SQLException {
        String sql = "UPDATE Reviews SET Response = ? WHERE Review_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, response);
            statement.setInt(2, reviewId);

            statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> getChats(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT Chat_Id, Customer_Id, Last_Message_Timestamp FROM Chats WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> chats = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> chat = new HashMap<>();
                    chat.put("chatId", resultSet.getInt("Chat_Id"));
                    chat.put("customerId", resultSet.getInt("Customer_Id"));
                    chat.put("lastMessage", resultSet.getTimestamp("Last_Message_Timestamp"));
                    chats.add(chat);
                }
                return chats;
            }
        }
    }

    private List<Map<String, Object>> getRecentChats(Connection connection, int serviceProviderId) throws SQLException {
        // Calculate the date 3 months ago from the current date
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        String sql = "SELECT Chat_Id, Customer_Id, Last_Message_Timestamp FROM Chats WHERE Service_Provider_Id = ? AND Last_Message_Timestamp >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setTimestamp(2, Timestamp.valueOf(threeMonthsAgo.atStartOfDay()));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> chats = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> chat = new HashMap<>();
                    chat.put("chatId", resultSet.getInt("Chat_Id"));
                    chat.put("customerId", resultSet.getInt("Customer_Id"));
                    chat.put("lastMessage", resultSet.getTimestamp("Last_Message_Timestamp"));
                    chats.add(chat);
                }
                return chats;
            }
        }
    }

    private boolean chatBelongsToServiceProvider(Connection connection, int chatId, int serviceProviderId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Chats WHERE Chat_Id = ? AND Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, chatId);
            statement.setInt(2, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("count") > 0;
            }
        }
    }

    private List<Map<String, Object>> getMessages(Connection connection, int chatId) throws SQLException {
        String sql = "SELECT Message_Id, User_Id, Message, Message_Timestamp, Reply_To_Message_Id FROM Messages WHERE Chat_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, chatId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> chatMessages = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("messageId", resultSet.getInt("Message_Id"));
                    message.put("userId", resultSet.getString("User_Id"));
                    message.put("message", resultSet.getString("Message"));
                    message.put("timestamp", resultSet.getTimestamp("Message_Timestamp"));
                    message.put("replyToMessageId", resultSet.getInt("Reply_To_Message_Id"));
                    chatMessages.add(message);
                }
                return chatMessages;
            }
        }
    }

    private int addMessage(Connection connection, int chatId, String userId, String message) throws SQLException {
        String sql = "INSERT INTO Messages (Chat_Id, User_Id, Message_Timestamp, Message) VALUES (?, ?, NOW(), ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, chatId);
            statement.setString(2, userId);
            statement.setString(3, message);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
        }
    }

    private void updateLastMessageTimestamp(Connection connection, int chatId) throws SQLException {
        String sql = "UPDATE Chats SET Last_Message_Timestamp = NOW() WHERE Chat_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, chatId);
            statement.executeUpdate();
        }
    }

    private boolean customerExists(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customers WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    private int startNewChat(Connection connection, int serviceProviderId, int customerId) throws SQLException {
        String sql = "INSERT INTO Chats (Service_Provider_Id, Customer_Id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, customerId);
            statement.executeUpdate();

            // Retrieve the generated chat ID
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated chat ID");
                }
            }
        }
    }

    private boolean messageExistsInChat(Connection connection, int messageId, int chatId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Messages WHERE Message_Id = ? AND Chat_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, messageId);
            statement.setInt(2, chatId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    private int sendReply(Connection connection, int chatId, String userId, int messageId, String message) throws SQLException {
        String sql = "INSERT INTO Messages (Chat_Id, User_Id, Message_Timestamp, Reply_To_Message_Id, Message) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, chatId);
            statement.setString(2, userId);
            statement.setInt(3, messageId);
            statement.setString(4, message);
            statement.executeUpdate();

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
        }
    }

    private Map<String, Object> fetchServiceProviderDetails(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT Company_Name, Industry_Id, Corporate_Identification_Number, Business_Type, Address, City_Code, State_Code, Country_Code, Pin_Code, Email_Address FROM ServiceProviders WHERE Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Map<String, Object> serviceProviderDetails = new HashMap<>();
                    serviceProviderDetails.put("companyName", resultSet.getString("Company_Name"));
                    serviceProviderDetails.put("industryId", resultSet.getInt("Industry_Id"));
                    serviceProviderDetails.put("corporateIdentificationNumber", resultSet.getString("Corporate_Identification_Number"));
                    serviceProviderDetails.put("businessType", resultSet.getString("Business_Type"));
                    serviceProviderDetails.put("address", resultSet.getString("Address"));
                    serviceProviderDetails.put("cityCode", resultSet.getInt("City_Code"));
                    serviceProviderDetails.put("stateCode", resultSet.getInt("State_Code"));
                    serviceProviderDetails.put("countryCode", resultSet.getInt("Country_Code"));
                    serviceProviderDetails.put("pinCode", resultSet.getString("Pin_Code"));
                    serviceProviderDetails.put("emailAddress", resultSet.getString("Email_Address"));

                    return serviceProviderDetails;
                }
            }
        }
        throw new SQLException("Failed to fetch service provider details");
    }

    private void insertFeedback(Connection connection, String userId, String feedback) throws SQLException {
        String sql = "INSERT INTO Feedback (User_Id, Feedback) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, feedback);
            statement.executeUpdate();
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String parameterName = ex.getParameterName();
        String message = "Required parameter '" + parameterName + "' is missing";
        return createErrorResponse(HttpStatus.BAD_REQUEST, 7, message);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        String headerName = ex.getHeaderName();
        String message = "Required header '" + headerName + "' is missing";
        return createErrorResponse(HttpStatus.BAD_REQUEST, 7, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Request body is missing or cannot be read";
        return createErrorResponse(HttpStatus.BAD_REQUEST, 7, message);
    }
}