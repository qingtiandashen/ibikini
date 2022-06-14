package com.andrea.ibikini.plugin.utils

import org.gradle.api.Project
import org.gradle.api.logging.Logger

class BiLog {
    static TAG = "BiLog"

    static Logger logger

    static void init(Project project) {
        logger = project.getLogger()
    }

//    static void i(String TAG, String info) {
//        if (null != info && null != logger) {
//            logger.debug(info)
//        }
//    }

    static void w(String TAG, String warning) {
        if (null != warning && null != logger) {
            logger.warn("${TAG}:: >>> " + warning)
        }
    }

    static void e(String TAG, String error) {
        if (null != error && null != logger) {
            logger.error("================================================================================================================================")
            logger.error(TAG)
            logger.error(error)
            logger.error("================================================================================================================================")
        }
    }


}