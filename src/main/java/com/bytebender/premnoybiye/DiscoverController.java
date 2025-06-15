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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
    private boolean isDataLoaded = false; // Flag to check if data is already loaded

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

    // Loading overlay elements
    @FXML
    private StackPane cardContainer;
    @FXML
    private VBox cardLoadingOverlay;
    @FXML
    private ProgressIndicator cardLoadingSpinner;
    @FXML
    private Label cardLoadingText;

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
    private Label discoverIncome;

    @FXML
    public void initialize() {
        // Initially hide back button and user details, show button holder
        hideUserDetails();
        // showButtonHolder();

        // Load available users asynchronously if not already loaded
        if (!isDataLoaded) {
            showLoadingState();
            loadAvailableUsersAsync();
        } else {
            showNextUser();
        }
    }

    private void showLoadingState() {
        // Show loading indicator on the card
        cardName.setText("Loading...");
        cardBio.setText("");
        cardLocation.setText("");

        hideButtonHolder();

        // You could also add a ProgressIndicator here if needed
        if (cardImg != null) {
            cardImg.setImage(null); // Clear existing image
        }
    }

    private void loadAvailableUsersAsync() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadAvailableUsers();
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isDataLoaded = true;
                showNextUser();
            });
        });

        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showErrorState();
            });
        });

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void showErrorState() {
        cardName.setText("Error loading users");
        cardBio.setText("Please try again later");
        cardLocation.setText("");
        hideButtonHolder();
        if (cardImg != null) {
            cardImg.setImage(null); // Clear existing image
        }
    }

    private void loadAvailableUsers() {
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
                availableUsers = firebaseConnection.getUsersByGender(desiredGender);
                // Remove current user if somehow included
                availableUsers.removeIf(user -> user.getUserId().equals(AuthController.CurrentUser.getUserId()));
                // Shuffle for random order
                Collections.shuffle(availableUsers);

                System.out.println("Loaded " + availableUsers.size() + " available users for discover");
            }
        }
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

            showButtonHolder();
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
        cardBio.setText("Sabr until you find your soulmate");
        cardLocation.setText("N/A");
        discoverBio.setText("Sabr until you find your soulmate");
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

    // Methods to handle card loading state
    private void showCardLoadingState(String message) {
        if (cardLoadingOverlay != null && cardLoadingText != null) {
            cardLoadingText.setText(message);
            cardLoadingOverlay.setVisible(true);
            cardLoadingOverlay.setManaged(true);
        }

        // Hide button holder during loading
        hideButtonHolder();
    }

    private void hideCardLoadingState() {
        if (cardLoadingOverlay != null) {
            cardLoadingOverlay.setVisible(false);
            cardLoadingOverlay.setManaged(false);
        }

        // Show button holder after loading
        showButtonHolder();
    }

    @FXML
    void handleReject(MouseEvent event) {
        if (currentDisplayUser != null && AuthController.CurrentUser != null) {
            // Show loading state
            showCardLoadingState("Rejecting...");

            // Create background task for rejection
            Task<Boolean> rejectTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    // Add to rejected users in database
                    return firebaseConnection.addToRejectedUsers(
                            AuthController.CurrentUser.getUserId(),
                            currentDisplayUser.getUserId());
                }
            };

            rejectTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    Boolean success = rejectTask.getValue();
                    if (success) {
                        // Update local CurrentUser object
                        AuthController.CurrentUser.addRejectedUser(currentDisplayUser.getUserId());

                        // Hide loading state
                        hideCardLoadingState();

                        // Show next user
                        showNextUser();

                        System.out.println("Rejected user: " + currentDisplayUser.getName());
                    } else {
                        // Hide loading state
                        hideCardLoadingState();
                        System.err.println("Failed to save rejection to database");
                    }
                });
            });

            rejectTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    // Hide loading state
                    hideCardLoadingState();
                    System.err.println("Error during rejection: " + rejectTask.getException().getMessage());
                });
            });

            Thread rejectThread = new Thread(rejectTask);
            rejectThread.setDaemon(true);
            rejectThread.start();
        }
    }

    @FXML
    void handleLove(MouseEvent event) {
        if (currentDisplayUser != null && AuthController.CurrentUser != null) {
            // Show loading state
            showCardLoadingState("Liking...");

            // Create background task for liking
            Task<Boolean> loveTask = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    // Add to liked users in database
                    boolean success = firebaseConnection.addToLikedUsers(
                            AuthController.CurrentUser.getUserId(),
                            currentDisplayUser.getUserId());

                    return success;
                }
            };

            loveTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    Boolean success = loveTask.getValue();
                    if (success) {
                        // Update local CurrentUser object
                        AuthController.CurrentUser.addLikedUser(currentDisplayUser.getUserId());

                        // Check if this creates a match (run in background)
                        Task<Boolean> matchTask = new Task<Boolean>() {
                            @Override
                            protected Boolean call() throws Exception {
                                return firebaseConnection.checkAndCreateMatch(
                                        AuthController.CurrentUser.getUserId(),
                                        currentDisplayUser.getUserId());
                            }
                        };

                        matchTask.setOnSucceeded(matchEvent -> {
                            Platform.runLater(() -> {
                                Boolean isMatch = matchTask.getValue();
                                if (isMatch) {
                                    AuthController.CurrentUser.addMatchedUser(currentDisplayUser.getUserId());
                                    System.out.println("🎉 It's a match with: " + currentDisplayUser.getName());
                                }

                                // Hide loading state
                                hideCardLoadingState();

                                // Show next user
                                showNextUser();

                                System.out.println("Liked user: " + currentDisplayUser.getName());
                                System.out.println(
                                        "Total liked users: " + AuthController.CurrentUser.getLikedUsers().size());
                            });
                        });

                        matchTask.setOnFailed(matchEvent -> {
                            Platform.runLater(() -> {
                                // Hide loading state even if match check fails
                                hideCardLoadingState();

                                // Show next user anyway
                                showNextUser();

                                System.out.println("Liked user: " + currentDisplayUser.getName());
                                System.out.println(
                                        "Total liked users: " + AuthController.CurrentUser.getLikedUsers().size());
                                System.err.println("Failed to check match status, but like was saved");
                            });
                        });

                        Thread matchThread = new Thread(matchTask);
                        matchThread.setDaemon(true);
                        matchThread.start();
                    } else {
                        // Hide loading state
                        hideCardLoadingState();
                        System.err.println("Failed to save like to database");
                    }
                });
            });

            loveTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    // Hide loading state
                    hideCardLoadingState();
                    System.err.println("Error during liking: " + loveTask.getException().getMessage());
                });
            });

            Thread loveThread = new Thread(loveTask);
            loveThread.setDaemon(true);
            loveThread.start();
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
