package com.bytebender.premnoybiye;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import com.bytebender.premnoybiye.Component.Component;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DiscoverController {
    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    private Component component = new Component();
    private List<userInfo> availableUsers = new ArrayList<>();
    private userInfo currentDisplayUser;
    private boolean isDataLoaded = false;

    @FXML
    private VBox buttonHolder, card, discoverUserDetails, cardLoadingOverlay;
    @FXML
    private HBox discoverBackButton;
    @FXML
    private ImageView cardImg, blurImg, cardReject, cardLove, cardInfo;
    @FXML
    private StackPane imgStack, cardContainer;
    @FXML
    private Label cardName, cardBio, cardLocation;
    @FXML
    private Label discoverBio, discoverAge, discoverGender, discoverReligion, discoverCity, discoverEducation,
            discoverProfession, discoverIncome, cardLoadingText;

    @FXML
    public void initialize() {
        component.toggleVisibility(false, discoverBackButton, discoverUserDetails);
        if (!isDataLoaded) {
            showLoadingState();
            loadAvailableUsersAsync();
        } else {
            showNextUser();
        }
    }

    private void showLoadingState() {
        updateCardLabels("Loading...", "", "");
        component.toggleVisibility(false, buttonHolder);
        if (cardImg != null)
            cardImg.setImage(null);
    }

    private void loadAvailableUsersAsync() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadAvailableUsers();
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            isDataLoaded = true;
            showNextUser();
        }));

        loadTask.setOnFailed(e -> Platform.runLater(this::showErrorState));

        new Thread(loadTask) {
            {
                setDaemon(true);
            }
        }.start();
    }

    private void showErrorState() {
        updateCardLabels("Error loading users", "Please try again later", "");
        component.toggleVisibility(false, buttonHolder);
        if (cardImg != null)
            cardImg.setImage(null);
    }

    private void loadAvailableUsers() {
        if (AuthController.CurrentUser != null) {
            String currentUserGender = AuthController.CurrentUser.getGender();
            String desiredGender = "male".equalsIgnoreCase(currentUserGender) ? "female"
                    : "female".equalsIgnoreCase(currentUserGender) ? "male" : "";

            if (!desiredGender.isEmpty()) {
                availableUsers = firebaseConnection.getUsersByGender(desiredGender);
                availableUsers.removeIf(user -> user.getUserId().equals(AuthController.CurrentUser.getUserId()));
                Collections.shuffle(availableUsers);
                System.out.println("Loaded " + availableUsers.size() + " available users for discover");
            }
        }
    }

    private void showNextUser() {
        userInfo nextUser = availableUsers.stream()
                .filter(user -> AuthController.CurrentUser != null
                        && !AuthController.CurrentUser.hasSeenUser(user.getUserId()))
                .findFirst().orElse(null);

        if (nextUser != null) {
            currentDisplayUser = nextUser;
            updateCardDisplay(nextUser);
        } else {
            showNoMoreUsersMessage();
        }
    }

    private void updateCardDisplay(userInfo user) {
        if (user == null)
            return;

        updateCardLabels(user.getName(), user.getBio(), user.getCity());
        updateDetailLabels(user);

        component.setImageWithClipCached(user, cardImg, blurImg, imgStack, 258, 358);
        component.toggleVisibility(true, buttonHolder);
    }

    private void updateCardLabels(String name, String bio, String location) {
        if (cardName != null)
            cardName.setText(name);
        if (cardBio != null)
            cardBio.setText(bio);
        if (cardLocation != null)
            cardLocation.setText(location);
    }

    private void updateDetailLabels(userInfo user) {
        if (discoverBio != null)
            discoverBio.setText(user.getBio());
        if (discoverGender != null)
            discoverGender.setText(user.getGender());
        if (discoverReligion != null)
            discoverReligion.setText(user.getReligion());
        if (discoverCity != null)
            discoverCity.setText(user.getCity());
        if (discoverEducation != null)
            discoverEducation.setText(user.getEducation());
        if (discoverProfession != null)
            discoverProfession.setText(user.getProfession());
        if (discoverIncome != null)
            discoverIncome.setText(user.getIncome());
        if (discoverAge != null)
            discoverAge.setText(component.calculateAge(user.getDob()));
    }

    private void showNoMoreUsersMessage() {
        updateCardLabels("No More Users", "Sabr until you find your soulmate", "N/A");
        setAllDetailLabels("N/A");
        component.toggleVisibility(false, discoverBackButton, discoverUserDetails, buttonHolder);
        cardImg.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/card-img.png")));
    }

    private void setAllDetailLabels(String value) {
        if (discoverBio != null)
            discoverBio.setText(value.equals("N/A") ? "Sabr until you find your soulmate" : value);
        if (discoverAge != null)
            discoverAge.setText(value);
        if (discoverGender != null)
            discoverGender.setText(value);
        if (discoverReligion != null)
            discoverReligion.setText(value);
        if (discoverCity != null)
            discoverCity.setText(value);
        if (discoverEducation != null)
            discoverEducation.setText(value);
        if (discoverProfession != null)
            discoverProfession.setText(value);
        if (discoverIncome != null)
            discoverIncome.setText(value);
    }

    private void showCardLoadingState(String message) {
        if (cardLoadingOverlay != null && cardLoadingText != null) {
            cardLoadingText.setText(message);
            component.toggleVisibility(true, cardLoadingOverlay);
        }
        component.toggleVisibility(false, buttonHolder);
    }

    private void hideCardLoadingState() {
        if (cardLoadingOverlay != null)
            component.toggleVisibility(false, cardLoadingOverlay);
        component.toggleVisibility(true, buttonHolder);
    }

    @FXML
    void handleReject(MouseEvent event) {
        handleUserAction("Rejecting...", () -> firebaseConnection.addToRejectedUsers(
                AuthController.CurrentUser.getUserId(), currentDisplayUser.getUserId()),
                () -> AuthController.CurrentUser.addRejectedUser(currentDisplayUser.getUserId()),
                "Rejected user: " + currentDisplayUser.getName());
    }

    @FXML
    void handleLove(MouseEvent event) {
        handleUserAction("Liking...", () -> firebaseConnection.addToLikedUsers(
                AuthController.CurrentUser.getUserId(), currentDisplayUser.getUserId()),
                () -> {
                    AuthController.CurrentUser.addLikedUser(currentDisplayUser.getUserId());
                    checkForMatch();
                },
                "Liked user: " + currentDisplayUser.getName());
    }

    private void handleUserAction(String loadingMessage, java.util.concurrent.Callable<Boolean> action,
            Runnable onSuccess, String successMessage) {
        if (currentDisplayUser == null || AuthController.CurrentUser == null)
            return;

        showCardLoadingState(loadingMessage);

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return action.call();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            if (task.getValue()) {
                onSuccess.run();
                hideCardLoadingState();
                showNextUser();
                System.out.println(successMessage);
            } else {
                hideCardLoadingState();
                System.err.println("Failed to save action to database");
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            hideCardLoadingState();
            System.err.println("Error during action: " + task.getException().getMessage());
        }));

        new Thread(task) {
            {
                setDaemon(true);
            }
        }.start();
    }

    private void checkForMatch() {
        Task<Boolean> matchTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return firebaseConnection.checkAndCreateMatch(
                        AuthController.CurrentUser.getUserId(), currentDisplayUser.getUserId());
            }
        };

        matchTask.setOnSucceeded(e -> Platform.runLater(() -> {
            if (matchTask.getValue()) {
                AuthController.CurrentUser.addMatchedUser(currentDisplayUser.getUserId());
                System.out.println("🎉 It's a match with: " + currentDisplayUser.getName());
            }
        }));

        new Thread(matchTask) {
            {
                setDaemon(true);
            }
        }.start();
    }

    @FXML
    void handleInfo(MouseEvent event) {
        component.toggleVisibility(false, buttonHolder);
        component.toggleVisibility(true, discoverBackButton, discoverUserDetails);
    }

    @FXML
    void handleBack(MouseEvent event) {
        component.toggleVisibility(true, buttonHolder);
        component.toggleVisibility(false, discoverBackButton, discoverUserDetails);
    }

    public List<String> getLikedUserIds() {
        return AuthController.CurrentUser != null ? new ArrayList<>(AuthController.CurrentUser.getLikedUsers())
                : new ArrayList<>();
    }
}
