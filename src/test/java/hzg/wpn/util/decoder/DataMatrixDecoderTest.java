package hzg.wpn.util.decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;

/**
 * This class is platform dependent and therefore is not included in regular tests.
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.07.13
 */
public class DataMatrixDecoderTest {
    static {
        System.setProperty("java.library.path","lib\\native\\win32");

        hackClassLoader();
    }

    private static void hackClassLoader(){
        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
            fieldSysPath.setAccessible( true );
            fieldSysPath.set( null, null );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) throws Exception {
        BufferedImage img = ImageIO.read(new File("target/test-classes/test-decode-DataMatrix.jpeg"));

        DataMatrixDecoder instance = new DataMatrixDecoder();

        String[] result = instance.decode(img);

        assertEquals("DMc00002149",result[0]);
    }
}
