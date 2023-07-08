package com.urbannovak.mandelbrotset;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    Stage window;
    Scene menu;
    Scene image;
    String fieldID;

    RadioButton sequential;
    RadioButton parallel;
    RadioButton distributed;
    VBox sizeAll;

    @Override
    public void start(Stage stage) throws IOException {
        window = stage;
        window.setTitle("Mandelbrot Set");
        window.setResizable(false);

        VBox vbox = new VBox(10);
        menu = new Scene(vbox,250,300);
        vbox.setAlignment(Pos.CENTER);

        // creates the toggle for displaying image and the prompt
        Label displayTxt = new Label("Display image?");
        ToggleButton displayImage = new ToggleButton("NO");
        displayImage.setSelected(false);
        displayImage.setOnAction(e -> {
            if (displayImage.isSelected()) displayImage.setText("YES");
            else displayImage.setText("NO");
        });

        // handles the input for size
        Label sizeQuestion = new Label("How big should the image be?");
        sizeQuestion.setPadding(new Insets(0,0,10,0));

        HBox sizeInput = new HBox(5);
        Text widthPrompt = new Text("width:");
        TextField widthInput = new TextField("800");
        widthInput.setId("width");
        widthInput.setMaxSize(50,20);
        Text heightPrompt = new Text("height:");
        TextField heightInput  = new TextField("600");
        heightInput.setId("height");
        heightInput.setMaxSize(50,20);
        sizeInput.getChildren().addAll(widthPrompt,widthInput,heightPrompt,heightInput);
        sizeInput.setAlignment(Pos.CENTER);

        sizeAll = new VBox(5);
        sizeAll.setAlignment(Pos.CENTER);
        sizeAll.getChildren().addAll(sizeQuestion,sizeInput);

        // handles the toggle group to switch between run modes
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

        distributed = new RadioButton("distributed");
        distributed.getStyleClass().remove("radio-button");
        distributed.getStyleClass().add("toggle-button");

        distributed.setToggleGroup(modeGroup);
        parallel.setToggleGroup(modeGroup);
        sequential.setToggleGroup(modeGroup);

        hbox.getChildren().addAll(sequential,parallel, distributed);

        // create horizontal lines to divide the menu
        SplitPane br1 = new SplitPane();
        SplitPane br2 = new SplitPane();
        SplitPane br3 = new SplitPane();

        // start button
        Button start = new Button("START");
        start.setMinSize(80,40);
        start.setTextFill(Color.RED);

        // runs when start button is pressed
        start.setOnAction(e -> {
            if(validateForInt(widthInput) && validateForInt(heightInput)){ // check if width end height are only ints
                window.setScene(image);
            } else {
                Alert alert = new Alert();
                alert.display("Mandelbrot", fieldID + " is not a number", "Okay");
            }
        });

        vbox.getChildren().addAll(displayTxt,displayImage,br1,sizeAll,br2,modeTxt,hbox,br3,start);

        window.setScene(menu);
        window.show();
    }

    // returns true if the field only contains numbers
    public boolean validateForInt(TextField input){
        fieldID = input.getId();
        return input.getText().matches("\\d+");
    }


    public static void main(String[] args) {
        launch();
    }
}