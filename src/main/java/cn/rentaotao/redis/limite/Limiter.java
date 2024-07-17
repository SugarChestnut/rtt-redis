package cn.rentaotao.redis.limite;

public interface Limiter {

    boolean allowed(String key, boolean add);
}


