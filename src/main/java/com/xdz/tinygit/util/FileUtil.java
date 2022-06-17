package com.xdz.tinygit.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Description: file util<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/17 13:30<br/>
 * Version: 1.0<br/>
 */
public class FileUtil {
    public static String read(String fileName) throws IOException {
        return Files.readString(Path.of(fileName));
    }

    public static void write(String fileName, String content) throws IOException {
        Files.writeString(Path.of(fileName), content);
    }
}
