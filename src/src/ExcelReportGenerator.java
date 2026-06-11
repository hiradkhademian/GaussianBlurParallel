package src;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generates a detailed Excel report for Gaussian Blur benchmark results.
 * Uses Apache POI library for Excel file generation.
 */
public class ExcelReportGenerator {
    
    public static void generate(String outputFile, List<ComprehensiveBenchmark.BenchmarkResult> results, 
                                int availableCores, int warmupIterations, int benchmarkIterations) throws Exception {
        
        // Create a workbook using Apache POI
        // Note: This requires org.apache.poi:poi and org.apache.poi:poi-ooxml
        Object workbook = createWorkbook();
        Object sheet = createSheet(workbook, "Benchmark Results");
        
        // Create detailed results
        int rowNum = 0;
        
        // Title and metadata
        createCell(sheet, rowNum, 0, "GAUSSIAN BLUR PARALLEL PROCESSING - BENCHMARK RESULTS");
        styleAsTitle(sheet, rowNum, 0);
        rowNum += 2;
        
        // System info
        createCell(sheet, rowNum, 0, "System Configuration");
        styleAsHeader(sheet, rowNum, 0);
        rowNum++;
        
        createCell(sheet, rowNum, 0, "Available CPU Cores:");
        createCell(sheet, rowNum, 1, String.valueOf(availableCores));
        rowNum++;
        
        createCell(sheet, rowNum, 0, "Warmup Iterations:");
        createCell(sheet, rowNum, 1, String.valueOf(warmupIterations));
        rowNum++;
        
        createCell(sheet, rowNum, 0, "Benchmark Iterations:");
        createCell(sheet, rowNum, 1, String.valueOf(benchmarkIterations));
        rowNum++;
        
        rowNum += 2;
        
        // Results header
        createCell(sheet, rowNum, 0, "Image File");
        createCell(sheet, rowNum, 1, "Width");
        createCell(sheet, rowNum, 2, "Height");
        createCell(sheet, rowNum, 3, "Total Pixels");
        createCell(sheet, rowNum, 4, "Seq Min (ms)");
        createCell(sheet, rowNum, 5, "Seq Max (ms)");
        createCell(sheet, rowNum, 6, "Seq Avg (ms)");
        createCell(sheet, rowNum, 7, "Seq Median (ms)");
        createCell(sheet, rowNum, 8, "FJ Min (ms)");
        createCell(sheet, rowNum, 9, "FJ Max (ms)");
        createCell(sheet, rowNum, 10, "FJ Avg (ms)");
        createCell(sheet, rowNum, 11, "FJ Median (ms)");
        createCell(sheet, rowNum, 12, "Speedup (S)");
        createCell(sheet, rowNum, 13, "Efficiency (%)");
        
        styleHeaderRow(sheet, rowNum);
        rowNum++;
        
        // Results data
        for (ComprehensiveBenchmark.BenchmarkResult r : results) {
            createCell(sheet, rowNum, 0, r.imageFile);
            createCell(sheet, rowNum, 1, String.valueOf(r.width));
            createCell(sheet, rowNum, 2, String.valueOf(r.height));
            createCell(sheet, rowNum, 3, String.valueOf(r.pixels));
            createCell(sheet, rowNum, 4, String.valueOf(r.seqMinTime));
            createCell(sheet, rowNum, 5, String.valueOf(r.seqMaxTime));
            createCell(sheet, rowNum, 6, String.valueOf(r.seqAvgTime));
            createCell(sheet, rowNum, 7, String.valueOf(r.seqMedianTime));
            createCell(sheet, rowNum, 8, String.valueOf(r.fjMinTime));
            createCell(sheet, rowNum, 9, String.valueOf(r.fjMaxTime));
            createCell(sheet, rowNum, 10, String.valueOf(r.fjAvgTime));
            createCell(sheet, rowNum, 11, String.valueOf(r.fjMedianTime));
            createCell(sheet, rowNum, 12, String.format("%.2f", r.speedup));
            createCell(sheet, rowNum, 13, String.format("%.1f", r.efficiency));
            rowNum++;
        }
        
        rowNum += 2;
        
        // Summary statistics
        createCell(sheet, rowNum, 0, "PERFORMANCE SUMMARY");
        styleAsHeader(sheet, rowNum, 0);
        rowNum++;
        
        createCell(sheet, rowNum, 0, "Metric");
        createCell(sheet, rowNum, 1, "Value");
        styleHeaderRow(sheet, rowNum);
        rowNum++;
        
        createCell(sheet, rowNum, 0, "Total Images Tested");
        createCell(sheet, rowNum, 1, String.valueOf(results.size()));
        rowNum++;
        
        double avgSpeedup = results.stream().mapToDouble(r -> r.speedup).average().orElse(0);
        createCell(sheet, rowNum, 0, "Average Speedup (FJ vs Sequential)");
        createCell(sheet, rowNum, 1, String.format("%.2fx", avgSpeedup));
        rowNum++;
        
        double avgEfficiency = results.stream().mapToDouble(r -> r.efficiency).average().orElse(0);
        createCell(sheet, rowNum, 0, "Average Efficiency");
        createCell(sheet, rowNum, 1, String.format("%.1f%%", avgEfficiency));
        rowNum++;
        
        double avgSeqTime = results.stream().mapToLong(r -> r.seqAvgTime).average().orElse(0);
        double avgFJTime = results.stream().mapToLong(r -> r.fjAvgTime).average().orElse(0);
        createCell(sheet, rowNum, 0, "Average Sequential Time");
        createCell(sheet, rowNum, 1, String.format("%.0f ms", avgSeqTime));
        rowNum++;
        
        createCell(sheet, rowNum, 0, "Average Fork/Join Time");
        createCell(sheet, rowNum, 1, String.format("%.0f ms", avgFJTime));
        rowNum++;
        
        double seqThroughput = results.stream().mapToDouble(r -> (double)r.pixels / r.seqAvgTime).average().orElse(0);
        double fjThroughput = results.stream().mapToDouble(r -> (double)r.pixels / r.fjAvgTime).average().orElse(0);
        createCell(sheet, rowNum, 0, "Avg Sequential Throughput");
        createCell(sheet, rowNum, 1, String.format("%.0f pixels/ms", seqThroughput));
        rowNum++;
        
        createCell(sheet, rowNum, 0, "Avg Fork/Join Throughput");
        createCell(sheet, rowNum, 1, String.format("%.0f pixels/ms", fjThroughput));
        rowNum++;
        
        double throughputGain = ((fjThroughput - seqThroughput) / seqThroughput) * 100;
        createCell(sheet, rowNum, 0, "Throughput Improvement");
        createCell(sheet, rowNum, 1, String.format("%.1f%%", throughputGain));
        
        // Auto-size columns
        autoSizeColumns(sheet, 14);
        
        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
            writeWorkbook(workbook, fileOut);
        }
    }
    
    // These methods will use reflection to support both Apache POI and a fallback mechanism
    
    private static Object createWorkbook() throws Exception {
        try {
            // Try to use Apache POI if available
            Class<?> xssfWorkbookClass = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
            return xssfWorkbookClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            // Fallback - throw exception to let the caller handle it
            throw new Exception("Apache POI not available. Install org.apache.poi:poi-ooxml", e);
        }
    }
    
    private static Object createSheet(Object workbook, String name) throws Exception {
        Class<?> clazz = workbook.getClass();
        var method = clazz.getMethod("createSheet", String.class);
        return method.invoke(workbook, name);
    }
    
    private static void createCell(Object sheet, int row, int col, String value) throws Exception {
        Class<?> sheetClass = sheet.getClass();
        var createRowMethod = sheetClass.getMethod("createRow", int.class);
        Object rowObj = createRowMethod.invoke(sheet, row);
        
        Class<?> rowClass = rowObj.getClass();
        var createCellMethod = rowClass.getMethod("createCell", int.class);
        Object cellObj = createCellMethod.invoke(rowObj, col);
        
        Class<?> cellClass = cellObj.getClass();
        var setCellValueMethod = cellClass.getMethod("setCellValue", String.class);
        setCellValueMethod.invoke(cellObj, value);
    }
    
    private static void styleAsTitle(Object sheet, int row, int col) {
        // Simple styling - can be enhanced with Apache POI CellStyle
    }
    
    private static void styleAsHeader(Object sheet, int row, int col) {
        // Simple styling
    }
    
    private static void styleHeaderRow(Object sheet, int row) {
        // Simple styling
    }
    
    private static void autoSizeColumns(Object sheet, int columnCount) throws Exception {
        Class<?> sheetClass = sheet.getClass();
        var autoSizeColumnMethod = sheetClass.getMethod("autoSizeColumn", int.class);
        for (int i = 0; i < columnCount; i++) {
            autoSizeColumnMethod.invoke(sheet, i);
        }
    }
    
    private static void writeWorkbook(Object workbook, FileOutputStream fileOut) throws Exception {
        Class<?> clazz = workbook.getClass();
        var writeMethod = clazz.getMethod("write", java.io.OutputStream.class);
        writeMethod.invoke(workbook, fileOut);
    }
}
