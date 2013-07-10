package hzg.wpn.tango.camera.webcam;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public class PlayerAdapters {
    private PlayerAdapters(){}

    public static PlayerAdapter newInstance(String impl) throws Exception{
        return (PlayerAdapter)Class.forName(impl).newInstance();
    }
}
