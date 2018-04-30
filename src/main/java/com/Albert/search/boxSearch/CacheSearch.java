package com.Albert.search.boxSearch;

/**
 * @author Albert
 */
public interface CacheSearch<KeyT, ResultT> extends Search<KeyT, ResultT> {
    void clearCache();
}
