package com.andrea.ibikini.plugin

import com.andrea.ibikini.plugin.bean.ImgBean
import com.andrea.ibikini.plugin.bean.ReportBean
import com.andrea.ibikini.plugin.extension.CompressExtension
import com.andrea.ibikini.plugin.extension.ConfigExtension
import com.andrea.ibikini.plugin.extension.TinyChannelExtension
import com.andrea.ibikini.plugin.filter.ImageFilter
import com.andrea.ibikini.plugin.utils.FileUtils
import com.andrea.ibikini.plugin.utils.BiLog
import com.andrea.ibikini.plugin.utils.StringUtils
import com.andrea.ibikini.plugin.utils.CRCUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tinify.AccountException
import com.tinify.ClientException
import com.tinify.ServerException
import com.tinify.Source
import com.tinify.Tinify
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.util.PatternSet


class TinyCompressTask implements Plugin<Project> {

    static TAG = "TinyCompressTask"
    static COMPRESS_DIR = "compress"
    static COMPRESS_LIST_TEXT = "compressList.txt"
    static COMPRESS_REPORT = "report_"
    static currentKeyIndex = 0

    static Set<String> recordList = new HashSet<>()//已经优化过的列表
    static List<ReportBean> reportList = new ArrayList<>()
    static Project mProject
    static Long startTimeForReport
    static List<File> targetFileList = new ArrayList<>()

    @Override
    void apply(Project project) {
        System.out.println("------------------开始----------------------")

        BiLog.init(project)
        mProject = project
        recordList.clear()
        reportList.clear()
        if (!mProject.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('generateTinkerApk: Android Application plugin required')
        }

        injectExtensions()
        registerEvaluateListener()

//        System.out.println("------------------结束了----------------------")
    }

    /**
     * 扩展extension
     * @param project
     */
    private static void injectExtensions() {
        //添加ibikini的配置支持
        mProject.extensions.create("ibikini", ConfigExtension)
//        //添加compress配置
        mProject.ibikini.extensions.create("compressConfig", CompressExtension)
//        //添加tiny的配置
        mProject.ibikini.compressConfig.extensions.create("tiny", TinyChannelExtension)
    }

    /**
     * 注册监听配置完成后的回调
     * @param project
     */
    private static void registerEvaluateListener() {
        mProject.afterEvaluate {
            registerCompressTask()
        }
    }

    /**
     * 注册压缩的task
     * @param project
     */
    private static void registerCompressTask() {
        mProject.tasks.create('tinyCompress') {
            setGroup('compress')
            setDescription('compress by tiny-png')
            doLast {
                BiLog.w(TAG, "project: " + mProject.getRootDir().name)
                preCompress()
            }
        }
    }

    /**
     * 压缩前的准备工作
     * @param project
     */
    private static void preCompress() {
        def compressEnable = mProject.ibikini.compressConfig.compressEnable
        if (!compressEnable) {
            return
        }
        def tinyKeys = mProject.ibikini.compressConfig.tiny.tinyKeys
        if (tinyKeys.isEmpty()) {//tinyPng没有key
            return
        }
        readFile()
        def isMultiProject = mProject.ibikini.compressConfig.isMultiProject
        if (isMultiProject) {
            collectMultiProject()
        } else {
            collectSingleProject()
        }
        startCompressProject()
    }

    /**
     * 压缩多项目
     */
    private static void collectMultiProject() {
        def fileDir = mProject.getRootDir()
        traverseFile(fileDir)
    }

