package com.acme.server.controller;

import com.acme.server.model.ChatRoom;
import com.acme.server.model.ChatMessage;
import com.acme.server.service.MessagingService;
import com.acme.server.util.ActionLinks;
import com.acme.server.util.StringUtils;

import org.springframework.web.bind.annotation.*;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  The MessagingController is the entry point for all Acme Collaboration client, partner & 3rd party
 *  REST requests for Messaging related Services
 *
 *  Supports GET / PUT / POST on all Resources that make up the Messaging Service.
 *
 *  Those resources include: ChatRoom & ChatMessage. The Resource structure for ChatRoom includes a list of ChatMessages
 *  that are associated with the ChatRoom
 *
 *  The MessagingController converts the InputStreams from clients into a String that contains the JSON body.
 *  Any parameters supported by the Endpoint are also handled by the controller & passed along to the Service layer accordingly.
 *  The actual business logic & processing is handed off to the MessagingService layer & when complete, the controller
 *  does the work to set the next set of valid ActionLinks on the Resource representation to ensure the API conforms fully
 *  to HATEOAS.
 */
@RestController
public class MessagingController {

    private static Logger log = Logger.getLogger(MessagingController.class);
    private MessagingService messagingService = new MessagingService();

    /**
     * Returns the full list of ChatRoom resources in the database
     * Accessible via a GET on /chatrooms
     *
     * The ChatRoom resource is the container resource for the ACME Collaboration Services.
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions
     *
     * POST /chatrooms - create a new ChatRoom resource for further manipulation
     * GET /chatrooms/{id} - GET a specific ChatRoom resource for further manipulation
     *
     * @param       userID To filter ChatRooms by UserID there is a userID parameter provided. Something that the Hyperlinks alone
     *                     do not make obvious (i.e. documentation of aspects of a REST API is in fact necessary)
     *
     *                     NOTE: There are no Key's in the datastore that can quickly retrieve the list of ChatRooms for a user so
     *                     for a very large datastore it is not recommended that this endpoint is used until a future iteration of
     *                     Acme development.
     *
     * @return      <code>List</code>
     */
    @RequestMapping(value = "/chatrooms",method = RequestMethod.GET)
    public List<ChatRoom> listAllchatRooms(@RequestParam(value="userid", required=false) String userID){
        log.info("Entering GET /chatrooms with userID parameter " + userID);

        List<ChatRoom> listOfChatRooms =  messagingService.retrieveAllChatRooms(userID);
        log.info(listOfChatRooms.size() + " of ChatRooms retrieved ");

        //Construct the next set of allowable actions to send back to the API Consumer to guarantee they can navigate the API
        //via a set of Hypertext links as outlined in Fielding's REST constraints
        for(int i = 0;i<listOfChatRooms.size();i++){
            ChatRoom nextChatRoom = listOfChatRooms.get(i);

            List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
            ActionLinks nextActionA = new ActionLinks("/chatrooms/", "self", "POST");
            ActionLinks nextActionB = new ActionLinks("/chatrooms/" + nextChatRoom.getChatRoomID(), "self", "GET");
            listOfActionLinks.add(nextActionA);
            listOfActionLinks.add(nextActionB);

            nextChatRoom.setNextActionLinks(listOfActionLinks);
        }

        return listOfChatRooms;
    }


    /**
     * Returns the ChatRoom Resource Representation with the id specified in the URL
     * Accessible via a GET on /chatrooms/{id}
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS
     *
     * POST /chatrooms/{id}/message - post a message to the chatroom
     * PUT  /chatrooms/{id} - modify the chatroom (topic)
     * GET  /chatrooms - get all the chatrooms in the system
     *
     * @return      <code> ChatRoom </code>
     */

