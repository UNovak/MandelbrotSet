package com.urbannovak.mandelbrotset;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Main extends Application {

    //<editor-fold desc="Variables for setCalculation">
    SetCalculation setMaker = new SetCalculation();
    private static int width = 800;
    private static int height = 600;
    private static final double zoom = 1.0;
    private String runMode = "parallel";
    private boolean render;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double zoomFactor = 1.0;
    private static final double ZOOM_DELTA = 0.1;

    //</editor-fold>

    //<editor-fold desc="variables needed for GUI">
    Stage window;
    Scene menu;
    Scene imageScene;
    Image image;
    ImageView imageView;

    RadioButton sequential;
    RadioButton parallel;
    RadioButton distributed;
    VBox sizeAll;
    String fieldID;
    Alert alert = new Alert();
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
            if(validateInput(widthInput) && validateInput(heightInput)){ // check if width end height are only ints
                runMode = getMode(modeGroup);  // sets the string for determining the mode to run in
                if (!setVariables(widthInput,heightInput)) e.consume();
                else {
                    render = displayImage.isSelected();
                    image = setMaker.run(runMode);
                    if (render) {
                        window.setResizable(true);
                        imageView.setFitWidth(width);
                        imageView.setFitHeight(height);
                        imageView.setImage(image);
                        window.setScene(imageScene);
                    } else {
                        alert.display("Image saved to downloads", "Okay");
                        saveFile();
                    }
                }
            } else {
                // size not specified in the right format, displays an alert
                alert.display(fieldID + " is not a number","Okay");
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

        //<editor-fold desc="ImageScene configuration">
        //code for the imageScene
        BorderPane borderPane = new BorderPane();
        imageView = new ImageView();
        borderPane.setCenter(imageView);

        // creating buttons
        Button home = new Button("HOME");
        Button icon = new Button("?");
        Button save = new Button("SAVE");

        // grouping and adding to boarderPane
        HBox buttonsContainer = new HBox(10);
        buttonsContainer.getChildren().addAll(home,save,icon);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonsContainer.setPadding(new Insets(5,20,5,0));
        borderPane.setTop(buttonsContainer);

        // button functionality
        save.setOnAction(e -> saveFile());
        home.setOnAction(e -> {
            window.setScene(menu);
            window.setResizable(false);
        });

        // adding the scene
        imageScene = new Scene(borderPane);
        //</editor-fold>

        imageScene.setOnKeyReleased(e -> {
            String key = e.getCode().toString().toUpperCase();
            System.out.println(key);
            switch (key) {
                case "UP" -> offsetY -= 10 / zoomFactor;
                case "DOWN" -> offsetY += 10 / zoomFactor;
                case "LEFT" -> offsetX -= 10 / zoomFactor;
                case "RIGHT" -> offsetX += 10 / zoomFactor;
                case "MINUS" -> zoomFactor = Math.max(zoomFactor - ZOOM_DELTA, 0.1);
                case "EQUALS", "PLUS" -> zoomFactor += ZOOM_DELTA;
                default -> System.out.println("unregistered key");
            }
            updateImage();
        });
    }

    private void updateImage() {
        System.out.println("offsetX : " + offsetX);
        System.out.println("offsetY : " + offsetY);
        System.out.println("zoomFactor : " + zoomFactor);
    }

    private void saveFile(){
        ZonedDateTime timeNow = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = timeNow.format(formatter);
        try {
            File outputFile = new File(System.getProperty("user.home"), "Downloads/mandelbrot_set_" + time + ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputFile);
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());
            alert.display("Image was saved", "great");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // returns true if the field only contains numbers
    private boolean validateInput(TextField input){
        fieldID = input.getId();
        return input.getText().matches("\\d+");
    }

    // a method that returns the ID of the selected radiobutton to determine the mode
    private String getMode(ToggleGroup group){
        RadioButton selected = (RadioButton) group.getSelectedToggle();
        return selected.getId();
    }

    // passes all variables needed to SetCalculation
    private boolean setVariables(TextField w, TextField h) {

        width = Integer.parseInt(w.getText());
        height = Integer.parseInt(h.getText());

        if (width < 300 || height < 300) {
            alert.display("Minimum size 300x300", "Okay");
            return false;
        }

        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        setMaker.setHeight(height);
        setMaker.setWidth(width);
        return true;
    }

    public static void main(String[] args) {
        launch();
    }
}