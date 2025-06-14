module com.bytebender.premnoybiye {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.bytebender.premnoybiye to javafx.fxml;

    exports com.bytebender.premnoybiye;
    exports com.bytebender.premnoybiye.DBConnection;
}
