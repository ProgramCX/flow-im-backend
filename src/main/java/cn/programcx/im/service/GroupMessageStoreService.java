package cn.programcx.im.service;

import cn.programcx.im.dao.GroupMessageMapper;
import cn.programcx.im.pojo.GroupDeviceReadOffset;
import cn.programcx.im.pojo.GroupMessage;
import cn.programcx.im.dao.GroupDeviceReadOffsetMapper;
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
public class GroupMessageStoreService {
    private final String REDIS_GROUP_MESSAGE_KEY = "msg-group-id";
    private final String REDIS_GROUP_MESSAGE_QUEUE_KEY = "msg-group-queue";

    @Resource(name = "redisTemplate")
    private RedisTemplate  redisTemplate;

    @Autowired
    private GroupMessageMapper groupMessageMapper;

    @Autowired
    private GroupDeviceReadOffsetMapper groupDeviceReadOffsetMapper;

    /**
     * 缓存群组消息
     *
     * @param groupMessage 群组消息对象
     */
    public void cacheMessage(GroupMessage groupMessage) {
        if (groupMessage == null) {
            return;
        }

        Long msgId = redisTemplate.opsForValue().increment(REDIS_GROUP_MESSAGE_KEY);
        groupMessage.setMessageId(msgId);
        final String key = "im:msg:group:" + groupMessage.getGroupId();
        redisTemplate.opsForZSet().add(key, groupMessage, msgId);
    }

    /**
     * 获取群组消息
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 消息列表
     */
    public List<GroupMessage> getRedisGroupMessage(Long groupId,Long userId,String deviceId) {
        // 获取用户在群组中的最后一条消息ID
        Long lastReadMessageId = getLastReadMessageId(groupId,userId,deviceId);
        Long lastMsgId = getLastMessageId(groupId,userId,deviceId);

        final String key = "im:msg:group:" + groupId;
        Object value = redisTemplate.opsForValue().get(REDIS_GROUP_MESSAGE_KEY);
//        Long msgId = 0L;
//
//        if (value instanceof Number) {
//            msgId = ((Number) value).longValue();
//        }
        Set<GroupMessage> set = redisTemplate.opsForZSet().reverseRangeByScore(key, lastReadMessageId + 1, Long.MAX_VALUE, 0, 10);
        List<GroupMessage> messages = new ArrayList<>();
        if (set != null) {
            for (Object o : set) {
                if (o instanceof GroupMessage) {
                   messages.add((GroupMessage)o);
                }
            }
        }

        if (messages.size() < 10) {
            List<GroupMessage> groupMessages = groupMessageMapper.getGroupMessagesByGroupId(groupId, lastReadMessageId, 10L - messages.size());
            // 使用Set避免重复消息
            Set<GroupMessage> messageSet = new HashSet<>(messages);
            for (GroupMessage groupMessage : groupMessages) {
                if (messageSet.add(groupMessage)) {
                    messages.add(groupMessage);
                }
            }
        }

        if(messages.size() == 10){
            Long lastLoadedMessageId  = messages.get(messages.size()-1).getMessageId();
            if(lastLoadedMessageId > lastReadMessageId){
                setLastReadMessageId(groupId,userId,deviceId,lastLoadedMessageId,lastMsgId);
            }else{
                setLastReadMessageId(groupId,userId,deviceId,lastMsgId,lastMsgId);
            }
        }else{
            setLastReadMessageId(groupId,userId,deviceId,lastMsgId,lastMsgId);
        }
        return messages;
    }

    /**
     * 获取用户在群组中的最后一条未读消息ID
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 最后一条消息ID
     */
    private Long getLastReadMessageId(Long groupId,Long userId,String deviceId) {
        GroupDeviceReadOffset groupDeviceReadOffset = groupDeviceReadOffsetMapper.getGroupDeviceReadOffset(groupId,deviceId,userId);
        if (groupDeviceReadOffset != null) {
            return groupDeviceReadOffset.getLastReadMsgId();
        }
        return 0L;
    }

    private void setLastReadMessageId(Long groupId,Long userId,String deviceId,Long lastReadMessageId,Long lastMessageId) {
        GroupDeviceReadOffset groupDeviceReadOffset = new GroupDeviceReadOffset();
            groupDeviceReadOffset.setGroupId(groupId);
            groupDeviceReadOffset.setDeviceId(deviceId);
            groupDeviceReadOffset.setUserId(userId);
            groupDeviceReadOffset.setLastReadMsgId(lastReadMessageId);
            groupDeviceReadOffsetMapper.insertGroupDeviceReadOffset(groupDeviceReadOffset);
    }


    private Long getLastMessageId(Long groupId,Long userId,String deviceId) {
        GroupDeviceReadOffset groupDeviceReadOffset = groupDeviceReadOffsetMapper.getGroupDeviceReadOffset(groupId,deviceId,userId);
        if (groupDeviceReadOffset == null) {
            return 0L;
        }
        return groupDeviceReadOffset.getLastReadMsgId();
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
                           syncGroupMessageToDatabase(message);
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

    @Async("asyncGroupMessageDatabaseExecutor")
    public void syncGroupMessageToDatabase(GroupMessage groupMessage) {
        groupMessageMapper.insertGroupMessage(groupMessage);
    }
}
