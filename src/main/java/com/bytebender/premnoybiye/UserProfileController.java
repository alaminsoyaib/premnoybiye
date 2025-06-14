package com.bytebender.premnoybiye;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.Component.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserProfileController {

    private Component component = new Component();
    private userInfo displayUser;
    private Stage modalStage;

    @FXML
    private HBox discoverBackButton;

    @FXML
    private VBox card;

    @FXML
    private VBox discoverUserDetails;

    // Card elements
    @FXML
    private ImageView cardImg;
    @FXML
    private ImageView blurImg;
    @FXML
    private StackPane imgStack;
    @FXML
    private Label cardName;
    @FXML
    private Label cardBio;
    @FXML
    private Label cardLocation;

    // Detail elements
    @FXML
    private Label discoverBio;
    @FXML
    private Label discoverAge;
    @FXML
    private Label discoverGender;
    @FXML
    private Label discoverReligion;
    @FXML
    private Label discoverCity;
    @FXML
    private Label discoverEducation;
    @FXML
    private Label discoverProfession;
    @FXML
    private Label discoverIncome;

    @FXML
    public void initialize() {
        // Initialize any default settings if needed
    }

    /**
     * Set the user to be displayed in this profile view
     */
    public void setDisplayUser(userInfo user) {
        this.displayUser = user;
        updateProfileDisplay();
    }

    /**
     * Set the modal stage reference so we can close it
     */
    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }

    /**
     * Update all UI elements with the user's information
     */
    private void updateProfileDisplay() {
        if (displayUser != null) {
            // Update card elements
            cardName.setText(displayUser.getName());
            cardBio.setText(displayUser.getBio());
            cardLocation.setText(displayUser.getCity());

            // Update detail elements
            discoverBio.setText(displayUser.getBio());
            discoverGender.setText(displayUser.getGender());
            discoverReligion.setText(displayUser.getReligion());
            discoverCity.setText(displayUser.getCity());
            discoverEducation.setText(displayUser.getEducation());
            discoverProfession.setText(displayUser.getProfession());
            discoverIncome.setText(displayUser.getIncome());

            // Calculate and display age
            String age = calculateAge(displayUser.getDob());
            discoverAge.setText(age);

            // Load user image
            if (displayUser.getImage() != null && !displayUser.getImage().isEmpty()) {
                component.setImage(displayUser.getImage(), cardImg, 258, 358, true, 0);
                component.setImage(displayUser.getImage(), blurImg, 258, 358, false, 0);

                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(258, 358);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                imgStack.setClip(clip);
            } else {
                // Set default image if no image available
                cardImg.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/card-img.png")));
                blurImg.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/card-img.png")));
            }
        }
    }

    /**
     * Calculate age from date of birth
     */
    private String calculateAge(String dob) {
        try {
            if (dob != null && !dob.isEmpty()) {
                LocalDate birthDate = LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate currentDate = LocalDate.now();
                Period period = Period.between(birthDate, currentDate);
                return String.valueOf(period.getYears());
            }
        } catch (Exception e) {
            System.err.println("Error calculating age: " + e.getMessage());
        }
        return "N/A";
    }

    /**
     * Handle back button click - close the modal
     */
    @FXML
    void handleBack(MouseEvent event) {
        if (modalStage != null) {
            modalStage.close();
        }
    }
}
