package com.bytebender.premnoybiye.DBConnection;

import com.google.gson.JsonObject;

public class FirebaseTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Firebase REST API connection...");

            // Test sign up
            JsonObject authResp = FirebaseRestClient.signUp("test@test.com", "test123");
            System.out.println("Auth response: " + authResp);

            if (authResp.has("error")) {
                System.out.println("SignUp failed, trying signIn...");
                authResp = FirebaseRestClient.signIn("test@test.com", "test123");
                System.out.println("SignIn response: " + authResp);
            }

            if (authResp.has("idToken")) {
                String idToken = authResp.get("idToken").getAsString();

                // Test database write
                JsonObject testData = new JsonObject();
                testData.addProperty("message", "Test from Java");
                testData.addProperty("timestamp", System.currentTimeMillis());

                JsonObject writeResp = FirebaseRestClient.setData("test", testData, idToken);
                System.out.println("Write response: " + writeResp);

                // Test database read
                JsonObject readResp = FirebaseRestClient.getData("test", idToken);
                System.out.println("Read response: " + readResp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
