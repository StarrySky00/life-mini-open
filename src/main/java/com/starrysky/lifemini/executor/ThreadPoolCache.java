package com.starrysky.lifemini.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolCache {
    private ThreadPoolCache(){}
    public  static class ThreadPoolHolder{
        //使用原子计数器作为线程名标识
        private static final AtomicInteger counter=new AtomicInteger(0);
        private static final ExecutorService cachePool=new ThreadPoolExecutor(
                8,
                16,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                r->new Thread(r,"cache-pool-"+counter.getAndIncrement()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    public static ExecutorService getInstance(){
        return ThreadPoolHolder.cachePool;
    }
}
