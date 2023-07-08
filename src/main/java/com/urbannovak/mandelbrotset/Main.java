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

    //<editor-fold desc="Variables for setCalculation">
    SetCalculation setMaker = new SetCalculation();
    private static double WIDTH = 800;
    private static final int HEIGHT = 600;
    private static double ZOOM = 1.0;
    private static double xCENTER = 0.0;
    private static double yCENTER = 0.0;
    private String runMode = "parallel";
    //</editor-fold>

    //<editor-fold desc="variables needed for GUI">
    Stage window;
    Scene menu;
    Scene image;

    RadioButton sequential;
    RadioButton parallel;
    RadioButton distributed;
    VBox sizeAll;
    String fieldID;
    //</editor-fold>

    @Override
    public void start(Stage stage) throws IOException {
        // renames stage to window to make it more logical
        window = stage;
        window.setTitle("Mandelbrot Set");
        window.setResizable(false);

        // creates the toggle for displaying image and the prompt
        Label displayTxt = new Label("Display image?");
        ToggleButton displayImage = new ToggleButton("NO");
        displayImage.setSelected(false);
        displayImage.setOnAction(e -> {
            if (displayImage.isSelected()) displayImage.setText("YES");
            else displayImage.setText("NO");
        });

        //<editor-fold desc="Size input">
        // label cration and styling
        Label sizeQuestion = new Label("How big should the image be?");
        sizeQuestion.setPadding(new Insets(0,0,10,0));

        // input field creation and styling
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

        // adding size elements together
        sizeAll = new VBox(5);
        sizeAll.setAlignment(Pos.CENTER);
        sizeAll.getChildren().addAll(sizeQuestion,sizeInput);
        //</editor-fold>


        //<editor-fold desc="Toggle buttons for determining the run mode">
        // handles the toggle group to switch between run modes
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);
        Label modeTxt = new Label("How should the program be run");
        ToggleGroup modeGroup = new ToggleGroup();

        sequential = new RadioButton("sequential");
        sequential.getStyleClass().remove("radio-button");
        sequential.getStyleClass().add("toggle-button");
        sequential.setId("sequential");
        sequential.setSelected(true); // default selected run mode == parallel

        parallel = new RadioButton("parallel");
        parallel.getStyleClass().remove("radio-button");
        parallel.getStyleClass().add("toggle-button");
        parallel.setId("parallel");

        distributed = new RadioButton("distributed");
        distributed.getStyleClass().remove("radio-button");
        distributed.getStyleClass().add("toggle-button");
        distributed.setId("distributed");

        distributed.setToggleGroup(modeGroup);
        parallel.setToggleGroup(modeGroup);
        sequential.setToggleGroup(modeGroup);

        // add all buttons to parent
        hbox.getChildren().addAll(sequential,parallel, distributed);
        //</editor-fold>

        //<editor-fold desc="create horizontal lines to divide the menu">
        SplitPane br1 = new SplitPane();
        SplitPane br2 = new SplitPane();
        SplitPane br3 = new SplitPane();
        //</editor-fold>

        // start button
        Button start = new Button("START");
        start.setMinSize(80,40);
        start.setTextFill(Color.RED);

        // runs when start button is pressed
        start.setOnAction(e -> {
            if(validateForInt(widthInput) && validateForInt(heightInput)){ // check if width end height are only ints
                runMode = getMode(modeGroup);       // sets the string for determining the mode to run in
                // passing the varibles to setCalculation
                setMaker.setHEIGHT(HEIGHT);
                setMaker.setWIDTH(WIDTH);
                setMaker.setXCENTER(xCENTER);
                setMaker.setYCENTER(yCENTER);
                setMaker.setZoom(ZOOM);
                // calls the method to run the calculation
                setMaker.run(runMode);
                window.setScene(image);             // changes the scene to image
            } else {
                // size not specified in the right format, displays an alert
                Alert alert = new Alert();
                alert.display("Mandelbrot", fieldID + " is not a number", "Okay");
            }
        });

        //<editor-fold desc="VBox layout for all menu components">
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        // joins all components of the menu in a vertical box layout
        vbox.getChildren().addAll(displayTxt,displayImage,br1,sizeAll,br2,modeTxt,hbox,br3,start);
        //</editor-fold>


        // makes the menu window visible upon running the app
        menu = new Scene(vbox,250,300);
        window.setScene(menu);
        window.show();
    }

    // returns true if the field only contains numbers
    public boolean validateForInt(TextField input){
        fieldID = input.getId();
        return input.getText().matches("\\d+");
    }

    // a method that returns the ID of the selected radiobutton to determine the mode
    private String getMode(ToggleGroup group){
        RadioButton selected = (RadioButton) group.getSelectedToggle();
        return selected.getId();
    }

    public static void main(String[] args) {
        launch();
    }
}