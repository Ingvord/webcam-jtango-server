package hzg.wpn.tango.camera.webcam;

import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public class Engine {
    private final Player player;
    private final CaptureDeviceInfo di;
    private final MediaLocator ml;
    private final AtomicReference<BufferedImage> lastCapturedImage = new AtomicReference<BufferedImage>(null);
    private final FrameGrabbingControl fgc;

    public Engine(String captureDevice /*= "vfw:Microsoft WDM Image Capture (Win32):0"*/) {
        di = CaptureDeviceManager.getDevice(captureDevice);
        ml = di.getLocator();

        player = createPlayer(ml);
        fgc = (FrameGrabbingControl)
                player.getControl("javax.media.control.FrameGrabbingControl");
    }

    private static Player createPlayer(MediaLocator mediaLocator) {
        try{
            return Manager.createRealizedPlayer(mediaLocator);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot create player!");
        }
    }

    public void start(){
        player.start();
    }

    public void captureImage(){
        Buffer buf = fgc.grabFrame();

        // Convert it to an image
        BufferToImage btoi = new BufferToImage((VideoFormat)buf.getFormat());

        lastCapturedImage.set((BufferedImage)btoi.createImage(buf));
    }

    public int[][] getImage(){
        BufferedImage image = lastCapturedImage.get();
        int height = image.getHeight();
        int width = image.getWidth();
        int[][] result = new int[height][width];
        for (int y = 0; y < height; y++) {
            image.getRGB(0,y,640,1,result[y],0,0);
        }
        return result;
    }

    public void stop(){
        player.stop();
    }

    public void shutdown() {
        player.close();
        player.deallocate();
    }
}
