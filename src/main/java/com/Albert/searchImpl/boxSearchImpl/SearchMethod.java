package com.Albert.searchImpl.boxSearchImpl;

import com.Albert.pojo.MessageOfSearched;
import com.Albert.searchModel.SearchModel;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class SearchMethod<KeyT, ResultT, PathT> {
    private final List<PathT> rootCanBeSearched;
    private final SearchModel<KeyT, PathT> searchModel;
    private final ExecutorService searchService;

    private SearchMethod(SearchModel searchModel, List rootCanBeSearched) {
        this.searchModel = searchModel;
        this.searchService = Executors.newCachedThreadPool();
        this.rootCanBeSearched = rootCanBeSearched;
    }

    public static SearchMethod createSearchMethod(SearchModel searchModel, List rootCanBeSearched) {
        return new SearchMethod<>(searchModel, rootCanBeSearched);
    }

    public WeakReference<BlockingQueue<ResultT>> methodOfHowSearch(KeyT keySearch) {
        SearchMethod.KeyAndResults keyAndResults = initParameter(keySearch);
        startAllSearch(keyAndResults, rootCanBeSearched);
        return new WeakReference<>(keyAndResults.results);
    }

    private KeyAndResults initParameter(KeyT keySearch) {
        BlockingQueue<ResultT> results = new LinkedBlockingDeque<>();
        return new KeyAndResults(keySearch, results);
    }

    private void startAllSearch(KeyAndResults keyAndResults, List<PathT> canBeSearched) {
        canBeSearched.forEach(beSearched -> asyncSearchOne(keyAndResults, beSearched));
    }

    private void asyncSearchOne(KeyAndResults keyAndResults, PathT canBeSearched) {
        searchService.execute(() -> {
            MessageOfSearched<ResultT, PathT> messageOfSearched = searchModel.search(keyAndResults.keySearch, canBeSearched);
            saveSatisfyResultsIfExist(keyAndResults, messageOfSearched);
            continueSearchIfExist(keyAndResults, messageOfSearched);
        });
    }

    private void saveSatisfyResultsIfExist(KeyAndResults keyAndResults, MessageOfSearched<ResultT, PathT> messageOfSearched) {
        messageOfSearched.getTrueResult()
                         .ifPresent(currentResults -> {
                             for (ResultT trueResult : currentResults) {
                                 saveAResult(keyAndResults, trueResult);
                             }
                         });
    }

    private void continueSearchIfExist(KeyAndResults keyAndResults, MessageOfSearched<ResultT, PathT> messageOfSearched) {
        Optional<List<PathT>> canBeSearchedOptional = messageOfSearched.getCanBeSearched();
        canBeSearchedOptional.ifPresent(list -> {
            startAllSearch(keyAndResults, canBeSearchedOptional.get());
        });
    }

    private void saveAResult(KeyAndResults keyAndResults, ResultT trueResult) {
        try {
            keyAndResults.results.put(trueResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopSearch() {
        searchService.shutdown();
    }

    public void stopSearchNow() {
        searchService.shutdownNow();
    }

    private class KeyAndResults {

        final BlockingQueue<ResultT> results;

        final KeyT keySearch;

        public KeyAndResults(KeyT keySearch, BlockingQueue<ResultT> results) {
            this.results = results;
            this.keySearch = keySearch;
        }

    }
}
