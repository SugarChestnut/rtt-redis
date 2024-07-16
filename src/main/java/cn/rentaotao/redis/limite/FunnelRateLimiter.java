package cn.rentaotao.redis.limite;

/**
 * 漏桶算法
 * 原理：请求先进入到桶中，按固定速率出去，当桶满了之后，拒绝请求。
 * 用于保护系统
 */
public class FunnelRateLimiter implements Limiter{
    @Override
    public boolean allowed(String key, boolean add) {
        return false;
    }
}
