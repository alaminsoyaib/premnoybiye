package com.bytebender.premnoybiye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.Component.Component;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
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
    private boolean isDataLoaded = false;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private GridPane cardGrid;

    @FXML
    public void initialize() {
        showLoadingState();
        if (!isDataLoaded) {
            loadMatchedUsersAsync();
        } else {
            displayMatchedUsers();
        }
    }

    private void showLoadingState() {
        cardGrid.getChildren().clear();
        cardGrid.add(component.createLoadingState("Loading your matches..."), 0, 0);
    }

    private void loadMatchedUsersAsync() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadMatchedUsers();
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            isDataLoaded = true;
            displayMatchedUsers();
        }));

        loadTask.setOnFailed(e -> Platform.runLater(this::showErrorState));

        new Thread(loadTask) {
            {
                setDaemon(true);
            }
        }.start();
    }

    private void showErrorState() {
        cardGrid.getChildren().clear();
        cardGrid.add(component.createErrorState("Failed to load matches. Please try again later."), 0, 0);
    }

    private void loadMatchedUsers() {
        if (AuthController.CurrentUser != null && AuthController.CurrentUser.getMatchedUsers() != null) {
            matchedUsersList.clear();
            List<String> matchedUserIds = AuthController.CurrentUser.getMatchedUsers();

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
        clearGrid();

        if (matchedUsersList.isEmpty()) {
            showNoMatchesMessage();
            return;
        }

        setupGridColumns();
        addUsersToGrid();
    }

    private void clearGrid() {
        scrollPane.setClip(null);
        cardGrid.setClip(null);
        cardGrid.getChildren().clear();
        cardGrid.getColumnConstraints().clear();
        cardGrid.getRowConstraints().clear();
    }

    private void showNoMatchesMessage() {
        Label noMatchesLabel = new Label("No matches yet! Keep discovering to find your perfect match.");
        noMatchesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-alignment: center;");
        cardGrid.add(noMatchesLabel, 0, 0);
    }

    private void setupGridColumns() {
        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.33);
            cardGrid.getColumnConstraints().add(column);
        }
    }

    private void addUsersToGrid() {
        int row = 0, col = 0;

        for (userInfo user : matchedUsersList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("card.fxml"));
                VBox cardNode = loader.load();
                populateCard(cardNode, user);
                cardGrid.add(cardNode, col, row);

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
            ImageView cardImg = (ImageView) cardNode.lookup("#cardImg");
            ImageView blurImg = (ImageView) cardNode.lookup("#blurImg");
            javafx.scene.layout.StackPane imgStack = (javafx.scene.layout.StackPane) cardNode.lookup("#imgStack");
            Label cardName = (Label) cardNode.lookup("#cardName");
            Label cardBio = (Label) cardNode.lookup("#cardBio");
            Label cardLocation = (Label) cardNode.lookup("#cardLocation");
            Label cardAge = (Label) cardNode.lookup("#cardAge");

            setLabelText(cardName, user.getName());
            setLabelText(cardBio, user.getBio());
            setLabelText(cardLocation, user.getCity());
            setLabelText(cardAge, component.calculateAge(user.getDob()));

            if (cardImg != null) {
                component.setImageWithClip(user.getImage(), cardImg, blurImg, imgStack, 200, 298);
            }

            setupCardInteraction(cardNode, user);
        } catch (Exception e) {
            System.err.println("Error populating card for user: " + user.getName());
            e.printStackTrace();
        }
    }

    private void setLabelText(Label label, String text) {
        if (label != null)
            label.setText(text);
    }

    private void setupCardInteraction(VBox cardNode, userInfo user) {
        cardNode.setOnMouseClicked(event -> {
            try {
                showUserProfileModal(user);
            } catch (Exception e) {
                System.err.println("Error opening user profile: " + e.getMessage());
                e.printStackTrace();
            }
        });

        cardNode.setOnMouseEntered(event -> cardNode.setStyle("-fx-cursor: hand;"));
        cardNode.setOnMouseExited(event -> cardNode.setStyle("-fx-cursor: default;"));
    }

    private void showUserProfileModal(userInfo user) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("userprofile.fxml"));
            Parent modalRoot = loader.load();

            UserProfileController modalController = loader.getController();
            modalController.setDisplayUser(user);

            Stage modalStage = new Stage();
            modalController.setModalStage(modalStage);

            modalStage.setTitle(user.getName() + "'s Profile");
            modalStage.getIcons()
                    .add(new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/Main-Logo.png")));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error loading user profile modal: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
