/**
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.Albert.cache;

import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author Albert
 */
public class EfficientCacheCompute<KeyT, ResultT> implements Compute<KeyT, ResultT> {
    private final boolean IS_NOT_RETURN = true;
    private final ConcurrentHashMap<KeyT, Future<ResultT>> cacheResult;

    private final Function<KeyT, ResultT> computeMethod;

    private EfficientCacheCompute(Function<KeyT, ResultT> computeMethod) {
        this.computeMethod = computeMethod;
        this.cacheResult = new ConcurrentHashMap<>();
    }

    public static <KeyT, ResultT> EfficientCacheCompute createNeedComputeFunction(Function<KeyT, ResultT> computeMethod) {
        return new EfficientCacheCompute<>(computeMethod);
    }

    @Override
    public ResultT compute(final KeyT keyT) {
        while (IS_NOT_RETURN) {
            Future<ResultT> resultFuture = cacheResult.get(keyT);
            if (isNotExitResult(resultFuture)) {
                Callable<ResultT> computeMethodHavingPutKey = () -> computeMethod.apply(keyT);
                FutureTask<ResultT> runWhenResultFutureNull = new FutureTask<>(computeMethodHavingPutKey);
                resultFuture = cacheResult.putIfAbsent(keyT, runWhenResultFutureNull);
                if (isNotExitResult(resultFuture)) {
                    resultFuture = runWhenResultFutureNull;
                    runWhenResultFutureNull.run();
                }
            }
            return getResultWithTryCatch(resultFuture);
        }
    }

    private boolean isNotExitResult(Future<ResultT> resultFuture) {
        return resultFuture == null;
    }

    private ResultT getResultWithTryCatch(Future<ResultT> resultTFuture) {
        ResultT resultT = null;
        try {
            resultT = resultTFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return resultT;
    }

    @Override
    public ResultT getCacheIfExist(KeyT key) {
        Future<ResultT> resultTFuture = cacheResult.get(key);
        ResultT result = null;
        if (isExistResult(resultTFuture)) {
            result = getResultWithTryCatch(resultTFuture);
        }
        return result;
    }

    private boolean isExistResult(Future<ResultT> resultTFuture) {
        return resultTFuture != null;
    }

    @Override
    public ConcurrentHashMap.KeySetView<KeyT, Future<ResultT>> getKeySetFromCacheResult() {
        return cacheResult.keySet();
    }

    @Override
    public void clearCache() {
        cacheResult.clear();
    }

    @Override
    public boolean isEmpty() {
        return cacheResult.isEmpty();
    }
}
