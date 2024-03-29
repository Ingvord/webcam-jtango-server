package hzg.wpn.tango.camera.webcam;

import org.junit.Test;

/**
 * @author Ingvord
 * @since 31.05.14
 */
public class WebCamTest {
    //@Test
    public void testGetImageAddressAndSize() throws Exception {
        WebCam instance = new WebCam();
        instance.init();
        instance.start();

        instance.capture();

        long[] result = instance.getImageAddressAndSize();

        instance.stop();
    }
}
