package com.bytebender.premnoybiye;

import java.io.IOException;

import com.bytebender.premnoybiye.DBConnection.userInfo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class test {
    @FXML
    private Label demoLabel;

    public void initialize() {
        // Access the user info
        userInfo user = StepperController.currentUser;

        if (user != null) {
            demoLabel.setText(
                    "Gender: " + user.getGender() + "\nReligion: " + user.getReligion() + "\nCity: " + user.getCity());
        }
    }
}
