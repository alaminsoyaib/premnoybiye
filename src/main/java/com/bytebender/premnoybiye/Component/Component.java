package com.bytebender.premnoybiye.Component;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import com.bytebender.premnoybiye.App;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Component {

    public void setImage(String imagePath, ImageView element, double width, double height, boolean preserveRatio,
            int arcSize) {
        element.setImage(new Image(imagePath));
        element.setFitWidth(width);
        element.setFitHeight(height);
        element.setPreserveRatio(preserveRatio);

        if (arcSize > 0) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
            clip.setArcWidth(arcSize);
            clip.setArcHeight(arcSize);
            element.setClip(clip);
        }
    }

    public VBox createLoadingState(String message) {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        indicator.setPrefSize(50, 50);

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        VBox container = new VBox(15, indicator, label);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-padding: 30px;");
        return container;
    }

    public VBox createErrorState(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #ea0000; -fx-alignment: center; -fx-padding: 30px;");
        VBox container = new VBox(label);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    public String calculateAge(String dob) {
        if (dob == null || dob.isEmpty())
            return "N/A";
        try {
            LocalDate birthDate = LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return String.valueOf(Period.between(birthDate, LocalDate.now()).getYears());
        } catch (Exception e) {
            return "N/A";
        }
    }

    public void setImageWithClip(String imagePath, ImageView imageView, ImageView blurView, StackPane stackPane,
            double width, double height) {
        if (imagePath != null && !imagePath.isEmpty()) {
            setImage(imagePath, imageView, width, height, true, 0);
            if (blurView != null)
                setImage(imagePath, blurView, width, height, false, 0);
        } else {
            imageView.setImage(
                    new Image(getClass().getResourceAsStream("/com/bytebender/premnoybiye/img/icon/card-img.png")));
        }

        if (stackPane != null) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            stackPane.setClip(clip);
        }
    }

    public void toggleVisibility(boolean visible, javafx.scene.Node... nodes) {
        for (javafx.scene.Node node : nodes) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

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
