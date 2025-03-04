package cn.programcx.im.dao;

import cn.programcx.im.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface UserMapper {
    void addUser(User user);

    User getUserById(Long id);

    List<User> getUserByName(String name);

    void updateUser(User user);

    void deleteUserById(Long id);
}
