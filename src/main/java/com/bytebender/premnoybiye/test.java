package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.DBConnection.userInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class test {
    int flag = 0;

    @FXML
    private Label demoLabel;
    @FXML
    private VBox sidebarCollapse;
    @FXML
    private HBox demosidebarProfile;
    @FXML
    private HBox demosidebarLogout;
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

        if (flag == 0) {
            // demosidebarProfile.visibleProperty().setValue(false);
            demosidebarProfile.setVisible(false);
            demosidebarProfile.setManaged(false);
            demosidebarLogout.setVisible(false);
            demosidebarLogout.setManaged(false);
            // Sidebar.setPrefWidth(flag == 0 ? 60 : 200);
            Sidebar.setPrefWidth(72);
            flag = 1;
        } else if (flag == 1) {
            demosidebarProfile.setVisible(true);
            demosidebarProfile.setManaged(true);
            demosidebarLogout.setVisible(true);
            demosidebarLogout.setManaged(true);
            Sidebar.setPrefWidth(244);
            flag = 0;
        }

    }
}
