package com.Albert.search.openSearch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 */
public interface OpenSearch<KeyT, ResultT, PathT> {
    ResultT getAResult(PathT pathList, KeyT keyT);
    ResultT getAResult(List<PathT> pathList, KeyT keyT);
    ResultT getAResultUntilTimeout(PathT pathList, KeyT keyT, long timeout, TimeUnit unit);
    ResultT getAResultUntilTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit);
}
