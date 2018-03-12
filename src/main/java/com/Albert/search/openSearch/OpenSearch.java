package com.Albert.search.openSearch;

/**
 * @author Albert
 * @create 2018-03-11 13:39
 */
public interface OpenSearch<PathT, KeyT, ResultT> {
    ResultT getAResult(PathT pathT, KeyT keyT);

    ResultT getAResultUntilTimeout(PathT pathT, KeyT keyT);
}
