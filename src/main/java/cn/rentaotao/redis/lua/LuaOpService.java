package cn.rentaotao.redis.lua;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rtt
 * @date 2024/9/5 15:30
 */
@Component
public class LuaOpService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisConnectionFactory connectionFactory;

    @Autowired
    public LuaOpService(RedisTemplate<String, Object> redisTemplate, RedisConnectionFactory connectionFactory) {
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
    }

    private final String GET_SCRIPT = "return redis.call('Get', KEY[1])";

    public void exec() {

        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>(GET_SCRIPT, String.class);

        List<String> keys = new ArrayList<>();
        keys.add("rtt");

        String result = redisTemplate.execute(redisScript, keys);

        System.out.println(result);

    }

    public void execWithCallback() {
        String result = redisTemplate.execute((RedisCallback<String>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            if (nativeConnection instanceof RedisCommands) {
                @SuppressWarnings("unchecked")
                RedisCommands<String, String> commands = (RedisCommands<String, String>) nativeConnection;
                Object result1 = commands.eval(GET_SCRIPT, ScriptOutputType.VALUE, "rtt");
                System.out.println(result1);
            }
            return null;
        });
        System.out.println(result);
    }

    public void execWithFactory() {
        RedisConnection connection = connectionFactory.getConnection();
        Object result = connection.eval(
                GET_SCRIPT.getBytes(StandardCharsets.UTF_8),
                ReturnType.VALUE,
                1,
                "rtt".getBytes(StandardCharsets.UTF_8));

        System.out.println(result);
    }
}
