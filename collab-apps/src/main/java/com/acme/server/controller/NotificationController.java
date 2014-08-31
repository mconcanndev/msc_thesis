package com.acme.server.controller;

import com.acme.server.model.Notification;
import com.acme.server.service.NotificationService;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 *  The NotificationController is the generic entry point for all ACME & Partner consumers of the Messaging
 *  services seeking to identify deltas between their local state & that on the server. In the absence of an asynchronous
 *  communication channel on which to push notifications to a consuming client, the client will invoke this endpoint at
 *  a sufficiently appropriate interval depending on the criticality of maintaining state in real-time e.g. a mute operation
 *  in some of the unimplemented calling services would be a very time sensitive operation which would reduce the polling interval
 *
 *  This offers a REST centric 'POLLING' mechanism which will serve as the trigger for clients to perform a GET operation
 *  on the Resources tagged by the server as 'modified'.
 *
 *  Supports GET operations on the Notification  Resource which is a generic resource that gives all the relevant information
 *  to the consumer about how to get back to the server to make requests for the data that has been created / modified so it can
 *  be processed according. This will result in several server round trips in the immediate aftermath of the receipt of a non null
 *  Notification resource in response to changes made by other users in the system. e.g. new message, modified read receipt or chatroom
 *  topic.
 *
 *  Those resources include: ChatRoom & ChatMessage. The Resource structure for ChatRoom includes a list of ChatMessages
 *  that are associated with the ChatRoom
 *
 *  The traffic generated from all clients consuming this endpoint puts a significant load on the cloud hosted services & unmonitored
 *  has the potential to significantly increase the volume of resources required - a key constraint of Acme's requirements.
 *  A set of independent endpoints on the existing controllers could be used such as /chatrooms/notifications & /users/notifications
 *  but this would double / triple the load generated by the polling depending on the number of endpoints so creating a top level
 *  NotificationController which observes changes across all Resources is a more pragmatic where polling is necessary to achieve
 *  state synchronisation.
 *
 *  Alternate approaches to polling include some pub / sub mechanism, the use of sockets etc.
 *
 */
@RestController
public class NotificationController {

    private static Logger log = Logger.getLogger(NotificationController.class);
    private static NotificationService notificationService= new NotificationService();

    /**
     * Returns a list of <code>Notification</code> resources which provide the detail of what resources have been
     * modified since the client last polled
     *
     * NOTE: The team at Acme have yet to build the correct implementation behind this endpoint that will accept a timestamp
     * as an input parameter & check what is in fact new.
     * It is used here in iteration 1 development only to illustrate the impact of a polling based design & will periodically
     * return a set of newly created messages to the API consumer to process as they would if a remote user posted new
     * messages to the chatroom.
     *
     * This is used by consumers to identify changes across all Collaboration resources (ChatRoom & Messaging)
     * Accessible via a GET on /notifications
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions
     *
     * GET  /chatroom/{id} - get the chatroom that has been modified
     * GET  /chatroom/{id}/messages - get the latest set of messages for a given modified chatroom
     * GET  /chatroom/{id}/message/{id} - get the specific message that has been modified
     *
     * @param       testNotifications - provided as an input in the absense of a proper polling mechanism that uses timestamps
     *                                or sequence numbers to find & return any newly created or modified
     *
     *
     * @return      <code> List </code>
     */

    @RequestMapping(value = "/notifications", method = RequestMethod.GET)
    public List<Notification> getNotifications(@RequestParam(value="chatroomID", required=false) String chatRoomID,
                                               @RequestParam(value="test", required=false, defaultValue="false") String testNotifications) {
        log.info("Entering GET /notifications?chatroomID=" + chatRoomID);

        //Call out to notificationService to simulate a scenario where there have been a series of new chatMessages created
        //by the other party in a chat room & the receiver needs to process them in their client.
        if (testNotifications.equals("true")){
            log.info("Request to create test notifications for real world simulation" + chatRoomID);
            return notificationService.createTestChatMessageNotifications(chatRoomID,1);
        }
        else {
         //   log.info("Entering GET /notifications?chatroomID=" + chatRoomID);
            //Do something else that will generally not return new notifications
            long clientTimeStamp = new Date().getTime();
            return notificationService.checkForNewEvents(clientTimeStamp);
        }
    }

}