package com.Albert.searchImpl.boxSearchImpl;

import com.Albert.pojo.MessageOfSearched;
import com.Albert.search.boxSearch.EntirelySearch;
import com.Albert.searchModel.SearchModel;
import com.Albert.utils.ParameterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @author Albert
 * @create 2018-02-09 14:22
 */
public class ConcurrentEntirelySearch<KeySearchT, ResultT, CanBeSearchT> implements EntirelySearch<KeySearchT, ResultT> {
    private static final int NOT_LIMIT_EXPECT_NUM = 0;
    public static final int NOT_HAVE_TIMEOUT = 0;

    private final SearchModel<KeySearchT, ResultT, CanBeSearchT> searchModel;
    private final List<CanBeSearchT> rootCanBeSearch;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public ConcurrentEntirelySearch(SearchModel<KeySearchT, ResultT, CanBeSearchT> searchModel, List<CanBeSearchT> rootCanBeSearch) {
        this.searchModel = searchModel;
        this.rootCanBeSearch = rootCanBeSearch;
    }

    @Override
    public ResultT getAResult(KeySearchT keySearch) {
        SearchParameter parameter = createSearchRuleBeforeSearch(keySearch, NOT_HAVE_TIMEOUT, TimeUnit.MILLISECONDS, NOT_LIMIT_EXPECT_NUM);
        startSearch(parameter, rootCanBeSearch);
        ResultT resultT = getResultAndShutdownNowIfHaveGot(parameter);
        return resultT;
    }

    private SearchParameter createSearchRuleBeforeSearch(KeySearchT keySearchT, long timeout, TimeUnit unit, int exceptNum) {
        SearchParameter parameter = new SearchParameter();
        BlockingQueue<ResultT> resultQueue = new LinkedBlockingDeque<>();
        ExecutorService searchService = Executors.newCachedThreadPool();
        long timeoutAfterCheck = ParameterUtil.preventTimeoutTooLong(timeout, unit);

        parameter.setKeySearchT(keySearchT);
        parameter.setResultQueue(resultQueue);
        parameter.setSearchService(searchService);
        parameter.setTimeout(timeoutAfterCheck);
        parameter.setExceptNum(exceptNum);
        return parameter;
    }

    private void startSearch(SearchParameter parameter, List<CanBeSearchT> canBeSearchTList) {
        canBeSearchTList.forEach(canBeSearchT -> {
            parameter.searchService.submit(() -> {
                asyncSearch(canBeSearchT, parameter);
            });
        });
    }

    private ResultT getResultAndShutdownNowIfHaveGot(SearchParameter parameter) {
        ResultT resultT = null;
        try {
            resultT = parameter.resultQueue.poll(parameter.timeout, parameter.unit);
            parameter.searchService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return resultT;
        }
    }

    private void asyncSearch(CanBeSearchT canBeSearchT, SearchParameter parameter) {
        MessageOfSearched messageOfSearched = searchModel.search(parameter.keySearchT, canBeSearchT);
        putUsefulValueToQueue(parameter, messageOfSearched);
        executorCanBeSearch(parameter, messageOfSearched);
    }

    private void putUsefulValueToQueue(SearchParameter parameter, MessageOfSearched messageOfSearched) {
        Optional<List<ResultT>> resultOptional = messageOfSearched.getTrueResult();
        resultOptional.ifPresent(resultList -> {
            resultList.forEach(resultT -> {
                parameter.resultQueue.add(resultT);
            });
        });
    }

    private void executorCanBeSearch(SearchParameter parameter, MessageOfSearched messageOfSearched) {
        Optional<List<CanBeSearchT>> optional = messageOfSearched.getCanBeSearched();
        optional.ifPresent(list -> {
            list.forEach(search -> {
                parameter.searchService.submit(() -> {
                    asyncSearch(search, parameter);
                });
            });
        });
    }

    @Override
    public ResultT getAResultUntilTimeout(KeySearchT keySearchT, long timeout, TimeUnit timeUnit) throws TimeoutException {
        SearchParameter parameter = createSearchRuleBeforeSearch(keySearchT, timeout, timeUnit, NOT_LIMIT_EXPECT_NUM);
        startSearch(parameter, rootCanBeSearch);
        shutdownSearchWhenTimeout(parameter);
        ResultT resultT = getResultAndShutdownNowIfHaveGot(parameter);
        return resultT;
    }

    private void shutdownSearchWhenTimeout(SearchParameter parameter) {
        scheduledExecutorService.schedule(() -> {
            parameter.searchService.shutdownNow();
        }, parameter.timeout, parameter.unit);
    }

    @Override
    public List<ResultT> getResultsUntilOneTimeout(KeySearchT keySearchT, long timeout, TimeUnit unit) {
        SearchParameter parameter = createSearchRuleBeforeSearch(keySearchT, timeout, unit, NOT_LIMIT_EXPECT_NUM);
        startSearch(parameter, rootCanBeSearch);
        ArrayList<ResultT> list = putResultUntilOneTimeoutOrEnough(parameter);
        return list;
    }

    private ArrayList<ResultT> putResultUntilOneTimeoutOrEnough(SearchParameter parameter) {
        ArrayList<ResultT> list = new ArrayList<>();
        boolean isNotTimeout = true;
        boolean isNotEnough = true;
        try {
            while (isNotTimeout && isNotEnough) {
                ResultT resultT = parameter.resultQueue.poll(parameter.timeout, parameter.unit);
                if (Objects.nonNull(resultT)) {
                    list.add(resultT);
                    continue;
                }
                isNotTimeout = false;
                isNotEnough = isEnough(parameter.exceptNum, list);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            parameter.searchService.shutdownNow();
        }
        return list;
    }

    private boolean isEnough(int exceptNum, ArrayList<ResultT> list) {
        return exceptNum != 0 && list.size() >= exceptNum;
    }

    @Override
    public List<ResultT> getResultsUntilTimeout(KeySearchT keySearchT, long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrTimeout(KeySearchT keySearchT, int expectNum, long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrGitOneTimeout(KeySearchT keySearchT, int expectNum, long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public List<ResultT> getResultsUntilEnough(KeySearchT keySearchT, int expectNum) {
        return null;
    }

    private class SearchParameter {
        public KeySearchT keySearchT;
        public long timeout;
        public int exceptNum;
        public TimeUnit unit = TimeUnit.MILLISECONDS;
        public BlockingQueue<ResultT> resultQueue;
        public ExecutorService searchService;

        public void setKeySearchT(KeySearchT keySearchT) {
            this.keySearchT = keySearchT;
        }

        public void setResultQueue(BlockingQueue<ResultT> resultQueue) {
            this.resultQueue = resultQueue;
        }

        public void setSearchService(ExecutorService searchService) {
            this.searchService = searchService;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public void setExceptNum(int exceptNum) {
            this.exceptNum = exceptNum;
        }
    }
}
