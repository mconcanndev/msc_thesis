package com.acme.server.model;

import com.acme.server.dao.ChatRoomDAO;
import com.acme.server.service.MessagingService;
import com.acme.server.service.UserService;
import com.acme.server.util.ActionLinks;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 *  The ChatRoom Class is a representation of one ChatRoom instance between twoUsers in the system
 *  TODO: There can only be one ChatRoom associated with any two users & this is a constraint that the constructor enforces
 *
 *  A ChatRoom instance contains:
 *
 *   UUID chatRoomID - a system generated chatRoomID
 *   List<User> - The list of participants in the chat room (Only supports 2 Participants in Phase 1)
 *   String topic - A Chat Room Topic, this is mutable
 *   long lastModified - Server generated timestamp when the ChatRoom was created or modified
 *   List<ChatMessage> - The list of ChatMessages posted to this ChatRoom
 *   List<NextActionLinks> - The allowable list of next actions on the ChatRoom Resource
 */
public class ChatRoom {

    private String chatRoomID;
    private List<User> participants;
    private String topic;
    private List<ChatMessage> chatMessages;
    private long lastModified; //set on creation or modification
    private List<ActionLinks> nextActionLinks;

    private static Logger log = Logger.getLogger(ChatRoom.class);

    public ChatRoom() {
        log.info("Entering ChatRoom()");
    }

    public ChatRoom(String topic, List<User> participants) {
        log.info("Entering ChatRoom: " + topic + " " + participants.toString());
        this.chatRoomID = "CHATROOM:" + UUID.randomUUID().toString(); //immutable
        this.topic = topic; //mutable
        this.participants=participants; //mutable but will only deal with 1:1
    }

    //Overloaded to allow reconstruct redis data when reading an exsting chatroom back out of the data store
    //Typically UUID & Messages will not be set
    public ChatRoom(String chatRoomID, String topic, List<User> participants, List<ChatMessage> messages) {
        log.info("Entering ChatRoom");
        if (chatRoomID !=null) {
            log.info("Setting chatRoom ID to: " + chatRoomID);
            this.chatRoomID = chatRoomID; }
        else{
            log.info("Setting chatRoom ID to random UUID");
            this.chatRoomID = "CHATROOM:" + UUID.randomUUID().toString();}

        this.topic = topic; //mutable
        this.participants=participants; //mutable but will only deal with 1:1
        this.chatMessages = messages;
    }

    public ChatRoom(ChatRoomDAO chatRoomDAO){
        this.setChatRoomID(chatRoomDAO.getChatRoomID());

        this.setTopic(chatRoomDAO.getTopic());
        log.info("Basic Info set");

        //Reconstruct two user objects associated with this ChatRoom using the UserService to retrieve them form the DB
        UserService userService = new UserService();
        MessagingService messagingService = new MessagingService();

        List<User> chatRoomParticipants = new ArrayList<User>();
        User chatRoomCreator = userService.retrieveExistingUser(chatRoomDAO.getChatRoomCreatorUserID());
        User chatRoomParticipant = userService.retrieveExistingUser(chatRoomDAO.getChatRoomParticipantID());
        chatRoomParticipants.add(chatRoomCreator);
        chatRoomParticipants.add(chatRoomParticipant);
        this.setParticipants(chatRoomParticipants);
        log.info("Participants Set");

        //Create a utility method that will search the DB for all messages associated with this chatroom (or last X)
        //This will return a list of ChatRoom Messages that we can assocaite with the ChatRoom to return the full resource
        //representation of the ChatRoom.

        // TODO: THIS IS AN AREA TO DEMONSTRATE WHERE VIOLATING THIS CONSTRAINT CAN CREATE OPTIMISATIONS
        // The first message of the chatRoom may be contained in the JSON body of the ChatRoom Resource but that
        // would violate one of the REST constraints that a POST against a resource should only create instances of
        // that resource.
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        chatMessages = messagingService.retrieveAllChatMessages(chatRoomDAO.getChatRoomID());
        log.info(chatMessages.size() + "Messages Found for chatroom ID: " + chatRoomDAO.getChatRoomID());

        this.setChatMessages(chatMessages);
        log.info("Messages Set: " + chatMessages.size());

    }

    public String getChatRoomID() {
        return chatRoomID;
    }

    public String getTopic() {
        return topic;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public List<ChatMessage> getChatMessages() {return chatMessages;};

    public List<ActionLinks> getNextActionLinks() {return nextActionLinks;};

    public void setChatRoomID(String chatRoomID){
        this.chatRoomID = chatRoomID;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setNextActionLinks(List<ActionLinks> nextActionLinks) {
        this.nextActionLinks = nextActionLinks;
    }
}