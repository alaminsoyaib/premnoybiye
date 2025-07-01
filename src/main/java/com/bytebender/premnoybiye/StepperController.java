package com.bytebender.premnoybiye;

import javafx.fxml.FXML;
import java.io.IOException;
import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.Component.ImageProcessingService;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

public class StepperController {
	private int flag = 0;
	private Component component = new Component();
	private FirebaseConnection firebaseConnection = new FirebaseConnection();
	private java.io.File selectedImageFile = null;
	private java.io.File processedImageFile = null;

	@FXML
	private VBox Stepper1, Stepper2, Stepper3, imagePicker;
	@FXML
	private ImageView img_inside_imgPicker, blurImg;
	@FXML
	private StackPane imgStack;
	@FXML
	private ImageView stepperIcon_1, stepperIcon_2, stepperIcon_3;
	@FXML
	private ImageView stepperLine_12, stepperLine_23;
	@FXML
	private ImageView stepperProgress_1, stepperProgress_2, stepperProgress_3;
	@FXML
	private HBox nextButton, prevButton;
	@FXML
	private DatePicker dobComboBox;
	@FXML
	private ComboBox<?> genderComboBox, religionComboBox, cityComboBox;
	@FXML
	private ComboBox<?> highestEduComboBox, professionComboBox, monthlyIncomeComboBox;
	@FXML
	private TextField aboutYouTextField;
	@FXML
	private ComboBox<?> prefPartnerAgeComboBox, prefLocationComboBox, prefProfessionComboBox;

	@FXML
	private void initialize() {
		showStep(1);
		setupImagePicker();
	}

