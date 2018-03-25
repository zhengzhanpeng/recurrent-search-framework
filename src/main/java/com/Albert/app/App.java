package com.Albert.app;

import com.Albert.search.boxSearch.EntirelySearch;
import com.Albert.searchImpl.boxSearchImpl.ConcurrentEntirelySearch;
import com.Albert.searchModel.DesktopSearchModel;
import com.Albert.searchModel.SearchModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 * @create 2018-02-08 20:09
 */
public class App {
    public static void main(String[] args) {
        SearchModel searchModel = new DesktopSearchModel();
        List<String> rootSearch = new ArrayList<>();
        rootSearch.add("D:\\");
        EntirelySearch<String, File> search = new ConcurrentEntirelySearch(searchModel, rootSearch);
        List<File> list = search.getResultsUntilEnoughOrOneTimeout("README.html", 1, 2, TimeUnit.MINUTES);
        System.out.println("------find size:" + list.size());
        list.forEach(file -> System.out.println(file.getPath()));
    }
}
