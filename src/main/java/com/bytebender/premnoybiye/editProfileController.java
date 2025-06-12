package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class editProfileController {
    private Component component = new Component();
    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    @FXML
    private ImageView editProfileImg;
    @FXML
    private Button changePicture;

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

    @FXML
    private void changePictureClicked() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg",
                            "*.jpeg", "*.gif"));

            java.io.File selectedFile = fileChooser.showOpenDialog(editProfileImg.getScene().getWindow());

            if (selectedFile != null) {
                Image image = new Image(selectedFile.toURI().toString());
                editProfileImg.setImage(image);

                // // Set size to 80x80
                editProfileImg.setFitWidth(80);
                editProfileImg.setFitHeight(80);
                editProfileImg.setPreserveRatio(false);

                // Set rounded corners
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(80, 80);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                editProfileImg.setClip(clip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveClicked() {
        try {
            // Get current user from AuthController
            userInfo user = AuthController.CurrentUser;

            if (user != null) {
                user.setImage(editProfileImg.getImage().getUrl());
                if (editNameTextField.getText() != null && !editNameTextField.getText().trim().isEmpty()) {
                    user.setName(editNameTextField.getText().trim());
                }
                if (editEmailTextField.getText() != null && !editEmailTextField.getText().trim().isEmpty()) {
                    user.setEmail(editEmailTextField.getText().trim());
                }
                if (editDobComboBox.getValue() != null) {
                    user.setDob(editDobComboBox.getValue().toString());
                }
                if (editGenderComboBox.getValue() != null) {
                    user.setGender(editGenderComboBox.getValue().toString());
                }
                if (editReligionComboBox.getValue() != null) {
                    user.setReligion(editReligionComboBox.getValue().toString());
                }
                if (editCityComboBox.getValue() != null) {
                    user.setCity(editCityComboBox.getValue().toString());
                }
                if (editHighestEduComboBox.getValue() != null) {
                    user.setEducation(editHighestEduComboBox.getValue().toString());
                }
                if (editProfessionComboBox.getValue() != null) {
                    user.setProfession(editProfessionComboBox.getValue().toString());
                }
                if (editMonthlyIncomeComboBox.getValue() != null) {
                    user.setIncome(editMonthlyIncomeComboBox.getValue().toString());
                }
                if (editAboutYouTextField.getText() != null && !editAboutYouTextField.getText().trim().isEmpty()) {
                    user.setBio(editAboutYouTextField.getText().trim());
                }
                if (editPrefPartnerAgeComboBox.getValue() != null) {
                    user.setPrefAge(editPrefPartnerAgeComboBox.getValue().toString());
                }
                if (editPrefLocationComboBox.getValue() != null) {
                    user.setPrefLocation(editPrefLocationComboBox.getValue().toString());
                }
                if (editPrefProfessionComboBox.getValue() != null) {
                    user.setPrefProfession(editPrefProfessionComboBox.getValue().toString());
                }

                // Update user profile in Firebase with all collected data (same pattern as
                // StepperController)
                boolean updateSuccess = firebaseConnection.updateUserProfile(user);

                if (updateSuccess) {
                    System.out.println("User profile updated successfully in Firebase!");
                    // You can add a success alert here if needed
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Profile Updated");
                        alert.setHeaderText(null);
                        alert.setContentText("Your profile has been updated successfully!");
                        alert.showAndWait();
                        // reloading the sidebar to reflect changes
                        try {
                            App.setRoot("sidebar");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    System.err.println("Failed to update user profile in Firebase");
                    // Firebase connection already shows error alerts for specific failures
                }
            } else {
                System.err.println("No current user found to update");
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Update Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("No user session found. Please log in again.");
                    alert.showAndWait();
                });
            }
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Update Error");
                alert.setHeaderText(null);
                alert.setContentText("An unexpected error occurred while updating your profile. Please try again.");
                alert.showAndWait();
            });
        }
    }
}
