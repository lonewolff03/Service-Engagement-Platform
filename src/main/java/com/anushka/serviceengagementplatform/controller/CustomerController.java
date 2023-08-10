package com.anushka.serviceengagementplatform.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final HikariDataSource dataSource;

    public CustomerController(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/customer-id")
    public ResponseEntity<Map<String, Object>> getCustomerIdByUserId(@RequestParam("userId") String userId) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the user exists in the Users table
            if (!userExists(connection, userId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "User not found");
            }

            // Check if the user is a customer
            if (!isCustomer(connection, userId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 4, "User is not a customer");
            }

            // Get the customer ID for the user
            int customerId = getCustomerId(connection, userId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("customerId", customerId);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/dashboard")
    private ResponseEntity<Map<String, Object>> getCustomerDashboard(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the customer's services
            List<Map<String, Object>> services = getCustomerServices(connection, customerId);

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
    private ResponseEntity<Map<String, Object>> logoutCustomer(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

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

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getCustomerDetails(@RequestParam("customerId") int customerId,
                                                                         @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the customer details
            Map<String, Object> serviceProviderDetails = fetchCustomerDetails(connection, customerId);

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
    public ResponseEntity<Map<String, Object>> updateCustomerDetails(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the customer ID from the request body
            int customerId = (int) requestBody.get("customerId");

            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Update the customer details
            updateDetails(connection, customerId, requestBody);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Customer details updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<Map<String, Object>> getPaymentMethods(@RequestParam("customerId") int customerId, @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the available payment methods
            Map<Integer, String> paymentMethods = fetchPaymentMethods(connection);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("paymentMethods", paymentMethods);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/payment-methods/saved")
    public ResponseEntity<Map<String, Object>> getSavedPaymentMethods(@RequestParam("customerId") int customerId, @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the saved payment methods
            Map<Integer, String> paymentMethods = fetchSavedPaymentMethods(connection, customerId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("paymentMethods", paymentMethods);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/payment-methods/saved/add")
    public ResponseEntity<Map<String, Object>> addPaymentMethod(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the customer id and payment method id from the request body
            int customerId = (int) requestBody.get("customerId");
            int paymentMethodId = (int) requestBody.get("paymentMethodId");

            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the payment method exists
            if (!paymentMethodExists(connection, paymentMethodId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Invalid payment method");
            }

            // Add the payment method to CustomerPaymentMethods
            addPaymentMethodToCustomer(connection, customerId, paymentMethodId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.CREATED, 1, "Payment method added successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @DeleteMapping("/payment-methods/saved/delete")
    public ResponseEntity<Map<String, Object>> deletePaymentMethodForCustomer(
            @RequestParam("customerId") int customerId,
            @RequestParam("paymentMethodId") int paymentMethodId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the payment method exists for the customer
            if (!paymentMethodExistsForCustomer(connection, customerId, paymentMethodId)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Invalid payment method");
            }

            // Delete the payment method from CustomerPaymentMethods
            deleteSavedPaymentMethod(connection, customerId, paymentMethodId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Payment method deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getCustomerTransactions(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Validate the auth token
            String userId = getCustomerUserId(connection, customerId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the customer's transactions
            List<Map<String, Object>> transactions = getCustomerTransactions(connection, customerId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("transactions", transactions);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/password/update")
    public ResponseEntity<Map<String, Object>> changeCustomerPassword(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int customerId = (int) requestBody.get("customerId");
            String newPassword = (String) requestBody.get("newPassword");

            // Validate the auth token
            String userId = getCustomerUserId(connection, customerId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Update the password for the customer in the Customers table
            changePassword(connection, customerId, newPassword);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Password updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/services/available")
    public ResponseEntity<Map<String, Object>> getServices(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the services from the Services table
            List<Map<String, Object>> services = getAvailableServices(connection);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("services", services);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/services/options")
    public ResponseEntity<Map<String, Object>> getServiceOptions(
            @RequestParam("serviceId") int serviceId,
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service exists in the Services table
            if (!serviceExists(connection, serviceId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Service not found");
            }

            // Fetch the service options from the ServiceProviderServices table
            List<Map<String, Object>> serviceOptions = getOptions(connection, serviceId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("serviceOptions", serviceOptions);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/services/details")
    public ResponseEntity<Map<String, Object>> getServiceDetails(
            @RequestParam("customerId") int customerId,
            @RequestParam("serviceId") int serviceId,
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service and service provider pair exists in the ServiceProviderServices table
            if (!serviceProviderServiceExists(connection, serviceId, serviceProviderId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 4, "Service provider service not found");
            }

            // Fetch the description and rating from the ServiceProviderServices table
            Map<String, Object> serviceProviderService = getServiceProviderServiceDetails(connection, serviceId, serviceProviderId);

            // Fetch the list of plans with details from the ServiceProviderServiceDetails table
            List<Map<String, Object>> servicePlans = getServicePlans(connection, serviceId, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("serviceProviderService", serviceProviderService);
            response.put("servicePlans", servicePlans);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/service-provider-details")
    public ResponseEntity<Map<String, Object>> getServiceProviderDetails(
            @RequestParam("customerId") int customerId,
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token for the customer
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service provider exists in the ServiceProviders table
            if (!serviceProviderExists(connection, serviceProviderId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Service Provider not found");
            }

            // Fetch the service provider details from the ServiceProviders table
            Map<String, Object> serviceProviderDetails = getServiceProviderDetails(connection, serviceProviderId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("serviceProviderDetails", serviceProviderDetails);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("services/reviews")
    public ResponseEntity<Map<String, Object>> getReviews(
            @RequestParam("customerId") int customerId,
            @RequestParam("serviceProviderId") int serviceProviderId,
            @RequestParam("serviceId") int serviceId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token for the customer
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service and service provider pair exists in the ServiceProviderServices table
            if (!serviceProviderServiceExists(connection, serviceId, serviceProviderId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Service provider service not found");
            }

            // Fetch the list of reviews from the Reviews table
            List<Map<String, Object>> reviews = getReviewsForService(connection, customerId, serviceProviderId, serviceId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("reviews", reviews);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PostMapping("/appointments/request")
    public ResponseEntity<Map<String, Object>> addAppointment(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int customerId = (int) requestBody.get("customerId");
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int serviceId = (int) requestBody.get("serviceId");
            LocalDateTime dateTime = LocalDateTime.parse(requestBody.get("dateTime").toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token for the customer
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the service and service provider pair exists in the ServiceProviderServices table
            if (!serviceProviderServiceExists(connection, serviceId, serviceProviderId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Service provider service not found");
            }

            // Add the appointment to the Appointments table and retrieve appointment ID
            int appointmentId = addAppointmentToTable(connection, customerId, serviceProviderId, serviceId, dateTime);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Appointment added successfully");
            response.put("appointmentId", appointmentId);

            //Send notification to service provider
            String emailAddress = getServiceProviderEmailAddress(connection, serviceProviderId);
            sendNotifEmail(emailAddress);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @GetMapping("/appointments")
    private ResponseEntity<Map<String, Object>> getAppointmentsByCustomerId(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the appointments for the given customer
            List<Map<String, Object>> appointments = getAppointments(connection, customerId);

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
    public ResponseEntity<Map<String, Object>> getPastAppointmentsByCustomerId(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the past appointments for the customer
            List<Map<String, Object>> pastAppointments = getPastAppointments(connection, customerId);

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
    public ResponseEntity<Map<String, Object>> getFutureAppointmentsByCustomerId(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the future appointments for the customer
            List<Map<String, Object>> futureAppointments = getFutureAppointments(connection, customerId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Success");
            response.put("appointments", futureAppointments);

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

            // Extract the customer ID, appointment ID, and new date-time from the request body
            int customerId = (int) requestBody.get("customerId");
            int appointmentId = (int) requestBody.get("appointmentId");
            LocalDateTime newDateTime = LocalDateTime.parse((String) requestBody.get("newDateTime"));

            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

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

            //Set status as Requested
            setRequested(connection, appointmentId);

            // Create the success response
            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Appointment rescheduled successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
        }
    }

    @PutMapping("/appointment/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @RequestBody Map<String, Integer> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int customerId = requestBody.get("customerId");
            int appointmentId = requestBody.get("appointmentId");

            // Validate the auth token for the customer
            String userId = getCustomerUserId(connection, customerId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the appointment exists in the Appointments table
            if (!appointmentExists(connection, appointmentId)) {
                return createErrorResponse(HttpStatus.NOT_FOUND, 3, "Appointment not found");
            }

            // Update the status of the appointment to "Cancelled"
            cancelAppointmentForCustomer(connection, appointmentId);

            // Create the success response
            Map<String, Object> apiResponse = createSuccessResponse(HttpStatus.OK, 1, "Appointment cancelled successfully");
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (SQLException e) {
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
            int customerId = (int) requestBody.get("customerId");
            String feedback = (String) requestBody.get("feedback");

            // Validate the auth token and get the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);
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

    @GetMapping("/chats")
    private ResponseEntity<Map<String, Object>> getCustomerChats(
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the chats for the given customer
            List<Map<String, Object>> chats = getChats(connection, customerId);

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
            @RequestParam("customerId") int customerId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Fetch the chats for the given customer with last message timestamp from the last 3 months
            List<Map<String, Object>> chats = getRecentChats(connection, customerId);

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
            @RequestParam("customerId") int customerId,
            @RequestParam("chatId") int chatId,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_Id for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the chat ID corresponds to the customer ID
            if (!chatBelongsToCustomer(connection, chatId, customerId)) {
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
            int customerId = (int) requestBody.get("customerId");
            int chatId = (int) requestBody.get("chatId");
            String message = (String) requestBody.get("message");

            // Validate the auth token
            String userId = getCustomerUserId(connection, customerId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the chat ID and customer ID are valid
            if (!chatBelongsToCustomer(connection, chatId, customerId)) {
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

            // Check if the customer exists in the Customers table
            if (!customerExists(connection, customerId)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Retrieve the User_ID for the customer
            String userId = getCustomerUserId(connection, customerId);

            // Validate the auth token for the customer
            if (!validateAuthToken(connection, userId, authToken)) {
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
            int customerId = (int) requestBody.get("customerId");
            int chatId = (int) requestBody.get("chatId");
            int messageId = (int) requestBody.get("messageId");
            String message = (String) requestBody.get("message");

            // Validate the auth token
            String userId = getCustomerUserId(connection, customerId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Check if the chat ID corresponds to the customer ID
            if (!chatBelongsToCustomer(connection, chatId, customerId)) {
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

    @PostMapping("/services/reviews/post")
    public ResponseEntity<Map<String, Object>> postReview(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("AuthToken") String authToken) {
        try (Connection connection = dataSource.getConnection()) {
            // Check if the request body is missing
            if (requestBody == null) {
                throw new HttpMessageNotReadableException("Request body is missing");
            }

            // Extract the required fields from the request body
            int customerId = (int) requestBody.get("customerId");
            int serviceProviderId = (int) requestBody.get("serviceProviderId");
            int serviceId = (int) requestBody.get("serviceId");
            float rating = (float) requestBody.get("rating");
            String review = (String) requestBody.get("review");

            // Validate the auth token for the customer
            String userId = getCustomerUserId(connection, customerId);
            if (!validateAuthToken(connection, userId, authToken)) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Unauthorized");
            }

            // Insert the review into the Reviews table
            insertReview(connection, customerId, serviceProviderId, serviceId, rating, review);

            // Update the service provider's service rating
            updateServiceProviderServiceRating(connection, serviceProviderId, serviceId);

            // Create the success response
            Map<String, Object> apiResponse = createSuccessResponse(HttpStatus.OK, 1, "Review posted successfully");
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Internal Server Error");
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

    private boolean isCustomer(Connection connection, String userId) throws SQLException {
        String sql = "SELECT Role FROM UserRoles WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String role = resultSet.getString("Role");
                    return "Customer".equals(role);
                }
                return false; // User not found
            }
        }
    }

    private int getCustomerId(Connection connection, String userId) throws SQLException {
        String sql = "SELECT Customer_Id FROM Customers WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Customer_Id");
                }
                return -1; // Customer ID not found
            }
        }
    }

    private boolean customerExists(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT Customer_Id FROM Customers WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private String getCustomerUserId(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT User_Id FROM Customers WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
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

    private List<Map<String, Object>> getCustomerServices(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT cs.Service_Id, cs.Service_Provider_Id, cs.Service_Startdate, cs.Service_Enddate, s.Service_Name " +
                "FROM CustomerServices cs " +
                "JOIN Services s ON cs.Service_Id = s.Service_Id " +
                "WHERE cs.Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> services = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> service = new HashMap<>();
                    service.put("serviceId", resultSet.getInt("Service_Id"));
                    service.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    service.put("serviceName", resultSet.getString("Service_Name"));
                    service.put("startDate", resultSet.getDate("Service_Startdate"));
                    service.put("endDate", resultSet.getDate("Service_Enddate"));
                    services.add(service);
                }
                return services;
            }
        }
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

    private void deleteAuthToken(Connection connection, String userId) throws SQLException {
        String sql = "DELETE FROM AuthenticationTokens WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.executeUpdate();
        }
    }

    private Map<String, Object> fetchCustomerDetails(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT First_Name, Last_Name, Phone_Number, Address, City_Code, State_Code, Country_Code, Pin_Code, Email_Address FROM Customers WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Map<String, Object> customerDetails = new HashMap<>();
                    customerDetails.put("firstName", resultSet.getString("First_Name"));
                    customerDetails.put("lastName", resultSet.getString("Last_Name"));
                    customerDetails.put("phoneNumber", resultSet.getString("Phone_Number"));
                    customerDetails.put("address", resultSet.getString("Address"));
                    customerDetails.put("cityCode", resultSet.getInt("City_Code"));
                    customerDetails.put("stateCode", resultSet.getInt("State_Code"));
                    customerDetails.put("countryCode", resultSet.getInt("Country_Code"));
                    customerDetails.put("pinCode", resultSet.getString("Pin_Code"));
                    customerDetails.put("emailAddress", resultSet.getString("Email_Address"));

                    return customerDetails;
                }
            }
        }
        throw new SQLException("Failed to fetch service provider details");
    }

    private void updateDetails(Connection connection, int customerId, Map<String, Object> requestBody) throws SQLException {
        String sql = "UPDATE Customers SET First_Name = ?, Last_Name = ?, Phone_Number = ?, Address = ?, Country_Code = ?, State_Code = ?, City_Code = ?, Pin_Code = ?, Email_Address = ? WHERE Customer_Id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Set the parameter values
            statement.setString(1, (String) requestBody.get("firstName"));
            statement.setString(2, (String) requestBody.get("lastName"));
            statement.setString(3, (String) requestBody.get("phoneNumber"));
            statement.setString(4, (String) requestBody.get("address"));
            statement.setInt(5, (Integer) requestBody.get("countryCode"));
            statement.setInt(6, (Integer) requestBody.get("stateCode"));
            statement.setInt(7, (Integer) requestBody.get("cityCode"));
            statement.setString(8, (String) requestBody.get("pinCode"));
            statement.setString(9, (String) requestBody.get("emailAddress"));
            statement.setInt(10, customerId);

            // Execute the update statement
            statement.executeUpdate();
        }
    }

    private Map<Integer, String> fetchPaymentMethods(Connection connection) throws SQLException {
        String sql = "SELECT Payment_Method_Id, Payment_Method FROM PaymentMethods";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            Map<Integer, String> paymentMethods = new HashMap<>();
            while (resultSet.next()) {
                int paymentMethodId = resultSet.getInt("Payment_Method_Id");
                String paymentMethod = resultSet.getString("Payment_Method");
                paymentMethods.put(paymentMethodId, paymentMethod);
            }
            return paymentMethods;
        }
    }

    private Map<Integer, String> fetchSavedPaymentMethods(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT cpm.Saved_Payment_Method_Id, pm.Payment_Method FROM CustomerPaymentMethods cpm " +
                "JOIN PaymentMethods pm ON cpm.Saved_Payment_Method_Id = pm.Payment_Method_Id " +
                "WHERE cpm.Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Integer, String> paymentMethods = new HashMap<>();
                while (resultSet.next()) {
                    int paymentMethodId = resultSet.getInt("Saved_Payment_Method_Id");
                    String paymentMethod = resultSet.getString("Payment_Method");
                    paymentMethods.put(paymentMethodId, paymentMethod);
                }
                return paymentMethods;
            }
        }
    }

    private boolean paymentMethodExists(Connection connection, int paymentMethodId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM PaymentMethods WHERE Payment_Method_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, paymentMethodId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        }
        return false;
    }

    private void addPaymentMethodToCustomer(Connection connection, int customerId, int paymentMethodId) throws SQLException {
        String sql = "INSERT INTO CustomerPaymentMethods (Customer_Id, Saved_Payment_Method_Id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setInt(2, paymentMethodId);
            statement.executeUpdate();
        }
    }

    private boolean paymentMethodExistsForCustomer(Connection connection, int customerId, int paymentMethodId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CustomerPaymentMethods WHERE Customer_Id = ? AND Saved_Payment_Method_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setInt(2, paymentMethodId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    private void deleteSavedPaymentMethod(Connection connection, int customerId, int paymentMethodId) throws SQLException {
        String sql = "DELETE FROM CustomerPaymentMethods WHERE Customer_Id = ? AND Saved_Payment_Method_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setInt(2, paymentMethodId);
            statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> getCustomerTransactions(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT * FROM Transactions WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> transactions = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("transactionId", resultSet.getInt("Transaction_Id"));
                    transaction.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    transaction.put("serviceId", resultSet.getInt("Service_Id"));
                    transaction.put("planId", resultSet.getInt("Plan_Id"));
                    transaction.put("transactionDateTime", resultSet.getTimestamp("Transaction_DateTime"));
                    transaction.put("transactionAmount", resultSet.getFloat("Transaction_Amount"));
                    transaction.put("currency", resultSet.getString("Currency"));
                    transaction.put("paymentMethodId", resultSet.getInt("Payment_Method_Id"));
                    transactions.add(transaction);
                }
                return transactions;
            }
        }
    }

    private void changePassword(Connection connection, int customerId, String newPassword) throws SQLException {
        String sql = "UPDATE Customers SET Password = ? WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setInt(2, customerId);

            statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> getAvailableServices(Connection connection) throws SQLException {
        String sql = "SELECT Service_Id, Service_Name, Industry_Id FROM Services;";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> services = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> service = new HashMap<>();
                    service.put("Service_Id", resultSet.getInt("Service_Id"));
                    service.put("Service_Name", resultSet.getString("Service_Name"));
                    service.put("Industry_Id", resultSet.getInt("Industry_Id"));
                    services.add(service);
                }
                return services;
            }
        }
    }

    private List<Map<String, Object>> getOptions(Connection connection, int serviceId) throws SQLException {
        String sql = "SELECT s.Service_Provider_Id, s.Description, s.Service_Rating FROM ServiceProviderServices s WHERE s.Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> serviceOptions = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> serviceOption = new HashMap<>();
                    serviceOption.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    serviceOption.put("description", resultSet.getString("Description"));
                    serviceOption.put("serviceRating", resultSet.getFloat("Service_Rating"));
                    serviceOptions.add(serviceOption);
                }
                return serviceOptions;
            }
        }
    }

    private boolean serviceExists(Connection connection, int serviceId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Services WHERE Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
                return false; // Service not found
            }
        }
    }

    private boolean serviceProviderServiceExists(Connection connection, int serviceId, int serviceProviderId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM ServiceProviderServices WHERE Service_Id = ? AND Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            statement.setInt(2, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
                return false; // Service provider service not found
            }
        }
    }

    private Map<String, Object> getServiceProviderServiceDetails(Connection connection, int serviceId, int serviceProviderId) throws SQLException {
        String sql = "SELECT Description, Service_Rating FROM ServiceProviderServices WHERE Service_Id = ? AND Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            statement.setInt(2, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Map<String, Object> serviceProviderService = new HashMap<>();
                    serviceProviderService.put("description", resultSet.getString("Description"));
                    serviceProviderService.put("rating", resultSet.getFloat("Service_Rating"));
                    return serviceProviderService;
                }
                return null; // Service provider service not found
            }
        }
    }

    private List<Map<String, Object>> getServicePlans(Connection connection, int serviceId, int serviceProviderId) throws SQLException {
        String sql = "SELECT Plan_Id, Plan_Description, Price, Currency FROM ServiceProviderServiceDetails WHERE Service_Id = ? AND Service_Provider_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            statement.setInt(2, serviceProviderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> servicePlans = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> plan = new HashMap<>();
                    plan.put("planId", resultSet.getInt("Plan_Id"));
                    plan.put("planDescription", resultSet.getString("Plan_Description"));
                    plan.put("price", resultSet.getFloat("Price"));
                    plan.put("currency", resultSet.getString("Currency"));
                    servicePlans.add(plan);
                }
                return servicePlans;
            }
        }
    }

    private Map<String, Object> getServiceProviderDetails(Connection connection, int serviceProviderId) throws SQLException {
        String sql = "SELECT Company_Name, Industry_Id, Corporate_Identification_Number, Business_Type, Address, City_Code, State_Code, Country_Code, Pin_Code, Email_Address, User_Id, Availability FROM ServiceProviders WHERE Service_Provider_Id = ?";
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
                    serviceProviderDetails.put("pinCode", resultSet.getInt("Pin_Code"));
                    serviceProviderDetails.put("emailAddress", resultSet.getString("Email_Address"));
                    serviceProviderDetails.put("userId", resultSet.getString("User_Id"));
                    serviceProviderDetails.put("availability", resultSet.getString("Availability"));
                    return serviceProviderDetails;
                }
                return null; // Service provider not found
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

    private List<Map<String, Object>> getReviewsForService(Connection connection, int customerId, int serviceProviderId, int serviceId) throws SQLException {
        String sql = "SELECT Review_Id, Rating, Review, Customer_Id, Response FROM Reviews WHERE Service_Provider_Id = ? AND Service_Id = ? AND Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            statement.setInt(3, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> reviews = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> review = new HashMap<>();
                    review.put("reviewId", resultSet.getInt("Review_Id"));
                    review.put("rating", resultSet.getFloat("Rating"));
                    review.put("review", resultSet.getString("Review"));
                    review.put("customerId", resultSet.getInt("Customer_Id"));
                    review.put("response", resultSet.getString("Response"));
                    reviews.add(review);
                }
                return reviews;
            }
        }
    }

    private int addAppointmentToTable(Connection connection, int customerId, int serviceProviderId, int serviceId, LocalDateTime dateTime) throws SQLException {
        String sql = "INSERT INTO Appointments (Customer_Id, Service_Provider_Id, Service_Id, Date_Time, Status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, customerId);
            statement.setInt(2, serviceProviderId);
            statement.setInt(3, serviceId);
            statement.setTimestamp(4, Timestamp.valueOf(dateTime));
            statement.setString(5, "Requested");
            statement.executeUpdate();

            // Retrieve the generated appointment ID
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve generated appointment ID");
                }
            }
        }
    }

    private List<Map<String, Object>> getAppointments(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT Appointment_Id, Service_Provider_Id, Service_Id, Date_Time, Status FROM Appointments WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> appointments = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentId", resultSet.getInt("Appointment_Id"));
                    appointment.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    appointment.put("serviceId", resultSet.getInt("Service_Id"));
                    appointment.put("dateTime", resultSet.getTimestamp("Date_Time"));
                    appointment.put("status", resultSet.getString("Status"));
                    appointments.add(appointment);
                }
                return appointments;
            }
        }
    }

    private List<Map<String, Object>> getPastAppointments(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE Customer_Id = ? AND Date_Time < CURRENT_TIMESTAMP";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> pastAppointments = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentId", resultSet.getInt("Appointment_Id"));
                    appointment.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    appointment.put("serviceId", resultSet.getInt("Service_Id"));
                    appointment.put("dateTime", resultSet.getTimestamp("Date_Time"));
                    appointment.put("status", resultSet.getString("Status"));
                    pastAppointments.add(appointment);
                }
                return pastAppointments;
            }
        }
    }

    private List<Map<String, Object>> getFutureAppointments(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT * FROM Appointments WHERE Customer_Id = ? AND Date_Time >= CURRENT_TIMESTAMP";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> futureAppointments = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("appointmentId", resultSet.getInt("Appointment_Id"));
                    appointment.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    appointment.put("serviceId", resultSet.getInt("Service_Id"));
                    appointment.put("dateTime", resultSet.getTimestamp("Date_Time"));
                    appointment.put("status", resultSet.getString("Status"));
                    futureAppointments.add(appointment);
                }
                return futureAppointments;
            }
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

    private void updateAppointmentDateTime(Connection connection, int appointmentId, LocalDateTime newDateTime) throws SQLException {
        String sql = "UPDATE Appointments SET Date_Time = ? WHERE Appointment_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(newDateTime));
            statement.setInt(2, appointmentId);
            statement.executeUpdate();
        }
    }

    private void setRequested(Connection connection, int appointmentId) throws SQLException {
        String sql = "UPDATE Appointments SET Status = 'Requested' WHERE Appointment_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            statement.executeUpdate();
        }
    }
    private void cancelAppointmentForCustomer(Connection connection, int appointmentId) throws SQLException {
        String sql = "UPDATE Appointments SET Status = 'Cancelled' WHERE Appointment_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, appointmentId);
            statement.executeUpdate();
        }
    }


    private void insertFeedback(Connection connection, String userId, String feedback) throws SQLException {
        String sql = "INSERT INTO Feedback (User_Id, Feedback) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, feedback);
            statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> getChats(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT Chat_Id, Service_Provider_Id, Last_Message_Timestamp FROM Chats WHERE Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> chats = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> chat = new HashMap<>();
                    chat.put("chatId", resultSet.getInt("Chat_Id"));
                    chat.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    chat.put("lastMessage", resultSet.getTimestamp("Last_Message_Timestamp"));
                    chats.add(chat);
                }
                return chats;
            }
        }
    }

    private List<Map<String, Object>> getRecentChats(Connection connection, int customerId) throws SQLException {
        // Calculate the date 3 months ago from the current date
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        String sql = "SELECT Chat_Id, Service_Provider_Id, Last_Message_Timestamp FROM Chats WHERE Customer_Id = ? AND Last_Message_Timestamp >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setTimestamp(2, Timestamp.valueOf(threeMonthsAgo.atStartOfDay()));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map<String, Object>> chats = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> chat = new HashMap<>();
                    chat.put("chatId", resultSet.getInt("Chat_Id"));
                    chat.put("serviceProviderId", resultSet.getInt("Service_Provider_Id"));
                    chat.put("lastMessage", resultSet.getTimestamp("Last_Message_Timestamp"));
                    chats.add(chat);
                }
                return chats;
            }
        }
    }

    private boolean chatBelongsToCustomer(Connection connection, int chatId, int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Chats WHERE Chat_Id = ? AND Customer_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, chatId);
            statement.setInt(2, customerId);

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


    private void insertReview(Connection connection, int customerId, int serviceProviderId, int serviceId, float rating, String review) throws SQLException {
        String sql = "INSERT INTO Reviews (Customer_Id, Service_Provider_Id, Service_Id, Rating, Review) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            statement.setInt(2, serviceProviderId);
            statement.setInt(3, serviceId);
            statement.setFloat(4, rating);
            statement.setString(5, review);
            statement.executeUpdate();
        }
    }

    private void updateServiceProviderServiceRating(Connection connection, int serviceProviderId, int serviceId) throws SQLException {
        String sql = "SELECT AVG(Rating) AS averageRating FROM Reviews WHERE Service_Provider_Id = ? AND Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceProviderId);
            statement.setInt(2, serviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    float averageRating = resultSet.getFloat("averageRating");
                    updateServiceRating(connection, serviceProviderId, serviceId, averageRating);
                }
            }
        }
    }

    private void updateServiceRating(Connection connection, int serviceProviderId, int serviceId, float averageRating) throws SQLException {
        String sql = "UPDATE ServiceProviderServices SET Service_Rating = ? WHERE Service_Provider_Id = ? AND Service_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setFloat(1, averageRating);
            statement.setInt(2, serviceProviderId);
            statement.setInt(3, serviceId);
            statement.executeUpdate();
        }
    }

    private void sendNotifEmail(String emailAddress) {
        final String username = "";
        final String password = "";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));
            message.setSubject("Service Engagement Platform Notification");
            message.setText("Hello!" + "\n" + "You have a new appointment request.");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String getServiceProviderEmailAddress(Connection connection, int serviceProviderId) throws SQLException {
        try (PreparedStatement emailStatement = connection.prepareStatement("SELECT Email_Address FROM ServiceProviders WHERE Service_Provider_Id = ?");) {

            emailStatement.setInt(1, serviceProviderId);
            ResultSet emailResultSet = emailStatement.executeQuery();

            if (emailResultSet.next()) {
                return emailResultSet.getString("Email_Address");
            }

            return null;
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