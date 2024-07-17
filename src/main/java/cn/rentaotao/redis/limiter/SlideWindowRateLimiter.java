package cn.rentaotao.redis.limiter;

import lombok.Setter;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

import java.time.Duration;

public class SlideWindowRateLimiter implements Limiter{

    private final RedissonClient redissonClient;

    @Setter
    private int maxCount = 5;

    @Setter
    private int period = 60;

    public SlideWindowRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean allowed(String key, boolean add) {
        long now = System.currentTimeMillis();
        RScoredSortedSet<Object> scoredSortedSet = redissonClient.getScoredSortedSet(key);
        scoredSortedSet.add(now, "" + now);
        // 根据 score 即时间删除 60s 之前的数据，剩下的即周期之内的请求数量
        scoredSortedSet.removeRangeByScore(now - period * 1000L, false, now, true);
        // 统计请求数量
        int size = scoredSortedSet.size();
        // 设置过期时间，即用户超过周期没有请求，删除缓存
        scoredSortedSet.expire(Duration.ofSeconds(period + 1));
        return size < maxCount;
    }
}
