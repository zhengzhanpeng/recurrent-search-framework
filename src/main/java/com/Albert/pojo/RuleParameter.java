package com.Albert.pojo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 */
public class RuleParameter<ResultT> {
    public final BlockingQueue<ResultT> resultTBlockingQueue;
    public final long milliTimeout;
    public final TimeUnit unit = TimeUnit.MILLISECONDS;
    public final int expectNum;

    public RuleParameter(BlockingQueue<ResultT> resultTBlockingQueue, long milliTimeout, int expectNum) {
        this.resultTBlockingQueue = resultTBlockingQueue;
        this.milliTimeout = milliTimeout;
        this.expectNum = expectNum;
    }
}
