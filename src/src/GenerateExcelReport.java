import java.io.*;
import java.util.*;

public class GenerateExcelReport {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  GENERATING COMPREHENSIVE EXCEL REPORT");
        System.out.println("========================================\n");
        
        String csvFile = "GaussianBlur_Combined_Results.csv";
        String htmlFile = "GaussianBlur_Comprehensive_Report.html";
        
        try {
            List<String> csvLines = readCSV(csvFile);
            if (csvLines.isEmpty()) {
                System.out.println("ERROR: Could not read " + csvFile);
                return;
            }
            
            // Generate HTML report
            generateHTMLReport(csvLines, htmlFile);
            
            System.out.println("✅ REPORT GENERATION COMPLETE!");
            System.out.println("");
            System.out.println("Files generated:");
            System.out.println("  - " + htmlFile + " (Comprehensive HTML Report)");
            System.out.println("");
            System.out.println("To convert to Excel:");
            System.out.println("  1. Open " + htmlFile + " in a web browser");
            System.out.println("  2. Print to PDF or copy tables to Excel manually");
            System.out.println("  OR");
            System.out.println("  3. Use the CSV file directly in Excel");
            
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateHTMLReport(List<String> csvLines, String outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("  <meta charset='UTF-8'>\n");
            writer.write("  <title>Gaussian Blur Parallel Benchmark - Complete Report</title>\n");
            writer.write("  <style>\n");
            writer.write("    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n");
            writer.write("    .container { max-width: 1400px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
            writer.write("    h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n");
            writer.write("    h2 { color: #34495e; margin-top: 30px; border-left: 4px solid #3498db; padding-left: 10px; }\n");
            writer.write("    table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n");
            writer.write("    th { background: #3498db; color: white; padding: 12px; text-align: left; font-weight: bold; }\n");
            writer.write("    td { padding: 10px; border-bottom: 1px solid #ecf0f1; }\n");
            writer.write("    tr:hover { background: #f8f9fa; }\n");
            writer.write("    .metric-box { display: inline-block; margin: 15px 20px 15px 0; padding: 20px; background: #ecf0f1; border-radius: 5px; min-width: 200px; }\n");
            writer.write("    .metric-value { font-size: 24px; font-weight: bold; color: #3498db; }\n");
            writer.write("    .metric-label { font-size: 12px; color: #7f8c8d; text-transform: uppercase; }\n");
            writer.write("    .section { margin: 30px 0; }\n");
            writer.write("    .highlight { background: #fff9e6; }\n");
            writer.write("    .good { color: #27ae60; font-weight: bold; }\n");
            writer.write("    .average { color: #f39c12; font-weight: bold; }\n");
            writer.write("    .poor { color: #e74c3c; font-weight: bold; }\n");
            writer.write("  </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("  <div class='container'>\n");
            
            // Header
            writer.write("    <h1>🎯 Gaussian Blur Parallel Benchmark - Complete Analysis</h1>\n");
            writer.write("    <p style='color: #7f8c8d;'>Comprehensive performance comparison of Sequential vs Fork/Join parallel implementations</p>\n");
            
            // Executive Summary
            writer.write("    <div class='section'>\n");
            writer.write("      <h2>📊 Executive Summary</h2>\n");
            writer.write("      <div class='metric-box'>\n");
            writer.write("        <div class='metric-label'>Total Images</div>\n");
            writer.write("        <div class='metric-value'>20</div>\n");
            writer.write("      </div>\n");
            writer.write("      <div class='metric-box'>\n");
            writer.write("        <div class='metric-label'>Average Speedup</div>\n");
            writer.write("        <div class='metric-value good'>3.11x</div>\n");
            writer.write("      </div>\n");
            writer.write("      <div class='metric-box'>\n");
            writer.write("        <div class='metric-label'>Average Efficiency</div>\n");
            writer.write("        <div class='metric-value good'>38.9%</div>\n");
            writer.write("      </div>\n");
            writer.write("      <div class='metric-box'>\n");
            writer.write("        <div class='metric-label'>Available Cores</div>\n");
            writer.write("        <div class='metric-value'>8</div>\n");
            writer.write("      </div>\n");
            writer.write("    </div>\n");
            
            // JPEG Results
            writer.write("    <div class='section'>\n");
            writer.write("      <h2>📸 JPEG Benchmark Results (14 images)</h2>\n");
            writer.write("      <table>\n");
            writer.write("        <tr>\n");
            writer.write("          <th>Image</th>\n");
            writer.write("          <th>Dimensions</th>\n");
            writer.write("          <th>Pixels</th>\n");
            writer.write("          <th>Sequential (ms)</th>\n");
            writer.write("          <th>Fork/Join (ms)</th>\n");
            writer.write("          <th>Speedup</th>\n");
            writer.write("          <th>Efficiency</th>\n");
            writer.write("        </tr>\n");
            
            boolean inJpeg = true;
            for (String line : csvLines) {
                if (line.contains("Metric,Value")) break;
                if (line.startsWith("Image File") || line.isEmpty()) continue;
                if (line.contains(".jpeg")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 15) {
                        writer.write("        <tr>\n");
                        writer.write("          <td>" + parts[0] + "</td>\n");
                        writer.write("          <td>" + parts[1] + "x" + parts[2] + "</td>\n");
                        writer.write("          <td>" + formatNumber(parts[3]) + "M</td>\n");
                        writer.write("          <td>" + parts[7] + "</td>\n");
                        writer.write("          <td>" + parts[11] + "</td>\n");
                        
                        double speedup = Double.parseDouble(parts[12]);
                        String speedupClass = speedup >= 3.5 ? "good" : (speedup >= 2.5 ? "average" : "poor");
                        writer.write("          <td class='" + speedupClass + "'>" + String.format("%.2f", speedup) + "x</td>\n");
                        writer.write("          <td>" + parts[13] + "%</td>\n");
                        writer.write("        </tr>\n");
                    }
                }
            }
            writer.write("      </table>\n");
            writer.write("    </div>\n");
            
            // PNG Results
            writer.write("    <div class='section'>\n");
            writer.write("      <h2>🖼️ PNG Benchmark Results (6 images)</h2>\n");
            writer.write("      <table>\n");
            writer.write("        <tr>\n");
            writer.write("          <th>Image</th>\n");
            writer.write("          <th>Dimensions</th>\n");
            writer.write("          <th>Pixels</th>\n");
            writer.write("          <th>Sequential (ms)</th>\n");
            writer.write("          <th>Fork/Join (ms)</th>\n");
            writer.write("          <th>Speedup</th>\n");
            writer.write("          <th>Efficiency</th>\n");
            writer.write("        </tr>\n");
            
            for (String line : csvLines) {
                if (line.contains(".png")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 15) {
                        writer.write("        <tr>\n");
                        writer.write("          <td>" + parts[0] + "</td>\n");
                        writer.write("          <td>" + parts[1] + "x" + parts[2] + "</td>\n");
                        writer.write("          <td>" + formatNumber(parts[3]) + "M</td>\n");
                        writer.write("          <td>" + parts[7] + "</td>\n");
                        writer.write("          <td>" + parts[11] + "</td>\n");
                        
                        double speedup = Double.parseDouble(parts[12]);
                        String speedupClass = speedup >= 3.5 ? "good" : (speedup >= 2.5 ? "average" : "poor");
                        writer.write("          <td class='" + speedupClass + "'>" + String.format("%.2f", speedup) + "x</td>\n");
                        writer.write("          <td>" + parts[13] + "%</td>\n");
                        writer.write("        </tr>\n");
                    }
                }
            }
            writer.write("      </table>\n");
            writer.write("    </div>\n");
            
            // Performance Analysis
            writer.write("    <div class='section'>\n");
            writer.write("      <h2>📈 Performance Analysis</h2>\n");
            writer.write("      <h3>Key Insights:</h3>\n");
            writer.write("      <ul>\n");
            writer.write("        <li><strong>PNG Performance:</strong> Average speedup of 3.41x (vs JPEG 2.98x)</li>\n");
            writer.write("        <li><strong>Scaling:</strong> Larger images achieve better parallelization efficiency</li>\n");
            writer.write("        <li><strong>Best Performance:</strong> yourName (132.7M pixels) - 3.96x speedup</li>\n");
            writer.write("        <li><strong>Worst Performance:</strong> JJBA (1M pixels) - 1.34x speedup (overhead dominates)</li>\n");
            writer.write("        <li><strong>Consistency:</strong> Most images achieve 3-3.5x speedup</li>\n");
            writer.write("      </ul>\n");
            writer.write("    </div>\n");
            
            // Footer
            writer.write("    <div style='margin-top: 50px; padding-top: 20px; border-top: 1px solid #ecf0f1; color: #7f8c8d; font-size: 12px;'>\n");
            writer.write("      <p>Report generated: " + new java.util.Date() + "</p>\n");
            writer.write("      <p>System: 8-core processor | Algorithm: Separable Gaussian Blur (10×10 kernel)</p>\n");
            writer.write("    </div>\n");
            
            writer.write("  </div>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        }
    }
    
    private static String formatNumber(String numStr) {
        try {
            long num = Long.parseLong(numStr);
            return String.format("%.2f", num / 1_000_000.0);
        } catch (NumberFormatException e) {
            return numStr;
        }
    }
    
    private static List<String> readCSV(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
