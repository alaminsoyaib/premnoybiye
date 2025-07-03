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

    public String registerUser(String name, String email, String password) {
        try {
            if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                showErrorAlert("Registration Error", "Name, email, and password must not be empty");
                return null;
            }

            String userId = createFirebaseAuthUser(email, password);
            if (userId == null) {
                return null;
            }

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

            Map<String, Object> firestoreDoc = toFirestoreDocument(userData);
            String jsonData = objectMapper.writeValueAsString(firestoreDoc);

            String url = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println(
                        "User registered successfully in Firestore: " + name + " (" + email + ") userId: " + userId);
                return userId;
            } else {
                showErrorAlert("Registration Failed",
                        "Failed to create user profile. Please check your internet connection and try again.");
                System.err.println("Firestore user profile creation failed. Status: " + response.statusCode());
                System.err.println("Response: " + response.body());

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

    public boolean updateUserProfile(userInfo user, String currentEmail) {
        try {
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

    public boolean updateUserProfile(userInfo user) {
        if (user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
            return updateUserProfileByUserId(user, user.getUserId());
        }

        return updateUserProfile(user, user.getEmail());
    }

    private boolean updateUserProfileByUserId(userInfo user, String userId) {
        try {
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
            userData.put("userId", userId);

            Map<String, Object> firestoreDoc = toFirestoreDocument(userData);
            String jsonData = objectMapper.writeValueAsString(firestoreDoc);

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

    public userInfo loginUser(String email, String password) {
        try {
            String userId = authenticateFirebaseUser(email, password);
            if (userId == null) {
                return null;
            }

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
                    Map<String, Object> userData = fromFirestoreDocument(userDoc);

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
                    @SuppressWarnings("unchecked")
                    java.util.List<String> likedUsers = (java.util.List<String>) userData.getOrDefault("likedUsers",
                            new java.util.ArrayList<String>());
                    @SuppressWarnings("unchecked")
                    java.util.List<String> rejectedUsers = (java.util.List<String>) userData
                            .getOrDefault("rejectedUsers", new java.util.ArrayList<String>());
                    @SuppressWarnings("unchecked")
                    java.util.List<String> matchedUsers = (java.util.List<String>) userData
                            .getOrDefault("matchedUsers", new java.util.ArrayList<String>());

                    user.setLikedUsers(likedUsers);
                    user.setRejectedUsers(rejectedUsers);
                    user.setMatchedUsers(matchedUsers);

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

    public boolean isEmailAlreadyRegistered(String email) {
        try {
            String tempPassword = "tempPassword123!";
            String tempUserId = createFirebaseAuthUser(email, tempPassword);

            if (tempUserId != null) {
                deleteFirebaseAuthUser(tempUserId);
                System.out.println("Email not registered: " + email);
                return false;
            } else {
                System.out.println("Email already registered: " + email);
                return true;
            }

        } catch (Exception e) {
            System.err.println("Error checking email registration status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getUserIdFromEmail(String email) {
        try {
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

    public String getUserId(userInfo user) {
        if (user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
            return user.getUserId();
        }

        return getUserIdFromEmail(user.getEmail());
    }

    public String uploadImageToStorage(File imageFile, String userId) {
        try {
            if (imageFile == null || !imageFile.exists()) {
                System.err.println("Image file does not exist");
                return null;
            }

            String originalName = imageFile.getName();
            String extension = "";
            int lastDotIndex = originalName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalName.substring(lastDotIndex);
            }
            String newFileName = "Prem-Noy-Biye/" + userId + extension;
            String contentType = getContentType(originalName);

            System.out.println("Uploading image to Firebase Storage");
            System.out.println("Original filename: " + originalName);
            System.out.println("New filename: " + newFileName);
            System.out.println("Content type: " + contentType);

            byte[] fileBytes;
            try (FileInputStream fis = new FileInputStream(imageFile)) {
                fileBytes = fis.readAllBytes();
            }

            String uploadUrl = "https://firebasestorage.googleapis.com/v0/b/" + storageBucket +
                    "/o/" + java.net.URLEncoder.encode(newFileName, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseJson = objectMapper.readTree(response.body());
                String downloadToken = responseJson.get("downloadTokens").asText();

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

    public String uploadImageToStorageWithToken(File imageFile, String userId, String idToken) {
        try {
            if (imageFile == null || !imageFile.exists()) {
                System.err.println("Image file does not exist");
                return null;
            }

            if (idToken == null || idToken.trim().isEmpty()) {
                System.err.println("ID token is required for authenticated storage upload");
                return null;
            }

            String originalName = imageFile.getName();
            String extension = "";
            int lastDotIndex = originalName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalName.substring(lastDotIndex);
            }

            String newFileName = "Prem-Noy-Biye/" + userId + extension;
            String contentType = getContentType(originalName);

            System.out.println("Uploading image to Firebase Storage with authentication");
            System.out.println("Original filename: " + originalName);
            System.out.println("New filename: " + newFileName);
            System.out.println("Content type: " + contentType);

            byte[] fileBytes;
            try (FileInputStream fis = new FileInputStream(imageFile)) {
                fileBytes = fis.readAllBytes();
            }

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
                JsonNode responseJson = objectMapper.readTree(response.body());
                String downloadToken = responseJson.get("downloadTokens").asText();

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

    public String getIdTokenForUser(String email, String password) {
        return getFirebaseIdToken(email, password);
    }

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

    private boolean deleteFirebaseAuthUser(String userId) {
        try {
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

    private boolean deleteUserData(String email) {
        try {
            boolean userRemoved = removeUserFromDatabase(email);

            return userRemoved;
        } catch (Exception e) {
            System.err.println("Error deleting user data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean removeUserFromDatabase(String email) {
        try {
            String userId = getUserIdFromEmail(email);
            if (userId == null) {
                System.err.println("User not found for email: " + email);
                return false;
            }

            String url = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

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

    public boolean changePassword(userInfo user, String currentPassword, String newPassword) {
        try {
            String idToken = getIdTokenForUser(user.getEmail(), currentPassword);
            if (idToken == null) {
                System.err.println("Failed to authenticate user for password change");
                return false;
            }

            return changeFirebaseAuthPassword(idToken, newPassword);
        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(userInfo user, String password) {
        try {
            String idToken = getIdTokenForUser(user.getEmail(), password);
            if (idToken == null) {
                System.err.println("Failed to authenticate user for account deletion");
                return false;
            }

            boolean dataDeleted = deleteUserData(user.getEmail());

            boolean authDeleted = deleteFirebaseAuthUserWithToken(idToken);

            return dataDeleted && authDeleted;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<userInfo> getUsersByGender(String desiredGender) {
        java.util.List<userInfo> users = new java.util.ArrayList<>();

        try {
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
                            String gender = (String) userData.get("gender");
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

    public boolean addToLikedUsers(String userId, String targetUserId) {
        return updateUserInteractionList(userId, targetUserId, "likedUsers", "add");
    }

    public boolean addToRejectedUsers(String userId, String targetUserId) {
        return updateUserInteractionList(userId, targetUserId, "rejectedUsers", "add");
    }

    public boolean addToMatchedUsers(String userId, String targetUserId) {
        return updateUserInteractionList(userId, targetUserId, "matchedUsers", "add");
    }

    public boolean checkAndCreateMatch(String userId, String targetUserId) {
        try {
            String getTargetUserUrl = firestoreBaseUrl + "/users/" + targetUserId + "?key=" + apiKey;

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getTargetUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (getResponse.statusCode() == 200) {
                JsonNode targetUserDocument = objectMapper.readTree(getResponse.body());
                Map<String, Object> targetUserData = fromFirestoreDocument(targetUserDocument);

                @SuppressWarnings("unchecked")
                java.util.List<String> targetUserLikedUsers = (java.util.List<String>) targetUserData
                        .getOrDefault("likedUsers", new java.util.ArrayList<String>());

                if (targetUserLikedUsers.contains(userId)) {
                    boolean match1 = addToMatchedUsers(userId, targetUserId);
                    boolean match2 = addToMatchedUsers(targetUserId, userId);

                    if (match1 && match2) {
                        System.out.println("Match created between users: " + userId + " and " + targetUserId);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking for match: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public userInfo getUserById(String userId) {
        try {
            String getUserUrl = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (getResponse.statusCode() == 200) {
                JsonNode userDocument = objectMapper.readTree(getResponse.body());
                Map<String, Object> userData = fromFirestoreDocument(userDocument);

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

                @SuppressWarnings("unchecked")
                java.util.List<String> likedUsers = (java.util.List<String>) userData.getOrDefault("likedUsers",
                        new java.util.ArrayList<>());
                user.setLikedUsers(likedUsers);

                @SuppressWarnings("unchecked")
                java.util.List<String> rejectedUsers = (java.util.List<String>) userData.getOrDefault("rejectedUsers",
                        new java.util.ArrayList<>());
                user.setRejectedUsers(rejectedUsers);

                @SuppressWarnings("unchecked")
                java.util.List<String> matchedUsers = (java.util.List<String>) userData.getOrDefault("matchedUsers",
                        new java.util.ArrayList<>());
                user.setMatchedUsers(matchedUsers);

                return user;
            } else {
                System.err.println("Failed to get user by ID. Status: " + getResponse.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean updateUserInteractionList(String userId, String targetUserId, String listType, String action) {
        try {
            String getUserUrl = firestoreBaseUrl + "/users/" + userId + "?key=" + apiKey;

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getUserUrl))
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            if (getResponse.statusCode() == 200) {
                JsonNode userDocument = objectMapper.readTree(getResponse.body());
                Map<String, Object> userData = fromFirestoreDocument(userDocument);

                @SuppressWarnings("unchecked")
                java.util.List<String> currentList = (java.util.List<String>) userData.getOrDefault(listType,
                        new java.util.ArrayList<String>());

                if (!currentList.contains(targetUserId)) {
                    currentList.add(targetUserId);

                    userData.put(listType, currentList);

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
                    return true;
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

    public boolean sendMessage(com.bytebender.premnoybiye.DBConnection.Message message) {
        try {
            String messageId = java.util.UUID.randomUUID().toString();
            message.setMessageId(messageId);

            String conversationId = createConversationId(message.getSenderId(), message.getReceiverId());

            Map<String, Object> messageData = new HashMap<>();
            messageData.put("messageId", message.getMessageId());
            messageData.put("senderId", message.getSenderId());
            messageData.put("receiverId", message.getReceiverId());
            messageData.put("content", message.getContent());
            messageData.put("timestamp", message.getTimestamp());
            messageData.put("isRead", message.isRead());

            Map<String, Object> firestoreDoc = toFirestoreDocument(messageData);
            String jsonData = objectMapper.writeValueAsString(firestoreDoc);

            String url = firestoreBaseUrl + "/conversations/" + conversationId + "/messages/" + messageId + "?key="
                    + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Message sent successfully");
                return true;
            } else {
                System.err.println("Failed to send message. Status: " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<com.bytebender.premnoybiye.DBConnection.Message> getConversation(String userId1,
            String userId2) {
        java.util.List<com.bytebender.premnoybiye.DBConnection.Message> messages = new java.util.ArrayList<>();

        try {
            String conversationId = createConversationId(userId1, userId2);
            String url = firestoreBaseUrl + "/conversations/" + conversationId + "/messages?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode documentsNode = objectMapper.readTree(response.body());

                if (documentsNode.has("documents")) {
                    for (JsonNode messageDoc : documentsNode.get("documents")) {
                        Map<String, Object> messageData = fromFirestoreDocument(messageDoc);

                        com.bytebender.premnoybiye.DBConnection.Message message = new com.bytebender.premnoybiye.DBConnection.Message(
                                (String) messageData.getOrDefault("messageId", ""),
                                (String) messageData.getOrDefault("senderId", ""),
                                (String) messageData.getOrDefault("receiverId", ""),
                                (String) messageData.getOrDefault("content", ""),
                                (String) messageData.getOrDefault("timestamp", ""),
                                Boolean.parseBoolean(messageData.getOrDefault("isRead", "false").toString()));

                        messages.add(message);
                    }
                }

                messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

            } else if (response.statusCode() == 404) {
                System.out.println("No conversation found between users");
            } else {
                System.err.println("Failed to get conversation. Status: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("Error getting conversation: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }

    private String createConversationId(String userId1, String userId2) {
        java.util.List<String> userIds = java.util.Arrays.asList(userId1, userId2);
        java.util.Collections.sort(userIds);
        return userIds.get(0) + "_" + userIds.get(1);
    }
}
