package com.acme.server.model;

import com.acme.server.service.MessagingService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.List;



/* Check of all of these should be included or just the straight forward GET of the resource modified

* GET  /chatroom/{id} - get the chatroom that has been modified
* GET  /chatroom/{id}/messages - get the latest set of messages for a given modified chatroom
* GET  /chatroom/{id}/message/{id} - get the specific message that has been modified
*
* */
public class Notification {

    private static Logger log = Logger.getLogger(Notification.class);
    private long timestamp;
    private String parentResourceIDModified;
    private String subResourceIDModified;
    private List<String> links;

    public Notification(String notificationType, String parentresourceID, String subresourceID) {
        log.info("Entering Notification Constructor: " + notificationType + " " + parentresourceID + " " );
        links = new ArrayList<String>();
        String baseURL = "http://localhost:8080"; //Move to be read  from  config file.

        if(notificationType.equals("CHATROOM")){
            log.info("Found New ChatRoom Notification: ");
            String chatRoomURL = baseURL + "/chatrooms/" + parentresourceID;
            this.parentResourceIDModified = parentresourceID;
            links.add(chatRoomURL);
        }
        else if(notificationType.equals("CHATMESSAGE")){
            log.info("Found New Message Notification: ");
            String chatMessageURL = baseURL + "/chatrooms/" + parentresourceID + "/chatmessages/" + subresourceID;
            this.parentResourceIDModified = parentresourceID;
            this.subResourceIDModified = subresourceID;
            links.add(chatMessageURL);
        }
        else if(notificationType.equals("USER")){
            log.info("Found New User Notification: ");
            String userURL = baseURL + "/users/" + parentresourceID;
            this.parentResourceIDModified = parentresourceID;
            links.add(userURL);
        }

        this.timestamp = new Date().getTime();
    }

    public long getTimestamp(){return timestamp;}

    public List<String> getLinks(){
        return links;
    }

    public String getParentResourceIDModified() {
        return parentResourceIDModified;
    }

    public String getSubResourceIDModified() {
        return subResourceIDModified;
    }

    public void setParentResourceIDModified(String parentResourceIDModified) {
        this.parentResourceIDModified = parentResourceIDModified;
    }

    public void setSubResourceIDModified(String subResourceIDModified) {
        this.subResourceIDModified = subResourceIDModified;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public void setLinks(List<String> modifiedResourceLinks){
        this.links = modifiedResourceLinks;
    };

}