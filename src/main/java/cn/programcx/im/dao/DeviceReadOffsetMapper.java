package cn.programcx.im.dao;

import cn.programcx.im.pojo.DeviceReadOffset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface DeviceReadOffsetMapper {
    void insertDeviceReadOffset(DeviceReadOffset deviceReadOffset);
    DeviceReadOffset getDeviceReadOffset(@Param("userId")Long userId,@Param("friendUserId") Long friendUserId,@Param("deviceId") String deviceId);
}
