package WebCam;

import fr.esrf.TangoDs.Attribute;
import hzg.wpn.tango.camera.webcam.Engine;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.DeviceClass;
import fr.esrf.TangoDs.DeviceImpl;
import hzg.wpn.tango.camera.webcam.Launcher;
import wpn.hdri.tango.attribute.EnumAttrWriteType;
import wpn.hdri.tango.attribute.TangoAttribute;
import wpn.hdri.tango.attribute.TangoAttributeListener;
import wpn.hdri.tango.data.format.TangoDataFormat;
import wpn.hdri.tango.data.type.ImageTangoDataTypes;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public class WebCam extends DeviceImpl {
    public WebCam(DeviceClass deviceClass, String s) throws DevFailed {
        super(deviceClass, s);
    }

    public WebCam(DeviceClass deviceClass, String s, String s1) throws DevFailed {
        super(deviceClass, s, s1);
    }

    public WebCam(DeviceClass deviceClass, String s, String s1, DevState devState, String s2) throws DevFailed {
        super(deviceClass, s, s1, devState, s2);
    }

    @Override
    public void init_device() throws DevFailed {
        set_state(DevState.ON);
    }

    @Override
    public void delete_device() throws DevFailed {
        set_state(DevState.OFF);
    }

    public void read_attr(Attribute attr) throws DevFailed {
        String attr_name = attr.get_name();
        get_logger().info("In read_attr for attribute " + attr_name);



        WebCamClass.attr.read(attr);
    }

    public Engine getEngine(){
        return Launcher.engine;
    }
}
