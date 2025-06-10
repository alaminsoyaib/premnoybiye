package com.bytebender.premnoybiye.Component;

import javafx.scene.control.Label;
import com.bytebender.premnoybiye.App;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class sidebarToggle {
    public int toggleSidebarState(ImageView sidebarCollapse, HBox demosidebarProfile, Label discoverLabel,
            Label matchesLabel, Label profileLabel, Label msgLabel, Label logoutLabel, VBox Sidebar,
            int currentFlag) {
        if (currentFlag == 0) {
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/open-drawer-icon.png")));
            demosidebarProfile.setVisible(false);
            demosidebarProfile.setManaged(false);

            discoverLabel.setVisible(false);
            discoverLabel.setManaged(false);
            matchesLabel.setVisible(false);
            matchesLabel.setManaged(false);
            profileLabel.setVisible(false);
            profileLabel.setManaged(false);
            msgLabel.setVisible(false);
            msgLabel.setManaged(false);
            logoutLabel.setVisible(false);
            logoutLabel.setManaged(false);
            Sidebar.setPrefWidth(72);
            return 1;
        } else {
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/close-drawer-icon.png")));
            demosidebarProfile.setVisible(true);
            demosidebarProfile.setManaged(true);

            discoverLabel.setVisible(true);
            discoverLabel.setManaged(true);
            matchesLabel.setVisible(true);
            matchesLabel.setManaged(true);
            profileLabel.setVisible(true);
            profileLabel.setManaged(true);
            msgLabel.setVisible(true);
            msgLabel.setManaged(true);
            logoutLabel.setVisible(true);
            logoutLabel.setManaged(true);
            Sidebar.setPrefWidth(244);
            return 0;
        }
    }
}
