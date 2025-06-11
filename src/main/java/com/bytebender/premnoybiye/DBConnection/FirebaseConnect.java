package com.bytebender.premnoybiye.DBConnection;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FirebaseConnect {

    private static Storage storage;
    private static final String BUCKET_NAME = "your-firebase-bucket-name.appspot.com";
    private static final String SERVICE_ACCOUNT_PATH = "path/to/serviceAccountKey.json";

    public static void initialize() {
        try {
            InputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(BUCKET_NAME)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            // Close the first stream and open a new one for Storage initialization
            serviceAccount.close();
            InputStream storageServiceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH);
            storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(storageServiceAccount))
                    .build()
                    .getService();

            storageServiceAccount.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Upload image file to Firebase Storage
     * 
     * @param filePath Local file path
     * @param userId   User ID to associate with the image
     * @return Download URL of the uploaded image
     */
    public static String uploadProfileImage(String filePath, String userId) {
        try {
            // Create a unique filename
            String fileName = "profile_images/" + userId + "/" + UUID.randomUUID().toString();
            BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/jpeg")
                    .build();

            // Upload the file
            Blob blob = storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

            // Return the public download URL
            return blob.getMediaLink();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete an image from Firebase Storage
     * 
     * @param imageUrl URL of the image to delete
     * @return true if deletion was successful
     */
    public static boolean deleteProfileImage(String imageUrl) {
        try {
            // Extract the path from the URL
            String path = extractPathFromUrl(imageUrl);
            BlobId blobId = BlobId.of(BUCKET_NAME, path);

            return storage.delete(blobId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String extractPathFromUrl(String url) {
        // Extract the path from the Firebase Storage URL
        // Handle different URL formats that Firebase Storage can return
        if (url.contains("storage.googleapis.com")) {
            // Format: https://storage.googleapis.com/bucket-name/path/to/file
            String prefix = "https://storage.googleapis.com/" + BUCKET_NAME + "/";
            if (url.startsWith(prefix)) {
                return url.substring(prefix.length());
            }
        } else if (url.contains("firebasestorage.googleapis.com")) {
            // Format:
            // https://firebasestorage.googleapis.com/v0/b/bucket-name/o/path%2Fto%2Ffile
            int objectIndex = url.indexOf("/o/");
            if (objectIndex != -1) {
                String encodedPath = url.substring(objectIndex + 3);
                // Remove query parameters if any
                int queryIndex = encodedPath.indexOf("?");
                if (queryIndex != -1) {
                    encodedPath = encodedPath.substring(0, queryIndex);
                }
                // Decode URL encoding
                return encodedPath.replace("%2F", "/");
            }
        }

        // Fallback: return the URL as is (this might not work, but better than
        // crashing)
        return url;
    }
}
