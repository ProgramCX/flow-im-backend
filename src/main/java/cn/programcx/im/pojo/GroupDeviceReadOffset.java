package cn.programcx.im.pojo;

import java.sql.Timestamp;

public class GroupDeviceReadOffset {
    private Long id;
    private Long groupId;
    private String deviceId;
    private Long userId;
    private Long lastReadMsgId;
    private Timestamp lastReadTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLastReadMsgId() {
        return lastReadMsgId;
    }

    public void setLastReadMsgId(Long lastReadMsgId) {
        this.lastReadMsgId = lastReadMsgId;
    }

    public Timestamp getLastReadTime() {
        return lastReadTime;
    }

    public void setLastReadTime(Timestamp lastReadTime) {
        this.lastReadTime = lastReadTime;
    }
}
