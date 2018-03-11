package com.Albert.search.openSearch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 * @create 2018-03-11 14:10
 */
public interface EntirelyOpenSearch<PathT, KeyT, ResultT> extends OpenSearch<PathT, KeyT, ResultT> {
    List<ResultT> getResultsUntilTimeout(PathT pathT, KeyT keyT, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilOneTimeout(PathT pathT, KeyT keyT, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilEnoughOrTimeout(PathT pathT, KeyT keyT, long timeout, TimeUnit unit, int exceptNum);
    List<ResultT> getResultsUntilEnoughOrOneTimeout(PathT pathT, KeyT keyT, long timeout, TimeUnit unit, int exceptNum);
    List<ResultT> getResultsUntilEnough(PathT pathT, KeyT keyT, int exceptNum);
}
