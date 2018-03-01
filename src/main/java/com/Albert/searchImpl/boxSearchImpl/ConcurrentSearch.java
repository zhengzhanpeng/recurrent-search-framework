package com.Albert.searchImpl.boxSearchImpl;

import com.Albert.search.boxSearch.Search;
import com.Albert.searchModel.SearchModel;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Albert
 * @create 2018-02-09 14:22
 */
public class ConcurrentSearch<KeySearchT, ResultT, CanBeSearchT> implements Search<KeySearchT, ResultT> {
    private final SearchModel<KeySearchT, ResultT, CanBeSearchT> searchModel;
    private List<CanBeSearchT> rootCanBeSearch;

    public ConcurrentSearch(SearchModel<KeySearchT, ResultT, CanBeSearchT> searchModel, List<CanBeSearchT> rootCanBeSearch) {
        this.searchModel = searchModel;
        this.rootCanBeSearch = rootCanBeSearch;
    }

    @Override
    public ResultT getAResult(KeySearchT keySearch) {
        return null;
    }

    @Override
    public ResultT getAResultUntilTimeout(KeySearchT keySearchT, long timeout, TimeUnit timeUnit) throws TimeoutException {
        return null;
    }

    public void setRootCanBeSearch(List<CanBeSearchT> rootCanBeSearch) {
        this.rootCanBeSearch = rootCanBeSearch;
    }
}
