package cn.rentaotao.redis.client;

import redis.clients.jedis.*;

import java.time.Duration;
import java.util.HashSet;

/**
 * 封装了常用的命令，但是缺少一些特殊命令，比如分布式锁
 * 多个线程共享一个 jedis 实例是不安全的，因为会共享缓冲区，因此使用 jedisPool 每个线程使用单独的实例
 *
 * @author rtt
 * @date 2024/7/15 09:51
 */
public class JedisExample {

    public static JedisPoolConfig poolConfig;

    public static void main(String[] args) {
        simple();
//        jedisPool();
//        sentinelPool();
//        cluster();
    }

    static {
        poolConfig = new JedisPoolConfig();
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        poolConfig.setBlockWhenExhausted(true);
        //设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        poolConfig.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        //是否启用pool的jmx管理功能, 默认true
        poolConfig.setJmxEnabled(true);
        //MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + "pool" + i); 默认为"pool", JMX不熟,具体不知道是干啥的...默认就好.
        poolConfig.setJmxNamePrefix("pool");
        //是否启用后进先出, 默认true
        poolConfig.setLifo(true);
        //最大空闲连接数, 默认8个
        poolConfig.setMaxIdle(8);
        //最大连接数, 默认8个
        poolConfig.setMaxTotal(8);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        poolConfig.setMaxWait(Duration.ofMillis(-1));
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(1_800_000));
        //最小空闲连接数, 默认0
        poolConfig.setMinIdle(0);
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        poolConfig.setNumTestsPerEvictionRun(3);
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        poolConfig.setSoftMinEvictableIdleDuration(Duration.ofMillis(1_800_000));
        //在获取连接的时候检查有效性, 默认false
        poolConfig.setTestOnBorrow(false);
        //在空闲时检查有效性, 默认false
        poolConfig.setTestWhileIdle(false);
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(-1));
    }

    /**
     * 简单使用
     */
    public static void simple() {
        try (Jedis jedis = new Jedis("127.0.0.1", 6379)) {
            String ping = jedis.ping();
            // PONG
            System.out.println(ping);
            jedis.set("user", "rtt");
        }
    }

    /**
     * 连接池
     */
    public static void jedisPool() {
        try (JedisPool jedisPool = new JedisPool(poolConfig, "127.0.0.1", 6379, 3000)) {
            Jedis jedis = jedisPool.getResource();
            String ping = jedis.ping();
            System.out.println(ping);
        }
    }

    /**
     * 哨兵模式
     */
    public static void sentinelPool() {
        // 哨兵集合
        HashSet<String> sentinels = new HashSet<>();
        sentinels.add("127.0.0.1:6379");
        try (JedisSentinelPool sentinelPool = new JedisSentinelPool("master", sentinels, poolConfig, 3000);) {
            Jedis jedis = sentinelPool.getResource();
            System.out.println(jedis.ping());
        }
    }

    /**
     * 集群模式
     */
    public static void cluster() {
        HashSet<HostAndPort> set = new HashSet<>();
        // redis 默认没有开启集群模式
        set.add(new HostAndPort("127.0.0.1", 6379));
        try (JedisCluster jedisCluster = new JedisCluster(set)) {
            System.out.println(jedisCluster.ping());
        }
    }
}
