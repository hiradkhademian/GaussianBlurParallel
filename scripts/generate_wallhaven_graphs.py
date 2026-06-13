#!/usr/bin/env python3
"""
Generate 3-panel comprehensive visualization for Wallhaven benchmark results
Panel 1: Sequential execution times across resolutions
Panel 2: Fork/Join execution times across resolutions  
Panel 3: Combined metrics (speedup and efficiency)
"""

import csv
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.ticker import FuncFormatter
import numpy as np

# Read wallhaven benchmark results
data = {
    'image': [],
    'pixels': [],
    'resolution': [],
    'seq_avg': [],
    'fj_avg': [],
    'speedup': [],
    'efficiency': []
}

with open('output/wallhaven/wallhaven_benchmark_results.csv', 'r') as f:
    reader = csv.DictReader(f)
    for row in reader:
        if row['Image File'].startswith('wallhaven'):
            # Parse data
            pixels = int(row['Total Pixels'])
            resolution = f"{row['Width']}×{row['Height']}"
            
            data['image'].append(row['Image File'].replace('.png', '').replace('wallhaven-kxd6d6_', ''))
            data['pixels'].append(pixels / 1_000_000)  # Convert to megapixels
            data['resolution'].append(resolution)
            data['seq_avg'].append(int(row['Seq Avg (ms)']))
            data['fj_avg'].append(int(row['FJ Avg (ms)']))
            data['speedup'].append(float(row['Speedup (S)']))
            data['efficiency'].append(float(row['Efficiency (%)']))

print("Wallhaven Data Loaded:")
for i, img in enumerate(data['image']):
    print(f"  {img}: {data['pixels'][i]:.1f}M px | Seq: {data['seq_avg'][i]}ms | FJ: {data['fj_avg'][i]}ms | Speedup: {data['speedup'][i]:.2f}x")

# Create figure with 3 subplots
fig, axes = plt.subplots(1, 3, figsize=(18, 6))
fig.suptitle('Wallhaven Resolution Scaling Analysis\n(Perfect Cache Alignment Study)', 
             fontsize=16, fontweight='bold', y=0.98)

# Define colors
color_seq = '#2E86AB'  # Blue
color_fj = '#A23B72'   # Purple
color_speedup = '#06A77D'  # Green
color_efficiency = '#F18F01'  # Orange

# ============================================================
# PANEL 1: Sequential Execution Times
# ============================================================
ax1 = axes[0]
x = np.arange(len(data['image']))
width = 0.6

bars1 = ax1.bar(x, data['seq_avg'], width, label='Sequential', color=color_seq, alpha=0.8, edgecolor='black', linewidth=1.5)

# Add value labels on bars
for i, (bar, val) in enumerate(zip(bars1, data['seq_avg'])):
    height = bar.get_height()
    ax1.text(bar.get_x() + bar.get_width()/2., height,
             f'{int(val)}ms',
             ha='center', va='bottom', fontsize=10, fontweight='bold')

ax1.set_xlabel('Image Resolution', fontsize=11, fontweight='bold')
ax1.set_ylabel('Execution Time (ms)', fontsize=11, fontweight='bold')
ax1.set_title('Sequential Blur Performance', fontsize=12, fontweight='bold')
ax1.set_xticks(x)
ax1.set_xticklabels([f"{data['resolution'][i]}\n({data['pixels'][i]:.1f}M px)" for i in range(len(data['image']))], 
                     fontsize=9)
ax1.grid(axis='y', alpha=0.3, linestyle='--')
ax1.set_axisbelow(True)

# Add horizontal line for reference
ax1.axhline(y=np.mean(data['seq_avg']), color='red', linestyle='--', linewidth=2, alpha=0.5, label='Average')
ax1.legend(fontsize=10)

# ============================================================
# PANEL 2: Fork/Join Execution Times
# ============================================================
ax2 = axes[1]

bars2 = ax2.bar(x, data['fj_avg'], width, label='Fork/Join', color=color_fj, alpha=0.8, edgecolor='black', linewidth=1.5)

# Add value labels on bars
for i, (bar, val) in enumerate(zip(bars2, data['fj_avg'])):
    height = bar.get_height()
    ax2.text(bar.get_x() + bar.get_width()/2., height,
             f'{int(val)}ms',
             ha='center', va='bottom', fontsize=10, fontweight='bold')

