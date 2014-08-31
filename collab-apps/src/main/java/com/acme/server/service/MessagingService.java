package com.acme.server.service;

import com.acme.server.dao.ChatMessageDAO;
import com.acme.server.dao.ChatRoomDAO;
import com.acme.server.model.ChatRoom;
import com.acme.server.model.ChatMessage;
import com.acme.server.model.User;
import com.acme.server.util.DatabaseManager;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

/**
 *  The MessagingService is invoked by the Messaging Controller & performs the necessary logic to convert between JSON requests
 *  to the Messaging Controller endpoints & the Database storage, retrieval necessary to return a JSON response containing the
 *  expected Resource Representations e.g. ChatRoom, ChatMessage & User Resources.
 *
 *  This service implements any of the business logic required to satisfy ACME's business constraints, abstracting that
 *  detail from the Controller or DAO layers.
 */
public class MessagingService {

    private DatabaseManager databaseManager;
    public UserService userService = new UserService();
    private static Logger log = Logger.getLogger(MessagingService.class);

    public MessagingService() {
        log.info("Entering MessagingService constructor");
        databaseManager = new DatabaseManager();
    }

    /**
     * Retrieves & Returns the List of all ChatRooms in the system
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions.
     * This would be controlled to be limited by User in a real world scenario (auth info or parameter input)
     *
     * GET /users - GET all ChatRooms in the system
     *
     * @return      <code>List</code>
     */
    public List<ChatRoom> retrieveAllChatRooms(String userID){
        log.info("Entering MessagingService.retrieveAllChatRooms()");

        List<ChatRoom> listOfChatRooms = new ArrayList<ChatRoom>();
        List<ChatRoomDAO> listOfChatRoomDAOs = databaseManager.getAllChatRoomDAOs(userID);
        log.info("Retrieved all ChatRoomDAOs");

        //Construct a list of actual ChatRoom objects for returning from the ChatRoom DAO & finding the users & messages
        //associated with each ChatRoom
        for(int i = 0;i<listOfChatRoomDAOs.size();i++){
            ChatRoomDAO nextChatRoomDAO = listOfChatRoomDAOs.get(i);

            //Set the information directly accessible from the DAO object into the ChatRoom Resource
            ChatRoom nextChatRoom = new ChatRoom();
            nextChatRoom.setChatRoomID(nextChatRoomDAO.getChatRoomID());

            nextChatRoom.setTopic(nextChatRoomDAO.getTopic());
            log.info("Basic Info set");

            //Reconstruct two user objects associated with this ChatRoom using the UserService to retrieve them form the DB
            List<User> chatRoomParticipants = new ArrayList<User>();
            User chatRoomCreator = userService.retrieveExistingUser(nextChatRoomDAO.getChatRoomCreatorUserID());
            User chatRoomParticipant = userService.retrieveExistingUser(nextChatRoomDAO.getChatRoomParticipantID());
            chatRoomParticipants.add(chatRoomCreator);
            chatRoomParticipants.add(chatRoomParticipant);
            nextChatRoom.setParticipants(chatRoomParticipants);
            log.info("Participants Set");

            //Create a utility method that will search the DB for all messages associated with this chatroom (or last X)
            //This will return a list of ChatRoom Messages that we can assocaite with the ChatRoom to return the full resource
            //representation of the ChatRoom.
            List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
            chatMessages = retrieveAllChatMessages(nextChatRoomDAO.getChatRoomID());
            nextChatRoom.setChatMessages(chatMessages);
            log.info("Messages Set");

            listOfChatRooms.add(nextChatRoom);
        }

        return listOfChatRooms;
    }

    /**
     * Retrieves & Returns the ChatRoom with the specified ID
     *
     * GET /users - GET all Users in the system
     *
     * @return      ChatRoom
     */
    public ChatRoom retrieveChatRoom(String chatRoomID){
        log.info("Entering retrieveChatRoom: " + chatRoomID);

        ChatRoomDAO chatRoomDAO = databaseManager.retrieveChatRoomDAO(chatRoomID);
        //chatRoomDAO.retrieve(chatRoomID);

        log.info("Building a ChatRoom Representation from DAO object retrieved for ChatRoom ID " + chatRoomDAO.getChatRoomID().toString());
        return createChatRoomFromDAO(chatRoomDAO);
    }

