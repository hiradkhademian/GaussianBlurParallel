#!/bin/bash

# Comprehensive benchmark automation script
# This script waits for JPEG results, runs PNG benchmark, and merges everything

cd /Users/hiradkhademian/Desktop/GaussianBlurParallel

echo "=========================================="
echo "  AUTOMATED BENCHMARK PIPELINE"
echo "=========================================="
echo ""

# Step 1: Wait for JPEG benchmark to complete
echo "Step 1: Waiting for JPEG benchmark to complete..."
JPEG_CSV="GaussianBlur_Benchmark_Results.csv"

while [ ! -f "$JPEG_CSV" ]; do
    sleep 30
    echo "  Waiting for $JPEG_CSV ... ($(date +%H:%M:%S))"
done

echo "✅ JPEG benchmark complete!"
echo ""

# Step 2: Show JPEG results
echo "Step 2: JPEG Benchmark Results"
echo "==============================="
JPEG_LINES=$(wc -l < "$JPEG_CSV")
echo "Total lines in JPEG CSV: $JPEG_LINES"
echo ""
echo "JPEG data rows:"
head -5 "$JPEG_CSV"
echo ""

# Step 3: Run PNG benchmark
echo "Step 3: Starting PNG benchmark..."
echo "=============================="
java -cp . src.PNGBenchmark 2>&1

PNG_CSV="GaussianBlur_PNG_Results.csv"
if [ -f "$PNG_CSV" ]; then
    echo "✅ PNG benchmark complete!"
    echo ""
else
    echo "❌ PNG benchmark failed!"
    exit 1
fi

# Step 4: Show PNG results
echo "Step 4: PNG Benchmark Results"
echo "============================="
PNG_LINES=$(wc -l < "$PNG_CSV")
echo "Total lines in PNG CSV: $PNG_LINES"
echo ""
echo "PNG data rows:"
head -5 "$PNG_CSV"
echo ""

# Step 5: Merge results
echo "Step 5: Merging JPEG and PNG results..."
echo "========================================"
java -cp . src.MergeResults 2>&1

COMBINED_CSV="GaussianBlur_Combined_Results.csv"
if [ -f "$COMBINED_CSV" ]; then
    echo ""
    echo "✅ Results merged successfully!"
    echo "Combined CSV: $COMBINED_CSV"
    echo ""
    echo "=== FINAL COMBINED RESULTS ==="
    cat "$COMBINED_CSV"
else
    echo "❌ Merge failed!"
    exit 1
fi

echo ""
echo "=========================================="
echo "  BENCHMARK PIPELINE COMPLETE"
echo "=========================================="
echo "Files generated:"
echo "  - $JPEG_CSV (JPEG results)"
echo "  - $PNG_CSV (PNG results)"
echo "  - $COMBINED_CSV (Combined results)"
echo ""
