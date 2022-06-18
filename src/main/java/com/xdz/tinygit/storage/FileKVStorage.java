package com.xdz.tinygit.storage;

import com.xdz.tinygit.util.FileUtil;

import java.io.IOException;

/**
 * Description: k-v storage implement by file<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/17 13:36<br/>
 * Version: 1.0<br/>
 *
 * <pre>
 *     K: file name
 *     V: file content. string
 * </pre>
 */
public class FileKVStorage implements IKVStorage<String, String> {
    @Override
    public String load(String key) {
        return FileUtil.read(key);
    }

    @Override
    public void store(String key, String value) {
        FileUtil.write(key, value);
    }
}
