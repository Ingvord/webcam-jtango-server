package hzg.wpn.tango.camera.webcam;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.videoInputLib;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.04.2015
 */
public class JavaCVPlayerImpl implements Player, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JavaCVPlayerImpl.class);

    private final String[] supportedFormats = {
            "640x480",
            "800x600",
            "1280x720"
    };
    private final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private int frameWidth = 800;
    private int frameHeight = 600;
    //    private final CanvasFrame frame = new CanvasFrame("Web Cam");
    private volatile BufferedImage bufImg;
    private volatile boolean running;

    public JavaCVPlayerImpl() {
    }

    @Override
    public void init(Properties webcamProperties) throws Exception {
        for (String webcamProperty : webcamProperties.stringPropertyNames()) {
            switch (webcamProperty) {
                case "JavaCVPlayerImpl.width":
                    frameWidth = Integer.parseInt(webcamProperties.getProperty(webcamProperty));
                    break;
                case "JavaCVPlayerImpl.height":
                    frameHeight = Integer.parseInt(webcamProperties.getProperty(webcamProperty));
                    break;
            }
        }
    }

    public void run() {
        try {
//            grabber.setImageWidth(frameWidth);
//            grabber.setImageHeight(frameHeight);
            grabber.start();
            while (running) {
                opencv_core.IplImage cvimg = grabber.grab();
                if (cvimg != null) {
                    bufImg = cvimg.getBufferedImage();
                }
            }
            grabber.stop();
            grabber.release();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        running = true;
        exec.submit(this);
    }

    @Override
    public BufferedImage capture() throws Exception {
        return bufImg;
    }

    @Override
    public void stop() throws Exception {
        running = false;
    }

    @Override
    public String[] supportedFormats() {
        return supportedFormats;
    }

    @Override
    public String currentFormat() {
        return grabber.getFormat();
    }

    @Override
    public void setFormat(int id) throws Exception {
        String format = supportedFormats[id];
        String[] dims = format.split("x");
        frameWidth = Integer.parseInt(dims[0]);
        frameHeight = Integer.parseInt(dims[1]);
    }

    @Override
    public void close() throws IOException {
        try {
            stop();
            exec.awaitTermination(3000, TimeUnit.MILLISECONDS);
            exec.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }
}
