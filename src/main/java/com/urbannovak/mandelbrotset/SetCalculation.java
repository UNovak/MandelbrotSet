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

public class SetCalculation {

    private record Chunk(int startX, int startY, int endX, int endY) {}
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
    BlockingQueue<Chunk> queue;

    //<editor-fold desc="Setters for SetCalculation class">
    public void setWidth(int width) {this.width = width;}

    public void setHeight(int height) {this.height = height;}

    public void setZoom(double zoom) {this.zoom = zoom;}
    //</editor-fold>

    public Image run(String mode){

        //scale x and y to always render x[-2,1] and y[-1.5,1.5]
        dx = (MAX_X - MIN_X) / width;
        dy = (MAX_Y - MIN_Y) / height;

        // create an empty writable image with width and height
        writableImage = new WritableImage(width,height);
        // define pixel-writer for setting color of individual pixel
        pixelWriter = writableImage.getPixelWriter();

        // run in the selected mode
        if (mode.equals("sequential")) runSequential();
        if (mode.equals("parallel")) runParallel();
        if (mode.equals("distributed")) runDistributed();

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
        System.out.println("sequential computation time: " + (end - start));
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
        System.out.println("parallel computation time: " + (end - start));
        result = convertToImage(writableImage);
    }

    // threads get Chunks from queue and call this method
    // set the x and y bounds of the part of the image to process
    // do the escape time on each pixel in the chunk and set the colour
    private void processChunk(Chunk chunk) {
        for (int pixelX = chunk.startX; pixelX < chunk.endX; pixelX++) {
            for (int pixelY = chunk.startY; pixelY < chunk.endY; pixelY++) {
                // calculate the coordinates as if the image had 0,0 in the center
                double x = MIN_X + pixelX * dx;
                double y = MIN_Y + pixelY * dy;

                int iterations = computeSet(x, y); // get the amount of needed iterations
                pixelWriter.setColor(pixelX, pixelY, getColor(iterations)); // set colour
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

    public void runDistributed() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/zsh", "./mpjScript.zsh", "6", String.valueOf(width), String.valueOf(height));
            processBuilder.directory(new File(System.getProperty("user.dir") + "/src/main/java/distributed"));
            Process process = processBuilder.start();
            process.waitFor();

            // Read the computation time result from the file
            Path timeFilePath = Path.of("src", "main", "java", "distributed", "time.txt");
            String computationTimeStr = Files.readString(timeFilePath);
            if (!computationTimeStr.isEmpty()) {
                long computationTime = Long.parseLong(computationTimeStr);
                System.out.println("Computation Time: " + computationTime + "ms");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}