    /**
     * Creates a new chatroom from the input JSON.
     * NOTE: This will ONLY create a chatroom Resource & will not create User or Message resources associated with the
     * Chatroom. To do so would violate REST contraints as a POST that calls into this is not manipulating the chatroom resource
     * rather a ChatMessage or a User resource
     *
     * GET /users - GET all Users in the system
     *
     * @return      <code>List</code>
     */
    public ChatRoom createChatRoomFromJSON(String json ){
        log.info("Entering createChatRoomFromJSON: " + json);

        //Create empty ChatRoom object in memory to return the JSON representation of the new object to the Controller
        ChatRoom chatRoom = new ChatRoom();
        Gson gsonFromJSON = new Gson();

        //Populate a ChatRoom object directly using data from the JSON representation
        chatRoom = gsonFromJSON.fromJson(json, chatRoom.getClass());
        log.info("ChatRoom object constructed from JSON representation");

        //Create a ChatRoomDAO to translate to something that can be persisted in a KEY / VALUE data store
        ChatRoomDAO chatRoomDAO = new ChatRoomDAO(chatRoom);

        //DAO Object ready to persist to Database
        log.info("Persisting ChatRoomDAO");
        chatRoomDAO.persist();

        //Reconstruct a representation of the  ChatRoom object from the data stored & retrieval of the other resources
        //referenced in the ChatRoom DAO.
        chatRoom.setChatRoomID(chatRoomDAO.getChatRoomID());

        //Retrieve the Users specified in the ChatRoomDAO object & set them into the chatRoom representation
        List<User> chatRoomUsers = new ArrayList<User>();

        User userA = databaseManager.getUser(chatRoomDAO.getChatRoomCreatorUserID().toString());
        User userB = databaseManager.getUser(chatRoomDAO.getChatRoomParticipantID().toString());

        chatRoomUsers.add(userA);
        chatRoomUsers.add(userB);
        chatRoom.setParticipants(chatRoomUsers);

        // THIS IS AN AREA TO DEMONSTRATE WHERE VIOLATING THIS CONSTRAINT CAN CREATE OPTIMISATIONS
        // The first message of the chatRoom may be contained in the JSON body of the ChatRoom Resource but that
        // would violate one of the REST constraints that a POST against a resource should only create instances of
        // that resource.
        return chatRoom;
    }

    //TODO there is this method and a constructor on ChatRoom that accepts DAO.. which to keep?
    private ChatRoom createChatRoomFromDAO(ChatRoomDAO chatRoomDAO){
        log.info("Entering createChatRoomFromDAO");

        ChatRoom chatRoom = new ChatRoom();

        //Reconstruct a representation of the  ChatRoom object from the data stored & retrieval of the other resources
        //referenced in the ChatRoom DAO.
        chatRoom.setChatRoomID(chatRoomDAO.getChatRoomID());
        chatRoom.setTopic(chatRoomDAO.getTopic());

        //Retrieve the Users specified in the ChatRoomDAO object & set them into the chatRoom representation
        List<User> chatRoomUsers = new ArrayList<User>();

        User userA = databaseManager.getUser(chatRoomDAO.getChatRoomCreatorUserID().toString());
        User userB = databaseManager.getUser(chatRoomDAO.getChatRoomParticipantID().toString());

        chatRoomUsers.add(userA);
        chatRoomUsers.add(userB);
        chatRoom.setParticipants(chatRoomUsers);

        // THIS IS AN AREA TO DEMONSTRATE WHERE VIOLATING THIS CONSTRAINT CAN CREATE OPTIMISATIONS
        // The first message of the chatRoom may be contained in the JSON body of the ChatRoom Resource but that
        // would violate one of the REST constraints that a POST against a resource should only create instances of
        // that resource.

        //Get any messages stored in the DB & associated with this ChatRoomDAO & add to the ChatRoom
        List<ChatMessage> chatMessages = retrieveAllChatMessages(chatRoomDAO.getChatRoomID());
        chatRoom.setChatMessages(chatMessages);

        return chatRoom;
    }