    @RequestMapping(value = "/chatrooms/{id}",method = RequestMethod.GET)
    public ChatRoom chatRoom(@PathVariable("id") String id) {
        log.info("Entering GET /chatrooms/" + id);

        //Call out to the MessagingService that will in turn, leverage the ChatRoomDAO to retrieve the Resource
        //Representation from the database
        ChatRoom chatRoom = messagingService.retrieveChatRoom(id);

        //Construct the next set of allowable actions to send back to the API Consumer to guarantee they can navigate the API
        //via a set of Hypertext links as outlined in Fielding's REST constraints
        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
        ActionLinks actionA = new ActionLinks("/chatrooms/"+chatRoom.getChatRoomID(),"self","GET" );
        ActionLinks actionB = new ActionLinks("/chatrooms/"+chatRoom.getChatRoomID(),"self","PUT" );
        ActionLinks actionC = new ActionLinks("/chatrooms","create","POST" );
        listOfActionLinks.add(actionA);
        listOfActionLinks.add(actionB);
        listOfActionLinks.add(actionC);
        chatRoom.setNextActionLinks(listOfActionLinks);

        return chatRoom;

    }


    /**
     * Creates a new ChatRoom resource from the representation provided in the JSON body & stores in the Database
     * Accessible via a POST on /chatrooms/{id}
     *
     * NOTE: The system will set the chatRoomID & lastModified fields that get set in the JSON Body of the HTTP response
     * so any values provided in those fields are ignored.
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS
     *
     * GET  /chatrooms/{id} - get the ChatRoom created using the newly created identifier returned in the ChatRoom resource representation
     * PUT  /chatrooms/{id} - modify the newly created ChatRoom (topic or add participants etc)
     * POST /chatrooms/{id}/message - post a message to the newly created ChatRoom
     * POST /chatrooms - create another new ChatRoom
     * GET  /chatrooms - get all the ChatRooms in the system (by user adding the userID parameter to the link provided)
     *
     * @return      <code> ChatRoom </code>
     */
    @RequestMapping(value = "/chatrooms", method = RequestMethod.POST)
    public ChatRoom postNewChatRoom(InputStream data) {
        log.info("Entering POST /chatrooms");

        //Extract incoming json to build a ChatRoom Object from
        String json = StringUtils.InputStringToString(data);
        log.info("POST data: " + json);

        //Call out to the MessagingService Class to build & store the new ChatRoom in the database after applying all
        //relevant business constraints on what the consumer is allowed to manipulate
        ChatRoom chatRoom = messagingService.createChatRoomFromJSON(json);

        //Construct the next set of allowable actions to send back to the API Consumer to guarantee they can navigate the API
        //via a set of Hypertext links as outlined in Fielding's REST constraints
        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
        ActionLinks actionA = new ActionLinks("/chatrooms/"+ chatRoom.getChatRoomID(),"self","GET" );
        ActionLinks actionB = new ActionLinks("/chatrooms/"+ chatRoom.getChatRoomID(),"self","PUT" );
        ActionLinks actionC = new ActionLinks("/chatrooms/"+ chatRoom.getChatRoomID() +"/chatmessages","postmessage","POST" );
        ActionLinks actionD = new ActionLinks("/chatrooms","create","POST" );
        ActionLinks actionE = new ActionLinks("/chatrooms","getall","GET" );
        listOfActionLinks.add(actionA);
        listOfActionLinks.add(actionB);
        listOfActionLinks.add(actionC);
        listOfActionLinks.add(actionD);
        listOfActionLinks.add(actionE);
        chatRoom.setNextActionLinks(listOfActionLinks);

        return chatRoom;
    }


