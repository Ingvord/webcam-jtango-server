package hzg.wpn.tango.camera.webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 03.04.2015
 */
public class Pi4JPlayerImpl implements Player {
//    private JvPi jvpi;

    private final String width = "960";
    private final String height = "720";
    private final String timeout = Long.toString(Long.MAX_VALUE);
    private final String timelapse = "100";
    private final String encoding = "jpg";
    private final String output = "capture.jpeg";
    private final String quality = "75";
    // Remember to add filename and extension!
    private final String startInstruction = "/usr/bin/raspistill -h " + height + " -w " + width +
            " -e " + encoding + " -t " + timeout + " -tl " + timelapse + " -q " + quality + " -o " + output;
    private volatile Process capturePrc;

    @Override
    public void init(Properties webcamProperties) throws Exception {
        //TODO
    }

    @Override
    public void start() throws Exception {
        if (capturePrc != null && capturePrc.isAlive())
            throw new IllegalStateException("Can not start capturing while another process is already alive.");

        Runtime r = Runtime.getRuntime();
        capturePrc = r.exec(this.startInstruction);
    }

    @Override
    public BufferedImage capture() throws Exception {
        BufferedImage result = ImageIO.read(new File(output));
        return result;
    }

    @Override
    public void stop() throws Exception {
        if (capturePrc != null && capturePrc.isAlive())
            capturePrc.destroy();
        else
            throw new IllegalStateException("Capture process neither exists nor alive!");
    }

    @Override
    public String[] supportedFormats() {
        //TODO return supported formats
        return new String[0];
    }

    @Override
    public String currentFormat() {
        //TODO
        return "RGB " + width + "x" + height;
    }

    @Override
    public void setFormat(int id) throws Exception {
        //TODO
    }

    @Override
    public void close() throws IOException {

    }
}
