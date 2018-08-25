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
        List list = getResultsUntilOneTimeout(ruleParameter);
        unifyResultCache(ruleParameter, list);
        return list;
    }

    @Override
    public List<ResultT> getResultsUntilTimeout(KeyT keyT, long timeout, TimeUnit unit) {
        RuleParameter<ResultT> ruleParameter = createSearchRule(keyT, timeout, unit, NOT_LIMIT_EXPECT_NUM);
        List<ResultT> resultList = new ArrayList<>();
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
        RuleParameter<ResultT> ruleParameter = createSearchRule(keyT, timeout, unit, expectNum);
        List<ResultT> resultList = new ArrayList<>();
        Future timingCancelFuture = gitService.submit(() -> {
            for (int i = 0; i < ruleParameter.expectNum; i++) {
                try {
                    resultList.add(ruleParameter.resultTBlockingQueue.take());
                } catch (InterruptedException e) {
                }
            }
        });
        startTimingCancel(timingCancelFuture, ruleParameter);
        unifyResultCache(ruleParameter, resultList);
        return resultList;
    }

    @Override
    public List<ResultT> getResultsUntilEnough(KeyT keyT, int expectNum) {
        RuleParameter<ResultT> rule = createSearchRule(keyT, NOT_HAVE_TIMEOUT, TimeUnit.MILLISECONDS, expectNum);
        List<ResultT> resultList = new ArrayList<>();
        while (resultList.size() < rule.expectNum) {
            resultList.add(takeOfQueueWithTryCatch(rule.resultTBlockingQueue));
        }
        unifyResultCache(rule, resultList);
        return resultList;
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

    private List<ResultT> getResultsUntilOneTimeout(RuleParameter ruleParameter) {
        List<ResultT> list = new ArrayList<>();
        while (true) {
            ResultT result = (ResultT) getResult(ruleParameter);
            if (result == null) {
                break;
            }
            list.add(result);
        }
        return list;
    }

    private ResultT getResult(RuleParameter<ResultT> rule) {
        ResultT result = null;
        try {
            result = rule.resultTBlockingQueue.poll(rule.milliTimeout, rule.unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void unifyResultCache(RuleParameter ruleParameter, List list) {
        list.forEach(result -> ruleParameter.resultTBlockingQueue.offer(result));
    }

    private void startTimingCancel(Future timingCancelFuture, RuleParameter rule) {
        try {
            timingCancelFuture.get(rule.milliTimeout, rule.unit);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            timingCancelFuture.cancel(true);
        }
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
        if (result != null) {
            list.add(result);
        } else {
            return false;
        }
        return true;
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