ax2.set_xlabel('Image Resolution', fontsize=11, fontweight='bold')
ax2.set_ylabel('Execution Time (ms)', fontsize=11, fontweight='bold')
ax2.set_title('Fork/Join Parallel Performance', fontsize=12, fontweight='bold')
ax2.set_xticks(x)
ax2.set_xticklabels([f"{data['resolution'][i]}\n({data['pixels'][i]:.1f}M px)" for i in range(len(data['image']))], 
                     fontsize=9)
ax2.grid(axis='y', alpha=0.3, linestyle='--')
ax2.set_axisbelow(True)

# Add horizontal line for reference
ax2.axhline(y=np.mean(data['fj_avg']), color='red', linestyle='--', linewidth=2, alpha=0.5, label='Average')
ax2.legend(fontsize=10)

# ============================================================
# PANEL 3: Combined Metrics (Speedup + Efficiency)
# ============================================================
ax3 = axes[2]

x_pos = np.arange(len(data['image']))
width = 0.35

bars3a = ax3.bar(x_pos - width/2, data['speedup'], width, label='Speedup (×)', 
                 color=color_speedup, alpha=0.8, edgecolor='black', linewidth=1.5)
ax3_twin = ax3.twinx()
bars3b = ax3_twin.bar(x_pos + width/2, data['efficiency'], width, label='Efficiency (%)',
                      color=color_efficiency, alpha=0.8, edgecolor='black', linewidth=1.5)

# Add value labels
for bar, val in zip(bars3a, data['speedup']):
    height = bar.get_height()
    ax3.text(bar.get_x() + bar.get_width()/2., height,
             f'{val:.2f}x',
             ha='center', va='bottom', fontsize=9, fontweight='bold')

for bar, val in zip(bars3b, data['efficiency']):
    height = bar.get_height()
    ax3_twin.text(bar.get_x() + bar.get_width()/2., height,
                  f'{val:.1f}%',
                  ha='center', va='bottom', fontsize=9, fontweight='bold')

ax3.set_xlabel('Image Resolution', fontsize=11, fontweight='bold')
ax3.set_ylabel('Speedup (×)', fontsize=11, fontweight='bold', color=color_speedup)
ax3_twin.set_ylabel('Efficiency (%)', fontsize=11, fontweight='bold', color=color_efficiency)
ax3.set_title('Speedup & Efficiency Analysis', fontsize=12, fontweight='bold')
ax3.set_xticks(x_pos)
ax3.set_xticklabels([f"{data['resolution'][i]}\n({data['pixels'][i]:.1f}M px)" for i in range(len(data['image']))], 
                     fontsize=9)
ax3.tick_params(axis='y', labelcolor=color_speedup)
ax3_twin.tick_params(axis='y', labelcolor=color_efficiency)
ax3.grid(axis='y', alpha=0.3, linestyle='--')
ax3.set_axisbelow(True)

# Add average lines
avg_speedup = np.mean(data['speedup'])
avg_efficiency = np.mean(data['efficiency'])
ax3.axhline(y=avg_speedup, color=color_speedup, linestyle='--', linewidth=2, alpha=0.5)
ax3_twin.axhline(y=avg_efficiency, color=color_efficiency, linestyle='--', linewidth=2, alpha=0.5)

# Combined legend
lines1, labels1 = ax3.get_legend_handles_labels()
lines2, labels2 = ax3_twin.get_legend_handles_labels()
ax3.legend(lines1 + lines2, labels1 + labels2, loc='upper right', fontsize=10)

# ============================================================
# Adjust layout and save
# ============================================================
plt.tight_layout()
plt.savefig('output/wallhaven/wallhaven_scaling_analysis.png', dpi=300, bbox_inches='tight')
print("\n✓ Graph saved: output/wallhaven/wallhaven_scaling_analysis.png")

# Print summary statistics
print("\n" + "="*70)
print("WALLHAVEN BENCHMARK ANALYSIS SUMMARY")
print("="*70)
print(f"Average Sequential Time: {np.mean(data['seq_avg']):.0f} ms")
print(f"Average Fork/Join Time: {np.mean(data['fj_avg']):.0f} ms")
print(f"Average Speedup: {np.mean(data['speedup']):.2f}x")
print(f"Average Efficiency: {np.mean(data['efficiency']):.1f}%")
print(f"Best Speedup: {max(data['speedup']):.2f}x at {data['resolution'][data['speedup'].index(max(data['speedup']))]}")
print(f"Worst Speedup: {min(data['speedup']):.2f}x at {data['resolution'][data['speedup'].index(min(data['speedup']))]}")
print("="*70)

plt.show()
