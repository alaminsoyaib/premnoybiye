package com.bytebender.premnoybiye;

import java.io.IOException;

import com.bytebender.premnoybiye.Component.Component;
import com.bytebender.premnoybiye.Component.PageLoader;
// import com.bytebender.premnoybiye.DBConnection.userInfo; 

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private ImageView sidebarBlurImg;

    @FXML
    private StackPane imgStack;

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
    private HBox discoverButton;
    @FXML
    private HBox profileButton;
    @FXML
    private HBox mymatchesButton;
    @FXML
    private HBox messageButton;
    @FXML
    private HBox logoutButton;
    @FXML
    private VBox container;

    @FXML
    void menuItemSwitch(MouseEvent event) throws IOException {
        // Remove selected class from all menu items
        clearAllSelectedStates();

        if (event.getSource() == discoverButton) {
            PageLoader.loadPageWithAnimation(container, "com/bytebender/premnoybiye/discover", "Loading Discover...");
            discoverButton.getStyleClass().add("selected");

        } else if (event.getSource() == profileButton) {
            PageLoader.loadPageWithAnimation(container, "com/bytebender/premnoybiye/editProfile", "Loading Profile...");
            profileButton.getStyleClass().add("selected");

        } else if (event.getSource() == mymatchesButton) {
            PageLoader.loadPageWithAnimation(container, "com/bytebender/premnoybiye/mymatches", "Loading Matches...");
            mymatchesButton.getStyleClass().add("selected");

        } else if (event.getSource() == messageButton) {
            PageLoader.loadPageWithAnimation(container, "com/bytebender/premnoybiye/chatUI", "Loading Messages...");
            messageButton.getStyleClass().add("selected");

        } else if (event.getSource() == logoutButton) {
            App.setRoot("loginsignupchoice");

        }

    }

    private void clearAllSelectedStates() {
        discoverButton.getStyleClass().remove("selected");
        profileButton.getStyleClass().remove("selected");
        mymatchesButton.getStyleClass().remove("selected");
        messageButton.getStyleClass().remove("selected");
    }

    @FXML
    void sidebarCollapse(MouseEvent event) {
        // Calling toggling method of the sidebar Component
        flag = component.toggleSidebarState(sidebarCollapse, sidebarProfileBox, discoverLabel, matchesLabel,
                profileLabel, msgLabel, logoutLabel, Sidebar, flag);
    }

    public void initialize() throws IOException {
        PageLoader.loadPageWithAnimation(container, "com/bytebender/premnoybiye/discover", "Loading Discover...");

        clearAllSelectedStates();
        discoverButton.getStyleClass().add("selected");

        try {
            if (AuthController.CurrentUser != null) {
                if (AuthController.CurrentUser.getName() != "") {
                    sidebarUserName.setText(AuthController.CurrentUser.getName());
                }
                if (AuthController.CurrentUser.getImage() != "") {
                    // component.setImage(user.getImage(), demoProfileImg, 84, 84, false, 20);
                    component.setImage(AuthController.CurrentUser.getImage(), sidebarProfileImage, 40, 40, true, 20);
                    component.setImage(AuthController.CurrentUser.getImage(), sidebarBlurImg, 40, 40, false, 20);

                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(84, 84);
                    clip.setArcWidth(20);
                    clip.setArcHeight(20);
                    imgStack.setClip(clip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
