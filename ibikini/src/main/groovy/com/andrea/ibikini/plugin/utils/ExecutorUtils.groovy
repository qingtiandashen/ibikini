package com.andrea.ibikini.plugin.utils

import java.util.concurrent.*

class ExecutorUtils {
    private static final int CORE_POOL_SIZE = 5
    private static final int MAX_POOL_SIZE = Integer.MAX_VALUE
    private static final long KEEP_ALIVE_TIME = 60L
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS

    /**
     * 异步
     */
    private static class ExecutorServiceAsynHolder {
        private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy())
    }

    /**
     * 同步
     */
    private static class ExecutorServiceSyncHolder {
        private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor()
    }

    private ExecutorUtils() {
    }

    public static void execute(Runnable runnable) {
        ExecutorServiceAsynHolder.THREAD_POOL.execute(runnable)
    }

    public static Future<?> submit(Runnable runnable) {
        return ExecutorServiceAsynHolder.THREAD_POOL.submit(runnable)
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return ExecutorServiceAsynHolder.THREAD_POOL.submit(task)
    }

    public static void executeSync(Runnable runnable) {
        ExecutorServiceSyncHolder.THREAD_POOL.execute(runnable)
    }

    public static Future<?> submitSync(Runnable runnable) {
        return ExecutorServiceSyncHolder.THREAD_POOL.submit(runnable)
    }

    public static <T> Future<T> submitSync(Callable<T> task) {
        return ExecutorServiceSyncHolder.THREAD_POOL.submit(task)
    }
}