package cn.programcx.im.dao;

import cn.programcx.im.pojo.Friend;
import cn.programcx.im.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface FriendMapper {
    void addFriend(Friend friend);
    List<Friend> getAllFriends(Friend friend);
    List<Friend> getFriendByState(@Param("userId")Long userId , @Param("state") Friend.Status state);
    void updateFriendStatus(Friend friend);
    void deleteFriend(Friend friend);

}
