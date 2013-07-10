package hzg.wpn.tango.camera.webcam;

import ClearImageJNI.*;
import ezjcom.JComException;

import javax.imageio.ImageIO;
import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.image.BufferedImage;
import java.io.*;
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

    private final ICiServer server;
    private final ICiDataMatrix data;

    public Engine(String captureDevice /*= "vfw:Microsoft WDM Image Capture (Win32):0"*/) {
        di = CaptureDeviceManager.getDevice(captureDevice);
        ml = di.getLocator();

        player = createPlayer(ml);
        fgc = (FrameGrabbingControl)
                player.getControl("javax.media.control.FrameGrabbingControl");

        server = createServer();
        data = createDataMatrix(server);
    }

    private static ICiDataMatrix createDataMatrix(ICiServer server) {
        try {
            return server.CreateDataMatrix();
        } catch (JComException e) {
            throw new RuntimeException(e);
        }
    }

    private static ICiServer createServer(){
        try {
            return new CiServer().getICiServer();
        } catch (JComException e) {
            throw new RuntimeException(e);
        }
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

    public String[] decodeBarcode(BufferedImage img) throws Exception{
        if(img == null)
            throw new IllegalArgumentException("img is null!");

        //force RGB format
        BufferedImage rgb = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
        rgb.getGraphics().drawImage(img,0,0,null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageIO.write(rgb, "bmp", baos);

        //TODO if debug
//        OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("output-rgb.bmp")));
//        os.write(baos.toByteArray());
//        os.close();

        baos.flush();
        ICiImage iCiImage = server.CreateImage();

        iCiImage.CreateBpp(rgb.getWidth(), rgb.getHeight(),24);
        iCiImage.LoadFromMemory(baos.toByteArray());

        //TODO if debug
        iCiImage.SaveAs("ici-output.bmp",EFileFormat.ciEXT);

        data.setImage(iCiImage);

        int length = data.Find(0);
        String[] result = new String[length];

        for(int i = 0; i < length; ++i){
            ICiBarcode code = data.getBarcodes().getItem(i + 1);
            result[i] = code.getText();
        }

        return result;
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

    public void stop(){
        player.stop();
    }

    public void shutdown() {
        player.close();
        player.deallocate();
    }
}
