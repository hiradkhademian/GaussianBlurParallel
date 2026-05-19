package src;

import java.awt.image.BufferedImage;
import java.util.concurrent.ForkJoinPool;

public class Main {
    // 3x3 Gaussian Kernel definitions
    public static final int[][] KERNEL = {
        {1, 2, 1},
        {2, 4, 2},
        {1, 2, 1}
    };
    public static final int KERNEL_NORMALIZER = 16; 

    // Threshold config: process sequentially if a chunk has fewer than 50 rows
    public static final int ROW_THRESHOLD = 50; 

    public static void main(String[] args) {
        String inputPath = "input.jpg"; 
        String outputPathSeq = "output_sequential.jpg";
        String outputPathFJ = "output_forkjoin.jpg";

        System.out.println("Loading image...");
        BufferedImage originalImage = ImageUtils.loadImage(inputPath);

        if (originalImage == null) {
            System.out.println("Exiting due to image loading failure.");
            return;
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        System.out.println("Image loaded. Resolution: " + width + "x" + height);

        // ==========================================
        // 1. SEQUENTIAL PROCESSING BASELINE (Ts)
        // ==========================================
        BufferedImage blurredImageSeq = ImageUtils.createBlankCopy(originalImage);
        System.out.println("\nStarting sequential blur...");
        long startTimeSeq = System.currentTimeMillis();
        
        SequentialBlur.applyBlur(originalImage, blurredImageSeq);
        
        long endTimeSeq = System.currentTimeMillis();
        long durationSeq = endTimeSeq - startTimeSeq;
        System.out.println("Sequential Processing Time (Ts): " + durationSeq + " ms");
        ImageUtils.saveImage(blurredImageSeq, outputPathSeq, "jpg");

        // ==========================================
        // 2. FORK/JOIN PARALLEL PROCESSING (Tp)
        // ==========================================
        BufferedImage blurredImageFJ = ImageUtils.createBlankCopy(originalImage);
        System.out.println("\nStarting Fork/Join parallel blur...");
        
        // Dynamically discover available CPU processing cores on your Mac
        int availableCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Targeting Concurrency Level (Active Cores): " + availableCores);
        
        // Construct custom pool mapped directly to your hardware footprint
        ForkJoinPool pool = new ForkJoinPool(availableCores);
        
        long startTimeFJ = System.currentTimeMillis();
        
        // Instantiate the top level task, skipping the 1-pixel outermost row border safely
        ForkJoinBlur topLevelTask = new ForkJoinBlur(originalImage, blurredImageFJ, 1, height - 1, ROW_THRESHOLD);
        pool.invoke(topLevelTask);
        
        long endTimeFJ = System.currentTimeMillis();
        long durationFJ = endTimeFJ - startTimeFJ;
        System.out.println("Fork/Join Parallel Processing Time (Tp): " + durationFJ + " ms");
        ImageUtils.saveImage(blurredImageFJ, outputPathFJ, "jpg");

        // Close the thread pool gracefully
        pool.shutdown();

        // ==========================================
        // 3. INITIAL TELEMETRY ANALYSIS
        // ==========================================
        double speedup = (double) durationSeq / durationFJ;
        System.out.println("\n--- Performance Snapshot ---");
        System.out.printf("Empirical Speedup Factor (S): %.2fx%n", speedup);
    }
}