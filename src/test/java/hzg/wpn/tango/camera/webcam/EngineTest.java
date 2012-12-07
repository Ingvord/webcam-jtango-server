package hzg.wpn.tango.camera.webcam;

import org.junit.After;
import org.junit.Before;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public class EngineTest {
    private Engine engine = null;

    @Before
    public void before(){
        engine = new Engine("vfw:Microsoft WDM Image Capture (Win32):0");
        engine.start();
    }

    @org.junit.Test
    public void testGetImage() throws Exception {
        Thread.sleep(1000);

        engine.captureImage();

        int[][] image = engine.getImage();



    }

    @After
    public void after(){
        engine.stop();
    }
}
