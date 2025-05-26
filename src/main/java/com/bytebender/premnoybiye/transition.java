package com.bytebender.premnoybiye;

import java.io.IOException;

import javafx.fxml.FXML;

public class transition {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

}
