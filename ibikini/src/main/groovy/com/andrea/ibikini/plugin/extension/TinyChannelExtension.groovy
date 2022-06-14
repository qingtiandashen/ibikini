package com.andrea.ibikini.plugin.extension

/**
 * 压缩渠道
 */
class TinyChannelExtension {
    /**
     * 扩展名
     */
    public static final EXTENSION_NAME = "tinyConfig"

    /**
     * 因为tiny有月500个的限制，可以多注册几个账号分开使用
     */
    Iterable<String> tinyKeys

    String config

    TinyChannelExtension() {
        tinyKeys = []
    }


    @Override
    public String toString() {
        return "TinyChannelExtension{" +
                "tinyKeys=" + tinyKeys +
                ", config='" + config + '\'' +
                '}'
    }
}