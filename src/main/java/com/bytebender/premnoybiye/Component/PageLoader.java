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

    public static void loadPageWithAnimation(VBox container, String fxml, String loadingText) {
        showLoadingScreen(container, loadingText != null ? loadingText : "Loading...");
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
                    switchContent(container, (Parent) content);
                });
            }
        });
    }

    public static void loadPageWithCustomLogic(VBox container, String fxml, String loadingText, Runnable preLoadLogic) {
        showLoadingScreen(container, loadingText != null ? loadingText : "Loading...");

        Task<Parent> loadTask = new Task<Parent>() {
            @Override
            protected Parent call() throws Exception {
                if (preLoadLogic != null) {
                    Platform.runLater(preLoadLogic);
                    Thread.sleep(100);
                }
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

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private static void showLoadingScreen(VBox container, String loadingText) {
        try {
            FXMLLoader loadingLoader = new FXMLLoader(
                    PageLoader.class.getResource("/com/bytebender/premnoybiye/loading.fxml"));
            Parent loadingContent = loadingLoader.load();

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
