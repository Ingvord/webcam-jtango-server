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

import de.humatic.dsj.*;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.07.13
 */
public class DsjPlayerImpl implements Player, PropertyChangeListener {
    static {
        DSEnvironment.unlockDLL("igor.khokhriakov@hzg.de", 687816, 2091745, 0);
    }

    private DSCapture graph;

    private DSCapture.CaptureDevice vDev;

    private DSFilter.DSPin activeOut;

    private DSMediaType[] mf;

    private int formatNdx = 0;
    private JFrame f;

    @Override
    public void init(Properties webcamProperties) throws Exception {
        DSFilterInfo[][] dsi = DSCapture.queryDevices();
//        dsi[0][0].setPreferredFormat(35);
        //TODO selectable capture device
        graph = new DSCapture(DSFiltergraph.DD7, dsi[0][0], false, DSFilterInfo.doNotRender(), this);
//        graph = new DSCapture(DSFiltergraph.JAVA_AUTODRAW & DSFiltergraph.D3D9 & DSCapture.MAX_RESIZEABLE & DSCapture.SKIP_AUDIO, null);
        f = new JFrame();
//        graph = DSCapture.fromUserDialog(f,DSFiltergraph.D3D9,null);

        vDev = graph.getActiveVideoDevice();

        DSFilter.DSPin previewOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_PREVIEW);

        DSFilter.DSPin captureOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_CAPTURE);

        activeOut = previewOut != null ? previewOut : captureOut;
//
        int pinIndex = activeOut.getIndex();
//
        DSFilterInfo.DSPinInfo usedPinInfo = vDev.getFilterInfo().getDownstreamPins()[pinIndex];
//
        mf = usedPinInfo.getFormats();

        //TODO selectable format
        vDev.setOutputFormat(activeOut, formatNdx);

        f.add(graph.asComponent());

        f.setVisible(true);
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public BufferedImage capture() throws Exception {
        BufferedImage bi = graph.getImage();

        BufferedImage result = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        result.getGraphics().drawImage(bi, 0, 0, graph);

        return result;
    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public String[] supportedFormats() {
        String[] result = new String[mf.length];

        int i = -1;
        for (DSMediaType mediaType : mf) {
            result[++i] = mediaType.getDisplayString();
        }
        return result;
    }

    @Override
    public void setFormat(int id) throws Exception {
        if (id < 0 || id > mf.length)
            throw new IllegalArgumentException("id is out of range[0," + mf.length + "]");

        formatNdx = id;
        vDev.setOutputFormat(activeOut, formatNdx);
    }

    @Override
    public String currentFormat() {
        return mf[formatNdx].getDisplayString();
    }

    @Override
    public void close() throws IOException {
        f.dispose();
        graph.dispose();
    }

    public void propertyChange(java.beans.PropertyChangeEvent pe) {
        if (DSJUtils.getEventType(pe) == DSFiltergraph.FORMAT_CHANGED) {
            f.add(graph.asComponent());

            f.pack();
        }
    }
}
