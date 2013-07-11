package hzg.wpn.tango.camera.webcam;

import com.xuggle.xuggler.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public class XugglerPlayerImpl implements Player {
    public static final String DRIVER_NAME = "vfwcap";
    private IContainer container;
    private String deviceName;
    private IStreamCoder videoCoder;
    private int videoStreamId;
    private IVideoResampler resampler;

    @Override
    public void init(Properties webcamProperties) throws Exception {
        container = IContainer.make();

        IContainerFormat format = IContainerFormat.make();
        if (format.setInputFormat(DRIVER_NAME) < 0)
            throw new IllegalArgumentException("couldn't open webcam device: " + DRIVER_NAME);

        IMetaData params = IMetaData.make();

        params.setValue("framerate", "30/1");
        params.setValue("video_size", "1600x1200");

        // Open up the container
//        deviceName = webcamProperties.getProperty("capture.device");
        deviceName = "0";
        int retval = container.open(deviceName, IContainer.Type.READ, format,
                false, true, params, null);
        if (retval < 0)
        {
            // This little trick converts the non friendly integer return value into
            // a slightly more friendly object to get a human-readable error name
            IError error = IError.make(retval);
            throw new IllegalArgumentException("could not open file: " + deviceName + "; Error: " + error.getDescription());
        }

        // query how many streams the call to open found
        int numStreams = container.getNumStreams();

        // and iterate through the streams to find the first video stream
        videoStreamId = -1;
        videoCoder = null;
        for(int i = 0; i < numStreams; i++)
        {
            // Find the stream object
            IStream stream = container.getStream(i);
            // Get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
            {
                videoStreamId = i;
                videoCoder = coder;
                break;
            }
        }
        if (videoStreamId == -1)
            throw new Exception("could not find video stream in container: "+ deviceName);

        /*
        * Now we have found the video stream in this file.  Let's open up our decoder so it can
        * do work.
        */
        if (videoCoder.open() < 0)
            throw new Exception("could not open video decoder for container: "+ deviceName);

        resampler = null;
        if (videoCoder.getPixelType() != IPixelFormat.Type.RGB24)
        {
            // if this stream is not in BGR24, we're going to need to
            // convert it.  The VideoResampler does that for us.
            resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.RGB24,
                    videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
            if (resampler == null)
                throw new RuntimeException("could not create color space resampler for: " + deviceName);
        }


    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public BufferedImage capture() throws Exception {
        /*
     * Now, we start walking through the container looking at each packet.
     */
        IPacket packet = IPacket.make();
        while(container.readNextPacket(packet) >= 0)
        {
            /*
            * Now we have a packet, let's see if it belongs to our video stream
            */
            if (packet.getStreamIndex() == videoStreamId)
            {
                /*
                * We allocate a new picture to get the data out of Xuggler
                */
                IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(),
                        videoCoder.getWidth(), videoCoder.getHeight());

                int offset = 0;
                while(offset < packet.getSize())
                {
                    /*
                    * Now, we decode the video, checking for any errors.
                    *
                    */
                    int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
                    if (bytesDecoded < 0)
                        throw new RuntimeException("got error decoding video in: " + deviceName);
                    offset += bytesDecoded;

                    /*
                    * Some decoders will consume data in a packet, but will not be able to construct
                    * a full video picture yet.  Therefore you should always check if you
                    * got a complete picture from the decoder
                    */
                    if (picture.isComplete())
                    {
                        IVideoPicture newPic = picture;
                        /*
                        * If the resampler is not null, that means we didn't get the video in BGR24 format and
                        * need to convert it into BGR24 format.
                        */
                        if (resampler != null)
                        {
                            // we must resample
                            newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
                            if (resampler.resample(newPic, picture) < 0)
                                throw new RuntimeException("could not resample video from: " + deviceName);
                        }
                        if (newPic.getPixelType() != IPixelFormat.Type.RGB24)
                            throw new RuntimeException("could not decode video as RGB 24 bit data in: " + deviceName);

                        // Convert the BGR24 to an Java buffered image
                        BufferedImage javaImage = Utils.videoPictureToImage(newPic);

                        // and display it on the Java Swing window
                        return javaImage;
                    }
                }
            }
            else
            {
                /*
                * This packet isn't part of our video stream, so we just silently drop it.
                */
                do {} while(false);
            }

        }
        return null;
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void close() throws IOException {
            videoCoder.close();
            container.close();
    }
}
