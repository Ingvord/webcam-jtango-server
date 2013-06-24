package hzg.wpn.tango.camera.webcam;

import org.tango.DeviceState;
import org.tango.server.ServerManager;
import org.tango.server.annotation.*;

import javax.imageio.ImageIO;
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

    @Attribute(maxDimX = 640, maxDimY = 480)
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
    public String[] decodeBarcode() throws Exception{
        return this.engine.decodeBarcode();
    }

    public int[][] getImage(){
        this.engine.captureImage();
        this.image = this.engine.getImage();
        return this.image;
    }




    @Delete
    public void delete(){
        engine.shutdown();
    }

    public static void main(String ... args){
        ServerManager.getInstance().start(args, WebCam.class);
    }
}
