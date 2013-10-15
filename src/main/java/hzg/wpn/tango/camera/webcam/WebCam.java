package hzg.wpn.tango.camera.webcam;

import org.tango.DeviceState;
import org.tango.server.ServerManager;
import org.tango.server.annotation.*;
import org.tango.server.dynamic.DynamicManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.12.12
 */
@Device
public class WebCam {
    private Player player;

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
    @AttributeProperties(description = "returns currently used format.")
    public String getCurrentFormat() {
        return player.currentFormat();
    }


    @Init
    @StateMachine(endState = DeviceState.ON)
    public void init() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("webcam.properties"));

        this.player = Players.newInstance(properties.getProperty("adapter.impl"));

        this.player.init(properties);
    }

    @Delete
    public void delete() throws Exception {
        player.close();
    }

    @Command(inTypeDesc = "Argument is an index of the desired format from the supported formats array. May deadlock server if hardware does not support the desired format - this is the case when abstract driver is used.")
    @StateMachine(deniedStates = DeviceState.RUNNING)
    public void changeFormat(int id) throws Exception {
        player.setFormat(id);
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
        ImageIO.write(img, "jpeg", new File("capture-out.jpeg"));
        this.image = WebCamHelper.imageToRGBArray(img);
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
