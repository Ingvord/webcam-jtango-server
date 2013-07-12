package hzg.wpn.util.decoder;

import java.awt.image.BufferedImage;
import java.io.Closeable;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 12.07.13
 */
public interface DataMatrixDecoder extends Closeable {
    String[] decode(BufferedImage img) throws DecodingException;

    class DecodingException extends Exception {
        public DecodingException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
