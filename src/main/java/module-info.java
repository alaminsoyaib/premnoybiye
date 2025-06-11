module com.bytebender.premnoybiye {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;

    // Core Java modules
    requires java.base;
    requires java.net.http;
    requires java.logging; // Firebase and Google Cloud dependencies (automatic modules)
    requires firebase.admin;
    requires google.cloud.storage;
    requires google.cloud.core;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    // Essential Google dependencies
    requires com.google.common;
    requires com.google.gson;

    // Additional required modules for proper functionality
    requires java.sql;
    requires java.management;

    // Open packages for JavaFX FXML loading and reflection access
    opens com.bytebender.premnoybiye to javafx.fxml, com.google.gson;
    opens com.bytebender.premnoybiye.DBConnection
            to javafx.fxml, com.google.gson, firebase.admin, com.google.auth.oauth2;

    // Export the main application package
    exports com.bytebender.premnoybiye;
}
