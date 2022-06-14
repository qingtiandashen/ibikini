package com.andrea.ibikini.plugin.utils;

public class StringUtils {

    public static String formatSize(long size) {
        if (size < 1024) {
            return (size / 1024) + "k";
        } else {
            return (size / (1024 * 1024)) + "M";
        }
    }
}
