package com.Albert.search.boxSearch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Albert
 * @create 2018-02-08 16:54
 */
public interface Search<KeySearchT, ResultT> {
    ResultT getAResult(KeySearchT keySearch);

    ResultT getAResultUntilTimeout(KeySearchT keySearchT, long timeout, TimeUnit timeUnit) throws TimeoutException;
}
