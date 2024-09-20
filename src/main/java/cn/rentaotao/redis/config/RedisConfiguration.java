package cn.rentaotao.redis.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

/**
 * @author rtt
 * @date 2024/7/15 09:50
 */
@Configuration
public class RedisConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedissonClient redissonClient() {

        Config config = new Config();
        config.setCodec(new StringCodec());
        SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
        RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
        List<String> nodes = sentinel.getNodes();
        nodes.forEach(node -> sentinelServersConfig.addSentinelAddress("redis://" + node));
        sentinelServersConfig.setMasterName(sentinel.getMaster());
        return Redisson.create(config);
    }

    @Bean
    public RedisClient redisClient() {
        RedisURI redisURI = new RedisURI(redisProperties.getHost(), redisProperties.getPort(), redisProperties.getTimeout());
        return RedisClient.create(redisURI);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        if (redisProperties.getCluster() != null) {

            RedisProperties.Cluster cluster = redisProperties.getCluster();
            RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(cluster.getNodes());
            redisClusterConfiguration.setMaxRedirects(cluster.getMaxRedirects());

            if (RedisProperties.ClientType.JEDIS.equals(redisProperties.getClientType())) {
                return new JedisConnectionFactory(redisClusterConfiguration, jedisClientConfiguration());
            } else {
                return new LettuceConnectionFactory(redisClusterConfiguration, lettuceClientConfiguration());
            }
        }

        if (redisProperties.getSentinel() != null) {

            RedisProperties.Sentinel sentinel = redisProperties.getSentinel();
            List<String> nodes = sentinel.getNodes();
            if (nodes.isEmpty()) {
                throw new BeanCreationException("RedisConnectionFactory create error: sentinel nodes is empty");
            }
            RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
            redisSentinelConfiguration.master(sentinel.getMaster());
            nodes.forEach(node -> redisSentinelConfiguration.sentinel(node.split(":")[0], Integer.valueOf(node.split(":")[1])));

            if (RedisProperties.ClientType.JEDIS.equals(redisProperties.getClientType())) {
                return new JedisConnectionFactory(redisSentinelConfiguration, jedisClientConfiguration());
            } else {
                return new LettuceConnectionFactory(redisSentinelConfiguration, lettuceClientConfiguration());
            }
        }

        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());

        if (RedisProperties.ClientType.JEDIS.equals(redisProperties.getClientType())) {
            return new JedisConnectionFactory(configuration, jedisClientConfiguration());
        } else {
            return new LettuceConnectionFactory(configuration, lettuceClientConfiguration());
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Bean
    @ConditionalOnProperty(value = "spring.redis.client-type", havingValue = "lettuce")
    public LettuceClientConfiguration lettuceClientConfiguration() {
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
        if (pool != null && pool.getEnabled()) {
            LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder()
                    .poolConfig(genericObjectPoolConfig())
                    .readFrom(ReadFrom.REPLICA_PREFERRED);
            if (redisProperties.getCluster() != null || redisProperties.getSentinel() != null) {
                builder.clientOptions(clusterClientOptions());

            } else {
                builder.clientOptions(clientOptions());
            }

            return builder.build();

        }
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED);
        if (redisProperties.getCluster() != null || redisProperties.getSentinel() != null) {
            builder.clientOptions(clusterClientOptions());

        } else {
            builder.clientOptions(clientOptions());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(value = "spring.redis.client-type", havingValue = "jedis")
    public JedisClientConfiguration jedisClientConfiguration() {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        RedisProperties.Pool pool = redisProperties.getJedis().getPool();
        if (pool != null && pool.getEnabled()) {
            builder.usePooling();
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(value = "spring.redis.client-type", havingValue = "lettuce")
    public ClusterClientOptions clusterClientOptions() {
        // 自适应集群拓扑刷新
        // https://github.com/lettuce-io/lettuce-core/wiki/Redis-Cluster#user-content-refreshing-the-cluster-topology-view
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                /*
                    开启全部自适应刷新触发器
                    MOVED_REDIRECT          重定向
                    ASK_REDIRECT
                    PERSISTENT_RECONNECTS
                    UNCOVERED_SLOT          没有覆盖的槽位
                    UNKNOWN_NODE            位置节点
                 */
                .enableAllAdaptiveRefreshTriggers()
                // 超时事件
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(30L))
                // 开启周期性自适应刷新
                .enablePeriodicRefresh(Duration.ofSeconds(30L))
                .build();

        // 客户端选项

        return ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "spring.redis.client-type", havingValue = "lettuce")
    public ClientOptions clientOptions() {
        return ClientOptions.builder()
                .build();
    }

    @Bean
    public GenericObjectPoolConfig<?> genericObjectPoolConfig() {
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        if (pool.getTimeBetweenEvictionRuns() != null) {
            poolConfig.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
        }
        if (pool.getMaxWait() != null) {
            poolConfig.setMaxWait(pool.getMaxWait());
        }
        return poolConfig;

    }
}
