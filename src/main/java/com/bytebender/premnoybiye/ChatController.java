package com.bytebender.premnoybiye;

import java.io.IOException;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChatController {
    private final FirebaseConnection firebaseConnection = new FirebaseConnection();
    private final Component component = new Component();
    private List<userInfo> matchedUsersList = new ArrayList<>();
    private userInfo selectedUser = null;
    private List<Message> currentConversation = new ArrayList<>();
    private Timeline messagePollingTimer;
    private int lastMessageCount = 0;
    private boolean isDataLoaded = false;

    @FXML
    private VBox userlistHolder, conversationHolder;
    @FXML
    private TextField msgInput;
    @FXML
    private ImageView sendButton, infoButton;
    @FXML
    private javafx.scene.text.Text userName, userLocationAge;

    @FXML
    public void initialize() {
        setupEventHandlers();
        showInitialState();

        if (!isDataLoaded) {
            showLoadingUserList();
            loadMatchedUsersAsync();
        } else {
            displayUserList();
        }
        stopMessagePolling();
    }

    private void setupEventHandlers() {
        sendButton.setOnMouseClicked(this::handleSendMessage);
        infoButton.setOnMouseClicked(this::handleInfoButton);
        msgInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                handleSendMessage(null);
        });
    }

    private void showLoadingUserList() {
        userlistHolder.getChildren().clear();
        userlistHolder.getChildren().add(component.createLoadingState("Loading conversations..."));
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
            displayUserList();
        }));

        loadTask.setOnFailed(e -> Platform.runLater(this::showErrorUserList));

        new Thread(loadTask) {
            {
                setDaemon(true);
            }
        }.start();
    }

    private void showErrorUserList() {
        userlistHolder.getChildren().clear();
        userlistHolder.getChildren().add(component.createErrorState("Failed to load conversations"));
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("userchatlist.fxml"));
                HBox chatListItem = loader.load();
                populateChatListItem(chatListItem, user);
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
            ImageView userImg = (ImageView) chatListItem.lookup("#userImg");
            ImageView blurImg = (ImageView) chatListItem.lookup("#blurImg");
            StackPane imgStack = (StackPane) chatListItem.lookup("#imgStack");
            Label userName = (Label) chatListItem.lookup("#userName");
            Label userLocationAge = (Label) chatListItem.lookup("#userLocationAge");

            if (userName != null)
                userName.setText(user.getName());
            if (userLocationAge != null) {
                String age = component.calculateAge(user.getDob());
                userLocationAge.setText(user.getCity() + ", " + age);
            }

            if (userImg != null && blurImg != null) {
                String defaultImage = "img/icon/profile-image.png";
                if (user.getImage() != null && !user.getImage().isEmpty()) {
                    component.setImage(user.getImage(), userImg, 48, 48, true, 24);
                    component.setImage(user.getImage(), blurImg, 48, 48, false, 24);
                } else {
                    userImg.setImage(new Image(getClass().getResourceAsStream(defaultImage)));
                    blurImg.setImage(new Image(getClass().getResourceAsStream(defaultImage)));
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
            String age = component.calculateAge(selectedUser.getDob());
            userLocationAge.setText(selectedUser.getCity() + ", " + age);
            infoButton.setVisible(true);
        }
    }

    private void loadConversation() {
        if (selectedUser == null)
            return;

        currentConversation = firebaseConnection.getConversation(
                AuthController.CurrentUser.getUserId(), selectedUser.getUserId());
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

        refreshLayout();
        Platform.runLater(this::scrollToBottom);
    }

    private VBox createMessageBox(Message message) {
        HBox messageRow = new HBox();
        messageRow.setMaxWidth(Double.MAX_VALUE);
        messageRow.setFillHeight(false);

        VBox messageBox = new VBox();
        messageBox.setSpacing(4);
        messageBox.setMaxWidth(350);
        messageBox.setMinWidth(80);

        Label contentLabel = createContentLabel(message.getContent());
        Label timeLabel = createTimeLabel(message.getFormattedTime());

        boolean isMyMessage = message.getSenderId().equals(AuthController.CurrentUser.getUserId());
        styleMessage(messageRow, messageBox, contentLabel, timeLabel, isMyMessage);

        messageBox.getChildren().addAll(contentLabel, timeLabel);
        messageRow.getChildren().add(messageBox);

        VBox wrapper = new VBox();
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.getChildren().add(messageRow);
        return wrapper;
    }

    private Label createContentLabel(String content) {
        Label label = new Label(content);
        label.setWrapText(true);
        label.setPadding(new Insets(10, 14, 10, 14));
        label.setFont(Font.font("Trebuchet MS", 14));
        label.setMaxWidth(350);
        label.setMinWidth(80);
        label.setMinHeight(Label.USE_PREF_SIZE);
        label.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setTextOverrun(OverrunStyle.CLIP);
        return label;
    }

    private Label createTimeLabel(String time) {
        Label label = new Label(time);
        label.setFont(Font.font("Trebuchet MS", 10));
        label.setStyle("-fx-text-fill: #666666;");
        return label;
    }

    private void styleMessage(HBox messageRow, VBox messageBox, Label contentLabel, Label timeLabel,
            boolean isMyMessage) {
        if (isMyMessage) {
            messageRow.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            contentLabel.setStyle(
                    "-fx-background-color: #6631c4; -fx-text-fill: white; -fx-background-radius: 18 18 4 18; -fx-label-padding: 0; -fx-text-overrun: clip;");
            timeLabel.setAlignment(Pos.CENTER_RIGHT);
            HBox.setMargin(messageBox, new Insets(5, 16, 5, 80));
        } else {
            messageRow.setAlignment(Pos.CENTER_LEFT);
            messageBox.setAlignment(Pos.CENTER_LEFT);
            contentLabel.setStyle(
                    "-fx-background-color: #E5E5EA; -fx-text-fill: #000000; -fx-background-radius: 18 18 18 4; -fx-label-padding: 0; -fx-text-overrun: clip;");
            timeLabel.setAlignment(Pos.CENTER_LEFT);
            HBox.setMargin(messageBox, new Insets(5, 80, 5, 16));
        }
    }

    private void handleSendMessage(MouseEvent event) {
        String messageText = msgInput.getText().trim();
        if (messageText.isEmpty() || selectedUser == null)
            return;

        Message message = new Message(AuthController.CurrentUser.getUserId(), selectedUser.getUserId(), messageText);
        if (firebaseConnection.sendMessage(message)) {
            currentConversation.add(message);
            lastMessageCount = currentConversation.size();

            VBox messageBox = createMessageBox(message);
            conversationHolder.getChildren().add(messageBox);

            refreshLayout();
            msgInput.clear();
            Platform.runLater(() -> {
                scrollToBottom();
                Platform.runLater(this::scrollToBottom);
            });

            System.out.println("Message sent and displayed: " + messageText);
        } else {
            System.err.println("Failed to send message");
        }
    }

    private void refreshLayout() {
        conversationHolder.autosize();
        conversationHolder.applyCss();
        conversationHolder.layout();
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

    private void showInitialState() {
        userName.setText("Select a conversation");
        userLocationAge.setText("Choose someone to start chatting");
        infoButton.setVisible(false);
        disableChatInput();
        conversationHolder.getChildren().clear();

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
        javafx.scene.Node parent = conversationHolder.getParent();
        while (parent != null && !(parent instanceof ScrollPane)) {
            parent = parent.getParent();
        }

        if (parent instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) parent;
            scrollPane.applyCss();
            scrollPane.layout();
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        }
    }

    public void refreshChat() {
        loadMatchedUsers();
        displayUserList();
        if (selectedUser != null) {
            loadConversation();
        }
    }

    private void startMessagePolling() {
        if (selectedUser == null)
            return;

        stopMessagePolling();
        lastMessageCount = currentConversation.size();

        messagePollingTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> checkForNewMessages()));
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
            List<Message> latestConversation = firebaseConnection.getConversation(
                    AuthController.CurrentUser.getUserId(), selectedUser.getUserId());

            if (latestConversation.size() > lastMessageCount) {
                System.out.println("New messages detected: " + (latestConversation.size() - lastMessageCount));

                for (int i = lastMessageCount; i < latestConversation.size(); i++) {
                    Message newMessage = latestConversation.get(i);
                    currentConversation.add(newMessage);

                    Platform.runLater(() -> {
                        VBox messageBox = createMessageBox(newMessage);
                        conversationHolder.getChildren().add(messageBox);
                        refreshLayout();
                        Platform.runLater(this::scrollToBottom);
                    });
                }
                lastMessageCount = latestConversation.size();
            }
        } catch (Exception e) {
            System.err.println("Error checking for new messages: " + e.getMessage());
        }
    }

    public void cleanup() {
        stopMessagePolling();
    }
}
