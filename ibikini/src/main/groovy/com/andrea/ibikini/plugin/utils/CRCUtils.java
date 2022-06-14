package com.andrea.ibikini.plugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class CRCUtils {

    public static String loadCRC32(File file) {
        CRC32 crc32 = new CRC32();
        FileInputStream inputStream = null;
        CheckedInputStream checkedinputstream = null;
        String crcStr = null;
        try {
            inputStream = new FileInputStream(file);
            checkedinputstream = new CheckedInputStream(inputStream, crc32);
            while (checkedinputstream.read() != -1) {
            }
            crcStr = Long.toHexString(crc32.getValue()).toUpperCase();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (checkedinputstream != null) {
                try {
                    checkedinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return crcStr;
    }

//    publicstaticvoid main(String[] args){
//        String path ="C:/Users/Administrator/Desktop/robots.txt";
//        System.out.println(loadCRC32(path));
//    }
}
