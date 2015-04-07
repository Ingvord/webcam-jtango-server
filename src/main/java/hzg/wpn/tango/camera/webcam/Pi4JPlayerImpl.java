package hzg.wpn.tango.camera.webcam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
private static final Logger logger = LoggerFactory.getLogger(Pi4JPlayerImpl.class);

    private final String width = "960";
    private final String height = "720";
    private final String timeout = "0";
    private final String rotation = "180";
    //    private final String timelapse = "100";
    private final String encoding = "jpg";
    private final String output = "capture.jpeg";
    private final String quality = "75";
    // Remember to add filename and extension!
    private final String captureInstruction = "/usr/bin/raspistill -h " + height + " -w " + width + " -rot " + rotation +
            " -e " + encoding + " -t " + timeout + /*" -tl " + timelapse +*/ " -q " + quality + " -o " + output;
    private final String killInstruction = "killall raspistill";

    @Override
    public void init(Properties webcamProperties) throws Exception {
        //TODO
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public BufferedImage capture() throws Exception {
        Runtime r = Runtime.getRuntime();

        logger.info("Executing: " + this.captureInstruction);

        r.exec(this.captureInstruction);

        return ImageIO.read(new File(output));
    }

    @Override
    public void stop() throws Exception {
        Runtime r = Runtime.getRuntime();

        logger.info("Executing: " + this.killInstruction);

        r.exec(killInstruction);
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
        try {
            stop();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
