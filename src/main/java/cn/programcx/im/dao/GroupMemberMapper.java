package cn.programcx.im.dao;

import cn.programcx.im.pojo.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface GroupMemberMapper {
    List<GroupMember> getGroupMembersByGroupId(Long groupId);
    void insertGroupMember(GroupMember groupMember);
    void modifyGroupMemberRole(GroupMember groupMember);
    void modifyGroupMemberRemark(GroupMember groupMember);
    void deleteGroupMember(GroupMember groupMember);
}
