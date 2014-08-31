package com.acme.server.service;

import com.acme.server.dao.ChatMessageDAO;
import com.acme.server.model.Notification;
import com.acme.server.model.ChatMessage;
import com.acme.server.model.ChatRoom;
import com.acme.server.model.User;
import com.acme.server.util.DatabaseManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

/**
 *  The NotificationService is invoked by the NotificationController & performs the necessary logic to check if there
 *  have been any new or modified resources in the database since last poll was performed via a basic algorithm of searching
 *  the DB for all created / modified timestamps since the last poll was performed (the GET /notifications endpoint is
 *  parameterised to accept the last server generated polling timestamp sent to the client.
 *
 *  NOTE: A real world polling implementation would likely be build on a more advance algorithm that would take race conditions
 *  & state consistency into account using a formal message queue such as RabbitMQ etc. There is some margin for error here.
 *
 *  It is responsible for searching the DB for new 'notification' & constructing Notification Objects to send back to the
 *  client that is polling which include links to the resources modified so that the consumers can subsequently perform
 *  a GET operation on the modified resource for further manipulation.
 *
 *  NOTE: This is done at a Resource level i.e. if a Message associated with a ChatRoom is modified, only a URL to the message
 *  modified is provided.
 */

public class NotificationService {

    private DatabaseManager databaseManager;
    private MessagingService messagingService = new MessagingService();
    private static Logger log = Logger.getLogger(MessagingService.class);

    public NotificationService() {
        databaseManager = new DatabaseManager();
    }

    //Perform a DB Lookup to see what changes have been made since the last time this client polled for
    //new events. Returns null if no changes since last poll. If a Notification is found & returned to the client,
    //the next operation by a client is a 'GET' to identify what exactly has changed & process that subset of data.
    public List<Notification> checkForNewEvents(long lastTimeStamp){
        log.info("Entering checkForNewEvents");

        List<Notification> allNewNotifications = new ArrayList<Notification>();

        //TODO Do a Look up in Database for ALL New or Modified ChatRoom, ChatMessage or User Resources since
        //some timestamp or sequence number specified by the last poll. Build a list of Notifications based on what
        //gets returned & send this back to the API consumer for processing.


        return allNewNotifications;
    }



    /**
     * Returns a set of Test Notification objects that pertain to a given ChatRoomID
     * Accessible via a GET on /notifications?chatroomID=id&test=true
     * Add some @parameters to filter by User at a later stage
     *
     * The ChatRoom resource is the container resource for the ACME Collaboration Services.
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions
     *
     * GET /chatrooms/{id}/chatmessages{id} - GET a specific chatmessage resource for further manipulation
     *                                        Typical client side processing would include rendering & setting read
     *                                        receipts.
     *
     * @param       chatRoomID The chatroom ID that we are interested in new notifications for
     * @param       numNotifications The nummber of new Notification resources that will be automatically created to send back to
     *                               the API consumer
     * @return      <code>List</code>
     */
    public List<Notification> createTestChatMessageNotifications(String chatRoomID, int numNotifications) {
        log.info("Entering createTestChatMessageNotifications" + chatRoomID + toString());

        ChatRoom testChatroom = messagingService.retrieveChatRoom(chatRoomID);
        if (testChatroom != null) {
            log.info("Valid ChatRoom ID - continue processing");
            List<User> users = testChatroom.getParticipants();

            //Identify the remote participant
            String fromParticipantID = users.get(0).getUserID();

            List<Notification> listOfTestNotifications = new ArrayList();
            for (int i = 0; i < numNotifications; i++) {

                log.info("Creating new Chat Message" + chatRoomID + toString());

                //Create a new ChatMessage DAO to push the new message to the database
                ChatMessageDAO testChatMessageDAO = new ChatMessageDAO(chatRoomID, fromParticipantID, "Test Message " + i
                        + "for chatroom id: " + chatRoomID, "false");

                //persist the new test ChatMessage to the database
                testChatMessageDAO.persist();

                ChatMessageDAO retrievedDAO = databaseManager.retrieveChatMessageDAO(testChatMessageDAO.getChatMessageID());
                log.info("ChatMessage ID of message stored: " + retrievedDAO.getChatMessageID());

                //Reconstruct the message to send back
                // messagingService.createMessageFromDAO(testChatMessageDAO);

                log.info("Creating a Message Notification");
                Notification testChatMessageNotification = new Notification("CHATMESSAGE", chatRoomID, retrievedDAO.getChatMessageID());
                listOfTestNotifications.add(testChatMessageNotification);

            }
            return listOfTestNotifications;
        } else {
            log.info("Invalid ChatRoom ID - returning null");
            return null;
        }

    }

}