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

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.Properties;

/**
 * Implements adapter pattern for different WebCam video capture libraries
 * <p/>
 * All implementations of this interface must have no-arg constructor.
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public interface Player extends Closeable {
    void init(Properties webcamProperties) throws Exception;

    void start() throws Exception;

    /**
     * Implementation must guarantee returning {@link BufferedImage.TYPE_INT_RGB} image
     *
     * @return
     * @throws Exception
     */
    BufferedImage capture() throws Exception;

    void stop() throws Exception;

    String[] supportedFormats();

    String currentFormat();

    /**
     * This method does not guarantee that target format is really supported by the underlying hardware.
     * <p/>
     * May behave unexpectedly.
     *
     * @param id ndx of the desired format from {@link this#supportedFormats()} array
     * @throws Exception
     */
    void setFormat(int id) throws Exception;
}
