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

    public boolean isFirst = true;
    double dx;
    double dy;
    Image result;
    WritableImage writableImage;
    PixelWriter pixelWriter;
    BlockingQueue<Chunk> queue;
    private int width;
    private int height;
    private double zoom;
    private double centerX;
    private double centerY;
    private boolean render;

    // a method for converting a WritableImage to normal Image
    public static Image convertToImage(WritableImage writableImage) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    //<editor-fold desc="Setters for SetCalculation class">
    public void setWidth(int width) {this.width = width;}

    public void setHeight(int height) {this.height = height;}

    public void setZoom(double zoom) {this.zoom = zoom;}

    public void setRender(boolean render) {this.render = render;}

    public void setCenterX(double centerX) {this.centerX = centerX;}

    public void setCenterY(double centerY) {this.centerY = centerY;}

    //</editor-fold>

    public Image getImage(String mode) {
        //scale x and y factor
        dx = (MAX_X - MIN_X) / width;
        dy = (MAX_Y - MIN_Y) / height;
        dx /= zoom;
        dy /= zoom;

        // create an empty writable image with width and height
        writableImage = new WritableImage(width, height);
        // define pixel-writer for setting color of individual pixel
        pixelWriter = writableImage.getPixelWriter();

        // run in the selected mode
        if (mode.equals("1"))
            runSequential();
        if (mode.equals("2"))
            runParallel();
        if (mode.equals("3"))
            runDistributed();
        // return the image once it was computed
        return result;
    }

    public void performanceRun(String mode) {
        //scale x and y factor
        dx = (MAX_X - MIN_X) / width;
        dy = (MAX_Y - MIN_Y) / height;
        dx /= zoom;
        dy /= zoom;

        if (isFirst)
            System.out.println("""
                                       -----------------!DISCLAIMER!--------------------
                                               
                                       Running without displaying the image. The result
                                       of the calculations gets discarded. This mode is
                                       meant only for performance tests.
                                       """);

        // run in the selected mode
        if (mode.equals("1"))
            runSequential();
        if (mode.equals("2"))
            runParallel();
        if (mode.equals("3"))
            runDistributed();
    }

    public void runSequential() {

        long start = System.currentTimeMillis();
        for (int pixelX = 0; pixelX < width; pixelX++) {
            for (int pixelY = 0; pixelY < height; pixelY++) {
                double x = MIN_X + pixelX * dx;
                double y = MIN_Y + pixelY * dy;

                int iterations = computeSet(x, y);

                if (render)
                    pixelWriter.setColor(pixelX, pixelY, getColor(iterations));
            }
        }
        long end = System.currentTimeMillis();
        if (!render)
            printStats("1", 1, width, height, MAX_ITERATIONS, (end - start));
        if (render)
            result = convertToImage(writableImage);
    }

    public void runParallel() {

        List<Thread> threads = new ArrayList<>();
        int numThreads = Runtime.getRuntime().availableProcessors();

        final int chunkSize = 30; // chunkSize = 30 ==> 30x30 pixels
        int numChunksX = (int) Math.ceil((double) width / chunkSize);
        int numChunksY = (int) Math.ceil((double) height / chunkSize);
        queue = new ArrayBlockingQueue<>(numChunksY * numChunksX, true);

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
                Chunk chunk;
                while ((chunk = queue.poll()) != null) {
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
        if (!render)
            printStats("2", numThreads, width, height, MAX_ITERATIONS, (end - start));
        if (render)
            result = convertToImage(writableImage);
    }

    public void runDistributed() {

        int cores = Runtime.getRuntime().availableProcessors();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/zsh", "./mpjScript.zsh", String.valueOf(cores), String.valueOf(width), String.valueOf(height), String.valueOf(MAX_ITERATIONS));

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
            int maxIter = Integer.parseInt(lines[3].replace("Iteration limit: ", ""));
            long computationTime = Long.parseLong(lines[4].replace("Computation time: ", "").replace("ms", ""));

            if (!render)
                printStats("3", coresUsed, width, height, maxIter, computationTime);

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
                if (render)
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
        if (iterations >= MAX_ITERATIONS)
            return Color.RED;
        else
            return Color.ALICEBLUE;
    }

    public void printStats(String mode, int res, int w, int h, int maxIter, long time) {

        String mode_str;
        String var = "";

        switch (mode) {
        case "1" -> mode_str = "sequential";
        case "2" -> {
            mode_str = "parallel";
            var = "threads";
        }
        case "3" -> {
            mode_str = "distributed";
            var = "nodes";
        }
        default -> {
            System.out.println("mode ID unknown");
            return;
        }
        }
        System.out.println("\n---------------------STATS------------------------");
        System.out.println("Computation mode: " + mode_str);
        if (!mode.equals("1"))
            System.out.println("Number of " + var + " used: " + res);
        System.out.println("Width: " + w + "px");
        System.out.println("Height: " + h + "px");
        System.out.println("Iteration limit: " + maxIter);
        System.out.println("Computation time: " + time + "ms");
        System.out.println("---------------------------------------------------\n\n");
    }
    //</editor-fold>

    private record Chunk(int startX, int startY, int endX, int endY) {}
}