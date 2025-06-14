package com.bytebender.premnoybiye.DBConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bytebender.premnoybiye.Component.DialogUtils;
import javafx.application.Platform;

public class FirebaseConnection {
    private String apiKey;
    private String projectId;
    private String storageBucket;
    private ObjectMapper objectMapper;
    private HttpClient httpClient;
    private String firestoreBaseUrl;

    public FirebaseConnection() {
        loadFirebaseConfig();
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
        this.firestoreBaseUrl = "https://firestore.googleapis.com/v1/projects/" + projectId
                + "/databases/(default)/documents";
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
            this.projectId = properties.getProperty("projectId");
            this.storageBucket = properties.getProperty("storageBucket");

            System.out.println("Firebase config loaded successfully");
            System.out.println("Project ID: " + this.projectId);
            System.out.println("Storage Bucket: " + this.storageBucket);

        } catch (IOException e) {
            System.err.println("Error loading Firebase config: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            DialogUtils.showErrorAlert(title, message);
        });
    }

    /**
     * Converts data to Firestore document format
     */
    private Map<String, Object> toFirestoreDocument(Map<String, Object> data) {
        Map<String, Object> document = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Map<String, Object> field = new HashMap<>();
            Object value = entry.getValue();

            if (value instanceof String) {
                field.put("stringValue", value);
            } else if (value instanceof Number) {
                field.put("integerValue", value.toString());
            } else if (value instanceof Boolean) {
                field.put("booleanValue", value);
            } else if (value instanceof java.util.List) {
                // Handle arrays/lists
                Map<String, Object> arrayValue = new HashMap<>();
                java.util.List<Map<String, Object>> values = new java.util.ArrayList<>();

                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) value;

                for (Object item : list) {
                    Map<String, Object> valueMap = new HashMap<>();
                    valueMap.put("stringValue", item.toString());
                    values.add(valueMap);
                }

                arrayValue.put("values", values);
                field.put("arrayValue", arrayValue);
            } else {
                field.put("stringValue", value != null ? value.toString() : "");
            }

            fields.put(entry.getKey(), field);
        }

        document.put("fields", fields);
        return document;
    }

    /**
     * Converts Firestore document to regular data format
     */
    private Map<String, Object> fromFirestoreDocument(JsonNode document) {
        Map<String, Object> data = new HashMap<>();

        if (document.has("fields")) {
            JsonNode fields = document.get("fields");
            fields.fieldNames().forEachRemaining(fieldName -> {
                JsonNode field = fields.get(fieldName);

                if (field.has("stringValue")) {
                    data.put(fieldName, field.get("stringValue").asText());
                } else if (field.has("integerValue")) {
                    data.put(fieldName, field.get("integerValue").asText());
                } else if (field.has("booleanValue")) {
                    data.put(fieldName, field.get("booleanValue").asBoolean());
                } else if (field.has("arrayValue")) {
                    // Handle arrays
                    java.util.List<String> arrayList = new java.util.ArrayList<>();
                    JsonNode arrayValue = field.get("arrayValue");

                    if (arrayValue.has("values")) {
                        JsonNode values = arrayValue.get("values");
                        for (JsonNode valueNode : values) {
                            if (valueNode.has("stringValue")) {
                                arrayList.add(valueNode.get("stringValue").asText());
                            }
                        }
                    }

                    data.put(fieldName, arrayList);
                } else {
                    data.put(fieldName, "");
                }
            });
        }

        return data;
    }

    /**
     * Registers a new user in Firebase with Firebase Authentication and stores
     * profile in Firestore
     * 
     * @param name     User's name
     * @param email    User's email
     * @param password User's password
     * @return String userId if successful, null if failed
     */
    public String registerUser(String name, String email, String password) {
        try {
            if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                showErrorAlert("Registration Error", "Name, email, and password must not be empty");
                return null;
            }

            // Step 1: Create user with Firebase Authentication
            String userId = createFirebaseAuthUser(email, password);
            if (userId == null) {
                return null; // Error already shown in createFirebaseAuthUser
            }

            // Step 2: Create user profile in Firestore using Firebase Auth UID as document
            // ID
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
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

            // Convert to Firestore document format
            Map<String, Object> firestoreDoc = toFirestoreDocument(userData);
            String jsonData = objectMapper.writeValueAsString(firestoreDoc);

            // Create HTTP PATCH request to Firestore
            String url = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // User profile created successfully
                System.out.println(
                        "User registered successfully in Firestore: " + name + " (" + email + ") userId: " + userId);
                return userId; // Return the Firebase Auth UID as primary key
            } else {
                showErrorAlert("Registration Failed",
                        "Failed to create user profile. Please check your internet connection and try again.");
                System.err.println("Firestore user profile creation failed. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());

                // Note: We could delete the Firebase Auth user here, but we'd need the ID token
                // For now, just log the issue - the user can try registering again
                System.err.println("Warning: Firebase Auth user created but profile creation failed for: " + email);
                return null;
            }

        } catch (Exception e) {
            showErrorAlert("Registration Error", "An unexpected error occurred during registration. Please try again.");
            System.err.println("Error registering user in Firestore: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates user profile information in Firestore
     * 
     * @param userInfo     Complete user information object
     * @param currentEmail The current email (before any changes) for lookup
     * @return boolean indicating success or failure
     */
    public boolean updateUserProfile(userInfo user, String currentEmail) {
        try {
            // Use current email to find the userId by querying users collection
            String targetUserId = getUserIdFromEmail(currentEmail);

            if (targetUserId != null) {
                return updateUserProfileByUserId(user, targetUserId);
            } else {
                System.err.println("User not found for email: " + currentEmail);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating user profile in Firestore: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates user profile information in Firebase (overloaded method for backward
     * compatibility)
     * Uses the userId from the user object if available, otherwise falls back to
     * email lookup
     * 
     * @param userInfo Complete user information object
     * @return boolean indicating success or failure
     */
    public boolean updateUserProfile(userInfo user) {
        // If user has userId, use it directly instead of email lookup
        if (user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
            return updateUserProfileByUserId(user, user.getUserId());
        }

        // Fall back to email-based lookup
        return updateUserProfile(user, user.getEmail());
    }

    /**
     * Updates user profile using userId directly (most efficient method)
     * 
     * @param user   The user information object
     * @param userId The userId to update
     * @return boolean indicating success or failure
     */
    private boolean updateUserProfileByUserId(userInfo user, String userId) {
        try {
            // Get current user data to check if email is changing
            String getCurrentUrl = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            HttpRequest getCurrentRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getCurrentUrl))
                    .GET()
                    .build();

            HttpResponse<String> getCurrentResponse = httpClient.send(getCurrentRequest,
                    HttpResponse.BodyHandlers.ofString());

            String oldEmail = "";
            String newEmail = user.getEmail();
            boolean emailChanged = false;

            if (getCurrentResponse.statusCode() == 200) {
                JsonNode currentUserDoc = objectMapper.readTree(getCurrentResponse.body());
                if (currentUserDoc != null && !currentUserDoc.isNull()) {
                    Map<String, Object> currentData = fromFirestoreDocument(currentUserDoc);
                    oldEmail = (String) currentData.getOrDefault("email", "");
                    emailChanged = !oldEmail.equals(newEmail);
                }
            }

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
            userData.put("userId", userId); // Ensure userId is maintained

            // Convert to Firestore document format
            Map<String, Object> firestoreDoc = toFirestoreDocument(userData);
            String jsonData = objectMapper.writeValueAsString(firestoreDoc);

            // Update user data using the userId
            String updateUrl = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            System.out.println("Updating user profile with ID: " + userId);
            System.out.println("Update URL: " + updateUrl);
            if (emailChanged) {
                System.out.println("Email changing from: " + oldEmail + " to: " + newEmail);
            }

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
                System.out.println("User profile updated successfully in Firestore: " + user.getName());
                return true;
            } else {
                System.err.println("Firestore profile update failed. Status: " + updateResponse.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating user profile in Firestore: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates user login with Firebase Authentication and retrieves profile
     * from Firestore
     * 
     * @param email    User's email
     * @param password User's password
     * @return userInfo object if successful, null if failed
     */
    public userInfo loginUser(String email, String password) {
        try {
            // Step 1: Authenticate with Firebase Authentication
            String userId = authenticateFirebaseUser(email, password);
            if (userId == null) {
                return null; // Error already shown in authenticateFirebaseUser
            }

            // Step 2: Get user profile from Firestore using userId
            String getUserUrl = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            System.out.println("Fetching user data for login: " + userId);
            System.out.println("User data URL: " + getUserUrl);

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("User data response status: " + getResponse.statusCode());

            if (getResponse.statusCode() == 200) {
                JsonNode userDoc = objectMapper.readTree(getResponse.body());

                if (userDoc != null && !userDoc.isNull()) {
                    // Convert Firestore document to regular data format
                    Map<String, Object> userData = fromFirestoreDocument(userDoc); // Create userInfo object with
                                                                                   // retrieved data
                    userInfo user = new userInfo(
                            (String) userData.getOrDefault("name", ""),
                            (String) userData.getOrDefault("email", ""),
                            (String) userData.getOrDefault("dob", ""),
                            (String) userData.getOrDefault("gender", ""),
                            (String) userData.getOrDefault("religion", ""),
                            (String) userData.getOrDefault("city", ""),
                            (String) userData.getOrDefault("image", ""),
                            (String) userData.getOrDefault("education", ""),
                            (String) userData.getOrDefault("profession", ""),
                            (String) userData.getOrDefault("income", ""),
                            (String) userData.getOrDefault("bio", ""),
                            (String) userData.getOrDefault("prefAge", ""),
                            (String) userData.getOrDefault("prefLocation", ""),
                            (String) userData.getOrDefault("prefProfession", ""),
                            userId);

                    // Set liked and rejected users lists
                    @SuppressWarnings("unchecked")
                    java.util.List<String> likedUsers = (java.util.List<String>) userData.getOrDefault("likedUsers",
                            new java.util.ArrayList<String>());
                    @SuppressWarnings("unchecked")
                    java.util.List<String> rejectedUsers = (java.util.List<String>) userData
                            .getOrDefault("rejectedUsers", new java.util.ArrayList<String>());

                    user.setLikedUsers(likedUsers);
                    user.setRejectedUsers(rejectedUsers);

                    System.out.println("User login successful: " + email + " (userId: " + userId + ")");
                    return user;
                } else {
                    showErrorAlert("Login Failed", "User profile not found. Please contact support.");
                    System.err.println("User data is null for userId: " + userId);
                    return null;
                }
            } else {
                showErrorAlert("Login Failed",
                        "Unable to fetch user profile. Please check your internet connection.");
                System.err.println("Firestore user data fetch failed. Status: " + getResponse.statusCode());
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
     * Checks if an email is already registered in Firebase Authentication
     * 
     * @param email The email to check
     * @return true if email is already registered, false otherwise
     */
    public boolean isEmailAlreadyRegistered(String email) {
        try {
            // Try to create a temporary user with Firebase Auth to check if email exists
            // This will fail if email is already in use
            String tempPassword = "tempPassword123!";
            String tempUserId = createFirebaseAuthUser(email, tempPassword);

            if (tempUserId != null) {
                // Email is not registered, delete the temporary user we just created
                deleteFirebaseAuthUser(tempUserId);
                System.out.println("Email not registered: " + email);
                return false;
            } else {
                // Creation failed, likely because email is already registered
                System.out.println("Email already registered: " + email);
                return true;
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
     * Public method to get userId by email for use by controllers
     * Queries the users collection directly to find the user with matching email
     * 
     * @param email The user's email
     * @return userId if found, null otherwise
     */
    public String getUserIdFromEmail(String email) {
        try {
            // Query all users to find the one with matching email
            String url = firestoreBaseUrl + "/users";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());

                if (jsonResponse.has("documents")) {
                    JsonNode documents = jsonResponse.get("documents");

                    for (JsonNode document : documents) {
                        try {
                            Map<String, Object> userData = fromFirestoreDocument(document);
                            String userEmail = (String) userData.get("email");

                            if (email.equals(userEmail)) {
                                String userId = (String) userData.get("userId");
                                System.out.println("Found userId: " + userId + " for email: " + email);
                                return userId;
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing user document: " + e.getMessage());
                            continue;
                        }
                    }
                }
            }

            System.out.println("No userId found for email: " + email);
            return null;

        } catch (Exception e) {
            System.err.println("Error looking up userId by email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get userId directly from the userInfo object if it exists
     * Falls back to email lookup if userId is not available
     * 
     * @param user The user object
     * @return userId if found, null otherwise
     */
    public String getUserId(userInfo user) {
        // First, try to get userId from the user object itself
        if (user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
            return user.getUserId();
        }

        // Fall back to email lookup
        return getUserIdFromEmail(user.getEmail());
    }

    /**
     * Upload an image to Firebase Storage and return the download URL
     * 
     * @param imageFile The image file to upload
     * @param userId    The user ID for renaming the file
     * @return String download URL if successful, null if failed
     */
    public String uploadImageToStorage(File imageFile, String userId) {
        try {
            if (imageFile == null || !imageFile.exists()) {
                System.err.println("Image file does not exist");
                return null;
            }

            // Get file extension from original file
            String originalName = imageFile.getName();
            String extension = "";
            int lastDotIndex = originalName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalName.substring(lastDotIndex);
            } // Create new filename with userId inside Prem-Noy-Biye folder
            String newFileName = "Prem-Noy-Biye/" + userId + extension;
            String contentType = getContentType(originalName);

            System.out.println("Uploading image to Firebase Storage");
            System.out.println("Original filename: " + originalName);
            System.out.println("New filename: " + newFileName);
            System.out.println("Content type: " + contentType);

            // Read file as bytes
            byte[] fileBytes;
            try (FileInputStream fis = new FileInputStream(imageFile)) {
                fileBytes = fis.readAllBytes();
            }

            // Upload to Firebase Storage (encode the full path including folder)
            String uploadUrl = "https://firebasestorage.googleapis.com/v0/b/" + storageBucket +
                    "/o/" + java.net.URLEncoder.encode(newFileName, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse response to get download token
                JsonNode responseJson = objectMapper.readTree(response.body());
                String downloadToken = responseJson.get("downloadTokens").asText();

                // Construct download URL
                String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/" + storageBucket +
                        "/o/" + java.net.URLEncoder.encode(newFileName, "UTF-8") +
                        "?alt=media&token=" + downloadToken;

                System.out.println("Image uploaded successfully");
                System.out.println("Download URL: " + downloadUrl);
                return downloadUrl;
            } else {
                System.err.println("Failed to upload image. Status code: " + response.statusCode());
                System.err.println("Response body: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error uploading image to Firebase Storage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Upload an image to Firebase Storage using ID token for authentication
     * 
     * @param imageFile The image file to upload
     * @param userId    The user ID for renaming the file
     * @param idToken   The Firebase ID token for authentication
     * @return String download URL if successful, null if failed
     */
    public String uploadImageToStorageWithToken(File imageFile, String userId, String idToken) {
        try {
            if (imageFile == null || !imageFile.exists()) {
                System.err.println("Image file does not exist");
                return null;
            }

            // Use the provided ID token for authentication
            if (idToken == null || idToken.trim().isEmpty()) {
                System.err.println("ID token is required for authenticated storage upload");
                return null;
            }

            // Get file extension from original file
            String originalName = imageFile.getName();
            String extension = "";
            int lastDotIndex = originalName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalName.substring(lastDotIndex);
            }

            // Create new filename with userId inside Prem-Noy-Biye folder
            String newFileName = "Prem-Noy-Biye/" + userId + extension;
            String contentType = getContentType(originalName);

            System.out.println("Uploading image to Firebase Storage with authentication");
            System.out.println("Original filename: " + originalName);
            System.out.println("New filename: " + newFileName);
            System.out.println("Content type: " + contentType);

            // Read file as bytes
            byte[] fileBytes;
            try (FileInputStream fis = new FileInputStream(imageFile)) {
                fileBytes = fis.readAllBytes();
            }

            // Upload to Firebase Storage (encode the full path including folder)
            String uploadUrl = "https://firebasestorage.googleapis.com/v0/b/" + storageBucket +
                    "/o/" + java.net.URLEncoder.encode(newFileName, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", contentType)
                    .header("Authorization", "Bearer " + idToken)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse response to get download token
                JsonNode responseJson = objectMapper.readTree(response.body());
                String downloadToken = responseJson.get("downloadTokens").asText();

                // Construct download URL
                String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/" + storageBucket +
                        "/o/" + java.net.URLEncoder.encode(newFileName, "UTF-8") +
                        "?alt=media&token=" + downloadToken;

                System.out.println("Image uploaded successfully with authentication");
                System.out.println("Download URL: " + downloadUrl);
                return downloadUrl;
            } else {
                System.err.println("Failed to upload image. Status code: " + response.statusCode());
                System.err.println("Response body: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error uploading image to Firebase Storage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Public method to get Firebase ID token for a user (used for authenticated
     * operations)
     * 
     * @param email    User's email
     * @param password User's password
     * @return String ID token if successful, null if failed
     */
    public String getIdTokenForUser(String email, String password) {
        return getFirebaseIdToken(email, password);
    }

    // ========== Firebase Authentication Helper Methods ==========

    /**
     * Creates a new user with Firebase Authentication
     * 
     * @param email    User's email
     * @param password User's password
     * @return Firebase Auth UID if successful, null if failed
     */
    private String createFirebaseAuthUser(String email, String password) {
        try {
            String authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + apiKey;

            Map<String, Object> authData = new HashMap<>();
            authData.put("email", email);
            authData.put("password", password);
            authData.put("returnSecureToken", true);

            String jsonData = objectMapper.writeValueAsString(authData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Firebase Auth registration response status: " + response.statusCode());
            System.out.println("Firebase Auth registration response body: " + response.body());

            if (response.statusCode() == 200) {
                JsonNode responseNode = objectMapper.readTree(response.body());
                String localId = responseNode.get("localId").asText();
                System.out.println("Firebase Auth user created successfully with UID: " + localId);
                return localId;
            } else {
                // Parse error response
                try {
                    JsonNode errorNode = objectMapper.readTree(response.body());
                    if (errorNode.has("error")) {
                        JsonNode errorDetails = errorNode.get("error");
                        String message = errorDetails.get("message").asText();

                        if (message.contains("EMAIL_EXISTS")) {
                            showErrorAlert("Registration Error",
                                    "An account with this email address already exists. Please use a different email or try logging in.");
                        } else if (message.contains("WEAK_PASSWORD")) {
                            showErrorAlert("Registration Error",
                                    "Password should be at least 6 characters long.");
                        } else if (message.contains("INVALID_EMAIL")) {
                            showErrorAlert("Registration Error",
                                    "Please enter a valid email address.");
                        } else {
                            showErrorAlert("Registration Error",
                                    "Registration failed: " + message);
                        }
                    }
                } catch (Exception e) {
                    showErrorAlert("Registration Error",
                            "Registration failed. Please check your internet connection and try again.");
                }
                return null;
            }

        } catch (Exception e) {
            showErrorAlert("Registration Error",
                    "An unexpected error occurred during registration. Please try again.");
            System.err.println("Error creating Firebase Auth user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Authenticates user with Firebase Authentication
     * 
     * @param email    User's email
     * @param password User's password
     * @return Firebase Auth UID if successful, null if failed
     */
    private String authenticateFirebaseUser(String email, String password) {
        try {
            String authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

            Map<String, Object> authData = new HashMap<>();
            authData.put("email", email);
            authData.put("password", password);
            authData.put("returnSecureToken", true);

            String jsonData = objectMapper.writeValueAsString(authData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Firebase Auth login response status: " + response.statusCode());
            System.out.println("Firebase Auth login response body: " + response.body());

            if (response.statusCode() == 200) {
                JsonNode responseNode = objectMapper.readTree(response.body());
                String localId = responseNode.get("localId").asText();
                System.out.println("Firebase Auth login successful with UID: " + localId);
                return localId;
            } else {
                // Parse error response
                try {
                    JsonNode errorNode = objectMapper.readTree(response.body());
                    if (errorNode.has("error")) {
                        JsonNode errorDetails = errorNode.get("error");
                        String message = errorDetails.get("message").asText();

                        if (message.contains("EMAIL_NOT_FOUND")) {
                            showErrorAlert("Login Failed",
                                    "No account found with this email address. Please check your email or register first.");
                        } else if (message.contains("INVALID_PASSWORD")) {
                            showErrorAlert("Login Failed",
                                    "Invalid password. Please check your password and try again.");
                        } else if (message.contains("USER_DISABLED")) {
                            showErrorAlert("Login Failed",
                                    "This account has been disabled. Please contact support.");
                        } else {
                            showErrorAlert("Login Failed",
                                    "Login failed: " + message);
                        }
                    }
                } catch (Exception e) {
                    showErrorAlert("Login Error",
                            "Login failed. Please check your internet connection and try again.");
                }
                return null;
            }

        } catch (Exception e) {
            showErrorAlert("Login Error",
                    "An unexpected error occurred during login. Please try again.");
            System.err.println("Error authenticating with Firebase Auth: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes a Firebase Authentication user
     * 
     * @param userId The Firebase Auth UID
     * @return true if successful, false if failed
     */
    private boolean deleteFirebaseAuthUser(String userId) {
        try {
            // Delete the Firebase Authentication user account
            String deleteUrl = "https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + apiKey;

            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("localId", userId);

            String jsonData = objectMapper.writeValueAsString(deleteData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Firebase Auth user deletion response status: " + response.statusCode());
            System.out.println("Firebase Auth user deletion response body: " + response.body());
            if (response.statusCode() == 200) {
                System.out.println("Firebase Auth user deleted successfully: " + userId);
                return true;
            } else {
                System.err.println("Failed to delete Firebase Auth user: " + userId);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting Firebase Auth user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets Firebase ID token for authenticated user
     * 
     * @param email    User's email
     * @param password User's password
     * @return ID token if successful, null if failed
     */
    private String getFirebaseIdToken(String email, String password) {
        try {
            String authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

            Map<String, Object> authData = new HashMap<>();
            authData.put("email", email);
            authData.put("password", password);
            authData.put("returnSecureToken", true);

            String jsonData = objectMapper.writeValueAsString(authData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Firebase Auth token request response status: " + response.statusCode());

            if (response.statusCode() == 200) {
                JsonNode responseNode = objectMapper.readTree(response.body());
                String idToken = responseNode.get("idToken").asText();
                System.out.println("Firebase ID token retrieved successfully");
                return idToken;
            } else {
                // Parse error response
                try {
                    JsonNode errorNode = objectMapper.readTree(response.body());
                    if (errorNode.has("error")) {
                        JsonNode errorDetails = errorNode.get("error");
                        String message = errorDetails.get("message").asText();

                        if (message.contains("INVALID_PASSWORD")) {
                            showErrorAlert("Password Change Failed",
                                    "Current password is incorrect. Please try again.");
                        } else if (message.contains("EMAIL_NOT_FOUND")) {
                            showErrorAlert("Password Change Failed",
                                    "Account not found. Please contact support.");
                        } else {
                            showErrorAlert("Password Change Failed",
                                    "Authentication failed: " + message);
                        }
                    }
                } catch (Exception e) {
                    showErrorAlert("Password Change Error",
                            "Authentication failed. Please check your current password and try again.");
                }
                return null;
            }

        } catch (Exception e) {
            showErrorAlert("Password Change Error",
                    "An unexpected error occurred during authentication. Please try again.");
            System.err.println("Error getting Firebase ID token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Changes password using Firebase Authentication
     * 
     * @param idToken     Firebase ID token for authentication
     * @param newPassword The new password to set
     * @return true if successful, false if failed
     */
    private boolean changeFirebaseAuthPassword(String idToken, String newPassword) {
        try {
            String changePasswordUrl = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=" + apiKey;

            Map<String, Object> changeData = new HashMap<>();
            changeData.put("idToken", idToken);
            changeData.put("password", newPassword);
            changeData.put("returnSecureToken", true);

            String jsonData = objectMapper.writeValueAsString(changeData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(changePasswordUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Firebase Auth password change response status: " + response.statusCode());
            System.out.println("Firebase Auth password change response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("Firebase Auth password changed successfully");
                return true;
            } else {
                // Parse error response
                try {
                    JsonNode errorNode = objectMapper.readTree(response.body());
                    if (errorNode.has("error")) {
                        JsonNode errorDetails = errorNode.get("error");
                        String message = errorDetails.get("message").asText();

                        if (message.contains("WEAK_PASSWORD")) {
                            showErrorAlert("Password Change Failed",
                                    "New password should be at least 6 characters long.");
                        } else if (message.contains("INVALID_ID_TOKEN")) {
                            showErrorAlert("Password Change Failed",
                                    "Authentication expired. Please try again.");
                        } else {
                            showErrorAlert("Password Change Failed",
                                    "Password change failed: " + message);
                        }
                    }
                } catch (Exception e) {
                    showErrorAlert("Password Change Error",
                            "Password change failed. Please try again.");
                }
                return false;
            }

        } catch (Exception e) {
            showErrorAlert("Password Change Error",
                    "An unexpected error occurred during password change. Please try again.");
            System.err.println("Error changing Firebase Auth password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes Firebase Authentication user using ID token
     * 
     * @param idToken Firebase ID token for authentication
     * @return true if successful, false if failed
     */
    private boolean deleteFirebaseAuthUserWithToken(String idToken) {
        try {
            String deleteUrl = "https://identitytoolkit.googleapis.com/v1/accounts:delete?key=" + apiKey;

            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("idToken", idToken);

            String jsonData = objectMapper.writeValueAsString(deleteData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Firebase Auth user deletion response status: " + response.statusCode());
            System.out.println("Firebase Auth user deletion response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("Firebase Auth user deleted successfully");
                return true;
            } else {
                System.err.println("Failed to delete Firebase Auth user. Status: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting Firebase Auth user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes all user data from Firestore
     * 
     * @param email The email of the user whose data to delete
     * @return true if the user data was deleted successfully, false otherwise
     */
    private boolean deleteUserData(String email) {
        try {
            // Remove the user from the database
            boolean userRemoved = removeUserFromDatabase(email);

            // Return true if user removal succeeded
            return userRemoved;
        } catch (Exception e) {
            System.err.println("Error deleting user data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes a user from Firestore
     * 
     * @param email The email of the user to remove
     * @return true if the user was removed successfully, false otherwise
     */
    private boolean removeUserFromDatabase(String email) {
        try {
            // First get the userId from email
            String userId = getUserIdFromEmail(email);
            if (userId == null) {
                System.err.println("User not found for email: " + email);
                return false;
            }

            // URL for the user document
            String url = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            // Create DELETE request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("User removed from database successfully: " + email);
                return true;
            } else {
                System.err.println("Failed to remove user from database. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error removing user from database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Determines the content type based on file extension
     * 
     * @param fileName The name of the file
     * @return The MIME content type
     */
    private String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }

        String lowercaseFileName = fileName.toLowerCase();

        if (lowercaseFileName.endsWith(".jpg") || lowercaseFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercaseFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowercaseFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercaseFileName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowercaseFileName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowercaseFileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowercaseFileName.endsWith(".ico")) {
            return "image/x-icon";
        } else if (lowercaseFileName.endsWith(".tiff") || lowercaseFileName.endsWith(".tif")) {
            return "image/tiff";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Changes the password for a user by first authenticating with the current
     * password
     * 
     * @param user            The user whose password to change
     * @param currentPassword The current password for authentication
     * @param newPassword     The new password to set
     * @return true if the password was changed successfully, false otherwise
     */
    public boolean changePassword(userInfo user, String currentPassword, String newPassword) {
        try {
            // First, get an ID token by authenticating with current credentials
            String idToken = getIdTokenForUser(user.getEmail(), currentPassword);
            if (idToken == null) {
                System.err.println("Failed to authenticate user for password change");
                return false;
            }

            // Use the private method to change the password
            return changeFirebaseAuthPassword(idToken, newPassword);
        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user account by first authenticating with the password
     * 
     * @param user     The user to delete
     * @param password The password for authentication
     * @return true if the user was deleted successfully, false otherwise
     */
    public boolean deleteUser(userInfo user, String password) {
        try {
            // First, get an ID token by authenticating with credentials
            String idToken = getIdTokenForUser(user.getEmail(), password);
            if (idToken == null) {
                System.err.println("Failed to authenticate user for account deletion");
                return false;
            }

            // Delete the user data from the database first
            boolean dataDeleted = deleteUserData(user.getEmail());

            // Delete the user authentication account
            boolean authDeleted = deleteFirebaseAuthUserWithToken(idToken);

            // Return true only if both operations succeeded
            return dataDeleted && authDeleted;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all users from Firestore filtered by gender (opposite gender for discover
     * functionality)
     */
    public java.util.List<userInfo> getUsersByGender(String desiredGender) {
        java.util.List<userInfo> users = new java.util.ArrayList<>();

        try {
            // Create the query URL to get all users
            String url = firestoreBaseUrl + "/users";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());

                if (jsonResponse.has("documents")) {
                    JsonNode documents = jsonResponse.get("documents");

                    for (JsonNode document : documents) {
                        try {
                            Map<String, Object> userData = fromFirestoreDocument(document);
                            String gender = (String) userData.get("gender"); // Filter by desired gender
                            if (desiredGender.equalsIgnoreCase(gender)) {
                                userInfo user = new userInfo(
                                        (String) userData.getOrDefault("name", ""),
                                        (String) userData.getOrDefault("email", ""),
                                        (String) userData.getOrDefault("dob", ""),
                                        (String) userData.getOrDefault("gender", ""),
                                        (String) userData.getOrDefault("religion", ""),
                                        (String) userData.getOrDefault("city", ""),
                                        (String) userData.getOrDefault("image", ""),
                                        (String) userData.getOrDefault("education", ""),
                                        (String) userData.getOrDefault("profession", ""),
                                        (String) userData.getOrDefault("income", ""),
                                        (String) userData.getOrDefault("bio", ""),
                                        (String) userData.getOrDefault("prefAge", ""),
                                        (String) userData.getOrDefault("prefLocation", ""),
                                        (String) userData.getOrDefault("prefProfession", ""),
                                        (String) userData.getOrDefault("userId", ""));

                                // Set liked and rejected users lists
                                @SuppressWarnings("unchecked")
                                java.util.List<String> likedUsers = (java.util.List<String>) userData
                                        .getOrDefault("likedUsers", new java.util.ArrayList<String>());
                                @SuppressWarnings("unchecked")
                                java.util.List<String> rejectedUsers = (java.util.List<String>) userData
                                        .getOrDefault("rejectedUsers", new java.util.ArrayList<String>());

                                user.setLikedUsers(likedUsers);
                                user.setRejectedUsers(rejectedUsers);

                                users.add(user);
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing user document: " + e.getMessage());
                        }
                    }
                }

                System.out.println("Retrieved " + users.size() + " users with gender: " + desiredGender);
            } else {
                System.err.println("Failed to get users. Status code: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("Error getting users by gender: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Add a user to the liked users list
     */
    public boolean addToLikedUsers(String userId, String targetUserId) {
        return updateUserInteractionList(userId, targetUserId, "likedUsers", "add");
    }

    /**
     * Add a user to the rejected users list
     */
    public boolean addToRejectedUsers(String userId, String targetUserId) {
        return updateUserInteractionList(userId, targetUserId, "rejectedUsers", "add");
    }

    /**
     * Helper method to update user interaction lists (liked/rejected)
     */
    private boolean updateUserInteractionList(String userId, String targetUserId, String listType, String action) {
        try {
            // First, get the current user data to retrieve existing lists
            String getUserUrl = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (getResponse.statusCode() == 200) {
                JsonNode userDocument = objectMapper.readTree(getResponse.body());
                Map<String, Object> userData = fromFirestoreDocument(userDocument);

                // Get existing list or create new one
                @SuppressWarnings("unchecked")
                java.util.List<String> currentList = (java.util.List<String>) userData.getOrDefault(listType,
                        new java.util.ArrayList<String>());

                // Add target user ID if not already present
                if (!currentList.contains(targetUserId)) {
                    currentList.add(targetUserId);

                    // Update the user data
                    userData.put(listType, currentList);

                    // Convert to Firestore format and update
                    Map<String, Object> firestoreDoc = toFirestoreDocument(userData);
                    String jsonData = objectMapper.writeValueAsString(firestoreDoc);

                    String updateUrl = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

                    HttpRequest updateRequest = HttpRequest.newBuilder()
                            .uri(URI.create(updateUrl))
                            .header("Content-Type", "application/json")
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                            .build();

                    HttpResponse<String> updateResponse = httpClient.send(updateRequest,
                            HttpResponse.BodyHandlers.ofString());

                    if (updateResponse.statusCode() == 200) {
                        System.out.println("Successfully updated " + listType + " for user: " + userId);
                        return true;
                    } else {
                        System.err.println("Failed to update " + listType + ". Status: " + updateResponse.statusCode());
                        return false;
                    }
                } else {
                    System.out.println("Target user already in " + listType + " list");
                    return true; // Already in list, consider it success
                }
            } else {
                System.err.println(
                        "Failed to get user data for updating " + listType + ". Status: " + getResponse.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating " + listType + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
