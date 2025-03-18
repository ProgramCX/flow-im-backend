package cn.programcx.im.dao;

import cn.programcx.im.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MessageMapper {
    void insertMessage(Message message);
    List<Message> getMessageById(Long id);
    List<Message> getMessageBySenderId(Long id);
    List<Message> getMessageByReceiverId(Long id);
    List<Message> getMessageBySenderAndReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId, @Param("lastMessageId") Long lastMessageId, @Param("limit") Integer limit);
    List<Message> getMessageBySenderOrReceiverId(Long id);
    void updateMessageState(Message message);
    void deleteMessageById(Long id);
}
