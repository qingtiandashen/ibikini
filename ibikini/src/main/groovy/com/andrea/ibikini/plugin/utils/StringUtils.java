package com.andrea.ibikini.plugin.utils;

import java.io.File;

public class StringUtils {

    public static String getModuleName(String url) {
        int index1 = url.indexOf(File.separator);
        int index2 = url.indexOf("/", 1);
        return url.substring(index1 + 1, index2);
    }

    public static String formatSize(long size) {
        if (size < 1024) {
            return (size / 1024) + "k";
        } else {
            return (size / (1024 * 1024)) + "M";
        }
    }
}