    /**
     * Modifies the ChatRoom with the detail specified in the JSON body & stores the modification in the database
     * Only the 'Topic' of a ChatRoom is modifiable after creation in this phase of Acme development so all other fields
     * input in JSON body get ignored.
     *
     * @param       json - Contains the detail of the modification to make
     * @return      ChatRoom - The Representation of the updated Resource as stored in the Database after modification
     */
    public ChatRoom modifyChatRoomFromJSON(String json){
        log.info("Entering modifyUserFromJSON: " + json);

        //Create empty ChatRoom object to populate with the JSON input
        ChatRoom chatRoom = new ChatRoom();
        Gson gsonFromJSON = new Gson();

        //Populate the User object directly using data from the JSON representation
        chatRoom = gsonFromJSON.fromJson(json, chatRoom.getClass());
        log.info("ChatRoom object constructed from JSON representation");

        //Log the User Object as constructed from JSON input
        log.info("ChatRoom ID: " + chatRoom.getChatRoomID());
        log.info("ChatRoom Topic: " + chatRoom.getTopic());

        //Retrieve the existing resource representation from Key/Value store
        //Validate the chatRoomID is valid / populated before doing anything else
        ChatRoomDAO existingChatRoom = databaseManager.retrieveChatRoomDAO(chatRoom.getChatRoomID());

        //Overwrite the current topic with the one from the JSON body in the DAO retrieved from the Key / Value store
        existingChatRoom.setTopic(chatRoom.getTopic());
        log.info("Updated Topic for existingChatRoomID: " + existingChatRoom.getChatRoomID() + " to" + chatRoom.getTopic() );

        //Update the data in the KeyValue Store using the same value
        //Will have the same key as one that already exists
        databaseManager.createChatRoomFromDAO(existingChatRoom);

        //Re-read the data from the key / value store after the update operation so the ChatRoom object built to send back
        //to the consumer is comprised of he actual data stored.
        //NOTE: In real world implementation this is subject to state synchronisation issues (i.e. another user could have
        //modified the same ChatRoom topic before this is invoked or immediately AFTER the response has gone back to the consumer
        //The User will not discover this until they do the next 'GET' operation. Another issue that surfaces prevalently using
        //synchronous REST V some sequence based notification mechanism
        ChatRoomDAO modifiedChatRoomDAO = databaseManager.retrieveChatRoomDAO(chatRoom.getChatRoomID());

        //Reconstruct a modified version of the ChatRoom to send back to the API consumer (including the messages & users associated
        //with the ChatRoom
        return createChatRoomFromDAO(modifiedChatRoomDAO);
    }


    //******************************** CHAT MESSAGE SERVICE LAYER **********************************************

    /**
     * Retrieves & Returns the ChatMessage with the specified ID
     *
     * @param       chatMessageID - ChatMessageID
     * @return      ChatMessage
     */
    public ChatMessage retrieveChatMessage(String chatMessageID){
        log.info("Entering retrieveChatMessage: " + chatMessageID);

        ChatMessageDAO chatMessageDAO = databaseManager.retrieveChatMessageDAO(chatMessageID);
        log.info("FromParticipant: " + chatMessageDAO.getFromParticipantID());

        log.info("Building a ChatMessage Representation from DAO object retrieved for ChatMessage ID " + chatMessageDAO.getChatMessageID().toString());
        return createMessageFromDAO(chatMessageDAO);
    }


    /**
     * Retrieves & Returns the List of all ChatMessages in the system for a given ChatRoom
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions.
     *
     * @param       chatRoomID - The ChatRoom ID for the messages requested
     * @return      <code>List</code> List of ChatMessages for the given ChatRoom
     *
     */
    public List<ChatMessage> retrieveAllChatMessages(String chatRoomID){
        log.info("Entering MessagingService.retrieveAllChatMessages() for ChatRoomID: " + chatRoomID);

        List<ChatMessage> listOfChatMessages = new ArrayList<ChatMessage>();
        List<ChatMessageDAO> listOfChatMessageDAOs = databaseManager.getAllChatMessageDAOs(chatRoomID);
        log.info("Retrieved all ChatMessageDAOs");

        //Construct a list of actual ChatMessage objects for returning from the ChatMessage DAO & finding the users
        //associated with each ChatMessage
        for(int i = 0;i<listOfChatMessageDAOs.size();i++){
            ChatMessageDAO nextChatMessageDAO = listOfChatMessageDAOs.get(i);

            //Set the information directly accessible from the DAO object into the ChatRoom Resource
            ChatMessage nextChatMessage = new ChatMessage();
            nextChatMessage.setChatMessageID(nextChatMessageDAO.getChatMessageID());
            nextChatMessage.setChatRoomID(nextChatMessageDAO.getChatRoomID());
            nextChatMessage.setMessage(nextChatMessageDAO.getMessage());
            nextChatMessage.setTimestamp(nextChatMessageDAO.getLastModified());
            nextChatMessage.setFromParticipantID(nextChatMessageDAO.getFromParticipantID());
            nextChatMessage.setReadReceipt(nextChatMessageDAO.getReadReceipt());

            log.info("Basic Info set");
            listOfChatMessages.add(nextChatMessage);
        }

        return listOfChatMessages;
    }

