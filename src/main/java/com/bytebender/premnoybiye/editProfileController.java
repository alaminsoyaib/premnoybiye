package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class editProfileController {
    private Component component = new Component();
    @FXML
    private ImageView editProfileImg;

    @FXML
    private TextField editAboutYouTextField;

    @FXML
    private Label editProfileName;
    @FXML
    private Label editEmailLabel;

    @FXML
    private TextField editNameTextField;
    @FXML
    private TextField editEmailTextField;

    @FXML
    private DatePicker editDobComboBox;
    @FXML
    private ComboBox<?> editGenderComboBox;
    @FXML
    private ComboBox<?> editReligionComboBox;
    @FXML
    private ComboBox<?> editCityComboBox;

    @FXML
    private ComboBox<?> editHighestEduComboBox;
    @FXML
    private ComboBox<?> editProfessionComboBox;
    @FXML
    private ComboBox<?> editMonthlyIncomeComboBox;

    @FXML
    private ComboBox<?> editPrefPartnerAgeComboBox;
    @FXML
    private ComboBox<?> editPrefLocationComboBox;
    @FXML
    private ComboBox<?> editPrefProfessionComboBox;

    public void initialize() {
        // userInfo user = StepperController.currentUser;
        userInfo user = AuthController.CurrentUser;
        try {
            if (user != null) {
                if (user.getName() != "") {
                    editProfileName.setText(user.getName());
                    editNameTextField.setText(user.getName());
                }
                if (user.getEmail() != "") {
                    editEmailLabel.setText(user.getEmail());
                    editEmailTextField.setText(user.getEmail());
                }
                if (user.getReligion() != "") {
                    editReligionComboBox.setPromptText(user.getReligion());
                }
                if (user.getDob() != "") {
                    editDobComboBox.setPromptText(user.getDob());
                }
                if (user.getGender() != "") {
                    editGenderComboBox.setPromptText(user.getGender());
                }
                if (user.getReligion() != "") {
                    editReligionComboBox.setPromptText(user.getReligion());
                }
                if (user.getCity() != "") {
                    editCityComboBox.setPromptText(user.getCity());
                }
                if (user.getImage() != "") {
                    component.setImage(user.getImage(), editProfileImg, 84, 84, false, 20);
                }
                if (user.getEducation() != "") {
                    editHighestEduComboBox.setPromptText(user.getEducation());
                }
                if (user.getProfession() != "") {
                    editProfessionComboBox.setPromptText(user.getProfession());
                }
                if (user.getIncome() != "") {
                    editMonthlyIncomeComboBox.setPromptText(user.getIncome());
                }
                if (user.getBio() != "") {
                    editAboutYouTextField.setText(user.getBio());
                }
                if (user.getPrefAge() != "") {
                    editPrefPartnerAgeComboBox.setPromptText(user.getPrefAge());
                }
                if (user.getPrefLocation() != "") {
                    editPrefLocationComboBox.setPromptText(user.getPrefLocation());
                }
                if (user.getPrefProfession() != "") {
                    editPrefProfessionComboBox.setPromptText(user.getPrefProfession());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
