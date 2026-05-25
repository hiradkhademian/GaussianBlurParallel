package src;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;

public class Main {
    // 10x10 Gaussian-like Kernel definitions (stronger blur)
    public static final int[] KERNEL_1D = {1, 4, 10, 16, 19, 19, 16, 10, 4, 1};
    public static final int KERNEL_NORMALIZER = 10000;
    public static final int[][] KERNEL = generateKernel();
    public static final int KERNEL_MARGIN = KERNEL.length / 2;

    // Threshold config: process sequentially if a chunk has fewer than 50 rows
    public static final int ROW_THRESHOLD = 50;

    private static int[][] generateKernel() {
        int size = KERNEL_1D.length;
        int[][] kernel = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                kernel[row][col] = KERNEL_1D[row] * KERNEL_1D[col];
            }
        }
        return kernel;
    } 

    public static void main(String[] args) {
        String outputDir = "output";
        try {
            Path outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                System.out.println("Created output directory: " + outputDir);
            }
        } catch (IOException e) {
            System.err.println("Error: Could not create output directory \"output\"");
            e.printStackTrace();
            return;
        }

        if (args.length == 0) {
            System.out.println("No input arguments provided. Using default: input.jpg");
            processImage("input.jpg", outputDir);
            return;
        }

        for (String inputPath : args) {
            processImage(inputPath, outputDir);
        }
    }

    private static void processImage(String inputPath, String outputDir) {
        System.out.println("\nLoading image: " + inputPath);
        BufferedImage originalImage = ImageUtils.loadImage(inputPath);

        if (originalImage == null) {
            System.out.println("Skipping " + inputPath + " due to load failure.");
            return;
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        System.out.println("Image loaded. Resolution: " + width + "x" + height);

        String baseName = Paths.get(inputPath).getFileName().toString();
        int dotIndex = baseName.lastIndexOf('.');
        String shortName = dotIndex > 0 ? baseName.substring(0, dotIndex) : baseName;
        
        // Define all three output target paths
        String outputPathSeq = outputDir + "/" + shortName + "_sequential.jpg";
        String outputPathFJ = outputDir + "/" + shortName + "_forkjoin.jpg";
        String outputPathThreads = outputDir + "/" + shortName + "_threaded.jpg";

        int availableCores = Runtime.getRuntime().availableProcessors();
        System.out.println("System Hardware Concurrency Footprint: " + availableCores + " Cores.");

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
        // 2. FORK/JOIN PARALLEL PROCESSING (Tp_fj)
        // ==========================================
        BufferedImage blurredImageFJ = ImageUtils.createBlankCopy(originalImage);
        System.out.println("\nStarting Fork/Join parallel blur...");
        ForkJoinPool pool = new ForkJoinPool(availableCores);
        long startTimeFJ = System.currentTimeMillis();
        ForkJoinBlur topLevelTask = new ForkJoinBlur(originalImage, blurredImageFJ, KERNEL_MARGIN, height - KERNEL_MARGIN, ROW_THRESHOLD);
        pool.invoke(topLevelTask);
        long endTimeFJ = System.currentTimeMillis();
        long durationFJ = endTimeFJ - startTimeFJ;
        System.out.println("Fork/Join Parallel Processing Time (Tp_fj): " + durationFJ + " ms");
        ImageUtils.saveImage(blurredImageFJ, outputPathFJ, "jpg");
        pool.shutdown();

        // ==========================================
        // 3. NATIVE JAVA THREADING ENGINE (Tp_threads)
        // ==========================================
        BufferedImage blurredImageThreads = ImageUtils.createBlankCopy(originalImage);
        System.out.println("\nStarting Native Thread parallel blur...");
        long startTimeThreads = System.currentTimeMillis();
        
        // Invoke Fatma's static partitioning engine
        ThreadedBlur.applyBlur(originalImage, blurredImageThreads, availableCores);
        
        long endTimeThreads = System.currentTimeMillis();
        long durationThreads = endTimeThreads - startTimeThreads;
        System.out.println("Native Threading Processing Time (Tp_threads): " + durationThreads + " ms");
        ImageUtils.saveImage(blurredImageThreads, outputPathThreads, "jpg");

        // ==========================================
        // 4. COMPARATIVE TELEMETRY MATRIX
        // ==========================================
        double speedupFJ = (double) durationSeq / durationFJ;
        double speedupThreads = (double) durationSeq / durationThreads;

        System.out.println("\n=============================================");
        System.out.println("          EMPIRICAL PERFORMANCE MATRIX       ");
        System.out.println("=============================================");
        System.out.printf("Fork/Join Speedup Factor (S_fj):        %.2fx%n", speedupFJ);
        System.out.printf("Native Threading Speedup Factor (S_th): %.2fx%n", speedupThreads);
        System.out.println("=============================================");
        System.out.println("Saved outputs to: " + outputDir + "/ directory.");
    }
}