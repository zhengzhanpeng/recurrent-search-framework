package com.Albert.utils;

import java.io.File;
import java.io.IOException;

/**
 * @author Albert
 */
public class RunEnvironmentUtil {
    private static final String locationForWindows = "D://dirBeUsedTest";
    private static final String locationForMac = "/Users/test";

    public static final String locationBeUse = locationForMac;

    public static final String[] fileNames = {locationBeUse};

    public static void runBefore() throws IOException {
        File dirFile = new File(locationBeUse);
        dirFile.mkdir();
        File readMeFile = new File(locationBeUse + "/README.md");
        readMeFile.createNewFile();
        File beUsedDeleteFile = new File(locationBeUse + "/delete.md");
        beUsedDeleteFile.createNewFile();
    }

    public static void runAfter() {
        File readMeFile = new File(locationBeUse + "/README.md");
        readMeFile.delete();
        File beUsedDeleteFile = new File(locationBeUse + "/delete.md");
        beUsedDeleteFile.delete();
        File dirFile = new File(locationBeUse);
        dirFile.delete();
    }
}
