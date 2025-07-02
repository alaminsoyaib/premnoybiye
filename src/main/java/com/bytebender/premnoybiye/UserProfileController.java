package com.bytebender.premnoybiye;

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
    private final Component component = new Component();
    private userInfo displayUser;
    private Stage modalStage;

    @FXML
    private HBox discoverBackButton;
    @FXML
    private VBox card, discoverUserDetails;
    @FXML
    private ImageView cardImg, blurImg;
    @FXML
    private StackPane imgStack;
    @FXML
    private Label cardName, cardBio, cardLocation;
    @FXML
    private Label discoverBio, discoverAge, discoverGender, discoverReligion, discoverCity, discoverEducation,
            discoverProfession, discoverIncome;

    @FXML
    public void initialize() {
    }

    public void setDisplayUser(userInfo user) {
        this.displayUser = user;
        updateProfileDisplay();
    }

    public void setModalStage(Stage stage) {
        this.modalStage = stage;
    }

    private void updateProfileDisplay() {
        if (displayUser == null) {
            return;
        }

        updateCardElements();
        updateDetailElements();
        updateUserImage();
    }

    private void updateCardElements() {
        cardName.setText(displayUser.getName());
        cardBio.setText(displayUser.getBio());
        cardLocation.setText(displayUser.getCity());
    }

    private void updateDetailElements() {
        discoverBio.setText(displayUser.getBio());
        discoverGender.setText(displayUser.getGender());
        discoverReligion.setText(displayUser.getReligion());
        discoverCity.setText(displayUser.getCity());
        discoverEducation.setText(displayUser.getEducation());
        discoverProfession.setText(displayUser.getProfession());
        discoverIncome.setText(displayUser.getIncome());
        discoverAge.setText(component.calculateAge(displayUser.getDob()));
    }

    private void updateUserImage() {
        component.setImageWithClipCached(displayUser, cardImg, blurImg, imgStack, 258, 358);
    }

    @FXML
    void handleBack(MouseEvent event) {
        if (modalStage != null) {
            modalStage.close();
        }
    }
}
