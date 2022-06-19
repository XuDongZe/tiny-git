package com.xdz.tinygit.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Description: file util<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/17 13:30<br/>
 * Version: 1.0<br/>
 */
public class FileUtil {
    public static String read(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void write(String path, String content) {
        try {
            createFile(path);
            Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(String path) {
        return new File(path).delete();
    }

    public static boolean exist(String path) {
        return new File(path).exists();
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

    // list first-child files in directory
    public static List<String> listAllDirectFile(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory()) {
            return new ArrayList<>();
        }
        String[] arr = file.list();
        if (arr == null || arr.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.asList(arr);
    }

    public static void main(String[] args) {
        List<String> strings = listAllDirectFile(".idea");
        System.out.println(Arrays.toString(strings.toArray()));
    }
}
