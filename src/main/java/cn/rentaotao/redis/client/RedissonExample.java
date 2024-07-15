package cn.rentaotao.redis.client;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.io.IOException;
import java.net.URL;

/**
 * redis 官方推荐
 *
 * @author rtt
 * @date 2024/7/15 09:51
 */
public class RedissonExample {

    public static Config config;

    public static RedissonClient redissonClient;

    static {
        URL url = RedissonExample.class.getClassLoader().getResource("redis.yml");
        try {
//            Config config1 = new Config();
//            config1.useSingleServer().setAddress("127.0.0.1");
            config = Config.fromYAML(url);
            // 必须设置 codec，不然会解码失败
            config.setCodec(new StringCodec());
            redissonClient = Redisson.create(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        sample();
    }

    public static void sample() {
        try {
            RBucket<Object> bucket = redissonClient.getBucket("user");
            System.out.println(bucket.get().toString());
        } finally {
            redissonClient.shutdown();
        }
    }
}
