package com.bytebender.premnoybiye.Component;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.bytebender.premnoybiye.App;

/**
 * Utility class for creating dialogs and alerts with consistent branding
 * All dialogs created through this class will have the application icon
 */
public class DialogUtils {

    /**
     * Sets the application icon for any dialog or window
     * 
     * @param dialog The dialog to set the icon for
     */
    public static void setDialogIcon(Dialog<?> dialog) {
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            Image icon = new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/Main-Logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Warning: Could not set dialog icon: " + e.getMessage());
            // Continue without icon rather than failing
        }
    }

    /**
     * Creates an Alert with the application icon
     * 
     * @param alertType   The type of alert
     * @param title       The alert title
     * @param headerText  The header text (can be null)
     * @param contentText The content text
     * @return Alert with application icon set
     */
    public static Alert createAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Set icon when the alert is shown
        alert.setOnShowing(e -> setDialogIcon(alert));

        return alert;
    }

    /**
     * Creates an information alert with application icon
     * 
     * @param title   The alert title
     * @param message The alert message
     * @return Alert with application icon set
     */
    public static Alert createInfoAlert(String title, String message) {
        return createAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    /**
     * Creates an error alert with application icon
     * 
     * @param title   The alert title
     * @param message The error message
     * @return Alert with application icon set
     */
    public static Alert createErrorAlert(String title, String message) {
        return createAlert(Alert.AlertType.ERROR, title, null, message);
    }

    /**
     * Creates a warning alert with application icon
     * 
     * @param title   The alert title
     * @param message The warning message
     * @return Alert with application icon set
     */
    public static Alert createWarningAlert(String title, String message) {
        return createAlert(Alert.AlertType.WARNING, title, null, message);
    }

    /**
     * Creates a confirmation alert with application icon
     * 
     * @param title   The alert title
     * @param message The confirmation message
     * @return Alert with application icon set
     */
    public static Alert createConfirmationAlert(String title, String message) {
        return createAlert(Alert.AlertType.CONFIRMATION, title, null, message);
    }

    /**
     * Shows an information alert and waits for user response
     * 
     * @param title   The alert title
     * @param message The alert message
     */
    public static void showInfoAlert(String title, String message) {
        Alert alert = createInfoAlert(title, message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert and waits for user response
     * 
     * @param title   The alert title
     * @param message The error message
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = createErrorAlert(title, message);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert and waits for user response
     * 
     * @param title   The alert title
     * @param message The warning message
     */
    public static void showWarningAlert(String title, String message) {
        Alert alert = createWarningAlert(title, message);
        alert.showAndWait();
    }
}
