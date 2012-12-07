package hzg.wpn.tango.camera.webcam;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.TangoConst;
import fr.esrf.TangoDs.Util;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public class Launcher {
    public static Engine engine = new Engine("vfw:Microsoft WDM Image Capture (Win32):0");

    public static void main(String... args) throws Exception {
        Util util = Util.init(new String[]{"development"},"WebCam");
        Util.set_serial_model(TangoConst.NO_SYNC);
        util.server_init();

        util.server_run();
    }
}
