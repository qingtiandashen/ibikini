package com.andrea.ibikini.plugin.extension

/**
 * 压缩配置
 */
class CompressExtension {

    /**
     * 扩展名
     */
    public static final EXTENSION_NAME = "compressConfig"

    /**
     * 是否开启压缩：默认false不开启
     */
    boolean compressEnable

//    /**
//     * 选择tiny压缩
//     */
//    TinyChannelExtension tiny

    /**
     * 压缩白名单，不压缩名单，默认为空
     */
    Iterable<String> whiteList

    /**
     * 最小压缩尺寸，默认为0，全都压缩
     */
    int minCompressSize = 0

    CompressExtension() {
        whiteList = []
    }


    @Override
    public String toString() {
        return "CompressExtension{" +
                "compressEnable=" + compressEnable +
                ", whiteList=" + whiteList +
                ", minCompressSize=" + minCompressSize +
                '}'
    }
}