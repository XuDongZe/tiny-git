package com.xdz.tinygit.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Description: file util<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/17 13:30<br/>
 * Version: 1.0<br/>
 */
public class FileUtil {
    public static String read(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void write(String fileName, String content) {
        try {
            createFile(fileName);
            Files.write(Paths.get(fileName), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean createDir(String path) {
        File file = new File(path);
        if (file.exists()) {
            return false;
        }
        return file.mkdirs();
    }

    public static boolean createFile(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                return false;
            }
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
