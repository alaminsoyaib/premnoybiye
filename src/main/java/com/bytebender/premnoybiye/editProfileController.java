package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.Component.DialogUtils;
import com.bytebender.premnoybiye.Component.ImageProcessingService;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.util.Optional;

public class editProfileController {
    private final Component component = new Component();
    private final FirebaseConnection firebaseConnection = new FirebaseConnection();
    private File selectedImageFile = null;

    @FXML
    private ImageView editProfileImg, blurImg;
    @FXML
    private StackPane imgStack;
    @FXML
    private Button changePicture, changePasswordButton, deleteAccountButton;
    @FXML
    private TextField editAboutYouTextField, editNameTextField;
    @FXML
    private Label editProfileName, editEmailLabel;
    @FXML
    private DatePicker editDobComboBox;
    @FXML
    private ComboBox<?> editGenderComboBox, editReligionComboBox, editCityComboBox, editHighestEduComboBox,
            editProfessionComboBox, editMonthlyIncomeComboBox, editPrefPartnerAgeComboBox,
            editPrefLocationComboBox, editPrefProfessionComboBox;
    @FXML
    private PasswordField passwordTextField;

    public void initialize() {
        userInfo user = AuthController.CurrentUser;
        if (user == null)
            return;

        try {
            populateUserFields(user);
            setupUserImage(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateUserFields(userInfo user) {
        if (user.getName() != null && !user.getName().isEmpty()) {
            editProfileName.setText(user.getName());
            editNameTextField.setText(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            editEmailLabel.setText(user.getEmail());
        }
        setPromptIfNotEmpty(user.getReligion(), editReligionComboBox);
        setPromptIfNotEmpty(user.getDob(), editDobComboBox);
        setPromptIfNotEmpty(user.getGender(), editGenderComboBox);
        setPromptIfNotEmpty(user.getCity(), editCityComboBox);
        setPromptIfNotEmpty(user.getEducation(), editHighestEduComboBox);
        setPromptIfNotEmpty(user.getProfession(), editProfessionComboBox);
        setPromptIfNotEmpty(user.getIncome(), editMonthlyIncomeComboBox);
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            editAboutYouTextField.setText(user.getBio());
        }
        setPromptIfNotEmpty(user.getPrefAge(), editPrefPartnerAgeComboBox);
        setPromptIfNotEmpty(user.getPrefLocation(), editPrefLocationComboBox);
        setPromptIfNotEmpty(user.getPrefProfession(), editPrefProfessionComboBox);
    }

    private void setPromptIfNotEmpty(String value, Control control) {
        if (value != null && !value.isEmpty()) {
            if (control instanceof ComboBox) {
                ((ComboBox<?>) control).setPromptText(value);
            } else if (control instanceof DatePicker) {
                ((DatePicker) control).setPromptText(value);
            }
        }
    }

    private void setupUserImage(userInfo user) {
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            component.setImageWithClipCached(user, editProfileImg, blurImg, imgStack, 84, 84);
        }
    }

    @FXML
    private void changePictureClicked() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            File selectedFile = fileChooser.showOpenDialog(editProfileImg.getScene().getWindow());
            if (selectedFile != null) {
                handleImageSelection(selectedFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.showErrorAlert("Image Selection Error", "Failed to select image. Please try again.");
        }
    }

    private void handleImageSelection(File selectedFile) {
        this.selectedImageFile = selectedFile;

        long fileSizeKB = ImageProcessingService.getFileSizeKB(selectedFile);
        System.out.println("Selected image: " + selectedFile.getName() + " (" + fileSizeKB + " KB)");

        if (fileSizeKB > 500) {
            System.out.println("Large image selected (" + fileSizeKB + " KB). Will be compressed automatically.");
        }

        Image image = new Image(selectedFile.toURI().toString());
        editProfileImg.setImage(image);
        editProfileImg.setFitWidth(80);
        editProfileImg.setFitHeight(80);
        editProfileImg.setPreserveRatio(true);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(80, 80);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        editProfileImg.setClip(clip);
    }

    @FXML
    private void saveClicked() {
        try {
            userInfo user = AuthController.CurrentUser;
            if (user == null) {
                DialogUtils.showErrorAlert("Update Failed", "No user session found. Please log in again.");
                return;
            }

            handleImageUpload(user);
            updateUserFields(user);
            saveUserProfile(user);
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showErrorAlert("Update Error",
                    "An unexpected error occurred while updating your profile. Please try again.");
        }
    }

    private void handleImageUpload(userInfo user) {
        if (selectedImageFile == null)
            return;

        System.out.println("Processing and uploading image to Firebase Storage...");
        File processedImageFile = processImageForUpload(selectedImageFile);

        if (processedImageFile != null) {
            String userId = firebaseConnection.getUserId(user);
            if (userId != null) {
                String idToken = firebaseConnection.getIdTokenForUser(AuthController.CurrentUserEmail,
                        AuthController.CurrentUserPassword);
                String imageUrl = firebaseConnection.uploadImageToStorageWithToken(processedImageFile, userId, idToken);

                if (imageUrl != null) {
                    user.setImage(imageUrl);
                    System.out.println("Image uploaded successfully. URL: " + imageUrl);
                    selectedImageFile = null;

                    if (!processedImageFile.equals(selectedImageFile)) {
                        processedImageFile.delete();
                    }
                } else {
                    DialogUtils.showWarningAlert("Image Upload Failed",
                            "Failed to upload the image. Your profile will be saved without the new image.");
                }
            }
        }
    }

    private void updateUserFields(userInfo user) {
        updateFieldIfNotEmpty(editNameTextField.getText(), user::setName);
        updateFieldIfNotEmpty(getValue(editDobComboBox), user::setDob);
        updateFieldIfNotEmpty(getValue(editGenderComboBox), user::setGender);
        updateFieldIfNotEmpty(getValue(editReligionComboBox), user::setReligion);
        updateFieldIfNotEmpty(getValue(editCityComboBox), user::setCity);
        updateFieldIfNotEmpty(getValue(editHighestEduComboBox), user::setEducation);
        updateFieldIfNotEmpty(getValue(editProfessionComboBox), user::setProfession);
        updateFieldIfNotEmpty(getValue(editMonthlyIncomeComboBox), user::setIncome);
        updateFieldIfNotEmpty(editAboutYouTextField.getText(), user::setBio);
        updateFieldIfNotEmpty(getValue(editPrefPartnerAgeComboBox), user::setPrefAge);
        updateFieldIfNotEmpty(getValue(editPrefLocationComboBox), user::setPrefLocation);
        updateFieldIfNotEmpty(getValue(editPrefProfessionComboBox), user::setPrefProfession);
    }

    private void updateFieldIfNotEmpty(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.trim().isEmpty()) {
            setter.accept(value.trim());
        }
    }

    private String getValue(Control control) {
        if (control instanceof ComboBox && ((ComboBox<?>) control).getValue() != null) {
            return ((ComboBox<?>) control).getValue().toString();
        } else if (control instanceof DatePicker && ((DatePicker) control).getValue() != null) {
            return ((DatePicker) control).getValue().toString();
        }
        return null;
    }

    private void saveUserProfile(userInfo user) {
        boolean updateSuccess = firebaseConnection.updateUserProfile(user);
        if (updateSuccess) {
            System.out.println("User profile updated successfully in Firebase!");
            DialogUtils.showInfoAlert("Profile Updated", "Your profile has been updated successfully!");
            try {
                App.setRoot("sidebar");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to update user profile in Firebase");
        }
    }

    private File processImageForUpload(File imageFile) {
        if (imageFile == null || !imageFile.exists())
            return null;

        try {
            int targetSizeKB = 500;
            if (!ImageProcessingService.needsProcessing(imageFile, targetSizeKB)) {
                return imageFile;
            }

            File processedFile = ImageProcessingService.processImageSync(imageFile, targetSizeKB);
            return processedFile != null ? processedFile : imageFile;
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            return imageFile;
        }
    }

    @FXML
    private void changePasswordClicked() {
        Dialog<String[]> dialog = createPasswordChangeDialog();
        Optional<String[]> result = dialog.showAndWait();

        result.ifPresent(passwords -> {
            if (!passwords[1].equals(passwords[2])) {
                DialogUtils.showErrorAlert("Password Mismatch", "New password and confirm password do not match.");
                return;
            }
            if (passwords[1].length() < 6) {
                DialogUtils.showWarningAlert("Weak Password", "New password should be at least 6 characters long.");
                return;
            }
            changeUserPassword(passwords[0], passwords[1]);
        });
    }

    private Dialog<String[]> createPasswordChangeDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current password and new password");
        dialog.setOnShowing(e -> DialogUtils.setDialogIcon(dialog));

        ButtonType changeButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPassword, 1, 2);

        javafx.scene.Node changeButton = dialog.getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);

