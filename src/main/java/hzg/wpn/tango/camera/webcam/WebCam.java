package hzg.wpn.tango.camera.webcam;

import hzg.wpn.util.decoder.DataMatrixDecoder;
import org.tango.DeviceState;
import org.tango.server.ServerManager;
import org.tango.server.annotation.*;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

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

    public DeviceState getState(){
        return state;
    }

    public void setState(DeviceState state){
        this.state = state;
    }

    @Attribute
    public String[] getSupportedFormats(){
        return player.supportedFormats();
    }

    @Attribute
    public void setCurrentFormat(int id) throws Exception{
        player.setFormat(id);
    }

    @Init
    @StateMachine(endState = DeviceState.ON)
    public void init() throws Exception{
        Properties properties = new Properties();
        properties.load(new FileInputStream("webcam.properties"));

        this.player = Players.newInstance(properties.getProperty("adapter.impl"));

        this.player.init(properties);

        this.decoder = new DataMatrixDecoder();
    }

    @Delete
    public void delete() throws Exception{
        player.close();
        decoder.close();
    }

    @Command
    @StateMachine(endState = DeviceState.RUNNING)
    public void start() throws Exception{
        this.player.start();
    }

    @Command
    @StateMachine(endState = DeviceState.ON)
    public void stop() throws Exception{
        this.player.stop();
    }

    @Command
    @StateMachine(deniedStates = DeviceState.ON)
    public String[] decodeBarcode() throws Exception{
        BufferedImage img = WebCamHelper.RGBArrayToImage(image);
        return decoder.decode(img);
    }

    @Command
    @StateMachine(deniedStates = DeviceState.ON)
    public void capture() throws Exception{
        BufferedImage img = player.capture();
        this.image = WebCamHelper.getImageAsRGBArray(img);
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
