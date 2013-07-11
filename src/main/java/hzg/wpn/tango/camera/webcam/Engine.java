package hzg.wpn.tango.camera.webcam;

import ClearImageJNI.*;
import ezjcom.JComException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public class Engine {
    private final PlayerAdapter player;

    private final AtomicReference<BufferedImage> lastCapturedImage = new AtomicReference<BufferedImage>(null);

    public Engine(/*= "vfw:Microsoft WDM Image Capture (Win32):0"*/PlayerAdapter player) {
        this.player = player;
    }



    public void init(Properties webcamProperties) throws Exception{
        player.init(webcamProperties);
    }

    public void start() throws Exception{
        player.start();
    }

    public void captureImage() throws Exception{
        BufferedImage capture = player.capture();
        ImageIO.write(capture,"jpeg", new File("capture.jpeg"));
        this.lastCapturedImage.set(capture);
    }

    public BufferedImage getLastCapturedImage(){
        return lastCapturedImage.get();
    }

    public int[][] getImageAsRGBArray(BufferedImage image){
        int height = image.getHeight();
        int width = image.getWidth();
        int[][] result = new int[height][width];
        for (int y = 0; y < height; ++y) {
            image.getRGB(0,y,width,1,result[y],0,0);
        }
        return result;
    }

    public BufferedImage RGBArrayToImage(int[][] rgb) {
        int width = rgb[0].length;
        int height = rgb.length;
        BufferedImage result = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
        for(int y = 0; y < height;++y){
            result.setRGB(0,y,width,1,rgb[y],0,0);
        }
        return result;
    }

    public void stop() throws Exception{
        player.stop();
    }

    public void shutdown() throws Exception{
        player.close();
    }
}
