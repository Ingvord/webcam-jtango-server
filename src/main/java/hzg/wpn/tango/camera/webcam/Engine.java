package hzg.wpn.tango.camera.webcam;

import ClearImageJNI.*;

import javax.imageio.ImageIO;
import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public String[] decodeBarcode() throws Exception{
        BufferedImage image;
        if((image = lastCapturedImage.get()) == null){
            captureImage();
            image = lastCapturedImage.get();
        }
        ImageIO.write(image,"bmp",new File("output-color.bmp"));

        ICiServer server = new CiServer().getICiServer();
        ICiDataMatrix data = server.CreateDataMatrix();
        ICiImage iCiImage = server.CreateImage();

        iCiImage.OpenFromFileBMP("output-color.bmp");

        data.setImage(iCiImage);

        int length = data.Find(0);
        String[] result = new String[length];

        for(int i = 0; i < length; ++i){
            ICiBarcode code = data.getBarcodes().getItem(i + 1);
            result[i] = code.getText();
        }

        return result;
    }

    private BufferedImage redrawInGrayScale(BufferedImage coloredImage){
        BufferedImage result = new BufferedImage(coloredImage.getWidth(), coloredImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(coloredImage, 0, 0, null);
        g.dispose();
        return result;
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
