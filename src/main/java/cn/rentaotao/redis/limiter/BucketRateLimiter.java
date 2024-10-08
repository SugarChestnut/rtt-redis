package cn.rentaotao.redis.limiter;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

/**
 * 令牌桶算法
 * 指定一个固定大小的桶，按固定速率向桶中添加令牌，满了则丢弃。
 * 请求从桶中获取令牌，
 * 该算法可以在短时间内请求拿到大量令牌，‌从而处理瞬时流量。
 * <p>
 * 漏桶算法适合于需要保证系统不被打垮的场景，‌即防止外部请求过多导致自身系统崩溃。
 * ‌而令牌桶算法则适合于具有突发特性的流量，‌允许在一段时间内突发传输数据直到达到用户配置的门限。
 */
public class BucketRateLimiter{

    private final RedissonClient redissonClient;

    public BucketRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean allowed(String key, boolean add) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return false;
    }
}
