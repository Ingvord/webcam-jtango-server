package hzg.wpn.tango.camera.webcam;

import java.awt.image.BufferedImage;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.07.13
 */
public class WebCamHelper {
    private WebCamHelper() {
    }

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
