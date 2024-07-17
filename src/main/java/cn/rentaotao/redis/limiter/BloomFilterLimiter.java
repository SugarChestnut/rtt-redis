package cn.rentaotao.redis.limiter;

import lombok.Setter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.SmartLifecycle;

/**
 * 使用 bitmap 存储布隆顾虑器
 */
@ConfigurationProperties(BloomFilterLimiter.PREFIX)
public class BloomFilterLimiter implements Limiter, SmartLifecycle {

    public static final String PREFIX = "limiter.bloom";

    private final RedissonClient redissonClient;

    @Setter
    private String name = "default-bloom-filter";

    @Setter
    private long expectedInsertions = 1000;

    @Setter
    private double falseProbability = 0.01;

    private RBloomFilter<String> bloomFilter;

    public BloomFilterLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean allowed(String element, boolean add) {
        boolean contains = bloomFilter.contains(element);
        if (add) {
            bloomFilter.add(element);
        }
        return !contains;
    }

    @Override
    public void start() {
        // 初始化
        bloomFilter = redissonClient.getBloomFilter(name);
        bloomFilter.tryInit(expectedInsertions, falseProbability);
    }

    @Override
    public void stop() {
        bloomFilter.delete();
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}
