package com.bytebender.premnoybiye;

import java.io.IOException;

import com.bytebender.premnoybiye.DBConnection.FirebaseRestClient;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AuthController {
    static userInfo CurrentUser;
    static String currentIdToken; // Store Firebase auth token
    static String currentUserId; // Store Firebase user ID

    @FXML
    private TextField emailTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField passwordTextField;

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }

    @FXML
    private void switchToSignUp() throws IOException {
        App.setRoot("signup");
    }

    @FXML
    private void switchToStepper() throws IOException {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        // Validate input
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.err.println("Please fill all fields");
            return;
        }

        // Authenticate with Firebase in background thread
        new Thread(() -> {
            try {
                // Try to sign up new user
                JsonObject authResp = FirebaseRestClient.signUp(email, password);

                // If user already exists, sign in instead
                if (authResp.has("error")) {
                    authResp = FirebaseRestClient.signIn(email, password);
                }

                if (authResp.has("idToken")) {
                    currentIdToken = authResp.get("idToken").getAsString();
                    currentUserId = authResp.get("localId").getAsString();

                    // Create user object
                    CurrentUser = new userInfo(name, email, password, "", "", "", "", "", "", "", "", "", "", "", "");

                    // Store user profile in Firebase Realtime Database
                    FirebaseRestClient.setData("users/" + currentUserId, CurrentUser, currentIdToken);

                    // Navigate to stepper on UI thread
                    javafx.application.Platform.runLater(() -> {
                        try {
                            App.setRoot("stepper");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    System.err.println("Authentication failed: " + authResp);
                }
            } catch (Exception e) {
                System.err.println("Firebase authentication error:");
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Upload user profile image to Firebase Storage
     */
    public static String uploadProfileImage(String imagePath) {
        if (currentIdToken == null || currentUserId == null) {
            System.err.println("User not authenticated");
            return null;
        }

        try {
            return FirebaseRestClient.uploadImageToStorage(imagePath, currentUserId, currentIdToken);
        } catch (Exception e) {
            System.err.println("Image upload failed:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update user profile in Firebase
     */
    public static void updateUserProfile(userInfo updatedUser) {
        if (currentIdToken == null || currentUserId == null) {
            System.err.println("User not authenticated");
            return;
        }

        new Thread(() -> {
            try {
                FirebaseRestClient.setData("users/" + currentUserId, updatedUser, currentIdToken);
                CurrentUser = updatedUser;
                System.out.println("Profile updated successfully");
            } catch (Exception e) {
                System.err.println("Profile update failed:");
                e.printStackTrace();
            }
        }).start();
    }

}