    /**
     * Creates a new ChatRoom from the input JSON.
     * NOTE: This will ONLY create a ChatRoom Resource & will not create User or Message resources associated with the
     * Chatroom. To do so would violate REST contraints as a POST that calls into this is not manipulating the ChatRoom resource
     * rather a ChatMessage or a User resource
     *
     * GET /users - GET all Users in the system
     *
     * @return      <code>List</code>
     */
    public ChatMessage createChatMessageFromJSON(String json ){
        log.info("Entering createChatMessageFromJSON: " + json);

        //Create empty ChatMessage object in memory to return the JSON representation of the new object to the Controller
        ChatMessage chatMessage = new ChatMessage();
        Gson gsonFromJSON = new Gson();

        //Populate a ChatRoom object directly using data from the JSON representation
        chatMessage = gsonFromJSON.fromJson(json, chatMessage.getClass());
        log.info("ChatMessage object constructed from JSON representation");

        //Create a ChatMessageDAO to translate to something that can be persisted in a KEY / VALUE data store
        ChatMessageDAO chatMessageDAO = new ChatMessageDAO(chatMessage);

        //DAO Object ready to persist to Database
        log.info("Persisting ChatMessageDAO");
        chatMessageDAO.persist();
        log.info("ChatMessage persisted");

        //Reconstruct a representation of the  ChatRoom object from the data stored & retrieval of the other resources
        //referenced in the ChatRoom DAO.
        chatMessage.setChatMessageID(chatMessageDAO.getChatMessageID());
        chatMessage.setChatRoomID(chatMessageDAO.getChatRoomID());
        chatMessage.setMessage(chatMessageDAO.getMessage());
        chatMessage.setTimestamp(chatMessageDAO.getLastModified());
        chatMessage.setReadReceipt(chatMessageDAO.getReadReceipt());

        //TODO Make this a resource representation of a USER object
        chatMessage.setFromParticipantID(chatMessageDAO.getFromParticipantID());
        log.info("From Participant: " + chatMessageDAO.getFromParticipantID());

        return chatMessage;
    }

    public ChatMessage createMessageFromDAO(ChatMessageDAO chatMessageDAO){

        ChatMessage chatMessage = new ChatMessage();

        //Reconstruct a representation of the  ChatMessage object from the data stored & retrieval of the other resources
        //referenced in the ChatMessage DAO.
        //NOTE: In the chat message case we are not currently retuning the User Resource that is referenced as that is already
        //available in the chatroom parent resource
        chatMessage.setChatMessageID(chatMessageDAO.getChatMessageID());
        chatMessage.setChatRoomID(chatMessageDAO.getChatRoomID());
        chatMessage.setMessage(chatMessageDAO.getMessage());
        chatMessage.setReadReceipt(chatMessageDAO.getReadReceipt());
        chatMessage.setFromParticipantID(chatMessageDAO.getFromParticipantID());
        chatMessage.setTimestamp(chatMessageDAO.getLastModified());

        return chatMessage;
    }

    /*public ChatMessage retrieveChatMessage(String chatMessageID){
        log.info("Entering retrieveChatMessage: " + chatMessageID);

        ChatMessageDAO chatMessageDAO = new ChatMessageDAO();
        chatMessageDAO.retrieve(chatMessageID);

        log.info("Building a ChatMessage Representation from DAO object retrieved for ChatMessage ID " + chatMessageDAO.getChatMessageID().toString());

        //TODO: Build this
        //return createChatMessageFromDAO(chatMessageDAO);

        return null;
    }*/

