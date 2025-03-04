package cn.programcx.im.dao;

import cn.programcx.im.pojo.GroupMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface GroupMessageMapper {
    void insertGroupMessage(GroupMessage groupMessage);
    void deleteGroupMessageByMessageId(Long messageId);
    List<GroupMessage> getGroupMessagesByGroupId(Long groupId);
}
