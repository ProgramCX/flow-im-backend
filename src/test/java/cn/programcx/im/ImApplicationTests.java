package cn.programcx.im;

import cn.programcx.im.dao.*;
import cn.programcx.im.dao.GroupDeviceReadOffsetMapper;
import cn.programcx.im.pojo.*;
import cn.programcx.im.service.GroupMessageStoreService;
import cn.programcx.im.service.UserMessageStoreService;
import cn.programcx.im.service.UserStateService;
import cn.programcx.im.util.DateTimeUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ImApplicationTests {

    private static Dotenv dotenv;

    @Autowired
    private UserMessageStoreService userMessageStoreService;

    @Autowired
    private GroupMessageStoreService groupMessageStoreService;

    @BeforeAll
    static void setup() {
        dotenv = Dotenv.configure()
                .directory(".")
                .filename(".env")  // 指定文件名
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @Test
    void testSchedule() {
        userMessageStoreService.syncToDatabase();
    }
    @Test
    void testCacheMessage() {
        Message message = new Message();
        message.setReceiverUserId(1L);
        message.setSenderUserId(2L);
        message.setContent("Hello, lmh!");
        message.setCreatedAt(DateTimeUtil.getCreatedAt());
        message.setState(Message.State.sent);
        message.setMessageId(22L);
        User sender = new User();
        sender.setUserId(1L);

        User receiver = new User();
        receiver.setUserId(2L);
        message.setSender(sender);
        message.setReceiver(receiver);
        userMessageStoreService.cacheMessage(message);
    }

    @Test
    void testGroupMessages() {
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setGroupId(1L);
        groupMessage.setContent("Hello, lmh!");
        groupMessage.setCreatedAt(DateTimeUtil.getCreatedAt());
        groupMessage.setMessageId(20L);
        Group group = new Group();
        group.setGroupId(1L);
        User sender = new User();
        sender.setUserId(1L);
        groupMessage.setGroup(group);
        groupMessage.setSenderUser(sender);
        groupMessageStoreService.cacheMessage(groupMessage);
    }

    @Test
    void testGetGroupMessages() {
        groupMessageStoreService.syncGroupMessages();
    }
    @Test
    void testGetUserMessages() {
        List<Message> messages = userMessageStoreService.getRedisUserMessages("1", "2", 0L);
        for (Message message : messages) {
            System.out.println(message.getContent());
        }
    }

    @Test
    void contextLoads() {
    }

    @Autowired
    UserStateService userStateService;

    @Test
    public void testUserStateService() {
        userStateService.setUserOnline(1L);
    }
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private GroupMessageMapper groupMessageMapper;

    @Autowired
    private GroupDeviceReadOffsetMapper groupDeviceReadOffsetMapper;

    @Autowired
    private DeviceReadOffsetMapper deviceReadOffsetMapper;

    @Test
    void insertUser() {
        User user = new User();
        user.setUserName("郭子梁");
        user.setEmail("553503285@qq.com");
        user.setPasswordHash("aefarfresg28");
        userMapper.addUser(user);
    }

    @ParameterizedTest
    @CsvSource({"小航,afsjsadfhsa@shfa.cn,123456",
            "小明,adsjf@aes.com,128756",})
    void insertUser(String userName, String email, String passwordHash) {
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        userMapper.addUser(user);
    }

    @Test
    void sendMessage() {
        User from = userMapper.getUserById(1L);
        User to = userMapper.getUserById(2L);
        System.out.println(from.getUserId());
        System.out.println(to.getUserId());
        Message message = new Message();
        message.setContent("刘明浩大佬带带我😍😍😍");
        message.setSender(from);
        message.setReceiver(to);
        message.setState(Message.State.sent);
        messageMapper.insertMessage(message);
    }

    @Test
    void getMessage() {
        List<Message> messageList = messageMapper.getMessageById(1L);
        for (Message message : messageList) {
            System.out.println(message.getContent());
        }
    }

    @Test
    void getMessageBySenderId() {
        List<Message> messageList = messageMapper.getMessageBySenderId(1L);
        for (Message message : messageList) {
            System.out.println(message.getContent());
        }
    }

    @Test
    void getMessageByReceiverId() {
        List<Message> messageList = messageMapper.getMessageByReceiverId(1L);
        for (Message message : messageList) {
            System.out.println(message.getContent());
        }
    }

    @Test
    void getMessageBySOrRId() {
        List<Message> messageList = messageMapper.getMessageBySenderOrReceiverId(1L);
        for (Message message : messageList) {
            System.out.println(message.getContent());
        }
    }

    @Test
    void getMessageBySAndRId() {
        DeviceReadOffset deviceReadOffset = deviceReadOffsetMapper.getDeviceReadOffset(1L,2L,"eaf4897ea8eabfff");
        List<Message> messageList = messageMapper.getMessageBySenderAndReceiverId(1L, 2L,deviceReadOffset.getLastReadMsgId());
        if(messageList.isEmpty()){
            return;
        }
        Long lastReadMsgId = messageList.get(0).getMessageId();
        for (Message message : messageList) {
            System.out.println(message.getContent());
        }
        deviceReadOffset.setLastReadMsgId(lastReadMsgId);
        deviceReadOffsetMapper.insertDeviceReadOffset(deviceReadOffset);
    }

    @Test
    void updateMsg() {
        Message message = new Message();
        message.setMessageId(5L);
        message.setState(Message.State.read);
        messageMapper.updateMessageState(message);
    }

    @Test
    void deleteMsg() {
        messageMapper.deleteMessageById(1L);
    }

    @Test
    void addFriend() {
        User user = userMapper.getUserById(1L);
        User friend = userMapper.getUserById(5L);
        Friend friendRelation = new Friend();
        friendRelation.setFriend(friend);
        friendRelation.setUser(user);
        friendRelation.setStatus(Friend.Status.accepted);
        friendMapper.addFriend(friendRelation);
        System.out.println(friendRelation.getId());
    }

    @Test
    void getAllFriends() {
        User user = userMapper.getUserById(1L);
        Friend friend = new Friend();
        friend.setUser(userMapper.getUserById(1L));
        List<Friend> friends = friendMapper.getAllFriends(friend);
        for (Friend fd : friends) {
            System.out.println(fd.getFriend());
        }
    }

    @Test
    void getFriendByState() {
        User user = userMapper.getUserById(1L);
        List<Friend> friends = friendMapper.getFriendByState(user.getUserId(), Friend.Status.pending);
        for (Friend fd : friends) {
            System.out.println(fd.getFriend().getUserName());
        }
    }

    @Test
    void acceptFriend() {
        User user = userMapper.getUserById(1L);
        User friend = userMapper.getUserById(2L);
        Friend friendRelation = new Friend();
        friendRelation.setUser(user);
        friendRelation.setFriend(friend);
        friendRelation.setStatus(Friend.Status.pending);
        friendMapper.updateFriendStatus(friendRelation);

//        assert friendMapper.getFriendByState(user.getUserId(), Friend.Status.accepted).size() == 1;
    }

    @Test
    void deleteFriend() {
        User user = userMapper.getUserById(2L);
        User friend = userMapper.getUserById(4L);
        Friend friendRelation = new Friend();
        friendRelation.setUser(user);
        friendRelation.setFriend(friend);
        friendMapper.deleteFriend(friendRelation);
        assert friendMapper.getAllFriends(friendRelation).isEmpty();
    }

    @Test
    void addBlackUser(){
        User user1 = userMapper.getUserById(1L);
        User user2 = userMapper.getUserById(2L);
        Blacklist blacklist = new Blacklist();
        blacklist.setUser(user1);
        blacklist.setBlockedUser(user2);
        blacklistMapper.insertBlacklist(blacklist);
    }
    @Test
    void getBlackedList(){
        User user = userMapper.getUserById(1L);
        List<Blacklist> blacklists = blacklistMapper.getBlockedUsers(user);
        for(Blacklist blacklist: blacklists){
            System.out.println(blacklist.getBlockedUser().getUserName());
        }
    }
    @Test
    void deleteBlackUser(){
        User user1 = userMapper.getUserById(1L);
        User user2 = userMapper.getUserById(2L);
        Blacklist blacklist = new Blacklist();
        blacklist.setUser(user1);
        blacklist.setBlockedUser(user2);
        blacklistMapper.deleteBlockedUser(blacklist);
    }


    @Test
    void createGroup(){
        User user = userMapper.getUserById(2L);
        Group group = new Group();
        group.setGroupName("Java技术交流群——群主是大佬");
        group.setOwnerUser(user);
        groupMapper.insertGroup(group);
        System.out.println(group.getGroupId());
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRemark("大佬");
        groupMember.setRole(GroupMember.Role.owner);
        groupMemberMapper.insertGroupMember(groupMember);

    }

    @Test
    void getGroup(){
        Group group = groupMapper.getGroupById(1L);
        System.out.println(group.getGroupName());
    }

    @Test
    void updateGroupName(){
        Group group = groupMapper.getGroupById(1L);
        group.setGroupName("Java综合实训");
        groupMapper.updateGroupName(group);
    }

    @Test
    void deleteGroup(){
        groupMapper.deleteGroupById(2L);
    }

    @Test
    void addGroupMember(){
        User user = userMapper.getUserById(2L);
        Group group = groupMapper.getGroupById(2L);
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRole(GroupMember.Role.admin);
        groupMember.setRemark("大佬");
        groupMemberMapper.insertGroupMember(groupMember);
    }

    @Test
    void getGroupMembers(){
        Group group = groupMapper.getGroupById(1L);
        List<GroupMember> groupMembers = groupMemberMapper.getGroupMembersByGroupId(group.getGroupId());
        for(GroupMember groupMember: groupMembers){
            System.out.println(groupMember.getUser().getUserName());
        }
    }

    @Test
    void modifyGroupMemberRole(){
        User user = userMapper.getUserById(2L);
        Group group = groupMapper.getGroupById(1L);
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRole(GroupMember.Role.member);
        groupMemberMapper.modifyGroupMemberRole(groupMember);
    }

    @Test
    void modifyGroupMemberRemark(){
        User user = userMapper.getUserById(1L);
        Group group = groupMapper.getGroupById(1L);
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRemark("菜鸟");
        groupMemberMapper.modifyGroupMemberRemark(groupMember);
    }

    @Test
    void deleteGroupMember(){
        User user = userMapper.getUserById(2L);
        Group group = groupMapper.getGroupById(1L);
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMemberMapper.deleteGroupMember(groupMember);
    }

    @Test
    void addGroupMessage(){
        User user = userMapper.getUserById(1L);
        Group group = groupMapper.getGroupById(1L);
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setGroup(group);
        groupMessage.setSenderUser(user);
        groupMessage.setContent("以后向大佬请教，请大佬多多包涵！！！");
        groupMessageMapper.insertGroupMessage(groupMessage);
    }

    @Test
    void getGroupMessage(){
        Group group = groupMapper.getGroupById(1L);
        GroupDeviceReadOffset groupDeviceReadOffset = groupDeviceReadOffsetMapper.getGroupDeviceReadOffset(1L, "eaf4897ea8eabfff", 1L);
        System.out.println(groupDeviceReadOffset.getLastReadMsgId());
        List<GroupMessage> groupMessages = groupMessageMapper.getGroupMessagesByGroupId(group.getGroupId(),groupDeviceReadOffset.getLastReadMsgId());
        if(groupMessages.isEmpty()){
            return;
        }
        Long lastReadMsgId = groupMessages.get(0).getMessageId();
        for(GroupMessage groupMessage: groupMessages){
            System.out.println(groupMessage.getContent());
        }
        System.out.println(lastReadMsgId);
        groupDeviceReadOffset.setLastReadMsgId(lastReadMsgId);
        groupDeviceReadOffsetMapper.insertGroupDeviceReadOffset(groupDeviceReadOffset);
    }

    @Test
    void deleteGroupMessage(){
        User user = userMapper.getUserById(1L);
        Group group = groupMapper.getGroupById(1L);

        groupMessageMapper.deleteGroupMessageByMessageId(2L);
    }

    @Test
    void getGroupsAdmin(){
//        User user = userMapper.getUserById(1L);
        List<Group> groups = groupMapper.getMemberUserGroups(2L);
        for(Group group: groups){
            System.out.println(group.getGroupName());
        }
    }

    @Test
    void markReadMessage(){
        GroupDeviceReadOffset groupDeviceReadOffset = new GroupDeviceReadOffset();
        groupDeviceReadOffset.setGroupId(1L);
        groupDeviceReadOffset.setDeviceId("eaf4897ea8eabfff");
        groupDeviceReadOffset.setUserId(1L);
        groupDeviceReadOffset.setLastReadMsgId(1L);
        groupDeviceReadOffsetMapper.insertGroupDeviceReadOffset(groupDeviceReadOffset);
    }

    @Test
    void getGroupDeviceReadOffset() {
        GroupDeviceReadOffset groupDeviceReadOffset = groupDeviceReadOffsetMapper.getGroupDeviceReadOffset(1L, "eaf4897ea8eabfff", 1L);
        System.out.println(groupDeviceReadOffset.getLastReadMsgId());
    }

    @Test
    void markOneToOneMessageRead() {
        DeviceReadOffset deviceReadOffset = new DeviceReadOffset();
        deviceReadOffset.setDeviceId("eaf4897ea8eabfff");
        deviceReadOffset.setUserId(1L);
        deviceReadOffset.setLastReadMsgId(2L);
        deviceReadOffset.setFriendUserId(2L);
        deviceReadOffsetMapper.insertDeviceReadOffset(deviceReadOffset);
    }

    @Test
    void getDeviceReadOffset() {
        DeviceReadOffset deviceReadOffset = deviceReadOffsetMapper.getDeviceReadOffset(1L, 2L,"eaf4897ea8eabfff");
        System.out.println(deviceReadOffset.getLastReadMsgId());
    }
}

//TODO: 接收消息偏移量