package de.diavololoop.chloroplast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Chloroplast on 05.02.2018.
 */
public class Util {

    public static String readInputStream(InputStream in) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int len;

        while (-1 != (len = in.read(b))) {
            buffer.write(b, 0, len);
        }

        return new String(buffer.toByteArray());

    }

}
