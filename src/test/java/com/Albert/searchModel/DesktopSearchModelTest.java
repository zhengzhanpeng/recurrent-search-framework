package com.Albert.searchModel;

import com.Albert.pojo.MessageOfSearched;
import com.Albert.utils.RunEnvironmentUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

class DesktopSearchModelTest {
    static DesktopSearchModel desktopSearchModel = new DesktopSearchModel();

    @BeforeAll
    static void initCreateFileOfTest() throws IOException {
        File dirFile = new File(RunEnvironmentUtil.locationBeUse);
        dirFile.mkdir();
        File readMeFile = new File(RunEnvironmentUtil.locationBeUse + "/fileOfBeUsedTest.txt");
        readMeFile.createNewFile();
        File beUsedDeleteFile = new File(RunEnvironmentUtil.locationBeUse + "/fileOfBeUsedDelete.txt");
        beUsedDeleteFile.createNewFile();
    }

    @AfterAll
    static void deleteFileOfTest() {
        File readMeFile = new File(RunEnvironmentUtil.locationBeUse + "/fileOfBeUsedTest.txt");
        readMeFile.delete();
        File beUsedDeleteFile = new File(RunEnvironmentUtil.locationBeUse + "/fileOfBeUsedDelete.txt");
        beUsedDeleteFile.delete();
        File dirFile = new File(RunEnvironmentUtil.locationBeUse + "");
        dirFile.delete();
    }

    @Test
    void search() {
        MessageOfSearched<File, String> messageOfSearched = desktopSearchModel.search("fileOfBeUsedTest", RunEnvironmentUtil.locationBeUse + "");
        Optional<List<File>> optionalFile = messageOfSearched.getTrueResult();
        List<File> list = optionalFile.get();
        Assertions.assertNotNull(list);
        Assertions.assertEquals("fileOfBeUsedTest.txt", list.get(0).getName());
    }

    @Test
    void remove() {
        File file = new File(RunEnvironmentUtil.locationBeUse + "/fileOfBeUsedDelete.txt");
        boolean result = file.delete();
        Assertions.assertEquals(true, result);
        createNewFile(file);
    }

    private void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void add() {
        File file = new File(RunEnvironmentUtil.locationBeUse + "/test");
        boolean result = file.isFile();
        Assertions.assertEquals(false, result);
        desktopSearchModel.add(file);
        result = file.isFile();
        Assertions.assertEquals(true, result);
        file.delete();
    }

    @Test
    void isTrueObject() {
        File file = new File(RunEnvironmentUtil.locationBeUse + "/fileOfBeUsedTest.txt");
        String keySearch = "fileOf";
        boolean result = desktopSearchModel.isTrueObject(keySearch, file);
        Assertions.assertEquals(true, result);
    }

    @Test
    void articleSearch() {
        ArticleSearchModel articleSearchModel = new ArticleSearchModel();
        String article = "This is a test article. This article is for testing.";
        String word = "article";
        int count = articleSearchModel.search(word, article);
        Assertions.assertEquals(2, count);
    }
}