package com.Albert.searchModel;

import com.Albert.pojo.MessageOfSearched;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Albert
 */
public class DesktopSearchModel implements SearchModel<String, String> {

    @Override
    public MessageOfSearched search(String key, String path) {
        File[] childFiles = getAllChildFile(path);
        List<File> trueResults  = getTrueResults(key, childFiles);
        List<String> paths = getPaths(childFiles);
        MessageOfSearched messageOfSearched = new MessageOfSearched(trueResults, paths);
        return messageOfSearched;
    }

    private File[] getAllChildFile(String canBeSearched) {
        File file = new File(canBeSearched);
        return file.listFiles();
    }

    private List<File> getTrueResults(String keySearch, File[] childFiles) {
        if (childFiles == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(childFiles)
                .filter(objectOfTest -> isTrueObject(keySearch, objectOfTest)).collect(Collectors.toList());
    }

    private List<String> getPaths(File[] childFiles) {
        if (childFiles == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(childFiles)
                .filter(objectOfTest -> objectOfTest.isDirectory())
                .map(objectOfTest -> objectOfTest.getPath()).collect(Collectors.toList());
    }

    public boolean remove(File removeTarget) {
        return removeTarget.delete();
    }

    public boolean add(File addTarget) {
        try {
            return addTarget.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return false;
        }
    }

    public boolean isTrueObject(String keySearch, File object) {
        if (keySearch != null && object != null) {
            return equalsName(keySearch, object);
        }
        return false;
    }

    private boolean equalsName(String keySearch, File object) {
        String objectName = object.getName();
        if (objectName.contains(keySearch)) {
            return true;
        }
        return false;
    }
}
