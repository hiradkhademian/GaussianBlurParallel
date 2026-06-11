package src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class PNGBenchmark {
    
    private static final String ROOT_DIR = ".";
    private static final String[] PNG_EXTENSIONS = {".png"};
    private static final int WARMUP_ITERATIONS = 1;
    private static final int BENCHMARK_ITERATIONS = 3;
    private static final String OUTPUT_CSV = "GaussianBlur_PNG_Results.csv";
    
    private static final String[] EXCLUDE_PATTERNS = {
        "_sequential", "_forkjoin", "_threaded", 
        "output_", "input", "test_", "converted"
    };
    
    static class BenchmarkResult {
        String imageFile;
        int width;
        int height;
        long pixels;
        long seqMinTime, seqMaxTime, seqAvgTime, seqMedianTime;
        long fjMinTime, fjMaxTime, fjAvgTime, fjMedianTime;
        double speedup, efficiency;
        int availableCores;
    }
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  PNG GAUSSIAN BLUR BENCHMARK");
        System.out.println("========================================\n");
        
        List<File> imageFiles = findPNGImages(ROOT_DIR);
        if (imageFiles.isEmpty()) {
            System.out.println("ERROR: No PNG images found!");
            return;
        }
        
        System.out.println("Found " + imageFiles.size() + " PNG image(s):\n");
        imageFiles.forEach(f -> System.out.println("  - " + f.getName()));
        System.out.println();
        
        int availableCores = Runtime.getRuntime().availableProcessors();
        System.out.println("System: " + availableCores + " cores");
        System.out.println("Warmup: " + WARMUP_ITERATIONS + " | Iterations: " + BENCHMARK_ITERATIONS + "\n");
        
        List<BenchmarkResult> results = new ArrayList<>();
        for (File imageFile : imageFiles) {
            System.out.println("Processing: " + imageFile.getName());
            BenchmarkResult result = runBenchmark(imageFile, availableCores);
            if (result != null) {
                results.add(result);
                printResultSummary(result);
            }
            System.out.println();
        }
        
        if (!results.isEmpty()) {
            generateCSVReport(results, availableCores);
            System.out.println("\n========================================");
            System.out.println("  PNG RESULTS SAVED");
            System.out.println("========================================");
            System.out.println("File: " + OUTPUT_CSV);
        }
    }
    
    private static List<File> findPNGImages(String directory) {
        try {
            return Files.walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.toString().toLowerCase();
                    boolean hasExt = false;
                    for (String ext : PNG_EXTENSIONS) {
                        if (name.endsWith(ext)) {
                            hasExt = true;
                            break;
                        }
                    }
                    if (!hasExt) return false;
                    for (String pattern : EXCLUDE_PATTERNS) {
                        if (name.contains(pattern)) return false;
                    }
                    return true;
                })
                .map(Path::toFile)
                .sorted(Comparator.comparingLong(File::length))
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private static BenchmarkResult runBenchmark(File imageFile, int availableCores) {
        try {
            BufferedImage originalImage = ImageUtils.loadImage(imageFile.getAbsolutePath());
            if (originalImage == null) {
                System.out.println("  ERROR: Could not load");
                return null;
            }
            
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            long pixels = (long) width * height;
            System.out.println("  Dimensions: " + width + "x" + height + " (" + pixels + " pixels)");
            
            System.out.println("  Warming up...");
            runWarmup(originalImage, availableCores);
            
            System.out.println("  Sequential Blur...");
            List<Long> seqTimes = runSequentialBenchmark(originalImage);
            
            System.out.println("  Fork/Join Blur...");
            List<Long> fjTimes = runForkJoinBenchmark(originalImage, availableCores);
            
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
            
            return result;
        } catch (Exception e) {
            System.err.println("  ERROR: " + e.getMessage());
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
        System.out.printf("  Sequential:  Min=%dms, Max=%dms, Avg=%dms, Median=%dms%n",
                result.seqMinTime, result.seqMaxTime, result.seqAvgTime, result.seqMedianTime);
        System.out.printf("  Fork/Join:   Min=%dms, Max=%dms, Avg=%dms, Median=%dms%n",
                result.fjMinTime, result.fjMaxTime, result.fjAvgTime, result.fjMedianTime);
        System.out.printf("  Speedup: %.2fx | Efficiency: %.1f%% (cores: %d)%n",
                result.speedup, result.efficiency, result.availableCores);
    }
    
    private static void generateCSVReport(List<BenchmarkResult> results, int availableCores) {
        try (FileWriter writer = new FileWriter(OUTPUT_CSV)) {
            writer.append("Image File,Width,Height,Total Pixels,");
            writer.append("Seq Min (ms),Seq Max (ms),Seq Avg (ms),Seq Median (ms),");
            writer.append("FJ Min (ms),FJ Max (ms),FJ Avg (ms),FJ Median (ms),");
            writer.append("Speedup (S),Efficiency (%),Available Cores\n");
            
            for (BenchmarkResult r : results) {
                writer.append(String.format("%s,%d,%d,%d,", r.imageFile, r.width, r.height, r.pixels));
                writer.append(String.format("%d,%d,%d,%d,", r.seqMinTime, r.seqMaxTime, r.seqAvgTime, r.seqMedianTime));
                writer.append(String.format("%d,%d,%d,%d,", r.fjMinTime, r.fjMaxTime, r.fjAvgTime, r.fjMedianTime));
                writer.append(String.format("%.2f,%.1f,%d\n", r.speedup, r.efficiency, r.availableCores));
            }
            
            writer.append("\n=== PNG BENCHMARK SUMMARY ===\n");
            writer.append("Metric,Value\n");
            writer.append(String.format("Total PNG Images,%d\n", results.size()));
            writer.append(String.format("Available Cores,%d\n", availableCores));
            
            double avgSpeedup = results.stream().mapToDouble(r -> r.speedup).average().orElse(0);
            double avgEfficiency = results.stream().mapToDouble(r -> r.efficiency).average().orElse(0);
            double seqThroughput = results.stream().mapToDouble(r -> (double)r.pixels / r.seqAvgTime).average().orElse(0);
            double fjThroughput = results.stream().mapToDouble(r -> (double)r.pixels / r.fjAvgTime).average().orElse(0);
            
            writer.append(String.format("Average Speedup,%.2fx\n", avgSpeedup));
            writer.append(String.format("Average Efficiency,%.1f%%\n", avgEfficiency));
            writer.append(String.format("Avg Sequential Throughput,%.0f pixels/ms\n", seqThroughput));
            writer.append(String.format("Avg Fork/Join Throughput,%.0f pixels/ms\n", fjThroughput));
            
        } catch (IOException e) {
            System.err.println("Error generating CSV: " + e.getMessage());
        }
    }
}
