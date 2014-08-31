package com.acme.server.util;

import com.acme.server.dao.ChatRoomDAO;
import com.acme.server.model.ChatMessage;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisShardInfo;

import com.acme.server.model.ChatRoom;
import com.acme.server.dao.ChatMessageDAO;
import com.acme.server.model.User;



import java.util.*;

@Configuration
public class DatabaseManager {

    private static Logger log = Logger.getLogger(DatabaseManager.class);
    public RedisTemplate< String, Object > template;

    public DatabaseManager() {
        log.info("Entering RedisManager constructor");
        template = redisTemplate();
    }

    JedisConnectionFactory jedisConnectionFactory() {
        log.info("Entering AppConfig.jedisConnectionFactory");

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        JedisShardInfo shardInfo = new JedisShardInfo("127.0.0.1", 6379, 5000);
        jedisConnectionFactory.setShardInfo(shardInfo);

        log.info("Created Jedis Connection factory from App Config OK");
        return jedisConnectionFactory;
    }

    RedisTemplate< String, Object > redisTemplate() {
        log.info("Entering AppConfig.redisTemplate");

        final RedisTemplate< String, Object > template =  new RedisTemplate< String, Object >();
        template.setConnectionFactory( jedisConnectionFactory() );
        template.setKeySerializer( new StringRedisSerializer() );
        template.setHashKeySerializer( new StringRedisSerializer() );
        template.setHashValueSerializer( new GenericToStringSerializer< Object >( Object.class ) );
        template.setValueSerializer( new GenericToStringSerializer< Object >( Object.class ) );

        log.info("Template created " + template.toString());
        return template;
    }

    public void setUser(final User user) {
        log.info("Entering DatabaseManager.setUser");

        final String key = user.getUserID();
        log.info("Redis Key for new User entry: " + key);

        final Map< String, Object > properties = new HashMap< String, Object >();

        properties.put("userid",user.getUserID());
        properties.put("firstname", user.getFirstName());
        properties.put("lastname", user.getLastName());
        properties.put("nickname", user.getNickname());

        //What is the success or failure return parameter?
        log.info("Pushing new User to Redis");
        template.opsForHash().putAll(key, properties);
    }


    public User getUser(String id) {
        log.info("Entering DatabaseManager.getUser" + id);

        final String key = id;

        final String userID = (String )template.opsForHash().get( key, "userid" );
        final String firstName = (String )template.opsForHash().get( key, "firstname" );
        final String lastName = (String) template.opsForHash().get( key, "lastname" );
        final String nickName = (String) template.opsForHash().get( key, "nickname" );

        log.info("Retrieved UserID: " + userID);
        log.info("Retrieved FirstName: " + firstName);
        log.info("Retrieved LastName: " +  lastName);
        log.info("Retrieved NickName: " +  nickName);

        return new User(firstName,lastName,nickName,userID);
    }


    public void createChatRoomFromDAO(ChatRoomDAO chatRoomDAO) {
        log.info("Entering DatabaseManager.createChatRoom");

        final String key = chatRoomDAO.getChatRoomID();
        log.info("Redis Key for new ChatRoom entry: " + key);

        final Map< String, Object > properties = new HashMap< String, Object >();

        properties.put("chatroomid",chatRoomDAO.getChatRoomID());
        properties.put("topic", chatRoomDAO.getTopic());
        log.info("Set topic to: " + chatRoomDAO.getTopic());

        properties.put("chatroomcreatoruserid", chatRoomDAO.getChatRoomCreatorUserID());
        properties.put("chatroomparticipantid", chatRoomDAO.getChatRoomParticipantID());

        log.info("Pushing new Chatroom to Redis");
        template.opsForHash().putAll(key, properties);

    }


    public ChatRoomDAO retrieveChatRoomDAO(String chatRoomID ) {
        log.info("Entering DatabaseManager.getChatRoom");

        final String key = chatRoomID;
        ChatRoomDAO chatRoomDAO = new ChatRoomDAO();
        log.info("Entering RedisManager.getChatRoom with ChatRoom ID" + key);

        final String chatRoomIDRetrieved = (String) template.opsForHash().get( key, "chatroomid" );
        final String topic = ( String )template.opsForHash().get( key, "topic" );
        final String chatRoomCreatorUserID = (String) template.opsForHash().get( key, "chatroomcreatoruserid" );
        final String chatRoomParticipantID = (String) template.opsForHash().get( key, "chatroomparticipantid" );

        log.info("Retrieved ChatRoomID: " + chatRoomIDRetrieved);
        chatRoomDAO.setChatRoomID(chatRoomIDRetrieved);

        log.info("Retrieved Topic: " + topic);
        chatRoomDAO.setTopic(topic);

        log.info("Retrieved CreatorID: " + chatRoomCreatorUserID);
        chatRoomDAO.setChatRoomCreatorUserID(chatRoomCreatorUserID);

        log.info("Retrieved ParticipantID: " + chatRoomParticipantID);
        chatRoomDAO.setChatRoomParticipantID(chatRoomParticipantID);

         return chatRoomDAO;
    }

