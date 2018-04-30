package com.Albert.searchImpl.openSearchImpl;

import com.Albert.pojo.MessageOfSearched;
import com.Albert.search.openSearch.EntirelyOpenSearch;
import com.Albert.searchModel.SearchModel;
import com.Albert.utils.ParameterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @author Albert
 */
public class ConcurrentEntirelyOpenSearch<KeyT, ResultT, PathT> implements EntirelyOpenSearch<KeyT, ResultT, PathT> {

    private static final int NOT_LIMIT_EXPECT_NUM = 0;
    private static final int NOT_HAVE_TIMEOUT = 0;
    private static final long MAX_WAIT_MILLI = 3*1000*60;

    private final SearchModel<KeyT, PathT> searchModel;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService getService = Executors.newCachedThreadPool();

    public ConcurrentEntirelyOpenSearch(SearchModel searchModel) {
        this.searchModel = searchModel;
    }

    @Override
    public ResultT getAResult(List<PathT> pathList, KeyT keyT) {
        SearchParameter parameter = createSearchRuleBeforeSearch(keyT, NOT_HAVE_TIMEOUT, TimeUnit.MILLISECONDS, NOT_LIMIT_EXPECT_NUM);
        startSearch(parameter, pathList);
        ResultT resultT = getUtilHaveGot(parameter);
        return resultT;
    }

    @Override
    public ResultT getAResultUntilTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit) {
        SearchParameter parameter = createSearchRuleBeforeSearch(keyT, timeout, unit, NOT_LIMIT_EXPECT_NUM);
        startSearch(parameter, pathList);
        shutdownSearchWhenTimeout(parameter);
        ResultT resultT = getResultAndShutdownNowWhenHaveGot(parameter);
        return resultT;
    }

    @Override
    public List<ResultT> getResultsUntilTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit) {
        return getResultsUntilEnoughOrTimeout(pathList, keyT, timeout, unit, NOT_LIMIT_EXPECT_NUM);
    }

    @Override
    public List<ResultT> getResultsUntilOneTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit) {
        return getResultsUntilEnoughOrOneTimeout(pathList, keyT, timeout, unit, NOT_LIMIT_EXPECT_NUM);
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit, int exceptNum) {
        final List<ResultT> list = new ArrayList<>();
        SearchParameter parameter = createSearchRuleBeforeSearch(keyT, timeout, unit, exceptNum);
        startSearch(parameter, pathList);
        addResultToListWithTiming(list, parameter);
        return list;
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrOneTimeout(List<PathT> pathList, KeyT keyT, long timeout, TimeUnit unit, int exceptNum) {
        SearchParameter parameter = createSearchRuleBeforeSearch(keyT, timeout, unit, exceptNum);
        startSearch(parameter, pathList);
        ArrayList<ResultT> list = putResultUntilOneTimeoutOrEnough(parameter);
        return list;
    }

    @Override
    public List<ResultT> getResultsUntilEnough(List<PathT> pathList, KeyT keyT, int exceptNum) throws TimeoutException {
        final List<ResultT> list = new ArrayList<>();
        SearchParameter parameter = createSearchRuleBeforeSearch(keyT, MAX_WAIT_MILLI, TimeUnit.MILLISECONDS, exceptNum);
        startSearch(parameter, pathList);
        addResultToListWithTimingThrowTimeoutException(list, parameter);
        return list;
    }

    private SearchParameter createSearchRuleBeforeSearch(KeyT keyT, long timeout, TimeUnit unit, int exceptNum) {
        SearchParameter parameter = new SearchParameter();
        BlockingQueue<ResultT> resultQueue = new LinkedBlockingDeque<>();
        ExecutorService searchService = Executors.newCachedThreadPool();
        long timeoutAfterCheck = ParameterUtil.preventTimeoutTooLong(timeout, unit);

        parameter.setKeySearchT(keyT);
        parameter.setResultQueue(resultQueue);
        parameter.setSearchService(searchService);
        parameter.setTimeout(timeoutAfterCheck);
        parameter.setExceptNum(exceptNum);
        return parameter;
    }

    private void startSearch(SearchParameter parameter, List<PathT> pathTList) {
        pathTList.forEach(pathT -> {
            parameter.searchService.submit(() -> {
                asyncSearch(pathT, parameter);
            });
        });
    }

    private void asyncSearch(PathT pathT, SearchParameter parameter) {
        MessageOfSearched messageOfSearched = searchModel.search(parameter.keyT, pathT);
        putUsefulValueToQueue(parameter, messageOfSearched);
        executorCanBeSearch(parameter, messageOfSearched);
    }

    private ResultT getUtilHaveGot(SearchParameter parameter) {
        ResultT resultT = null;
        try {
            resultT = parameter.resultQueue.take();
            parameter.searchService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            return resultT;
        }
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
        Optional<List<PathT>> optional = messageOfSearched.getCanBeSearched();
        optional.ifPresent(list -> {
            list.forEach(search -> {
                parameter.searchService.submit(() -> {
                    asyncSearch(search, parameter);
                });
            });
        });
    }

    private void shutdownSearchWhenTimeout(SearchParameter parameter) {
        scheduledExecutorService.schedule(() -> {
            parameter.searchService.shutdownNow();
        }, parameter.timeout, parameter.unit);
    }

    private ResultT getResultAndShutdownNowWhenHaveGot(SearchParameter parameter) {
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

    private boolean isEnough(int exceptNum, List<ResultT> list) {
        return exceptNum != 0 && list.size() >= exceptNum;
    }

    private void addResultToListWithTiming(List<ResultT> list, SearchParameter parameter) {
        final Future<?> cancelFuture = submitAddResultToList(list, parameter);
        timingCancel(parameter, cancelFuture);
    }

    private Future<?> submitAddResultToList(List<ResultT> list, SearchParameter parameter) {
        return getService.submit(() -> {
            boolean isNotEnough = true;
            while (isNotEnough) {
                ResultT result = takeResultFromQueue(parameter);
                Optional.ofNullable(result).ifPresent(r -> list.add(r));
                isNotEnough = !isEnough(parameter.exceptNum, list);
            }
        });
    }

    private void addResultToListWithTimingThrowTimeoutException(List<ResultT> list, SearchParameter parameter) throws TimeoutException {
        final Future<?> cancelFuture = submitAddResultToList(list, parameter);
        timingCancelThrowTimeoutException(parameter, cancelFuture);
    }

    private ResultT takeResultFromQueue(SearchParameter parameter) {
        ResultT resultT = null;
        try {
            resultT = parameter.resultQueue.take();
        } catch (InterruptedException e) {
            System.out.println("The take method from queue is canceled");
        }
        return resultT;
    }

    private void timingCancel(SearchParameter parameter, Future<?> cancelFuture) {
        try {
            cancelFuture.get(parameter.timeout, parameter.unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {

        } finally {
            cancelFuture.cancel(true);
            parameter.searchService.shutdownNow();
        }
    }

    private void timingCancelThrowTimeoutException(SearchParameter parameter, Future<?> cancelFuture) throws TimeoutException {
        try {
            cancelFuture.get(parameter.timeout, parameter.unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            throw e;
        } finally {
            cancelFuture.cancel(true);
            parameter.searchService.shutdownNow();
        }
    }

    private class SearchParameter {
        public KeyT keyT;
        public long timeout;
        public int exceptNum;
        public TimeUnit unit = TimeUnit.MILLISECONDS;
        public BlockingQueue<ResultT> resultQueue;
        public ExecutorService searchService;

        public void setKeySearchT(KeyT keyT) {
            this.keyT = keyT;
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
