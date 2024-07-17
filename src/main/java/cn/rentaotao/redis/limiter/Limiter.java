package cn.rentaotao.redis.limiter;

public interface Limiter {

    boolean allowed(String key, boolean add);
}


