package cn.rentaotao.redis.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

/**
 * @author rtt
 * @date 2024/7/15 09:48
 */
@Service
public class ProductServiceImpl implements ProductService{

    public Type type = getClass();
}