	private void setupImagePicker() {
		img_inside_imgPicker.setOnMouseClicked((MouseEvent event) -> {
			try {
				javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
				fileChooser.setTitle("Select Image");
				fileChooser.getExtensionFilters().add(
						new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg",
								"*.gif"));

				java.io.File selectedFile = fileChooser.showOpenDialog(img_inside_imgPicker.getScene().getWindow());
				if (selectedFile != null) {
					handleImageSelection(selectedFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Image Selection Error - Failed to select or process image. Please try again.");
			}
		});
	}

	private void handleImageSelection(java.io.File selectedFile) {
		selectedImageFile = selectedFile;
		long fileSizeKB = ImageProcessingService.getFileSizeKB(selectedFile);
		System.out.println("Selected image: " + selectedFile.getName() + " (" + fileSizeKB + " KB)");

		processedImageFile = processImageForUpload(selectedFile);
		if (processedImageFile != null) {
			long processedSizeKB = ImageProcessingService.getFileSizeKB(processedImageFile);
			System.out.println("Image processed. Final size: " + processedSizeKB + " KB");
			if (fileSizeKB > 500 && processedSizeKB < fileSizeKB) {
				System.out.println("Image compressed from " + fileSizeKB + " KB to " + processedSizeKB);
			}
		}

		component.setImageWithClip(selectedFile.toURI().toString(), img_inside_imgPicker, blurImg, imgStack, 80, 80);
		System.out.println("Image selected for upload: " + selectedFile.getName());
	}

	@FXML
	private void nextButtonClicked() throws IOException {
		switch (flag) {
			case 0:
				transitionStep(1, 2, "Complete", "Progress");
				break;
			case 1:
				transitionStep(2, 3, "Complete", "Progress");
				break;
			case 2:
				saveUserDataAndFinish();
				break;
		}
	}

	@FXML
	private void prevButtonClicked() throws IOException {
		switch (flag) {
			case 1:
				transitionStep(2, 1, "Progress", "Pending");
				prevButton.setOpacity(0.3);
				break;
			case 2:
				transitionStep(3, 2, "Progress", "Pending");
				break;
		}
	}

	private void transitionStep(int fromStep, int toStep, String fromState, String toState) {
		showStep(toStep);
		updateStepperUI(fromStep, toStep, fromState, toState);
		flag = toStep == 1 ? 0 : toStep == 2 ? 1 : 2;
		if (toStep > 1)
			prevButton.setOpacity(1);
	}

	private void showStep(int step) {
		component.toggleVisibility(step == 1, Stepper1);
		component.toggleVisibility(step == 2, Stepper2);
		component.toggleVisibility(step == 3, Stepper3);
	}

	private void updateStepperUI(int fromStep, int toStep, String fromState, String toState) {
		ImageView fromIcon = getStepperIcon(fromStep);
		ImageView toIcon = getStepperIcon(toStep);
		ImageView fromProgress = getStepperProgress(fromStep);
		ImageView toProgress = getStepperProgress(toStep);
		ImageView line = getStepperLine(fromStep, toStep);

		setStepperImages(fromIcon, fromProgress, fromState);
		setStepperImages(toIcon, toProgress, toState);

		if (line != null) {
			String lineState = toStep > fromStep ? "Enable" : "Disable";
			line.setImage(createImage("Stepper_Line_" + lineState + ".png"));
		}

		resetScale(fromIcon);
		setScale(toIcon, 1.27);
	}

	private ImageView getStepperIcon(int step) {
		return step == 1 ? stepperIcon_1 : step == 2 ? stepperIcon_2 : stepperIcon_3;
	}

	private ImageView getStepperProgress(int step) {
		return step == 1 ? stepperProgress_1 : step == 2 ? stepperProgress_2 : stepperProgress_3;
	}

	private ImageView getStepperLine(int fromStep, int toStep) {
		if ((fromStep == 1 && toStep == 2) || (fromStep == 2 && toStep == 1))
			return stepperLine_12;
		if ((fromStep == 2 && toStep == 3) || (fromStep == 3 && toStep == 2))
			return stepperLine_23;
		return null;
	}

	private void setStepperImages(ImageView icon, ImageView progress, String state) {
		String iconFile = "Checkbox_" + state + ".png";
		String progressFile = "Tag_" + state + ".png";
		icon.setImage(createImage(iconFile));
		progress.setImage(createImage(progressFile));
	}

	private Image createImage(String filename) {
		return new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/" + filename));
	}

	private void resetScale(ImageView imageView) {
		setScale(imageView, 1.0);
	}

	private void setScale(ImageView imageView, double scale) {
		imageView.setScaleX(scale);
		imageView.setScaleY(scale);
	}

	private void saveUserDataAndFinish() throws IOException {
		updateUserProfileData();
		handleImageUpload();

		boolean updateSuccess = firebaseConnection.updateUserProfile(AuthController.CurrentUser);
		if (updateSuccess) {
			System.out.println("User profile updated successfully in Firebase!");
		} else {
			System.err.println("Failed to update user profile in Firebase, but proceeding to sidebar");
		}
		App.setRoot("sidebar");
	}

	private void updateUserProfileData() {
		setUserFieldIfNotNull(dobComboBox.getValue(), v -> AuthController.CurrentUser.setDob(v.toString()));
		setUserFieldIfNotNull(genderComboBox.getValue(), v -> AuthController.CurrentUser.setGender(v.toString()));
		setUserFieldIfNotNull(religionComboBox.getValue(), v -> AuthController.CurrentUser.setReligion(v.toString()));
		setUserFieldIfNotNull(cityComboBox.getValue(), v -> AuthController.CurrentUser.setCity(v.toString()));
		setUserFieldIfNotNull(highestEduComboBox.getValue(),
				v -> AuthController.CurrentUser.setEducation(v.toString()));
		setUserFieldIfNotNull(professionComboBox.getValue(),
				v -> AuthController.CurrentUser.setProfession(v.toString()));
		setUserFieldIfNotNull(monthlyIncomeComboBox.getValue(),
				v -> AuthController.CurrentUser.setIncome(v.toString()));
		setUserFieldIfNotNull(aboutYouTextField.getText(), v -> AuthController.CurrentUser.setBio((String) v));
		setUserFieldIfNotNull(prefPartnerAgeComboBox.getValue(),
				v -> AuthController.CurrentUser.setPrefAge(v.toString()));
		setUserFieldIfNotNull(prefLocationComboBox.getValue(),
				v -> AuthController.CurrentUser.setPrefLocation(v.toString()));
		setUserFieldIfNotNull(prefProfessionComboBox.getValue(),
				v -> AuthController.CurrentUser.setPrefProfession(v.toString()));
	}

	private void setUserFieldIfNotNull(Object value, java.util.function.Consumer<Object> setter) {
		if (value != null)
			setter.accept(value);
	}

	private void handleImageUpload() {
		if (selectedImageFile == null)
			return;

		System.out.println("Uploading image to Firebase Storage...");
		String userId = firebaseConnection.getUserId(AuthController.CurrentUser);
		if (userId != null) {
			String idToken = firebaseConnection.getIdTokenForUser(AuthController.CurrentUserEmail,
					AuthController.CurrentUserPassword);
			java.io.File imageToUpload = processedImageFile != null ? processedImageFile : selectedImageFile;
			String imageUrl = firebaseConnection.uploadImageToStorageWithToken(imageToUpload, userId, idToken);

			if (imageUrl != null) {
				AuthController.CurrentUser.setImage(imageUrl);
				System.out.println("Image uploaded successfully. URL: " + imageUrl);
			} else {
				System.err.println("Failed to upload image to Firebase Storage");
			}
			cleanupTempFiles();
		} else {
			System.err.println("Could not find userId for user: " + AuthController.CurrentUser.getEmail());
		}
	}

	private void cleanupTempFiles() {
		if (processedImageFile != null && !processedImageFile.equals(selectedImageFile)) {
			try {
				if (processedImageFile.exists() && processedImageFile.delete()) {
					System.out.println("Temporary processed image file cleaned up");
				}
			} catch (Exception e) {
				System.err.println("Failed to clean up temporary file: " + e.getMessage());
			}
		}
		selectedImageFile = null;
		processedImageFile = null;
	}

	private java.io.File processImageForUpload(java.io.File imageFile) {
		if (imageFile == null)
			return null;
		try {
			java.io.File processedFile = ImageProcessingService.processImageSync(imageFile, 500);
			return processedFile != null ? processedFile : imageFile;
		} catch (Exception e) {
			System.err.println("Error processing image: " + e.getMessage());
			return imageFile;
		}
	}
}
