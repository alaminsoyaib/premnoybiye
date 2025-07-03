package com.bytebender.premnoybiye.Component;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.bytebender.premnoybiye.App;

public class DialogUtils {
    public static void setDialogIcon(Dialog<?> dialog) {
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            Image icon = new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/Main-Logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Warning: Could not set dialog icon: " + e.getMessage());
        }
    }

    public static Alert createAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.setOnShowing(e -> setDialogIcon(alert));

        return alert;
    }

    public static Alert createInfoAlert(String title, String message) {
        return createAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    public static Alert createErrorAlert(String title, String message) {
        return createAlert(Alert.AlertType.ERROR, title, null, message);
    }

    public static Alert createWarningAlert(String title, String message) {
        return createAlert(Alert.AlertType.WARNING, title, null, message);
    }

    public static Alert createConfirmationAlert(String title, String message) {
        return createAlert(Alert.AlertType.CONFIRMATION, title, null, message);
    }

    public static void showInfoAlert(String title, String message) {
        Alert alert = createInfoAlert(title, message);
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String message) {
        Alert alert = createErrorAlert(title, message);
        alert.showAndWait();
    }

    public static void showWarningAlert(String title, String message) {
        Alert alert = createWarningAlert(title, message);
        alert.showAndWait();
    }
}
