package com.urbannovak.mandelbrotset;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.urbannovak.mandelbrotset.Constants.*;

public class SetCalculation {

    private record Chunk(int startX, int startY, int endX, int endY) {}
    private int width;
    private int height;
    private double zoom;
    private double centerX;
    private double centerY;
    double dx;
    double dy;

    Image result;
    WritableImage writableImage;
    PixelWriter pixelWriter;
    BlockingQueue<Chunk> queue;
    private boolean render;

    //<editor-fold desc="Setters for SetCalculation class">
    public void setWidth(int width) {this.width = width;}

    public void setHeight(int height) {this.height = height;}

    public void setZoom(double zoom) {this.zoom = zoom;}

    public void setRender(boolean render) {this.render = render;}

    public void setCenterX(double centerX) {this.centerX = centerX;}

    public void setCenterY(double centerY) {this.centerY = centerY;}

    //</editor-fold>

    public Image getImage(String mode){

        //scale x and y factor
        dx = (MAX_X - MIN_X) / width;
        dy = (MAX_Y - MIN_Y) / height;
        dx /= zoom;
        dy /= zoom;

        // create an empty writable image with width and height
        writableImage = new WritableImage(width,height);
        // define pixel-writer for setting color of individual pixel
        pixelWriter = writableImage.getPixelWriter();

        // run in the selected mode
        if (mode.equals("1")) runSequential();
        if (mode.equals("2")) runParallel();
        if (mode.equals("3")) runDistributed();

        // return the image once it was computed
        return result;
    }

    public void runSequential(){
        System.out.println("running sequential");
        long start = System.currentTimeMillis();
        for (int pixelX = 0; pixelX < width; pixelX++) {
            for (int pixelY = 0; pixelY < height; pixelY++) {
                double x = MIN_X + pixelX * dx;
                double y = MIN_Y + pixelY * dy;

                int iterations = computeSet(x, y);
                pixelWriter.setColor(pixelX, pixelY, getColor(iterations));
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("sequential computation time: " + (end - start) + "ms");
        result = convertToImage(writableImage);
    }

    public void runParallel() {

        System.out.println("running parallel"); // debugging

        List<Thread> threads = new ArrayList<>();
        int numThreads = Runtime.getRuntime().availableProcessors();

        final int chunkSize = 30; // chunkSize = 30 ==> 30x30 pixels
        int numChunksX = (int) Math.ceil((double) width / chunkSize);
        int numChunksY = (int) Math.ceil((double) height / chunkSize);
        queue = new ArrayBlockingQueue<>(numChunksY * numChunksX,true);

        // Populate the chunk queue
        for (int chunkX = 0; chunkX < numChunksX; chunkX++) {
            for (int chunkY = 0; chunkY < numChunksY; chunkY++) {
                int startX = chunkX * chunkSize;
                int endX = Math.min(startX + chunkSize, width); // ensure not crossing width
                int startY = chunkY * chunkSize;
                int endY = Math.min(startY + chunkSize, height); // ensure not crossing height

                Chunk chunk = new Chunk(startX, startY, endX, endY); // create a new chunk
                queue.offer(chunk); // add chunk to queue, fails if queue is full
            }
        }

        // Start worker threads
        // while loops until queue is empty
        // all threads take jobs from queue until its empty
        long start = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(() -> {
                while (!queue.isEmpty()) {
                    Chunk chunk = queue.poll(); // get chunk to compute
                    processChunk(chunk); // go to process this chunk
                }
            });
            threads.add(thread);    // add the newly created chunk to list for keeping track
            thread.start();     // start the thread
        }

        // Wait for all threads to finish
        try {
            // calls Thread.join() on each of the threads in the list
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long end = System.currentTimeMillis(); // all the threads are done
        System.out.println("parallel computation time: " + (end - start) + "ms");
        result = convertToImage(writableImage);
    }

    public void runDistributed() {
        System.out.println("running distributed");

        int cores = Runtime.getRuntime().availableProcessors();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/zsh", "./mpjScript.zsh", String.valueOf(cores), String.valueOf(width), String.valueOf(height));
            processBuilder.directory(new File(System.getProperty("user.dir") + "/src/main/java/distributed"));
            Process process = processBuilder.start();
            process.waitFor();

            // Read the computation time result from the file
            Path timeFilePath = Path.of("src", "main", "java", "distributed", "time.txt");
            String timeFileContent = Files.readString(timeFilePath);
            String[] lines = timeFileContent.split("\n");

            int coresUsed = Integer.parseInt(lines[0].replace("Number of cores used: ", ""));
            int width = Integer.parseInt(lines[1].replace("Width: ", ""));
            int height = Integer.parseInt(lines[2].replace("Height: ", ""));
            long computationTime = Long.parseLong(lines[3].replace("Computation time: ", "").replace("ms", ""));

            System.out.println("Number of cores used: " + coresUsed);
            System.out.println("Width: " + width);
            System.out.println("Height: " + height);
            System.out.println("Computation time: " + computationTime + "ms");
            System.out.println("\n\n-----------------!DISCLAIMER!----------------\n\n" +
                                "The distributed method does nothing with the data\n" +
                                "It just calculates values for colors but does not set them");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    //<editor-fold desc="Helper functions">
    // threads get Chunks from queue and call this method
    // set the x and y bounds of the part of the image to process
    // do the escape time on each pixel in the chunk and set the colour
    private void processChunk(Chunk chunk) {
        for (int pixelX = chunk.startX; pixelX < chunk.endX; pixelX++) {
            for (int pixelY = chunk.startY; pixelY < chunk.endY; pixelY++) {
                // calculate the coordinates as if the image had 0,0 in the center
                double x = MIN_X + (pixelX - centerX) * dx / zoom;
                double y = MIN_Y + (pixelY - centerY) * dy / zoom;

                int iterations = computeSet(x, y); // get the amount of needed iterations
                pixelWriter.setColor(pixelX, pixelY, getColor(iterations)); // set color
            }
        }
    }

    // standard escape time algorithm for plotting the mandelbrot set
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

    // returns the color based on the amount of used iterations
    private Color getColor(int iterations) {
        if(iterations >= MAX_ITERATIONS) return Color.RED;
        else return Color.ALICEBLUE;
    }

    // a method for converting a WritableImage to normal Image
    public static Image convertToImage(WritableImage writableImage) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
    //</editor-fold>
}

// TODO
// adjust rendering methods in a way that if rendering is toggled off, the methods do not set the pixel values

// TODO
// adjust gui in a whay that when distributed is selected rendering can not be toggled on

// TODO
// adjust the alerts so that when rendering is toggled of the alert just tels the user to look at the console.