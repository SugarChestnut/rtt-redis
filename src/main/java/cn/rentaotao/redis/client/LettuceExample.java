package cn.rentaotao.redis.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.time.Duration;

/**
 * springboot2.0 默认使用的客户端
 *
 * @author rtt
 * @date 2024/7/15 09:52
 */
public class LettuceExample {

    public static RedisClient redisClient;

    static {
        RedisURI redisURI = RedisURI.builder()
                .withHost("127.0.0.1")
                .withPort(6379)
                .withTimeout(Duration.ofMillis(3_000))
                .build();
        redisClient = RedisClient.create(redisURI);
    }

    public static void main(String[] args) {
        simple();
    }

    public static void simple() {
        try (StatefulRedisConnection<String, String> connect = redisClient.connect()) {
            // 异步
            RedisAsyncCommands<String, String> async = connect.async();
            System.out.println(async.ping());
        }
    }
}
