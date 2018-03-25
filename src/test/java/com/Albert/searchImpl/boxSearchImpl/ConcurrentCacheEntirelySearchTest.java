package com.Albert.searchImpl.boxSearchImpl;

import com.Albert.searchModel.DesktopSearchModel;
import com.Albert.searchModel.SearchModel;
import com.Albert.utils.RunEnvironmentUtil;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.Duration.ofMillis;

class ConcurrentCacheEntirelySearchTest {
    final SearchModel searchModel = new DesktopSearchModel();
    final String[] fileNames = {RunEnvironmentUtil.locationBeUse};
    ConcurrentCacheEntirelySearch<String, File, String> concurrentCacheEntirelyOperator = null;
    private static final long ERROR_RANGE = 100;

    @BeforeAll
    static void initCreateFileOfTest() throws IOException {
        RunEnvironmentUtil.runBefore();
    }

    @AfterAll
    static void deleteFileOfTest() {
        RunEnvironmentUtil.runAfter();
    }

    @BeforeEach
    void makeSureOperatorIsNew() throws IOException {
        concurrentCacheEntirelyOperator = new ConcurrentCacheEntirelySearch(searchModel, Arrays.asList(fileNames));
    }

    private void createNewFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void clearCache() {
        concurrentCacheEntirelyOperator.clearCache();
        Assertions.assertEquals(true, concurrentCacheEntirelyOperator.isEmpty());
    }

    @Test
    void testGetResultsUntilGitOneTimeout() {
        long startTime = System.currentTimeMillis();
        String keyCanNotExist = "sdlfksdlfksd.sdfsd";
        int timeout = 1000;
        List<File> list = concurrentCacheEntirelyOperator.getResultsUntilOneTimeout(keyCanNotExist, timeout, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        Assertions.assertTrue(runTime >= timeout);
    }

    @Test
    void getResultsUntilTimeout() {
        long startTime = System.currentTimeMillis();
        String keyCanNotExist = "sdlfksdlfksd.sdfsd";
        int timeout = 1000;
        List<File> list = concurrentCacheEntirelyOperator.getResultsUntilTimeout(keyCanNotExist, timeout, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        Assertions.assertTrue(runTime >= timeout);
    }

    @Test
    void getResultsUntilEnoughOrTimeout() {
        String keyExist = "README";
        int expectNum = 1;
        int timeout = 1000 * 3;
        Assertions.assertTimeout(ofMillis(1000), () -> {
            long startTime = System.currentTimeMillis();
            List<File> list = concurrentCacheEntirelyOperator.getResultsUntilEnoughOrTimeout(keyExist, expectNum, timeout, TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime - ERROR_RANGE;
            Assertions.assertTrue(isEnoughAndNotTimeout(list, expectNum, runTime, timeout));
        });

        String keyCanNotExist = "sdlfksdlfksd.sdfsd";
        long startTime = System.currentTimeMillis();
        List<File> list = concurrentCacheEntirelyOperator.getResultsUntilEnoughOrTimeout(keyCanNotExist, expectNum, timeout, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime + ERROR_RANGE;
        Assertions.assertTrue(isTimeoutAndNotEnough(expectNum, list, runTime, timeout));
    }

    private boolean isEnoughAndNotTimeout(List<File> list, int expectNum, long runTime, long timeout) {
        return runTime < timeout && list != null && list.size() == expectNum;
    }

    private boolean isTimeoutAndNotEnough(int expectNum, List<File> list, long runTime, long timeout) {
        boolean notEnough = list == null || (list != null && list.size() <= expectNum);
        return runTime > timeout && notEnough;
    }

    @Test
    void getResultsUntilEnoughOrOneTimeout() {
        String keyExist = "README";
        int expectNum = 1;
        int timeout = 1000 * 3;
        Assertions.assertTimeout(ofMillis(1000), () -> {
            long startTime = System.currentTimeMillis();
            List<File> list = concurrentCacheEntirelyOperator.getResultsUntilEnoughOrOneTimeout(keyExist, expectNum, timeout, TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime - ERROR_RANGE;
            Assertions.assertTrue(isEnoughAndNotTimeout(list, expectNum, runTime, timeout));
        });

        String keyCanNotExist = "sdlfksdlfksd.sdfsd";
        long startTime = System.currentTimeMillis();
        List<File> list = concurrentCacheEntirelyOperator.getResultsUntilEnoughOrOneTimeout(keyCanNotExist, expectNum, timeout, TimeUnit.MILLISECONDS);
        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime + ERROR_RANGE;
        Assertions.assertTrue(isTimeoutAndNotEnough(expectNum, list, runTime, timeout));
    }

    @Test
    void testGetAResult() {
        String key = "README";
        File file = concurrentCacheEntirelyOperator.getAResult(key);
        Assertions.assertNotNull(file);
        String name = file.getName();
        Assertions.assertTrue(name.contains(key));
    }

    @Test
    void testGetResultsUntilEnough() {
        String key = "README";
        List<File> list = concurrentCacheEntirelyOperator.getResultsUntilEnough(key, 1);
        Assertions.assertTrue(list.size() == 1);
    }

    @Test
    void testGetAResultUntilTimeout() {
        String key = "README";
        File file = getFileWithTryCatch(key);
        Assertions.assertTrue(file != null);
        String keyNotExist = "ASDFSFSDFSFD";
        File fileIsNull = getFileWithTryCatch(keyNotExist);
        Assertions.assertTrue(fileIsNull == null);
    }

    private File getFileWithTryCatch(String key) {
        File file = null;
        try {
            file =  concurrentCacheEntirelyOperator.getAResultUntilTimeout(key, 2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {

        } finally {
            return file;
        }
    }
}