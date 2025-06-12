package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class editProfileController {
    private Component component = new Component();
    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    private java.io.File selectedImageFile = null; // Store the selected image file for upload
    @FXML
    private ImageView editProfileImg;
    @FXML
    private Button changePicture;
    @FXML
    private Button changePasswordButton;
    @FXML
    private Button deleteAccountButton;

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

    @FXML
    private PasswordField passwordTextField;

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
                // Store the selected file for later upload
                this.selectedImageFile = selectedFile;

                // Display the image in the UI
                Image image = new Image(selectedFile.toURI().toString());
                editProfileImg.setImage(image);

                // Set size to 80x80
                editProfileImg.setFitWidth(80);
                editProfileImg.setFitHeight(80);
                editProfileImg.setPreserveRatio(false);

                // Set rounded corners
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(80, 80);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                editProfileImg.setClip(clip);

                System.out.println("Image selected for upload: " + selectedFile.getName());
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

            if (user != null) { // Handle image upload if a new image was selected
                if (selectedImageFile != null) {
                    System.out.println("Uploading image to Firebase Storage...");

                    // Get the actual userId from the user object (which now includes userId)
                    String userId = firebaseConnection.getUserId(user);

                    if (userId != null) {
                        String imageUrl = firebaseConnection.uploadImageToStorage(selectedImageFile, userId);

                        if (imageUrl != null) {
                            user.setImage(imageUrl);
                            System.out.println("Image uploaded successfully. URL: " + imageUrl);
                            // Clear the selected file after successful upload
                            selectedImageFile = null;
                        } else {
                            System.err.println("Failed to upload image to Firebase Storage");
                            javafx.application.Platform.runLater(() -> {
                                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                        javafx.scene.control.Alert.AlertType.WARNING);
                                alert.setTitle("Image Upload Failed");
                                alert.setHeaderText(null);
                                alert.setContentText(
                                        "Failed to upload the image. Your profile will be saved without the new image.");
                                alert.showAndWait();
                            });
                        }
                    } else {
                        System.err.println("Could not find userId for user: " + user.getEmail());
                        javafx.application.Platform.runLater(() -> {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.WARNING);
                            alert.setTitle("Image Upload Failed");
                            alert.setHeaderText(null);
                            alert.setContentText(
                                    "Could not identify user for image upload. Your profile will be saved without the new image.");
                            alert.showAndWait();
                        });
                    }
                }

                // Update other user data
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

    /**
     * Changes user password with proper verification
     * This method can be called from UI components like password change dialogs
     *
     * @param currentPassword The user's current password
     * @param newPassword     The new password to set
     * @return boolean indicating success or failure
     */
    public boolean changeUserPassword(String currentPassword, String newPassword) {
        try {
            userInfo user = AuthController.CurrentUser;

            if (user != null) {
                boolean success = firebaseConnection.changePassword(user, currentPassword, newPassword);

                if (success) {
                    System.out.println("Password changed successfully for user: " + user.getEmail());

                    // Show success message
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Password Changed");
                        alert.setHeaderText(null);
                        alert.setContentText("Your password has been changed successfully!");
                        alert.showAndWait();
                    });

                    return true;
                } else {
                    System.err.println("Failed to change password for user: " + user.getEmail());
                    return false;
                }
            } else {
                System.err.println("No current user found for password change");
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Password Change Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("No user session found. Please log in again.");
                    alert.showAndWait();
                });
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error during password change: " + e.getMessage());
            e.printStackTrace();

            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Password Change Error");
                alert.setHeaderText(null);
                alert.setContentText("An unexpected error occurred while changing password. Please try again.");
                alert.showAndWait();
            });
            return false;
        }
    }

    @FXML
    private void changePasswordClicked() {
        try {
            // Create a custom dialog for password change
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle("Change Password");
            dialog.setHeaderText("Enter your current password and new password");

            // Set the button types
            javafx.scene.control.ButtonType changeButtonType = new javafx.scene.control.ButtonType("Change Password",
                    javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, javafx.scene.control.ButtonType.CANCEL);

            // Create the password fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            PasswordField currentPassword = new PasswordField();
            currentPassword.setPromptText("Current Password");
            currentPassword.setPrefWidth(200);

            PasswordField newPassword = new PasswordField();
            newPassword.setPromptText("New Password");
            newPassword.setPrefWidth(200);

            PasswordField confirmPassword = new PasswordField();
            confirmPassword.setPromptText("Confirm New Password");
            confirmPassword.setPrefWidth(200);

            grid.add(new Label("Current Password:"), 0, 0);
            grid.add(currentPassword, 1, 0);
            grid.add(new Label("New Password:"), 0, 1);
            grid.add(newPassword, 1, 1);
            grid.add(new Label("Confirm Password:"), 0, 2);
            grid.add(confirmPassword, 1, 2);

            // Enable/Disable change button depending on whether passwords are entered
            javafx.scene.Node changeButton = dialog.getDialogPane().lookupButton(changeButtonType);
            changeButton.setDisable(true);

            // Do some validation (using the Java 8 lambda syntax).
            currentPassword.textProperty().addListener((observable, oldValue, newValue) -> {
                changeButton.setDisable(newValue.trim().isEmpty() || newPassword.getText().trim().isEmpty()
                        || confirmPassword.getText().trim().isEmpty());
            });

            newPassword.textProperty().addListener((observable, oldValue, newValue) -> {
                changeButton.setDisable(newValue.trim().isEmpty() || currentPassword.getText().trim().isEmpty()
                        || confirmPassword.getText().trim().isEmpty());
            });

            confirmPassword.textProperty().addListener((observable, oldValue, newValue) -> {
                changeButton.setDisable(newValue.trim().isEmpty() || currentPassword.getText().trim().isEmpty()
                        || newPassword.getText().trim().isEmpty());
            });

            dialog.getDialogPane().setContent(grid);

            // Request focus on the current password field by default.
            javafx.application.Platform.runLater(() -> currentPassword.requestFocus());

            // Convert the result to password array when the change button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == changeButtonType) {
                    return new String[] { currentPassword.getText(), newPassword.getText(), confirmPassword.getText() };
                }
                return null;
            });

            java.util.Optional<String[]> result = dialog.showAndWait();

            result.ifPresent(passwords -> {
                String currentPass = passwords[0];
                String newPass = passwords[1];
                String confirmPass = passwords[2];

                // Validate that new passwords match
                if (!newPass.equals(confirmPass)) {
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Password Mismatch");
                        alert.setHeaderText(null);
                        alert.setContentText("New password and confirm password do not match. Please try again.");
                        alert.showAndWait();
                    });
                    return;
                }

                // Validate password strength (optional)
                if (newPass.length() < 6) {
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.WARNING);
                        alert.setTitle("Weak Password");
                        alert.setHeaderText(null);
                        alert.setContentText("New password should be at least 6 characters long.");
                        alert.showAndWait();
                    });
                    return;
                }

                // Call the password change method
                boolean success = changeUserPassword(currentPass, newPass);

                if (success) {
                    // Clear the password field in the UI for security
                    passwordTextField.clear();
                }
            });

        } catch (Exception e) {
            System.err.println("Error opening password change dialog: " + e.getMessage());
            e.printStackTrace();

            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to open password change dialog. Please try again.");
                alert.showAndWait();
            });
        }
    }

    @FXML
    private void deleteAccountClicked() {
        try {
            // Create confirmation dialog with password verification
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Delete Account");
            dialog.setHeaderText("WARNING: This action cannot be undone!");

            // Set the button types
            javafx.scene.control.ButtonType deleteButtonType = new javafx.scene.control.ButtonType("Delete Account",
                    javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, javafx.scene.control.ButtonType.CANCEL);

            // Create the confirmation grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            Label warningLabel = new Label("Deleting your account will permanently remove:\n" +
                    "• All your profile information\n" +
                    "• Your uploaded photos\n" +
                    "• Your match history\n" +
                    "• All associated data\n\n" +
                    "Please enter your password to confirm:");
            warningLabel.setStyle("-fx-text-fill: #d73027; -fx-font-size: 12px;");
            warningLabel.setWrapText(true);
            warningLabel.setPrefWidth(300);

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Enter your password");
            passwordField.setPrefWidth(200);

            grid.add(warningLabel, 0, 0, 2, 1);
            grid.add(new Label("Password:"), 0, 1);
            grid.add(passwordField, 1, 1);

            // Enable/Disable delete button depending on whether password is entered
            javafx.scene.Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
            deleteButton.setDisable(true);
            deleteButton.setStyle("-fx-background-color: #e45f5f; -fx-text-fill: white;");

            passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
                deleteButton.setDisable(newValue.trim().isEmpty());
            });

            dialog.getDialogPane().setContent(grid);

            // Request focus on the password field by default
            javafx.application.Platform.runLater(() -> passwordField.requestFocus());

            // Convert the result to password when the delete button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == deleteButtonType) {
                    return passwordField.getText();
                }
                return null;
            });

            java.util.Optional<String> result = dialog.showAndWait();

            result.ifPresent(password -> {
                // Show final confirmation dialog
                javafx.scene.control.Alert finalConfirm = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                finalConfirm.setTitle("Final Confirmation");
                finalConfirm.setHeaderText("Are you absolutely sure?");
                finalConfirm.setContentText(
                        "This is your last chance to cancel. Once deleted, your account cannot be recovered.");

                // Customize the buttons
                finalConfirm.getButtonTypes().setAll(
                        new javafx.scene.control.ButtonType("Yes, Delete My Account",
                                javafx.scene.control.ButtonBar.ButtonData.OK_DONE),
                        new javafx.scene.control.ButtonType("Cancel",
                                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE));

                java.util.Optional<javafx.scene.control.ButtonType> finalResult = finalConfirm.showAndWait();

                if (finalResult.isPresent()
                        && finalResult.get().getButtonData() == javafx.scene.control.ButtonBar.ButtonData.OK_DONE) {
                    // Proceed with account deletion
                    deleteUserAccount(password);
                }
            });

        } catch (Exception e) {
            System.err.println("Error opening delete account dialog: " + e.getMessage());
            e.printStackTrace();

            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to open delete account dialog. Please try again.");
                alert.showAndWait();
            });
        }
    }

    /**
     * Handles the actual deletion of user account
     * 
     * @param password The user's password for verification
     */
    private void deleteUserAccount(String password) {
        try {
            userInfo user = AuthController.CurrentUser;

            if (user != null) {
                // Call Firebase connection to delete the user
                boolean success = firebaseConnection.deleteUser(user, password);

                if (success) {
                    // Account deleted successfully
                    javafx.application.Platform.runLater(() -> {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Account Deleted");
                        alert.setHeaderText(null);
                        alert.setContentText(
                                "Your account has been permanently deleted. You will now be redirected to the login screen.");
                        alert.showAndWait();

                        // Clear current user session
                        AuthController.CurrentUser = null;

                        // Redirect to login screen
                        try {
                            App.setRoot("loginsignupchoice");
                        } catch (Exception e) {
                            System.err.println("Error redirecting to login: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } else {
                    // Deletion failed (Firebase connection already shows error alerts)
                    System.err.println("Failed to delete user account");
                }
            } else {
                System.err.println("No current user found for account deletion");
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Delete Account Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("No user session found. Please log in again.");
                    alert.showAndWait();
                });
            }
        } catch (Exception e) {
            System.err.println("Error during account deletion: " + e.getMessage());
            e.printStackTrace();

            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Account Deletion Error");
                alert.setHeaderText(null);
                alert.setContentText("An unexpected error occurred while deleting your account. Please try again.");
                alert.showAndWait();
            });
        }
    }

    // ...existing code...
}