    /**
     * Modifies an existing ChatRoom resource from the representation provided in the JSON body & stores in the Database
     * Accessible via a PUT on /chatrooms/{id}
     *
     * Mutable data on the ChatRoom Resource is confined to Topic for this first phase of ACME development (adding additional
     * Users to the list of ChatRoom participants is another use case that should be supported in a later phase of ACME development).
     * Any other fields populated in the JSON body input will be ignored
     *
     * NOTE: This is something that needs to be documented - the consumer cannot obviously infer via hyperlinks what JSON
     * fields are modifiable when conforming to HATEOAS
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS
     *
     * GET /chatroom - get all the chatrooms in the system
     * GET  /chatroom/{id} - get the chatroom created using the newly created identifier returned in the ChatRoom resource representation
     * POST /chatroom/{id}/chatmessages - post a message to the newly created chatroom
     * PUT  /chatroom/{id} - modify the newly created chatroom (topic or add participants etc)
     *
     * @return      <code> ChatRoom </code>
     */
    @RequestMapping(value = "/chatrooms/{id}", method = RequestMethod.PUT)
    public ChatRoom modifyExistingChatRoom(InputStream data, @PathVariable("id") String id){
        log.info("Entering PUT /chatrooms/{id}" + id);

        //Extract incoming json to build a ChatRoom Object from
        String json = StringUtils.InputStringToString(data);
        log.info("PUT data: " + json);

        ChatRoom modifiedChatRoom = messagingService.modifyChatRoomFromJSON(json);

        //Construct the next set of allowable actions to send back to the API Consumer to guarantee they can navigate the API
        //via a set of Hypertext links as outlined in Fielding's REST constraints
        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
        ActionLinks actionA = new ActionLinks("/chatrooms/"+ modifiedChatRoom.getChatRoomID(),"self","GET" );
        ActionLinks actionB = new ActionLinks("/chatrooms/"+ modifiedChatRoom.getChatRoomID(),"self","PUT" );
        ActionLinks actionC = new ActionLinks("/chatrooms/"+ modifiedChatRoom.getChatRoomID() +"/chatmessages","postmessage","POST" );
        ActionLinks actionD = new ActionLinks("/chatrooms","create","POST" );
        ActionLinks actionE = new ActionLinks("/chatrooms","getall","GET" );
        listOfActionLinks.add(actionA);
        listOfActionLinks.add(actionB);
        listOfActionLinks.add(actionC);
        listOfActionLinks.add(actionD);
        listOfActionLinks.add(actionE);
        modifiedChatRoom.setNextActionLinks(listOfActionLinks);

        return modifiedChatRoom;
    }


    /**
     * Returns the full list of ChatMessage resources in the database for the ChatRoom ID specified in the URL
     * In a real world implementation this needs throttling & paging applied so as not to overload the system i.e.
     * this endpoint would also need to be parameterized to ensure that only the last X number of new ChatMessages are
     * returned & that would need to be documented here for any API consumer to become aware of this type of detail.
     *
     * Accessible via a GET on /chatrooms/{id}/chatmessages
     *
     * The ChatMessage resource contains the messages that are sent back & forth between two Users in a ChatRoom.
     * In a real world implementation such messages need to be fully encrypted so as not to expose sensitive information
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions
     *
     * GET /chatrooms/{id}/chatmessages/{id} - GET the individual ChatMessage resource
     * PUT /chatrooms/{id}/chatmessages/{id} - Modify the ChatMessage to set a read receipt
     * POST /chatrooms/{id}/chatmessages - POST another new chatmessage to the chatroom
     *
     * @return      <code>List</code>
     */

    @RequestMapping(value = "/chatrooms/{chatroomid}/chatmessages",method = RequestMethod.GET)
    public List<ChatMessage> chatMessagesforRoom(@PathVariable("chatroomid") String chatroomid) {
        log.info("Entering GET /chatrooms/" + chatroomid + "/chatmessages");

        List<ChatMessage> listOfChatMessages = messagingService.retrieveAllChatMessages(chatroomid);
        log.info("List of ChatMessages retrieved " + listOfChatMessages.size());

        //Set the action list for each ChatMessage found at the controller level so can base the allowable actions on the
        //endpoint being invoked (Map to JavaDoc comments)
        for(int i = 0;i<listOfChatMessages.size();i++){
            log.info("For Loop " + i);
            ChatMessage nextChatMessage = listOfChatMessages.get(i);

            List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
            ActionLinks nextActionA = new ActionLinks("/chatrooms/"+ chatroomid + "/chatmessages/" + nextChatMessage.getChatMessageID(),"self","GET" );
            ActionLinks nextActionB = new ActionLinks("/chatrooms/"+ chatroomid + "/chatmessages/" + nextChatMessage.getChatMessageID(),"self","PUT" );
            ActionLinks nextActionC = new ActionLinks("/chatrooms/"+ chatroomid + "/chatmessages","postmessage","POST" );
            listOfActionLinks.add(nextActionA);
            listOfActionLinks.add(nextActionB);
            listOfActionLinks.add(nextActionC);

            nextChatMessage.setNextActionLinks(listOfActionLinks);
        }

        return listOfChatMessages;
    }



