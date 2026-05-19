package src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtils {

    // Loads an image from the local disk into a BufferedImage buffer [cite: 37, 51]
    public static BufferedImage loadImage(String path) {
        try {
            File inputFile = new File(path);
            return ImageIO.read(inputFile);
        } catch (IOException e) {
            System.err.println("Error: Could not load image from " + path);
            e.printStackTrace();
            return null;
        }
    }

    // Saves the processed BufferedImage buffer back to disk [cite: 52, 58]
    public static void saveImage(BufferedImage image, String path, String format) {
        try {
            File outputFile = new File(path);
            ImageIO.write(image, format, outputFile);
            System.out.println("Image successfully saved to: " + path);
        } catch (IOException e) {
            System.err.println("Error: Could not save image to " + path);
            e.printStackTrace();
        }
    }

    // Creates a blank matching image buffer for output regions [cite: 52]
    public static BufferedImage createBlankCopy(BufferedImage src) {
        return new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
    }
}