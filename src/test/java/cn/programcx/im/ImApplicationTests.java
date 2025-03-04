package cn.programcx.im;

import cn.programcx.im.dao.BlacklistMapper;
import cn.programcx.im.dao.FriendMapper;
import cn.programcx.im.dao.MessageMapper;
import cn.programcx.im.dao.UserMapper;
import cn.programcx.im.pojo.Blacklist;
import cn.programcx.im.pojo.Friend;
import cn.programcx.im.pojo.Message;
import cn.programcx.im.pojo.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ImApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Test
    void insertUser() {
        User user = new User();
        user.setUserName("刘明浩");
        user.setEmail("2379355158@qq.com");
        user.setPasswordHash("65+98589");
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
        User from = userMapper.getUserById(2L);
        User to = userMapper.getUserById(1L);
        System.out.println(from.getUserId());
        System.out.println(to.getUserId());
        Message message = new Message();
        message.setContent("程旭菜鸟，你好");
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
        List<Message> messageList = messageMapper.getMessageBySenderAndReceiverId(1L, 2L);
        for (Message message : messageList) {
            System.out.println(message.getContent());
        }
    }

    @Test
    void updateMsg() {
        Message message = new Message();
        message.setMessageId(1L);
        message.setState(Message.State.read);
        messageMapper.updateMessageState(message);
    }

    @Test
    void deleteMsg() {
        messageMapper.deleteMessageById(1L);
    }

    @Test
    void addFriend() {
        User user = userMapper.getUserById(2L);
        User friend = userMapper.getUserById(4L);
        Friend friendRelation = new Friend();
        friendRelation.setFriend(friend);
        friendRelation.setUser(user);
        friendRelation.setStatus(Friend.Status.accepted);
        friendMapper.addFriend(friendRelation);
    }

    @Test
    void getAllFriends() {
        User user = userMapper.getUserById(1L);
        Friend friend = new Friend();
        friend.setUser(userMapper.getUserById(1L));
        List<Friend> friends = friendMapper.getAllFriends(friend);
        for (Friend fd : friends) {
            System.out.println(fd.getFriend().getUserName());
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

}