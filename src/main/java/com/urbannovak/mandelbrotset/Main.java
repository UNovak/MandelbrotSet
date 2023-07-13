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
    private double centerX;
    private double centerY;
    private double zoom = 1.0;
    private static final double ZOOM_DELTA = 0.1;
    private String runMode = "1";
    private boolean render = false;
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
    ToggleButton displayImage;
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
        displayImage = new ToggleButton("NO");
        displayImage.setSelected(false);
        displayImage.setId("0");

        //<editor-fold desc="Size input">
        // label creation and styling
        Label sizeQuestion = new Label("How big should the image be?");
        sizeQuestion.setPadding(new Insets(0, 0, 10, 0));

        // input field creation and styling
        HBox sizeInput = new HBox(5);
        Text widthPrompt = new Text("width:");
        TextField widthInput = new TextField("800");
        widthInput.setId("width");
        widthInput.setMaxSize(50, 20);
        Text heightPrompt = new Text("height:");
        TextField heightInput = new TextField("600");
        heightInput.setId("height");
        heightInput.setMaxSize(50, 20);
        sizeInput.getChildren().addAll(widthPrompt, widthInput, heightPrompt, heightInput);
        sizeInput.setAlignment(Pos.CENTER);

        // adding size elements together
        sizeAll = new VBox(5);
        sizeAll.setAlignment(Pos.CENTER);
        sizeAll.getChildren().addAll(sizeQuestion, sizeInput);
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
        sequential.setId("1");
        sequential.setSelected(true); // default selected run mode == parallel

        parallel = new RadioButton("parallel");
        parallel.getStyleClass().remove("radio-button");
        parallel.getStyleClass().add("toggle-button");
        parallel.setId("2");

        distributed = new RadioButton("distributed");
        distributed.getStyleClass().remove("radio-button");
        distributed.getStyleClass().add("toggle-button");
        distributed.setId("3");

        distributed.setToggleGroup(modeGroup);
        parallel.setToggleGroup(modeGroup);
        sequential.setToggleGroup(modeGroup);

        // add all buttons to parent
        hbox.getChildren().addAll(sequential, parallel, distributed);
        //</editor-fold>

        //<editor-fold desc="create horizontal lines to divide the menu">
        SplitPane br1 = new SplitPane();
        SplitPane br2 = new SplitPane();
        SplitPane br3 = new SplitPane();
        //</editor-fold>

        // start button
        Button start = new Button("START");
        start.setMinSize(80, 40);
        start.setTextFill(Color.RED);


        //menu components layout
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        // joins all components of the menu in a vertical box layout
        vbox.getChildren().addAll(displayTxt, displayImage, br1, sizeAll, br2, modeTxt, hbox, br3, start);

        //<editor-fold desc="ImageScene configuration">
        BorderPane borderPane = new BorderPane();
        imageView = new ImageView();
        borderPane.setCenter(imageView);

        // creating buttons
        Button home = new Button("HOME");
        Button icon = new Button("?");
        Button save = new Button("SAVE");

        // grouping and adding to boarderPane
        HBox buttonsContainer = new HBox(10);
        buttonsContainer.getChildren().addAll(home, save, icon);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonsContainer.setPadding(new Insets(5, 20, 5, 0));
        borderPane.setTop(buttonsContainer);

        imageScene = new Scene(borderPane);
        //</editor-fold>

        // menu scene actions
        distributed.setOnAction(e -> updateButtonStates(modeGroup, distributed.getId()));
        sequential.setOnAction(e -> updateButtonStates(modeGroup, sequential.getId()));
        parallel.setOnAction(e -> updateButtonStates(modeGroup, parallel.getId()));
        displayImage.setOnAction(e -> updateButtonStates(modeGroup, displayImage.getId()));
        start.setOnAction(e -> {
            // check if width end height are only ints
            if (!setVariables(widthInput, heightInput))
                e.consume(); // failed to set width/height

            // all variables were ok
            if (render) {
                image = setMaker.getImage(runMode);
                imageView.setImage(image);
                window.setScene(imageScene);
                window.setResizable(true);
            }

            setMaker.performanceRun(runMode);
            if (setMaker.isFirst)
                alert.display("Check console for performance info", "OK");

        });

        // image scene actions
        save.setOnAction(e -> saveFile());
        home.setOnAction(e -> {
            window.setScene(menu);
            window.setResizable(false);
        });
        imageScene.setOnKeyReleased(e -> {
            String key = e.getCode().toString().toUpperCase();
            switch (key) {
            case "UP" -> centerY += 50 / zoom;
            case "DOWN" -> centerY -= 50 / zoom;
            case "LEFT" -> centerX += 50 / zoom;
            case "RIGHT" -> centerX -= 50 / zoom;
            case "MINUS" -> zoom = Math.max(zoom - ZOOM_DELTA, 0.1);
            case "EQUALS", "PLUS" -> zoom += ZOOM_DELTA;
            default -> System.out.println("unregistered key");
            }
            updatePosition();
        });

        menu = new Scene(vbox, 250, 300);
        window.setScene(menu);
        window.show();
    }

    // helper methods

    private void updatePosition() {

        setMaker.setCenterX(centerX);
        setMaker.setCenterY(centerY);
        setMaker.setZoom(zoom);
        image = setMaker.getImage("2");
        imageView.setImage(image);
    }

    private void saveFile() {
        ZonedDateTime timeNow = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String time = timeNow.format(formatter);
        try {
            File outputFile = new File(System.getProperty("user.home"), "Downloads/mandelbrot_set_" + time + ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputFile);
            System.out.println("Image saved to: " + outputFile.getAbsolutePath());
            if (render)
                alert.display("Image was saved", "great");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        // returns all the parameters the original value
        centerX = 0.0;
        centerY = 0.0;
        zoom = 1.0;
        window.setResizable(false);
        window.setScene(menu);
    }

    // attempts to assign new width and height
    // fails if input is not strictly an integer
    // fails if size too small
    // displays an alert with error message when fail
    private boolean setVariables(TextField w, TextField h) {

        if (validateInput(w) && validateInput(h)) { // both fields are ints

            int newWidth = Integer.parseInt(w.getText());
            int newHeight = Integer.parseInt(h.getText());

            if (newHeight < 300 || newWidth < 300) {
                // enters is size too small
                alert.display("Image size too small", "Okay");
                return false;
            }

            // passed all variable tests
            // assign updated values
            width = newWidth;
            height = newHeight;

            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            setMaker.setHeight(height);
            setMaker.setWidth(width);
            setMaker.setZoom(zoom);
            setMaker.setCenterX(centerX);
            setMaker.setCenterY(centerY);
            setMaker.setRender(render);
            return true;
        }
        alert.display(fieldID + " is not a number", "Okay");
        return false;
    }

    private boolean validateInput(TextField input) {
        fieldID = input.getId();
        return input.getText().matches("\\d+");
    }

    // this method gets called when one of the toggle buttons or displayImage is pressed
    private void updateButtonStates(ToggleGroup group, String ID) {
        // display image changed
        if (ID.equals("0")) {

            if (displayImage.isSelected()) {
                render = true;
                displayImage.setText("YES");
                distributed.setDisable(true);
            } else {
                distributed.setDisable(false);
                render = false;
                displayImage.setText("NO");
            }
        } else {
            if (ID.equals("3")) { // distributed
                render = false;
                displayImage.setText("NO");
                displayImage.setDisable(true);
                displayImage.setSelected(false);
                runMode = ID;
            } else {
                runMode = ID;
                displayImage.setDisable(false);
                displayImage.setSelected(false);
            }
        }
    }

    // called when running the program
    public static void main(String[] args) {
        launch();
    }
}