    /**
     * Returns the ChatMessage resource representation with the specific chatMessageID in the URL
     * Accessible via a GET on /chatrooms/{id}/chatmessages/{id}
     *
     * The ChatMessage resource contains one message that was sent back & forth between two Users in a ChatRoom.
     * In a real world implementation such messages need to be fully encrypted so as not to expose sensitive information
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions
     * NOTE: ChatMessages are IMMUTABLE for compliance purposes so no further modification of a chat message is possible
     *
     * PUT  /chatroom/{id}/chatmessages/{id} - modify the message resource to (set read receipt)
     * POST /chatroom/{id}/chatmessages - post a message to the chatroom
     * PUT  /chatroom/{id} - modify the chatroom (topic)
     *
     * @return      <code> ChatMessage </code>
     */
    //http://localhost:8080/chatroom/{id}/chatmessage{id} */
    @RequestMapping(value = "/chatrooms/{chatroomID}/chatmessages/{messageID}",method = RequestMethod.GET)
    public ChatMessage chatMessage(@PathVariable("chatroomID") String chatroomID,@PathVariable("messageID") String messageID) {
        log.info("Entering GET /chatroom/" + chatroomID + "/chatmessages" + messageID);

        ChatMessage chatMessage = messagingService.retrieveChatMessage(messageID);

        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
        ActionLinks nextActionA = new ActionLinks("/chatrooms/"+ chatroomID + "/chatmessages/" + chatMessage.getChatMessageID(),"self","PUT");
        ActionLinks nextActionB = new ActionLinks("/chatrooms/"+ chatroomID + "/chatmessages","postmessage","POST");
        ActionLinks nextActionC = new ActionLinks("/chatrooms/"+ chatroomID,"modifychatroom","PUT");
        listOfActionLinks.add(nextActionA);
        listOfActionLinks.add(nextActionB);
        listOfActionLinks.add(nextActionC);
        chatMessage.setNextActionLinks(listOfActionLinks);

        return chatMessage;
    }

    /**
     * Creates a new ChatMessage resource from the representation provided in the JSON body & stores in Database
     * The ChatMessage resource gets associated with the ChatRoom id provided in the URL
     * Accessible via a POST on /chatrooms/{id}/chatmessages
     *
     * NOTE: The system will set the chatMessageID, lastModified & default value of 'false' in the read receipt field
     * fields that get set in the JSON Body of the HTTP response so any values provided in those fields are ignored.
     *
     * NOTE: This only supports creation of a single ChatMessages at a time which is in keeping with REST guidelines.
     * Batch processing is a more performant option but introduces complexity on dealing with failures & requires so async
     * mechanism to inform clients when a job is completed in domains where the processing is time intensive.
     *
     * For the purposes of the research however, the endpoint is overloaded to supports the creation of a specified number of
     * test messages for a given ChatRoom which is documented in the parameter settings below. The JSON body for each of these is
     * not returned as do so would imply support on a POST of batch operations which inherently reaches a threshold where synchronous
     * request / responses are not feasible. This is not something that would be overloaded in a real world example so should not
     * be considered a best practice.
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS
     *
     * GET  /chatroom/{id}/chatmessages/{id} - perform a GET on the newly created Unique message ID returned in the JSON Body
     * PUT  /chatroom/{id}/chatmessages/{id} - modify the newly created message resource (set read receipt)
     * POST /chatroom/{id}/chatmessages - POST another message to the chatroom
     *
     * @param       "test"=true/false - if set, will create a list of test users without any JSON body
     * @param       "num"=int - if set AND if "test" is true, will be used to determine how many test users to create
     * @return      <code> User </code>
     */

