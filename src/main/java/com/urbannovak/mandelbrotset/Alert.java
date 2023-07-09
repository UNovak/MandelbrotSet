package com.urbannovak.mandelbrotset;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Alert {
    public void display(String msg, String btn){

        Stage window = new Stage();
        window.setTitle("Mandelbrot Set");
        window.setResizable(false);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setAlwaysOnTop(true);
        Label message = new Label(msg);
        Button button = new Button(btn);
        button.setOnAction(e -> window.close());

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(200,150);
        vbox.getChildren().addAll(message, button);

        Scene alertScene = new Scene(vbox);
        window.setScene(alertScene);
        window.showAndWait();

    }
}