    /**
     * Modifies the ChatMessage with the detail specified in the JSON body & stores the modification in the database
     * Only the 'ReadReceipt' of a ChatMessage is modifiable after creation in this phase of Acme development so all other fields
     * input in JSON body get ignored.
     *
     * @param       json - Contains the detail of the modification to make
     * @return      ChatMessage - The Representation of the updated Resource as stored in the Database after modification
     */
    public ChatMessage modifyChatMessageFromJSON(String json){
        log.info("Entering modifyChatMessageFromJSON: " + json);

        //Create empty ChatMessage object to populate with the JSON input
        ChatMessage chatMessage = new ChatMessage();
        Gson gsonFromJSON = new Gson();

        //Populate the ChatMessage object directly using data from the JSON representation
        chatMessage = gsonFromJSON.fromJson(json, chatMessage.getClass());
        log.info("ChatMessage object constructed from JSON representation");

        //Log the ChatMessage Object as constructed from JSON input
        log.info("ChatMessage ID: " + chatMessage.getChatMessageID());
        log.info("ChatMessage ReadReceipt: " + chatMessage.getReadReceipt());

        //Retrieve the existing resource representation from Key/Value store
        //Validate the ChatMessageID is valid / populated before doing anything else
        ChatMessageDAO existingChatMessage = databaseManager.retrieveChatMessageDAO(chatMessage.getChatMessageID());

        //Overwrite the current topic with the one from the JSON body in the DAO retrieved from the Key / Value store
        existingChatMessage.setReadReceipt(chatMessage.getReadReceipt());
        log.info("Updated ReadReceipt for existingChatMessageID: " + existingChatMessage.getChatMessageID() + " to" + chatMessage.getReadReceipt() );

        //Update the data in the KeyValue Store using the same value
        //Will have the same key as one that already exists
        databaseManager.createChatMessageFromDAO(existingChatMessage);
        log.info("Pushed data to DB");

        //Re-read the data from the key / value store after the update operation so the ChatMessage object built to send back
        //to the consumer is comprised of he actual data stored.
        //NOTE: When support >2 participants in any given chatroom readreceipts would need to be set on a per user basis as
        //users will read messages at different times.
        ChatMessageDAO modifiedChatMessageDAO = databaseManager.retrieveChatMessageDAO(chatMessage.getChatMessageID());

        //Reconstruct a modified version of the ChatMessage to send back to the API consumer (including the messages & users associated
        //with the ChatMessage
        return createMessageFromDAO(modifiedChatMessageDAO);
    }

    /**
     * Creates a number of test messages for a given chatroom ID
     *
     * @param       numTestMessages - The number of test Messages to create //NOTE: need to throttle this in a real world
     * @return      listOfTestUsers - The list of new users created
     */
    public List<ChatMessage> createTestMessages(String chatRoomID, int numTestMessages) {
        log.info("Entering createTestMessage: " + chatRoomID);

        ChatRoom chatRoom = retrieveChatRoom(chatRoomID);
        if(chatRoom != null){
            String fromParticipantID = chatRoom.getParticipants().get(1).getUserID();

            List listOfTestMessages = new ArrayList();
            for (int i = 0; i < numTestMessages; i++) {
                ChatMessageDAO testMessage = new ChatMessageDAO(chatRoomID, fromParticipantID,
                        "Test Messsage " + i + "for chatroom ID: " + chatRoomID, "false");

                //Push the DAO object to the Database
                databaseManager.createChatMessageFromDAO(testMessage);

                //Should retrieve it again so have the DAO version of what got stored
                ChatMessageDAO retrievedDAO = databaseManager.retrieveChatMessageDAO(testMessage.getChatMessageID());
                log.info("ChatMessage ID of message stored: " + retrievedDAO.getChatMessageID());

                //Build a ChatMessage to return from the DAO object created
                ChatMessage nextChatMessage = new ChatMessage(retrievedDAO);
                listOfTestMessages.add(nextChatMessage);
            }

            return listOfTestMessages;
        }
        else{
            log.info("ChatRoom with ID: " + chatRoomID + " does not exist, no TestMessages created");
            return null;
        }
    }



}