package com.bytebender.premnoybiye;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.Component.Component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MyMatchesController {

    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    private Component component = new Component();
    private List<userInfo> matchedUsersList = new ArrayList<>();

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane cardGrid;

    @FXML
    public void initialize() {
        loadMatchedUsers();
        displayMatchedUsers();
    }

    private void loadMatchedUsers() {
        if (AuthController.CurrentUser != null && AuthController.CurrentUser.getMatchedUsers() != null) {
            matchedUsersList.clear();

            // Get the list of matched user IDs
            List<String> matchedUserIds = AuthController.CurrentUser.getMatchedUsers();

            // Fetch user details for each matched user ID
            for (String userId : matchedUserIds) {
                userInfo matchedUser = firebaseConnection.getUserById(userId);
                if (matchedUser != null) {
                    matchedUsersList.add(matchedUser);
                }
            }

            System.out.println("Loaded " + matchedUsersList.size() + " matched users");
        }
    }

    private void displayMatchedUsers() {
        // Clear existing content
        scrollPane.setClip(null);
        cardGrid.setClip(null);

        cardGrid.getChildren().clear();
        cardGrid.getColumnConstraints().clear();
        cardGrid.getRowConstraints().clear();

        if (matchedUsersList.isEmpty()) {
            // Show message when no matches
            Label noMatchesLabel = new Label("No matches yet! Keep discovering to find your perfect match.");
            noMatchesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-alignment: center;");
            cardGrid.add(noMatchesLabel, 0, 0);
            return;
        }

        // Set up grid columns (3 columns)
        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.33);
            cardGrid.getColumnConstraints().add(column);
        }

        // Add matched users to grid (3 per row)
        int row = 0;
        int col = 0;

        for (userInfo user : matchedUsersList) {
            try {
                // Load card.fxml for each user
                FXMLLoader loader = new FXMLLoader(getClass().getResource("card.fxml"));
                VBox cardNode = loader.load();

                // Get the card elements and populate with user data
                populateCard(cardNode, user);

                // Add to grid
                cardGrid.add(cardNode, col, row);

                // Move to next position
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }

            } catch (IOException e) {
                System.err.println("Error loading card for user: " + user.getName());
                e.printStackTrace();
            }
        }
    }

    private void populateCard(VBox cardNode, userInfo user) {
        try {
            // Find the card elements by fx:id
            ImageView cardImg = (ImageView) cardNode.lookup("#cardImg");
            ImageView blurImg = (ImageView) cardNode.lookup("#blurImg");
            javafx.scene.layout.StackPane imgStack = (javafx.scene.layout.StackPane) cardNode.lookup("#imgStack");
            Label cardName = (Label) cardNode.lookup("#cardName");
            Label cardBio = (Label) cardNode.lookup("#cardBio");
            Label cardLocation = (Label) cardNode.lookup("#cardLocation");
            Label cardAge = (Label) cardNode.lookup("#cardAge");

            // Populate the card with user data
            if (cardName != null) {
                cardName.setText(user.getName());
            }

            if (cardBio != null) {
                cardBio.setText(user.getBio());
            }

            if (cardLocation != null) {
                cardLocation.setText(user.getCity());
            }

            if (cardAge != null) {
                String age = calculateAge(user.getDob());
                cardAge.setText(age);
            }

            // Load user image
            if (cardImg != null) {
                if (user.getImage() != null && !user.getImage().isEmpty()) {
                    component.setImage(user.getImage(), cardImg, 200, 298, true, 0);
                    component.setImage(user.getImage(), blurImg, 200, 298, false, 0);
                } else {
                    // Set default image if no image available
                    cardImg.setImage(
                            new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/card-img.png")));
                }

                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(200, 298);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                imgStack.setClip(clip);
            }

            // Add click handler to open user profile modal
            cardNode.setOnMouseClicked(event -> {
                try {
                    showUserProfileModal(user);
                } catch (Exception e) {
                    System.err.println("Error opening user profile: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Add hover effect
            cardNode.setOnMouseEntered(event -> {
                cardNode.setStyle("-fx-cursor: hand;");
            });

            cardNode.setOnMouseExited(event -> {
                cardNode.setStyle("-fx-cursor: default;");
            });

        } catch (Exception e) {
            System.err.println("Error populating card for user: " + user.getName());
            e.printStackTrace();
        }
    }

    private String calculateAge(String dob) {
        try {
            if (dob != null && !dob.isEmpty()) {
                LocalDate birthDate = LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate currentDate = LocalDate.now();
                Period period = Period.between(birthDate, currentDate);
                return String.valueOf(period.getYears());
            }
        } catch (Exception e) {
            System.err.println("Error calculating age: " + e.getMessage());
        }
        return "N/A";
    }

    // Method to refresh the matches display (can be called when returning to this
    // page)
    public void refreshMatches() {
        loadMatchedUsers();
        displayMatchedUsers();
    }

    /**
     * Show user profile in a modal dialog
     */
    private void showUserProfileModal(userInfo user) throws IOException {
        try {
            // Load the user profile FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("userprofile.fxml"));
            Parent modalRoot = loader.load();

            // Get the controller and set the user data
            UserProfileController modalController = loader.getController();
            modalController.setDisplayUser(user);

            // Create a new stage for the modal
            Stage modalStage = new Stage();
            modalController.setModalStage(modalStage);

            modalStage.setTitle(user.getName() + "'s Profile");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);

            // Show the modal and wait for it to close
            modalStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error loading user profile modal: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
