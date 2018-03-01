package com.Albert.app;

import com.Albert.search.boxSearch.CacheEntirelySearch;
import com.Albert.searchImpl.boxSearchImpl.ConcurrentCacheEntirelySearch;
import com.Albert.searchModel.DesktopSearchModel;
import com.Albert.searchModel.SearchModel;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 * @create 2018-02-08 20:09
 */
public class App {
    public static void main(String[] args) {
        SearchModel searchModel = new DesktopSearchModel();
        String[] rootSearch = {"D:\\"};
        CacheEntirelySearch<String, File> search = new ConcurrentCacheEntirelySearch(searchModel, Arrays.asList(rootSearch));
        List<File> list = search.getResultsUntilEnoughOrGitOneTimeout("README.html", 1, 2, TimeUnit.MINUTES);
        System.out.println("------find size:" + list.size());
        list.forEach(file -> System.out.println(file.getPath()));
    }
}
