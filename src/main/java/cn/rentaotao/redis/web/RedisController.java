package cn.rentaotao.redis.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author rtt
 * @date 2024/7/15 09:41
 */
@RestController
@RequestMapping("/redis")
public class RedisController {

    @RequestMapping
    public String index() {
        return "ok";
    }
}
