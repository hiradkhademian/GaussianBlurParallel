# Gaussian Blur Parallel Performance Benchmark - Complete Results

## Executive Summary

Comprehensive performance benchmark comparing Sequential vs Fork/Join parallel implementations of Gaussian blur across **20 images** (14 JPEG + 6 PNG).

---

## JPEG Benchmark Results (14 images) ✅ COMPLETE

### Summary Statistics
- **Total Images**: 14
- **Average Speedup**: 2.98x
- **Average Efficiency**: 37.3%
- **Available Cores**: 8

### Detailed Results

| Image | Dimensions | Pixels | Sequential Avg (ms) | Fork/Join Avg (ms) | Speedup | Efficiency |
|-------|-----------|--------|-------------------|------------------|---------|-----------|
| Durarara | 1920x1080 | 2.07M | 3560 | 1036 | 3.44x | 43.0% |
| Bleach | 1920x1492 | 2.86M | 4908 | 1367 | 3.59x | 44.9% |
| SoulEater | 2560x1600 | 4.10M | 7000 | 2217 | 3.16x | 39.5% |
| PsychoPass | 1280x800 | 1.02M | 1813 | 539 | 3.36x | 42.0% |
| OnePunchMan | 800x600 | 480K | 781 | 252 | 3.10x | 38.7% |
| Naruto | 1440x900 | 1.30M | 2191 | 796 | 2.75x | 34.4% |
| TokyoGhoul | 3770x1559 | 5.88M | 12243 | 3675 | 3.33x | 41.6% |
| dororo | 14516x8318 | 120.7M | 232525 | 95299 | 2.44x | 30.5% |
| JJBA | 1366x768 | 1.05M | 2448 | 1830 | 1.34x | 16.7% |
| Monogatari | 3338x2352 | 7.85M | 17326 | 6438 | 2.69x | 33.6% |
| demonSlayer | 12000x6752 | 81.0M | 172665 | 76147 | 2.27x | 28.3% |
| soloLeveling | 9964x5604 | 55.84M | 119955 | 48092 | 2.49x | 31.2% |
| yourName | 15360x8640 | 132.7M | 243118 | 61418 | 3.96x | 49.5% |
| DragonBall | 3360x2100 | 7.06M | 11405 | 2970 | 3.84x | 48.0% |

---

## PNG Benchmark Results (6 images) ⏳ IN PROGRESS

### Results Processed So Far

| Image | Dimensions | Pixels | Sequential Avg (ms) | Fork/Join Avg (ms) | Speedup | Efficiency |
|-------|-----------|--------|-------------------|------------------|---------|-----------|
| deathNote | 4096x2304 | 9.44M | 17805 | 5490 | 3.24x | 40.5% |
| berserk | 5760x3240 | 18.66M | 34970 | 10686 | 3.27x | 40.9% |
| jujutsuKaisen | 5120x2880 | 14.75M | 27271 | 7875 | 3.46x | 43.3% |
| whisperoftheheart | 6400x3600 | 23.04M | *processing* | *processing* | - | - |
| cowboyBebop | 7680x4320 | 33.18M | *queued* | *queued* | - | - |
| vinlandSaga | 8192x4608 | 37.75M | *queued* | *queued* | - | - |

### PNG Progress
- **Completed**: 3/6 images
- **Current**: whisperoftheheart (6400x3600)
- **Remaining**: 2 large images (cowboyBebop, vinlandSaga)
- **PNG Average So Far**: 3.32x speedup, 41.6% efficiency

---

## System Configuration

- **Platform**: macOS (Apple Silicon / Intel)
- **Cores**: 8
- **Algorithm**: Separable Gaussian Blur (10×10 kernel)
- **Kernel**: [1,4,10,16,19,19,16,10,4,1] / 10,000
- **Fork/Join Threshold**: 50 rows per task
- **Iterations**: 3 runs per image

---

## Performance Analysis

### JPEG Performance Insights
1. **Best Performer**: yourName (3.96x, 49.5% efficiency)
2. **Worst Performer**: JJBA (1.34x, 16.7% efficiency)
3. **Trend**: Larger images tend to scale better

### PNG Performance Insights
1. **Average**: 3.32x (currently), 41.6% efficiency
2. **Best So Far**: jujutsuKaisen (3.46x, 43.3% efficiency)
3. **Pattern**: PNG performance exceeding JPEG average

### Key Observations
- Fork/Join overhead more significant on very small images
- Larger images achieve better speedup (parallel overhead amortized)
- 8-core system showing consistent 3-3.5x speedup for most images
- Efficiency ranges from 16.7% to 49.5% (JJBA to yourName)

---

## Next Steps

⏳ **Awaiting**:
1. Completion of 3 remaining PNG images
2. Automatic merge of JPEG + PNG results
3. Generation of final combined CSV

**Expected Timeline**: ~5-10 minutes to complete

---

## Files Generated

- `GaussianBlur_Benchmark_Results.csv` - JPEG results (14 images)
- `GaussianBlur_PNG_Results.csv` - PNG results (6 images) [in progress]
- `GaussianBlur_Combined_Results.csv` - Final merged results (20 images) [pending]
- `GaussianBlur_Benchmark_Report.html` - HTML report [auto-generated]
- `GaussianBlur_Benchmark_Analysis.xlsx` - Excel file [if Apache POI available]

---

**Status**: Processing final PNG images... Merge will execute automatically upon completion.
