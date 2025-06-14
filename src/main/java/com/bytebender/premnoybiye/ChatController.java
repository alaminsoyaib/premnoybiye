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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChatController {

    private FirebaseConnection firebaseConnection = new FirebaseConnection();
    private Component component = new Component();
    private List<userInfo> matchedUsersList = new ArrayList<>();
    private userInfo selectedUser = null;
    private List<Message> currentConversation = new ArrayList<>();

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
        loadMatchedUsers();
        displayUserList();
        setupEventHandlers();
        showInitialState();
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
        selectedUser = user;
        updateChatHeader();
        loadConversation();
        enableChatInput();
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

        // Scroll to bottom after loading messages
        Platform.runLater(this::scrollToBottom);
    }

    private VBox createMessageBox(Message message) {
        VBox messageBox = new VBox();
        messageBox.setSpacing(4);
        messageBox.setMaxWidth(400);

        // Create message content
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setPadding(new Insets(8, 12, 8, 12));
        contentLabel.setFont(Font.font("Trebuchet MS", 14));

        // Create time label
        Label timeLabel = new Label(message.getFormattedTime());
        timeLabel.setFont(Font.font("Trebuchet MS", 10));
        timeLabel.setStyle("-fx-text-fill: #666666;");

        boolean isMyMessage = message.getSenderId().equals(AuthController.CurrentUser.getUserId());

        if (isMyMessage) {
            // My message - align right, blue background
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            contentLabel.setStyle("-fx-background-color: #6631c4; -fx-text-fill: white; -fx-background-radius: 12;");
            timeLabel.setStyle("-fx-text-fill: #666666; -fx-alignment: center-right;");
            VBox.setMargin(messageBox, new Insets(0, 0, 0, 50));
        } else {
            // Their message - align left, gray background
            messageBox.setAlignment(Pos.CENTER_LEFT);
            contentLabel.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 12;");
            timeLabel.setStyle("-fx-text-fill: #666666; -fx-alignment: center-left;");
            VBox.setMargin(messageBox, new Insets(0, 50, 0, 0));
        }

        messageBox.getChildren().addAll(contentLabel, timeLabel);
        return messageBox;
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
            VBox messageBox = createMessageBox(message);
            conversationHolder.getChildren().add(messageBox);

            // Clear input and scroll to bottom
            msgInput.clear();
            Platform.runLater(this::scrollToBottom);
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
        initialBox.setSpacing(10);
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
        emptyBox.setSpacing(8);

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
            scrollPane.setVvalue(1.0);
        }
    }

    // private void scrollUserListToTop() {
    //     // Find the ScrollPane parent of userlistHolder
    //     javafx.scene.Node parent = userlistHolder.getParent();
    //     while (parent != null && !(parent instanceof ScrollPane)) {
    //         parent = parent.getParent();
    //     }

    //     if (parent instanceof ScrollPane) {
    //         ScrollPane scrollPane = (ScrollPane) parent;
    //         scrollPane.setVvalue(0.0);
    //     }
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
    }

    // Method to refresh the chat (can be called when returning to this page)
    public void refreshChat() {
        loadMatchedUsers();
        displayUserList();
        if (selectedUser != null) {
            loadConversation();
        }
    }
}
