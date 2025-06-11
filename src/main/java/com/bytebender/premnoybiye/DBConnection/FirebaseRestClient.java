package com.bytebender.premnoybiye.DBConnection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Properties;

/**
 * Simple REST client for Firebase Authentication and Realtime Database.
 * Firestore is excluded. Requires Java 11+ for HttpClient.
 */
public class FirebaseRestClient {
    private static final String API_KEY;
    private static final String DATABASE_URL;
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    static {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("firebase.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not load firebase.properties", e);
        }
        API_KEY = props.getProperty("apiKey");
        String rawUrl = props.getProperty("databaseUrl");
        // Remove trailing slash if present
        DATABASE_URL = rawUrl.endsWith("/") ? rawUrl.substring(0, rawUrl.length() - 1) : rawUrl;
    }

    /**
     * Sign up user with email & password.
     * Returns JSON with idToken, refreshToken, localId.
     */
    public static JsonObject signUp(String email, String password) throws IOException, InterruptedException {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);
        payload.addProperty("returnSecureToken", true);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(resp.body(), JsonObject.class);
    }

    /**
     * Sign in user with email & password.
     * Returns JSON with idToken, refreshToken, localId.
     */
    public static JsonObject signIn(String email, String password) throws IOException, InterruptedException {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);
        payload.addProperty("returnSecureToken", true);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(resp.body(), JsonObject.class);
    }

    /**
     * Get data from Realtime Database at given path.
     * idToken obtained from signIn/signUp.
     */
    public static JsonObject getData(String path, String idToken) throws IOException, InterruptedException {
        String url = String.format("%s/%s.json?auth=%s", DATABASE_URL, path, idToken);
        System.out.println("[FirebaseRestClient] GET " + url);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[FirebaseRestClient] GET response: " + resp.statusCode() + " body=" + resp.body());
        return gson.fromJson(resp.body(), JsonObject.class);
    }

    /**
     * Write data (PUT) to Realtime Database at given path.
     * Data can be any Map or POJO.
     */
    public static JsonObject setData(String path, Object data, String idToken)
            throws IOException, InterruptedException {
        String url = String.format("%s/%s.json?auth=%s", DATABASE_URL, path, idToken);
        System.out.println("[FirebaseRestClient] PUT " + url + " payload=" + gson.toJson(data));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(data)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("[FirebaseRestClient] PUT response: " + resp.statusCode() + " body=" + resp.body());
        return gson.fromJson(resp.body(), JsonObject.class);
    }

    /**
     * Update (PATCH) data at path.
     */
    public static JsonObject updateData(String path, Map<String, Object> updates, String idToken)
            throws IOException, InterruptedException {
        String url = String.format("%s/%s.json?auth=%s", DATABASE_URL, path, idToken);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(updates)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(resp.body(), JsonObject.class);
    }

    /**
     * Delete node at path.
     */
    public static boolean deleteData(String path, String idToken) throws IOException, InterruptedException {
        String url = String.format("%s/%s.json?auth=%s", DATABASE_URL, path, idToken);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return resp.statusCode() == 200;
    }
}
