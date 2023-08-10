package com.anushka.serviceengagementplatform.controller;

import com.anushka.serviceengagementplatform.model.Customer;
import com.anushka.serviceengagementplatform.model.ServiceProvider;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


@RestController
@RequestMapping("/api")
public class UserRegistrationAuthenticationController {

    private final HikariDataSource dataSource;

    public UserRegistrationAuthenticationController(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/service-provider/add")
    public ResponseEntity<Map<String, Object>> addServiceProvider(@RequestBody ServiceProvider serviceProvider) {
        try (Connection connection = dataSource.getConnection()) {
            if (userExists(connection, serviceProvider.getUserId())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 2, "This username is taken");
            }

            insertUserRole(connection, serviceProvider.getUserId(), "ServiceProvider");
            int serviceProviderId = insertServiceProvider(connection, serviceProvider);

            Map<String, Object> response = createSuccessResponse(HttpStatus.CREATED, 1, "Service provider added successfully");
            response.put("serviceProviderId", serviceProviderId);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to add service provider");
        }
    }

    @GetMapping("/industries")
    public ResponseEntity<Map<String, Object>> getIndustries() {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> industries = fetchIndustries(connection);

            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "List of industries retrieved successfully");
            response.put("industries", industries);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to retrieve industries");
        }
    }

    @PostMapping("/customer/add")
    public ResponseEntity<Map<String, Object>> addCustomer(@RequestBody Customer customer) {
        try (Connection connection = dataSource.getConnection()) {
            if (userExists(connection, customer.getUserId())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 2, "This username is taken");
            }

            insertUserRole(connection, customer.getUserId(), "Customer");
            int customerId = insertCustomer(connection, customer);

            Map<String, Object> response = createSuccessResponse(HttpStatus.CREATED, 1, "Customer added successfully");
            response.put("customerId", customerId);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to add customer");
        }
    }

    @GetMapping("/countries")
    public ResponseEntity<Map<String, Object>> getCountries() {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> countries = retrieveCountries(connection);

            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "List of countries retrieved successfully");
            response.put("countries", countries);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to retrieve countries");
        }
    }

    @GetMapping("/states")
    public ResponseEntity<Map<String, Object>> getStatesByCountry(@RequestParam("country") String countryName) {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> states = retrieveStatesByCountry(connection, countryName);

            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "List of states retrieved successfully");
            response.put("states", states);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to retrieve states");
        }
    }

    @GetMapping("/cities")
    public ResponseEntity<Map<String, Object>> getCitiesByState(@RequestParam("state") String stateName) {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> cities = retrieveCitiesByState(connection, stateName);

            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "List of cities retrieved successfully");
            response.put("cities", cities);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to retrieve cities");
        }
    }

    @PostMapping("/otp/generate")
    public ResponseEntity<Map<String, Object>> generateOTP(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");

        try (Connection connection = dataSource.getConnection()) {
            String otp = generateOTPForUser(connection, userId);
            String emailAddress = getUserEmailAddress(connection, userId);

            if (emailAddress != null) {
                sendOTPEmail(emailAddress, otp);

                Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "OTP generated successfully");
                response.put("otp", otp);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            return createErrorResponse(HttpStatus.NOT_FOUND, 3, "User not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to generate OTP");
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, Object>> verifyOTP(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        String otp = requestBody.get("otp");

        try (Connection connection = dataSource.getConnection()) {
            if (verifyUserOTP(connection, userId, otp)) {
                deleteOTPRecord(connection, userId);

                Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "OTP verification successful");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Invalid OTP");
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to verify OTP");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        String password = requestBody.get("password");
        if (userId == null || password == null)
            return createErrorResponse(HttpStatus.BAD_REQUEST, 7, "Input is incomplete");

        try {
            String role = getUserRole(userId);

            if (role == null) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 3, "User not found");
            }

            boolean passwordMatched;
            String tableName;
            if (role.equals("ServiceProvider")) {
                passwordMatched = checkPassword(userId, password, "ServiceProviders");
                tableName = "ServiceProviders";
            } else if (role.equals("Customer")) {
                passwordMatched = checkPassword(userId, password, "Customers");
                tableName = "Customers";
            } else {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 3, "Invalid role");
            }

            if (!passwordMatched) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, 4, "Wrong password");
            }

            if (isUserLoggedIn(userId)) {
                return createErrorResponse(HttpStatus.OK, 6, "User is already logged in");
            }

            String authToken = generateAuthToken();
            insertAuthToken(userId, authToken);

            Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Login is successful");
            response.put("authToken", authToken);
            response.put("role", role);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Unable to login");
        }
    }


    @PutMapping("/password/update")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        String newPassword = requestBody.get("newPassword");
        if (userId == null || newPassword == null)
            return createErrorResponse(HttpStatus.BAD_REQUEST, 7, "Input is incomplete");
        try {
            String role = getUserRole(userId);
            String tableName = getTableNameByRole(role);

            if (tableName == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 3, "Invalid role");
            }

            String updateSql = String.format("UPDATE %s SET Password = ? WHERE User_Id = ?", tableName);

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

                updateStatement.setString(1, newPassword);
                updateStatement.setString(2, userId);
                int rowsAffected = updateStatement.executeUpdate();

                if (rowsAffected > 0) {
                    Map<String, Object> response = createSuccessResponse(HttpStatus.OK, 1, "Password updated successfully");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Failed to update password");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 5, "Failed to update password");
        }
    }

    private boolean userExists(Connection connection, String userId) throws SQLException {
        String sql = "SELECT User_Id FROM UserRoles WHERE User_Id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private int insertServiceProvider(Connection connection, ServiceProvider serviceProvider) throws SQLException {
        String sql = "INSERT INTO ServiceProviders (Company_Name, Industry_Id, Corporate_Identification_Number, " +
                "Business_Type, Address, City_Code, State_Code, Country_Code, Pin_Code, Email_Address, Password, " +
                "User_Id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters for the statement
            statement.setString(1, serviceProvider.getCompanyName());
            statement.setInt(2, serviceProvider.getIndustryId());
            statement.setString(3, serviceProvider.getCorporateIdentificationNumber());
            statement.setString(4, serviceProvider.getBusinessType());
            statement.setString(5, serviceProvider.getAddress());
            statement.setInt(6, serviceProvider.getCityCode());
            statement.setInt(7, serviceProvider.getStateCode());
            statement.setInt(8, serviceProvider.getCountryCode());
            statement.setInt(9, serviceProvider.getPinCode());
            statement.setString(10, serviceProvider.getEmailAddress());
            statement.setString(11, serviceProvider.getPassword());
            statement.setString(12, serviceProvider.getUserId());

            // Execute the statement and retrieve the generated keys
            int rowsAffected = statement.executeUpdate();
            int serviceProviderId = 0;
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    serviceProviderId = generatedKeys.getInt(1);
                }
            }
            return serviceProviderId;
        }
    }

    private void insertUserRole(Connection connection, String userId, String role) throws SQLException {
        String sql = "INSERT INTO UserRoles (User_Id, Role) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, role);
            statement.executeUpdate();
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

    private Map<String, String> fetchIndustries(Connection connection) throws SQLException {
        String sql = "SELECT Industry_Id, Industry_Name FROM Industries";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            Map<String, String> industries = new HashMap<>();
            while (resultSet.next()) {
                String industryId = resultSet.getString("Industry_Id");
                String industryName = resultSet.getString("Industry_Name");
                industries.put(industryId, industryName);
            }
            return industries;
        }
    }


    private int insertCustomer(Connection connection, Customer customer) throws SQLException {
        String sql = "INSERT INTO Customers (User_Id, Password, First_Name, Last_Name, Address, City_Code, State_Code, Country_Code, Pin_Code, Phone_Number, Email_Address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getUserId());
            statement.setString(2, customer.getPassword());
            statement.setString(3, customer.getFirstName());
            statement.setString(4, customer.getLastName());
            statement.setString(5, customer.getAddress());
            statement.setInt(6, customer.getCityCode());
            statement.setInt(7, customer.getStateCode());
            statement.setInt(8, customer.getCountryCode());
            statement.setString(9, customer.getPinCode());
            statement.setString(10, customer.getPhoneNumber());
            statement.setString(11, customer.getEmailAddress());

            // Execute the statement and retrieve the generated keys
            int rowsAffected = statement.executeUpdate();
            int customerId = 0;
            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    customerId = generatedKeys.getInt(1);
                }
            }
            return customerId;
        }
    }

    private Map<String, String> retrieveCountries(Connection connection) throws SQLException {
        String sql = "SELECT Country_Code, Country_Name FROM Countries";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            Map<String, String> countries = new HashMap<>();
            while (resultSet.next()) {
                String countryCode = resultSet.getString("Country_Code");
                String countryName = resultSet.getString("Country_Name");
                countries.put(countryCode, countryName);
            }
            return countries;
        }
    }

    private Map<String, String> retrieveStatesByCountry(Connection connection, String countryName) throws SQLException {
        String sql = "SELECT State_Code, State_Name FROM States WHERE Country_Code IN (SELECT Country_Code FROM Countries WHERE Country_Name = ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, countryName);

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, String> states = new HashMap<>();
                while (resultSet.next()) {
                    String stateCode = resultSet.getString("State_Code");
                    String stateName = resultSet.getString("State_Name");
                    states.put(stateCode, stateName);
                }
                return states;
            }
        }
    }

    private Map<String, String> retrieveCitiesByState(Connection connection, String stateName) throws SQLException {
        String sql = "SELECT City_Code, City_Name FROM Cities c JOIN States s ON c.State_Code = s.State_Code WHERE s.State_Name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, stateName);

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, String> cities = new HashMap<>();
                while (resultSet.next()) {
                    String cityCode = resultSet.getString("City_Code");
                    String cityName = resultSet.getString("City_Name");
                    cities.put(cityCode, cityName);
                }
                return cities;
            }
        }
    }

    private String generateOTPForUser(Connection connection, String userId) throws SQLException {
        try (PreparedStatement otpStatement = connection.prepareStatement(
                "INSERT INTO UserOTP (User_Id, OTP, Generated_At, Valid_Till) " +
                        "VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "OTP = VALUES(OTP), " +
                        "Generated_At = VALUES(Generated_At), " +
                        "Valid_Till = VALUES(Valid_Till)")) {

            // Generate a random 6-digit OTP
            Random rnd = new Random();
            int number = rnd.nextInt(999999);
            String otp = String.format("%06d", number);

            // Get the current datetime
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Calculate the datetime of 10 minutes later
            LocalDateTime validTillDateTime = currentDateTime.plusMinutes(10);

            // Insert or update the OTP in the UserOTP table
            otpStatement.setString(1, userId);
            otpStatement.setString(2, otp);
            otpStatement.setTimestamp(3, Timestamp.valueOf(currentDateTime));
            otpStatement.setTimestamp(4, Timestamp.valueOf(validTillDateTime));
            otpStatement.executeUpdate();

            return otp;
        }
    }

    private String getUserEmailAddress(Connection connection, String userId) throws SQLException {
        try (PreparedStatement roleStatement = connection.prepareStatement("SELECT Role FROM UserRoles WHERE User_Id = ?");) {

            // Get the user's role
            roleStatement.setString(1, userId);
            ResultSet roleResultSet = roleStatement.executeQuery();

            if (roleResultSet.next()) {
                String role = roleResultSet.getString("Role");

                // Fetch the user's email address based on the role
                String emailSql = "";
                if (role.equals("ServiceProvider")) {
                    emailSql = "SELECT Email_Address FROM ServiceProviders WHERE User_Id = ?";
                } else if (role.equals("Customer")) {
                    emailSql = "SELECT Email_Address FROM Customers WHERE User_Id = ?";
                }

                PreparedStatement emailStatement = connection.prepareStatement(emailSql);
                emailStatement.setString(1, userId);
                ResultSet emailResultSet = emailStatement.executeQuery();

                if (emailResultSet.next()) {
                    return emailResultSet.getString("Email_Address");
                }
            }

            return null;
        }
    }

    private void sendOTPEmail(String emailAddress, String otp) {
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
            message.setSubject("OTP for your account");
            message.setText("Hello!" + "\n" + "Your OTP is: " + otp + "\n" + "It will be valid for the next 10 minutes.");

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private boolean verifyUserOTP(Connection connection, String userId, String otp) throws SQLException {
        try (PreparedStatement otpStatement = connection.prepareStatement("SELECT * FROM UserOTP WHERE User_Id = ?")) {
            // Retrieve the OTP record for the user
            otpStatement.setString(1, userId);
            ResultSet resultSet = otpStatement.executeQuery();

            if (resultSet.next()) {
                String storedOTP = resultSet.getString("OTP");
                Timestamp validTill = resultSet.getTimestamp("Valid_Till");

                // Check if the OTP matches and is still valid
                return otp.equals(storedOTP) && LocalDateTime.now().isBefore(validTill.toLocalDateTime());
            }

            return false;
        }
    }

    private void deleteOTPRecord(Connection connection, String userId) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM UserOTP WHERE User_Id = ?")) {
            deleteStatement.setString(1, userId);
            deleteStatement.executeUpdate();
        }
    }

    private boolean checkPassword(String userId, String password, String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement passwordStatement = connection.prepareStatement("SELECT Password FROM " + tableName + " WHERE User_Id = ?")) {

            passwordStatement.setString(1, userId);
            ResultSet passwordResultSet = passwordStatement.executeQuery();

            if (passwordResultSet.next()) {
                String storedPassword = passwordResultSet.getString("Password");
                return password.equals(storedPassword);
            }
        }

        return false;
    }

    private boolean isUserLoggedIn(String userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement("SELECT * FROM AuthenticationTokens WHERE User_Id = ?")) {

            checkStatement.setString(1, userId);
            ResultSet checkResultSet = checkStatement.executeQuery();

            return checkResultSet.next();
        }
    }

    private String generateAuthToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(6);

        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    private void insertAuthToken(String userId, String authToken) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO AuthenticationTokens (User_Id, Auth_Token) VALUES (?, ?)")) {

            insertStatement.setString(1, userId);
            insertStatement.setString(2, authToken);
            insertStatement.executeUpdate();
        }
    }

    private String getUserRole(String userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement roleStatement = connection.prepareStatement("SELECT Role FROM UserRoles WHERE User_Id = ?")) {

            roleStatement.setString(1, userId);
            ResultSet roleResultSet = roleStatement.executeQuery();

            if (roleResultSet.next()) {
                return roleResultSet.getString("Role");
            }
        }

        return null;
    }

    private String getTableNameByRole(String role) {
        switch (role) {
            case "ServiceProvider":
                return "ServiceProviders";
            case "Customer":
                return "Customers";
            default:
                return null;
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String parameterName = ex.getParameterName();
        String message = "Required parameter '" + parameterName + "' is missing";
        return createErrorResponse(HttpStatus.BAD_REQUEST, 7, message);
    }

}