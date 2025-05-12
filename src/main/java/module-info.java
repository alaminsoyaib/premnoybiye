module com.bytebender.premnoybiye {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.bytebender.premnoybiye to javafx.fxml;
    exports com.bytebender.premnoybiye;
}
