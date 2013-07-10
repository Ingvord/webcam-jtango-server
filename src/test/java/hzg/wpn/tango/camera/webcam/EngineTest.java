package hzg.wpn.tango.camera.webcam;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public class EngineTest {
    private Engine engine = null;

    @Before
    public void before() throws Exception{
        PlayerAdapter player = new JmfPlayerAdapterImpl();

        engine = new Engine(player);

        Properties properties = new Properties();
        properties.setProperty("capture.device","vfw:Microsoft WDM Image Capture (Win32):0");

        engine.init(properties);
        engine.start();
    }

    @org.junit.Test
    public void testGetImage() throws Exception {
        Thread.sleep(1000);

        engine.captureImage();

        int[][] image = engine.getImageAsRGBArray(engine.getLastCapturedImage());



    }

    @Test
    public void testDecode(){

    }

    @After
    public void after() throws Exception{
        engine.stop();
        engine.shutdown();
    }
}
