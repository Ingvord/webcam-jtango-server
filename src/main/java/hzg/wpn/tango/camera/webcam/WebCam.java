package hzg.wpn.tango.camera.webcam;

import org.tango.DeviceState;
import org.tango.server.ServerManager;
import org.tango.server.annotation.*;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.12.12
 */
@Device
public class WebCam {
    private Engine engine;

    @State
    private DeviceState state = DeviceState.OFF;

    @Attribute(maxDimX = 1600, maxDimY = 1200)
    private volatile int[][] image;

    public DeviceState getState(){
        return state;
    }

    public void setState(DeviceState state){
        this.state = state;
    }

    @Init
    @StateMachine(endState = DeviceState.ON)
    public void init() throws Exception{
        Properties properties = new Properties();
        properties.load(new FileInputStream("webcam.properties"));

        this.engine = new Engine(properties.getProperty("capture.device"));
    }

    @Delete
    public void delete(){
        engine.shutdown();
    }

    @Command
    @StateMachine(endState = DeviceState.RUNNING)
    public void start(){
        this.engine.start();
    }

    @Command
    @StateMachine(endState = DeviceState.ON)
    public void stop(){
        this.engine.stop();
    }

    @Command
    @StateMachine(deniedStates = DeviceState.ON)
    public String[] decodeBarcode() throws Exception{
        BufferedImage img = engine.RGBArrayToImage(image);
        return this.engine.decodeBarcode(img);
    }

    @Command
    @StateMachine(deniedStates = DeviceState.ON)
    public void capture(){
        engine.captureImage();
        this.image = engine.getImageAsRGBArray(engine.getLastCapturedImage());
    }

    public int[][] getImage(){
        return this.image;
    }

    public void setImage(int[][] v){
        this.image = v;
    }

    public static void main(String ... args){
        ServerManager.getInstance().start(args, WebCam.class);
    }
}
