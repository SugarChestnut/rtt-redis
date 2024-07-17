package cn.rentaotao.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * @author rtt
 * @date 2024/7/15 09:50
 */
@Configuration
public class RedisConfiguration {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        config.setCodec(new StringCodec());
        return Redisson.create(config);
    }
}
