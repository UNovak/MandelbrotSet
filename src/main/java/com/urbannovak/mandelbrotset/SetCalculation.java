package com.urbannovak.mandelbrotset;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

public class SetCalculation {

    private final static int MAX_ITERATIONS = 500;
    private static final double MIN_X = -2.0;
    private static final double MAX_X = 1.0;
    private static final double MIN_Y = -1.5;
    private static final double MAX_Y = 1.5;
    private double zoom;
    double dx;
    double dy;
    private int width;
    private int height;

    Image result;
    WritableImage writableImage;
    PixelWriter pixelWriter;

    //<editor-fold desc="Setters for SetCalculation class">
    public void setWidth(int width) {this.width = width;}

    public void setHeight(int height) {this.height = height;}

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    //</editor-fold>

    public Image run(String mode){

        dx = (MAX_X - MIN_X) / width;
        dy = (MAX_Y - MIN_Y) / height;
        writableImage = new WritableImage(width,height);
        pixelWriter = writableImage.getPixelWriter();

        if (mode.equals("sequential")) runSequential();
        if (mode.equals("parallel")) runParallel();
        if (mode.equals("distributed")) runDistributed();
        return result;
    }

    public void runSequential(){

        for (int pixelX = 0; pixelX < width; pixelX++) {
            for (int pixelY = 0; pixelY < height; pixelY++) {
                double x = MIN_X + pixelX * dx;
                double y = MIN_Y + pixelY * dy;

                int iterations = computeSet(x, y);
                pixelWriter.setColor(pixelX, pixelY, getColor(iterations));
            }
        }
        result = convertToImage(writableImage);

    }

    public void runParallel(){
        System.out.println("running parallel");

    }

    public void runDistributed() {
        System.out.println("running distributed");

    }

    private int computeSet(double x, double y) {
        double zx = x;
        double zy = y;

        int iterations = 0;
        while (iterations < MAX_ITERATIONS && (zx * zx + zy * zy) < 4) {
            double xTemp = zx * zx - zy * zy + x;
            zy = 2 * zx * zy + y;
            zx = xTemp;
            iterations++;
        }
        return iterations;
    }


    private Color getColor(int iterations) {
        if(iterations >= MAX_ITERATIONS) return Color.RED;
        else return Color.ALICEBLUE;
    }

    public static Image convertToImage(WritableImage writableImage) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
