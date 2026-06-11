package src;

import java.io.*;
import java.util.*;

public class MergeResults {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  MERGING BENCHMARK RESULTS");
        System.out.println("========================================\n");
        
        String jpegCsv = "GaussianBlur_Benchmark_Results.csv";
        String pngCsv = "GaussianBlur_PNG_Results.csv";
        String outputCsv = "GaussianBlur_Combined_Results.csv";
        
        try {
            // Read JPEG results
            List<String> jpegLines = readCSV(jpegCsv);
            List<String> pngLines = readCSV(pngCsv);
            
            if (jpegLines.isEmpty() || pngLines.isEmpty()) {
                System.out.println("ERROR: Could not read CSV files!");
                System.out.println("JPEG CSV: " + jpegCsv + " (" + jpegLines.size() + " lines)");
                System.out.println("PNG CSV: " + pngCsv + " (" + pngLines.size() + " lines)");
                return;
            }
            
            System.out.println("JPEG Results: " + (jpegLines.size() - 1) + " images");
            System.out.println("PNG Results: " + (pngLines.size() - 1) + " images");
            System.out.println();
            
            // Merge results
            List<String> mergedLines = new ArrayList<>();
            
            // Add header (from JPEG file)
            mergedLines.add(jpegLines.get(0));
            
            // Add all data rows from JPEG
            for (int i = 1; i < jpegLines.size(); i++) {
                String line = jpegLines.get(i).trim();
                if (!line.isEmpty() && !line.startsWith("=")) {
                    mergedLines.add(line);
                }
            }
            
            // Add all data rows from PNG (skip header)
            for (int i = 1; i < pngLines.size(); i++) {
                String line = pngLines.get(i).trim();
                if (!line.isEmpty() && !line.startsWith("=")) {
                    mergedLines.add(line);
                }
            }
            
            // Write combined file
            try (FileWriter writer = new FileWriter(outputCsv)) {
                for (String line : mergedLines) {
                    writer.write(line + "\n");
                }
            }
            
            // Calculate summary statistics
            double totalSpeedup = 0;
            double totalEfficiency = 0;
            int dataCount = 0;
            
            for (int i = 1; i < mergedLines.size(); i++) {
                String line = mergedLines.get(i);
                String[] parts = line.split(",");
                if (parts.length >= 13) {
                    try {
                        double speedup = Double.parseDouble(parts[12]);
                        double efficiency = Double.parseDouble(parts[13]);
                        totalSpeedup += speedup;
                        totalEfficiency += efficiency;
                        dataCount++;
                    } catch (NumberFormatException e) {
                        // Skip non-numeric lines
                    }
                }
            }
            
            // Append summary
            try (FileWriter writer = new FileWriter(outputCsv, true)) {
                writer.write("\n=== COMBINED BENCHMARK SUMMARY ===\n");
                writer.write("Metric,Value\n");
                writer.write("Total Images," + dataCount + "\n");
                writer.write("JPEG Images," + (jpegLines.size() - 1) + "\n");
                writer.write("PNG Images," + (pngLines.size() - 1) + "\n");
                writer.write(String.format("Average Speedup,%.2fx\n", totalSpeedup / dataCount));
                writer.write(String.format("Average Efficiency,%.1f%%\n", totalEfficiency / dataCount));
            }
            
            System.out.println("✅ MERGE COMPLETE!");
            System.out.println("Output file: " + outputCsv);
            System.out.println("Total images: " + dataCount);
            System.out.println("Average Speedup: " + String.format("%.2f", totalSpeedup / dataCount) + "x");
            System.out.println("Average Efficiency: " + String.format("%.1f", totalEfficiency / dataCount) + "%");
            
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static List<String> readCSV(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        File file = new File(filename);
        
        if (!file.exists()) {
            System.out.println("WARNING: " + filename + " not found!");
            return lines;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        return lines;
    }
}
