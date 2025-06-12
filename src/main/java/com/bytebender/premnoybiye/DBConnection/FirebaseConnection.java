package com.bytebender.premnoybiye.DBConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("firebase.properties")) {
            if (input == null) {
                System.err.println("Firebase properties file not found in resources!");
                return;
            }
            
            props.load(input);
            
            this.apiKey = props.getProperty("apiKey");
            this.databaseUrl = props.getProperty("databaseUrl");
            
            System.out.println("Firebase config loaded successfully");
            System.out.println("Database URL: " + this.databaseUrl);
            
        } catch (IOException e) {
            System.err.println("Error loading Firebase config: " + e.getMessage());
        }
    }
      /**
     * Registers a new user in Firebase with basic information
     * @param name User's name
     * @param email User's email
     * @param password User's password
     * @return boolean indicating success or failure
     */
    public boolean registerUser(String name, String email, String password) {
        try {
            // Generate unique user ID
            String userId = UUID.randomUUID().toString();
            
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
                    .build();
            
            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                System.out.println("User registered successfully in Firebase: " + name + " (" + email + ")");
                return true;
            } else {
                System.err.println("Firebase registration failed. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error registering user in Firebase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
      /**
     * Updates user profile information in Firebase
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
            
            // Find user by email first (since we don't store userId in userInfo class)
            String getUserUrl = databaseUrl + "/Users.json?auth=" + apiKey + "&orderBy=\"email\"&equalTo=\"" + user.getEmail() + "\"";
            
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();
            
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            
            if (getResponse.statusCode() == 200) {
                JsonNode responseNode = objectMapper.readTree(getResponse.body());
                
                if (responseNode.size() > 0) {
                    // Get the first (and should be only) user key
                    String userId = responseNode.fieldNames().next();
                    
                    // Update user data
                    String updateUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;
                    
                    HttpRequest updateRequest = HttpRequest.newBuilder()
                            .uri(URI.create(updateUrl))
                            .header("Content-Type", "application/json")
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                            .build();
                    
                    HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
                    
                    if (updateResponse.statusCode() == 200) {
                        System.out.println("User profile updated successfully in Firebase: " + user.getName());
                        return true;
                    } else {
                        System.err.println("Firebase profile update failed. Status: " + updateResponse.statusCode());
                        return false;
                    }
                } else {
                    System.err.println("User not found in Firebase for email: " + user.getEmail());
                    return false;
                }
            } else {
                System.err.println("Failed to fetch user from Firebase. Status: " + getResponse.statusCode());
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
     * @param email User's email
     * @param password User's password
     * @return userInfo object if successful, null if failed
     */
    public userInfo loginUser(String email, String password) {
        try {
            // Query Firebase for user with matching email
            String getUserUrl = databaseUrl + "/Users.json?auth=" + apiKey + "&orderBy=\"email\"&equalTo=\"" + email + "\"";
            
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();
            
            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            
            if (getResponse.statusCode() == 200) {
                JsonNode responseNode = objectMapper.readTree(getResponse.body());
                
                if (responseNode.size() > 0) {
                    // Get the first (and should be only) user
                    JsonNode userNode = responseNode.elements().next();
                    
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
                            userNode.get("prefProfession").asText("")
                        );
                        
                        System.out.println("User login successful: " + email);
                        return user;
                    } else {
                        System.err.println("Invalid password for user: " + email);
                        return null;
                    }
                } else {
                    System.err.println("User not found: " + email);
                    return null;
                }
            } else {
                System.err.println("Firebase login query failed. Status: " + getResponse.statusCode());
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error during user login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
