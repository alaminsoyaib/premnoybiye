package com.bytebender.premnoybiye;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class LoadingController {

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label loadingText;

    public void setLoadingText(String text) {
        if (loadingText != null) {
            loadingText.setText(text);
        }
    }

    public void initialize() {
        // Start the loading animation
        if (loadingIndicator != null) {
            loadingIndicator.setProgress(-1); // Indeterminate progress
        }
    }
}
