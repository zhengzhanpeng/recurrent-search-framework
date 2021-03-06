package com.Albert.search.boxSearch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Albert
 */
public interface Search<KeyT, ResultT> {
    ResultT getAResult(KeyT keySearch);

    ResultT getAResultUntilTimeout(KeyT keyT, long timeout, TimeUnit timeUnit) throws TimeoutException;
}
