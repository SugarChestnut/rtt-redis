package cn.rentaotao.redis.limiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 令牌桶算法
 * 以固定速率向桶中放入令牌，请求过来的时候，请求一个令牌
 */
public class NoRedisBucketRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean isActionAllowed(String key, int capacity, int leakingRate) {
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            synchronized (buckets) {
                bucket = buckets.get(key);
                if (bucket == null) {
                    bucket = new Bucket(capacity, leakingRate);
                    buckets.put(key, bucket);
                }
            }
        }
        return bucket.getToken();
    }

    static class Bucket {
        // 容量
        final int capacity;
        // 令牌放入的速率
        final int putRate;
        // 剩余令牌数
        volatile AtomicInteger leftTokens;
        // 上次请求进来的时间戳
        volatile long touchTs;

        public Bucket(int capacity, int putRate) {
            this.capacity = capacity;
            this.putRate = putRate;
            this.leftTokens = new AtomicInteger(capacity);
            this.touchTs = System.currentTimeMillis();
        }

        /**
         * 获取令牌
         */
        boolean getToken() {
            makeSpace();
            for (; ; ) {
                int num = this.leftTokens.get();
                // 没有令牌
                if (num == 0) {
                    return false;
                }
                // 原子更新令牌数量
                if (this.leftTokens.compareAndSet(num, num - 1)) {
                    return true;
                }
            }
        }

        /**
         * 计算令牌数量
         */
        void makeSpace() {
            int oldNum = this.leftTokens.get();
            long nowTs = System.currentTimeMillis();
            // 间隔时间
            long space = nowTs - touchTs;
            // 间隔时间内需要放入的令牌数
            long tokens = space / 1000 * putRate;
            if (tokens < 1) {
                return;
            }
            long num = oldNum + tokens;
            // 更新令牌数量
            if (this.leftTokens.compareAndSet(oldNum, num > capacity ? capacity : (int) num)) {
                this.touchTs = nowTs;
            }
        }
    }
}
