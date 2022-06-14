package com.andrea.ibikini.plugin.bean

/**
 * 压缩报告
 */
class ReportBean {

    /**
     * module名
     */
    String moduleName

    /**
     * 图片信息
     */
    List<ImgBean> imgList

    ReportBean(String moduleName, List<ImgBean> imgList) {
        this.moduleName = moduleName
        this.imgList = imgList
    }
}