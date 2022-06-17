package com.xdz.tinygit.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Description: tig settings<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/17 13:46<br/>
 * Version: 1.0<br/>
 */
public class TigSettingsUtil {
    public static Properties getSettings() {
        try {
            Properties properties = new Properties();
            properties.load(TigSettingsUtil.class.getResourceAsStream("/tig.properties"));
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Properties settings = getSettings();
        Object a = settings.get("version");
        System.out.println(a);
    }
}
