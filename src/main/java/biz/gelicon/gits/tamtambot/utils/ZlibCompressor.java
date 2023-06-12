package biz.gelicon.gits.tamtambot.utils;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Component
public class ZlibCompressor implements FileCompressor {
    private static void shovelInToOut(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[32768];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    @Override
    public void decompressFile(InputStream compressed, File raw) throws IOException {
        InputStream in =
                new InflaterInputStream(compressed);
        OutputStream out = new FileOutputStream(raw);
        shovelInToOut(in, out);
        in.close();
        out.close();
    }

    @Override
    public String decompressToString(InputStream compressed) throws IOException {
        StringBuilder result = new StringBuilder();
        byte[] buf = new byte[5];
        int rlen;
        while ((rlen = compressed.read(buf)) != -1) {
            result.append(new String(Arrays.copyOf(buf, rlen)));
        }
        return result.toString();
    }

    public void compressFile(File raw, File compressed) throws IOException {
        InputStream in = new FileInputStream(raw);
        OutputStream out =
                new DeflaterOutputStream(new FileOutputStream(compressed));
        shovelInToOut(in, out);
        in.close();
        out.close();
    }
}
