package com.bytebender.premnoybiye.Component;

import javafx.scene.image.Image;
import javafx.application.Platform;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.function.Consumer;

public class ImageCache {
    private static final String CACHE_DIR = System.getProperty("user.home") + File.separator + ".premnoybiye"
            + File.separator + "cache";
    private static final ConcurrentHashMap<String, Image> memoryCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CompletableFuture<Image>> loadingImages = new ConcurrentHashMap<>();

    static {
        try {
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                System.out.println("Image cache directory created: " + CACHE_DIR);
            }
        } catch (Exception e) {
            System.err.println("Failed to create cache directory: " + e.getMessage());
        }
    }

    public static void getImageAsync(String imageUrl, Consumer<Image> onSuccess, Consumer<String> onError) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Platform.runLater(() -> onError.accept("Invalid image URL"));
            return;
        }

        Image cachedImage = memoryCache.get(imageUrl);
        if (cachedImage != null) {
            Platform.runLater(() -> onSuccess.accept(cachedImage));
            return;
        }

        CompletableFuture<Image> existingLoad = loadingImages.get(imageUrl);
        if (existingLoad != null) {
            existingLoad.thenAccept(image -> Platform.runLater(() -> onSuccess.accept(image)))
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> onError.accept(throwable.getMessage()));
                        return null;
                    });
            return;
        }

        CompletableFuture<Image> loadingFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return loadImageWithCache(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load image: " + e.getMessage(), e);
            }
        });

        loadingImages.put(imageUrl, loadingFuture);

        loadingFuture.thenAccept(image -> {
            loadingImages.remove(imageUrl);
            if (image != null) {
                memoryCache.put(imageUrl, image);
                Platform.runLater(() -> onSuccess.accept(image));
            } else {
                Platform.runLater(() -> onError.accept("Failed to load image"));
            }
        }).exceptionally(throwable -> {
            loadingImages.remove(imageUrl);
            Platform.runLater(() -> onError.accept(throwable.getMessage()));
            return null;
        });
    }

    public static Image getImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        Image cachedImage = memoryCache.get(imageUrl);
        if (cachedImage != null) {
            return cachedImage;
        }

        try {
            Image image = loadImageWithCache(imageUrl);
            if (image != null) {
                memoryCache.put(imageUrl, image);
            }
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load image synchronously: " + e.getMessage());
            return null;
        }
    }

    private static Image loadImageWithCache(String imageUrl) throws Exception {
        String fileName = generateCacheFileName(imageUrl);
        File cacheFile = new File(CACHE_DIR, fileName);

        if (cacheFile.exists()) {
            try {
                Image image = new Image(cacheFile.toURI().toString());
                if (!image.isError()) {
                    return image;
                }
            } catch (Exception e) {
                System.err.println("Failed to load cached image, will re-download: " + e.getMessage());
                cacheFile.delete();
            }
        }

        return downloadAndCacheImage(imageUrl, cacheFile);
    }

    private static Image downloadAndCacheImage(String imageUrl, File cacheFile) throws Exception {
        System.out.println("Downloading image: " + imageUrl);

        try (InputStream inputStream = URI.create(imageUrl).toURL().openConnection().getInputStream();
                FileOutputStream outputStream = new FileOutputStream(cacheFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        Image image = new Image(cacheFile.toURI().toString());
        if (image.isError()) {
            cacheFile.delete(); // Delete invalid cached file
            throw new Exception("Downloaded image is invalid");
        }

        System.out.println("Image cached successfully: " + cacheFile.getName());
        return image;
    }

    private static String generateCacheFileName(String imageUrl) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(imageUrl.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            String extension = ".jpg";
            int lastDot = imageUrl.lastIndexOf('.');
            if (lastDot > 0 && lastDot < imageUrl.length() - 1) {
                String urlExtension = imageUrl.substring(lastDot);
                if (urlExtension.matches("\\.(jpg|jpeg|png|gif|bmp)")) {
                    extension = urlExtension;
                }
            }

            return sb.toString() + extension;
        } catch (Exception e) {
            return String.valueOf(imageUrl.hashCode()) + ".jpg";
        }
    }

    public static void clearMemoryCache() {
        memoryCache.clear();
        System.out.println("Memory cache cleared");
    }

    public static void clearFileCache() {
        try {
            File cacheDir = new File(CACHE_DIR);
            if (cacheDir.exists()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
            System.out.println("File cache cleared");
        } catch (Exception e) {
            System.err.println("Failed to clear file cache: " + e.getMessage());
        }
    }

    public static String getCacheStats() {
        File cacheDir = new File(CACHE_DIR);
        int fileCount = 0;
        long totalSize = 0;

        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                fileCount = files.length;
                for (File file : files) {
                    totalSize += file.length();
                }
            }
        }

        return String.format("Memory cache: %d items, File cache: %d files (%.2f MB)",
                memoryCache.size(), fileCount, totalSize / (1024.0 * 1024.0));
    }
}
