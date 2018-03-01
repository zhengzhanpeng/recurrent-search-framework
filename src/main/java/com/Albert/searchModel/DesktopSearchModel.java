package com.Albert.searchModel;

import com.Albert.pojo.MessageOfSearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Albert
 * @create 2018-02-03 20:17
 */
public class DesktopSearchModel implements SearchModel<String, File, String> {

    @Override
    public MessageOfSearch<File, String> search(String keySearch, String canBeSearched) {
        File[] childFiles = getAllChildFile(canBeSearched);
        List<File> trueResults  = getTrueResults(keySearch, childFiles);
        List<String> canBeSearcheds = getCanBeSearcheds(childFiles);
        MessageOfSearch messageOfSearch = new MessageOfSearch(trueResults, canBeSearcheds);
        return messageOfSearch;
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

    private List<String> getCanBeSearcheds(File[] childFiles) {
        if (childFiles == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(childFiles)
                .filter(objectOfTest -> objectOfTest.isDirectory())
                .map(objectOfTest -> objectOfTest.getPath()).collect(Collectors.toList());
    }

    @Override
    public boolean remove(File removeTarget) {
        return removeTarget.delete();
    }

    @Override
    public boolean add(File addTarget) {
        try {
            return addTarget.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return false;
        }
    }

    @Override
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
