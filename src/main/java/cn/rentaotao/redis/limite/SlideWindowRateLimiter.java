package cn.rentaotao.redis.limite;

import lombok.Setter;
import org.redisson.api.RedissonClient;

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

        return true;
    }
}
