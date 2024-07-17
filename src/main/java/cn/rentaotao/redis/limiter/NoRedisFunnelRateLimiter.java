package cn.rentaotao.redis.limiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 令牌桶算法
 * 以固定速率向桶中放入令牌，请求过来的时候，请求一个令牌
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
        return funnel.watering();
    }

    static class Funnel {
        // 容量
        final int capacity;
        // 泄漏的速率
        final int leakingRate;
        // 水位
        volatile AtomicInteger water;
        // 上次请求进来的时间戳
        volatile long leakingTs;

        public Funnel(int capacity, int leakingRate) {
            this.capacity = capacity;
            this.leakingRate = leakingRate;
            this.water = new AtomicInteger(0);
            this.leakingTs = System.currentTimeMillis();
        }

        boolean watering() {
            leaking();
            for (; ; ) {
                int w = this.water.get();
                // 超过漏斗容量，拒绝请求
                if (w >= capacity) {
                    return false;
                }
                // 水位为空
                if (w == 0 && this.water.compareAndSet(w, w + 1)) {
                    // 执行一个请求，水位加1
                    return true;
                }
                // 根据水位和泄漏速率，决定休眠时间
                int nanos = w * 1000 * 1000 / leakingRate;
                if (this.water.compareAndSet(w, w + 1)) {
                    LockSupport.parkNanos(nanos);
                    return true;
                }
            }
        }

        /**
         *
         */
        void leaking() {
            int w1 = this.water.get();
            long nowTs = System.currentTimeMillis() / 1000;
            // 间隔时间
            long deltaTs = nowTs - leakingTs;
            // 漏出的水量
            long delta = deltaTs / 1000 * leakingRate;
            if (delta < 1) {
                return;
            }
            long w2 = w1 - delta;
            // 有竞争，说明其他线程已经更新完成水位了
            if (this.water.compareAndSet(w1, w2 < 0 ? 0 : (int) w2)) {
                this.leakingTs = nowTs;
            }
        }
    }
}
