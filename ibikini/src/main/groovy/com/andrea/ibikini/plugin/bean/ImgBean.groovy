package com.andrea.ibikini.plugin.bean

/**
 * 压缩报告
 */
class ImgBean {

    String convert//转换工具
    String prePath
    Long preSize
    String preFormat
    String currentPath
    Long currentSize
    String currentFormat

    ImgBean(String convert, String prePath, Long preSize, String preFormat, String currentPath, Long currentSize, String currentFormat) {
        this.convert = convert
        this.prePath = prePath
        this.preSize = preSize
        this.preFormat = preFormat
        this.currentPath = currentPath
        this.currentSize = currentSize
        this.currentFormat = currentFormat
    }
}