package cn.programcx.im.dao;

import cn.programcx.im.pojo.Blacklist;
import cn.programcx.im.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface BlacklistMapper {

    void insertBlacklist(Blacklist blacklist);
    List<Blacklist> getBlockedUsers(User user);
    void deleteBlockedUser(Blacklist blacklist);
}
