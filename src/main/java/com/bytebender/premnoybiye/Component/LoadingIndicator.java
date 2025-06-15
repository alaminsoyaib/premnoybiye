package com.bytebender.premnoybiye.Component;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

/**
 * Reusable loading indicator component for async operations
 */
public class LoadingIndicator {
    
    /**
     * Create a simple loading indicator with text
     */
    public static VBox createSimpleLoader(String text) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1); // Indeterminate progress
        progressIndicator.setPrefSize(50, 50);
        
        Label loadingLabel = new Label(text);
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        loadingBox.setMaxWidth(Double.MAX_VALUE);
        loadingBox.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(loadingBox, Priority.ALWAYS);
        
        return loadingBox;
    }
    
    /**
     * Create a loading indicator with progress bar
     */
    public static VBox createProgressLoader(String text) {
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        
        Label loadingLabel = new Label(text);
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        
        Label progressLabel = new Label("0%");
        progressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999;");
        
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(loadingLabel, progressBar, progressLabel);
        loadingBox.setMaxWidth(Double.MAX_VALUE);
        loadingBox.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(loadingBox, Priority.ALWAYS);
        
        return loadingBox;
    }
    
    /**
     * Update progress bar value
     */
    public static void updateProgress(VBox loadingBox, int percentage) {
        Platform.runLater(() -> {
            ProgressBar progressBar = (ProgressBar) loadingBox.getChildren().get(1);
            Label progressLabel = (Label) loadingBox.getChildren().get(2);
            
            progressBar.setProgress(percentage / 100.0);
            progressLabel.setText(percentage + "%");
        });
    }
    
    /**
     * Show loading state in a container
     */
    public static void showLoading(VBox container, String text) {
        Platform.runLater(() -> {
            container.getChildren().clear();
            container.getChildren().add(createSimpleLoader(text));
        });
    }
    
    /**
     * Show loading with progress in a container
     */
    public static VBox showProgressLoading(VBox container, String text) {
        VBox loadingBox = createProgressLoader(text);
        Platform.runLater(() -> {
            container.getChildren().clear();
            container.getChildren().add(loadingBox);
        });
        return loadingBox;
    }
}
