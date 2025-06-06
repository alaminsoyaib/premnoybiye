package com.bytebender.premnoybiye;
// package com.bytebender.premnoybiye.DBConnection;

import javafx.fxml.FXML;
import java.io.IOException;

import com.bytebender.premnoybiye.DBConnection.userInfo;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

public class StepperController {
	int flag = 0;

	public static userInfo currentUser;

	@FXML
	private Label demoLabel;

	@FXML
	private VBox Stepper1;
	@FXML
	private VBox Stepper2;
	@FXML
	private VBox Stepper3;

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
	private ComboBox<?> religionComboBox;
	@FXML
	private ComboBox<?> cityComboBox;
	@FXML
	private ComboBox<?> genderComboBox;

	@FXML
	private void initialize() {
		// Initialize: Show Stepper1, hide Stepper2
		Stepper1.setVisible(true);
		Stepper1.setManaged(true);
		Stepper2.setVisible(false);
		Stepper2.setManaged(false);
		Stepper3.setVisible(false);
		Stepper3.setManaged(false);
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
			String name = "";
			String email = "";
			String password = "";
			String dob = "";
			String gender = "";
			String religion = "";
			String city = "";
			if (genderComboBox.getValue() != null) {
				gender = genderComboBox.getValue().toString();
			}
			if (religionComboBox.getValue() != null) {
				religion = religionComboBox.getValue().toString();
			}
			if (cityComboBox.getValue() != null) {
				city = cityComboBox.getValue().toString();
			}

			userInfo user = new userInfo(name, email, password, dob, gender, religion, city);

			currentUser = user;

			App.setRoot("DemoTest");

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

			nextButton.setOpacity(1);

			flag = 1;
		}

	}
}
