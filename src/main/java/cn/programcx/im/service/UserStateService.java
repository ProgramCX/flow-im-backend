package cn.programcx.im.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserStateService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    public void setUserOnline(Long userId) {
        redisTemplate.opsForHash().put("im:online", Long.toString(userId), 1);
    }

    public void setUserOffline(Long userId) {
        redisTemplate.opsForHash().put("im:offline", Long.toString(userId), 1);
    }
}
