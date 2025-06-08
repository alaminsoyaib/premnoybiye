package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.DBConnection.userInfo;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class test {
    @FXML
    private Label demoLabel;

    public void initialize() {
        // Access the user info
        userInfo user = StepperController.currentUser;
        if (user != null) {
            demoLabel.setText("Date of Birth: " + user.getDob() + "\nGender: " + user.getGender()
                    + "\nReligion: " + user.getReligion() + "\nCity: " + user.getCity()
                    + "\nEducation: " + user.getEducation() + "\nProfession: " + user.getProfession()
                    + "\nMonthly Income: " + user.getIncome() + "\nAbout You: " + user.getBio()
                    + "\nPreferred Age: " + user.getPrefAge() + "\nPreferred Location: "
                    + user.getPrefLocation() + "\nPreferred Profession: " + user.getPrefProfession());
        }
    }
}
