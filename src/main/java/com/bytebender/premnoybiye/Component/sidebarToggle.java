package com.bytebender.premnoybiye.Component;

import com.bytebender.premnoybiye.App;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class sidebarToggle {
    public int toggleSidebarState(ImageView sidebarCollapse, HBox demosidebarProfile, Text logoutLabel, VBox Sidebar,
            int currentFlag) {
        if (currentFlag == 0) {
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/open-drawer-icon.png")));
            demosidebarProfile.setVisible(false);
            demosidebarProfile.setManaged(false);
            logoutLabel.setVisible(false);
            logoutLabel.setManaged(false);
            Sidebar.setPrefWidth(72);
            return 1;
        } else {
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/close-drawer-icon.png")));
            demosidebarProfile.setVisible(true);
            demosidebarProfile.setManaged(true);
            logoutLabel.setVisible(true);
            logoutLabel.setManaged(true);
            Sidebar.setPrefWidth(244);
            return 0;
        }
    }
}
