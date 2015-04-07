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
    public static final String PI4_J_PROPERTIES = "/Pi4J.properties";
    //    private JvPi jvpi;
private static final Logger logger = LoggerFactory.getLogger(Pi4JPlayerImpl.class);
    private final String killInstruction = "killall raspistill";
    private String mode = "0";
    private String width = "960";
    private String height = "720";
    private String timeout = "0";
    private String rotation = "180";
    //    private final String timelapse = "100";
    private String encoding = "jpg";
    private String quality = "75";
    private String output = "capture.jpeg";
    private String[] supportedFormats;

    @Override
    public void init(Properties webcamProperties) throws Exception {
        for (String webcamProperty : webcamProperties.stringPropertyNames()) {
            switch (webcamProperty) {
                case "Pi4JPlayerImpl.width":
                    width = webcamProperties.getProperty(webcamProperty);
                    break;
                case "Pi4JPlayerImpl.height":
                    height = webcamProperties.getProperty(webcamProperty);
                    break;
                case "Pi4JPlayerImpl.rotation":
                    rotation = webcamProperties.getProperty(webcamProperty);
                    break;
                case "Pi4JPlayerImpl.timeout":
                    timeout = webcamProperties.getProperty(webcamProperty);
                    break;
                case "Pi4JPlayerImpl.encoding":
                    encoding = webcamProperties.getProperty(webcamProperty);
                    break;
                case "Pi4JPlayerImpl.quality":
                    quality = webcamProperties.getProperty(webcamProperty);
                    break;
            }
        }

        Properties pi4jProperties = new Properties();
        pi4jProperties.load(getClass().getResourceAsStream(PI4_J_PROPERTIES));

        int supportedFormatsSize = Integer.parseInt(pi4jProperties.getProperty("mode.total"));
        supportedFormats = new String[supportedFormatsSize];

        for (int i = 0; i < supportedFormatsSize; ++i) {
            String mode = pi4jProperties.getProperty("mode." + i);
            supportedFormats[i] = mode;
        }
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public BufferedImage capture() throws Exception {
        Runtime r = Runtime.getRuntime();

        logger.info("Executing: " + getCaptureInstruction());

        r.exec(getCaptureInstruction());

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
        return supportedFormats;
    }

    @Override
    public String currentFormat() {
        //TODO
        return "RGB " + width + "x" + height;
    }

    @Override
    public void setFormat(int id) throws Exception {
        mode = Integer.toString(id + 1);
    }

    @Override
    public void close() throws IOException {
        try {
            stop();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private String getCaptureInstruction() {
        return "/usr/bin/raspistill -h " + height + " -w " + width + " -rot " + rotation +
                " -md " + mode + " -e " + encoding + " -t " + timeout + /*" -tl " + timelapse +*/ " -q " + quality + " -o " + output;
    }
}
