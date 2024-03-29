package hzg.wpn.util;

import java.awt.image.BufferedImage;

/**
 * @author Ingvord
 * @since 31.05.14
 */
public class BufferedImageHelper {
    private BufferedImageHelper(){}

    public static int[][] imageToRGBArray(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        int[][] result = new int[height][width];
        for (int y = 0; y < height; ++y) {
            image.getRGB(0, y, width, 1, result[y], 0, 0);
        }
        return result;
    }

    public static BufferedImage RGBArrayToImage(int[][] rgb) {
        int width = rgb[0].length;
        int height = rgb.length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; ++y) {
            result.setRGB(0, y, width, 1, rgb[y], 0, 0);
        }
        return result;
    }
}
