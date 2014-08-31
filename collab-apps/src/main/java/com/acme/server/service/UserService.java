package com.acme.server.service;

import com.acme.server.model.User;
import com.acme.server.util.DatabaseManager;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  The UserService is invoked by the UserController & performs the necessary logic to create & retrieve new user accounts
 *  in the system & modify certain aspects of them (nickname)
 *
 *  A real world implementation would include other admin like functionality such as retrieving
 *  users by org or domain etc & integrating with the businesses address book services etc
 *
 *  For the purposed of this prototype, they are simply used to ensure that there are real users in any given ChatRoom which
 *  could ultimately support multiple users (the demo is contained to 2).
 */

public class UserService {

    private DatabaseManager databaseManager;
    private static Logger log = Logger.getLogger(MessagingService.class);

    public UserService() {
        databaseManager = new DatabaseManager();
    }

    /**
     * Retrieves & Returns the List of all Users in the system
     *
     * The User resource is a subresource of the ChatRoom & ChatMessage resources for ACME Collaboration Services.
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions.
     * This would be locked down to a user with admin auth privs in a real world deployment for security purposes etc.
     *
     * @return      <code>List</code>
     */
    public List<User> retrieveAllUsers(){
        log.info("Entering UserService.retrieveAllUsers()");
        return databaseManager.getAllUsers();
    }

    /**
     * Returns the User with the specified ID from the Database
     *
     * The User resource is a subresource of the ChatRoom & ChatMessage resources for ACME Collaboration Services.
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions.
     * This would be locked down to a user with admin auth privs in a real world deployment for security purposes etc.
     *
     * @param       id - User ID to retrieve
     * @return      <code>User</code> The User Resource retrieved
     */
    public User retrieveExistingUser(String id){
        log.info("Entering retrieveExistingUser: " + id);
        return databaseManager.getUser(id);
    }

    /**
     * Creates a new User from the JSON Body input by the API consumer & pushes it to the Database
     *
     * @param       json - The JSON Body passed in from the API Consumer
     * @return      user -  The User object that is getting returned to the API Consumer
     */

    public User createUserFromJSON(String json ){
        log.info("Entering createUserFromJSON: " + json);

        //Create empty User object to send to Redis using combination of JSON input & ACME business constraints
        User user = new User();
        Gson gsonFromJSON = new Gson();

        //Populate the User object directly using data from the JSON representation
        user = gsonFromJSON.fromJson(json, user.getClass());
        log.info("User object constructed from JSON representation");

        //Manipulate the User object to enforce ACME business constraints
        //e.g. UUID is a system generated identified to guarantee uniqueness & therefore cannot be set by a consumer
        user.setUserID("USER:" + UUID.randomUUID().toString());

        //Log the User Object about to be pushed to Redis
        log.info("User ID: " + user.getUserID());
        log.info("User FirstName: " + user.getFirstName());
        log.info("User LastName: " + user.getLastName());
        log.info("User NickName: " + user.getNickname());

        //Call out to the redisManager to store the data in Redis
        databaseManager.setUser(user);

        //return the representation of the object stored (not what we think we stored incase there is inconsistency)
        return user;
    }


    /**
     * Modifies the User with the detail specified in the JSON body & stores the modification in the database
     * Only the 'NickName' of a User is modifiable after creation so all other fields input in JSON body get ignored.
     *
     * @param       json - Contains the detail of the modification to make
     * @return      <code>User</code> The Representation of the updated Resource
     */
    public User modifyUserFromJSON(String json ){
        log.info("Entering modifyUserFromJSON: " + json);

        //Create empty User object to send to Redis using combination of JSON input & ACME business constraints
        User user = new User();
        Gson gsonFromJSON = new Gson();

        //Populate the User object directly using data from the JSON representation
        user = gsonFromJSON.fromJson(json, user.getClass());
        log.info("User object constructed from JSON representation");

        //Log the User Object as constructed from JSON input
        log.info("User ID: " + user.getUserID());
        log.info("User FirstName: " + user.getFirstName());
        log.info("User LastName: " + user.getLastName());
        log.info("User NickName: " + user.getNickname());

        //Retrieve the existing resource representation from Redis (validate the UserID input is valid before doing anything else)
        User existingUser = databaseManager.getUser(user.getUserID());

        //NOTE: Redis is optimised for insertions & deletions, not updates - may be better to delete the existing
        //entry & create a new one rather than trying to update an existing one.

        //Overwrite the current nickname with the one from the JSON body
        existingUser.setNickname(user.getNickname());

        //Call out to the redisManager to update the data in Redis using the same value
        //will have the same key as one that already exists
        databaseManager.setUser(existingUser);

        //return the representation of the object as stored (not what we think we stored)
        return databaseManager.getUser(user.getUserID());
    }


    /**
     * Creates a number of test users
     *
     * @param       numTestUsers - The number of test Users to create //NOTE: need to throttle this in a real world
     * @return      listOfTestUsers - The list of new users created
     */
    public List<User> createTestUsers(int numTestUsers){
        List listOfTestUsers = new ArrayList();
        for (int i= 0;i<numTestUsers;i++){
            User testUser = new User("Test User First Name" + i, "Test User Last Name" + i, "Test User NickName" + i);

            databaseManager.setUser(testUser);
            listOfTestUsers.add(testUser);
        }

        return listOfTestUsers;
    }

}