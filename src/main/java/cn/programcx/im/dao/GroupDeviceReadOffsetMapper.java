package cn.programcx.im.dao;

import cn.programcx.im.pojo.GroupDeviceReadOffset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface GroupDeviceReadOffsetMapper {
    void insertGroupDeviceReadOffset(GroupDeviceReadOffset groupDeviceReadOffset);
    GroupDeviceReadOffset getGroupDeviceReadOffset(@Param("groupId") Long groupId, @Param("deviceId") String deviceId, @Param("userId") Long userId);
}
