package com.bytebender.premnoybiye;

import javafx.fxml.FXML;
import java.io.IOException;

import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

public class StepperController {
	int flag = 0;
	private FirebaseConnection firebaseConnection = new FirebaseConnection();
	private java.io.File selectedImageFile = null; // Store the selected image file for upload

	@FXML
	private VBox Stepper1;
	@FXML
	private VBox Stepper2;
	@FXML
	private VBox Stepper3;

	@FXML
	private VBox imagePicker;
	@FXML
	private ImageView img_inside_imgPicker;

	@FXML
	private ImageView stepperIcon_1;
	@FXML
	private ImageView stepperIcon_2;
	@FXML
	private ImageView stepperIcon_3;
	@FXML
	private ImageView stepperLine_12;
	@FXML
	private ImageView stepperLine_23;
	@FXML
	private ImageView stepperProgress_1;
	@FXML
	private ImageView stepperProgress_2;
	@FXML
	private ImageView stepperProgress_3;

	@FXML
	private HBox nextButton;
	@FXML
	private HBox prevButton;

	@FXML
	private DatePicker dobComboBox;
	@FXML
	private ComboBox<?> genderComboBox;
	@FXML
	private ComboBox<?> religionComboBox;
	@FXML
	private ComboBox<?> cityComboBox;

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

