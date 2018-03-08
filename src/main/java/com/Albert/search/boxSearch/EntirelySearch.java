package com.Albert.search.boxSearch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 * @create 2018-02-08 16:53
 */
public interface EntirelySearch<KeySearchT, ResultT> extends Search<KeySearchT, ResultT> {
    List<ResultT> getResultsUntilOneTimeout(KeySearchT keySearchT, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilTimeout(KeySearchT keySearchT, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilEnoughOrTimeout(KeySearchT keySearchT, int expectNum, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilEnoughOrGitOneTimeout(KeySearchT keySearchT, int expectNum, long timeout, TimeUnit unit);
    List<ResultT> getResultsUntilEnough(KeySearchT keySearchT, int expectNum);
}
