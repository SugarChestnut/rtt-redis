package cn.rentaotao.redis.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author rtt
 * @date 2024/7/15 09:41
 */
@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @RequestMapping
    public String index() {
        return "ok";
    }

    @RequestMapping("/get/{key}")
    public String name(@PathVariable("key") String key) {
        if (key != null) {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                return "value is not found";
            }
            return value;
        }

        return "key is empty";
    }
}
