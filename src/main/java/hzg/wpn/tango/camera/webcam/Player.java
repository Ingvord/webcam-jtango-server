package hzg.wpn.tango.camera.webcam;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.Properties;

/**
 * Implements adapter pattern for different WebCam video capture libraries
 *
 * All implementations of this interface must have no-arg constructor.
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public interface Player extends Closeable {
    void init(Properties webcamProperties) throws Exception;

    void start() throws Exception;

    /**
     * Implementation must guarantee returning {@link BufferedImage.TYPE_INT_RGB} image
     *
     * @return
     * @throws Exception
     */
    BufferedImage capture() throws Exception;

    void stop() throws Exception;

    String[] supportedFormats();

    /**
     * This method does not guarantee that target format is really supported by the underlying hardware.
     *
     * May behave unexpectedly.
     *
     * @param id ndx of the desired format from {@link this#supportedFormats()} array
     * @throws Exception
     */
    void setFormat(int id) throws Exception;
}
