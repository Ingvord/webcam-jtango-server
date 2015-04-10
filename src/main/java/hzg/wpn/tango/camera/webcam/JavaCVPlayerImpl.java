package hzg.wpn.tango.camera.webcam;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.04.2015
 */
public class JavaCVPlayerImpl implements Player {
    private final int frameWidth = 800;
    private final int frameHeight = 600;
    private final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    //    private final CanvasFrame frame = new CanvasFrame("Web Cam");
    private volatile boolean running = false;
    private volatile BufferedImage bufImg;

    public JavaCVPlayerImpl() {
    }

    @Override
    public void init(Properties webcamProperties) throws Exception {
        //TODO
    }

    @Override
    public void start() throws Exception {
        //TODO thread pool
        running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    grabber.setImageWidth(frameWidth);
                    grabber.setImageHeight(frameHeight);
                    grabber.start();
                    while (running) {

                        opencv_core.IplImage cvimg = grabber.grab();
                        if (cvimg != null) {

                            // cvFlip(cvimg, cvimg, 1); // mirror

                            // show image on window

                            bufImg = cvimg.getBufferedImage();
//                            frame.showImage(bufImg);
                        }
                    }
                    grabber.stop();
                    grabber.release();
//                    frame.dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
        throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
    }

    @Override
    public String currentFormat() {
        throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
    }

    @Override
    public void setFormat(int id) throws Exception {
        throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
    }

    @Override
    public void close() throws IOException {

    }
}