	@FXML
	private void initialize() {
		// Initialize: Show Stepper1, hide Stepper2
		Stepper1.setVisible(true);
		Stepper1.setManaged(true);
		Stepper2.setVisible(false);
		Stepper2.setManaged(false);
		Stepper3.setVisible(false);
		Stepper3.setManaged(false);
		// image picker starts
		img_inside_imgPicker.setOnMouseClicked((MouseEvent event) -> {
			try {
				javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
				fileChooser.setTitle("Select Image");
				fileChooser.getExtensionFilters().addAll(
						new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg",
								"*.jpeg", "*.gif"));

				java.io.File selectedFile = fileChooser.showOpenDialog(img_inside_imgPicker.getScene().getWindow());

				if (selectedFile != null) {
					// Store the selected file for later upload
					this.selectedImageFile = selectedFile;

					// Display the image in the UI
					Image image = new Image(selectedFile.toURI().toString());
					img_inside_imgPicker.setImage(image);

					// Set size to 80x80
					img_inside_imgPicker.setFitWidth(80);
					img_inside_imgPicker.setFitHeight(80);
					img_inside_imgPicker.setPreserveRatio(false);

					// Set rounded corners
					javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(80, 80);
					clip.setArcWidth(20);
					clip.setArcHeight(20);
					img_inside_imgPicker.setClip(clip);

					System.out.println("Image selected for upload: " + selectedFile.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		// image picker ends
	}

	@FXML
	private void nextButtonClicked() throws IOException {
		// flag 0 means Stepper1 is visible
		// flag 1 means Stepper2 is visible
		// flag 2 means Stepper3 is visible
		// flag 3 means done
		if (flag == 0) {
			Stepper1.setVisible(false);
			Stepper1.setManaged(false);
			Stepper2.setVisible(true);
			Stepper2.setManaged(true);
			stepperIcon_1.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Complete.png")));
			stepperProgress_1.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Complete.png")));
			stepperLine_12.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Stepper_Line_Enable.png")));
			stepperIcon_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Progress.png")));
			stepperProgress_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Progress.png")));

			stepperIcon_1.scaleXProperty().set(1.0);
			stepperIcon_1.scaleYProperty().set(1.0);
			stepperIcon_2.scaleXProperty().set(1.27);
			stepperIcon_2.scaleYProperty().set(1.27);

			prevButton.setOpacity(1);

			flag = 1;
		} else if (flag == 1) {
			Stepper2.setVisible(false);
			Stepper2.setManaged(false);
			Stepper3.setVisible(true);
			Stepper3.setManaged(true);
			stepperIcon_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Complete.png")));
			stepperProgress_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Complete.png")));
			stepperLine_23.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Stepper_Line_Enable.png")));
			stepperIcon_3.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Progress.png")));
			stepperProgress_3.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Progress.png")));

			stepperIcon_2.scaleXProperty().set(1.0);
			stepperIcon_2.scaleYProperty().set(1.0);
			stepperIcon_3.scaleXProperty().set(1.27);
			stepperIcon_3.scaleYProperty().set(1.27);

			prevButton.setOpacity(1);
			flag = 2;
		} else if (flag == 2) {
			if (dobComboBox.getValue() != null) {
				AuthController.CurrentUser.setDob(dobComboBox.getValue().toString());
			}
			if (genderComboBox.getValue() != null) {
				AuthController.CurrentUser.setGender(genderComboBox.getValue().toString());
			}
			if (religionComboBox.getValue() != null) {
				AuthController.CurrentUser.setReligion(religionComboBox.getValue().toString());
			}
			if (cityComboBox.getValue() != null) {
				AuthController.CurrentUser.setCity(cityComboBox.getValue().toString());
			} // Handle image upload if a new image was selected
			if (selectedImageFile != null) {
				System.out.println("Uploading image to Firebase Storage...");

				// Get the actual userId from email
				String userId = firebaseConnection.getUserIdFromEmail(AuthController.CurrentUser.getEmail());

				if (userId != null) {
					String imageUrl = firebaseConnection.uploadImageToStorage(selectedImageFile, userId);

					if (imageUrl != null) {
						AuthController.CurrentUser.setImage(imageUrl);
						System.out.println("Image uploaded successfully. URL: " + imageUrl);
						// Clear the selected file after successful upload
						selectedImageFile = null;
					} else {
						System.err.println("Failed to upload image to Firebase Storage");
						// Continue with profile update even if image upload fails
					}
				} else {
					System.err.println("Could not find userId for email: " + AuthController.CurrentUser.getEmail());
					// Continue with profile update even if image upload fails
				}
			}

			if (highestEduComboBox.getValue() != null) {
				AuthController.CurrentUser.setEducation(highestEduComboBox.getValue().toString());
			}
			if (professionComboBox.getValue() != null) {
				AuthController.CurrentUser.setProfession(professionComboBox.getValue().toString());
			}
			if (monthlyIncomeComboBox.getValue() != null) {
				AuthController.CurrentUser.setIncome(monthlyIncomeComboBox.getValue().toString());
			}
			if (aboutYouTextField.getText() != null) {
				AuthController.CurrentUser.setBio(aboutYouTextField.getText());
			}
			if (prefPartnerAgeComboBox.getValue() != null) {
				AuthController.CurrentUser.setPrefAge(prefPartnerAgeComboBox.getValue().toString());
			}
			if (prefLocationComboBox.getValue() != null) {
				AuthController.CurrentUser.setPrefLocation(prefLocationComboBox.getValue().toString());
			}
			if (prefProfessionComboBox.getValue() != null) {
				AuthController.CurrentUser.setPrefProfession(prefProfessionComboBox.getValue().toString());
			}

			// Update user profile in Firebase with all collected data
			boolean updateSuccess = firebaseConnection.updateUserProfile(AuthController.CurrentUser);

			if (updateSuccess) {
				System.out.println("User profile updated successfully in Firebase!");
				App.setRoot("sidebar");
			} else {
				System.err.println("Failed to update user profile in Firebase, but proceeding to sidebar");
				App.setRoot("sidebar"); // Still proceed even if Firebase update fails
			}
			// App.setRoot("editProfile");
			// App.setRoot("mymatches");

		}
	}

	@FXML
	private void prevButtonClicked() throws IOException {
		if (flag == 1) {
			Stepper2.setVisible(false);
			Stepper2.setManaged(false);
			Stepper1.setVisible(true);
			Stepper1.setManaged(true);
			stepperIcon_1.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Progress.png")));
			stepperProgress_1.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Progress.png")));
			stepperLine_12.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Stepper_Line_Disable.png")));
			stepperIcon_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Pending.png")));
			stepperProgress_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Pending.png")));

			stepperIcon_2.scaleXProperty().set(1.0);
			stepperIcon_2.scaleYProperty().set(1.0);
			stepperIcon_1.scaleXProperty().set(1.27);
			stepperIcon_1.scaleYProperty().set(1.27);

			prevButton.setOpacity(0.3);

			flag = 0;
		} else if (flag == 2) {
			Stepper3.setVisible(false);
			Stepper3.setManaged(false);
			Stepper2.setVisible(true);
			Stepper2.setManaged(true);
			stepperIcon_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Progress.png")));
			stepperProgress_2.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Progress.png")));
			stepperLine_23.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Stepper_Line_Disable.png")));
			stepperIcon_3.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Checkbox_Pending.png")));
			stepperProgress_3.setImage(new Image(
					App.class.getResourceAsStream(
							"/com/bytebender/premnoybiye/img/icon/Tag_Pending.png")));

			stepperIcon_3.scaleXProperty().set(1.0);
			stepperIcon_3.scaleYProperty().set(1.0);
			stepperIcon_2.scaleXProperty().set(1.27);
			stepperIcon_2.scaleYProperty().set(1.27);

			flag = 1;
		}

	}
}
