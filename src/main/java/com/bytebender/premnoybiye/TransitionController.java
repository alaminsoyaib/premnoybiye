package com.bytebender.premnoybiye;

import java.io.IOException;
import javafx.fxml.FXML;

public class TransitionController {

    @FXML
    private void switchToTransition() throws IOException {
        App.setRoot("transition");
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }

    @FXML
    private void switchToSignUp() throws IOException {
        App.setRoot("signup");
    }

}
