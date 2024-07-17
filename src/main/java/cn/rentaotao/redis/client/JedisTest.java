package cn.rentaotao.redis.client;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author rtt
 * @date 2024/7/15 10:59
 */
public class JedisTest {

    public static void main(String[] args) {
        try (JedisPool jedisPool = new JedisPool(JedisExample.poolConfig, "127.0.0.1", 6379, 3000)) {
            Jedis jedis = jedisPool.getResource();
            // String-动态字符串
            
            // hash-类似与java中的hashMap实现

            // list-双向链表

            // set-类似与hashSet

            // sorted set 有序集合

            // ----------------------- 地理位置 内置使用 sorted set，地理位置为 score -----------------------
            // 添加地理位置
//            jedis.geoadd("shops", 120.212000, 30.208301, "杭州市滨江区龙湖滨江天街");
//            jedis.geoadd("shops", 120.066972, 30.292537, "杭州市西湖区龙湖西溪天街");
//            jedis.geoadd("shops", 120.327490, 30.310104, "杭州市钱塘区龙湖杭州金沙天街");
//            jedis.geoadd("shops", 120.215670, 30.350765, "杭州市上城区龙湖杭州丁桥天街");

            // 获取位置
//            List<GeoCoordinate> geoPosList = jedis.geopos("shops", "杭州市滨江区龙湖滨江天街");
//            geoPosList.forEach(pos -> System.out.println(pos.toString()));

            // 计算两个位置的距离，不是很准确，而且是直线距离
//            Double geoDist = jedis.geodist("shops", "杭州市滨江区龙湖滨江天街", "杭州市上城区龙湖杭州丁桥天街", GeoUnit.M);
//            System.out.println(geoDist);

            // 获取指定位置附近的地理位置
//            List<GeoRadiusResponse> shops = jedis.georadius("shops", 120.196822, 30.323427, 10, GeoUnit.KM);
//            shops.forEach(shop -> System.out.println(shop.getMemberByString()));
            // ----------------------- JSON -----------------------
        }
    }
}
