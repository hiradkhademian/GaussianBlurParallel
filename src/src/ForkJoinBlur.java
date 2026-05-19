package src;

import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;

public class ForkJoinBlur extends RecursiveAction {
    private final BufferedImage src;
    private final BufferedImage dest;
    private final int startRow;
    private final int endRow;
    private final int threshold;

    public ForkJoinBlur(BufferedImage src, BufferedImage dest, int startRow, int endRow, int threshold) {
        this.src = src;
        this.dest = dest;
        this.startRow = startRow;
        this.endRow = endRow;
        this.threshold = threshold;
    }

    @Override
    protected void compute() {
        int rowCount = endRow - startRow;

        // Base Case: If the workload is small enough, compute sequentially
        if (rowCount <= threshold) {
            computeSequentially();
        } else {
            // Divide Step: Find the midpoint of the current row range
            int midRow = startRow + (rowCount / 2);

            ForkJoinBlur topHalf = new ForkJoinBlur(src, dest, startRow, midRow, threshold);
            ForkJoinBlur bottomHalf = new ForkJoinBlur(src, dest, midRow, endRow, threshold);

            // Fork and Join: Run both subtasks in parallel
            invokeAll(topHalf, bottomHalf);
        }
    }

    // Leverages the exact same mathematical convolution loop as the sequential baseline
    private void computeSequentially() {
        int width = src.getWidth();
        int[][] kernel = Main.KERNEL;
        int normalizer = Main.KERNEL_NORMALIZER;

        // Process only the rows assigned to this specific fork/chunk
        for (int y = startRow; y < endRow; y++) {
            for (int x = 1; x < width - 1; x++) {
                
                int redSum = 0;
                int greenSum = 0;
                int blueSum = 0;

                // 3x3 Convolution matrix overlay
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = src.getRGB(x + kx, y + ky);
                        
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        int weight = kernel[ky + 1][kx + 1];

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
}