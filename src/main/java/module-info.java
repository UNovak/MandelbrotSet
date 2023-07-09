module com.urbannovak.mandelbrotset {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;

    opens com.urbannovak.mandelbrotset to javafx.fxml;
    exports com.urbannovak.mandelbrotset;
}