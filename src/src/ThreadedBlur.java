package src;

import java.awt.image.BufferedImage;

public class ThreadedBlur implements Runnable {
    private final BufferedImage src;
    private final BufferedImage dest;
    private final int startRow;
    private final int endRow;

    public ThreadedBlur(BufferedImage src, BufferedImage dest, int startRow, int endRow) {
        this.src = src;
        this.dest = dest;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public void run() {
        int width = src.getWidth();
        int[][] kernel = Main.KERNEL;
        int normalizer = Main.KERNEL_NORMALIZER;
        int kernelSize = kernel.length;
        int offsetStart = -((kernelSize - 1) / 2);
        int offsetEnd = offsetStart + kernelSize - 1;

        // Thread safely crunches ONLY its statically assigned block of rows
        for (int y = startRow; y < endRow; y++) {
            for (int x = kernelSize / 2; x < width - (kernelSize / 2); x++) {
                
                int redSum = 0;
                int greenSum = 0;
                int blueSum = 0;

                // Spatial convolution matrix overlay
                for (int ky = offsetStart; ky <= offsetEnd; ky++) {
                    for (int kx = offsetStart; kx <= offsetEnd; kx++) {
                        int rgb = src.getRGB(x + kx, y + ky);
                        
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        int weight = kernel[ky - offsetStart][kx - offsetStart];

                        redSum += r * weight;
                        greenSum += g * weight;
                        blueSum += b * weight;
                    }
                }

                int finalR = redSum / normalizer;
                int finalG = greenSum / normalizer;
                int finalB = blueSum / normalizer;

                int blurredPixel = (0xFF << 24) | (finalR << 16) | (finalG << 8) | finalB;
                dest.setRGB(x, y, blurredPixel);
            }
        }
    }

    // Static orchestrator utility to partition the rows and manage thread lifecycle
    public static void applyBlur(BufferedImage src, BufferedImage dest, int threadCount) {
        int height = src.getHeight();
        Thread[] threads = new Thread[threadCount];
        int kernelSize = Main.KERNEL.length;
        int border = kernelSize / 2;
        
        // Calculate the height of each thread's slice (skipping the outer border margin)
        int totalRowsToProcess = height - (2 * border);
        int rowsPerThread = totalRowsToProcess / threadCount;
        int remainingRows = totalRowsToProcess % threadCount; // Catch any remainder rows

        int currentStartRow = border;

        // Allocate workloads statically across the native thread array
        for (int i = 0; i < threadCount; i++) {
            int currentEndRow = currentStartRow + rowsPerThread;
            
            // The last thread absorbs any leftover remainder rows to ensure full coverage
            if (i == threadCount - 1) {
                currentEndRow += remainingRows;
            }

            // Construct the worker and spawn the OS-level thread
            threads[i] = new Thread(new ThreadedBlur(src, dest, currentStartRow, currentEndRow));
            threads[i].start();

            currentStartRow = currentEndRow;
        }

        // Main thread waits for all concurrent workers to join back before completing
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Thread synchronization was interrupted.");
            e.printStackTrace();
        }
    }
}