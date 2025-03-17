package cn.programcx.im.service;

import cn.programcx.im.dao.GroupMessageMapper;
import cn.programcx.im.pojo.GroupMessage;
import cn.programcx.im.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class GroupMessageStoreService {
    @Resource(name = "redisTemplate")
    private RedisTemplate  redisTemplate;

    @Autowired
    private GroupMessageMapper groupMessageMapper;
    /**
     * 缓存群组消息
     *
     * @param groupMessage 群组消息对象
     */
    public void cacheMessage(GroupMessage groupMessage) {
        if (groupMessage == null) {
            return;
        }
        final String key = "im:msg:group:" + groupMessage.getGroupId();
        redisTemplate.opsForZSet().add(key, groupMessage, DateTimeUtil.getCreatedAt().getTime());
    }

    /**
     * 获取群组消息
     * @param groupId 群组ID
     * @return 消息列表
     */
    public List<GroupMessage> getRedisGroupMessage(Long groupId) {
        final String key = "im:msg:group:" + groupId;
        Set set = redisTemplate.opsForZSet().range(key, 0, -1);
        List<GroupMessage> messages = new ArrayList<>();
        if (set != null) {
            for (Object o : set) {
                if (o instanceof GroupMessage) {
                   messages.add((GroupMessage)o);
                }
            }
        }
        return messages;
    }


    /**
     * 同步群组消息
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void syncGroupMessages() {
        // 同步群组消息
        try{
            final String key = "im:msg:group:*";
            Set keys = redisTemplate.keys(key);
            for(Object keyObj: keys){
                Set<GroupMessage> messages = redisTemplate.opsForZSet().range(keyObj.toString(), 0, -1);

                if(messages != null){
                    for(GroupMessage message: messages){
                        if(message.getMessageId() != null){
                            groupMessageMapper.insertGroupMessage(message);
                        }
                    }
                    redisTemplate.delete(keyObj);
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
