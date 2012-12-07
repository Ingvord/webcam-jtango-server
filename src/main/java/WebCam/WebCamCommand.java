package WebCam;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.Command;
import org.apache.log4j.Logger;
import wpn.hdri.tango.command.AbsCommand;
import wpn.hdri.tango.data.type.ScalarTangoDataTypes;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 07.12.12
 */
public enum WebCamCommand {
    START(new AbsCommand<WebCam,Void,Void>("startCapturing",
            ScalarTangoDataTypes.VOID,ScalarTangoDataTypes.VOID,
            "","") {
        @Override
        protected Void executeInternal(WebCam instance, Void data, Logger log) throws DevFailed {
            instance.getEngine().start();
            instance.set_state(DevState.RUNNING);
            return null;
        }
    }),
    STOP(new AbsCommand<WebCam,Void,Void>("stopCapturing",
            ScalarTangoDataTypes.VOID,ScalarTangoDataTypes.VOID,
            "","") {
        @Override
        protected Void executeInternal(WebCam instance, Void data, Logger log) throws DevFailed {
            instance.getEngine().stop();
            instance.set_state(DevState.ON);
            return null;
        }
    });
    private final Command cmd;


    private WebCamCommand(Command cmd) {
        this.cmd = cmd;
    }

    public Command toCommand() {
        return cmd;
    }
}
