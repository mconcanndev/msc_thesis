package com.acme.server.dao;

import com.acme.server.model.ChatMessage;
import com.acme.server.model.ChatRoom;
import com.acme.server.model.User;
import com.acme.server.util.DatabaseManager;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;


/**
 *  The ChatRoomDAO is a representation of the flattened ChatRoom DB structure held in the data store
 *  The data store in use is a KEY / VALUE representation of the model.ChatRoom object which will be constructed
 *  from a number of dao objects satisfying the detail provided in the URL / JSON Body & retrieved from the store
 *
 *  A ChatRoomDAO instance contains:
 *
 *  UUID chatRoomID - a system generated chatRoomID
 *  UUID chatRoomCreatorUserID - the User that created the ChatRoom
 *  UUID chatRoomParticipantID - the User invited to the ChatRoom by the creator of the ChatRoom
 *
 *   String message - actual message posted (requires encryption over HTTPS or other)

 */
public class ChatRoomDAO {

    private String chatRoomID;
    private String chatRoomCreatorUserID;
    private String chatRoomParticipantID;
    private String topic;
    private long lastModified;
    private DatabaseManager databaseManager = new DatabaseManager();

    private static Logger log = Logger.getLogger(ChatRoomDAO.class);

    public ChatRoomDAO() {
        log.info("ChatRoomDAO()");
    }

    public ChatRoomDAO(String chatRoomCreatorUserID, String chatRoomParticipantID,String topic) {
        log.info("ChatRoomDAO() with the following parameters ");

        if( chatRoomCreatorUserID!=null) {
            log.info("UserID of ChatRoom creator: " + chatRoomCreatorUserID);
        }else{
            log.info("ChatRoom creator is null");
        }


        if( chatRoomParticipantID!=null) {
            log.info("UserID of ChatRoom participant: " + chatRoomParticipantID);
        }else{
            log.info("ChatRoom participant is null");
        }

        log.info("Topic: " + topic + " " + topic);

        //TODO: Perform a look up for other ChatRooms that already exist with these two User ID's & return that
        //to the consumer instead of creating a new one if one already exists (USE HTTP RESPONSE CODE TO INDICATE)
        this.chatRoomID = "CHATROOM:" + UUID.randomUUID().toString(); //Create a new UUID
        this.topic = topic;
        this.chatRoomCreatorUserID = chatRoomCreatorUserID;
        this.chatRoomParticipantID = chatRoomParticipantID;
    }

    //Constructor that will create a ChatRoomDAO object from the ChatRoom object constructed from JSON input
    public ChatRoomDAO(ChatRoom chatRoom) {
        log.info("Entering ChatRoomDAO(Chatroom)");

        if(chatRoom.getParticipants().size() != 0){
            //Only 1:1 ChatRooms supported for now
            for(int i=0;i<chatRoom.getParticipants().size();i++){
                User chatRoomParticipant = chatRoom.getParticipants().get(i);
                if(chatRoomParticipant != null && i == 0){
                    log.info("Setting ChatRoom Creator to: " + chatRoomParticipant.getUserID());
                    this.setChatRoomCreatorUserID(chatRoomParticipant.getUserID());
                }
                else if (chatRoomParticipant != null) {
                    log.info("Setting ChatRoom Participant to: " + chatRoomParticipant.getUserID().toString());
                    this.setChatRoomParticipantID(chatRoomParticipant.getUserID());
                }
            }
        }

        this.setTopic(chatRoom.getTopic());

        //e.g. UUID is a system generated identified to guarantee uniqueness & therefore cannot be set by a consumer
        //If anything is sent in it gets ignored by this invocation.
        //if(chatRoom)
       // this.setChatRoomID(chatRoom.getChatRoomID());

        this.setChatRoomID("CHATROOM:" + UUID.randomUUID().toString());
    }

    public String getChatRoomID() {
        return chatRoomID;
    }

    public String getChatRoomCreatorUserID() {
        return chatRoomCreatorUserID;
    }

    public String getChatRoomParticipantID() {
        return chatRoomParticipantID;
    }

    public String getTopic() {
        return topic;
    }

    public void setChatRoomID(String chatRoomID){
        this.chatRoomID = chatRoomID;
    }

    public void setChatRoomCreatorUserID(String chatRoomCreatorUserID) {
        this.chatRoomCreatorUserID = chatRoomCreatorUserID;
    }

    public void setChatRoomParticipantID(String chatRoomParticipantID) {
        this.chatRoomParticipantID = chatRoomParticipantID;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }


// TODO: Every dao object that represents a specific resource should implement an interface with methods:
// persist() - push this single dao instance to the DB
// retrieve(key) - retrieve a single dao instance from the DB with , static method returning a dao populated from data

// retrieveAll() - get ALL instances of dao in the persistent data store
// retrieve(value) - get all instances of the das in the persistent data store that have the specified values


    public void persist() {
        log.info("Entering ChatRoomDAO.persist()");

        final String key = this.getChatRoomID();

        log.info("Key for new ChatRoom: " + key);
        log.info("Topic for new ChatRoom: " + this.getTopic());

        if(this.getChatRoomCreatorUserID() !=null) {
            log.info("UserID of ChatRoom creator: " + this.getChatRoomCreatorUserID());
        }else{
            log.info("ChatRoom creator is null");
        }

        if(this.getChatRoomParticipantID() !=null) {
            log.info("UserID of other participant: " + this.getChatRoomParticipantID());
        }else{
            log.info("Other Participant is null");
        }

        //Build up HashMap of data that will be pushed to the database
        final Map< String, Object > properties = new HashMap< String, Object >();

        //create a Map / Hash to hold all the message ID's for a given ChatRoom
       /* final Map<String, Object> messages = new HashMap<String, Object>();
        ChatMessage testMessage1 = new ChatMessage(this.chatRoomID,this.getChatRoomCreatorUserID(),"test Message");
        ChatMessage testMessage2 = new ChatMessage(this.chatRoomID,this.getChatRoomParticipantID(),"test response");

        //Put the two New ChatMessages into the HashMap using their ID's as the keys (consider the timestamp)..
        messages.put(testMessage1.getChatMessageID().toString(),testMessage1);
        log.info("TestMessage 1 added to Hash");

        messages.put(testMessage2.getChatMessageID().toString(),testMessage2);
        log.info("TestMessage 2 added to Hash");

        ChatMessage testRetrievalFromHash1 = (ChatMessage) messages.get(testMessage1.getChatMessageID().toString());
        log.info("TestMessage 1 retrieved from Hash: " + testRetrievalFromHash1.getMessage());

        ChatMessage testRetrievalFromHash2 = (ChatMessage) messages.get(testMessage2.getChatMessageID().toString());
        log.info("TestMessage 2 retrieved from Hash: " + testRetrievalFromHash2.getMessage());*/

        properties.put("chatroomid",this.getChatRoomID());
        properties.put("topic", this.getTopic() );
        properties.put("chatroomcreatoruserid", this.getChatRoomCreatorUserID() );
        properties.put("chatroomparticipantid", this.getChatRoomParticipantID() );
        // properties.put( "messages",messages);


        //User the databaseManager to push the HashMap constructed
        databaseManager.setStringObjectHash(key,properties);
    }

}