package cn.programcx.im.dao;

import cn.programcx.im.pojo.Group;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@Mapper
public interface GroupMapper {
   void insertGroup(Group group);
   void deleteGroupById(Long groupId);
   void updateGroupName(Group group);
   Group getGroupById(Long groupId);
   List<Group> getCreatedUserGroups(Long userId);
   List<Group> getAdminUserGroups(Long userId);
   List<Group> getMemberUserGroups(Long userId);
}
