package cn.programcx.im.service;

import cn.programcx.im.dao.MessageMapper;
import cn.programcx.im.pojo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserMessageStoreService {

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 缓存消息
     *
     * @param message 消息对象
     */
    public void cacheMessage(Message message) {
        if (message == null) {
            return;
        }
        final String key = "im:msg:single:" + message.getReceiverUserId() + ":" + message.getSenderUserId();
        redisTemplate.opsForZSet().add(key, message, message.getCreatedAt().getTime());
    }

    /**
     * 获取用户消息
     * @param receiverUserId 接收者用户ID
     * @param senderUserId   发送者用户ID
     * @param lastMessageId  最后一条读取消息ID(如果没有则传0)
     * @return 消息列表
     */
    public List<Message> getRedisUserMessages(String receiverUserId, String senderUserId, Long lastMessageId) {
        final String key = "im:msg:single:" + receiverUserId + ":" + senderUserId;
        Set range = redisTemplate.opsForZSet().range(key, 0, -1);
        List<Message> messages = new ArrayList<>();
        for (Object o : range) {
            if(o instanceof Message){
                Message message = (Message) o;
                if (message.getMessageId() > lastMessageId) {
                    messages.add(message);
                }
            }
        }
        return messages;
    }

    /**
     * 删除用户消息
     * @param receiverUserId 接收者用户ID
     * @param senderUserId   发送者用户ID
     */
    public void deleteUserMessages(String receiverUserId, String senderUserId) {
        final String key = "im:msg:single:" + receiverUserId + ":" + senderUserId;
        redisTemplate.delete(key);

    }


    /**
     * 同步消息到数据库
     * @Scheduled(fixedRate = 5 * 60 * 1000) 5分钟同步一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void syncToDatabase() {
        try{
            final String key = "im:msg:single:*";
            Set keys = redisTemplate.keys(key); // 获取所有的key
            for (Object keyObj : keys) {
//               String[] spitedKeys = keyObj.toString().split(":");
//               Long receiverUserId = Long.parseLong(spitedKeys[3]);
//               Long senderUserId = Long.parseLong(spitedKeys[4]);
               Set<Message> messages = redisTemplate.opsForZSet().range(keyObj.toString(), 0, -1);
               //同步到数据库
               if(messages != null && !messages.isEmpty()){
                   for (Message message : messages) {
                      if (message.getMessageId() != null) {
                          messageMapper.insertMessage(message);
                            redisTemplate.opsForZSet().remove(keyObj.toString(), message);
                      }
                   }
               }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
