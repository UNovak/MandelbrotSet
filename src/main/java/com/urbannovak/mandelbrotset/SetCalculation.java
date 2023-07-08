package com.urbannovak.mandelbrotset;

import javafx.scene.image.ImageView;

public class SetCalculation {

    private final static int MAX_ITERATIONS = 1000;
    private double WIDTH;
    private double HEIGHT;
    private double zoom;
    private double XCENTER;
    private double YCENTER;
    ImageView setImage = new ImageView();

    //<editor-fold desc="Setters for SetCalculation class">
    public void setWIDTH(double WIDTH) {
        this.WIDTH = WIDTH;
    }

    public void setHEIGHT(double HEIGHT) {
        this.HEIGHT = HEIGHT;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setXCENTER(double XCENTER) {
        this.XCENTER = XCENTER;
    }

    public void setYCENTER(double YCENTER) {
        this.YCENTER = YCENTER;
    }
    //</editor-fold>

    public void run(String mode){

    }

    public void runSequential(){

    }

    public void runParallel(){

    }

    public void runDistributed(){

    }

    private int getColor(int iterations) {
        if (iterations == MAX_ITERATIONS) {
            return 0xFF000000;  // Black
        } else {
            float hue = (float) iterations / MAX_ITERATIONS;
            return java.awt.Color.HSBtoRGB(hue, 1, 1);
        }
    }

}
