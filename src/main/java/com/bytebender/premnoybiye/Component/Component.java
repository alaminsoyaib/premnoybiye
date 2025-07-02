package com.bytebender.premnoybiye.Component;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import com.bytebender.premnoybiye.App;
import com.bytebender.premnoybiye.DBConnection.userInfo;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class Component {

    public void setImage(String imagePath, ImageView element, double width, double height, boolean preserveRatio,
            int arcSize) {
        element.setImage(new Image(imagePath));
        element.setFitWidth(width);
        element.setFitHeight(height);
        element.setPreserveRatio(preserveRatio);

        if (arcSize > 0) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
            clip.setArcWidth(arcSize);
            clip.setArcHeight(arcSize);
            element.setClip(clip);
        }
    }

    /**
     * Enhanced setImage method with caching support for userInfo objects
     */
    public void setImageCached(userInfo user, ImageView element, double width, double height, boolean preserveRatio,
            int arcSize) {
        if (user == null) {
            setDefaultImage(element, width, height, preserveRatio, arcSize);
            return;
        }

        // Check if user has cached image
        if (user.hasCachedImage()) {
            applyImageToElement(user.getCachedImage(), element, width, height, preserveRatio, arcSize);
            return;
        }

        String imagePath = user.getImage();
        if (imagePath == null || imagePath.isEmpty()) {
            setDefaultImage(element, width, height, preserveRatio, arcSize);
            return;
        }

        // Set default image first, then load cached/remote image asynchronously
        setDefaultImage(element, width, height, preserveRatio, arcSize);

        // Load image with caching
        ImageCache.getImageAsync(imagePath,
                image -> {
                    user.setCachedImage(image); // Cache in user object
                    applyImageToElement(image, element, width, height, preserveRatio, arcSize);
                },
                error -> {
                    System.err.println("Failed to load image for user " + user.getName() + ": " + error);
                    // Keep default image on error
                });
    }

    /**
     * Sets a default placeholder image
     */
    private void setDefaultImage(ImageView element, double width, double height, boolean preserveRatio, int arcSize) {
        Image defaultImage = new Image(
                getClass().getResourceAsStream("/com/bytebender/premnoybiye/img/icon/card-img.png"));
        applyImageToElement(defaultImage, element, width, height, preserveRatio, arcSize);
    }

    /**
     * Applies an image to an ImageView element with specified properties
     */
    private void applyImageToElement(Image image, ImageView element, double width, double height, boolean preserveRatio,
            int arcSize) {
        element.setImage(image);
        element.setFitWidth(width);
        element.setFitHeight(height);
        element.setPreserveRatio(preserveRatio);

        if (arcSize > 0) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
            clip.setArcWidth(arcSize);
            clip.setArcHeight(arcSize);
            element.setClip(clip);
        }
    }

    public VBox createLoadingState(String message) {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        indicator.setPrefSize(50, 50);

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        VBox container = new VBox(15, indicator, label);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-padding: 30px;");
        return container;
    }

    public VBox createErrorState(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #ea0000; -fx-alignment: center; -fx-padding: 30px;");
        VBox container = new VBox(label);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    public String calculateAge(String dob) {
        if (dob == null || dob.isEmpty())
            return "N/A";
        try {
            LocalDate birthDate = LocalDate.parse(dob, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return String.valueOf(Period.between(birthDate, LocalDate.now()).getYears());
        } catch (Exception e) {
            return "N/A";
        }
    }

    public void setImageWithClip(String imagePath, ImageView imageView, ImageView blurView, StackPane stackPane,
            double width, double height) {
        if (imagePath != null && !imagePath.isEmpty()) {
            setImage(imagePath, imageView, width, height, true, 0);
            if (blurView != null)
                setImage(imagePath, blurView, width, height, false, 0);
        } else {
            imageView.setImage(
                    new Image(getClass().getResourceAsStream("/com/bytebender/premnoybiye/img/icon/card-img.png")));
        }

        if (stackPane != null) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            stackPane.setClip(clip);
        }
    }

    /**
     * Enhanced setImageWithClip method with caching support for userInfo objects
     */
    public void setImageWithClipCached(userInfo user, ImageView imageView, ImageView blurView, StackPane stackPane,
            double width, double height) {
        if (user == null || user.getImage() == null || user.getImage().isEmpty()) {
            // Set default image
            Image defaultImage = new Image(
                    getClass().getResourceAsStream("/com/bytebender/premnoybiye/img/icon/card-img.png"));
            imageView.setImage(defaultImage);
            if (blurView != null) {
                blurView.setImage(defaultImage);
            }
        } else {
            // Check if user has cached image
            if (user.hasCachedImage()) {
                Image cachedImage = user.getCachedImage();
                imageView.setImage(cachedImage);
                if (blurView != null) {
                    blurView.setImage(cachedImage);
                }
            } else {
                // Set default image first, then load asynchronously
                Image defaultImage = new Image(
                        getClass().getResourceAsStream("/com/bytebender/premnoybiye/img/icon/card-img.png"));
                imageView.setImage(defaultImage);
                if (blurView != null) {
                    blurView.setImage(defaultImage);
                }

                // Load image with caching
                ImageCache.getImageAsync(user.getImage(),
                        image -> {
                            user.setCachedImage(image); // Cache in user object
                            imageView.setImage(image);
                            if (blurView != null) {
                                blurView.setImage(image);
                            }
                        },
                        error -> {
                            System.err.println("Failed to load image for user " + user.getName() + ": " + error);
                            // Keep default image on error
                        });
            }
        }

        // Apply clip regardless
        if (stackPane != null) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            stackPane.setClip(clip);
        }
    }

    /**
     * Cached version of setImage for userInfo objects with custom clipping
     */
    public void setImageCachedWithCustomClip(userInfo user, ImageView imageView, ImageView blurView,
            StackPane stackPane, double width, double height, double arcSize) {
        String defaultImagePath = "/com/bytebender/premnoybiye/img/icon/profile-image.png";

        if (user == null || user.getImage() == null || user.getImage().isEmpty()) {
            // Set default image
            Image defaultImage = new Image(getClass().getResourceAsStream(defaultImagePath));
            imageView.setImage(defaultImage);
            if (blurView != null) {
                blurView.setImage(defaultImage);
            }
        } else {
            // Check if user has cached image
            if (user.hasCachedImage()) {
                Image cachedImage = user.getCachedImage();
                applyImageToElement(cachedImage, imageView, width, height, true, (int) arcSize);
                if (blurView != null) {
                    applyImageToElement(cachedImage, blurView, width, height, false, (int) arcSize);
                }
            } else {
                // Set default image first, then load asynchronously
                Image defaultImage = new Image(getClass().getResourceAsStream(defaultImagePath));
                applyImageToElement(defaultImage, imageView, width, height, true, (int) arcSize);
                if (blurView != null) {
                    applyImageToElement(defaultImage, blurView, width, height, false, (int) arcSize);
                }

                // Load image with caching
                ImageCache.getImageAsync(user.getImage(),
                        image -> {
                            user.setCachedImage(image); // Cache in user object
                            applyImageToElement(image, imageView, width, height, true, (int) arcSize);
                            if (blurView != null) {
                                applyImageToElement(image, blurView, width, height, false, (int) arcSize);
                            }
                        },
                        error -> {
                            System.err.println("Failed to load image for user " + user.getName() + ": " + error);
                            // Keep default image on error
                        });
            }
        }

        // Apply clip to stack pane if provided
        if (stackPane != null) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(width, height);
            clip.setArcWidth(arcSize);
            clip.setArcHeight(arcSize);
            stackPane.setClip(clip);
        }
    }

    public void toggleVisibility(boolean visible, javafx.scene.Node... nodes) {
        for (javafx.scene.Node node : nodes) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    public int toggleSidebarState(ImageView sidebarCollapse, HBox demosidebarProfile, Label discoverLabel,
            Label matchesLabel, Label profileLabel, Label msgLabel, Label logoutLabel, VBox Sidebar,
            int currentFlag) {
        if (currentFlag == 0) {
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/open-drawer-icon.png")));
            demosidebarProfile.setVisible(false);
            demosidebarProfile.setManaged(false);

            discoverLabel.setVisible(false);
            discoverLabel.setManaged(false);
            matchesLabel.setVisible(false);
            matchesLabel.setManaged(false);
            profileLabel.setVisible(false);
            profileLabel.setManaged(false);
            msgLabel.setVisible(false);
            msgLabel.setManaged(false);
            logoutLabel.setVisible(false);
            logoutLabel.setManaged(false);
            Sidebar.setPrefWidth(72);
            return 1;
        } else {
            sidebarCollapse.setImage(new Image(
                    App.class.getResourceAsStream("/com/bytebender/premnoybiye/img/icon/close-drawer-icon.png")));
            demosidebarProfile.setVisible(true);
            demosidebarProfile.setManaged(true);

            discoverLabel.setVisible(true);
            discoverLabel.setManaged(true);
            matchesLabel.setVisible(true);
            matchesLabel.setManaged(true);
            profileLabel.setVisible(true);
            profileLabel.setManaged(true);
            msgLabel.setVisible(true);
            msgLabel.setManaged(true);
            logoutLabel.setVisible(true);
            logoutLabel.setManaged(true);
            Sidebar.setPrefWidth(244);
            return 0;
        }
    }
}
