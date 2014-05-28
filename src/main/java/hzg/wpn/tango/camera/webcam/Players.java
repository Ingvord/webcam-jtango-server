package hzg.wpn.tango.camera.webcam;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public class Players {
    private Players() {
    }

    public static Player newInstance(String impl) throws Exception {
        return (Player) Class.forName(impl).newInstance();
    }
}
