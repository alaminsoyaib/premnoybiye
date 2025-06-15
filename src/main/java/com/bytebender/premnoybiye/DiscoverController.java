package com.bytebender.premnoybiye;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import com.bytebender.premnoybiye.Component.Component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DiscoverController {

    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    private Component component = new Component(); // Available users to show
    private List<userInfo> availableUsers = new ArrayList<>();
    // Current user being displayed
    private userInfo currentDisplayUser;

    @FXML
    private VBox buttonHolder;
    @FXML
    private HBox discoverBackButton;
    @FXML
    private VBox card;
    @FXML
    private VBox discoverUserDetails;

    // Card elements
    @FXML
    private ImageView cardImg;
    @FXML
    private ImageView blurImg;

    @FXML
    private StackPane imgStack;

    @FXML
    private Label cardName;
    @FXML
    private Label cardBio;
    @FXML
    private Label cardLocation;

    // Button elements
    @FXML
    private ImageView cardReject;
    @FXML
    private ImageView cardLove;
    @FXML
    private ImageView cardInfo;

    // Detail elements
    @FXML
    private Label discoverBio;
    @FXML
    private Label discoverAge;
    @FXML
    private Label discoverGender;
    @FXML
    private Label discoverReligion;
    @FXML
    private Label discoverCity;
    @FXML
    private Label discoverEducation;
    @FXML
    private Label discoverProfession;
    @FXML
    private Label discoverIncome;    @FXML
    public void initialize() {
        // Initially hide back button and user details, show button holder
        hideUserDetails();
        showButtonHolder();

        // Show loading indicator while loading users
        showLoadingState();

        // Load available users asynchronously
        loadAvailableUsersAsync();
    }

    private void showLoadingState() {
        cardName.setText("Loading...");
        cardBio.setText("Finding amazing people for you!");
        cardLocation.setText("Please wait...");
        
        // Hide buttons while loading
        hideButtonHolder();
        
        // Set a default loading image
        try {
            cardImg.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/card-img.png")));
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
        }
    }

    private void loadAvailableUsersAsync() {
        if (AuthController.CurrentUser != null) {
            String currentUserGender = AuthController.CurrentUser.getGender();
            String desiredGender = "";

            // If current user is male, show females and vice versa
            if ("male".equalsIgnoreCase(currentUserGender)) {
                desiredGender = "female";
            } else if ("female".equalsIgnoreCase(currentUserGender)) {
                desiredGender = "male";
            }

            if (!desiredGender.isEmpty()) {
                // Use async method with callbacks
                firebaseConnection.getUsersByGenderAsync(
                    desiredGender,
                    this::onUsersLoaded,
                    this::onUsersLoadError
                );
            } else {
                showNoMoreUsersMessage();
            }
        } else {
            showNoMoreUsersMessage();
        }
    }
    
    private void onUsersLoaded(List<userInfo> users) {
        Platform.runLater(() -> {
            availableUsers = new ArrayList<>(users);
            // Remove current user if somehow included
            if (AuthController.CurrentUser != null) {
                availableUsers.removeIf(user -> user.getUserId().equals(AuthController.CurrentUser.getUserId()));
            }
            // Shuffle for random order
            Collections.shuffle(availableUsers);

            System.out.println("Loaded " + availableUsers.size() + " available users for discover");
            
            // Show first user or no users message
            showNextUser();
            showButtonHolder();
        });
    }
    
    private void onUsersLoadError(String error) {
        Platform.runLater(() -> {
            System.err.println("Error loading users: " + error);
            cardName.setText("Connection Error");
            cardBio.setText("Unable to load profiles. Please check your connection and try again.");
            cardLocation.setText("Tap to retry");
            hideButtonHolder();
        });
    }

    private void showNextUser() {
        // Find next unseen user
        userInfo nextUser = null;
        for (userInfo user : availableUsers) {
            if (AuthController.CurrentUser != null && !AuthController.CurrentUser.hasSeenUser(user.getUserId())) {
                nextUser = user;
                break;
            }
        }

        if (nextUser != null) {
            currentDisplayUser = nextUser;
            updateCardDisplay(nextUser);
        } else {
            // No more users to show
            showNoMoreUsersMessage();
        }
    }

    private void updateCardDisplay(userInfo user) {
        if (user != null) {
            // Update card elements
            cardName.setText(user.getName());
            cardBio.setText(user.getBio());
            cardLocation.setText(user.getCity());

            // Update detail elements
            discoverBio.setText(user.getBio());
            discoverGender.setText(user.getGender());
            discoverReligion.setText(user.getReligion());
            discoverCity.setText(user.getCity());
            discoverEducation.setText(user.getEducation());
            discoverProfession.setText(user.getProfession());
            discoverIncome.setText(user.getIncome());

            // Calculate and display age
            String age = calculateAge(user.getDob());
            discoverAge.setText(age);

            // Load user image
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                component.setImage(user.getImage(), cardImg, 258, 358, true, 0);
                component.setImage(user.getImage(), blurImg, 258, 358, false, 0);

                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(258, 358);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                imgStack.setClip(clip);
            } else {
                // Set default image if no image available
                cardImg.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/card-img.png")));
            }
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

    private void showNoMoreUsersMessage() {
        cardName.setText("No More Users");
        cardBio.setText("You've seen all available users. Check back later for new profiles!");
        cardLocation.setText("N/A");
        discoverBio.setText("You've seen all available users. Check back later for new profiles!");
        discoverAge.setText("N/A");
        discoverGender.setText("N/A");
        discoverReligion.setText("N/A");
        discoverCity.setText("N/A");
        discoverEducation.setText("N/A");
        discoverProfession.setText("N/A");
        discoverIncome.setText("N/A");

        hideUserDetails();
        hideButtonHolder();

        // Set default image
        cardImg.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/card-img.png")));
    }

    private void hideUserDetails() {
        discoverBackButton.setManaged(false);
        discoverBackButton.setVisible(false);

        discoverUserDetails.setManaged(false);
        discoverUserDetails.setVisible(false);
    }

    private void showUserDetails() {
        discoverBackButton.setManaged(true);
        discoverBackButton.setVisible(true);

        discoverUserDetails.setManaged(true);
        discoverUserDetails.setVisible(true);
    }

    private void hideButtonHolder() {
        buttonHolder.setManaged(false);
        buttonHolder.setVisible(false);
    }

    private void showButtonHolder() {
        buttonHolder.setManaged(true);
        buttonHolder.setVisible(true);
    }

    @FXML
    void handleReject(MouseEvent event) {
        if (currentDisplayUser != null && AuthController.CurrentUser != null) {
            // Add to rejected users in database
            boolean success = firebaseConnection.addToRejectedUsers(
                    AuthController.CurrentUser.getUserId(),
                    currentDisplayUser.getUserId());

            if (success) {
                // Update local CurrentUser object
                AuthController.CurrentUser.addRejectedUser(currentDisplayUser.getUserId());

                // Show next user
                showNextUser();

                System.out.println("Rejected user: " + currentDisplayUser.getName());
            } else {
                System.err.println("Failed to save rejection to database");
            }
        }
    }

    @FXML
    void handleLove(MouseEvent event) {
        if (currentDisplayUser != null && AuthController.CurrentUser != null) {
            // Add to liked users in database
            boolean success = firebaseConnection.addToLikedUsers(
                    AuthController.CurrentUser.getUserId(),
                    currentDisplayUser.getUserId());

            if (success) {
                // Update local CurrentUser object
                AuthController.CurrentUser.addLikedUser(currentDisplayUser.getUserId());

                // Check if this creates a match
                boolean isMatch = firebaseConnection.checkAndCreateMatch(
                        AuthController.CurrentUser.getUserId(),
                        currentDisplayUser.getUserId());

                if (isMatch) {
                    // Update local CurrentUser object with the match
                    AuthController.CurrentUser.addMatchedUser(currentDisplayUser.getUserId());
                    System.out.println("🎉 It's a match with: " + currentDisplayUser.getName());
                    // You could show a match notification here if desired
                }

                // Show next user
                showNextUser();

                System.out.println("Liked user: " + currentDisplayUser.getName());
                System.out.println("Total liked users: " + AuthController.CurrentUser.getLikedUsers().size());
            } else {
                System.err.println("Failed to save like to database");
            }
        }
    }

    @FXML
    void handleInfo(MouseEvent event) {
        // Hide button holder and show user details
        hideButtonHolder();
        showUserDetails();
    }

    @FXML
    void handleBack(MouseEvent event) {
        // Hide user details and show button holder
        hideUserDetails();
        showButtonHolder();
    } // Getter for liked users (can be used by other controllers)

    public List<String> getLikedUserIds() {
        if (AuthController.CurrentUser != null) {
            return new ArrayList<>(AuthController.CurrentUser.getLikedUsers());
        }
        return new ArrayList<>();
    }
}
