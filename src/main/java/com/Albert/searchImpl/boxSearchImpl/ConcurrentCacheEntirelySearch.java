package com.Albert.searchImpl.boxSearchImpl;

import com.Albert.cache.EfficientCacheCompute;
import com.Albert.pojo.RuleParameter;
import com.Albert.search.boxSearch.CacheEntirelySearch;
import com.Albert.searchModel.SearchModel;
import com.Albert.utils.ParameterUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Albert
 */
public class ConcurrentCacheEntirelySearch<KeyT, ResultT, PathT> implements CacheEntirelySearch<KeyT, ResultT> {

    private static final int NOT_LIMIT_EXPECT_NUM = 0;
    public static final int NOT_HAVE_TIMEOUT = 0;

    private final EfficientCacheCompute<KeyT, WeakReference<BlockingQueue<ResultT>>> cacheResults;
    private final ExecutorService gitService;
    private final SearchMethod<KeyT, ResultT, PathT> searchMethod;

    public ConcurrentCacheEntirelySearch(SearchModel searchModel, List<PathT> rootCanBeSearched) {
        searchMethod = SearchMethod.createSearchMethod(searchModel, rootCanBeSearched);
        this.cacheResults = EfficientCacheCompute.createNeedComputeFunction(searchMethod::methodOfHowSearch);
        this.gitService = Executors.newCachedThreadPool();
    }

    @Override
    public List<ResultT> getResultsUntilOneTimeout(KeyT keyT, long timeout, TimeUnit unit) {
        RuleParameter ruleParameter = createSearchRule(keyT, timeout, unit, NOT_LIMIT_EXPECT_NUM);

        List list = startGetResultsUntilOneTimeout(ruleParameter);
        unifyResultCache(ruleParameter, list);
        return list;
    }

