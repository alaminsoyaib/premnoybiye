package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class editProfileController {
    int flag = 0;
    private Component component = new Component();

    @FXML
    private ImageView sidebarCollapse;
    @FXML
    private HBox demosidebarProfile;
    @FXML
    private Label discoverLabel;
    @FXML
    private Label matchesLabel;
    @FXML
    private Label profileLabel;
    @FXML
    private Label msgLabel;
    @FXML
    private Label logoutLabel;

    @FXML
    private VBox Sidebar;

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField emailTextField;

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
    private ImageView demoProfileSubImg;

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
        try {
            if (user != null) {
                String name = user.getName();
                if (name != "") {
                    demoLabel.setText(name);
                    demosub.setText(name);
                    nameTextField.setText(name);
                }
                if (user.getEmail() != "") {
                    emailLabel.setText(user.getEmail());
                    emailTextField.setText(user.getEmail());
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
                    // Image image = new Image(user.getImage());

                    // // demoProfileImg.setImage(image);
                    // demoProfileSubImg.setImage(image);

                    // // demoProfileImg.setFitWidth(84);
                    // // demoProfileImg.setFitHeight(84);
                    // // demoProfileImg.setPreserveRatio(false);

                    // demoProfileSubImg.setFitWidth(40);
                    // demoProfileSubImg.setFitHeight(40);
                    // demoProfileSubImg.setPreserveRatio(false);

                    // // javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(84,
                    // 84);
                    // // clip.setArcWidth(20);
                    // // clip.setArcHeight(20);
                    // // demoProfileImg.setClip(clip);

                    // javafx.scene.shape.Rectangle clip2 = new javafx.scene.shape.Rectangle(40,
                    // 40);
                    // clip2.setArcWidth(20);
                    // clip2.setArcHeight(20);
                    // demoProfileSubImg.setClip(clip2);

                    component.setImage(user.getImage(), demoProfileSubImg, 40, 40, false, 20);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void sideCollapse(MouseEvent event) {
        // Calling toggling method of the sidebar Component
        flag = component.toggleSidebarState(sidebarCollapse, demosidebarProfile, discoverLabel, matchesLabel,
                profileLabel, msgLabel, logoutLabel, Sidebar, flag);
    }
}
