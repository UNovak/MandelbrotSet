package com.urbannovak.mandelbrotset;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {


    Stage window;
    Scene menu;
    Scene image;

    RadioButton sequential;
    RadioButton parallel;
    RadioButton MPI;

    @Override
    public void start(Stage stage) throws IOException {
        window = stage;
        window.setTitle("Mandelbrot Set");
        window.setResizable(false);

        VBox vbox = new VBox(10);
        Scene menu = new Scene(vbox,250,250);
        vbox.setAlignment(Pos.CENTER);

        Label displayTxt = new Label("Display image?");
        ToggleButton displayImage = new ToggleButton("NO");
        displayImage.setSelected(false);
        displayImage.setOnAction(e -> {
            if (displayImage.isSelected()) displayImage.setText("YES");
            else displayImage.setText("NO");
        });

        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);
        Label modeTxt = new Label("How should the program be run");
        ToggleGroup modeGroup = new ToggleGroup();

        sequential = new RadioButton("sequential");
        sequential.getStyleClass().remove("radio-button");
        sequential.getStyleClass().add("toggle-button");
        sequential.setSelected(true);

        parallel = new RadioButton("parallel");
        parallel.getStyleClass().remove("radio-button");
        parallel.getStyleClass().add("toggle-button");

        MPI = new RadioButton("MPI");
        MPI.getStyleClass().remove("radio-button");
        MPI.getStyleClass().add("toggle-button");

        MPI.setToggleGroup(modeGroup);
        parallel.setToggleGroup(modeGroup);
        sequential.setToggleGroup(modeGroup);

        hbox.getChildren().addAll(sequential,parallel,MPI);

        SplitPane br = new SplitPane();

        Button start = new Button("START");
        start.setMinSize(80,40);


        vbox.getChildren().addAll(displayTxt,displayImage,modeTxt,hbox,br,start);

        window.setScene(menu);
        window.show();
    }

    public static void main(String[] args) {
        launch();
    }
}