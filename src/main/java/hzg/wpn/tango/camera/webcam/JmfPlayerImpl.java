package hzg.wpn.tango.camera.webcam;

import javax.media.*;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public class JmfPlayerImpl implements Player {
    private javax.media.Player player;
    private CaptureDeviceInfo di;
    private MediaLocator ml;
    private FrameGrabbingControl fgc;

    @Override
    public void init(Properties webcamProperties) throws Exception {
        di = CaptureDeviceManager.getDevice(webcamProperties.getProperty("capture.device"));
        ml = di.getLocator();

        player = createPlayer(ml);
        fgc = (FrameGrabbingControl)
                player.getControl("javax.media.control.FrameGrabbingControl");
    }

    private static javax.media.Player createPlayer(MediaLocator mediaLocator) {
        try {
            return Manager.createRealizedPlayer(mediaLocator);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot create player!");
        }
    }


    @Override
    public void start() throws Exception {
        player.start();
    }

    @Override
    public BufferedImage capture() throws Exception {
        Buffer buf = fgc.grabFrame();

        // Convert it to an image
        BufferToImage btoi = new BufferToImage((VideoFormat) buf.getFormat());

        BufferedImage image = (BufferedImage) btoi.createImage(buf);
        if (image == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            return capture();
        } else {
            return image;
        }
    }

    @Override
    public void stop() throws Exception {
        player.stop();
    }

    @Override
    public String[] supportedFormats() {
        Format[] formats = di.getFormats();
        String[] result = new String[formats.length];
        int i = -1;
        for (Format format : formats) {
            result[++i] = format.toString();
        }

        return result;
    }

    @Override
    public void setFormat(int id) throws Exception {
        throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
    }

    @Override
    public void close() throws IOException {
        player.deallocate();
    }
}
