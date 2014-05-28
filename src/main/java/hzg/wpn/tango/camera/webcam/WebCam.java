package hzg.wpn.tango.camera.webcam;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DispLevel;
import hzg.wpn.util.decoder.CiDataMatrixDecoderImpl;
import hzg.wpn.util.decoder.DataMatrixDecoder;
import org.tango.DeviceState;
import org.tango.server.ServerManager;
import org.tango.server.StateMachineBehavior;
import org.tango.server.annotation.*;
import org.tango.server.command.CommandConfiguration;
import org.tango.server.command.ICommandBehavior;
import org.tango.server.dynamic.DynamicManager;
import org.tango.utils.DevFailedUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.12.12
 */
@Device
public class WebCam {
    private Player player;
    private DataMatrixDecoder decoder;

    @State
    private DeviceState state = DeviceState.OFF;

    @Attribute(maxDimX = 1600, maxDimY = 1200)
    private volatile int[][] image;

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    @Attribute
    public String[] getSupportedFormats() {
        return player.supportedFormats();
    }

    @DynamicManagement
    private DynamicManager dynamicManagement;

    public void setDynamicManagement(DynamicManager dynamicManagement) {
        this.dynamicManagement = dynamicManagement;
    }

    @Attribute
    @AttributeProperties(description = "set a new format to the hardware. Argument is an index of the desired format in the supported formats array. May deadlock server if hardware does not support the desired format.")
    @StateMachine(deniedStates = DeviceState.RUNNING)
    public void setCurrentFormat(int id) throws Exception {
        player.setFormat(id);
    }

    @Init
    @StateMachine(endState = DeviceState.ON)
    public void init() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("webcam.properties"));

        this.player = Players.newInstance(properties.getProperty("adapter.impl"));

        this.player.init(properties);

        if (Boolean.parseBoolean(properties.getProperty("datamatrix.decoder.enabled"))) {
            //TODO decoder factory
            this.decoder = new CiDataMatrixDecoderImpl();
            dynamicManagement.addCommand(new ICommandBehavior() {
                @Override
                public CommandConfiguration getConfiguration() throws DevFailed {
                    return new CommandConfiguration("decodeBarcode", Void.class, String[].class, "", "DevVarStringArray with decoded values if any", DispLevel.OPERATOR, false, Integer.MAX_VALUE);
                }

                @Override
                public Object execute(Object arg) throws DevFailed {
                    BufferedImage img = WebCamHelper.RGBArrayToImage(image);
                    try {
                        return decoder.decode(img);
                    } catch (DataMatrixDecoder.DecodingException e) {
                        throw DevFailedUtils.newDevFailed(e);
                    }
                }

                @Override
                public StateMachineBehavior getStateMachine() throws DevFailed {
                    return new StateMachineBehavior();
                }
            });
        }
    }

    @Delete
    public void delete() throws Exception {
        player.close();
        if (decoder != null)
            decoder.close();
    }

    @Command
    @StateMachine(endState = DeviceState.RUNNING)
    public void start() throws Exception {
        this.player.start();
    }

    @Command
    @StateMachine(endState = DeviceState.ON)
    public void stop() throws Exception {
        this.player.stop();
    }

    @Command
    @StateMachine(deniedStates = DeviceState.ON)
    public void capture() throws Exception {
        BufferedImage img = player.capture();
        //TODO if debug
        Path tmpImg = Files.createTempFile("capture-out", ".jpeg");
        ImageIO.write(img, "jpeg", tmpImg.toFile());
        this.image = WebCamHelper.getImageAsRGBArray(img);
    }

    public int[][] getImage() {
        return this.image;
    }

    public void setImage(int[][] v) {
        this.image = v;
    }

    public static void main(String... args) {
        ServerManager.getInstance().start(args, WebCam.class);
    }
}