    @RequestMapping(value = "/chatrooms/{chatroomID}/chatmessages",method = RequestMethod.POST)
    public ChatMessage chatMessage(@PathVariable("chatroomID") String chatroomID, InputStream data,
                                   @RequestParam(value="test", required=false, defaultValue = "false") String testMessages,
                                   @RequestParam(value="num", required=false, defaultValue="2") Integer numTestMessages) {
        log.info("Entering POST /chatroom/" + chatroomID + "/chatmessages");

        if(testMessages.equals("true")){
            log.info("Test Parameter set to true. Creating " + numTestMessages + " new Chat Messages for Chat Messages " +
                    "for Chat Room ID: " + chatroomID);

            List<ChatMessage> listOfChatMessagesCreated = new ArrayList<ChatMessage>();
            listOfChatMessagesCreated = messagingService.createTestMessages(chatroomID, numTestMessages.intValue());

            return null;
        }
        else {

            //Extract incoming json to build a ChatRoom Object from
            String json = StringUtils.InputStringToString(data);
            log.info("POST data: " + json);

            ChatMessage newChatMessage = messagingService.createChatMessageFromJSON(json);

            List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
            ActionLinks nextActionA = new ActionLinks("/chatrooms/"+ chatroomID + "/chatmessages" + newChatMessage.getChatMessageID(),"self","GET" );
            ActionLinks nextActionB = new ActionLinks("/chatrooms/"+ chatroomID + "/chatmessages" + newChatMessage.getChatMessageID(),"self","PUT" );
            ActionLinks nextActionC = new ActionLinks("/chatrooms/"+ chatroomID + "/chatmessages","self","POST");
            listOfActionLinks.add(nextActionA);
            listOfActionLinks.add(nextActionB);
            listOfActionLinks.add(nextActionC);
            newChatMessage.setNextActionLinks(listOfActionLinks);

            //Call out to the MessagingService Class to build & store the new ChatRoom in the database after applying all
            //relevant business constraints on what the consumer is allowed to manipulate
            return newChatMessage;
        }
    }

    /**
     * Modifies an existing ChatMessage resource from the representation provided in the JSON body & stores in the Database
     * Accessible via a PUT on /chatrooms/{id}/chatmessages/{id}
     *
     * Mutable data on the ChatMessage Resource is confined to ReadReceipt.
     * It is also not possible to 'unread' a message so any subsequent attempts to modify the ChatMessage in question is
     * idempotent.
     *
     * NOTE: This is something that needs to be documented - the consumer cannot obviously infer via hyperlinks what JSON
     * fields are modifiable when conforming to HATEOAS
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS.
     *
     * GET  /chatrooms/{id}/chatmessages/{id} - perform a GET on the newly modified Unique message ID returned in the JSON Body
     * POST /chatrooms/{id}/chatmessages - post a new message to the ChatRoom associated with the ChatMessage
     * GET  /chatrooms/{id} - get the ChatRoom created using the newly created identifier returned in the ChatRoom resource representation
     * PUT  /chatrooms/{id} - modify the ChatRoom associated with the ChatMessage
     * POST /chatrooms/ - post another message to the ChatRoom
     * GET  /chatrooms - get all the ChatRooms in the system
     *
     * @return      <code> ChatMessage </code>
     */
    @RequestMapping(value = "/chatrooms/{chatroomID}/chatmessages/{messageID}", method = RequestMethod.PUT)
    public ChatMessage modifyExistingChatMessage(InputStream data, @PathVariable("chatroomID") String chatroomID,
                                              @PathVariable("messageID") String messageID){
        log.info("Entering PUT /chatrooms/" + chatroomID + "/chatmessages/" + messageID);

        //Extract incoming json to build a ChatRoom Object from
        String json = StringUtils.InputStringToString(data);
        log.info("PUT data: " + json);

        ChatMessage modifiedChatMessage = messagingService.modifyChatMessageFromJSON(json);

        //Construct the next set of allowable actions to send back to the API Consumer to guarantee they can navigate the API
        //via a set of Hypertext links as outlined in Fielding's REST constraints
        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();

        ActionLinks actionA = new ActionLinks("/chatrooms/"+ chatroomID + "/" + messageID,"self","GET");
        ActionLinks actionB = new ActionLinks("/chatrooms/"+ chatroomID +"/chatmessages","postmessage","POST" );
        ActionLinks actionC = new ActionLinks("/chatrooms/"+ chatroomID,"retrievechatroom","GET");
        ActionLinks actionD = new ActionLinks("/chatrooms/"+ chatroomID,"modifychatroom","PUT" );
        ActionLinks actionE = new ActionLinks("/chatrooms","create","POST" );
        ActionLinks actionF = new ActionLinks("/chatrooms","getall","GET" );

        listOfActionLinks.add(actionA);
        listOfActionLinks.add(actionB);
        listOfActionLinks.add(actionC);
        listOfActionLinks.add(actionD);
        listOfActionLinks.add(actionE);
        listOfActionLinks.add(actionF);

        modifiedChatMessage.setNextActionLinks(listOfActionLinks);

        return modifiedChatMessage;
    }



