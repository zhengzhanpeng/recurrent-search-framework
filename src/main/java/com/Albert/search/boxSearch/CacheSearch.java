package com.Albert.search.boxSearch;

/**
 * @author Albert
 * @create 2018-02-08 16:54
 */
public interface CacheSearch<KeyT, ResultT> extends Search<KeyT, ResultT> {
    void clearCache();
}
