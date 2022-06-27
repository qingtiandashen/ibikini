package com.andrea.ibikini.plugin.filter;

import java.io.File;
import java.io.FileFilter;

public class ImageFilter implements FileFilter {

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;//继续遍历
        }

        if (exclude(file)) {
            return false;
        }

        return match(file);
    }

    /**
     * 不包含的内容
     * .9图
     *
     * @param file
     * @return
     */
    public boolean exclude(File file) {
        return file.getName().toLowerCase().endsWith(".9.png");
    }

    public boolean match(File file) {
        return file.getName().toLowerCase().contains("src/main/res") ||
                file.getName().toLowerCase().endsWith("png") ||
                file.getName().toLowerCase().endsWith("jpg") ||
                file.getName().toLowerCase().endsWith("jpeg") ||
                file.getName().toLowerCase().endsWith("webp");
    }

}
