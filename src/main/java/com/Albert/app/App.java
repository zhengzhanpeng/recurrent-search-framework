package com.Albert.app;

import com.Albert.search.boxSearch.EntirelySearch;
import com.Albert.searchImpl.boxSearchImpl.ConcurrentEntirelySearch;
import com.Albert.searchModel.DesktopSearchModel;
import com.Albert.searchModel.SearchModel;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 */
public class App {
    public static void main(String[] args) {
        SearchModel searchModel = new DesktopSearchModel();
        EntirelySearch<String, File> search = new ConcurrentEntirelySearch(searchModel, "D:\\");

        List<File> list = search.getResultsUntilEnoughOrOneTimeout("README.html", 3, 5, TimeUnit.SECONDS);

        System.out.println("------find size:" + list.size());
        list.forEach(file -> System.out.println(file.getPath()));
    }
}
