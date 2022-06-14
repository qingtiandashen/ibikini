package com.andrea.ibikini.plugin.utils

class FileUtils {
    /**
     * 获取文件后缀的方法
     *
     * @param file 要获取文件后缀的文件
     * @return 文件后缀
     * @author https://www.4spaces.org/
     */
    public static String getFileExtension(File file) {
        String extension = ""
        try {
            if (file != null && file.exists()) {
                String name = file.getName()
                extension = name.substring(name.lastIndexOf(".") + 1)
            }
        } catch (Exception e) {
            extension = ""
        }
        return extension
    }


}