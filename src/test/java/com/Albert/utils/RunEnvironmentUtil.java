package com.Albert.utils;

import com.Albert.searchModel.DesktopSearchModel;
import com.Albert.searchModel.SearchModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Albert
 */
public class RunEnvironmentUtil {
    private static final String locationForWindows = "D://dirBeUsedTest";
    private static final String locationForMac = "/Users/test";

    public static final String locationBeUse = locationForMac;

    public static final SearchModel searchModel = new DesktopSearchModel();
    public static final String[] fileNames = {locationBeUse};
    public static final List<String> rootCanBeSearched = Arrays.asList(fileNames);

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
