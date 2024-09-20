package cn.rentaotao.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Type;

/**
 * @author rtt
 * @date 2024/7/15 09:48
 */
@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private RedisOperations<String, String> operations;

    @Resource(name = "redisTemplate")
    private ListOperations<String, String> listOps;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Type type = getClass();

    @Override
    public String searchManufacturer(String name) {
//        for(;;) {
//            redisTemplate.watch(name);
//            redisTemplate.multi();
//        }

        return null;
    }
}
