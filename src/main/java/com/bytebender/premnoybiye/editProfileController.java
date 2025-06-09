package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.DBConnection.userInfo;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class editProfileController {
    @FXML
    private Label demoLabel;
    @FXML
    private Label demosub;
    @FXML
    private Label emailLabel;

    @FXML
    private DatePicker dobComboBox;
    @FXML
    private ComboBox<?> genderComboBox;
    @FXML
    private ComboBox<?> religionComboBox;
    @FXML
    private ComboBox<?> cityComboBox;

    @FXML
    private ImageView demoProfileImg;

    @FXML
    private ComboBox<?> highestEduComboBox;
    @FXML
    private ComboBox<?> professionComboBox;
    @FXML
    private ComboBox<?> monthlyIncomeComboBox;

    @FXML
    private TextField aboutYouTextField;
    @FXML
    private ComboBox<?> prefPartnerAgeComboBox;
    @FXML
    private ComboBox<?> prefLocationComboBox;
    @FXML
    private ComboBox<?> prefProfessionComboBox;

    public void initialize() {
        // Access the user info
        userInfo user = StepperController.currentUser;
        if (user != null) {
            String name = user.getName();
            if (name != "") {
                demoLabel.setText(name);
                demosub.setText(name);
            }
            if (user.getEmail() != "") {
                emailLabel.setText(user.getEmail());
            }
            if (user.getReligion() != "") {
                religionComboBox.setPromptText(user.getReligion());
            }
            if (user.getDob() != "") {
                dobComboBox.setPromptText(user.getDob());
            }
            if (user.getGender() != "") {
                genderComboBox.setPromptText(user.getGender());
            }
            if (user.getReligion() != "") {
                religionComboBox.setPromptText(user.getReligion());
            }
            if (user.getCity() != "") {
                cityComboBox.setPromptText(user.getCity());
            }
            if (user.getImage() != "") {

                Image image = new Image(user.getImage());

                demoProfileImg.setImage(image);

                // Set size to 80x80
                demoProfileImg.setFitWidth(84);
                demoProfileImg.setFitHeight(84);
                demoProfileImg.setPreserveRatio(false);
            }
            if (user.getEducation() != "") {
                highestEduComboBox.setPromptText(user.getEducation());
            }
            if (user.getProfession() != "") {
                professionComboBox.setPromptText(user.getProfession());
            }
            if (user.getIncome() != "") {
                monthlyIncomeComboBox.setPromptText(user.getIncome());
            }
            if (user.getBio() != "") {
                aboutYouTextField.setText(user.getBio());
            }
            if (user.getPrefAge() != "") {
                prefPartnerAgeComboBox.setPromptText(user.getPrefAge());
            }
            if (user.getPrefLocation() != "") {
                prefLocationComboBox.setPromptText(user.getPrefLocation());
            }
            if (user.getPrefProfession() != "") {
                prefProfessionComboBox.setPromptText(user.getPrefProfession());
            }

            // demoLabel.setText("Date of Birth: " + user.getDob() + "\nGender: " +
            // user.getGender() setPromptText​
            // + "\nReligion: " + user.getReligion() + "\nCity: " + user.getCity()
            // + "\nEducation: " + user.getEducation() + "\nProfession: " +
            // user.getProfession()
            // + "\nMonthly Income: " + user.getIncome() + "\nAbout You: " + user.getBio()
            // + "\nPreferred Age: " + user.getPrefAge() + "\nPreferred Location: "
            // + user.getPrefLocation() + "\nPreferred Profession: " +
            // user.getPrefProfession());
        }
    }
}
