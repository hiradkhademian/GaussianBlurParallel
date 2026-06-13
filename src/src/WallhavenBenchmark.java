package src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class WallhavenBenchmark {
    
    private static final String ROOT_DIR = ".";
    private static final String[] WALLHAVEN_FILES = {
        "wallhaven-kxd6d6_1920x1088.png",
        "wallhaven-kxd6d6_3840x2176.png",
        "wallhaven-kxd6d6_5120x2880.png",
        "wallhaven-kxd6d6_7680x4352.png",
        "wallhaven-kxd6d6_15360x8640.png"
    };
    
    private static final int WARMUP_ITERATIONS = 1;
    private static final int BENCHMARK_ITERATIONS = 5;
    private static final String OUTPUT_CSV = "output/wallhaven/wallhaven_benchmark_results.csv";
    
    static class BenchmarkResult {
        String imageFile;
        int width;
        int height;
        long pixels;
        long seqMinTime, seqMaxTime, seqAvgTime, seqMedianTime;
        long fjMinTime, fjMaxTime, fjAvgTime, fjMedianTime;
        double speedup, efficiency;
        double seqThroughput, fjThroughput;
        int availableCores;
    }
    
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  WALLHAVEN RESOLUTION SCALING BENCHMARK");
        System.out.println("  (Controlled Experiment: Same Image, Different Resolutions)");
        System.out.println("=".repeat(60) + "\n");
        
        int availableCores = Runtime.getRuntime().availableProcessors();
        System.out.println("System: " + availableCores + " cores");
        System.out.println("Warmup: " + WARMUP_ITERATIONS + " iteration(s)");
        System.out.println("Benchmark: " + BENCHMARK_ITERATIONS + " iterations per image");
        System.out.println("Purpose: Validate cache alignment hypothesis with perfect alignment\n");
        
        List<BenchmarkResult> results = new ArrayList<>();
        
        System.out.println("Processing wallhaven images in resolution order:\n");
        for (String filename : WALLHAVEN_FILES) {
            File imageFile = new File(ROOT_DIR, filename);
            if (!imageFile.exists()) {
                System.out.println("⚠ SKIP: " + filename + " (not found)");
                continue;
            }
            
            System.out.println("➜ " + filename);
            BenchmarkResult result = runBenchmark(imageFile, availableCores);
            if (result != null) {
                results.add(result);
                printResultSummary(result);
            }
            System.out.println();
        }
        
        if (!results.isEmpty()) {
            generateCSVReport(results, availableCores);
            System.out.println("\n" + "=".repeat(60));
            System.out.println("  ✓ RESULTS SAVED");
            System.out.println("=".repeat(60));
            System.out.println("File: " + OUTPUT_CSV + "\n");
        }
    }
    
    private static BenchmarkResult runBenchmark(File imageFile, int availableCores) {
        try {
            BufferedImage originalImage = ImageUtils.loadImage(imageFile.getAbsolutePath());
            if (originalImage == null) {
                System.out.println("  ✗ ERROR: Could not load image");
                return null;
            }
            
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            long pixels = (long) width * height;
            
            System.out.printf("  Resolution: %dx%d (%,d pixels)%n", width, height, pixels);
            System.out.printf("  Cache alignment: %s (width %% 64 = %d)%n", 
                width % 64 == 0 ? "✓ GOOD" : "✗ POOR", width % 64);
            
            System.out.print("  Warming up... ");
            runWarmup(originalImage, availableCores);
            System.out.println("✓");
            
            System.out.print("  Sequential blur...");
            List<Long> seqTimes = runSequentialBenchmark(originalImage);
            System.out.println(" ✓");
            
            System.out.print("  Fork/Join blur...");
            List<Long> fjTimes = runForkJoinBenchmark(originalImage, availableCores);
            System.out.println(" ✓");
            
            BenchmarkResult result = new BenchmarkResult();
            result.imageFile = imageFile.getName();
            result.width = width;
            result.height = height;
            result.pixels = pixels;
            result.availableCores = availableCores;
            
            result.seqMinTime = seqTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            result.seqMaxTime = seqTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            result.seqAvgTime = Math.round(seqTimes.stream().mapToLong(Long::longValue).average().orElse(0));
            result.seqMedianTime = calculateMedian(seqTimes);
            
            result.fjMinTime = fjTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            result.fjMaxTime = fjTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            result.fjAvgTime = Math.round(fjTimes.stream().mapToLong(Long::longValue).average().orElse(0));
            result.fjMedianTime = calculateMedian(fjTimes);
            
            result.speedup = (double) result.seqAvgTime / result.fjAvgTime;
            result.efficiency = result.speedup / availableCores * 100;
            
            result.seqThroughput = (double) pixels / result.seqAvgTime;
            result.fjThroughput = (double) pixels / result.fjAvgTime;
            
            return result;
        } catch (Exception e) {
            System.err.println("  ✗ ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void runWarmup(BufferedImage originalImage, int availableCores) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            BufferedImage blurred = ImageUtils.createBlankCopy(originalImage);
            SequentialBlur.applyBlur(originalImage, blurred);
            
            blurred = ImageUtils.createBlankCopy(originalImage);
            ForkJoinPool pool = new ForkJoinPool(availableCores);
            ForkJoinBlur task = new ForkJoinBlur(originalImage, blurred, Main.KERNEL_MARGIN,
                    originalImage.getHeight() - Main.KERNEL_MARGIN, Main.ROW_THRESHOLD);
            pool.invoke(task);
            pool.shutdown();
        }
    }
    
    private static List<Long> runSequentialBenchmark(BufferedImage originalImage) {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            BufferedImage blurred = ImageUtils.createBlankCopy(originalImage);
            long start = System.nanoTime();
            SequentialBlur.applyBlur(originalImage, blurred);
            long end = System.nanoTime();
            times.add((end - start) / 1_000_000);
        }
        return times;
    }
    
    private static List<Long> runForkJoinBenchmark(BufferedImage originalImage, int availableCores) {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            BufferedImage blurred = ImageUtils.createBlankCopy(originalImage);
            ForkJoinPool pool = new ForkJoinPool(availableCores);
            long start = System.nanoTime();
            ForkJoinBlur task = new ForkJoinBlur(originalImage, blurred, Main.KERNEL_MARGIN,
                    originalImage.getHeight() - Main.KERNEL_MARGIN, Main.ROW_THRESHOLD);
            pool.invoke(task);
            long end = System.nanoTime();
            pool.shutdown();
            times.add((end - start) / 1_000_000);
        }
        return times;
    }
    
    private static long calculateMedian(List<Long> values) {
        Collections.sort(values);
        int size = values.size();
        return size % 2 == 0 ? (values.get(size / 2 - 1) + values.get(size / 2)) / 2 : values.get(size / 2);
    }
    
    private static void printResultSummary(BenchmarkResult result) {
        System.out.printf("  Sequential:   Min=%dms, Max=%dms, Avg=%dms, Median=%dms (%.0f px/ms)%n",
                result.seqMinTime, result.seqMaxTime, result.seqAvgTime, result.seqMedianTime, result.seqThroughput);
        System.out.printf("  Fork/Join:    Min=%dms, Max=%dms, Avg=%dms, Median=%dms (%.0f px/ms)%n",
                result.fjMinTime, result.fjMaxTime, result.fjAvgTime, result.fjMedianTime, result.fjThroughput);
        System.out.printf("  Speedup: %.2fx | Efficiency: %.1f%% | Cores: %d%n",
                result.speedup, result.efficiency, result.availableCores);
    }
    
    private static void generateCSVReport(List<BenchmarkResult> results, int availableCores) {
        try (FileWriter writer = new FileWriter(OUTPUT_CSV)) {
            // Header row
            writer.append("Image File,Width,Height,Total Pixels,");
            writer.append("Seq Min (ms),Seq Max (ms),Seq Avg (ms),Seq Median (ms),");
            writer.append("FJ Min (ms),FJ Max (ms),FJ Avg (ms),FJ Median (ms),");
            writer.append("Speedup (S),Efficiency (%),");
            writer.append("Seq Throughput (px/ms),FJ Throughput (px/ms),");
            writer.append("Available Cores\n");
            
            // Data rows
            for (BenchmarkResult r : results) {
                writer.append(String.format("%s,%d,%d,%d,", r.imageFile, r.width, r.height, r.pixels));
                writer.append(String.format("%d,%d,%d,%d,", r.seqMinTime, r.seqMaxTime, r.seqAvgTime, r.seqMedianTime));
                writer.append(String.format("%d,%d,%d,%d,", r.fjMinTime, r.fjMaxTime, r.fjAvgTime, r.fjMedianTime));
                writer.append(String.format("%.2f,%.1f,", r.speedup, r.efficiency));
                writer.append(String.format("%.0f,%.0f,", r.seqThroughput, r.fjThroughput));
                writer.append(String.format("%d\n", r.availableCores));
            }
            
            // Summary section
            writer.append("\n=== PERFORMANCE SUMMARY ===\n");
            writer.append("Metric,Value\n");
            writer.append(String.format("Total Images,%d\n", results.size()));
            writer.append(String.format("Available Cores,%d\n", availableCores));
            writer.append(String.format("Benchmark Iterations,%d\n", BENCHMARK_ITERATIONS));
            
            double avgSpeedup = results.stream().mapToDouble(r -> r.speedup).average().orElse(0);
            double avgEfficiency = results.stream().mapToDouble(r -> r.efficiency).average().orElse(0);
            double avgSeqThroughput = results.stream().mapToDouble(r -> r.seqThroughput).average().orElse(0);
            double avgFJThroughput = results.stream().mapToDouble(r -> r.fjThroughput).average().orElse(0);
            
            double minSpeedup = results.stream().mapToDouble(r -> r.speedup).min().orElse(0);
            double maxSpeedup = results.stream().mapToDouble(r -> r.speedup).max().orElse(0);
            
            long totalPixels = results.stream().mapToLong(r -> r.pixels).sum();
            long totalSeqTime = results.stream().mapToLong(r -> r.seqAvgTime).sum();
            long totalFJTime = results.stream().mapToLong(r -> r.fjAvgTime).sum();
            double overallSpeedup = (double) totalSeqTime / totalFJTime;
            
            writer.append("\n");
            writer.append(String.format("Average Speedup,%.2fx\n", avgSpeedup));
            writer.append(String.format("Min Speedup,%.2fx\n", minSpeedup));
            writer.append(String.format("Max Speedup,%.2fx\n", maxSpeedup));
            writer.append(String.format("Average Efficiency,%.1f%%\n", avgEfficiency));
            writer.append(String.format("Overall Speedup (Total Pixels),%.2fx\n", overallSpeedup));
            writer.append(String.format("Average Sequential Throughput,%.0f px/ms\n", avgSeqThroughput));
            writer.append(String.format("Average Fork/Join Throughput,%.0f px/ms\n", avgFJThroughput));
            writer.append(String.format("Total Pixels Processed,%,d\n", totalPixels));
            
        } catch (IOException e) {
            System.err.println("Error generating CSV: " + e.getMessage());
        }
    }
}
