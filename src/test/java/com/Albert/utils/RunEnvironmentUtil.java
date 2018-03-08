package com.Albert.utils;

import com.Albert.searchModel.DesktopSearchModel;
import com.Albert.searchModel.SearchModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Albert
 * @create 2018-03-02 3:19 PM
 */
public class RunEnvironmentUtil {
    public final static SearchModel searchModel = new DesktopSearchModel();
    public final static String[] fileNames = {"D://dirBeUsedTest"};
    public final static List<String> rootCanBeSearched = Arrays.asList(fileNames);

    public static void runBefore() throws IOException {
        File dirFile = new File("D://dirBeUsedTest");
        dirFile.mkdir();
        File readMeFile = new File("D://dirBeUsedTest/README.md");
        readMeFile.createNewFile();
        File beUsedDeleteFile = new File("D://dirBeUsedTest/delete.md");
        beUsedDeleteFile.createNewFile();
    }

    public static void runAfter() {
        File readMeFile = new File("D://dirBeUsedTest/README.md");
        readMeFile.delete();
        File beUsedDeleteFile = new File("D://dirBeUsedTest/delete.md");
        beUsedDeleteFile.delete();
        File dirFile = new File("D://dirBeUsedTest");
        dirFile.delete();
    }
}
