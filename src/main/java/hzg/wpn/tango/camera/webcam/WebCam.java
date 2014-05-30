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

import org.tango.DeviceState;
import org.tango.server.ServerManager;
import org.tango.server.annotation.*;
import org.tango.server.dynamic.DynamicManager;
import sun.nio.ch.DirectBuffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * This class implements Tango device server. This server operates on webcam and is used during sample pre-characterization
 * stage of tomography experiments at Helmholtz-Zentrum Geesthacht.
 * <p/>
 * It implements the following use case: start server, configure format using format related attributes and command, start
 * capturing video (start command), capture frame, read image, stop capturing.
 * <p/>
 * The main component of this server is {@link Player}. This component encapsulates interaction with the camera. Several
 * implementations are available: {@link JmfPlayerImpl} is based on Java Media Framework which uses vfw driver and therefore
 * allows maximum resolution 640x480; {@link DsjPlayerImpl} uses DirectX and is unlimited in terms of maximum allowed resolution;
 * {@link XugglerPlayerImpl} is an experimental one and currently does not work, but it probably allows to port this server
 * to Linux. Users can choose the implementation they want setting webcam.properties accordingly.
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.12.12
 */
@Device
public class WebCam {
    private Player player;    

    @State
    private DeviceState state = DeviceState.OFF;

    @Attribute(maxDimX = 1600, maxDimY = 1200)
    private volatile int[][] image;

    @Attribute
    private volatile String pathToCapturedImage;

    private volatile long imageAddress;

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    @Attribute
    public String[] getSupportedFormats() {
        return player.supportedFormats();
    }

    @DynamicManagement
    private DynamicManager dynamicManagement;

    public void setDynamicManagement(DynamicManager dynamicManagement) {
        this.dynamicManagement = dynamicManagement;
    }

    @Attribute
    @AttributeProperties(description = "set a new format to the hardware. Argument is an index of the desired format in the supported formats array. May deadlock server if hardware does not support the desired format.")
    @StateMachine(deniedStates = DeviceState.RUNNING)
    public void setCurrentFormat(int id) throws Exception {
        player.setFormat(id);
    }

    @Init
    @StateMachine(endState = DeviceState.ON)
    public void init() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("webcam.properties"));

        this.player = Players.newInstance(properties.getProperty("adapter.impl"));

        this.player.init(properties);        
    }

    @Delete
    public void delete() throws Exception {
        player.close();        
    }

    @Command
    @StateMachine(endState = DeviceState.RUNNING)
    public void start() throws Exception {
        this.player.start();
    }

    @Command
    @StateMachine(endState = DeviceState.ON)
    public void stop() throws Exception {
        this.player.stop();
    }

    @Command
    @StateMachine(deniedStates = DeviceState.ON)
    public void capture() throws Exception {
        BufferedImage bufferedImage = player.capture();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", bos);
        ByteBuffer buffer = ByteBuffer.allocateDirect(bos.size());
        buffer.put(bos.toByteArray());
        imageAddress = ((DirectBuffer) buffer).address();
        //TODO if debug
        Path tmpImg = Files.createTempFile("capture-out-", ".jpeg");
        ImageIO.write(bufferedImage, "jpeg", tmpImg.toFile());
        this.pathToCapturedImage = tmpImg.toAbsolutePath().toString();
        this.image = WebCamHelper.imageToRGBArray(bufferedImage);
    }

    public String getPathToCapturedImage() {
        return pathToCapturedImage;
    }

    public int[][] getImage() {
        return this.image;
    }

    @Attribute
    public long getImageAdress() throws IOException {
        return imageAddress;
    }

    public static void main(String... args) {
        ServerManager.getInstance().start(args, WebCam.class);
    }
}