    //PUT exception firing here:
    public void createChatMessageFromDAO(ChatMessageDAO chatMessageDAO) {
        log.info("Entering DatabaseManager.createChatMessageFromDAO");

        final String key = chatMessageDAO.getChatMessageID();
        log.info(" Key for new ChatMessage entry: " + key);

        final Map< String, Object > properties = new HashMap< String, Object >();

        properties.put("chatmessageid",chatMessageDAO.getChatMessageID());
        log.info("Pushing chatmessageid: " + chatMessageDAO.getChatMessageID());

        properties.put("chatroomid", chatMessageDAO.getChatRoomID());
        log.info("Pushing chatroomid: " + chatMessageDAO.getChatRoomID());

        properties.put("fromParticipantID", chatMessageDAO.getFromParticipantID());
        log.info("Pushing fromParticipantID: " + chatMessageDAO.getFromParticipantID());

        properties.put("message", chatMessageDAO.getMessage());
        log.info("Pushing Message: " + chatMessageDAO.getMessage());

        properties.put("lastmodified", chatMessageDAO.getLastModified());
        log.info("Pushing lastmodified: " + chatMessageDAO.getLastModified());

        //TODO control this so that you can only set the readreceipt from false --> true, not vice versa
        properties.put("readreceipt", chatMessageDAO.getReadReceipt());
        log.info("Pushing new ChatMessage to Database - readReceipt: " + chatMessageDAO.getReadReceipt());

        template.opsForHash().putAll(key, properties);

    }

    //bringing back a null participant ID...why...?
    public ChatMessageDAO retrieveChatMessageDAO(String chatMessageID) {
        log.info("Entering DatabaseManager.retrieveChatMessageDAO: " + chatMessageID);

        final String key = chatMessageID;
        ChatMessageDAO chatMessageDAO = new ChatMessageDAO();

        final String chatMessageIDRetrieved = (String) template.opsForHash().get( key, "chatmessageid" );
        final String chatRoomIDRetrieved = (String) template.opsForHash().get( key, "chatroomid" );
        final String fromParticipantID = ( String )template.opsForHash().get( key, "fromParticipantID" );
        final String message = (String) template.opsForHash().get( key, "message" );
        final String readReceipt = (String) template.opsForHash().get(key, "readreceipt");
       // final String lastmodified = (String) template.opsForHash().get( key, "lastmodified" );

        log.info("Retrieved ChatMessage ID: " + chatMessageIDRetrieved);
        chatMessageDAO.setChatMessageID(chatMessageIDRetrieved);

        log.info("Retrieved ChatRoom ID: " + chatRoomIDRetrieved);
        chatMessageDAO.setChatRoomID(chatRoomIDRetrieved);

        log.info("Retrieved Participant ID: " + fromParticipantID);
        chatMessageDAO.setFromParticipantID(fromParticipantID);

        log.info("Retrieved message: " + message);
        chatMessageDAO.setMessage(message);

        log.info("Retrieved read receipt: " + readReceipt);
        chatMessageDAO.setReadReceipt(readReceipt);

        //log.info("Retrieved lastmodified: " + lastmodified);
        //chatMessageDAO.setLastModified((Long)lastmodified);

        return chatMessageDAO;
    }


    //TODO Modify to insert DAO layer & follow the same pattern as ChatMessage & ChatRoom to future proof & guarantee
    //consistency of design. There is no benefit to doing this in the case of a User that does not contact other subresouces
    //that get reconstructed at the Service layer but there may be a reason in the future.
    public List<User> getAllUsers() {
        log.info("Entering DatabaseManager.getAllUser");

        List<User> allUsers = new ArrayList<User>();

        Set<String> keys = template.keys("USER:*");
        log.info("Print keys: " + keys);

        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext()) {
            String nextElement = iterator.next();

            User nextUser = getUser(nextElement);
            allUsers.add(nextUser);
        }

        return allUsers;
    }

    public List<ChatRoomDAO> getAllChatRoomDAOs(String userID) {
        log.info("Entering DatabaseManager.getAllChatRooms with userID " + userID);

        List<ChatRoomDAO> allChatRoomDAOs = new ArrayList<ChatRoomDAO>();

        //TODO: ChatRoom ID's need reference to the users if we are to easily find ChatRooms for a user
        Set<String> keys = template.keys("CHATROOM:*");
        log.info("Print keys: " + keys);

        Iterator<String> iterator = keys.iterator();
       while(iterator.hasNext()) {
            String nextElement = iterator.next();

            ChatRoomDAO nextChatRoomDAO = retrieveChatRoomDAO(nextElement);
            allChatRoomDAOs.add(nextChatRoomDAO);

        }

        return allChatRoomDAOs;
    }

    public List<ChatMessageDAO> getAllChatMessageDAOs(String chatRoomID) {
        log.info("Entering DatabaseManager.getAllChatMessageDAOs: " + chatRoomID);

        List<ChatMessageDAO> allChatMessageDAOs = new ArrayList<ChatMessageDAO>();

        //TODO use this to get all the keys needed.
        Set<String> keys = template.keys("MESSAGE:" + chatRoomID + "*");
        log.info("Print keys: " + keys);

        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext()) {
            String nextElement = iterator.next();

            ChatMessageDAO nextMessageRoomDAO = retrieveChatMessageDAO(nextElement);
            allChatMessageDAOs.add(nextMessageRoomDAO);
        }

        return allChatMessageDAOs;
    }

    //Invoked by the persist methods on the ChatRoomDAO & ChatMessageDAO
    public void setStringObjectHash(String key, Map< String, Object > properties){
        log.info("Entering DatabaseManager.setStringObjectHash");
        template.opsForHash().putAll(key, properties);
    }

}