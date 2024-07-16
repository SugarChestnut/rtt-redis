package cn.rentaotao.redis.limite;

import lombok.Setter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

/**
 * 使用 bitmap 存储布隆顾虑器
 */
public class BloomFilterLimiter implements Limiter{

    private final RedissonClient redissonClient;

    @Setter
    private long expectedInsertions = 1000;

    @Setter
    private double falseProbability = 0.01;

    public BloomFilterLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean allowed(String key, boolean add) {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(key);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        boolean contains = bloomFilter.contains(key);
        if (add) {
            bloomFilter.add(key);
        }
        return !contains;
    }
}
