package src;

import java.awt.image.BufferedImage;

public class SequentialBlur {
    public static void applyBlur(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[][] kernel = Main.KERNEL;
        int normalizer = Main.KERNEL_NORMALIZER;
        int kernelSize = kernel.length;
        int offsetStart = -((kernelSize - 1) / 2);
        int offsetEnd = offsetStart + kernelSize - 1;
        int border = kernelSize / 2;

        for (int y = border; y < height - border; y++) {
            for (int x = border; x < width - border; x++) {
                int redSum = 0;
                int greenSum = 0;
                int blueSum = 0;

                for (int ky = offsetStart; ky <= offsetEnd; ky++) {
                    for (int kx = offsetStart; kx <= offsetEnd; kx++) {
                        int rgb = src.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        int weight = kernel[ky - offsetStart][kx - offsetStart];
                        redSum += r * weight;
                        greenSum += g * weight;
                        blueSum += b * weight;
                    }
                }

                int finalR = redSum / normalizer;
                int finalG = greenSum / normalizer;
                int finalB = blueSum / normalizer;
                int blurredPixel = (0xFF << 24) | (finalR << 16) | (finalG << 8) | finalB;
                dest.setRGB(x, y, blurredPixel);
            }
        }
    }
}
