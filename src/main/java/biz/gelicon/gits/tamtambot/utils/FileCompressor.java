package biz.gelicon.gits.tamtambot.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FileCompressor {
    void decompressFile(InputStream compressed, File raw) throws IOException;

    String decompressToString(InputStream compressed) throws IOException;
}