    @Override
    public List<ResultT> getResultsUntilTimeout(KeyT keyT, long timeout, TimeUnit unit) {
        RuleParameter<ResultT> ruleParameter = createSearchRule(keyT, timeout, unit, NOT_LIMIT_EXPECT_NUM);

        final List<ResultT> resultList = new ArrayList<>();
        Future timingCancelFuture = gitService.submit(() -> {
            while (true) {
                resultList.add(ruleParameter.resultTBlockingQueue.take());
            }
        });
        startTimingCancel(timingCancelFuture, ruleParameter);
        unifyResultCache(ruleParameter, resultList);
        return resultList;
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrTimeout(KeyT keyT, int expectNum, long timeout, TimeUnit unit) {
        RuleParameter ruleParameter = createSearchRule(keyT, timeout, unit, expectNum);

        final List<ResultT> resultList = new ArrayList<>();
        Future timingCancelFuture = startAddResultToListUntilEnough(resultList, ruleParameter);
        startTimingCancel(timingCancelFuture, ruleParameter);
        unifyResultCache(ruleParameter, resultList);
        return resultList;
    }

    @Override
    public List<ResultT> getResultsUntilEnough(KeyT keyT, int expectNum) {
        RuleParameter<ResultT> rule = createSearchRule(keyT, NOT_HAVE_TIMEOUT, TimeUnit.MILLISECONDS, expectNum);
        List<ResultT> list = startGetResultsUntilEnough(rule);
        unifyResultCache(rule, list);
        return list;
    }

    @Override
    public List<ResultT> getResultsUntilEnoughOrOneTimeout(KeyT keyT, int expectNum, long timeout, TimeUnit unit) {
        RuleParameter ruleParameter = createSearchRule(keyT, timeout, unit, expectNum);

        List list = startGetResultsUntilEnoughOrOneTimeout(ruleParameter);
        unifyResultCache(ruleParameter, list);
        return list;
    }

    @Override
    public ResultT getAResult(KeyT keySearch) {
        BlockingQueue<ResultT> resultTBlockingQueue = cacheResults.compute(keySearch).get();
        ResultT resultT = takeOfQueueWithTryCatch(resultTBlockingQueue);
        unifyResultCache(resultT, resultTBlockingQueue);
        return resultT;
    }

    @Override
    public ResultT getAResultUntilTimeout(KeyT keyT, long timeout, TimeUnit timeUnit) throws TimeoutException {
        RuleParameter<ResultT> ruleParameter = createSearchRule(keyT, timeout, timeUnit, NOT_LIMIT_EXPECT_NUM);

        ResultT resultT = startGetAResultUntilTimeout(ruleParameter);
        unifyResultCache(resultT, ruleParameter.resultTBlockingQueue);
        return resultT;
    }

    private RuleParameter createSearchRule(KeyT keyT, long timeout, TimeUnit unit, int expectNum) {
        BlockingQueue<ResultT> resultBlockingQueue = cacheResults.compute(keyT).get();
        long milliTimeout = ParameterUtil.preventTimeoutTooLong(timeout, unit);
        return new RuleParameter(resultBlockingQueue, milliTimeout, expectNum);
    }

    private List<ResultT> startGetResultsUntilOneTimeout(RuleParameter ruleParameter) {
        List<ResultT> list = new ArrayList<>();
        boolean notTimeout = true;
        while (notTimeout) {
            notTimeout = addToListUntilOneTimeout(list, ruleParameter);
        }
        return list;
    }

    private void unifyResultCache(RuleParameter ruleParameter, List list) {
        list.stream().forEach(result -> {
            ruleParameter.resultTBlockingQueue.offer(result);
        });
    }

    private boolean addToListUntilOneTimeout(List<ResultT> list, RuleParameter<ResultT> rule) {
        ResultT result;
        try {
            result = rule.resultTBlockingQueue.poll(rule.milliTimeout, rule.unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        if (isNotTimeout(result)) {
            list.add(result);
        } else {
            return false;
        }
        return true;
    }

    private boolean isNotTimeout(ResultT result) {
        return result != null;
    }

    private void startTimingCancel(Future timingCancelFuture, RuleParameter rule) {
        try {
            timingCancelFuture.get(rule.milliTimeout, rule.unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {

        } finally {
            timingCancelFuture.cancel(true);
        }
    }

    private Future startAddResultToListUntilEnough(List<ResultT> resultList, RuleParameter<ResultT> rule) {
        return gitService.submit(() -> {
            for (int i = 0; i < rule.expectNum; i++) {
                try {
                    ResultT resultT = rule.resultTBlockingQueue.take();
                    resultList.add(resultT);
                } catch (InterruptedException e) {

                }
            }
        });
    }

    private List startGetResultsUntilEnoughOrOneTimeout(RuleParameter ruleParameter) {
        List<ResultT> list = new ArrayList<>();
        boolean notTimeout = true;
        while (notTimeout) {
            notTimeout = addToListUntilEnoughOrOneTimeout(list, ruleParameter);
        }
        return list;
    }

    private boolean addToListUntilEnoughOrOneTimeout(List<ResultT> list, RuleParameter<ResultT> rule) {
        ResultT result;
        if (list.size() >= rule.expectNum) {
            return false;
        }
        try {
            result = rule.resultTBlockingQueue.poll(rule.milliTimeout, rule.unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        if (isNotTimeout(result)) {
            list.add(result);
        } else {
            return false;
        }
        return true;
    }

    private List<ResultT> startGetResultsUntilEnough(RuleParameter<ResultT> rule) {
        List<ResultT> list = new ArrayList<>();
        ResultT resultT;
        while (list.size() < rule.expectNum) {
            resultT = takeOfQueueWithTryCatch(rule.resultTBlockingQueue);
            list.add(resultT);
        }
        return list;
    }

    private ResultT takeOfQueueWithTryCatch(BlockingQueue<ResultT> resultTBlockingQueue) {
        try {
            return resultTBlockingQueue.take();
        } catch (InterruptedException e) {
            System.out.println("the action of take method is canceled");
        }
        return null;
    }

    private void unifyResultCache(ResultT resultT, BlockingQueue queue) {
        queue.offer(resultT);
    }

    private ResultT startGetAResultUntilTimeout(RuleParameter<ResultT> ruleParameter) {
        List<ResultT> saveResult = new ArrayList<>();
        Future future = gitService.submit(() -> {
            ResultT resultT = takeOfQueueWithTryCatch(ruleParameter.resultTBlockingQueue);
            saveResult.add(resultT);
        });
        startTimingCancel(future, ruleParameter);
        ResultT resultT = null;
        if (saveResult.size() != 0) {
            resultT = saveResult.get(0);
        }
        return resultT;
    }

    @Override
    public void clearCache() {
        cacheResults.clearCache();
    }

    public boolean isEmpty() {
        return cacheResults.isEmpty();
    }

    public void stopSearch() {
        searchMethod.stopSearch();
    }

    public void stopSearchNow() {
        searchMethod.stopSearchNow();
    }

}
