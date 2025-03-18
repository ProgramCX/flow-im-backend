package cn.programcx.im.pojo;

import java.sql.Timestamp;

public class DeviceReadOffset {
    private Long id;
    private Long userId;
    private Long friendUserId;
    private String deviceId;
    private Long lastReadMsgId;
    private Timestamp lastReadTime;
    private Long lastMsgId;

    public Long getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(Long lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(Long friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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
