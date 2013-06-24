package hzg.wpn.tango.camera.webcam;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

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

        BufferedImage image = (BufferedImage) btoi.createImage(buf);
        if(image == null){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            captureImage();
        } else {
            lastCapturedImage.set(image);
        }
    }

    public String decodeBarcode() throws Exception{
        BufferedImage image;
        if((image = lastCapturedImage.get()) == null)
            throw new NullPointerException("No image was captured.");

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        Result result = reader.decode(bitmap);

        return result.getText();
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
