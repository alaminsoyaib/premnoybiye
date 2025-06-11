module com.bytebender.premnoybiye {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;

    opens com.bytebender.premnoybiye to javafx.fxml;

    exports com.bytebender.premnoybiye;
}
