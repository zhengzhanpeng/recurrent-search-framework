package com.Albert.searchImpl.boxSearchImpl;

import com.Albert.utils.RunEnvironmentUtil;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.Duration.ofMillis;

class ConcurrentEntirelySearchTest {
    final ConcurrentEntirelySearch<String, File, String> searchService = new ConcurrentEntirelySearch<>(RunEnvironmentUtil.rootCanBeSearched, RunEnvironmentUtil.searchModel);
    private final String key = "README";
    private final String keyNotExist = "SDFSDFSDF.SDFSF";

    @BeforeAll
    static void initCreateFileOfTest() throws IOException {
        RunEnvironmentUtil.runBefore();
    }

    @AfterAll
    static void deleteFileOfTest() {
        RunEnvironmentUtil.runAfter();
    }

    @Test
    void getAResult() {
        String key = "README";
        File result = searchService.getAResult(key);
        Assertions.assertEquals(result != null, true);
    }

    @Test
    void getAResultUntilTimeout() throws TimeoutException {
        File result = searchService.getAResultUntilTimeout(key, 100, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(result != null);

        Assertions.assertTimeout(ofMillis(300), () -> {
            File nullResult = searchService.getAResultUntilTimeout(keyNotExist, 100, TimeUnit.MILLISECONDS);
            Assertions.assertEquals(null, nullResult);
        });
    }

    @Test
    void getResultsUntilOneTimeout() {
        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            list = searchService.getResultsUntilOneTimeout(key, 1000, TimeUnit.MILLISECONDS);
            Assertions.assertTrue(list.size() >= 1);
        });

        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            list = searchService.getResultsUntilOneTimeout(keyNotExist, 1000, TimeUnit.MILLISECONDS);
            Assertions.assertTrue(list.size() == 0);
        });
    }

    @Test
    void getResultsUntilTimeout() {
        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            list = searchService.getResultsUntilTimeout(key, 1000, TimeUnit.MILLISECONDS);
            Assertions.assertTrue(list.size() >= 1);
        });

        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            list = searchService.getResultsUntilTimeout(keyNotExist, 1000, TimeUnit.MILLISECONDS);
            Assertions.assertTrue(list.size() == 0);
        });
    }

    @Test
    void getResultsUntilEnoughOrTimeout() {
        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            list = searchService.getResultsUntilEnoughOrTimeout(key,1, 1000, TimeUnit.MILLISECONDS);
            Assertions.assertTrue(list.size() >= 1);
        });

        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            long starTime = System.currentTimeMillis();
            list = searchService.getResultsUntilEnoughOrTimeout(key,2, 1000, TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();
            long runTime = endTime - starTime;
            Assertions.assertTrue(list.size() >= 1);
            Assertions.assertTrue(runTime >= 1000);
        });
    }

    @Test
    void getResultsUntilEnoughOrOneTimeout() {
        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            list = searchService.getResultsUntilEnoughOrOneTimeout(key,1, 1000, TimeUnit.MILLISECONDS);
            Assertions.assertTrue(list.size() >= 1);
        });

        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            long starTime = System.currentTimeMillis();
            list = searchService.getResultsUntilEnoughOrOneTimeout(key,2, 1000, TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();
            long runTime = endTime - starTime;
            Assertions.assertTrue(list.size() >= 1);
            Assertions.assertTrue(runTime >= 1000);
        });
    }

    @Test
    void getResultsUntilEnough() {
        Assertions.assertTimeout(ofMillis(3000), () -> {
            List<File> list;
            list = searchService.getResultsUntilEnough(key,1);
            Assertions.assertTrue(list.size() >= 1);
        });
    }
}