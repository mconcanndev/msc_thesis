package com.acme.server.controller;

import com.acme.server.model.User;
import com.acme.server.service.UserService;
import com.acme.server.util.ActionLinks;
import com.acme.server.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  The UserController is the generic REST entry point for provisioning new users on the system
 *  Supports GET / PUT / POST on the User Resource
 *
 *  In a real world implementation, Authorised User creation & retrieval would typically be locked down & accessed only
 *  by administrators but there may be a series of use cases where certain users have privileges to invite other
 *  users to use the system & this endpoint may be opened up based on the correct level of Auth access.
 *
 *  A User, once created, can use the endpoint to modify certain aspects of their user account e.g. nickname.
 *  A real world scenario would impose Auth checks on any endpoint but this type in particular
 *
 *  The UserController converts the InputStreams from clients into a String that contains the JSON body.
 *  Any parameters supported by the Endpoint are also handled by the controller & passed along to the Service layer accordingly.
 *  The actual business logic & processing is handed off to the UserService layer & when complete, the controller
 *  does the work to set the next set of valid ActionLinks on the Resource representation to ensure the API conforms fully
 *  to HATEOAS.
 *
 */

@RestController
public class UserController {

    private static Logger log = Logger.getLogger(UserController.class);
    private static UserService userService= new UserService();

    /**
     * Returns the full list of User resources representing the list of provisioned users
     * Two or more User resources are associated with the ChatRoom & Meeting Resources but exist independent of the
     * Messaging services in Acme's overall model so are accessed at the root level
     *
     * Accessible via a GET on /users
     * Add some @parameters to filter by Domain or Org at a later stage which would be an additional element of the
     * User Resource in a future iteration
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions
     *
     * GET /users/{id} - GET a specific user resource for further manipulation
     * PUT /users/{id} - Modify the User resource (the nickname which is the only mutable data)
     *
     * @return      <code>List</code>
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> getAllUsers() {
        log.info("Entering GET /users");

        List <User> listOfUsers = userService.retrieveAllUsers();

        //Set the action list for each User found at the controller level so can base the allowable actions on the
        //endpoint being invoked
        for(int i = 0;i<listOfUsers.size();i++){
            User nextUser = listOfUsers.get(i);

            List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
            ActionLinks nextActionA = new ActionLinks("/users/"+nextUser.getUserID(),"self","GET" );
            ActionLinks nextActionB = new ActionLinks("/users/"+nextUser.getUserID(),"self","PUT" );
            listOfActionLinks.add(nextActionA);
            listOfActionLinks.add(nextActionB);
            nextUser.setNextActionLinks(listOfActionLinks);
        }

        return listOfUsers;
    }
    /**
     * Returns the User resource representation with the specific id in the URL
     * Accessible via a GET on http://localhost:8080/user/{id}
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable action
     *
     * PUT  /users/{id} - modify the user (nickname, most User Attributes are not modifyable)
     *
     * @return      <code> User </code>
     */
    @RequestMapping(value = "/users/{id}",method = RequestMethod.GET)
    public User retrieveUser(@PathVariable("id") String id) {
        log.info("Entering GET /users");

        User newUser = userService.retrieveExistingUser(id);

        log.info("Setting Next Action links: ");
        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
        ActionLinks newNextAction = new ActionLinks("/users/"+newUser.getUserID(),"self","PUT" );
        listOfActionLinks.add(newNextAction);

        newUser.setNextActionLinks(listOfActionLinks);
        return newUser;
    }


    /**
     * Creates a new User resource from the representation provided in the JSON body & stores in the Database for
     * use accross the Acme system (i.e. Users are not specific to Messaging services, they are equally applicable to
     * the unimplemented Meeting services
     *
     * Accessible via a POST on /users
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS
     *
     * NOTE: This endpoint is overloaded to create a set of test users for the purposes of the research & again, is only
     * discoverable by the documentation provided here, not as a result of full compliance to HATEOAS.
     *
     * GET  /users/{id} - perform a GET on the newly created User ID returned in the JSON Body
     * PUT  /users/{id} - modify the newly created user (set nickname)
     *
     * @param       "test"=true/false - if set, will create a list of test users without any JSON body
     * @param       "num"=int - if set AND if "test" is true, will be used to determine how many test users to create
     * @return      <code> User </code>
     */
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public User createNewUser(InputStream data, @RequestParam(value="test", required=false, defaultValue = "false") String testUsers,
                              @RequestParam(value="num", required=false, defaultValue="2") Integer numTestUsers) {
        log.info("Entering POST /users");

        //Extract incoming json to construct a User Resource from the representation input
        String json = StringUtils.InputStringToString(data);
        log.info("POST data: " + json);

        if(testUsers.equals("true")){
            log.info("Test Parameter set to true. Creating " + numTestUsers + " new Users");
            userService.createTestUsers(numTestUsers.intValue());

            return null;
        }

        else {
            //Call out to the UserService Class to build & store the new user in the database after applying all
            //relevant business constraints on what the consumer is allowed to manipulate
            User newUser = userService.createUserFromJSON(json);

            //Set the next action links at the controller level before returning the JSON to the API consumer
            //NOTE: These links are not relevant anywhere else in the business logic & are part of the resource representations
            //only to implement HATEOAS
            log.info("Setting Next Action links: ");
            List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
            ActionLinks newNextAction = new ActionLinks("/users/"+newUser.getUserID(),"self","GET" );
            listOfActionLinks.add(newNextAction);

            log.info("Setting the List: ");
            newUser.setNextActionLinks(listOfActionLinks);
            return newUser;
        }
    }

    /**
     * Modifies an existing User resource from the representation provided in the JSON body & stores in the Database
     *
     * Accessible via a PUT on /users/{id}
     * Mutable data on the User Resource is confined to NickName for this first phase of ACME development.
     *
     * Note: As the User resource is global, changing it here will take effect across all services where a User
     * Resource is used.
     *
     * The JSON object returned includes a set of Action Links that represent the next set of allowable actions in
     * order to conform to HATEOAS
     *
     * GET  /users/{id} - get the modified user
     * PUT  /users/{id} - modify the nickname again
     *
     * @return      <code> User </code>
     */
    @RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
    public User modifyUser(InputStream data,@PathVariable("id") String id) {
        log.info("Entering PUT /users/" + id);

        //Extract incoming json to determine what fields in the User Resource the consumer wants modified
        String json = StringUtils.InputStringToString(data);
        log.info("PUT data: " + json);

        //Call out to the UserService Class to build & store the new user in the database after applying all
        //relevant business constraints on what the consumer is allowed to manipulate
        User modifiedUser = userService.modifyUserFromJSON(json);

        //Set the next action links at the controller level before returning the JSON to the API consumer
        log.info("Setting Next Action links: ");
        List<ActionLinks> listOfActionLinks = new ArrayList<ActionLinks>();
        ActionLinks nextActionA = new ActionLinks("/users/"+modifiedUser.getUserID(),"self","GET" );
        ActionLinks nextActionB = new ActionLinks("/users/"+modifiedUser.getUserID(),"self","PUT" );
        listOfActionLinks.add(nextActionA);
        listOfActionLinks.add(nextActionB);

        modifiedUser.setNextActionLinks(listOfActionLinks);
        return modifiedUser;

    }



}