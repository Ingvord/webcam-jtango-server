package hzg.wpn.tango.camera.webcam;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.Properties;

/**
 * Implements adapter pattern for different WebCam video capture libraries
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public interface PlayerAdapter extends Closeable {
    void init(Properties webcamProperties) throws Exception;

    void start() throws Exception;

    BufferedImage capture() throws Exception;

    void stop() throws Exception;
}
