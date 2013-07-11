package hzg.wpn.util.decoder;

import ClearImageJNI.*;
import ezjcom.JComException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;

/**
 * Backed by ClearImage Java API
 *
 * Constructor may throw {@link RuntimeException}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.07.13
 */
public class DataMatrixDecoder implements Closeable {
    private final ICiServer server = createServer();
    private final ICiDataMatrix data = createDataMatrix(server);

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

    /**
     *
     * @param img an image with DataMatrix to decode
     * @return an array with decoded values
     * @throws DecodingException
     */
    public String[] decode(BufferedImage img) throws DecodingException{
        if(img == null)
            throw new IllegalArgumentException("img is null!");

        //force RGB format
        BufferedImage rgb = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
        rgb.getGraphics().drawImage(img,0,0,null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            //TODO if debug
//        OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("output-rgb.bmp")));
//        os.write(baos.toByteArray());
//        os.close();

            ImageIO.write(rgb, "bmp", baos);
            baos.flush();

            ICiImage iCiImage = server.CreateImage();

            iCiImage.CreateBpp(rgb.getWidth(), rgb.getHeight(),24);
            iCiImage.LoadFromMemory(baos.toByteArray());

            //TODO if debug
            iCiImage.SaveAs("ici-output.bmp", EFileFormat.ciEXT);

            data.setImage(iCiImage);

            int length = data.Find(0);
            String[] result = new String[length];

            for(int i = 0; i < length; ++i){
                ICiBarcode code = data.getBarcodes().getItem(i + 1);
                result[i] = code.getText();
            }

            return result;
            //TODO handle recoverable non-recoverable exceptions
        } catch (JComException e) {
            throw new DecodingException("Can not decode image", e);
        } catch (IOException e){
            throw new DecodingException("Can not decode image", e);
        }

    }

    public void close(){
        server.__release();
    }

    public static class DecodingException extends Exception {
        public DecodingException(String msg, Throwable cause) {
            super(msg,cause);
        }
    }
}
