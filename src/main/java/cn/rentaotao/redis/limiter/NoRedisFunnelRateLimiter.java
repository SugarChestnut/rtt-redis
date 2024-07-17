package cn.rentaotao.redis.limiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 漏桶算法
 * 原理：请求先进入到桶中，按固定速率出去，当桶满了之后，拒绝请求。
 * 用于保护系统
 * 如果将 funnel 的数据存储到 redis，无法做到数据更新的原子化
 */
public class NoRedisFunnelRateLimiter {

    private final Map<String, Funnel> funnels = new ConcurrentHashMap<>();

    public boolean isActionAllowed(String key, int capacity, int leakingRate) {
        Funnel funnel = funnels.get(key);
        if (funnel == null) {
            synchronized (funnels) {
                funnel = funnels.get(key);
                if (funnel == null) {
                    funnel = new Funnel(capacity, leakingRate);
                    funnels.put(key, funnel);

                }
            }
        }
        return funnel.watering(1);
    }

    static class Funnel {

        final ReentrantLock lock = new ReentrantLock(true);
        // 容量
        final int capacity;
        // 泄漏的速率
        final int leakingRate;
        // 剩余配额
        volatile AtomicInteger leftQuota;
        // 上次请求进来的时间戳
        volatile long leakingTs;

        public Funnel(int capacity, int leakingRate) {
            this.capacity = capacity;
            this.leakingRate = leakingRate;
            this.leftQuota = new AtomicInteger(capacity);
            this.leakingTs = System.currentTimeMillis() / 1000;
        }

        /**
         * @param quota 配额
         * @return 是否获取成功
         */
        boolean watering(int quota) {
            for (; ; ) {
                makeSpace();
                int q = this.leftQuota.get();
                if (q < quota) {
                    return false;
                }
                if (this.leftQuota.compareAndSet(q, q - quota)) {
                    return true;
                }
            }
        }

        /**
         * 每
         */
        void makeSpace() {
            int oldQuota = this.leftQuota.get();
            long nowTs = System.currentTimeMillis() / 1000;
            long deltaTs = nowTs - leakingTs;
            long deltaQuota = deltaTs * leakingRate;
            if (deltaQuota < 1) {
                return;
            }
            long nQuota = deltaQuota + oldQuota;
            if (this.leftQuota.compareAndSet(oldQuota, nQuota > capacity ? capacity : (int) nQuota)) {
                this.leakingTs = nowTs;
            }
        }
    }
}
