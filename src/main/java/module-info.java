module com.urbannovak.mandelbrotset {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.urbannovak.mandelbrotset to javafx.fxml;
    exports com.urbannovak.mandelbrotset;
}