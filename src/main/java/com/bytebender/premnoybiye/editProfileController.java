package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.Component.DialogUtils;
import com.bytebender.premnoybiye.Component.ImageProcessingService;
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
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class editProfileController {
    private Component component = new Component();
    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    private java.io.File selectedImageFile = null; // Store the selected image file for upload
    @FXML
    private ImageView editProfileImg;
    @FXML
    private ImageView blurImg;

    @FXML
    private StackPane imgStack;

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
                    component.setImage(user.getImage(), editProfileImg, 84, 84, true, 0);
                    component.setImage(user.getImage(), blurImg, 84, 84, false, 0);

                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(84, 84);
                    clip.setArcWidth(20);
                    clip.setArcHeight(20);
                    imgStack.setClip(clip);
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

            File selectedFile = fileChooser.showOpenDialog(editProfileImg.getScene().getWindow());

            if (selectedFile != null) {
                // Store the selected file for later upload
                this.selectedImageFile = selectedFile;

                // Show file size info to user
                long fileSizeKB = ImageProcessingService.getFileSizeKB(selectedFile);
                System.out.println("Selected image: " + selectedFile.getName() + " (" + fileSizeKB + " KB)");

                // Show info to user if file is large
                if (fileSizeKB > 500) {
                    System.out.println("Large Image Selected, The selected image is " + fileSizeKB
                            + " KB. It will be automatically compressed to reduce upload time and storage usage.");

                }

                // Display the image in the UI
                Image image = new Image(selectedFile.toURI().toString());
                editProfileImg.setImage(image);

                // Set size to 80x80
                editProfileImg.setFitWidth(80);
                editProfileImg.setFitHeight(80);
                editProfileImg.setPreserveRatio(true);

                // Set rounded corners
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(80, 80);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                editProfileImg.setClip(clip);

                System.out.println("Image selected for upload: " + selectedFile.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                DialogUtils.showErrorAlert("Image Selection Error",
                        "Failed to select image. Please try again.");
            });
        }
    }

    @FXML
    private void saveClicked() {
        try {
            // Get current user from AuthController
            userInfo user = AuthController.CurrentUser;

            if (user != null) {
                // Handle image upload if a new image was selected
                if (selectedImageFile != null) {
                    System.out.println("Processing and uploading image to Firebase Storage...");

                    // First, process the image to reduce size if needed
                    File processedImageFile = processImageForUpload(selectedImageFile);

                    if (processedImageFile != null) {
                        // Get the actual userId from the user object (which now includes userId)
                        String userId = firebaseConnection.getUserId(user);
                        if (userId != null) {
                            // Use authenticated upload with user credentials
                            String idToken = firebaseConnection.getIdTokenForUser(AuthController.CurrentUserEmail,
                                    AuthController.CurrentUserPassword);
                            String imageUrl = firebaseConnection.uploadImageToStorageWithToken(processedImageFile,
                                    userId,
                                    idToken);

                            if (imageUrl != null) {
                                user.setImage(imageUrl);
                                System.out.println("Image uploaded successfully. URL: " + imageUrl);
                                // Clear the selected file after successful upload
                                selectedImageFile = null;

                                // Clean up temporary processed file if it's different from original
                                if (!processedImageFile.equals(selectedImageFile)) {
                                    processedImageFile.delete();
                                }
                            } else {
                                System.err.println("Failed to upload image to Firebase Storage");
                                javafx.application.Platform.runLater(() -> {
                                    DialogUtils.showWarningAlert("Image Upload Failed",
                                            "Failed to upload the image. Your profile will be saved without the new image.");
                                });
                            }
                        } else {
                            System.err.println("Could not find userId for user: " + user.getEmail());
                            javafx.application.Platform.runLater(() -> {
                                DialogUtils.showWarningAlert("Image Upload Failed",
                                        "Could not identify user for image upload. Your profile will be saved without the new image.");
                            });
                        }
                    } else {
                        System.err.println("Failed to process image");
                        javafx.application.Platform.runLater(() -> {
                            DialogUtils.showWarningAlert("Image Processing Failed",
                                    "Failed to process the image. Your profile will be saved without the new image.");
                        });
                    }
                }

                // Update other user data
                if (editNameTextField.getText() != null && !editNameTextField.getText().trim().isEmpty()) {
                    user.setName(editNameTextField.getText().trim());
                }
                // Email is not editable in the UI - using current email from user object
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
                        DialogUtils.showInfoAlert("Profile Updated", "Your profile has been updated successfully!");
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
                    DialogUtils.showErrorAlert("Update Failed", "No user session found. Please log in again.");
                });
            }
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                DialogUtils.showErrorAlert("Update Error",
                        "An unexpected error occurred while updating your profile. Please try again.");
            });
        }
    }

    /**
     * Process an image file for upload - compress if needed
     * 
     * @param imageFile The original image file
     * @return Processed image file ready for upload, or null if processing failed
     */
    private File processImageForUpload(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            System.err.println("Invalid image file for processing");
            return null;
        }

        try {
            // Define target size in KB (e.g., 500KB for profile images)
            int targetSizeKB = 500;

            // Check current file size
            long currentSizeKB = ImageProcessingService.getFileSizeKB(imageFile);
            System.out.println("Original image size: " + currentSizeKB + " KB");

            // If file is already under the target size, return original
            if (!ImageProcessingService.needsProcessing(imageFile, targetSizeKB)) {
                System.out.println("Image is already under target size, no processing needed");
                return imageFile;
            }

            // Process the image to reduce size
            System.out.println("Processing image to reduce size...");
            File processedFile = ImageProcessingService.processImageSync(imageFile, targetSizeKB);

            if (processedFile != null) {
                long processedSizeKB = ImageProcessingService.getFileSizeKB(processedFile);
                System.out.println("Processed image size: " + processedSizeKB + " KB");
                return processedFile;
            } else {
                System.err.println("Failed to process image, using original");
                return imageFile; // Fallback to original if processing fails
            }

        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
            return imageFile; // Fallback to original if error occurs
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
                    System.out.println("Password changed successfully for user: " + user.getEmail()); // Show success
                                                                                                      // message
                    javafx.application.Platform.runLater(() -> {
                        DialogUtils.showInfoAlert("Password Changed", "Your password has been changed successfully!");
                    });

                    return true;
                } else {
                    System.err.println("Failed to change password for user: " + user.getEmail());
                    return false;
                }
            } else {
                System.err.println("No current user found for password change");
                javafx.application.Platform.runLater(() -> {
                    DialogUtils.showErrorAlert("Password Change Failed", "No user session found. Please log in again.");
                });
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error during password change: " + e.getMessage());
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                DialogUtils.showErrorAlert("Password Change Error",
                        "An unexpected error occurred while changing password. Please try again.");
            });
            return false;
        }
    }

    @FXML
    private void changePasswordClicked() {
        try { // Create a custom dialog for password change
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle("Change Password");
            dialog.setHeaderText("Enter your current password and new password");

            // Set the application icon
            dialog.setOnShowing(e -> DialogUtils.setDialogIcon(dialog));

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
                String confirmPass = passwords[2]; // Validate that new passwords match
                if (!newPass.equals(confirmPass)) {
                    javafx.application.Platform.runLater(() -> {
                        DialogUtils.showErrorAlert("Password Mismatch",
                                "New password and confirm password do not match. Please try again.");
                    });
                    return;
                } // Validate password strength (optional)
                if (newPass.length() < 6) {
                    javafx.application.Platform.runLater(() -> {
                        DialogUtils.showWarningAlert("Weak Password",
                                "New password should be at least 6 characters long.");
                    });
                    return;
                }

                // Call the password change method
                changeUserPassword(currentPass, newPass);
            });

        } catch (Exception e) {
            System.err.println("Error opening password change dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteAccountClicked() {
        try { // Create confirmation dialog with password verification
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Delete Account");
            dialog.setHeaderText("WARNING: This action cannot be undone!");

            // Set the application icon
            dialog.setOnShowing(e -> DialogUtils.setDialogIcon(dialog));

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

            result.ifPresent(password -> { // Show final confirmation dialog
                javafx.scene.control.Alert finalConfirm = DialogUtils.createAlert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION,
                        "Final Confirmation",
                        "Are you absolutely sure?",
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
                DialogUtils.showErrorAlert("Error", "Failed to open delete account dialog. Please try again.");
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

                if (success) { // Account deleted successfully
                    javafx.application.Platform.runLater(() -> {
                        DialogUtils.showInfoAlert("Account Deleted",
                                "Your account has been permanently deleted. You will now be redirected to the login screen.");

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
                    DialogUtils.showErrorAlert("Delete Account Failed", "No user session found. Please log in again.");
                });
            }
        } catch (Exception e) {
            System.err.println("Error during account deletion: " + e.getMessage());
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                DialogUtils.showErrorAlert("Account Deletion Error",
                        "An unexpected error occurred while deleting your account. Please try again.");
            });
        }
    }

    // ...existing code...
}
