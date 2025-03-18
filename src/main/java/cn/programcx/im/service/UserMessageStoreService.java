package cn.programcx.im.service;

import cn.programcx.im.dao.DeviceReadOffsetMapper;
import cn.programcx.im.dao.MessageMapper;
import cn.programcx.im.pojo.DeviceReadOffset;
import cn.programcx.im.pojo.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserMessageStoreService {

    private final String REDIS_USER_MESSAGE_KEY = "msg-user-id";

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private DeviceReadOffsetMapper deviceReadOffsetMapper;
    /**
     * 缓存消息
     *
     * @param message 消息对象
     */
    public void cacheMessage(Message message) {
        if (message == null) {
            return;
        }
        Long msgId = redisTemplate.opsForValue().increment(REDIS_USER_MESSAGE_KEY);
        message.setMessageId(msgId);
        final String key = "im:msg:single:" + message.getReceiverUserId() + ":" + message.getSenderUserId();
        redisTemplate.opsForZSet().add(key, message, msgId);
    }

    /**
     * 获取用户消息
     * @param receiverUserId 接收者用户ID
     * @param senderUserId   发送者用户ID
     * @param deviceId     设备ID
     * @return 消息列表
     */
    public List<Message> getRedisUserMessages(Long receiverUserId, Long senderUserId, String deviceId) {
        // 获取用户在群组中的读取的最后一条消息ID
        Long lastReadMessageId = getLastReadMessageId(senderUserId,receiverUserId,deviceId);
        Long lastMessageId = getLastMessageId(senderUserId,receiverUserId,deviceId);

        Object value = redisTemplate.opsForValue().get(REDIS_USER_MESSAGE_KEY);
        final String key = "im:msg:single:" + receiverUserId + ":" + senderUserId;
        Set range = redisTemplate.opsForZSet().reverseRangeByScore(key, lastReadMessageId +1, Long.MAX_VALUE,0,10) ;// 获取最后10条消息
        List<Message> messages = new ArrayList<>();
        for (Object o : range) {
            if(o instanceof Message){
                Message message = (Message) o;
                if (message.getMessageId() > lastReadMessageId) {
                    messages.add(message);
                }
            }
        }

        if (messages.size() <10) {
            List<Message> dbMessages = messageMapper.getMessageBySenderAndReceiverId(senderUserId, receiverUserId, lastReadMessageId,10 - messages.size());
            Set<Message> messageSet = new HashSet<>(messages);
            for (Message message : dbMessages) {
                if (messageSet.add(message)) {
                    messages.add(message);
                }
            }
        }

        //判断是否已经读取完消息
        if(messages.size() == 10){    //可能未读取完
            //获取最后加载的消息ID
            Long lastLoadedMessageId = messages.get(messages.size()-1).getMessageId();
            //如果最后加载的消息ID大于最后一条消息ID，则说明未读取完
            if(lastLoadedMessageId > lastReadMessageId){
                //更新用户在群组中的读取的最后一条消息ID为最后读取消息的ID
                setLastReadMessageId(senderUserId,receiverUserId,deviceId,lastLoadedMessageId);
            }else{
               //如果读取完了，就设置为最新的消息ID
                setLastReadMessageId(senderUserId,receiverUserId,deviceId,lastMessageId);
            }

        }else{
            setLastReadMessageId(senderUserId,receiverUserId,deviceId,lastMessageId);
        }

        return messages;
    }
    /**
     * 获取用户在群组中的最后一条消息ID
     * @param senderUserId 发送者用户ID
     * @param receiverUserId 接收者用户ID
     * @param deviceId 设备ID
     * @return 最后一条消息ID
     */
    private Long getLastReadMessageId(Long senderUserId, Long receiverUserId, String deviceId) {
        DeviceReadOffset deviceReadOffset = deviceReadOffsetMapper.getDeviceReadOffset(senderUserId, receiverUserId, deviceId);
        if (deviceReadOffset == null) {
            return 0L;
        }
        return deviceReadOffset.getLastReadMsgId();
    }

    private void setLastReadMessageId(Long senderUserId, Long receiverUserId, String deviceId, Long lastMessageId) {
        DeviceReadOffset deviceReadOffset = new DeviceReadOffset();
        deviceReadOffset.setUserId(senderUserId);
        deviceReadOffset.setFriendUserId(receiverUserId);
        deviceReadOffset.setDeviceId(deviceId);
        deviceReadOffset.setLastReadMsgId(lastMessageId);
        deviceReadOffsetMapper.insertDeviceReadOffset(deviceReadOffset);
    }

    private Long getLastMessageId(Long senderUserId, Long receiverUserId, String deviceId) {
        DeviceReadOffset deviceReadOffset = deviceReadOffsetMapper.getDeviceReadOffset(senderUserId, receiverUserId, deviceId);
        if (deviceReadOffset == null) {
            return 0L;
        }
        return deviceReadOffset.getLastMsgId();
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
                            syncToDatabaseAsync(message);
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

    @Async("asyncUserDatabaseExecutor")
    public void syncToDatabaseAsync(Message message) {
       messageMapper.insertMessage(message);
    }
}