        Runnable updateButtonState = () -> changeButton.setDisable(
                currentPassword.getText().trim().isEmpty() ||
                        newPassword.getText().trim().isEmpty() ||
                        confirmPassword.getText().trim().isEmpty());

        currentPassword.textProperty().addListener((obs, old, text) -> updateButtonState.run());
        newPassword.textProperty().addListener((obs, old, text) -> updateButtonState.run());
        confirmPassword.textProperty().addListener((obs, old, text) -> updateButtonState.run());

        dialog.getDialogPane().setContent(grid);
        javafx.application.Platform.runLater(currentPassword::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return new String[] { currentPassword.getText(), newPassword.getText(), confirmPassword.getText() };
            }
            return null;
        });

        return dialog;
    }

    public boolean changeUserPassword(String currentPassword, String newPassword) {
        try {
            userInfo user = AuthController.CurrentUser;
            if (user == null) {
                DialogUtils.showErrorAlert("Password Change Failed", "No user session found. Please log in again.");
                return false;
            }

            boolean success = firebaseConnection.changePassword(user, currentPassword, newPassword);
            if (success) {
                DialogUtils.showInfoAlert("Password Changed", "Your password has been changed successfully!");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error during password change: " + e.getMessage());
            DialogUtils.showErrorAlert("Password Change Error",
                    "An unexpected error occurred while changing password. Please try again.");
            return false;
        }
    }

    @FXML
    private void deleteAccountClicked() {
        Dialog<String> dialog = createDeleteAccountDialog();
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(password -> {
            Alert finalConfirm = DialogUtils.createAlert(Alert.AlertType.CONFIRMATION, "Final Confirmation",
                    "Are you absolutely sure?",
                    "This is your last chance to cancel. Once deleted, your account cannot be recovered.");

            finalConfirm.getButtonTypes().setAll(
                    new ButtonType("Yes, Delete My Account", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));

            Optional<ButtonType> finalResult = finalConfirm.showAndWait();
            if (finalResult.isPresent() && finalResult.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                deleteUserAccount(password);
            }
        });
    }

    private Dialog<String> createDeleteAccountDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Delete Account");
        dialog.setHeaderText("WARNING: This action cannot be undone!");
        dialog.setOnShowing(e -> DialogUtils.setDialogIcon(dialog));

        ButtonType deleteButtonType = new ButtonType("Delete Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label warningLabel = new Label("Deleting your account will permanently remove:\n" +
                "• All your profile information\n• Your uploaded photos\n• Your match history\n• All associated data\n\n"
                +
                "Please enter your password to confirm:");
        warningLabel.setStyle("-fx-text-fill: #d73027; -fx-font-size: 12px;");
        warningLabel.setWrapText(true);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        grid.add(warningLabel, 0, 0, 2, 1);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        javafx.scene.Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setDisable(true);
        deleteButton.setStyle("-fx-background-color: #e45f5f; -fx-text-fill: white;");

        passwordField.textProperty().addListener((obs, old, text) -> deleteButton.setDisable(text.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);
        javafx.application.Platform.runLater(passwordField::requestFocus);

        dialog.setResultConverter(dialogButton -> dialogButton == deleteButtonType ? passwordField.getText() : null);

        return dialog;
    }

    private void deleteUserAccount(String password) {
        try {
            userInfo user = AuthController.CurrentUser;
            if (user == null) {
                DialogUtils.showErrorAlert("Delete Account Failed", "No user session found. Please log in again.");
                return;
            }

            boolean success = firebaseConnection.deleteUser(user, password);
            if (success) {
                DialogUtils.showInfoAlert("Account Deleted",
                        "Your account has been permanently deleted. You will now be redirected to the login screen.");
                AuthController.CurrentUser = null;
                try {
                    App.setRoot("loginsignupchoice");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error during account deletion: " + e.getMessage());
            DialogUtils.showErrorAlert("Account Deletion Error",
                    "An unexpected error occurred while deleting your account. Please try again.");
        }
    }
}
