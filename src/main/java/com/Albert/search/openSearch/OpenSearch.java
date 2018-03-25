package com.Albert.search.openSearch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 * @create 2018-03-11 13:39
 */
public interface OpenSearch<KeyT, ResultT, PathT> {
    ResultT getAResult(List<PathT> pathList, KeyT keyT);

    ResultT getAResultUntilTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit);
}
