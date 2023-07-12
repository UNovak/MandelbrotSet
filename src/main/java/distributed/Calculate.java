import mpi.MPI;
import mpi.MPIException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Calculate {
    public static void main(String[] args) throws MPIException {
        MPI.Init(args);

        long start = System.currentTimeMillis(); // Start the computation timer

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int width = Integer.parseInt(args[3]);
        int height = Integer.parseInt(args[4]);

        // Calculate the range of values each process will compute
        int startRow = rank * (height / size);
        int endRow = (rank + 1) * (height / size);

        // Calculate the Mandelbrot set for each pixel in the range
        for (int y = startRow; y < endRow; y++) {
            for (int x = 0; x < width; x++) {
                double zx = 0;
                double zy = 0;
                double cx = (x - width / 2.0) * 4.0 / width;  // Scale and shift x coordinate
                double cy = (y - height / 2.0) * 4.0 / width; // Scale and shift y coordinate
                int iterations = 0;
                while (zx * zx + zy * zy < 4 && iterations < 1000) {
                    double xtemp = zx * zx - zy * zy + cx;
                    zy = 2.0 * zx * zy + cy;
                    zx = xtemp;
                    iterations++;
                }

                // Color the pixel based on the number of iterations
                int color = (iterations == 1000) ? 0x000000 : 0xFFFFFF;
                // Set the color value in your image representation

                // Print the pixel coordinates and color value
                // System.out.println("Pixel: (" + x + ", " + y + ") Color: " + color);
            }
        }

        long end = System.currentTimeMillis(); // Stop the computation timer
        long computationTime = end - start; // Compute the total computation time

        // Write the computation time result to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("time.txt", false))) {
            // Write computation time to the file
            writer.write(String.valueOf(computationTime));
        } catch (IOException e) {
            e.printStackTrace();
        }

        MPI.Finalize();
    }
}
