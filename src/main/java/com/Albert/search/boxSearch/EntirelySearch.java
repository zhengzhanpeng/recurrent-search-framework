package com.Albert.search.boxSearch;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Albert
 */
public interface EntirelySearch<KeyT, ResultT> extends Search<KeyT, ResultT> {
    List<ResultT> getResultsUntilOneTimeout(KeyT keyT, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilTimeout(KeyT keyT, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilEnoughOrTimeout(KeyT keyT, int expectNum, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilEnoughOrOneTimeout(KeyT keyT, int expectNum, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilEnough(KeyT keyT, int expectNum) throws TimeoutException;
}
