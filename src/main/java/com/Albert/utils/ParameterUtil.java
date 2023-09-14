package com.Albert.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author Albert
 */
public class ParameterUtil {
    private static final long MAX_WAIT_MILLISECOND = 1000 * 60 * 2;

    public static long preventTimeoutTooLong(long milliTimeout, TimeUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit parameter cannot be null");
        }
        long currentTimeout = unit.toMillis(milliTimeout);
        if (currentTimeout > MAX_WAIT_MILLISECOND) {
            currentTimeout = MAX_WAIT_MILLISECOND;
        }
        return currentTimeout;
    }
}
