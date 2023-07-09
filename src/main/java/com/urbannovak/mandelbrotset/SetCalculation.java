package com.urbannovak.mandelbrotset;

import javafx.scene.image.Image;

public class SetCalculation {

    private final static int MAX_ITERATIONS = 1000;
    private double WIDTH;
    private double HEIGHT;
    private double zoom;
    private double XCENTER;
    private double YCENTER;
    Image image;

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

    public Image run(String mode){
        if (mode.equals("sequential")) runSequential();
        if (mode.equals("parallel")) runParallel();
        if (mode.equals("distributed")) runDistributed();
        return image;
    }

    public void runSequential(){
        System.out.println("look for image in sequential");
        image = new Image("/mandelbrot1.jpeg");
    }

    public void runParallel(){
        System.out.println("look for image in parallel");
        image = new Image("/image.png");
    }

    public void runDistributed() {
        System.out.println("look for image in distributed");
        image = new Image("/image.png");
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
