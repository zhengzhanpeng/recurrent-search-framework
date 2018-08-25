package com.Albert.search.openSearch;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Albert
 */
public interface EntirelyOpenSearch<KeyT, ResultT, PathT> extends OpenSearch<KeyT, ResultT, PathT> {
    List<ResultT> getResultsUntilTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit);

    List<ResultT> getResultsUntilOneTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit);

    List<ResultT> getResultsUntilEnoughOrTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit, int exceptNum);

    List<ResultT> getResultsUntilEnoughOrOneTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit, int exceptNum);

    List<ResultT> getResultsUntilEnough(List<PathT> pathList, KeyT keyT, int exceptNum) throws TimeoutException;
}