    private static void traverseFile(File file) {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles(new ImageFilter());
            for (File f : fileList) {
                traverseFile(f);//递归调用
            }
        } else {
            targetFileList.add(file)
        }
    }

    private static void collectSingleProject() {
        //获取各module
        mProject.getRootProject().getSubprojects().eachWithIndex { Project entry, int i ->
            def moduleName = entry.project.name
            def file = entry.layout.projectDirectory.asFileTree
            def patternSet = new PatternSet()
            //不处理.9图
            patternSet.exclude("**/*.9.png")
            //只处理png,jpg,jpeg,JPEG
            patternSet.include("src/main/res/**/*.png")
            patternSet.include("src/main/res/**/*.jpg")
            patternSet.include("src/main/res/**/*.jpeg")
            patternSet.include("src/main/res/**/*.JPEG")
            patternSet.include("src/main/res/**/*.webp")

            def resultFile = file.matching(patternSet)
            resultFile.eachWithIndex { File picFile, int picIndex ->
                targetFileList.add(picFile)
            }
        }

    }

    /**
     * 压缩项目
     * 1，读取compressList.txt文件列表，获取已压缩文件
     * 2，只压缩未压缩过的文件并记录到压缩列表里面
     * 3，将本次压缩生成压缩报告
     * 4，保存新的压缩文件到compressList.txt文件列表
     */
    private static void startCompressProject() {
        startTimeForReport = System.currentTimeMillis()
        targetFileList.eachWithIndex { File picFile, int picIndex ->
            def fileRelativePath = picFile.path.replace(mProject.rootDir.path, "")
            //不在白名单，图片大小允许裁剪，没有优化过才优化
            if (!isWhiteList(picFile) && checkImgSize(picFile)) {
                //这样不参与压缩的不用计算crc32
                String crc32 = CRCUtils.loadCRC32(picFile)
                if (!checkImgPathExits(crc32)) {
                    def tinyKeys = mProject.ibikini.compressConfig.tiny.tinyKeys
//                    if (currentKeyIndex)
                    def result = resizePng(StringUtils.getModuleName(fileRelativePath), tinyKeys, currentKeyIndex, picFile, fileRelativePath)
                    if (result) {
                        String newCrc32 = CRCUtils.loadCRC32(picFile)//新文件的crc32值
                        addImgPath(newCrc32)//保存
                    } else {
                        if (currentKeyIndex + 1 > tinyKeyList.size()) {//key不够用
                            return false//退出循环
                        }
                    }
                }
            }
        }
        saveFile()//多余一步
        saveReport()
    }

    /**
     * 开始压缩图片
     * @param moduleName
     * @param tinyKeyList
     * @param keyIndex
     * @param picFile
     * @param fileRelativePath
     * @return
     */
    static boolean resizePng(String moduleName, List<String> tinyKeyList, int keyIndex, File picFile, String fileRelativePath) {
        try {
            if (keyIndex + 1 > tinyKeyList.size()) {//key不够用了
                BiLog.e(TAG, "账号不够用了")
                return false
            }
            currentKeyIndex = keyIndex
            def preSize = picFile.size()
            def preMediaType = FileUtils.getFileExtension(picFile)
            def tinyKey = tinyKeyList[keyIndex]
            Tinify.setKey(tinyKey)
            def filePath = picFile.getAbsolutePath()
            def startTime = System.currentTimeMillis()
            BiLog.w(TAG, "start press ------------${fileRelativePath}")
            Source source = Tinify.fromFile(filePath)
            if (source.result().size() > preSize) {
                BiLog.e(TAG, "压缩后的图片更大，不采纳： ${filePath}")
            } else {
                source.toFile(filePath)
            }
//            picFile
            def currSize = picFile.size()
            def currMediaType = FileUtils.getFileExtension(picFile)
            BiLog.w(TAG, "end press ------------耗时：${(System.currentTimeMillis() - startTime) / 1000}s 体积减少：${(preSize - source.result().size()) / 1024}k")
            def imgBean = new ImgBean("tinyPng", fileRelativePath, preSize, preMediaType, fileRelativePath, currSize, currMediaType)
            addReport(moduleName, imgBean)
            return true
        } catch (Exception e) {
            switch (e) {
                case AccountException:
                    BiLog.e(TAG, "AccountException>>> ${e.toString()}")
                    //直接
                    return resizePng(moduleName, tinyKeyList, keyIndex + 1, picFile, fileRelativePath)
                case ClientException:
                    BiLog.e(TAG, "ClientException>>> ${e.toString()}")
                    return false
                case ServerException:
                    BiLog.e(TAG, "ServerException>>> ${e.toString()}")
                    return false
                default:
                    BiLog.e(TAG, "Exception>>> ${e.toString()}")
                    return false
            }
        }
    }

    /**
     * 检查图片的大小是不是允许压缩
     * @return
     */
    private static boolean checkImgSize(File filePath) {
        def size = mProject.ibikini.compressConfig.minCompressSize
        return filePath.size() > size * 1024
    }

    /**
     * 检查图片的大小是不是允许压缩
     * @return
     */
    private static boolean isWhiteList(File filePath) {
        def whiteList = mProject.ibikini.compressConfig.whiteList
        def isWhiteList = false
        whiteList.forEach {
            if (filePath.absolutePath.endsWith(it)) {
                isWhiteList = true
            }
        }
        return isWhiteList
    }

    /**
     * 检查图片是否已经压缩过了
     * @return
     */
    private static boolean checkImgPathExits(String crc32) {
        return recordList.contains(crc32)
    }

    /**
     * 添加到记录列表
     * @return
     */
    private static void addImgPath(String crc32) {
        recordList.add(crc32)
        saveFile()//转换成功一个，保存一次
    }

    /**
     * 检查图片是否已经压缩过了
     * @return
     */
    private static void addReport(String name, ImgBean bean) {
//        println "reportList0 " + new Gson().toJson(bean)
        def addResult = false
        reportList.forEach {
            if (name == it.moduleName) {//找到module
                it.imgList.add(bean)
                addResult = true
            }
        }
//        println "reportList1 " + new Gson().toJson(reportList)
        if (!addResult) {
            def hashSet = new ArrayList<ImgBean>()
            hashSet.add(bean)
//            println "reportList2.1 " + new Gson().toJson(hashSet)
            reportList.add(new ReportBean(name, hashSet))
        }
//        println "reportList2 " + new Gson().toJson(reportList)
    }

    /**
     * 从文件里面读字符串转换成json
     * @return
     */
    private static void readFile() {
        def compressDir = new File(mProject.getRootDir(), COMPRESS_DIR)
        if (!compressDir.exists()) {//创建压缩记录文件夹
            compressDir.mkdirs()
        }
        def compressList = new File(compressDir, COMPRESS_LIST_TEXT)
        if (!compressList.exists()) {//创建图片压缩记录文件
            compressList.createNewFile()
        }

        def result = compressList.text
        if (result) {
            //创建gson对象，用于json处理
            Gson gson = new Gson()
            //将从json字符串中读取的数据转换为接送对象
//            JsonObject returnData = new JsonParser().parse(result).getAsJsonObject()
            //将json数组对象转换成list集合，gson提供fromJson方法进行放序列化
            List<String> retList = gson.fromJson(result, new TypeToken<List<String>>() {
            }.getType())
            recordList.clear()
            recordList.addAll(retList)
        }
    }

    /**
     * 将json写入到文件
     * @return
     */
    private static void saveFile() {
        def compressDir = new File(mProject.getRootDir(), COMPRESS_DIR)
        if (!compressDir.exists()) {//创建压缩记录文件夹
            compressDir.mkdirs()
        }
        def compressList = new File(compressDir, COMPRESS_LIST_TEXT)
        if (!compressList.exists()) {//创建图片压缩记录文件
            compressList.createNewFile()
        }

//        def file = new File(compressList)
        //创建gson对象，用于json处理
        Gson gson = new Gson()
        compressList.write(gson.toJson(recordList))
    }

    /**
     * 将json写入到文件
     * @return
     */
    private static void saveReport() {
        def compressDir = new File(mProject.getRootDir(), COMPRESS_DIR)
        if (!compressDir.exists()) {//创建压缩记录文件夹
            compressDir.mkdirs()
        }
        def date = new Date().format("yyyyMMdd_HHmmss")
        def compressList = new File(compressDir, COMPRESS_REPORT + date + ".txt")
        if (!compressList.exists()) {//创建图片压缩记录文件
            compressList.createNewFile()
        }

        //创建gson对象，用于json处理
        Gson gson = new Gson()
        compressList.write(gson.toJson(reportList))
        showTips()
    }

    private static void showTips() {
        def count = 0
        def space = 0L

        reportList.forEach {
            if (it.moduleName) {
                count += it.imgList.size()

                it.imgList.forEach { imgInfo ->
                    space += (imgInfo.preSize - imgInfo.currentSize)
                }
            }
        }
        BiLog.w(TAG, "共耗时(s)：${(System.currentTimeMillis() - startTimeForReport) / (1000)}")
        BiLog.w(TAG, "本次共压缩文件(个)：${count}")
        BiLog.w(TAG, "本次共节省空间：${StringUtils.formatSize(space)}")
    }
}