    /**
     * Modified version of the initially REST Compliant POST on /chatrooms.
     * The URI is slightly modified so both services can co-exist on the Messaging Controller endpoint for ease of performance
     * analysis. In a real world only one of the models would be adopted so the endpoint will be modified to say 'v2'
     *
     * Supports any 'Create / Modify' request on the ChatRoom resource from the representation provided in the JSON body
     * as well as any child resources associated with the ChatRoom, namingly ChatMessage & User Resources
     *
     * Accessible via a POST on /v2/chatrooms/{id}
     *
     * NOTE: The system will still set the immutable data across ChatRoom, ChatMessage & User resources so any of these values
     * will continue to be ignored in the JSON body.
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS
     *
     * GET  /chatrooms/{id} - get the ChatRoom created using the newly created identifier returned in the ChatRoom resource representation
     * PUT  /chatrooms/{id} - modify the newly created ChatRoom (topic or add participants etc)
     * POST /chatrooms/{id}/message - post a message to the newly created ChatRoom
     * POST /chatrooms - create another new ChatRoom
     * GET  /chatrooms - get all the ChatRooms in the system (by user adding the userID parameter to the link provided)
     *
     * @return      <code> ChatRoom </code>
     */
    @RequestMapping(value = "/v2/chatrooms", method = RequestMethod.POST)
    public ChatRoom postNewChatRoomv2(InputStream data) {
        log.info("Entering POST /v2/chatrooms");

        //Extract incoming json to build a ChatRoom Object from
        String json = StringUtils.InputStringToString(data);
        log.info("POST data: " + json);

        //Call out to the MessagingService Class to build & store the new ChatRoom & child resources in the database
        // after applying all relevant business constraints on what the consumer is allowed to manipulate
        ChatRoom chatRoom = messagingService.createModifiedChatRoomFromJSON(json);

        //Construct the next set of allowable actions to send back to the API Consumer to guarantee they can navigate the API
        //via a set of Hypertext links as outlined in Fielding's REST constraints
        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
        ActionLinks actionA = new ActionLinks("/chatrooms/"+ chatRoom.getChatRoomID(),"self","GET" );
        ActionLinks actionB = new ActionLinks("/chatrooms/"+ chatRoom.getChatRoomID(),"self","PUT" );
        ActionLinks actionC = new ActionLinks("/chatrooms/"+ chatRoom.getChatRoomID() +"/chatmessages","postmessage","POST" );
        ActionLinks actionD = new ActionLinks("/chatrooms","create","POST" );
        ActionLinks actionE = new ActionLinks("/chatrooms","getall","GET" );
        listOfActionLinks.add(actionA);
        listOfActionLinks.add(actionB);
        listOfActionLinks.add(actionC);
        listOfActionLinks.add(actionD);
        listOfActionLinks.add(actionE);
        chatRoom.setNextActionLinks(listOfActionLinks);

        return chatRoom;
    }





}







