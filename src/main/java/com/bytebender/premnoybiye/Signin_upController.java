package com.bytebender.premnoybiye;

import java.io.IOException;

import com.bytebender.premnoybiye.DBConnection.userInfo;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Signin_upController {
    static userInfo NewUser;

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

    @FXML // temporary method to develop stepper
    private void switchToStepper() throws IOException {
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        NewUser = new userInfo(name, email, password, "", "", "", "", "", "", "", "", "", "", "");
        App.setRoot("Stepper");
    }

}
