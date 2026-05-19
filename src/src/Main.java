package src;

import java.awt.image.BufferedImage;

public class Main {
    // 3x3 Gaussian Kernel definitions [cite: 39, 40, 41, 42]
    public static final int[][] KERNEL = {
        {1, 2, 1},
        {2, 4, 2},
        {1, 2, 1}
    };
    public static final int KERNEL_NORMALIZER = 16; // Sum of kernel weights [cite: 44]

    public static void main(String[] args) {
        // Replace with a path to a test image on your Mac (e.g., a 1080p or 4K .jpg/.png) [cite: 11]
        String inputPath = "input.jpg"; 
        String outputPathSeq = "output_sequential.jpg";

        System.out.println("Loading image...");
        BufferedImage originalImage = ImageUtils.loadImage(inputPath);

        if (originalImage == null) {
            System.out.println("Exiting due to image loading failure.");
            return;
        }

        System.out.println("Image loaded successfully. Resolution: " 
            + originalImage.getWidth() + "x" + originalImage.getHeight());

        // --- Sequential Processing Execution Block ---
        long startTime = System.currentTimeMillis(); // [cite: 59, 75]
        
        // TODO: Invoke the sequential blur algorithm here [cite: 45]
        BufferedImage blurredImageSeq = ImageUtils.createBlankCopy(originalImage);
        
        long endTime = System.currentTimeMillis(); // [cite: 59, 75]
        System.out.println("Sequential Processing Time: " + (endTime - startTime) + " ms"); // [cite: 59, 75]

        // Save output to verify mathematical accuracy [cite: 10, 58]
        ImageUtils.saveImage(blurredImageSeq, outputPathSeq, "jpg");
    }
}