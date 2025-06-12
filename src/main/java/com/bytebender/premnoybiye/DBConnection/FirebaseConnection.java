package com.bytebender.premnoybiye.DBConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class FirebaseConnection {
    private String apiKey;
    private String databaseUrl;
    private ObjectMapper objectMapper;
    private HttpClient httpClient;

    public FirebaseConnection() {
        loadFirebaseConfig();
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    private void loadFirebaseConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("firebase.properties")) {
            if (input == null) {
                System.err.println("Firebase properties file not found in resources!");
                return;
            }

            properties.load(input);

            this.apiKey = properties.getProperty("apiKey");
            this.databaseUrl = properties.getProperty("databaseUrl");

            System.out.println("Firebase config loaded successfully");
            System.out.println("Database URL: " + this.databaseUrl);

        } catch (IOException e) {
            System.err.println("Error loading Firebase config: " + e.getMessage());
        }
    }

    private String generateCustomTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy_h:mm:ssa");
        return now.format(formatter).toLowerCase();
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Registers a new user in Firebase with basic information
     * 
     * @param name     User's name
     * @param email    User's email
     * @param password User's password
     * @return boolean indicating success or failure
     */
    public boolean registerUser(String name, String email, String password) {
        try {
            if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                showErrorAlert("Registration Error", "Name, email, and password must not be empty");
                return false;
            }

            // Check if email is already registered
            if (isEmailAlreadyRegistered(email)) {
                showErrorAlert("Registration Error",
                        "An account with this email address already exists. Please use a different email or try logging in.");
                return false;
            }

            // Extract first name only (up to first space or full name if no space)
            String firstName = name.trim().split(" ")[0];
            String userId = firstName + "__" + generateCustomTimestamp();

            // Create user data map
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("password", password); // Note: In production, hash this password
            userData.put("dob", "");
            userData.put("gender", "");
            userData.put("religion", "");
            userData.put("city", "");
            userData.put("image", "");
            userData.put("education", "");
            userData.put("profession", "");
            userData.put("income", "");
            userData.put("bio", "");
            userData.put("prefAge", "");
            userData.put("prefLocation", "");
            userData.put("prefProfession", "");
            userData.put("userId", userId);

            // Convert to JSON
            String jsonData = objectMapper.writeValueAsString(userData);

            // Create HTTP PUT request to Firebase Realtime Database
            String url = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build(); // Send request to create user
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // User created successfully, now add email to index for faster lookups
                boolean emailIndexSuccess = addEmailToIndex(email, userId);

                if (emailIndexSuccess) {
                    System.out.println(
                            "User registered successfully in Firebase with email index: " + name + " (" + email + ")");
                } else {
                    System.out.println("User registered successfully in Firebase, but email index failed: " + name
                            + " (" + email + ")");
                }
                return true;
            } else {
                showErrorAlert("Registration Failed",
                        "Failed to register user. Please check your internet connection and try again.");
                System.err.println("Firebase registration failed. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return false;
            }

        } catch (Exception e) {
            showErrorAlert("Registration Error", "An unexpected error occurred during registration. Please try again.");
            System.err.println("Error registering user in Firebase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates user profile information in Firebase
     * 
     * @param userInfo Complete user information object
     * @return boolean indicating success or failure
     */
    public boolean updateUserProfile(userInfo user) {
        try {
            // Create updated user data map
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("dob", user.getDob());
            userData.put("gender", user.getGender());
            userData.put("religion", user.getReligion());
            userData.put("city", user.getCity());
            userData.put("image", user.getImage());
            userData.put("education", user.getEducation());
            userData.put("profession", user.getProfession());
            userData.put("income", user.getIncome());
            userData.put("bio", user.getBio());
            userData.put("prefAge", user.getPrefAge());
            userData.put("prefLocation", user.getPrefLocation());
            userData.put("prefProfession", user.getPrefProfession());

            // Convert to JSON
            String jsonData = objectMapper.writeValueAsString(userData);

            // Use email index for fast lookup
            String targetUserId = getUserIdByEmail(user.getEmail());

            if (targetUserId != null) {
                // Update user data using the found userId
                String updateUrl = databaseUrl + "/Users/" + targetUserId + ".json?auth=" + apiKey;

                System.out.println("Updating user profile with ID: " + targetUserId);
                System.out.println("Update URL: " + updateUrl);

                HttpRequest updateRequest = HttpRequest.newBuilder()
                        .uri(URI.create(updateUrl))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                        .build();

                HttpResponse<String> updateResponse = httpClient.send(updateRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Update response status: " + updateResponse.statusCode());
                System.out.println("Update response body: " + updateResponse.body());

                if (updateResponse.statusCode() == 200) {
                    System.out.println("User profile updated successfully in Firebase: " + user.getName());
                    return true;
                } else {
                    System.err.println("Firebase profile update failed. Status: " + updateResponse.statusCode());
                    return false;
                }
            } else {
                System.err.println("User not found in email index for email: " + user.getEmail());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating user profile in Firebase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates user login with Firebase
     * 
     * @param email    User's email
     * @param password User's password
     * @return userInfo object if successful, null if failed
     */
    public userInfo loginUser(String email, String password) {
        try {
            // Use email index for fast lookup
            String userId = getUserIdByEmail(email);

            if (userId != null) {
                // Get user data directly using the userId
                String getUserUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

                System.out.println("Fetching user data for login: " + userId);
                System.out.println("User data URL: " + getUserUrl);

                HttpRequest getRequest = HttpRequest.newBuilder()
                        .uri(URI.create(getUserUrl))
                        .GET()
                        .build();

                HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

                System.out.println("User data response status: " + getResponse.statusCode());
                System.out.println("User data response body: " + getResponse.body());

                if (getResponse.statusCode() == 200) {
                    JsonNode userNode = objectMapper.readTree(getResponse.body());

                    if (userNode != null && !userNode.isNull()) {
                        // Check password (Note: In production, use proper password hashing)
                        String storedPassword = userNode.get("password").asText();

                        if (password.equals(storedPassword)) {
                            // Create and return userInfo object with retrieved data
                            userInfo user = new userInfo(
                                    userNode.get("name").asText(""),
                                    userNode.get("email").asText(""),
                                    userNode.get("password").asText(""),
                                    userNode.get("dob").asText(""),
                                    userNode.get("gender").asText(""),
                                    userNode.get("religion").asText(""),
                                    userNode.get("city").asText(""),
                                    userNode.get("image").asText(""),
                                    userNode.get("education").asText(""),
                                    userNode.get("profession").asText(""),
                                    userNode.get("income").asText(""),
                                    userNode.get("bio").asText(""),
                                    userNode.get("prefAge").asText(""),
                                    userNode.get("prefLocation").asText(""),
                                    userNode.get("prefProfession").asText(""));

                            System.out.println("User login successful: " + email);
                            return user;
                        } else {
                            showErrorAlert("Login Failed",
                                    "Invalid password. Please check your password and try again.");
                            System.err.println("Invalid password for user: " + email);
                            return null;
                        }
                    } else {
                        showErrorAlert("Login Failed", "User data not found. Please try again.");
                        System.err.println("User data is null for userId: " + userId);
                        return null;
                    }
                } else {
                    showErrorAlert("Login Failed", "Unable to fetch user data. Please check your internet connection.");
                    System.err.println("Firebase user data fetch failed. Status: " + getResponse.statusCode());
                    return null;
                }
            } else {
                showErrorAlert("Login Failed",
                        "No account found with this email address. Please check your email or register first.");
                System.err.println("User not found in email index: " + email);
                return null;
            }
        } catch (Exception e) {
            showErrorAlert("Login Error", "An unexpected error occurred during login. Please try again.");
            System.err.println("Error during user login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if an email is already registered in the system
     * 
     * @param email The email to check
     * @return true if email is already registered, false otherwise
     */
    public boolean isEmailAlreadyRegistered(String email) {
        try {
            // Use email index for fast lookup
            String userId = getUserIdByEmail(email);

            if (userId != null) {
                System.out.println("Email already registered: " + email + " (userId: " + userId + ")");
                return true;
            } else {
                System.out.println("Email not registered: " + email);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error checking email registration status: " + e.getMessage());
            e.printStackTrace();
            // In case of error, assume email is not registered to allow registration
            // attempt
            return false;
        }
    }

    /**
     * Adds email to index for faster user lookups
     * 
     * @param email  The user's email
     * @param userId The user's ID
     * @return true if successful, false otherwise
     */
    private boolean addEmailToIndex(String email, String userId) {
        try {
            // Add to AllEmails index: PUT /AllEmails/{email}.json
            String emailKey = email.replace(".", "_DOT_").replace("@", "_AT_"); // Firebase keys can't contain . or @
            String indexUrl = databaseUrl + "/AllEmails/" + emailKey + ".json?auth=" + apiKey;

            System.out.println("Adding email to index: " + email + " -> " + userId);
            System.out.println("Index URL: " + indexUrl);

            HttpRequest indexRequest = HttpRequest.newBuilder()
                    .uri(URI.create(indexUrl))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString("\"" + userId + "\"")) // Store the userId as a JSON string
                    .build();

            HttpResponse<String> indexResponse = httpClient.send(indexRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Email index response status: " + indexResponse.statusCode());
            System.out.println("Email index response body: " + indexResponse.body());

            if (indexResponse.statusCode() == 200) {
                System.out.println("Email index created successfully for: " + email);
                return true;
            } else {
                System.err.println("Failed to create email index. Status: " + indexResponse.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error creating email index: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets userId from email index for faster lookups
     * 
     * @param email The user's email
     * @return userId if found, null otherwise
     */
    private String getUserIdByEmail(String email) {
        try {
            String emailKey = email.replace(".", "_DOT_").replace("@", "_AT_");
            String indexUrl = databaseUrl + "/AllEmails/" + emailKey + ".json?auth=" + apiKey;

            System.out.println("Looking up userId for email: " + email);
            System.out.println("Index lookup URL: " + indexUrl);

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(indexUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Email lookup response status: " + getResponse.statusCode());
            System.out.println("Email lookup response body: " + getResponse.body());

            if (getResponse.statusCode() == 200) {
                String responseBody = getResponse.body();
                if (!responseBody.equals("null") && !responseBody.isEmpty()) {
                    // Remove quotes from the response (since we stored it as a JSON string)
                    String userId = responseBody.replace("\"", "");
                    System.out.println("Found userId from index: " + userId + " for email: " + email);
                    return userId;
                }
            }

            System.out.println("No userId found in index for email: " + email);
            return null;

        } catch (Exception e) {
            System.err.println("Error looking up userId by email: " + e.getMessage());
            return null;
        }
    }
}
