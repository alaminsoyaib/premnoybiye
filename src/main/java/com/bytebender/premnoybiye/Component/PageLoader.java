package com.bytebender.premnoybiye.Component;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class PageLoader {

    private static final Duration FADE_DURATION = Duration.millis(300);

    /**
     * Loads a page with loading animation
     * 
     * @param container   The container to load the page into
     * @param fxml        The FXML file name to load
     * @param loadingText Custom loading text (optional)
     */
    public static void loadPageWithAnimation(VBox container, String fxml, String loadingText) {
        // Show loading screen first
        showLoadingScreen(container, loadingText != null ? loadingText : "Loading...");
        // Load the actual page in background
        CompletableFuture.supplyAsync(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(PageLoader.class.getResource("/" + fxml + ".fxml"));
                return loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAcceptAsync(content -> {
            if (content != null) {
                Platform.runLater(() -> {
                    // Fade out loading screen and fade in new content
                    switchContent(container, (Parent) content);
                });
            }
        });
    }

    /**
     * Load page with custom loading logic
     */
    public static void loadPageWithCustomLogic(VBox container, String fxml, String loadingText, Runnable preLoadLogic) {
        // Show loading screen first
        showLoadingScreen(container, loadingText != null ? loadingText : "Loading...");

        // Execute custom logic and load page
        Task<Parent> loadTask = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                // Execute pre-load logic if provided
                if (preLoadLogic != null) {
                    Platform.runLater(preLoadLogic);
                    Thread.sleep(100); // Small delay to allow UI updates
                }
                // Load the FXML
                FXMLLoader loader = new FXMLLoader(PageLoader.class.getResource("/" + fxml + ".fxml"));
                return loader.load();
            }
        };

        loadTask.setOnSucceeded(e -> {
            Parent content = loadTask.getValue();
            if (content != null) {
                switchContent(container, content);
            }
        });

        loadTask.setOnFailed(e -> {
            System.err.println("Failed to load page: " + fxml);
            e.getSource().getException().printStackTrace();
        });

        // Run task in background thread
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private static void showLoadingScreen(VBox container, String loadingText) {
        try {
            FXMLLoader loadingLoader = new FXMLLoader(
                    PageLoader.class.getResource("/com/bytebender/premnoybiye/loading.fxml"));
            Parent loadingContent = loadingLoader.load();

            // Set custom loading text if provided
            if (loadingText != null && !loadingText.isEmpty()) {
                Object controller = loadingLoader.getController();
                if (controller instanceof com.bytebender.premnoybiye.LoadingController) {
                    ((com.bytebender.premnoybiye.LoadingController) controller).setLoadingText(loadingText);
                }
            }

            container.getChildren().clear();
            container.getChildren().add(loadingContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void switchContent(VBox container, Parent newContent) {
        // Create fade transition for smooth content switching
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);

        fadeOut.setOnFinished(e -> {
            container.getChildren().clear();
            container.getChildren().add(newContent);

            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, container);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }
}
