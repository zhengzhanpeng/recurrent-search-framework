package com.Albert.searchImpl.boxSearchImpl;

import com.Albert.search.boxSearch.EntirelySearch;
import com.Albert.searchImpl.openSearchImpl.ConcurrentEntirelyOpenSearch;
import com.Albert.searchModel.SearchModel;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Albert
 */
public class ConcurrentEntirelySearch<KeyT, ResultT, PathT> implements EntirelySearch<KeyT, ResultT> {
    private final List<PathT> rootCanBeSearch;
    private final ConcurrentEntirelyOpenSearch<KeyT, ResultT, PathT> openSearch;

    public ConcurrentEntirelySearch(SearchModel searchModel, List<PathT> rootCanBeSearch) {
        this.rootCanBeSearch = rootCanBeSearch;
        this.openSearch = new ConcurrentEntirelyOpenSearch<>(searchModel);
    }

    @Override
    public ResultT getAResult(KeyT keySearch) {
        return openSearch.getAResult(rootCanBeSearch, keySearch);
    }

    @Override
    public ResultT getAResultUntilTimeout(KeyT keyT, long timeout, TimeUnit timeUnit) throws TimeoutException {
        return openSearch.getAResultUntilTimeout(rootCanBeSearch, keyT, timeout, timeUnit);
    }

    @Override
    public List<ResultT> getResultsUntilOneTimeout(KeyT keyT, long timeout, TimeUnit unit) {
        return openSearch.getResultsUntilOneTimeout(rootCanBeSearch, keyT, timeout, unit);
    }

    @Override
    public List<ResultT> getResultsUntilTimeout(KeyT keyT, long timeout, TimeUnit unit) {
        return openSearch.getResultsUntilTimeout(rootCanBeSearch, keyT, timeout, unit);
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrTimeout(KeyT keyT, int expectNum, long timeout, TimeUnit unit) {
        return openSearch.getResultsUntilEnoughOrTimeout(rootCanBeSearch, keyT, timeout, unit, expectNum);
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrOneTimeout(KeyT keyT, int expectNum, long timeout, TimeUnit unit) {
        return openSearch.getResultsUntilEnoughOrOneTimeout(rootCanBeSearch, keyT, timeout, unit, expectNum);
    }

    @Override
    public List<ResultT> getResultsUntilEnough(KeyT keyT, int expectNum) throws TimeoutException {
        return openSearch.getResultsUntilEnough(rootCanBeSearch, keyT, expectNum);
    }
}
