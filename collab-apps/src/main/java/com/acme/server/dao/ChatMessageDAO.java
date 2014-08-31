package com.acme.server.dao;

import com.acme.server.model.ChatMessage;
import com.acme.server.model.ChatRoom;
import com.acme.server.model.User;
import com.acme.server.util.DatabaseManager;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Collection;


/**
 *  The ChatMessageDAO is a representation of the flattened ChatMessage DB structure held in the data store
 *  The data store in use is a KEY / VALUE representation of the model.ChatMessage object which will be constructed
 *  from a number of dao objects satisfying the detail provided in the URL / JSON Body & retrieved from the store
 *
 *  A ChatMessageDAO instance contains:
 *
 *  UUID chatRoomID - a system generated chatRoomID
 *  UUID chatRoomCreatorUserID - the User that created the ChatRoom
 *  UUID chatRoomParticipantID - the User invited to the ChatRoom by the creator of the ChatRoom
 *
 *   String message - actual message posted (requires encryption over HTTPS or other)

 */
public class ChatMessageDAO {

    private String chatMessageID;       //ChatMessage resource identifier, used on PUTs to set read receipt
    private String chatRoomID;          //Every message has an associated ChatRoom
    private String fromParticipantID;   //The participant that posted the message
    private String message;           //Actual message, needs encryption
    private long lastModified;           //Server generated timestamp of when the message was last modified (created or updated)
    private String readReceipt;      //Flag indicating whether the other Participant in the chatroom read the message (Assumes 1:1)

    private DatabaseManager databaseManager = new DatabaseManager();

    private static Logger log = Logger.getLogger(ChatMessageDAO.class);

    public ChatMessageDAO() {
        log.info("ChatRoomDAO()");
    }

    public ChatMessageDAO(String chatRoomID, String fromParticipantID, String message, String readReceipt) {
        log.info("ChatMessageDAO()");


        if( chatRoomID!=null) {
            log.info("ChatRoomID : " + chatRoomID);

            //TODO Look it up to ensure that this is a valid ChatRoom
            this.chatRoomID = chatRoomID;
        }

        if( fromParticipantID!=null) {
            log.info("fromParticipantID t: " + fromParticipantID);
            this.fromParticipantID = fromParticipantID;
        }else{
            log.info("fromParticipantID is null");
        }

        //Note: Should not log messages in real world case, they need to be encrypted
        log.info("Message: " + message);
        this.message = message;

        //need to default to 'false' if nothing entered.
        this.readReceipt = readReceipt;

        //e.g. UUID & lastModified are system generated & therefore cannot be set by a consumer
        this.chatMessageID = "MESSAGE:" + chatRoomID + ":" + UUID.randomUUID().toString();
       // this.lastModified = new Date().getTime();
    }

    //Constructor that will create a ChatMessageDAO object from the ChatMessage object constructed from JSON input
    public ChatMessageDAO(ChatMessage chatMessage) {
        log.info("Entering ChatMessageDAO(ChatMesssage)");

        this.setChatRoomID(chatMessage.getChatRoomID());
        this.setFromParticipantID(chatMessage.getFromParticipantID());
        this.setMessage(chatMessage.getMessage());
        this.setReadReceipt(chatMessage.getReadReceipt());

        //e.g. UUID & lastModified are system generated & therefore cannot be set by a consumer
        //If anything is sent in it gets ignored by this invocation.
        this.setChatMessageID("MESSAGE:" + chatRoomID + ":" + UUID.randomUUID().toString());
       // this.setLastModified(new Date().getTime());
    }


    public String getChatMessageID() {return chatMessageID;}

    public String getChatRoomID() {
        return chatRoomID;
    }

    public String getFromParticipantID() {return fromParticipantID; }

    public String getMessage() {return message;}

    public long getLastModified() {return lastModified;}

    public String getReadReceipt() {return readReceipt;}

    public void setChatMessageID(String chatMessageID) {
        this.chatMessageID = chatMessageID;
    }

    public void setChatRoomID(String chatRoomID) {
        this.chatRoomID = chatRoomID;
    }

    public void setFromParticipantID(String fromParticipantID) {
        this.fromParticipantID = fromParticipantID;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public void setReadReceipt(String readReceipt) {
        this.readReceipt = readReceipt;
    }

    public void persist() {
        log.info("Entering ChatMessageDAO.persist()");

        final String key = this.chatMessageID.toString();

        log.info("Key for new ChatMessage: " + key);

        if(this.getChatRoomID() !=null) {
            log.info("ChatRoomID: " + this.getChatRoomID().toString());
        }else{
            log.info("ChatRoomID is null"); //throw error
        }

        if(this.getFromParticipantID() !=null) {
            log.info("ChatMessage was sent from: " + this.getFromParticipantID().toString());
        }else{
            log.info("Other Participant is null");
        }

        //Build up HashMap of data that will be pushed to the database
        final Map< String, Object > properties = new HashMap< String, Object >();

        properties.put("chatmessageid",this.getChatMessageID());
        properties.put("chatroomid", this.getChatRoomID() );
        properties.put("fromParticipantID", this.getFromParticipantID() );
        properties.put("message", this.getMessage() );
        properties.put("readreceipt", this.getReadReceipt() );
        properties.put("lastmodified", new Date().getTime() );

        //Use the databaseManager to push the HashMap constructed
        databaseManager.setStringObjectHash(key,properties);
    }


/*  public Collection<ChatMessageDAO> retrieveByChatRoomID(String chatRoomID) {
        log.info("Entering ChatMessageDAO.retrieveByChatRoomID: " + chatRoomID);
        final String key = chatRoomID;

        //Populate a HashMap from the ChatMessageID which is the key used to store the HashMap Object
       // Collection<Map<String, Object>> myCollection = new Collection<Map<String, Object>>();

        Map<String, Object> retrievedFromDB = databaseManager.getStringObjectHash(key);

        final String chatMessageIDRetrieved = ( String )retrievedFromDB.get("chatmessageid");
        final String chatRoomIDRetrieved = ( String )retrievedFromDB.get("chatroomid");
        final String fromParticipantID = ( String )retrievedFromDB.get("fromparticipantid");
        final String message = ( String )retrievedFromDB.get("message");

        log.info("Trying to convert to Boolean: ");
        final Boolean readReceipt = ( Boolean )retrievedFromDB.get( "readreceipt");

        log.info("Trying to convert to Long: ");
        final Long lastModified = ( Long )retrievedFromDB.get( "lastmodified");

        //Set the properties on this instance of the ChatMessageDAO to return to the MessagingService for translation
        //into a model.ChatMessage instance for JSON delivery back to the API consumer (complete with full resource representation)
        //i.e. including User Objects associated with this ChatMessage.

        log.info("Populating the ChatMessageDAO from DB info retrieved ");
        if(chatMessageIDRetrieved !=null) {
            this.setChatMessageID(UUID.fromString(chatMessageIDRetrieved));
        }else{
            log.info("ChatMessageID retrieved is null"); //Do something
        }

        if(chatRoomIDRetrieved !=null) {
            this.setChatRoomID(UUID.fromString(chatRoomIDRetrieved));
        }else{
            log.info("ChatRoomID retrieved is null");
        }

        if(fromParticipantID !=null) {
            this.setFromParticipantID((UUID.fromString(fromParticipantID)));
        }else{
            log.info("FromParticipantID retrieved is null");
        }

        this.setMessage(message);
        this.setReadReceipt(readReceipt);
        this.setLastModified(lastModified);

        //return this;
    }*/


}