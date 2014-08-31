package com.acme.server.model;

import com.acme.server.dao.ChatMessageDAO;
import com.acme.server.util.ActionLinks;
import org.apache.log4j.Logger;

import javax.lang.model.element.NestingKind;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *  The ChatMessage Class is a representation of one message that is posted from one participant in a specific ChatRoom.
 *  Used in the MessagingController & MessagingService Layers to perform necessary necessary translation between the consumer
 *  facing API layer & the DAO layer for persistent storage
 *
 *  A Message instance contains:
 *
 *   UUID chatMessageID - ChatMessage resource identifier, used on PUTs to set read receipt
 *   UUID chatRoomID - Every ChatMessage has an associated ChatRoom UUID
 *   UUID fromParticipantID - The participant that posted the message in the ChatRoom
 *   String message - Actual message posted (requires encryption over HTTPS or other)
 *   long timestamp - Server generated timestamp of when the message was last modified
 *   boolean readReceipt - Flag indicating whether the other Participant in the chatroom read the message (Assumes 1:1)
 *   List<NextActionLinks> - The allowable list of next actions on the ChatRoom Resource
 */

public class ChatMessage {

    private String chatMessageID;       //ChatMessage resource identifier, used on PUTs to set read receipt
    private String chatRoomID;          //Every message has an associated ChatRoom
    private String fromParticipantID;   //The participant that posted the message - Any value in this being an actual Object?
    private String message;           //Actual message, needs encryption
    private long timestamp;           //Server generated timestamp of when the message was last modified (created or updated)
    private String readReceipt;      //Flag indicating whether the other Participant in the chatroom read the message (Assumes 1:1)
    private List<ActionLinks> nextActionLinks;

    private static Logger log = Logger.getLogger(ChatRoom.class);

    public ChatMessage(){
        log.info("Entering ChatMessage()");
    }

    public ChatMessage(String chatRoomID, String fromParticipantID, String message) {
        this.chatMessageID = "MESSAGE:" + chatRoomID + ":" + UUID.randomUUID().toString();
        this.chatRoomID = chatRoomID;
        this.fromParticipantID=fromParticipantID; //No value in this being a resource representation, also available on ChatRoom
        this.message = message;
        //this.timestamp = new Date().getTime();
        this.readReceipt = "false"; //when first created the message cannot by definition have been read yet by the other party
    }

    //Used when reconstructing a ChatMessage for return to API consumer
    public ChatMessage(ChatMessageDAO chatMessageDAO){

        this.chatMessageID = chatMessageDAO.getChatMessageID();
        this.chatRoomID = chatMessageDAO.getChatRoomID();
        this.message = chatMessageDAO.getMessage();
       // this.timestamp = chatMessageDAO.getLastModified();
        this.readReceipt = chatMessageDAO.getReadReceipt();

        //TODO make this a resource representation of a User object to justify DAO layer in majority case
        this.fromParticipantID = chatMessageDAO.getFromParticipantID();

    }

    public String getChatMessageID() {
        return chatMessageID;
    }

    public String getChatRoomID() {
        return chatRoomID;
    }

    public String getFromParticipantID() {
        return fromParticipantID;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getReadReceipt() { return readReceipt;}

    public List<ActionLinks> getNextActionLinks() {
        return nextActionLinks;
    }

    public void setChatMessageID(String chatMessageID) {
        this.chatMessageID = chatMessageID;
    }

    public void setChatRoomID(String chatRoomID) {
        this.chatRoomID = chatRoomID;
    }

    public void setFromParticipantID(String fromParticipantID)
    {
        this.fromParticipantID = fromParticipantID;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public void setReadReceipt(String readReceipt){
        this.readReceipt = readReceipt;
    }

    public void setNextActionLinks(List<ActionLinks> nextActionLinks) {
        this.nextActionLinks = nextActionLinks;
    }
}