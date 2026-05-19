package src;

import java.awt.image.BufferedImage;

public class SequentialBlur {
    public static void applyBlur(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[][] kernel = Main.KERNEL;
        int normalizer = Main.KERNEL_NORMALIZER;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int redSum = 0;
                int greenSum = 0;
                int blueSum = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = src.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        int weight = kernel[ky + 1][kx + 1];
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
