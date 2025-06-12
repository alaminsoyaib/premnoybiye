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
    private String databaseUrl;
    private String storageBucket;
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
            this.storageBucket = properties.getProperty("storageBucket");

            System.out.println("Firebase config loaded successfully");
            System.out.println("Database URL: " + this.databaseUrl);
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
     * Registers a new user in Firebase with Firebase Authentication
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
            } // Step 1: Create user with Firebase Authentication
            String userId = createFirebaseAuthUser(email, password);
            if (userId == null) {
                return null; // Error already shown in createFirebaseAuthUser
            }

            // Step 2: Create user profile in Realtime Database using Firebase Auth UID as
            // primary key
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
            userData.put("userId", userId); // Firebase Auth UID used as primary key // Convert to JSON
            String jsonData = objectMapper.writeValueAsString(userData);

            // Create HTTP PUT request to Firebase Realtime Database
            String url = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // User profile created successfully, now add email to index for faster lookups
                boolean emailIndexSuccess = addEmailToIndex(email, userId);

                if (emailIndexSuccess) {
                    System.out.println(
                            "User registered successfully in Firebase with email index: " + name + " (" + email
                                    + ") userId: " + userId);
                } else {
                    System.out.println("User registered successfully in Firebase, but email index failed: " + name
                            + " (" + email + ") userId: " + userId);
                }
                return userId; // Return the Firebase Auth UID as primary key
            } else {
                showErrorAlert("Registration Failed",
                        "Failed to create user profile. Please check your internet connection and try again.");
                System.err.println("Firebase user profile creation failed. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());

                // Note: We could delete the Firebase Auth user here, but we'd need the ID token
                // For now, just log the issue - the user can try registering again
                System.err.println("Warning: Firebase Auth user created but profile creation failed for: " + email);
                return null;
            }

        } catch (Exception e) {
            showErrorAlert("Registration Error", "An unexpected error occurred during registration. Please try again.");
            System.err.println("Error registering user in Firebase: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates user profile information in Firebase
     * 
     * @param userInfo     Complete user information object
     * @param currentEmail The current email (before any changes) for lookup
     * @return boolean indicating success or failure
     */
    public boolean updateUserProfile(userInfo user, String currentEmail) {
        try {
            // Use current email to find the userId
            String targetUserId = getUserIdByEmail(currentEmail);

            if (targetUserId != null) {
                // Get current user data to check if email is changing
                String getCurrentUrl = databaseUrl + "/Users/" + targetUserId + ".json?auth=" + apiKey;

                HttpRequest getCurrentRequest = HttpRequest.newBuilder()
                        .uri(URI.create(getCurrentUrl))
                        .GET()
                        .build();

                HttpResponse<String> getCurrentResponse = httpClient.send(getCurrentRequest,
                        HttpResponse.BodyHandlers.ofString());

                String oldEmail = currentEmail;
                String newEmail = user.getEmail();
                boolean emailChanged = false;

                if (getCurrentResponse.statusCode() == 200) {
                    JsonNode currentUserNode = objectMapper.readTree(getCurrentResponse.body());
                    if (currentUserNode != null && !currentUserNode.isNull()) {
                        oldEmail = currentUserNode.get("email").asText("");
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
                userData.put("userId", targetUserId); // Ensure userId is maintained

                // Convert to JSON
                String jsonData = objectMapper.writeValueAsString(userData);

                // Update user data using the found userId
                String updateUrl = databaseUrl + "/Users/" + targetUserId + ".json?auth=" + apiKey;

                System.out.println("Updating user profile with ID: " + targetUserId);
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
                    // If email changed, update the email index
                    if (emailChanged) {
                        System.out.println("Email changed, updating email index...");

                        // Remove old email index
                        boolean oldIndexRemoved = removeEmailFromIndex(oldEmail);

                        // Add new email index
                        boolean newIndexAdded = addEmailToIndex(newEmail, targetUserId);

                        if (oldIndexRemoved && newIndexAdded) {
                            System.out.println("Email index updated successfully");
                        } else {
                            System.err.println("Warning: Email index update may have failed");
                            System.err.println(
                                    "Old index removed: " + oldIndexRemoved + ", New index added: " + newIndexAdded);
                        }
                    }

                    System.out.println("User profile updated successfully in Firebase: " + user.getName());
                    return true;
                } else {
                    System.err.println("Firebase profile update failed. Status: " + updateResponse.statusCode());
                    return false;
                }
            } else {
                System.err.println("User not found in email index for email: " + currentEmail);
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating user profile in Firebase: " + e.getMessage());
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
            String getCurrentUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

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
                JsonNode currentUserNode = objectMapper.readTree(getCurrentResponse.body());
                if (currentUserNode != null && !currentUserNode.isNull()) {
                    oldEmail = currentUserNode.get("email").asText("");
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

            // Convert to JSON
            String jsonData = objectMapper.writeValueAsString(userData);

            // Update user data using the userId
            String updateUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

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
                // If email changed, update the email index
                if (emailChanged) {
                    System.out.println("Email changed, updating email index...");

                    // Remove old email index
                    boolean oldIndexRemoved = removeEmailFromIndex(oldEmail);

                    // Add new email index
                    boolean newIndexAdded = addEmailToIndex(newEmail, userId);

                    System.out.println("Old email index removed: " + oldIndexRemoved);
                    System.out.println("New email index added: " + newIndexAdded);
                }

                System.out.println("User profile updated successfully in Firebase: " + user.getName());
                return true;
            } else {
                System.err.println("Firebase profile update failed. Status: " + updateResponse.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error updating user profile in Firebase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates user login with Firebase Authentication
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

            // Step 2: Get user profile from Realtime Database using userId
            String getUserUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

            System.out.println("Fetching user data for login: " + userId);
            System.out.println("User data URL: " + getUserUrl);

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("User data response status: " + getResponse.statusCode());

            if (getResponse.statusCode() == 200) {
                JsonNode userNode = objectMapper.readTree(getResponse.body());

                if (userNode != null && !userNode.isNull()) {
                    // Create userInfo object with retrieved data
                    userInfo user = new userInfo(
                            userNode.get("name").asText(""),
                            userNode.get("email").asText(""),
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
                            userNode.get("prefProfession").asText(""),
                            userId);

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
                System.err.println("Firebase user data fetch failed. Status: " + getResponse.statusCode());
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

    /**
     * Public method to get userId by email for use by controllers
     * 
     * @param email The user's email
     * @return userId if found, null otherwise
     */
    public String getUserIdFromEmail(String email) {
        return getUserIdByEmail(email);
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
        return getUserIdByEmail(user.getEmail());
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
     * Helper method to determine content type based on file extension
     */
    private String getContentType(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerCaseFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerCaseFileName.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Removes email from index when email is changed or user is deleted
     * 
     * @param email The email to remove from index
     * @return true if successful, false otherwise
     */
    private boolean removeEmailFromIndex(String email) {
        try {
            String emailKey = email.replace(".", "_DOT_").replace("@", "_AT_");
            String indexUrl = databaseUrl + "/AllEmails/" + emailKey + ".json?auth=" + apiKey;

            System.out.println("Removing email from index: " + email);
            System.out.println("Index removal URL: " + indexUrl);

            HttpRequest deleteRequest = HttpRequest.newBuilder()
                    .uri(URI.create(indexUrl))
                    .DELETE()
                    .build();

            HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Email index removal response status: " + deleteResponse.statusCode());
            System.out.println("Email index removal response body: " + deleteResponse.body());

            if (deleteResponse.statusCode() == 200) {
                System.out.println("Email index removed successfully for: " + email);
                return true;
            } else {
                System.err.println("Failed to remove email index. Status: " + deleteResponse.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error removing email index: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Changes user password using userId as primary key with Firebase
     * Authentication
     * 
     * @param userId          The user's ID
     * @param currentPassword The current password for verification
     * @param newPassword     The new password to set
     * @return boolean indicating success or failure
     */
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                showErrorAlert("Password Change Error", "User ID is required for password change");
                return false;
            }

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                showErrorAlert("Password Change Error", "Current password is required");
                return false;
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                showErrorAlert("Password Change Error", "New password cannot be empty");
                return false;
            }

            // Get user email from database to authenticate with Firebase Auth
            String getUserUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (getResponse.statusCode() == 200) {
                JsonNode userNode = objectMapper.readTree(getResponse.body());

                if (userNode != null && !userNode.isNull()) {
                    String email = userNode.get("email").asText();

                    // Step 1: Authenticate with current password using Firebase Auth
                    String idToken = getFirebaseIdToken(email, currentPassword);
                    if (idToken == null) {
                        return false; // Error already shown in getFirebaseIdToken
                    }

                    // Step 2: Change password with Firebase Auth
                    boolean success = changeFirebaseAuthPassword(idToken, newPassword);

                    if (success) {
                        System.out.println("Password updated successfully for userId: " + userId);
                        return true;
                    } else {
                        return false; // Error already shown in changeFirebaseAuthPassword
                    }
                } else {
                    showErrorAlert("Password Change Failed", "User data not found");
                    System.err.println("User data is null for userId: " + userId);
                    return false;
                }
            } else {
                showErrorAlert("Password Change Failed", "Unable to fetch user data");
                System.err
                        .println("Failed to fetch user data for password change. Status: " + getResponse.statusCode());
                return false;
            }

        } catch (Exception e) {
            showErrorAlert("Password Change Error", "An unexpected error occurred. Please try again.");
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Changes user password using userInfo object (convenience method)
     * 
     * @param user            The user object (must contain userId)
     * @param currentPassword The current password for verification
     * @param newPassword     The new password to set
     * @return boolean indicating success or failure
     */
    public boolean changePassword(userInfo user, String currentPassword, String newPassword) {
        String userId = getUserId(user);
        if (userId == null) {
            showErrorAlert("Password Change Error", "Unable to identify user for password change");
            return false;
        }
        return changePassword(userId, currentPassword, newPassword);
    }

    /**
     * Verifies that a password change was successful by attempting Firebase Auth
     * login
     * This is useful for testing password change functionality
     * 
     * @param email       User's email
     * @param newPassword The new password to verify
     * @return boolean indicating if login with new password succeeds
     */
    public boolean verifyPasswordChange(String email, String newPassword) {
        try {
            // Attempt authentication with Firebase Auth using new password
            String userId = authenticateFirebaseUser(email, newPassword);

            if (userId != null) {
                System.out.println("Password verification for " + email + ": SUCCESS");
                return true;
            } else {
                System.out.println("Password verification for " + email + ": FAILED");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error verifying password change: " + e.getMessage());
            return false;
        }
    }

    /**
     * Permanently deletes a user account and all associated data
     * This includes user data, email index, Firebase Storage images, and Firebase
     * Auth account
     * 
     * @param user     The user object to delete
     * @param password The user's password for verification
     * @return boolean indicating success or failure
     */
    public boolean deleteUser(userInfo user, String password) {
        try {
            if (user == null) {
                showErrorAlert("Delete Account Error", "No user provided for deletion");
                return false;
            }

            if (password == null || password.trim().isEmpty()) {
                showErrorAlert("Delete Account Error", "Password is required for account deletion");
                return false;
            }

            String userId = getUserId(user);
            if (userId == null) {
                showErrorAlert("Delete Account Error", "Unable to identify user for deletion");
                return false;
            }

            System.out.println("Starting account deletion process for userId: " + userId);

            // Step 1: Verify password with Firebase Authentication
            String email = user.getEmail();
            String idToken = getFirebaseIdToken(email, password);
            if (idToken == null) {
                return false; // Error already shown in getFirebaseIdToken
            }

            System.out.println("Password verified with Firebase Auth. Proceeding with account deletion...");

            // Step 2: Get user data to check for image
            String getUserUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            JsonNode userNode = null;
            if (getResponse.statusCode() == 200) {
                userNode = objectMapper.readTree(getResponse.body());
            }

            // Step 3: Delete user image from Firebase Storage (if exists)
            if (userNode != null && userNode.has("image")) {
                String imageUrl = userNode.get("image").asText("");
                if (!imageUrl.isEmpty()) {
                    boolean imageDeleted = deleteImageFromStorage(userId);
                    if (imageDeleted) {
                        System.out.println("User image deleted from Firebase Storage");
                    } else {
                        System.err.println("Warning: Failed to delete user image from Firebase Storage");
                        // Continue with deletion even if image deletion fails
                    }
                }
            }

            // Step 4: Remove email from index
            boolean emailIndexRemoved = removeEmailFromIndex(email);
            if (emailIndexRemoved) {
                System.out.println("Email index removed successfully for: " + email);
            } else {
                System.err.println("Warning: Failed to remove email index for: " + email);
                // Continue with deletion even if email index removal fails
            }

            // Step 5: Delete user data from Firebase Realtime Database
            String deleteUserUrl = databaseUrl + "/Users/" + userId + ".json?auth=" + apiKey;

            HttpRequest deleteUserRequest = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUserUrl))
                    .DELETE()
                    .build();

            HttpResponse<String> deleteUserResponse = httpClient.send(deleteUserRequest,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("User data deletion response status: " + deleteUserResponse.statusCode());
            System.out.println("User data deletion response body: " + deleteUserResponse.body());

            if (deleteUserResponse.statusCode() != 200) {
                showErrorAlert("Delete Account Failed", "Failed to delete user data. Please try again.");
                System.err.println("Firebase user data deletion failed. Status: " + deleteUserResponse.statusCode());
                return false;
            }

            // Step 6: Delete Firebase Authentication user
            boolean authUserDeleted = deleteFirebaseAuthUserWithToken(idToken);
            if (authUserDeleted) {
                System.out.println("Firebase Auth user deleted successfully");
            } else {
                System.err.println("Warning: Failed to delete Firebase Auth user");
                // Continue as user data is already deleted
            }

            System.out.println("User account deleted successfully: " + email + " (userId: " + userId + ")");
            return true;

        } catch (Exception e) {
            showErrorAlert("Delete Account Error",
                    "An unexpected error occurred during account deletion. Please try again.");
            System.err.println("Error deleting user account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes user image from Firebase Storage
     * 
     * @param userId The user ID to identify the image file
     * @return boolean indicating success or failure
     */
    private boolean deleteImageFromStorage(String userId) {
        try {
            // The image is stored as Prem-Noy-Biye/{userId}.{extension}
            // We need to find the exact filename first, then delete it

            // Common image extensions to check
            String[] extensions = { ".jpg", ".jpeg", ".png", ".gif" };

            for (String extension : extensions) {
                String fileName = "Prem-Noy-Biye/" + userId + extension;
                String deleteUrl = "https://firebasestorage.googleapis.com/v0/b/" + storageBucket +
                        "/o/" + java.net.URLEncoder.encode(fileName, "UTF-8");

                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create(deleteUrl))
                        .DELETE()
                        .build();

                HttpResponse<String> deleteResponse = httpClient.send(deleteRequest,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("Attempting to delete image: " + fileName);
                System.out.println("Delete response status: " + deleteResponse.statusCode());

                if (deleteResponse.statusCode() == 200) {
                    System.out.println("Successfully deleted image: " + fileName);
                    return true;
                } else if (deleteResponse.statusCode() == 404) {
                    System.out.println("Image not found: " + fileName + " (continuing to check other extensions)");
                    // Continue checking other extensions
                } else {
                    System.err.println("Error deleting image " + fileName + ". Status: " + deleteResponse.statusCode());
                    System.err.println("Response: " + deleteResponse.body());
                    // Continue checking other extensions
                }
            }
            System.out.println("No user image found to delete for userId: " + userId);
            return true; // Return true since no image to delete is not an error

        } catch (Exception e) {
            System.err.println("Error deleting image from Firebase Storage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
}
