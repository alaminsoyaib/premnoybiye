package com.bytebender.premnoybiye;

import com.bytebender.premnoybiye.DBConnection.userInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class test {
    int flag = 0;

    @FXML
    private Label demoLabel;
    @FXML
    private ImageView sidebarCollapse;
    @FXML
    private HBox demosidebarProfile;
    @FXML
    private Text logoutLabel;

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
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/open-drawer-icon.png")));
            demosidebarProfile.setVisible(false);
            demosidebarProfile.setManaged(false);
            logoutLabel.setVisible(false);
            logoutLabel.setManaged(false);
            Sidebar.setPrefWidth(72);
            flag = 1;
        } else if (flag == 1) {
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/close-drawer-icon.png")));
            demosidebarProfile.setVisible(true);
            demosidebarProfile.setManaged(true);
            logoutLabel.setVisible(true);
            logoutLabel.setManaged(true);
            Sidebar.setPrefWidth(244);
            flag = 0;
        }

    }
}
