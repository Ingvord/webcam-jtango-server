/*
 * The main contributor to this project is Institute of Materials Research,
 * Helmholtz-Zentrum Geesthacht,
 * Germany.
 *
 * This project is a contribution of the Helmholtz Association Centres and
 * Technische Universitaet Muenchen to the ESS Design Update Phase.
 *
 * The project's funding reference is FKZ05E11CG1.
 *
 * Copyright (c) 2013. Institute of Materials Research,
 * Helmholtz-Zentrum Geesthacht,
 * Germany.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package hzg.wpn.tango.camera.webcam;

import javax.media.*;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public class JmfPlayerImpl implements Player {
    private javax.media.Player player;
    private CaptureDeviceInfo di;
    private MediaLocator ml;
    private FrameGrabbingControl fgc;
    private FormatControl fc;
    private Format[] formats;

    @Override
    public void init(Properties webcamProperties) throws Exception {
        di = CaptureDeviceManager.getDevice(webcamProperties.getProperty("capture.device"));
        ml = di.getLocator();
        player = createPlayer(ml);
        fgc = (FrameGrabbingControl)
                player.getControl("javax.media.control.FrameGrabbingControl");
        fc = (FormatControl) player.getControl("javax.media.control.FormatControl");
        formats = fc.getSupportedFormats();
    }

    private static javax.media.Player createPlayer(MediaLocator mediaLocator) {
        try {
            return Manager.createRealizedPlayer(mediaLocator);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot create player!");
        }
    }


    @Override
    public void start() throws Exception {
        player.start();
    }

    @Override
    public BufferedImage capture() throws Exception {
        Buffer buf = fgc.grabFrame();

        // Convert it to an image
        BufferToImage btoi = new BufferToImage((VideoFormat) buf.getFormat());

        BufferedImage image = (BufferedImage) btoi.createImage(buf);
        if (image == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            return capture();
        } else {
            return image;
        }
    }

    @Override
    public void stop() throws Exception {
        player.stop();
    }

    @Override
    public String[] supportedFormats() {
        String[] result = new String[formats.length];
        int i = -1;
        for (Format format : formats) {
            result[++i] = format.toString();
        }

        return result;
    }

    /**
     * The format needs to be set before the javax.media.Player is started. After the player has been started, the FormatControl.setFormat() has no effect.
     *
     * @param id ndx of the desired format from {@link this#supportedFormats()} array
     * @throws Exception
     * @see <a href="http://popscan.blogspot.de/2012/02/capturing-webcam-image-with-java-media.html">code example</a>
     */
    @Override
    public void setFormat(int id) throws Exception {
        if (id < 0 || id >= formats.length)
            throw new IllegalArgumentException("Invalid format id! Check supportedFormats...");
        fc.setFormat(formats[id]);
    }

    @Override
    public String currentFormat() {
        return fc.getFormat().toString();
    }

    @Override
    public void close() throws IOException {
        player.deallocate();
    }
}
