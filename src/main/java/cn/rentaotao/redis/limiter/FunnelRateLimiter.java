package cn.rentaotao.redis.limiter;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 漏桶算法
 * 原理：请求先进入到桶中，按固定速率出去，当桶满了之后，拒绝请求。
 * 用于保护系统
 * 使用 Redis-Cell 提供的命令，能够做到原子化命令
 */
public class FunnelRateLimiter {

    private final RedissonClient redissonClient;

    public FunnelRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean allowed(String key, boolean add) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 60s 内 30次
        rateLimiter.setRate(RateType.PER_CLIENT, 30L, 60L, RateIntervalUnit.SECONDS);
        return false;
    }
}
