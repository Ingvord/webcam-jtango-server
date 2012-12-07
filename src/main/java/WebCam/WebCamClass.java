package WebCam;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoDs.DeviceClass;
import fr.esrf.TangoDs.Util;
import hzg.wpn.tango.camera.webcam.Launcher;
import wpn.hdri.tango.attribute.EnumAttrWriteType;
import wpn.hdri.tango.attribute.TangoAttribute;
import wpn.hdri.tango.attribute.TangoAttributeListener;
import wpn.hdri.tango.data.format.TangoDataFormat;
import wpn.hdri.tango.data.type.ImageTangoDataTypes;

import java.util.Vector;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public class WebCamClass extends DeviceClass {
    public static TangoAttribute<?> attr;

    /**
     * Singleton instance. Initialized in {@link this#init(String)}
     */
    private static WebCamClass _instance;

    protected WebCamClass(String s) throws DevFailed {
        super(s);
    }

    public static WebCamClass init(String class_name) throws DevFailed {
        if (_instance == null) {
            _instance = new WebCamClass(class_name);
        }
        return _instance;
    }

    public static WebCamClass instance() {
        if (_instance == null) {
            IllegalStateException ex = new IllegalStateException("_instance is null");
            throw ex;
        }
        return _instance;
    }


    @Override
    public void command_factory() {
        for (WebCamCommand cmd : WebCamCommand.values()) {
            command_list.add(cmd.toCommand());
        }
    }

    @Override
    public void attribute_factory(Vector attrs) throws DevFailed {
            attrs.add((attr = new TangoAttribute<int[][]>("image", TangoDataFormat.<int[][]>createImageDataFormat(),
                    ImageTangoDataTypes.INT_IMAGE, EnumAttrWriteType.READ, new TangoAttributeListener<int[][]>() {
                @Override
                public int[][] onLoad() {
                    Launcher.engine.captureImage();
                    return Launcher.engine.getImage();
                }

                @Override
                public void onSave(int[][] value) {
                    throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
                }
            })).toAttr());
    }

    @Override
    public void device_factory(String[] dev_list) throws DevFailed {
        for (String aDev : dev_list) {
            // Create device and add it into the device list
            //----------------------------------------------
            WebCam statusServer = new WebCam(this, aDev);
            device_list.addElement(statusServer);

            // Export device to the outside world
            //----------------------------------------------
            if (Util._UseDb)
                export_device(statusServer);
            else
                export_device(statusServer, aDev);
        }
    }
}
