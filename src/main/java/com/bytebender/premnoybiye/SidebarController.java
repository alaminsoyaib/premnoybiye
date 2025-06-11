package com.bytebender.premnoybiye;

import java.io.IOException;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.DBConnection.userInfo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SidebarController {
    int flag = 0;
    private Component component = new Component();

    @FXML
    private ImageView sidebarCollapse;
    @FXML
    private HBox sidebarProfileBox;
    @FXML
    private ImageView sidebarProfileImage;
    @FXML
    private Label sidebarUserName;
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
    private VBox container;

    private void loadCardIntoContainer() {
        try { // Fxml inside Fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("card.fxml"));
            Parent cardContent = loader.load();
            container.getChildren().clear();
            container.getChildren().add(cardContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void TemporarySwitchToStepper() throws IOException {
        App.setRoot("stepper");
    }

    @FXML
    void sidebarCollapse(MouseEvent event) {
        // Calling toggling method of the sidebar Component
        flag = component.toggleSidebarState(sidebarCollapse, sidebarProfileBox, discoverLabel, matchesLabel,
                profileLabel, msgLabel, logoutLabel, Sidebar, flag);
    }

    public void initialize() {
        // Load the card.fxml into the container
        loadCardIntoContainer();

        // Access the user info
        userInfo user = StepperController.currentUser;
        try {
            if (user != null) {
                if (user.getName() != "") {
                    sidebarUserName.setText(user.getName());
                }
                if (user.getImage() != "") {
                    // component.setImage(user.getImage(), demoProfileImg, 84, 84, false, 20);
                    component.setImage(user.getImage(), sidebarProfileImage, 40, 40, false, 20);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
