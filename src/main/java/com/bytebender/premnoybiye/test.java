package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.Component.sidebarToggle;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class test {
    int flag = 0;

    private sidebarToggle sidebarComponent = new sidebarToggle();

    @FXML
    private Label demoLabel;
    @FXML
    private ImageView sidebarCollapse;
    @FXML
    private HBox demosidebarProfile;
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

    public void initialize() {
        // Access the user info
        userInfo user = StepperController.currentUser;
        if (user != null) {
            demoLabel.setText("Date of Birth: " + user.getDob() + "\nGender: " + user.getGender()
                    + "\nReligion: " + user.getReligion() + "\nCity: " + user.getCity()
                    + "\nEducation: " + user.getEducation() + "\nProfession: " + user.getProfession()
                    + "\nMonthly Income: " + user.getIncome() + "\nAbout You: " + user.getBio()
                    + "\nPreferred Age: " + user.getPrefAge() + "\nPreferred Location: "
                    + user.getPrefLocation() + "\nPreferred Profession: " + user.getPrefProfession());
        }
    }

    @FXML
    void sideCollapse(MouseEvent event) {
        // Calling toggling method of the sidebar component
        flag = sidebarComponent.toggleSidebarState(sidebarCollapse, demosidebarProfile, discoverLabel, matchesLabel,
                profileLabel, msgLabel, logoutLabel, Sidebar, flag);
    }
}
