package com.bytebender.premnoybiye;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.bytebender.premnoybiye.DBConnection.FirebaseConnection;
import com.bytebender.premnoybiye.DBConnection.Message;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import com.bytebender.premnoybiye.Component.Component;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChatController {
    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    private Component component = new Component();
    private List<userInfo> matchedUsersList = new ArrayList<>();
    private userInfo selectedUser = null;
    private List<Message> currentConversation = new ArrayList<>();
    private Timeline messagePollingTimer;
    private int lastMessageCount = 0;
    private boolean isDataLoaded = false; // Flag to check if data is already loaded

    @FXML
    private VBox userlistHolder;
    @FXML
    private VBox conversationHolder;
    @FXML
    private TextField msgInput;
    @FXML
    private ImageView sendButton;
    @FXML
    private ImageView infoButton;
    @FXML
    private Text userName;
    @FXML
    private Text userLocationAge;

    @FXML
    public void initialize() {
        setupEventHandlers();
        showInitialState();

        // Load data asynchronously if not already loaded
        if (!isDataLoaded) {
            showLoadingUserList();
            loadMatchedUsersAsync();
        } else {
            displayUserList();
        }

        // Ensure polling is stopped when initializing
        stopMessagePolling();
    }

    private void showLoadingUserList() {
        userlistHolder.getChildren().clear();

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setProgress(-1);
        loadingIndicator.setPrefSize(40, 40);
        loadingIndicator.getStyleClass().add("loading-spinner");

        Label loadingLabel = new Label("Loading conversations...");
        loadingLabel.getStyleClass().add("loading-text");

        VBox loadingContainer = new VBox(10);
        loadingContainer.getChildren().addAll(loadingIndicator, loadingLabel);
        loadingContainer.setStyle("-fx-alignment: center; -fx-padding: 20px;");

        userlistHolder.getChildren().add(loadingContainer);
    }

    private void loadMatchedUsersAsync() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loadMatchedUsers();
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                isDataLoaded = true;
                displayUserList();
            });
        });

        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showErrorUserList();
            });
        });

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void showErrorUserList() {
        userlistHolder.getChildren().clear();
        Label errorLabel = new Label("Failed to load conversations");
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ea0000; -fx-alignment: center; -fx-padding: 20px;");
        userlistHolder.getChildren().add(errorLabel);
    }

    private void setupEventHandlers() {
        // Send button click
        sendButton.setOnMouseClicked(this::handleSendMessage);

        // Info button click
        infoButton.setOnMouseClicked(this::handleInfoButton);

        // Enter key in message input
        msgInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSendMessage(null);
            }
        });
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

            System.out.println("Loaded " + matchedUsersList.size() + " matched users for chat");
        }
    }

    private void displayUserList() {
        userlistHolder.getChildren().clear();

        if (matchedUsersList.isEmpty()) {
            showEmptyMatchesState();
            return;
        }

        for (userInfo user : matchedUsersList) {
            try {
                // Load userchatlist.fxml for each user
                FXMLLoader loader = new FXMLLoader(getClass().getResource("userchatlist.fxml"));
                HBox chatListItem = loader.load();

                // Populate the chat list item with user data
                populateChatListItem(chatListItem, user);

                // Add click handler
                chatListItem.setOnMouseClicked(event -> selectUser(user));

                userlistHolder.getChildren().add(chatListItem);

            } catch (IOException e) {
                System.err.println("Error loading chat list item for user: " + user.getName());
                e.printStackTrace();
            }
        }
    }

    private void populateChatListItem(HBox chatListItem, userInfo user) {
        try {
            // Find the elements by fx:id
            ImageView userImg = (ImageView) chatListItem.lookup("#userImg");
            ImageView blurImg = (ImageView) chatListItem.lookup("#blurImg");
            StackPane imgStack = (StackPane) chatListItem.lookup("#imgStack");
            Label userName = (Label) chatListItem.lookup("#userName");
            Label userLocationAge = (Label) chatListItem.lookup("#userLocationAge");

            // Populate with user data
            if (userName != null) {
                userName.setText(user.getName());
            }

            if (userLocationAge != null) {
                String age = calculateAge(user.getDob());
                userLocationAge.setText(user.getCity() + ", " + age);
            }

            // Load user image
            if (userImg != null && blurImg != null) {
                if (user.getImage() != null && !user.getImage().isEmpty()) {
                    component.setImage(user.getImage(), userImg, 48, 48, true, 24);
                    component.setImage(user.getImage(), blurImg, 48, 48, false, 24);
                } else {
                    // Set default image if no image available
                    userImg.setImage(
                            new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/profile-image.png")));
                    blurImg.setImage(
                            new javafx.scene.image.Image(getClass().getResourceAsStream("img/icon/profile-image.png")));
                }

                if (imgStack != null) {
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(48, 48);
                    clip.setArcWidth(24);
                    clip.setArcHeight(24);
                    imgStack.setClip(clip);
                }
            }

        } catch (Exception e) {
            System.err.println("Error populating chat list item for user: " + user.getName());
            e.printStackTrace();
        }
    }

    private void selectUser(userInfo user) {
        // Stop any existing polling
        stopMessagePolling();

        selectedUser = user;
        updateChatHeader();
        loadConversation();
        enableChatInput();
        startMessagePolling();
    }

    private void updateChatHeader() {
        if (selectedUser != null) {
            userName.setText(selectedUser.getName());
            String age = calculateAge(selectedUser.getDob());
            userLocationAge.setText(selectedUser.getCity() + ", " + age);
            infoButton.setVisible(true);
        }
    }

    private void loadConversation() {
        if (selectedUser == null)
            return;

        // Load messages between current user and selected user
        currentConversation = firebaseConnection.getConversation(
                AuthController.CurrentUser.getUserId(),
                selectedUser.getUserId());

        displayConversation();
    }

    private void displayConversation() {
        conversationHolder.getChildren().clear();

        if (currentConversation.isEmpty()) {
            showEmptyConversationState();
            return;
        }

        for (Message message : currentConversation) {
            VBox messageBox = createMessageBox(message);
            conversationHolder.getChildren().add(messageBox);
        }

        // Force layout refresh
        conversationHolder.autosize();
        conversationHolder.applyCss();
        conversationHolder.layout();

        // Scroll to bottom after loading messages
        Platform.runLater(this::scrollToBottom);
    }

    private VBox createMessageBox(Message message) {
        // Create main container for the entire message row
        HBox messageRow = new HBox();
        messageRow.setMaxWidth(Double.MAX_VALUE);
        messageRow.setFillHeight(false);

        // Create message content container
        VBox messageBox = new VBox();
        messageBox.setSpacing(4);
        messageBox.setMaxWidth(350); // Max width for individual message bubble
        messageBox.setMinWidth(80); // Min width to prevent too narrow bubbles

        // Create message content
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setPadding(new Insets(10, 14, 10, 14));
        contentLabel.setFont(Font.font("Trebuchet MS", 14));
        contentLabel.setMaxWidth(350); // Match messageBox max width
        contentLabel.setMinWidth(80); // Match messageBox min width
        contentLabel.setMinHeight(Label.USE_PREF_SIZE); // Use preferred height
        contentLabel.setMaxHeight(Double.MAX_VALUE); // Allow unlimited height
        contentLabel.setEllipsisString(""); // Remove ellipsis
        contentLabel.setTextOverrun(OverrunStyle.CLIP); // Don't use ellipsis

        // Set HBox grow properties to ensure proper width distribution
        HBox.setHgrow(contentLabel, Priority.ALWAYS);

        // Create time label
        Label timeLabel = new Label(message.getFormattedTime());
        timeLabel.setFont(Font.font("Trebuchet MS", 10));
        timeLabel.setStyle("-fx-text-fill: #666666;");

        boolean isMyMessage = message.getSenderId().equals(AuthController.CurrentUser.getUserId());

        if (isMyMessage) {
            // My message - align right, purple background
            messageRow.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            contentLabel.setStyle(
                    "-fx-background-color: #6631c4; -fx-text-fill: white; -fx-background-radius: 18 18 4 18; -fx-label-padding: 0; -fx-text-overrun: clip;");
            timeLabel.setStyle("-fx-text-fill: #666666;");
            timeLabel.setAlignment(Pos.CENTER_RIGHT);

            // Add margin to push message to the right and leave space on left
            HBox.setMargin(messageBox, new Insets(5, 16, 5, 80));
        } else {
            // Their message - align left, gray background
            messageRow.setAlignment(Pos.CENTER_LEFT);
            messageBox.setAlignment(Pos.CENTER_LEFT);
            contentLabel.setStyle(
                    "-fx-background-color: #E5E5EA; -fx-text-fill: #000000; -fx-background-radius: 18 18 18 4; -fx-label-padding: 0; -fx-text-overrun: clip;");
            timeLabel.setStyle("-fx-text-fill: #666666;");
            timeLabel.setAlignment(Pos.CENTER_LEFT);

            // Add margin to push message to the left and leave space on right
            HBox.setMargin(messageBox, new Insets(5, 80, 5, 16));
        }

        messageBox.getChildren().addAll(contentLabel, timeLabel);
        messageRow.getChildren().add(messageBox);

        // Create a VBox wrapper to return (since the method signature expects VBox)
        VBox wrapper = new VBox();
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.getChildren().add(messageRow);

        return wrapper;
    }

    private void handleSendMessage(MouseEvent event) {
        String messageText = msgInput.getText().trim();
        if (messageText.isEmpty() || selectedUser == null) {
            return;
        }

        // Create and send message
        Message message = new Message(
                AuthController.CurrentUser.getUserId(),
                selectedUser.getUserId(),
                messageText);
        boolean success = firebaseConnection.sendMessage(message);
        if (success) {
            // Add to current conversation and display
            currentConversation.add(message);
            lastMessageCount = currentConversation.size(); // Update message count

            // Create and add the message box
            VBox messageBox = createMessageBox(message);
            conversationHolder.getChildren().add(messageBox);

            // Force layout refresh
            conversationHolder.autosize();
            conversationHolder.applyCss();
            conversationHolder.layout();

            // Clear input and scroll to bottom
            msgInput.clear();
            Platform.runLater(() -> {
                scrollToBottom();
                // Double check the scroll after a brief delay to ensure it works
                Platform.runLater(this::scrollToBottom);
            });

            System.out.println("Message sent and displayed: " + messageText);
        } else {
            System.err.println("Failed to send message");
        }
    }

    private void handleInfoButton(MouseEvent event) {
        if (selectedUser != null) {
            try {
                showUserProfileModal(selectedUser);
            } catch (Exception e) {
                System.err.println("Error opening user profile: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

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

            Image icon = new Image(App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/Main-Logo.png"));
            modalStage.getIcons().add(icon);

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

    private void showInitialState() {
        userName.setText("Select a conversation");
        userLocationAge.setText("Choose someone to start chatting");
        infoButton.setVisible(false);
        disableChatInput();
        conversationHolder.getChildren().clear();

        // Show initial message
        Label initialLabel = new Label("Select a user from the list to start chatting");
        initialLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 16px;");
        VBox initialBox = new VBox(initialLabel);
        initialBox.setAlignment(Pos.CENTER);
        initialBox.setMaxWidth(Double.MAX_VALUE);
        initialBox.setMaxHeight(Double.MAX_VALUE);
        initialBox.setSpacing(10);
        VBox.setVgrow(initialBox, Priority.ALWAYS);
        conversationHolder.getChildren().add(initialBox);
    }

    private void showEmptyMatchesState() {
        Label noMatchesLabel = new Label("No matches yet!");
        noMatchesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

        Label subLabel = new Label("Find your perfect match in the Discover section");
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999;");

        VBox emptyBox = new VBox(noMatchesLabel, subLabel);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setSpacing(8);

        userlistHolder.getChildren().add(emptyBox);

        // Update right side
        userName.setText("No matches");
        userLocationAge.setText("Find matches in Discover");
        infoButton.setVisible(false);
        disableChatInput();
    }

    private void showEmptyConversationState() {
        Label startLabel = new Label("Start your conversation!");
        startLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

        Label subLabel = new Label("Say hello to " + selectedUser.getName());
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999;");

        VBox emptyBox = new VBox(startLabel, subLabel);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setMaxWidth(Double.MAX_VALUE);
        emptyBox.setMaxHeight(Double.MAX_VALUE);
        emptyBox.setSpacing(8);
        VBox.setVgrow(emptyBox, Priority.ALWAYS);

        conversationHolder.getChildren().add(emptyBox);
    }

    private void enableChatInput() {
        msgInput.setDisable(false);
        sendButton.setDisable(false);
        msgInput.setPromptText("Type your message...");
    }

    private void disableChatInput() {
        msgInput.setDisable(true);
        sendButton.setDisable(true);
        msgInput.setPromptText("Select a user to start chatting");
    }

    private void scrollToBottom() {
        // Find the ScrollPane parent of conversationHolder
        javafx.scene.Node parent = conversationHolder.getParent();
        while (parent != null && !(parent instanceof ScrollPane)) {
            parent = parent.getParent();
        }

        if (parent instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) parent;
            // Force layout update first
            scrollPane.applyCss();
            scrollPane.layout();
            // Then scroll to bottom
            Platform.runLater(() -> {
                scrollPane.setVvalue(1.0);
            });
        }
    }

    // private void scrollUserListToTop() {
    // // Find the ScrollPane parent of userlistHolder
    // javafx.scene.Node parent = userlistHolder.getParent();
    // while (parent != null && !(parent instanceof ScrollPane)) {
    // parent = parent.getParent();
    // }

    // if (parent instanceof ScrollPane) {
    // ScrollPane scrollPane = (ScrollPane) parent;
    // scrollPane.setVvalue(0.0);
    // }
    // }

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
    } // Method to refresh the chat (can be called when returning to this page)

    public void refreshChat() {
        loadMatchedUsers();
        displayUserList();
        if (selectedUser != null) {
            loadConversation();
        }
    }

    // Real-time message polling methods
    private void startMessagePolling() {
        if (selectedUser == null)
            return;

        // Stop any existing polling first
        stopMessagePolling();

        // Set initial message count
        lastMessageCount = currentConversation.size();

        // Create timeline for polling every 2 seconds
        messagePollingTimer = new Timeline(new KeyFrame(
                Duration.seconds(2),
                e -> checkForNewMessages()));
        messagePollingTimer.setCycleCount(Timeline.INDEFINITE);
        messagePollingTimer.play();

        System.out.println("Started message polling for user: " + selectedUser.getName());
    }

    private void stopMessagePolling() {
        if (messagePollingTimer != null) {
            messagePollingTimer.stop();
            messagePollingTimer = null;
            System.out.println("Stopped message polling");
        }
    }

    private void checkForNewMessages() {
        if (selectedUser == null)
            return;

        try {
            // Get latest conversation
            List<Message> latestConversation = firebaseConnection.getConversation(
                    AuthController.CurrentUser.getUserId(),
                    selectedUser.getUserId());

            // Check if there are new messages
            if (latestConversation.size() > lastMessageCount) {
                System.out.println("New messages detected: " + (latestConversation.size() - lastMessageCount));

                // Add only the new messages to avoid flickering
                for (int i = lastMessageCount; i < latestConversation.size(); i++) {
                    Message newMessage = latestConversation.get(i);
                    currentConversation.add(newMessage);

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        VBox messageBox = createMessageBox(newMessage);
                        conversationHolder.getChildren().add(messageBox);

                        // Force layout refresh and scroll to bottom
                        conversationHolder.autosize();
                        conversationHolder.applyCss();
                        conversationHolder.layout();

                        Platform.runLater(() -> {
                            scrollToBottom();
                        });
                    });
                }

                // Update message count
                lastMessageCount = latestConversation.size();
            }
        } catch (Exception e) {
            System.err.println("Error checking for new messages: " + e.getMessage());
        }
    }

    // Cleanup method to stop polling when controller is destroyed
    public void cleanup() {
        stopMessagePolling();
    }
}
